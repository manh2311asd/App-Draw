package com.example.appdraw;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;

public class CreatePostActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivMediaPreview;
    private View llPlaceholder;
    private EditText etCaption;
    private Uri selectedMediaUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        ivMediaPreview = findViewById(R.id.iv_media_preview);
        llPlaceholder = findViewById(R.id.ll_add_media_placeholder);
        etCaption = findViewById(R.id.et_caption);
        
        setupToolbar();

        findViewById(R.id.card_add_media).setOnClickListener(v -> openGallery());

        findViewById(R.id.btn_post).setOnClickListener(v -> uploadPost());

        findViewById(R.id.tv_choose_lesson).setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng chọn bài học đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_create_post);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/* video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh hoặc video"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedMediaUri = data.getData();
            ivMediaPreview.setImageURI(selectedMediaUri);
            ivMediaPreview.setVisibility(View.VISIBLE);
            llPlaceholder.setVisibility(View.GONE);
        }
    }

    private void uploadPost() {
        String caption = etCaption.getText().toString().trim();

        if (selectedMediaUri == null && caption.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm nội dung hoặc hình ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạm thời giả lập việc đăng bài không cần Firebase Auth/Database
        findViewById(R.id.btn_post).setEnabled(false);
        Toast.makeText(this, "Đang tải bài viết lên cộng đồng (Chế độ test)...", Toast.LENGTH_SHORT).show();

        new android.os.Handler().postDelayed(() -> {
            Toast.makeText(CreatePostActivity.this, "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
            finish();
        }, 1500);
    }
}
