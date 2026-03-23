package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class EventListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        Toolbar toolbar = findViewById(R.id.toolbar_event_list);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupEvents();
    }

    private void setupEvents() {
        // Event 1 - Workshop: Màu nước nâng cao
        setupEventItem(R.id.event_1, R.drawable.banner_watercolor, "Workshop: Màu nước nâng cao", "20/10/2024 - 19:30", "Trực tuyến qua Zoom", "Đăng ký");
        
        // Event 2 - Triển lãm: Sắc màu mùa thu
        setupEventItem(R.id.event_2, R.drawable.tp_trending_1, "Triển lãm: Sắc màu mùa thu", "22/10/2024 - 09:00", "Bảo tàng Mỹ thuật", "Mua vé");
        
        // Event 3 - Live: Vẽ phong cảnh biển
        setupEventItem(R.id.event_3, R.drawable.ve_thien_nhien, "Live: Vẽ phong cảnh biển", "25/10/2024 - 20:00", "Livestream Facebook", "Nhắc tôi");
        
        // Event 4 - Lớp học: Ký họa nhân vật
        setupEventItem(R.id.event_4, R.drawable.ve_hoa_mau_nuoc, "Lớp học: Ký họa nhân vật", "28/10/2024 - 14:00", "AppDraw Studio", "Đăng ký");
    }

    private void setupEventItem(int layoutId, int imageRes, String title, String time, String location, String actionText) {
        View view = findViewById(layoutId);
        if (view == null) return;

        ImageView ivThumb = view.findViewById(R.id.iv_event_thumb);
        TextView tvTitle = view.findViewById(R.id.tv_event_title);
        TextView tvTime = view.findViewById(R.id.tv_event_time);
        TextView tvLocation = view.findViewById(R.id.tv_event_location);
        com.google.android.material.button.MaterialButton btnAction = view.findViewById(R.id.btn_event_action);

        if (ivThumb != null) ivThumb.setImageResource(imageRes);
        if (tvTitle != null) tvTitle.setText(title);
        if (tvTime != null) tvTime.setText(time);
        if (tvLocation != null) tvLocation.setText(location);
        
        if (btnAction != null) {
            btnAction.setText(actionText);
            
            // Xử lý khi nhấn "Đăng ký"
            if ("Đăng ký".equals(actionText)) {
                btnAction.setOnClickListener(v -> {
                    btnAction.setText("Đã đăng ký");
                    btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2ECC71)); // Màu xanh lá
                    
                    // Chuyển sang màn hình Lịch học
                    Intent intent = new Intent(this, CalendarActivity.class);
                    startActivity(intent);
                });
            } else if ("Mua vé".equals(actionText) || "Nhắc tôi".equals(actionText)) {
                // Các nút khác vẫn có thể ấn được nhưng không đổi màu xanh
                btnAction.setOnClickListener(v -> {
                    // Logic xử lý khác nếu cần
                });
            }
        }
    }
}
