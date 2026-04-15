package com.example.appdraw.community;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdraw.R;
import com.example.appdraw.model.Event;
import com.example.appdraw.model.EventTicket;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventTicketActivity extends AppCompatActivity {

    private ImageView btnBack, btnShare, ivQrCode;
    private TextView tvTitle, tvFormat, tvTime, tvLocation, tvTicketCode, tvTicketFooterWarning;
    private View btnViewMap, btnRemindMe, btnJoinZoom;
    private ImageView ivRemindIcon;
    private TextView tvRemindText;

    private String eventId;
    private String ticketId;
    private Event currentEvent;
    
    private SharedPreferences prefs;
    private boolean isReminded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_ticket);

        eventId = getIntent().getStringExtra("EVENT_ID");
        ticketId = getIntent().getStringExtra("TICKET_ID");
        prefs = getSharedPreferences("EventReminders", Context.MODE_PRIVATE);
        
        if (eventId != null) {
            isReminded = prefs.getBoolean("remind_" + eventId, false);
        }

        initViews();
        setupListeners();
        updateRemindUI();
        
        if (eventId != null) {
            loadTicketData();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back_ticket);
        btnShare = findViewById(R.id.btn_share_ticket);
        tvTitle = findViewById(R.id.tv_ticket_event_title);
        tvFormat = findViewById(R.id.tv_ticket_format);
        tvTime = findViewById(R.id.tv_ticket_time);
        tvLocation = findViewById(R.id.tv_ticket_location);
        tvTicketCode = findViewById(R.id.tv_ticket_code);
        ivQrCode = findViewById(R.id.iv_qr_code);
        tvTicketFooterWarning = findViewById(R.id.tv_ticket_footer_warning);
        btnViewMap = findViewById(R.id.btn_view_map);
        btnJoinZoom = findViewById(R.id.btn_join_zoom);
        btnRemindMe = findViewById(R.id.btn_remind_me);
        ivRemindIcon = findViewById(R.id.iv_remind_icon);
        tvRemindText = findViewById(R.id.tv_remind_text);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Tôi đã đăng ký sự kiện thành công trên App Draw!");
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ vé"));
        });

        btnViewMap.setOnClickListener(v -> {
            if (currentEvent != null && currentEvent.getLocation() != null) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(currentEvent.getLocation()));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    String url = "https://www.google.com/maps/search/?api=1&query=" + Uri.encode(currentEvent.getLocation());
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            }
        });

        btnRemindMe.setOnClickListener(v -> {
            if (currentEvent != null) {
                isReminded = !isReminded;
                prefs.edit().putBoolean("remind_" + eventId, isReminded).apply();
                
                if (isReminded) {
                    // Mở dialog thông báo
                    showRemindSuccessDialog();

                    // Optional: Push to Calendar Intent as well
                    long beginTimeMillis = currentEvent.getDateMillis();
                    long endTimeMillis = beginTimeMillis + 3600000; // default +1h
                    try {
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTimeInMillis(beginTimeMillis);
                        if (currentEvent.getStartTime() != null && currentEvent.getStartTime().contains(":")) {
                            String[] parts = currentEvent.getStartTime().split(":");
                            cal.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0].trim()));
                            cal.set(java.util.Calendar.MINUTE, Integer.parseInt(parts[1].trim()));
                            beginTimeMillis = cal.getTimeInMillis();
                        }
                        
                        java.util.Calendar calEnd = java.util.Calendar.getInstance();
                        calEnd.setTimeInMillis(currentEvent.getDateMillis());
                        if (currentEvent.getEndTime() != null && currentEvent.getEndTime().contains(":")) {
                            String[] parts = currentEvent.getEndTime().split(":");
                            calEnd.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0].trim()));
                            calEnd.set(java.util.Calendar.MINUTE, Integer.parseInt(parts[1].trim()));
                            endTimeMillis = calEnd.getTimeInMillis();
                        } else {
                            endTimeMillis = beginTimeMillis + 3600000;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(Intent.ACTION_INSERT)
                            .setData(CalendarContract.Events.CONTENT_URI)
                            .putExtra(CalendarContract.Events.TITLE, currentEvent.getTitle())
                            .putExtra(CalendarContract.Events.EVENT_LOCATION, currentEvent.getLocation())
                            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTimeMillis)
                            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeMillis)
                            .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    updateRemindUI();
                }
            }
        });
    }

    private void showRemindSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_remind_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        
        View btnOk = dialog.findViewById(R.id.btn_dialog_ok);
        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            updateRemindUI();
        });
        
        dialog.show();
    }

    private void updateRemindUI() {
        if (isReminded) {
            btnRemindMe.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E53935"))); // Nền đỏ
            tvRemindText.setText("Tắt nhắc");
            tvRemindText.setTextColor(Color.WHITE);
            // Replace with a crossed bell icon if available, or just tint it white
            ivRemindIcon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        } else {
            btnRemindMe.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
            tvRemindText.setText("Nhắc tôi");
            tvRemindText.setTextColor(Color.parseColor("#333333"));
            ivRemindIcon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#333333")));
        }
    }

    private void loadTicketData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("Events").document(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                currentEvent = doc.toObject(Event.class);
                if (currentEvent != null) {
                    tvTitle.setText(currentEvent.getTitle());
                    tvFormat.setText(currentEvent.isOnline() ? "Online" : "Offline");
                    
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTimeInMillis(currentEvent.getDateMillis());
                    int day = cal.get(java.util.Calendar.DAY_OF_MONTH);
                    int month = cal.get(java.util.Calendar.MONTH) + 1;
                    int dow = cal.get(java.util.Calendar.DAY_OF_WEEK);
                    String dayOfWeekStr = getDayOfWeek(dow);
                    
                    tvTime.setText(currentEvent.getStartTime() + " - " + currentEvent.getEndTime() + " - " + dayOfWeekStr + ", " + day + " Th" + month);
                    if (currentEvent.isOnline()) {
                        tvLocation.setText("Phòng học trực tiếp qua nền tảng Zoom / Meet");
                        tvFormat.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E8F5E9")));
                        tvFormat.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                        
                        btnViewMap.setVisibility(View.GONE);
                        btnJoinZoom.setVisibility(View.VISIBLE);
                        
                        if (ivQrCode != null) ivQrCode.setVisibility(View.GONE);
                        if (tvTicketCode != null) tvTicketCode.setVisibility(View.GONE);
                        if (tvTicketFooterWarning != null) tvTicketFooterWarning.setVisibility(View.GONE);
                        
                        btnJoinZoom.setOnClickListener(v -> {
                            if (currentEvent.getZoomLink() != null && !currentEvent.getZoomLink().isEmpty()) {
                                if (currentEvent.getZoomPasscode() != null && !currentEvent.getZoomPasscode().isEmpty()) {
                                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    android.content.ClipData clip = android.content.ClipData.newPlainText("Passcode", currentEvent.getZoomPasscode());
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(EventTicketActivity.this, "Đã sao chép Passcode: " + currentEvent.getZoomPasscode(), Toast.LENGTH_LONG).show();
                                }
                                
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentEvent.getZoomLink()));
                                startActivity(intent);
                            } else {
                                Toast.makeText(EventTicketActivity.this, "Chưa có đường dẫn cho sự kiện này", Toast.LENGTH_SHORT).show();
                            }
                        });
                        
                    } else {
                        tvLocation.setText(currentEvent.getLocation());
                        tvFormat.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFEBEE")));
                        tvFormat.setTextColor(android.graphics.Color.parseColor("#E53935"));
                        
                        btnJoinZoom.setVisibility(View.GONE);
                        btnViewMap.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        
        if (ticketId != null && !ticketId.isEmpty()) {
            db.collection("EventRegistrations").document(ticketId).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    EventTicket ticket = doc.toObject(EventTicket.class);
                    if (ticket != null) {
                        tvTicketCode.setText("Mã vé : " + ticket.getTicketCode());
                    }
                }
            });
        }
    }
    
    private String getDayOfWeek(int dow) {
        switch (dow) {
            case java.util.Calendar.MONDAY: return "Thứ 2";
            case java.util.Calendar.TUESDAY: return "Thứ 3";
            case java.util.Calendar.WEDNESDAY: return "Thứ 4";
            case java.util.Calendar.THURSDAY: return "Thứ 5";
            case java.util.Calendar.FRIDAY: return "Thứ 6";
            case java.util.Calendar.SATURDAY: return "Thứ 7";
            case java.util.Calendar.SUNDAY: return "CN";
            default: return "";
        }
    }
}
