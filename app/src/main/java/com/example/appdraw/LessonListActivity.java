package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class LessonListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_list);

        String title = getIntent().getStringExtra("TITLE");
        if (title != null) {
            TextView tvTitle = findViewById(R.id.tv_toolbar_title);
            if (tvTitle != null) tvTitle.setText(title);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        setupLessons();
    }

    private void setupLessons() {
        // Lesson 1: Vẽ hoa màu nước (Đã hoàn thành)
        View lesson1 = findViewById(R.id.lesson_1);
        if (lesson1 != null) {
            ((ImageView) lesson1.findViewById(R.id.iv_lesson_thumb)).setImageResource(R.drawable.ve_hoa_mau_nuoc);
            ((TextView) lesson1.findViewById(R.id.tv_lesson_title)).setText("Vẽ hoa màu nước");
            ((TextView) lesson1.findViewById(R.id.tv_author)).setText("Phong Artist");
            ((TextView) lesson1.findViewById(R.id.tv_duration)).setText("25 min");
            
            TextView status = lesson1.findViewById(R.id.tv_status);
            status.setText("✓ Hoàn thành");
            status.setBackgroundResource(R.drawable.bg_status_pill);
            status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2ECC71));
            status.setTextColor(0xFFFFFFFF);
            
            ((RatingBar) lesson1.findViewById(R.id.rating_bar)).setRating(5);
            
            lesson1.setOnClickListener(v -> {
                Intent intent = new Intent(this, LessonDetailActivity.class);
                intent.putExtra("LESSON_STATUS", "COMPLETED");
                intent.putExtra("LESSON_TITLE", "Vẽ hoa màu nước");
                startActivity(intent);
            });
        }

        // Lesson 5: Vẽ cối xay gió (Chưa học)
        View lesson5 = findViewById(R.id.lesson_5);
        if (lesson5 != null) {
            ((ImageView) lesson5.findViewById(R.id.iv_lesson_thumb)).setImageResource(R.drawable.img_challenge_tree);
            ((TextView) lesson5.findViewById(R.id.tv_lesson_title)).setText("Vẽ cối xay gió");
            ((TextView) lesson5.findViewById(R.id.tv_author)).setText("Phong Artist");
            ((TextView) lesson5.findViewById(R.id.tv_duration)).setText("25 min");
            
            TextView status = lesson5.findViewById(R.id.tv_status);
            status.setText("Chưa học");
            status.setBackgroundResource(R.drawable.rounded_bg_gray);
            status.setBackgroundTintList(null);
            status.setTextColor(0xFF666666);
            
            ((RatingBar) lesson5.findViewById(R.id.rating_bar)).setRating(3);

            lesson5.setOnClickListener(v -> {
                Intent intent = new Intent(this, LessonDetailActivity.class);
                intent.putExtra("LESSON_STATUS", "NOT_STARTED");
                intent.putExtra("LESSON_TITLE", "Vẽ cối xay gió");
                startActivity(intent);
            });
        }
        
        // Cấu hình các bài khác tương tự...
        setupOtherLesson(R.id.lesson_2, "Làm đồ gốm", R.drawable.tp_trending_1, "COMPLETED", 3);
        setupOtherLesson(R.id.lesson_3, "Cách đổ bóng", R.drawable.ve_thien_nhien, "COMPLETED", 3);
        setupOtherLesson(R.id.lesson_4, "Vẽ cây cầu", R.drawable.banner_watercolor, "COMPLETED", 4);
        setupOtherLesson(R.id.lesson_6, "Vẽ khu rừng", R.drawable.item_lesson_preview, "NOT_STARTED", 3);
        setupOtherLesson(R.id.lesson_7, "Vẽ ao hồ", R.drawable.backgroud_app_draw, "NOT_STARTED", 3);
    }

    private void setupOtherLesson(int id, String title, int thumb, String statusStr, float rating) {
        View view = findViewById(id);
        if (view == null) return;

        ((ImageView) view.findViewById(R.id.iv_lesson_thumb)).setImageResource(thumb);
        ((TextView) view.findViewById(R.id.tv_lesson_title)).setText(title);
        ((TextView) view.findViewById(R.id.tv_author)).setText("Phong Artist");
        ((TextView) view.findViewById(R.id.tv_duration)).setText("25 min");
        ((RatingBar) view.findViewById(R.id.rating_bar)).setRating(rating);

        TextView tvStatus = view.findViewById(R.id.tv_status);
        if ("COMPLETED".equals(statusStr)) {
            tvStatus.setText("✓ Hoàn thành");
            tvStatus.setBackgroundResource(R.drawable.bg_status_pill);
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2ECC71));
            tvStatus.setTextColor(0xFFFFFFFF);
        } else {
            tvStatus.setText("Chưa học");
            tvStatus.setBackgroundResource(R.drawable.rounded_bg_gray);
            tvStatus.setTextColor(0xFF666666);
        }

        view.setOnClickListener(v -> {
            Intent intent = new Intent(this, LessonDetailActivity.class);
            intent.putExtra("LESSON_STATUS", statusStr);
            intent.putExtra("LESSON_TITLE", title);
            startActivity(intent);
        });
    }
}
