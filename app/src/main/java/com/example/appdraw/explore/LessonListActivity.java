package com.example.appdraw.explore;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.appdraw.R;

public class LessonListActivity extends AppCompatActivity {
    private String[] lessonTitles;
    private String[] lessonTimes;
    private float[] lessonRatings;
    private String[] lessonAuthors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_list);

        String titleHeader = getIntent().getStringExtra("TITLE");
        if (titleHeader == null) titleHeader = "Tương Tác";
        ((TextView) findViewById(R.id.tv_toolbar_title)).setText(titleHeader);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        setupArraysForCategory(titleHeader);

        android.widget.LinearLayout container = findViewById(R.id.lesson_container);
        if (container != null) {
            android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
            for (int i = 0; i < lessonTitles.length; i++) {
                final String realTitle = lessonTitles[i];
                
                android.view.View lessonView = inflater.inflate(R.layout.item_lesson_list, container, false);
                
                ((TextView) lessonView.findViewById(R.id.tv_lesson_title)).setText(realTitle);
                ((TextView) lessonView.findViewById(R.id.tv_duration)).setText(lessonTimes[i]);
                TextView tvAuthor = lessonView.findViewById(R.id.tv_author);
                if (tvAuthor != null) tvAuthor.setText("Bởi " + lessonAuthors[i]);
                
                android.widget.RatingBar rb = lessonView.findViewById(R.id.rating_bar);
                if (rb != null) rb.setRating(lessonRatings[i]);

                lessonView.setOnClickListener(v -> {
                    android.content.Intent intent = new android.content.Intent(LessonListActivity.this, LessonDetailActivity.class);
                    // Pass real title
                    intent.putExtra("LESSON_TITLE", realTitle);
                    startActivity(intent);
                });
                
                container.addView(lessonView);
            }
        }
    }

    private void setupArraysForCategory(String category) {
        if (category.contains("Origami")) { // 8 bài
            lessonTitles = new String[]{"Gấp hạc giấy cơ bản", "Ếch nhảy Origami", "Xếp hoa sen giấy", "Gấp rồng cổ đại", "Làm hộp quà vuông", "Phi tiêu Ninja", "Gấp bướm mùa xuân", "Cắt dán hoa thị"};
            lessonTimes = new String[]{"15 min", "10 min", "25 min", "60 min", "20 min", "10 min", "15 min", "12 min"};
            lessonRatings = new float[]{5.0f, 4.0f, 4.5f, 5.0f, 4.0f, 3.5f, 4.5f, 4.0f};
            lessonAuthors = new String[]{"Donal", "Mai Anh", "Quốc Bảo", "Mai Anh", "Quốc Bảo", "Donal", "Donal", "Mai Anh"};
        } else if (category.contains("thiên nhiên")) { // 10 bài
            lessonTitles = new String[]{"Phong cảnh đồi núi", "Rừng thông sương mù", "Bầu trời hoàng hôn", "Kỹ thuật vẽ mây", "Lá cây mùa thu", "Mặt hồ phẳng lặng", "Vẽ hoa anh đào", "Thác nước hùng vĩ", "Vườn hoa cúc", "Núi tuyết Phú Sĩ"};
            lessonTimes = new String[]{"45 min", "50 min", "30 min", "25 min", "20 min", "35 min", "40 min", "60 min", "45 min", "55 min"};
            lessonRatings = new float[]{4.5f, 5.0f, 4.0f, 3.5f, 4.5f, 4.0f, 5.0f, 4.8f, 4.5f, 5.0f};
            lessonAuthors = new String[]{"Thùy Chi", "Tuấn Vũ", "Hải Nam", "Tuấn Vũ", "Thùy Chi", "Phong Artist", "Thùy Chi", "Hải Nam", "Tuấn Vũ", "Phong Artist"};
        } else if (category.contains("màu nước")) { // Khám phá màu nước
            lessonTitles = new String[]{"Vẽ hoa màu nước", "Phong cảnh hồ thu", "Cánh đồng hoa cúc", "Vẽ cá vàng", "Trời sao lung linh", "Cây cổ thụ", "Màu nước cơ bản"};
            lessonTimes = new String[]{"25 min", "40 min", "60 min", "35 min", "20 min", "90 min", "30 min"};
            lessonRatings = new float[]{5.0f, 4.5f, 4.0f, 5.0f, 3.5f, 4.5f, 4.0f};
            lessonAuthors = new String[]{"Hoàng Lam", "Hoàng Lam", "Thu Thủy", "Hoàng Lam", "Thu Thủy", "Minh Khang", "Thu Thủy"};
        } else if (category.contains("Manga")) { // Chân dung Manga
            lessonTitles = new String[]{"Phác thảo Manga", "Tỷ lệ khuôn mặt", "Vẽ mắt Manga", "Trang phục nữ sinh", "Biểu cảm nhân vật", "Tóc bay trong gió", "Tô bóng cơ bản"};
            lessonTimes = new String[]{"40 min", "30 min", "25 min", "45 min", "20 min", "35 min", "50 min"};
            lessonRatings = new float[]{4.5f, 4.0f, 5.0f, 4.5f, 4.0f, 5.0f, 4.5f};
            lessonAuthors = new String[]{"Linh Trần", "Nhật Anh", "Linh Trần", "Nhật Anh", "Minh Khang", "Linh Trần", "Nhật Anh"};
        } else {
            // "Digital Art" or others
            lessonTitles = new String[]{"Làm quen Procreate", "Layer và Blending", "Màu sắc Digital", "Thiết kế nhân vật", "Kỹ thuật loang màu", "Line art", "Color grading"};
            lessonTimes = new String[]{"25 min", "40 min", "60 min", "35 min", "20 min", "90 min", "30 min"};
            lessonRatings = new float[]{5.0f, 4.5f, 4.0f, 5.0f, 3.5f, 4.5f, 4.0f};
            lessonAuthors = new String[]{"Minh Khang", "Tuấn Vũ", "Minh Khang", "Tuấn Vũ", "Thùy Chi", "Minh Khang", "Tuấn Vũ"};
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLessonStatuses();
    }

    private void updateLessonStatuses() {
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getUid();
        
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        android.widget.LinearLayout container = findViewById(R.id.lesson_container);
        if (container == null) return;
        
        for (int i = 0; i < lessonTitles.length; i++) {
            final String lessonTitle = lessonTitles[i];
            final android.view.View lessonView = container.getChildAt(i);
            if (lessonView == null) continue;

            db.collection("Users").document(uid).collection("lessonProgress").document(lessonTitle)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String status = documentSnapshot.getString("status");
                            android.widget.TextView tvStatus = lessonView.findViewById(R.id.tv_status);
                            if (tvStatus != null) {
                                if ("COMPLETED".equals(status)) {
                                    tvStatus.setText("Đã nộp");
                                    tvStatus.setTextColor(0xFF2ECC71); // Xanh lá
                                    tvStatus.setBackgroundResource(R.drawable.rounded_bg_gray);
                                } else if ("WAITING_FOR_HOMEWORK".equals(status)) {
                                    tvStatus.setText("Chưa nộp");
                                    tvStatus.setTextColor(0xFFFF9800); // Cam
                                } else if ("IN_PROGRESS".equals(status)) {
                                    tvStatus.setText("Đang học");
                                    tvStatus.setTextColor(0xFF4272D0); // Xanh lam
                                }
                            }
                        }
                    });
        }
    }
}
