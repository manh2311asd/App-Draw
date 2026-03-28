package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.appdraw.drawing.DrawingActivity;
import com.google.android.material.button.MaterialButton;

public class CreateProjectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        Toolbar toolbar = findViewById(R.id.toolbar_create_project);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        MaterialButton btnStart = findViewById(R.id.btn_start_now);
        btnStart.setOnClickListener(v -> {
            // Điều hướng đến trang bảng vẽ
            Intent intent = new Intent(this, DrawingActivity.class);
            // Có thể truyền thêm flag để biểu thị đây là dự án mới đang vẽ dở
            intent.putExtra("IS_PROJECT_CONTINUE", true);
            startActivity(intent);
        });
    }
}
