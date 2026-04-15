package com.example.appdraw.community;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdraw.R;
import com.example.appdraw.model.Event;
import com.example.appdraw.model.EventTicket;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventScheduleActivity extends AppCompatActivity {

    private ImageView btnBack, btnAddEvent;
    private TextView tvTabSchedule, tvTabExplore;
    private View containerSchedule, containerExplore;

    // Explore Container Views
    private TextView tvFilterLatest, tvFilterOnline, tvFilterOffline;
    private RecyclerView rvExploreEvents;
    private ExploreEventAdapter exploreEventAdapter;
    private List<Event> allEventsList = new ArrayList<>();
    private List<Event> exploreEventsList = new ArrayList<>();
    private String currentExploreFilter = "Mới nhất";

    // Schedule Container Views
    private TextView tvCalendarMonth, tvSelectedDateLabel;
    private ImageView btnPrevWeek, btnNextWeek;
    private RecyclerView rvHorizontalCalendar;
    private RecyclerView rvScheduleEvents;
    private HorizontalCalendarAdapter calendarAdapter;
    private ScheduleEventAdapter scheduleEventAdapter;
    private List<Calendar> currentWeekList = new ArrayList<>();
    private Calendar selectedCalendar;
    
    private List<EventTicket> myTickets = new ArrayList<>();
    private List<Event> myScheduleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_schedule);
        
        selectedCalendar = Calendar.getInstance();

        initViews();
        setupTabs();
        setupExploreContainer();
        setupScheduleContainer();
        
        checkMentorRole();
        fetchMyTickets();
        fetchAllEvents();

        boolean openExplore = getIntent().getBooleanExtra("OPEN_EXPLORE", false);
        if (openExplore) {
            tvTabExplore.performClick();
        } else {
            tvTabSchedule.performClick();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back_events);
        btnAddEvent = findViewById(R.id.btn_add_event);
        tvTabSchedule = findViewById(R.id.tv_tab_schedule);
        tvTabExplore = findViewById(R.id.tv_tab_explore);
        containerSchedule = findViewById(R.id.container_schedule);
        containerExplore = findViewById(R.id.container_explore);

        btnBack.setOnClickListener(v -> finish());
        btnAddEvent.setOnClickListener(v -> startActivity(new Intent(this, CreateEventActivity.class)));
    }

    private void checkMentorRole() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("Users").document(uid).get().addOnSuccessListener(d -> {
                if (d.exists() && "mentor".equals(d.getString("role"))) {
                    btnAddEvent.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void setupTabs() {
        tvTabSchedule.setOnClickListener(v -> {
            tvTabSchedule.setBackgroundResource(R.drawable.rounded_bg_white);
            tvTabSchedule.setTextColor(Color.parseColor("#333333"));
            tvTabExplore.setBackgroundResource(0);
            tvTabExplore.setTextColor(Color.parseColor("#757575"));
            
            containerSchedule.setVisibility(View.VISIBLE);
            containerExplore.setVisibility(View.GONE);
        });

        tvTabExplore.setOnClickListener(v -> {
            tvTabExplore.setBackgroundResource(R.drawable.rounded_bg_white);
            tvTabExplore.setTextColor(Color.parseColor("#333333"));
            tvTabSchedule.setBackgroundResource(0);
            tvTabSchedule.setTextColor(Color.parseColor("#757575"));
            
            containerSchedule.setVisibility(View.GONE);
            containerExplore.setVisibility(View.VISIBLE);
        });
    }

    // ================= EXPLORE LOGIC =================
    private void setupExploreContainer() {
        tvFilterLatest = findViewById(R.id.tv_filter_latest);
        tvFilterOnline = findViewById(R.id.tv_filter_online);
        tvFilterOffline = findViewById(R.id.tv_filter_offline);

        View.OnClickListener filterListener = v -> {
            tvFilterLatest.setBackgroundTintList(ColorStateListHelper.white());
            tvFilterLatest.setTextColor(Color.parseColor("#333333"));
            tvFilterOnline.setBackgroundTintList(ColorStateListHelper.white());
            tvFilterOnline.setTextColor(Color.parseColor("#333333"));
            tvFilterOffline.setBackgroundTintList(ColorStateListHelper.white());
            tvFilterOffline.setTextColor(Color.parseColor("#333333"));

            TextView tv = (TextView) v;
            tv.setBackgroundTintList(ColorStateListHelper.blue());
            tv.setTextColor(Color.WHITE);
            currentExploreFilter = tv.getText().toString();
            filterExploreEvents();
        };

        tvFilterLatest.setOnClickListener(filterListener);
        tvFilterOnline.setOnClickListener(filterListener);
        tvFilterOffline.setOnClickListener(filterListener);

        rvExploreEvents = findViewById(R.id.rv_explore_events);
        rvExploreEvents.setLayoutManager(new LinearLayoutManager(this));
        exploreEventAdapter = new ExploreEventAdapter();
        rvExploreEvents.setAdapter(exploreEventAdapter);
    }

    private void fetchAllEvents() {
        FirebaseFirestore.getInstance().collection("Events")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    allEventsList.clear();
                    long now = System.currentTimeMillis();
                    for (DocumentSnapshot doc : value) {
                        Event e = doc.toObject(Event.class);
                        if (e != null) {
                            boolean isExpired = false;
                            try {
                                if (e.getEndTime() != null && e.getEndTime().contains(":")) {
                                    String[] parts = e.getEndTime().split(":");
                                    int hour = Integer.parseInt(parts[0].trim());
                                    int min = Integer.parseInt(parts[1].trim());
                                    java.util.Calendar cal = java.util.Calendar.getInstance();
                                    cal.setTimeInMillis(e.getDateMillis());
                                    cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
                                    cal.set(java.util.Calendar.MINUTE, min);
                                    if (cal.getTimeInMillis() < now) isExpired = true;
                                } else if (e.getDateMillis() + 24 * 60 * 60 * 1000L < now) {
                                    isExpired = true;
                                }
                            } catch (Exception ex) {
                                if (e.getDateMillis() + 24 * 60 * 60 * 1000L < now) isExpired = true;
                            }
                            if (!isExpired) {
                                allEventsList.add(e);
                            }
                        }
                    }
                    filterExploreEvents();
                    updateScheduleViewForSelectedDate(); // Also updates schedule
                });
    }

    private void filterExploreEvents() {
        exploreEventsList.clear();
        for (Event e : allEventsList) {
            if ("Mới nhất".equals(currentExploreFilter)) {
                exploreEventsList.add(e);
            } else if ("Online".equals(currentExploreFilter) && e.isOnline()) {
                exploreEventsList.add(e);
            } else if ("Offline".equals(currentExploreFilter) && !e.isOnline()) {
                exploreEventsList.add(e);
            }
        }
        exploreEventAdapter.notifyDataSetChanged();
    }

    // ================= SCHEDULE LOGIC =================
    private void setupScheduleContainer() {
        tvCalendarMonth = findViewById(R.id.tv_calendar_month);
        tvSelectedDateLabel = findViewById(R.id.tv_selected_date_label);
        btnPrevWeek = findViewById(R.id.btn_prev_week);
        btnNextWeek = findViewById(R.id.btn_next_week);
        rvHorizontalCalendar = findViewById(R.id.rv_horizontal_calendar);
        rvScheduleEvents = findViewById(R.id.rv_schedule_events);

        rvHorizontalCalendar.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 7));
        calendarAdapter = new HorizontalCalendarAdapter();
        rvHorizontalCalendar.setAdapter(calendarAdapter);

        rvScheduleEvents.setLayoutManager(new LinearLayoutManager(this));
        scheduleEventAdapter = new ScheduleEventAdapter();
        rvScheduleEvents.setAdapter(scheduleEventAdapter);

        calculateWeekOffset(0);

        btnPrevWeek.setOnClickListener(v -> calculateWeekOffset(-1));
        btnNextWeek.setOnClickListener(v -> calculateWeekOffset(1));
    }
    
    private void fetchMyTickets() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("EventRegistrations")
                    .whereEqualTo("userId", uid)
                    .addSnapshotListener((value, error) -> {
                        if (error != null || value == null) return;
                        myTickets.clear();
                        for (DocumentSnapshot doc : value) {
                            EventTicket t = doc.toObject(EventTicket.class);
                            if (t != null) myTickets.add(t);
                        }
                        updateScheduleViewForSelectedDate();
                    });
        }
    }

    private void calculateWeekOffset(int offsetFactor) {
        if (offsetFactor != 0) {
            selectedCalendar.add(Calendar.DAY_OF_YEAR, offsetFactor * 28);
        }
        
        tvCalendarMonth.setText("Tháng " + (selectedCalendar.get(Calendar.MONTH) + 1) + ", " + selectedCalendar.get(Calendar.YEAR));
        
        Calendar weekStart = (Calendar) selectedCalendar.clone();
        int dow = weekStart.get(Calendar.DAY_OF_WEEK);
        if (dow == Calendar.SUNDAY) {
            weekStart.add(Calendar.DAY_OF_YEAR, -6);
        } else {
            weekStart.add(Calendar.DAY_OF_YEAR, - (dow - Calendar.MONDAY));
        }

        currentWeekList.clear();
        for (int i = 0; i < 28; i++) {
            Calendar day = (Calendar) weekStart.clone();
            day.add(Calendar.DAY_OF_YEAR, i);
            currentWeekList.add(day);
        }
        
        calendarAdapter.notifyDataSetChanged();
        updateScheduleViewForSelectedDate();
    }

    private void updateScheduleViewForSelectedDate() {
        Calendar today = Calendar.getInstance();
        boolean isToday = today.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
                          today.get(Calendar.DAY_OF_YEAR) == selectedCalendar.get(Calendar.DAY_OF_YEAR);
        
        String prefix = isToday ? "Hôm nay, " : "Ngày ";
        tvSelectedDateLabel.setText(prefix + selectedCalendar.get(Calendar.DAY_OF_MONTH) + " tháng " + (selectedCalendar.get(Calendar.MONTH) + 1));
        
        myScheduleList.clear();
        
        // Find events that match tickets and this day
        String uid = FirebaseAuth.getInstance().getUid();
        
        for (Event e : allEventsList) {
            boolean hasTicket = false;
            for (EventTicket t : myTickets) {
                if (t.getEventId().equals(e.getId())) {
                    hasTicket = true;
                    break;
                }
            }
            boolean isAuthor = uid != null && uid.equals(e.getAuthorId());
            
            if (!hasTicket && !isAuthor && !("Live".equals(e.getEventType()))) {
                continue; // Include Live by default, authored events, and ticketed events
            }
            
            // Check if day matches
            Calendar eventCal = Calendar.getInstance();
            eventCal.setTimeInMillis(e.getDateMillis());
            if (eventCal.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
                eventCal.get(Calendar.DAY_OF_YEAR) == selectedCalendar.get(Calendar.DAY_OF_YEAR)) {
                myScheduleList.add(e);
            }
        }
        scheduleEventAdapter.notifyDataSetChanged();
    }

    // ================= HELPER & DIALOG =================
    private void registerEvent(Event event) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        
        // Check if already registered
        for (EventTicket t : myTickets) {
            if (t.getEventId().equals(event.getId())) {
                Toast.makeText(this, "Bạn đã đăng ký sự kiện này rồi", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        String ticketId = FirebaseFirestore.getInstance().collection("EventRegistrations").document().getId();
        String ticketCode = "TKT" + String.format("%04d", (int)(Math.random() * 10000));
        EventTicket ticket = new EventTicket(ticketId, event.getId(), uid, ticketCode, System.currentTimeMillis());
        
        FirebaseFirestore.getInstance().collection("EventRegistrations").document(ticketId)
                .set(ticket)
                .addOnSuccessListener(aVoid -> {
                    myTickets.add(ticket);
                    if (exploreEventAdapter != null) exploreEventAdapter.notifyDataSetChanged();
                    showSuccessDialog(event, ticket);
                    
                    // Gửi thông báo cho tác giả (Author)
                    if (!event.getAuthorId().equals(uid)) {
                        com.example.appdraw.utils.NotificationHelper.sendNotification(event.getAuthorId(), "EVENT", "Một người dùng vừa đăng ký sự kiện: " + event.getTitle(), event.getId());
                    }
                    // Thông báo hệ thống cho người đăng ký
                    com.example.appdraw.utils.NotificationHelper.sendNotification(uid, "EVENT", "Bạn đã đăng ký thành công sự kiện: " + event.getTitle(), event.getId());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi đăng ký", Toast.LENGTH_SHORT).show());
    }

    private void showSuccessDialog(Event event, EventTicket ticket) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_event_registered);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        
        TextView tvTitle = dialog.findViewById(R.id.tv_dialog_event_title);
        TextView tvTime = dialog.findViewById(R.id.tv_dialog_event_time);
        TextView tvLocation = dialog.findViewById(R.id.tv_dialog_event_location);
        TextView tvFormat = dialog.findViewById(R.id.tv_dialog_event_format);
        TextView tvPrice = dialog.findViewById(R.id.tv_dialog_event_price);
        View btnViewTicket = dialog.findViewById(R.id.btn_dialog_view_ticket);
        View btnBackSchedule = dialog.findViewById(R.id.btn_dialog_back_schedule);

        tvTitle.setText(event.getTitle());
        
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(event.getDateMillis());
        tvTime.setText(event.getStartTime() + " - " + event.getEndTime() + " - " + cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH)+1));
        
        tvLocation.setText(event.getLocation());
        tvFormat.setText(event.isOnline() ? "Online" : "Offline");
        tvPrice.setText(event.getPrice());

        btnViewTicket.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, EventTicketActivity.class);
            intent.putExtra("EVENT_ID", event.getId());
            intent.putExtra("TICKET_ID", ticket.getId());
            startActivity(intent);
        });
        
        btnBackSchedule.setOnClickListener(v -> {
            dialog.dismiss();
            // Switch to schedule tab
            tvTabSchedule.performClick();
        });

        dialog.show();
    }

    // ================= ADAPTERS =================
    private class HorizontalCalendarAdapter extends RecyclerView.Adapter<HorizontalCalendarAdapter.VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Calendar day = currentWeekList.get(position);
            
            holder.tvDate.setText(String.valueOf(day.get(Calendar.DAY_OF_MONTH)));
            switch (day.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.MONDAY: holder.tvDow.setText("MON"); break;
                case Calendar.TUESDAY: holder.tvDow.setText("TUE"); break;
                case Calendar.WEDNESDAY: holder.tvDow.setText("WED"); break;
                case Calendar.THURSDAY: holder.tvDow.setText("THU"); break;
                case Calendar.FRIDAY: holder.tvDow.setText("FRI"); break;
                case Calendar.SATURDAY: holder.tvDow.setText("SAT"); break;
                case Calendar.SUNDAY: holder.tvDow.setText("SUN"); break;
            }
            
            boolean isSelected = day.get(Calendar.DAY_OF_YEAR) == selectedCalendar.get(Calendar.DAY_OF_YEAR) &&
                                 day.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR);
                                 
            if (isSelected) {
                holder.tvDate.setBackgroundResource(R.drawable.rounded_bg_red); 
                holder.tvDate.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E53935")));
                holder.tvDate.setTextColor(Color.WHITE);
                holder.tvDow.setTextColor(Color.parseColor("#E53935"));
            } else {
                holder.tvDate.setBackgroundResource(0);
                holder.tvDate.setBackgroundTintList(null);
                holder.tvDate.setTextColor(Color.parseColor("#333333"));
                holder.tvDow.setTextColor(Color.parseColor("#AAAAAA"));
            }

            if (day.get(Calendar.MONTH) != selectedCalendar.get(Calendar.MONTH)) {
                holder.tvDate.setAlpha(0.3f);
                holder.tvDow.setAlpha(0.3f);
            } else {
                holder.tvDate.setAlpha(1.0f);
                holder.tvDow.setAlpha(1.0f);
            }
            
            holder.itemView.setOnClickListener(v -> {
                selectedCalendar = (Calendar) day.clone();
                notifyDataSetChanged();
                updateScheduleViewForSelectedDate();
            });
        }
        @Override
        public int getItemCount() { return currentWeekList.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvDow, tvDate;
            VH(@NonNull View itemView) {
                super(itemView);
                tvDow = itemView.findViewById(R.id.tv_day_of_week);
                tvDate = itemView.findViewById(R.id.tv_date_number);
            }
        }
    }

    private class ScheduleEventAdapter extends RecyclerView.Adapter<ScheduleEventAdapter.VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_schedule, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Event e = myScheduleList.get(position);
            holder.tvTitle.setText(e.getTitle());
            String endTimeStr = e.getEndTime();
            if (endTimeStr != null && !endTimeStr.isEmpty()) {
                holder.tvTime.setText(e.getStartTime() + " - " + endTimeStr);
            } else {
                holder.tvTime.setText(e.getStartTime());
            }

            FirebaseFirestore.getInstance().collection("Users").document(e.getAuthorId())
                .get().addOnSuccessListener(doc -> {
                    if (doc.exists() && holder.tvSubtitle != null) {
                        java.util.Map<String, Object> profile = (java.util.Map<String, Object>) doc.get("profile");
                        String fullName = "Người ẩn danh";
                        if (profile != null && profile.containsKey("fullName")) {
                            fullName = (String) profile.get("fullName");
                        }
                        holder.tvSubtitle.setText(fullName + " - " + (e.isOnline() ? "Online" : "Offline"));
                    }
                });
            
            if ("Live".equals(e.getEventType())) {
                holder.tvBadge.setText("Live");
                holder.tvBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E53935")));
                holder.btnAction.setText("Tham gia ngay");
                holder.btnAction.setOnClickListener(v -> {
                    Toast.makeText(EventScheduleActivity.this, "Đang vào phòng Live...", Toast.LENGTH_SHORT).show();
                });
            } else {
                holder.tvBadge.setText("Workshop");
                holder.tvBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F57C00")));
                
                String uid = FirebaseAuth.getInstance().getUid();
                boolean isAuthor = uid != null && uid.equals(e.getAuthorId());
                
                if (isAuthor) {
                    holder.btnAction.setText("Của bạn");
                    holder.btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#888888")));
                    holder.btnAction.setOnClickListener(v -> Toast.makeText(EventScheduleActivity.this, "Bạn là nhà tổ chức (Đang phát triển)", Toast.LENGTH_SHORT).show());
                } else {
                    String myTicketId = null;
                    for (EventTicket t : myTickets) {
                        if (t.getEventId().equals(e.getId())) myTicketId = t.getId();
                    }
                    if (myTicketId != null) {
                        holder.btnAction.setText("Xem vé");
                        String finalMyTicketId = myTicketId;
                        holder.btnAction.setOnClickListener(v -> {
                            Intent intent = new Intent(EventScheduleActivity.this, EventTicketActivity.class);
                            intent.putExtra("EVENT_ID", e.getId());
                            intent.putExtra("TICKET_ID", finalMyTicketId);
                            startActivity(intent);
                        });
                    } else {
                        holder.btnAction.setText("Đăng ký");
                        holder.btnAction.setOnClickListener(v -> {
                            Intent intent = new Intent(EventScheduleActivity.this, EventTicketActivity.class);
                            intent.putExtra("EVENT_ID", e.getId());
                            startActivity(intent);
                        });
                    }
                }
            }

            if (e.getCoverImageBase64() != null && e.getCoverImageBase64().startsWith("data:image")) {
                byte[] b = Base64.decode(e.getCoverImageBase64().split(",")[1], Base64.DEFAULT);
                Glide.with(EventScheduleActivity.this).load(b).centerCrop().into(holder.ivCover);
            }
        }
        @Override
        public int getItemCount() { return myScheduleList.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSubtitle, tvTime, tvBadge, btnAction;
            ShapeableImageView ivCover;
            VH(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_event_title);
                tvSubtitle = itemView.findViewById(R.id.tv_event_subtitle);
                tvTime = itemView.findViewById(R.id.tv_event_time);
                tvBadge = itemView.findViewById(R.id.tv_event_badge);
                btnAction = itemView.findViewById(R.id.btn_event_action);
                ivCover = itemView.findViewById(R.id.iv_event_cover);
            }
        }
    }

    private class ExploreEventAdapter extends RecyclerView.Adapter<ExploreEventAdapter.VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_explore, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Event e = exploreEventsList.get(position);
            holder.tvTitle.setText(e.getTitle());
            holder.tvFormat.setText(e.isOnline() ? "Online" : "Offline");
            holder.tvPrice.setText(e.getPrice());
            
            if (holder.tvSubtitle != null) {
                FirebaseFirestore.getInstance().collection("Users").document(e.getAuthorId())
                    .get().addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            java.util.Map<String, Object> profile = (java.util.Map<String, Object>) doc.get("profile");
                            String fullName = "Người ẩn danh";
                            if (profile != null && profile.containsKey("fullName")) {
                                fullName = (String) profile.get("fullName");
                            }
                            holder.tvSubtitle.setText(fullName + " - " + (e.isOnline() ? "Online" : "Offline"));
                        }
                    });
            }
            
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(e.getDateMillis());
            holder.tvTime.setText(e.getStartTime() + " - " + e.getEndTime() + " - " + cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH)+1));

            if (e.getCoverImageBase64() != null && e.getCoverImageBase64().startsWith("data:image")) {
                byte[] b = Base64.decode(e.getCoverImageBase64().split(",")[1], Base64.DEFAULT);
                Glide.with(EventScheduleActivity.this).load(b).centerCrop().into(holder.ivCover);
            }

            String uid = FirebaseAuth.getInstance().getUid();
            boolean isAuthor = uid != null && uid.equals(e.getAuthorId());
            boolean isRegistered = false;
            String myTicketId = null;
            for (EventTicket t : myTickets) {
                if (t.getEventId().equals(e.getId())) {
                    isRegistered = true;
                    myTicketId = t.getId();
                    break;
                }
            }

            if (isAuthor) {
                holder.btnAction.setText("Của bạn");
                holder.btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#888888")));
                holder.btnAction.setOnClickListener(v -> {
                    Toast.makeText(EventScheduleActivity.this, "Bạn là người tạo sự kiện này", Toast.LENGTH_SHORT).show();
                });
            } else if (isRegistered) {
                holder.btnAction.setText("Xem vé");
                holder.btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                String finalTicketId = myTicketId;
                holder.btnAction.setOnClickListener(v -> {
                    Intent intent = new Intent(EventScheduleActivity.this, EventTicketActivity.class);
                    intent.putExtra("EVENT_ID", e.getId());
                    intent.putExtra("TICKET_ID", finalTicketId);
                    startActivity(intent);
                });
            } else {
                holder.btnAction.setText("Đăng ký");
                holder.btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4272D0")));
                holder.btnAction.setOnClickListener(v -> registerEvent(e));
            }
        }
        @Override
        public int getItemCount() { return exploreEventsList.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvFormat, tvPrice, tvTime, btnAction, tvSubtitle;
            ShapeableImageView ivCover;
            VH(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_event_title);
                tvSubtitle = v.findViewById(R.id.tv_event_subtitle);
                tvFormat = v.findViewById(R.id.tv_event_format);
                tvPrice = v.findViewById(R.id.tv_event_price);
                tvTime = v.findViewById(R.id.tv_event_time);
                btnAction = v.findViewById(R.id.btn_event_action);
                ivCover = v.findViewById(R.id.iv_event_cover);
            }
        }
    }

    // Workaround helper
    static class ColorStateListHelper {
        static android.content.res.ColorStateList white() { return android.content.res.ColorStateList.valueOf(Color.parseColor("#FFFFFF")); }
        static android.content.res.ColorStateList blue() { return android.content.res.ColorStateList.valueOf(Color.parseColor("#4272D0")); }
    }
}
