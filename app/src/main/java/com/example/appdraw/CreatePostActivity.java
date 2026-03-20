package com.example.appdraw;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;

public class CreatePostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        Toolbar toolbar = findViewById(R.id.toolbar_create_post);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        MaterialButton btnPost = findViewById(R.id.btn_post);
        btnPost.setOnClickListener(v -> {
            Toast.makeText(this, "Đang đăng bài viết...", Toast.LENGTH_SHORT).show();
            // Logic to save post to Firebase would go here
            finish();
        });

        findViewById(R.id.card_add_media).setOnClickListener(v -> {
            Toast.makeText(this, "Mở thư viện ảnh/video", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.tv_choose_lesson).setOnClickListener(v -> {
            Toast.makeText(this, "Chọn bài học để gắn", Toast.LENGTH_SHORT).show();
        });
    }
}
