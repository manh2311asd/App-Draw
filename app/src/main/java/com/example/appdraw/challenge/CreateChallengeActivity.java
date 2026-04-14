package com.example.appdraw.challenge;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;
import android.app.DatePickerDialog;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.appdraw.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateChallengeActivity extends AppCompatActivity {
    
    private Uri selectedLocalUri;
    private ImageView ivSelectedImage;
    private LinearLayout llPlaceholder;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedLocalUri = result.getData().getData();
                    if (llPlaceholder != null) llPlaceholder.setVisibility(View.GONE);
                    if (ivSelectedImage != null) {
                        ivSelectedImage.setVisibility(View.VISIBLE);
                        Glide.with(this).load(selectedLocalUri).centerCrop().into(ivSelectedImage);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_challenge);

        Toolbar toolbar = findViewById(R.id.toolbar_create_challenge);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        EditText edtTitle = findViewById(R.id.edt_challenge_title);
        EditText edtRules = findViewById(R.id.edt_challenge_rules);
        EditText edtRewards = findViewById(R.id.edt_challenge_rewards);
        MaterialButton btnSubmit = findViewById(R.id.btn_create_challenge_submit);

        View cardAddImage = findViewById(R.id.card_add_image);
        ivSelectedImage = findViewById(R.id.iv_selected_image);
        llPlaceholder = findViewById(R.id.ll_placeholder_image);
        
        TextView tvStartDate = findViewById(R.id.tv_start_date);
        TextView tvEndDate = findViewById(R.id.tv_end_date);
        
        final Calendar calStart = Calendar.getInstance();
        final Calendar calEnd = Calendar.getInstance();
        calEnd.add(Calendar.DAY_OF_YEAR, 7);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        
        if (tvStartDate != null) {
            tvStartDate.setOnClickListener(v -> {
                new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                    calStart.set(Calendar.YEAR, year);
                    calStart.set(Calendar.MONTH, month);
                    calStart.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    new android.app.TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                        calStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calStart.set(Calendar.MINUTE, minute);
                        tvStartDate.setText(sdf.format(calStart.getTime()));
                    }, calStart.get(Calendar.HOUR_OF_DAY), calStart.get(Calendar.MINUTE), true).show();
                }, calStart.get(Calendar.YEAR), calStart.get(Calendar.MONTH), calStart.get(Calendar.DAY_OF_MONTH)).show();
            });
        }
        
        if (tvEndDate != null) {
            tvEndDate.setOnClickListener(v -> {
                new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                    calEnd.set(Calendar.YEAR, year);
                    calEnd.set(Calendar.MONTH, month);
                    calEnd.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    new android.app.TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                        calEnd.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calEnd.set(Calendar.MINUTE, minute);
                        tvEndDate.setText(sdf.format(calEnd.getTime()));
                    }, calEnd.get(Calendar.HOUR_OF_DAY), calEnd.get(Calendar.MINUTE), true).show();
                }, calEnd.get(Calendar.YEAR), calEnd.get(Calendar.MONTH), calEnd.get(Calendar.DAY_OF_MONTH)).show();
            });
        }

        if (cardAddImage != null) {
            cardAddImage.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                galleryLauncher.launch(intent);
            });
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String title = edtTitle != null ? edtTitle.getText().toString().trim() : "";
                if (title.isEmpty()) {
                    title = "Thử thách Ký họa ngẫu hứng";
                }
                String rules = edtRules != null ? edtRules.getText().toString().trim() : "";
                String rewards = edtRewards != null ? edtRewards.getText().toString().trim() : "";
                
                String finalTitle = title;

                long now = System.currentTimeMillis();
                // Cho phép độ trễ 10 phút trong lúc người dùng điền form
                if (calStart.getTimeInMillis() < now - 10 * 60 * 1000) {
                    Toast.makeText(this, "Thời gian bắt đầu không được ở trong quá khứ!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (calEnd.getTimeInMillis() <= calStart.getTimeInMillis()) {
                    Toast.makeText(this, "Hạn deadline phải lớn hơn thời gian bắt đầu!", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Toast.makeText(this, "Yêu cầu đăng nhập!", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnSubmit.setEnabled(false);
                Toast.makeText(this, "Đang xử lý...", Toast.LENGTH_SHORT).show();

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String mentorName = "Mentor";
                        if (documentSnapshot.exists()) {
                            Map<String, Object> profile = (Map<String, Object>) documentSnapshot.get("profile");
                            if (profile != null && profile.containsKey("fullName")) {
                                mentorName = "Mentor: " + profile.get("fullName");
                            }
                        }

                        // Generate Dates
                        SimpleDateFormat shortSdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
                        String startDateStr = shortSdf.format(calStart.getTime());
                        String endDateStr = shortSdf.format(calEnd.getTime());
                        String dateStr = startDateStr + " - " + endDateStr;

                        String finalImageUrl = "";
                        if (selectedLocalUri != null) {
                            try {
                                android.graphics.Bitmap bitmap;
                                if (android.os.Build.VERSION.SDK_INT >= 28) {
                                    android.graphics.ImageDecoder.Source source = android.graphics.ImageDecoder.createSource(getContentResolver(), selectedLocalUri);
                                    bitmap = android.graphics.ImageDecoder.decodeBitmap(source);
                                } else {
                                    bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), selectedLocalUri);
                                }
                                
                                // Banner aspect ratio optimization and scaling
                                int currentWidth = bitmap.getWidth();
                                int currentHeight = bitmap.getHeight();
                                int maxWidth = 800;
                                int maxHeight = 600;
                                
                                float scale = Math.min(((float)maxWidth / currentWidth), ((float)maxHeight / currentHeight));
                                
                                android.graphics.Bitmap scaledBitmap = bitmap;
                                if (scale < 1) {
                                    int newWidth = Math.round(currentWidth * scale);
                                    int newHeight = Math.round(currentHeight * scale);
                                    scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                                }

                                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                                scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, buffer);
                                byte[] fileBytes = buffer.toByteArray();
                                String base64Image = android.util.Base64.encodeToString(fileBytes, android.util.Base64.DEFAULT);
                                finalImageUrl = "data:image/jpeg;base64," + base64Image;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        Map<String, Object> data = new HashMap<>();
                        data.put("title", finalTitle);
                        data.put("author", mentorName);
                        data.put("authorId", user.getUid());
                        data.put("dateStr", dateStr);
                        data.put("participantsCount", "0 đã tham gia");
                        data.put("rules", rules);
                        data.put("rewards", rewards);
                        
                        if (!finalImageUrl.isEmpty()) {
                            data.put("imageUrl", finalImageUrl);
                        } else {
                            data.put("imageRes", String.valueOf(R.drawable.ve_hoa_mau_nuoc));
                        }
                        
                        data.put("endTimeMillis", calEnd.getTimeInMillis());

                        db.collection("Challenges").add(data)
                            .addOnSuccessListener(dr -> {
                                Toast.makeText(this, "Tạo thử thách thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> btnSubmit.setEnabled(true));
                    })
                    .addOnFailureListener(e -> btnSubmit.setEnabled(true));
            });
        }
    }
}
