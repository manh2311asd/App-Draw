package com.example.appdraw;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ProjectDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        // Lấy dữ liệu từ Intent
        String projectName = getIntent().getStringExtra("PROJECT_NAME");
        boolean isDone = getIntent().getBooleanExtra("IS_DONE", false);

        // Ánh xạ View
        Toolbar toolbar = findViewById(R.id.toolbar_project_detail);
        TextView tvToolbarTitle = findViewById(R.id.tv_project_title_toolbar);
        ImageView ivMain = findViewById(R.id.iv_project_main);
        TextView tvDoneDate = findViewById(R.id.tv_done_date_detail);

        // Hiển thị thông tin
        if (projectName != null) {
            tvToolbarTitle.setText(projectName);
        }

        // Tùy chỉnh ảnh và ngày tháng dựa trên dự án (Mẫu)
        if ("Tranh tĩnh vật".equals(projectName)) {
            ivMain.setImageResource(R.drawable.tp_trending_2);
            tvDoneDate.setText("Hoàn thành ngày: 15/06/2023");
        } else if ("Tranh màu nước".equals(projectName)) {
            ivMain.setImageResource(R.drawable.ve_hoa_mau_nuoc);
            tvDoneDate.setText("Hoàn thành ngày: 10/12/2024");
        } else if ("Tranh cái cốc".equals(projectName)) {
            ivMain.setImageResource(R.drawable.coc_nuoc);
            tvDoneDate.setText("Hoàn thành ngày: 20/10/2022");
        } else {
            ivMain.setImageResource(R.drawable.tp_trending_1);
            tvDoneDate.setText("Hoàn thành ngày: 20/12/2023");
        }

        // Thiết lập Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}
