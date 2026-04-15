package com.example.appdraw;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HomeworkActivity extends AppCompatActivity {

    private ImageView ivUploadedImage;
    private LinearLayout llUploadPlaceholder;
    private boolean isImageUploaded = false;

    private String lessonTitle;
    private com.google.firebase.firestore.FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework);

        Toolbar toolbar = findViewById(R.id.toolbar_homework);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        lessonTitle = getIntent().getStringExtra("LESSON_TITLE");
        if (lessonTitle == null) {
            lessonTitle = "Unknown Lesson";
        }

        String imageResStr = getIntent().getStringExtra("IMAGE_RES");
        if (imageResStr != null && !imageResStr.isEmpty()) {
            ImageView ivBg = findViewById(R.id.iv_homework_bg);
            if (ivBg != null) {
                int resId = getResources().getIdentifier(imageResStr, "drawable", getPackageName());
                if (resId != 0) {
                    ivBg.setImageResource(resId);
                }
            }
        }

        com.example.appdraw.utils.HomeworkHelper.HomeworkDetails details = com.example.appdraw.utils.HomeworkHelper.getHomeworkDetails(lessonTitle);
        TextView tvContent = findViewById(R.id.tv_homework_content);
        TextView tvCriteria1 = findViewById(R.id.tv_criteria_1);
        TextView tvCriteria2 = findViewById(R.id.tv_criteria_2);

        if (tvContent != null && details.desc != null) tvContent.setText(details.desc);
        if (tvCriteria1 != null && details.criteria1 != null) tvCriteria1.setText(details.criteria1);
        if (tvCriteria2 != null && details.criteria2 != null) tvCriteria2.setText(details.criteria2);

        db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            uid = "guest";
        }

        ivUploadedImage = findViewById(R.id.iv_uploaded_image);
        llUploadPlaceholder = findViewById(R.id.ll_upload_placeholder);

        findViewById(R.id.card_upload).setOnClickListener(v -> showSubmissionChoiceBottomSheet());

        Button btnSubmit = findViewById(R.id.btn_submit_homework);
        btnSubmit.setOnClickListener(v -> {
            if (!isImageUploaded) {
                Toast.makeText(this, "Vui lòng tải ảnh bài vẽ lên trước!", Toast.LENGTH_SHORT).show();
            } else {
                markLessonCompleted();
            }
        });

        fetchProgressFromFirestore();
    }

    private android.net.Uri selectedImageUri = null;

    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> galleryLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivUploadedImage.setImageURI(selectedImageUri);
                    ivUploadedImage.setVisibility(View.VISIBLE);
                    llUploadPlaceholder.setVisibility(View.GONE);
                    isImageUploaded = true;
                    Toast.makeText(this, "Đã tải ảnh lên thành công!", Toast.LENGTH_SHORT).show();
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> drawingLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String base64Url = result.getData().getStringExtra("SAVED_BASE64");
                    if (base64Url != null && base64Url.startsWith("data:image")) {
                        String cleanBase64 = base64Url.substring(base64Url.indexOf(",") + 1);
                        byte[] decodedString = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT);
                        android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory
                                .decodeByteArray(decodedString, 0, decodedString.length);
                        ivUploadedImage.setImageBitmap(decodedByte);

                        String path = android.provider.MediaStore.Images.Media.insertImage(getContentResolver(),
                                decodedByte, "Homework_" + System.currentTimeMillis(), null);
                        if (path != null)
                            selectedImageUri = android.net.Uri.parse(path);

                        ivUploadedImage.setVisibility(View.VISIBLE);
                        llUploadPlaceholder.setVisibility(View.GONE);
                        isImageUploaded = true;
                        Toast.makeText(this, "Đã cập nhật bài vẽ!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void fetchProgressFromFirestore() {
        if ("guest".equals(uid))
            return;
        db.collection("Users").document(uid).collection("lessonProgress").document(lessonTitle)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");
                        if ("COMPLETED".equals(status)) {
                            String base64Url = documentSnapshot.getString("imageUrl");
                            if (base64Url != null && base64Url.startsWith("data:image")) {
                                String cleanBase64 = base64Url.substring(base64Url.indexOf(",") + 1);
                                byte[] decodedString = android.util.Base64.decode(cleanBase64,
                                        android.util.Base64.DEFAULT);
                                android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory
                                        .decodeByteArray(decodedString, 0, decodedString.length);
                                ivUploadedImage.setImageBitmap(decodedByte);

                                // Tạo local cache URI để có thể Share bài
                                String path = android.provider.MediaStore.Images.Media.insertImage(getContentResolver(),
                                        decodedByte, "Homework_" + System.currentTimeMillis(), null);
                                if (path != null)
                                    selectedImageUri = android.net.Uri.parse(path);
                            } else {
                                ivUploadedImage.setImageResource(R.drawable.ve_hoa_mau_nuoc); // Fallback
                            }

                            ivUploadedImage.setVisibility(View.VISIBLE);
                            llUploadPlaceholder.setVisibility(View.GONE);
                            isImageUploaded = true;

                            Button btnSubmit = findViewById(R.id.btn_submit_homework);
                            btnSubmit.setText("Cập nhật bài nộp");
                            btnSubmit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2ECC71)); // Xanh
                                                                                                                     // lá

                            // Show Grade
                            findViewById(R.id.card_grade_result).setVisibility(View.VISIBLE);
                            TextView tvScore = findViewById(R.id.tv_homework_score);
                            TextView tvFeedback = findViewById(R.id.tv_homework_feedback);

                            int hash = Math.abs((uid + lessonTitle).hashCode());
                            float mockScore = 8.0f + (hash % 20) / 10.0f; // 8.0 to 9.9
                            tvScore.setText(String.format(java.util.Locale.getDefault(), "%.1f / 10", mockScore));

                            String[] feeds = {
                                    "Nét vẽ rất tự nhiên và loang màu mượt mà. Tuyệt vời!",
                                    "Bố cục hoàn hảo, bạn đã nắm bắt được trọng tâm bài học.",
                                    "Màu sắc rất có hồn, bạn đang tiến bộ rất nhanh đấy!",
                                    "Chú ý một chút ở kỹ thuật đi nét mỏng, còn lại rất xuất sắc!"
                            };
                            tvFeedback.setText("Nhận xét từ Giảng viên: " + feeds[hash % feeds.length]);

                            // findViewById(R.id.card_upload).setClickable(false); // Cho phép nộp lại ảnh
                            // mới
                        }
                    }
                });
    }

    private void markLessonCompleted() {
        if (!"guest".equals(uid)) {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("status", "COMPLETED");
            data.put("lastUpdated", System.currentTimeMillis());

            if (selectedImageUri != null) {
                try {
                    android.graphics.Bitmap bitmap;
                    if (android.os.Build.VERSION.SDK_INT >= 28) {
                        android.graphics.ImageDecoder.Source source = android.graphics.ImageDecoder
                                .createSource(getContentResolver(), selectedImageUri);
                        bitmap = android.graphics.ImageDecoder.decodeBitmap(source);
                    } else {
                        bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(),
                                selectedImageUri);
                    }
                    java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, buffer);
                    byte[] fileBytes = buffer.toByteArray();
                    String base64Image = android.util.Base64.encodeToString(fileBytes, android.util.Base64.DEFAULT);
                    String finalImageUrl = "data:image/jpeg;base64," + base64Image;

                    data.put("imageUrl", finalImageUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            db.collection("Users").document(uid).collection("lessonProgress").document(lessonTitle)
                    .update(data)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            // Cập nhật đè nếu chưa tồn tại
                            db.collection("Users").document(uid).collection("lessonProgress").document(lessonTitle)
                                    .set(data);
                        }
                    });
        }
        showSuccessDialog();
    }

    private void simulateImageUpload() {
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void showSubmissionChoiceBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(
                this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_submission_choice, null);
        bottomSheetDialog.setContentView(dialogView);

        dialogView.findViewById(R.id.card_draw_canvas).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            android.content.Intent intent = new android.content.Intent(HomeworkActivity.this,
                    com.example.appdraw.drawing.DrawingActivity.class);
            drawingLauncher.launch(intent);
        });

        dialogView.findViewById(R.id.card_upload_photo).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            simulateImageUpload();
        });

        bottomSheetDialog.show();
    }

    private void showSuccessDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_lesson_complete); // Sử dụng lại layout thành công
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvTitle = dialog.findViewById(R.id.tv_completion_title);
        TextView tvSubTitle = dialog.findViewById(R.id.tv_completion_subtitle);
        Button btnMain = dialog.findViewById(R.id.btn_do_homework_now);
        Button btnClose = dialog.findViewById(R.id.btn_later);

        if (tvTitle != null)
            tvTitle.setText("Nộp bài thành công!");
        if (tvSubTitle != null)
            tvSubTitle.setText("Bài vẽ của bạn đã được gửi đi.\nHãy chia sẻ với Cộng đồng nào!");
        if (btnMain != null)
            btnMain.setText("Chia sẻ tác phẩm");

        btnMain.setOnClickListener(v -> {
            dialog.dismiss();
            android.content.Intent intent = new android.content.Intent(HomeworkActivity.this,
                    com.example.appdraw.community.CreatePostActivity.class);
            intent.putExtra("PREFILL_TEXT", "#" + lessonTitle.replaceAll("\\s+", "")
                    + " \nĐây là tác phẩm bài tập của mình. Mọi người nhận xét giúp nhé!");
            if (isImageUploaded) {
                if (selectedImageUri != null) {
                    intent.putExtra("PREFILL_IMAGE_URI", selectedImageUri.toString());
                } else {
                    android.net.Uri imageUri = android.net.Uri
                            .parse("android.resource://" + getPackageName() + "/" + R.drawable.ve_hoa_mau_nuoc);
                    intent.putExtra("PREFILL_IMAGE_URI", imageUri.toString());
                }
            }
            startActivity(intent);
            finish();
        });

        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }
}
