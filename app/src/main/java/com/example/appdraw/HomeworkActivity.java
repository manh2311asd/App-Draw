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

        ivUploadedImage = findViewById(R.id.iv_uploaded_image);
        llUploadPlaceholder = findViewById(R.id.ll_upload_placeholder);

        findViewById(R.id.card_upload).setOnClickListener(v -> simulateImageUpload());

        Button btnSubmit = findViewById(R.id.btn_submit_homework);
        btnSubmit.setOnClickListener(v -> {
            if (!isImageUploaded) {
                Toast.makeText(this, "Vui lòng tải ảnh bài vẽ lên trước!", Toast.LENGTH_SHORT).show();
            } else {
                showSuccessDialog();
            }
        });
    }

    private void simulateImageUpload() {
        // Giả lập việc chọn ảnh từ thư viện
        ivUploadedImage.setImageResource(R.drawable.ve_hoa_mau_nuoc);
        ivUploadedImage.setVisibility(View.VISIBLE);
        llUploadPlaceholder.setVisibility(View.GONE);
        isImageUploaded = true;
        Toast.makeText(this, "Đã tải ảnh lên thành công!", Toast.LENGTH_SHORT).show();
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
        if (tvSubTitle != null) tvSubTitle.setText("Bài vẽ của bạn đã được gửi đi.\nHãy chờ giáo viên chấm điểm nhé!");
        if (btnMain != null) btnMain.setText("Về trang chủ");
        
        btnMain.setOnClickListener(v -> {
            dialog.dismiss();
            finish(); // Thoát về danh sách bài học
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
