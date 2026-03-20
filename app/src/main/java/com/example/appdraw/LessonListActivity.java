package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LessonListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_list);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        setupLessons();
    }

    private void setupLessons() {
        // Lesson 1
        View lesson1 = findViewById(R.id.lesson_1);
        if (lesson1 != null) {
            ((ImageView) lesson1.findViewById(R.id.iv_lesson_thumb)).setImageResource(R.drawable.ve_hoa_mau_nuoc);
            ((TextView) lesson1.findViewById(R.id.tv_lesson_title)).setText("Vẽ hoa màu nước");
            ((TextView) lesson1.findViewById(R.id.tv_author)).setText("Phong Artist");
            ((TextView) lesson1.findViewById(R.id.tv_duration)).setText("25 min");
            ((TextView) lesson1.findViewById(R.id.tv_status)).setText("Chưa học");
            ((RatingBar) lesson1.findViewById(R.id.rating_bar)).setRating(2);
            
            lesson1.setOnClickListener(v -> {
                Intent intent = new Intent(this, LessonDetailActivity.class);
                startActivity(intent);
            });
        }

        // Lesson 2
        View lesson2 = findViewById(R.id.lesson_2);
        if (lesson2 != null) {
            ((ImageView) lesson2.findViewById(R.id.iv_lesson_thumb)).setImageResource(R.drawable.tp_trending_1);
            ((TextView) lesson2.findViewById(R.id.tv_lesson_title)).setText("Vẽ hoàng hôn");
            ((TextView) lesson2.findViewById(R.id.tv_author)).setText("Phong Artist");
            ((TextView) lesson2.findViewById(R.id.tv_duration)).setText("35 min");
            TextView status = lesson2.findViewById(R.id.tv_status);
            status.setText("✓ Hoàn thành");
            status.setBackgroundResource(R.drawable.bg_status_pill);
            status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2ECC71));
            status.setTextColor(0xFFFFFFFF);
            ((RatingBar) lesson2.findViewById(R.id.rating_bar)).setRating(3);
        }

        // Lesson 3
        View lesson3 = findViewById(R.id.lesson_3);
        if (lesson3 != null) {
            ((ImageView) lesson3.findViewById(R.id.iv_lesson_thumb)).setImageResource(R.drawable.ve_thien_nhien);
            ((TextView) lesson3.findViewById(R.id.tv_lesson_title)).setText("Vẽ dã ngoại");
            ((TextView) lesson3.findViewById(R.id.tv_author)).setText("Phong Artist");
            ((TextView) lesson3.findViewById(R.id.tv_duration)).setText("30 min");
            TextView status = lesson3.findViewById(R.id.tv_status);
            status.setText("✓ Hoàn thành");
            status.setBackgroundResource(R.drawable.bg_status_pill);
            status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2ECC71));
            status.setTextColor(0xFFFFFFFF);
            ((RatingBar) lesson3.findViewById(R.id.rating_bar)).setRating(3);
        }

        // Lesson 4
        View lesson4 = findViewById(R.id.lesson_4);
        if (lesson4 != null) {
            ((ImageView) lesson4.findViewById(R.id.iv_lesson_thumb)).setImageResource(R.drawable.banner_watercolor);
            ((TextView) lesson4.findViewById(R.id.tv_lesson_title)).setText("Vẽ cây cầu");
            ((TextView) lesson4.findViewById(R.id.tv_author)).setText("Phong Artist");
            ((TextView) lesson4.findViewById(R.id.tv_duration)).setText("25 min");
            TextView status = lesson4.findViewById(R.id.tv_status);
            status.setText("✓ Hoàn thành");
            status.setBackgroundResource(R.drawable.bg_status_pill);
            status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2ECC71));
            status.setTextColor(0xFFFFFFFF);
            ((RatingBar) lesson4.findViewById(R.id.rating_bar)).setRating(4);
        }

        // Lesson 5
        View lesson5 = findViewById(R.id.lesson_5);
        if (lesson5 != null) {
            ((ImageView) lesson5.findViewById(R.id.iv_lesson_thumb)).setImageResource(R.drawable.img_challenge_tree);
            ((TextView) lesson5.findViewById(R.id.tv_lesson_title)).setText("Vẽ cối xay gió");
            ((TextView) lesson5.findViewById(R.id.tv_author)).setText("Phong Artist");
            ((TextView) lesson5.findViewById(R.id.tv_duration)).setText("25 min");
            ((TextView) lesson5.findViewById(R.id.tv_status)).setText("Chưa học");
            ((RatingBar) lesson5.findViewById(R.id.rating_bar)).setRating(3);
        }
    }
}
