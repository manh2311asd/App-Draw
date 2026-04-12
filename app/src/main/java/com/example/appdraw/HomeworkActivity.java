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
    }

    private void markLessonCompleted() {
        if (!"guest".equals(uid)) {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("status", "COMPLETED");
            data.put("lastUpdated", System.currentTimeMillis());

            db.collection("Users").document(uid).collection("lessonProgress").document(lessonTitle)
                    .update(data)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            // Cập nhật đè nếu chưa tồn tại
                            db.collection("Users").document(uid).collection("lessonProgress").document(lessonTitle).set(data);
                        }
                    });
        }
        showSuccessDialog();
    }

    private void simulateImageUpload() {
        // Giả lập việc chọn ảnh từ thư viện
        ivUploadedImage.setImageResource(R.drawable.ve_hoa_mau_nuoc);
        ivUploadedImage.setVisibility(View.VISIBLE);
        llUploadPlaceholder.setVisibility(View.GONE);
        isImageUploaded = true;
        Toast.makeText(this, "Đã tải ảnh lên thành công!", Toast.LENGTH_SHORT).show();
    }

    private void showSubmissionChoiceBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_submission_choice, null);
        bottomSheetDialog.setContentView(dialogView);

        dialogView.findViewById(R.id.card_draw_canvas).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            // Mở công cụ vẽ
            android.content.Intent intent = new android.content.Intent(HomeworkActivity.this, com.example.appdraw.drawing.DrawingActivity.class);
            startActivity(intent);
            
            // MOCK: Giả định ảnh đã tải lên để cho phép nộp
            isImageUploaded = true;
            ivUploadedImage.setImageResource(R.drawable.ve_hoa_mau_nuoc);
            ivUploadedImage.setVisibility(View.VISIBLE);
            llUploadPlaceholder.setVisibility(View.GONE);
            Toast.makeText(this, "Đã mở Canvas! Bài vẽ sẽ được đính kèm khi bạn quay lại.", Toast.LENGTH_LONG).show();
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

        if (tvTitle != null) tvTitle.setText("Nộp bài thành công!");
        if (tvSubTitle != null) tvSubTitle.setText("Bài vẽ của bạn đã được gửi đi.\nHãy chia sẻ với Cộng đồng nào!");
        if (btnMain != null) btnMain.setText("Chia sẻ tác phẩm");
        
        btnMain.setOnClickListener(v -> {
            dialog.dismiss();
            android.content.Intent intent = new android.content.Intent(HomeworkActivity.this, com.example.appdraw.community.CreatePostActivity.class);
            intent.putExtra("PREFILL_TEXT", "#ThucHanhVeMauNuoc \nĐây là tác phẩm bài tập của mình. Mọi người nhận xét giúp nhé!");
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
