package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ProjectListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        Toolbar toolbar = findViewById(R.id.toolbar_projects);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Mẫu: Click vào dự án hoa hồng để xem chi tiết
        View cardRose = findViewById(R.id.card_project_rose);
        if (cardRose != null) {
            cardRose.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProjectDetailActivity.class);
                startActivity(intent);
            });
        }
        
        // Setup tabs (đang làm / đã hoàn thành)
        View tabDoing = findViewById(R.id.tab_doing);
        View tabCompleted = findViewById(R.id.tab_completed);
        
        if (tabDoing != null) {
            tabDoing.setOnClickListener(v -> {
                // Logic chuyển tab
            });
        }
        
        if (tabCompleted != null) {
            tabCompleted.setOnClickListener(v -> {
                // Logic chuyển tab
            });
        }
    }
}
