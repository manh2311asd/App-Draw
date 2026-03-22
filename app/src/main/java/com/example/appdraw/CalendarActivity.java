package com.example.appdraw;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class CalendarActivity extends AppCompatActivity {

    private View layoutLessonsContent;
    private View layoutEventsContent;
    private TextView tabLessons;
    private TextView tabEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Ánh xạ View
        layoutLessonsContent = findViewById(R.id.layout_lessons_content);
        layoutEventsContent = findViewById(R.id.layout_events_content);
        tabLessons = findViewById(R.id.tab_lessons);
        tabEvents = findViewById(R.id.tab_events);

        // Nút quay lại trên Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_calendar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Mặc định hiển thị Lịch học
        showLessonsTab();

        // Xử lý chuyển tab
        if (tabLessons != null) {
            tabLessons.setOnClickListener(v -> showLessonsTab());
        }

        if (tabEvents != null) {
            tabEvents.setOnClickListener(v -> showEventsTab());
        }

        // --- Thiết lập sự kiện cho phần ĐĂNG KÝ (bên tab Sự kiện) ---
        setupEventRegistration();
    }

    private void setupEventRegistration() {
        // Nút Đăng ký sự kiện 1
        View btnRegister1 = findViewById(R.id.btn_register_event_1);
        if (btnRegister1 != null) {
            btnRegister1.setOnClickListener(v -> {
                Intent intent = new Intent(this, EventRegistrationSuccessActivity.class);
                startActivity(intent);
            });
        }

        // Nút Đăng ký sự kiện 2
        View btnRegister2 = findViewById(R.id.btn_register_event_2);
        if (btnRegister2 != null) {
            btnRegister2.setOnClickListener(v -> {
                Intent intent = new Intent(this, EventRegistrationSuccessActivity.class);
                startActivity(intent);
            });
        }
    }

    private void showLessonsTab() {
        if (layoutLessonsContent != null) layoutLessonsContent.setVisibility(View.VISIBLE);
        if (layoutEventsContent != null) layoutEventsContent.setVisibility(View.GONE);

        // Cập nhật UI cho Tab "Lịch học" (Selected)
        if (tabLessons != null) {
            tabLessons.setBackgroundResource(R.drawable.bg_chip_selected);
            if (tabLessons.getBackground() != null) {
                tabLessons.getBackground().setTint(ContextCompat.getColor(this, R.color.primary_blue));
            }
            tabLessons.setTextColor(Color.WHITE);
        }

        // Cập nhật UI cho Tab "Sự kiện" (Unselected)
        if (tabEvents != null) {
            tabEvents.setBackgroundResource(0);
            tabEvents.setTextColor(Color.parseColor("#888888"));
        }
    }

    private void showEventsTab() {
        if (layoutLessonsContent != null) layoutLessonsContent.setVisibility(View.GONE);
        if (layoutEventsContent != null) layoutEventsContent.setVisibility(View.VISIBLE);

        // Cập nhật UI cho Tab "Sự kiện" (Selected)
        if (tabEvents != null) {
            tabEvents.setBackgroundResource(R.drawable.bg_chip_selected);
            if (tabEvents.getBackground() != null) {
                tabEvents.getBackground().setTint(ContextCompat.getColor(this, R.color.primary_blue));
            }
            tabEvents.setTextColor(Color.WHITE);
        }

        // Cập nhật UI cho Tab "Lịch học" (Unselected)
        if (tabLessons != null) {
            tabLessons.setBackgroundResource(0);
            tabLessons.setTextColor(Color.parseColor("#888888"));
        }
    }
}
