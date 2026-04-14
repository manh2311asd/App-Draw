package com.example.appdraw.community;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appdraw.R;
import com.example.appdraw.model.Event;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class CreateEventActivity extends AppCompatActivity {

    private ImageView ivBack, ivCoverPreview;
    private View cvUploadCover, llUploadPrompt;
    private View llOfflineInputs, llZoomInputs, llPriceInputs;
    private EditText etTitle, etLocation, etPrice, etZoomLink, etZoomPasscode;
    private TextView btnPickDate, btnPickStartTime, btnPickEndTime, btnFormatOnline, btnFormatOffline;
    private MaterialButton btnSubmit;

    private String coverImageBase64 = "";
    private long selectedDateMillis = 0;
    private boolean isOnline = true; // Default Online

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        Bitmap resized = getResizedBitmap(bitmap, 800);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        resized.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                        byte[] imageBytes = baos.toByteArray();
                        coverImageBase64 = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                        ivCoverPreview.setVisibility(View.VISIBLE);
                        llUploadPrompt.setVisibility(View.GONE);
                        com.bumptech.glide.Glide.with(this).load(bitmap).centerCrop().into(ivCoverPreview);

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi khi xử lý ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        initViews();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.btn_back_create_event);
        cvUploadCover = findViewById(R.id.cv_upload_cover);
        ivCoverPreview = findViewById(R.id.iv_event_cover_preview);
        llUploadPrompt = findViewById(R.id.ll_upload_prompt);
        etTitle = findViewById(R.id.et_event_title);
        etLocation = findViewById(R.id.et_event_location);
        etPrice = findViewById(R.id.et_event_price);
        etZoomLink = findViewById(R.id.et_zoom_link);
        etZoomPasscode = findViewById(R.id.et_zoom_passcode);
        llOfflineInputs = findViewById(R.id.ll_offline_inputs);
        llZoomInputs = findViewById(R.id.ll_zoom_inputs);
        llPriceInputs = findViewById(R.id.ll_price_inputs);
        btnPickDate = findViewById(R.id.btn_pick_date);
        btnPickStartTime = findViewById(R.id.btn_pick_start_time);
        btnPickEndTime = findViewById(R.id.btn_pick_end_time);
        btnFormatOnline = findViewById(R.id.btn_format_online);
        btnFormatOffline = findViewById(R.id.btn_format_offline);
        btnSubmit = findViewById(R.id.btn_create_event_submit);
        
        updateFormatUI();
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        cvUploadCover.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnPickStartTime.setOnClickListener(v -> showTimePicker(btnPickStartTime));
        btnPickEndTime.setOnClickListener(v -> showTimePicker(btnPickEndTime));

        btnFormatOnline.setOnClickListener(v -> {
            isOnline = true;
            updateFormatUI();
        });

        btnFormatOffline.setOnClickListener(v -> {
            isOnline = false;
            updateFormatUI();
        });

        btnSubmit.setOnClickListener(v -> submitEvent());
    }

    private void updateFormatUI() {
        if (isOnline) {
            btnFormatOnline.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E8F5E9")));
            btnFormatOnline.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            
            btnFormatOffline.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F5F5F5")));
            btnFormatOffline.setTextColor(android.graphics.Color.parseColor("#9E9E9E"));
            
            llZoomInputs.setVisibility(View.VISIBLE);
            llOfflineInputs.setVisibility(View.GONE);
            llPriceInputs.setVisibility(View.GONE);
        } else {
            btnFormatOnline.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F5F5F5")));
            btnFormatOnline.setTextColor(android.graphics.Color.parseColor("#9E9E9E"));
            
            btnFormatOffline.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFEBEE")));
            btnFormatOffline.setTextColor(android.graphics.Color.parseColor("#E53935"));
            
            llZoomInputs.setVisibility(View.GONE);
            llOfflineInputs.setVisibility(View.VISIBLE);
            llPriceInputs.setVisibility(View.VISIBLE);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            btnPickDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
            Calendar selCal = Calendar.getInstance();
            selCal.set(year, month, dayOfMonth, 0, 0, 0);
            selectedDateMillis = selCal.getTimeInMillis();
            btnPickDate.setTextColor(android.graphics.Color.parseColor("#333333"));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimePicker(TextView targetTextView) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String time = String.format("%02d:%02d", hourOfDay, minute);
            targetTextView.setText(time);
            targetTextView.setTextColor(android.graphics.Color.parseColor("#333333"));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private void submitEvent() {
        String title = etTitle.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String startTime = btnPickStartTime.getText().toString();
        String endTime = btnPickEndTime.getText().toString();

        String location = "";
        String zoomLink = "";
        String zoomPasscode = "";
        
        if (isOnline) {
            price = "Miễn phí (Zoom)";
            zoomLink = etZoomLink.getText().toString().trim();
            zoomPasscode = etZoomPasscode.getText().toString().trim();
            if (zoomLink.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập Link phòng Zoom/Meet", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            location = etLocation.getText().toString().trim();
            if (location.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập địa điểm tổ chức thực tế", Toast.LENGTH_SHORT).show();
                return;
            }
            if (price.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập giá vé", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (title.isEmpty() || selectedDateMillis == 0 || startTime.contains(">") || endTime.contains(">")) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin chung", Toast.LENGTH_SHORT).show();
            return;
        }

        if (coverImageBase64.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ảnh bìa", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Đang tạo...");

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Lỗi xác thực", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventId = FirebaseFirestore.getInstance().collection("Events").document().getId();

        String eventType = "Workshop";
        if (isOnline && title.toLowerCase().contains("live")) {
            eventType = "Live";
        }

        Event newEvent = new Event(
                eventId,
                uid,
                title,
                coverImageBase64,
                selectedDateMillis,
                startTime,
                endTime,
                location,
                isOnline,
                price,
                eventType,
                zoomLink,
                zoomPasscode,
                System.currentTimeMillis()
        );

        FirebaseFirestore.getInstance().collection("Events").document(eventId)
                .set(newEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tạo sự kiện thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Tạo sự kiện");
                    Toast.makeText(this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                });
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}
