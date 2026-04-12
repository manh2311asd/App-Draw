package com.example.appdraw.explore;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.appdraw.R;

public class LessonListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_list);

        String title = getIntent().getStringExtra("TITLE");
        if (title != null) {
            ((TextView) findViewById(R.id.tv_toolbar_title)).setText(title);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Add dummy click listeners for lesson lists
        int[] lessonIds = {R.id.lesson_1, R.id.lesson_2, R.id.lesson_3, R.id.lesson_4, R.id.lesson_5, R.id.lesson_6, R.id.lesson_7};
        for (int id : lessonIds) {
            android.view.View lessonView = findViewById(id);
            if (lessonView != null) {
                lessonView.setOnClickListener(v -> {
                    android.content.Intent intent = new android.content.Intent(LessonListActivity.this, LessonDetailActivity.class);
                    // Pass a mock title to the detail activity
                    intent.putExtra("LESSON_TITLE", title != null ? title + " - Bài Tương Tác" : "Bài học mẫu");
                    startActivity(intent);
                });
            }
        }
    }
}
