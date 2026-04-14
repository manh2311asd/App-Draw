package com.example.appdraw.explore;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.appdraw.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LessonListActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean isSeeding = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_list);

        String titleHeader = getIntent().getStringExtra("TITLE");
        if (titleHeader == null) titleHeader = "Bài học gợi ý";
        ((TextView) findViewById(R.id.tv_toolbar_title)).setText(titleHeader);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Xóa loadAllLessons() trong onCreate vì onResume() sẽ chạy ngay sau đó, 
        // tránh đúp truy vấn gây race-condition.
    }

    private void loadAllLessons() {
        android.widget.LinearLayout container = findViewById(R.id.lesson_container);
        if (container == null) return;
        container.removeAllViews();

        String titleHeader = getIntent().getStringExtra("TITLE");
        if (titleHeader == null) titleHeader = "Bài học gợi ý";

        com.google.firebase.firestore.Query query;
        if ("Bài học gợi ý".equals(titleHeader)) {
            query = db.collection("SuggestedLessons").orderBy("title");
        } else {
            query = db.collection("Lessons").whereEqualTo("category", titleHeader);
        }

        final String finalTitleHeader = titleHeader;
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            boolean needsReseed = false;
            if (queryDocumentSnapshots.isEmpty()) {
                needsReseed = true;
            } else if (!"Bài học gợi ý".equals(finalTitleHeader)) {
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    String t = doc.getString("title");
                    if (t != null && (t.contains("Khởi động với " + finalTitleHeader) || 
                                      t.contains("Thực hành " + finalTitleHeader) || 
                                      t.contains("Nâng cao " + finalTitleHeader) ||
                                      t.contains("Kiểm tra cuối khóa " + finalTitleHeader) ||
                                      t.equals("Bài 1: Khởi động với Dành cho người mới bắt đầu") ||
                                      t.matches("^Bài \\d+:.*") ||
                                      t.equals("Bài tập ôn luyện") ||
                                      t.contains("/"))) {
                        needsReseed = true;
                        break;
                    }
                }
            }

            if (needsReseed && !"Bài học gợi ý".equals(finalTitleHeader)) {
                seedLessonsForCategory(finalTitleHeader);
                return;
            }

            LayoutInflater inflater = LayoutInflater.from(this);
            String uid = auth.getUid();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String title = doc.getString("title");
                
                // SuggestedLessons uses "author", but Lessons uses "authorName". Let's handle both:
                String author = doc.getString("author");
                if (author == null) author = doc.getString("authorName");
                
                String imageResStr = doc.getString("imageRes");
                String imageUrl = doc.getString("imageUrl");

                View lessonView = inflater.inflate(R.layout.item_lesson_list, container, false);

                TextView tvTitle = lessonView.findViewById(R.id.tv_lesson_title);
                TextView tvAuthor = lessonView.findViewById(R.id.tv_author);
                ImageView ivThumb = lessonView.findViewById(R.id.iv_lesson_thumb);
                TextView tvStatus = lessonView.findViewById(R.id.tv_status);

                if (tvTitle != null) tvTitle.setText(title);
                if (tvAuthor != null) tvAuthor.setText(author);

                if (ivThumb != null) {
                    if (imageResStr != null && !imageResStr.isEmpty() && !imageResStr.matches("-?\\d+")) {
                        try {
                            int resId = getResources().getIdentifier(imageResStr, "drawable", getPackageName());
                            if (resId != 0) ivThumb.setImageResource(resId);
                        } catch (Exception e) {}
                    } else if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this).load(imageUrl).centerCrop().into(ivThumb);
                    }
                }

                tvStatus.setText("Chưa học");
                tvStatus.setBackgroundResource(R.drawable.rounded_bg_gray);
                tvStatus.setTextColor(Color.parseColor("#808080"));

                RatingBar rb = lessonView.findViewById(R.id.rating_bar);
                if (rb != null) {
                    float randomRating = 3.5f + (float) (Math.random() * 1.5f);
                    rb.setRating(randomRating);
                }

                if (uid != null && title != null && !title.contains("/")) {
                    db.collection("Users").document(uid).collection("lessonProgress").document(title)
                            .get().addOnSuccessListener(progDoc -> {
                                if (progDoc.exists()) {
                                    String status = progDoc.getString("status");
                                    if ("COMPLETED".equals(status)) {
                                        tvStatus.setText("Hoàn thành");
                                        tvStatus.setBackgroundResource(R.drawable.bg_badge_completed);
                                        tvStatus.setTextColor(Color.WHITE);
                                    } else if ("IN_PROGRESS".equals(status)) {
                                        tvStatus.setText("Đang học");
                                        tvStatus.setBackgroundResource(R.drawable.rounded_bg_gray);
                                        tvStatus.setTextColor(Color.parseColor("#666666"));
                                    }
                                }
                            });
                }

                lessonView.setOnClickListener(v -> {
                    String currentStatus = tvStatus.getText().toString();
                    if ("Hoàn thành".equals(currentStatus)) {
                        Toast.makeText(this, "Bạn đã hoàn thành bài học: " + title, Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(this, LessonDetailActivity.class);
                        intent.putExtra("LESSON_TITLE", title);
                        startActivity(intent);
                    }
                });

                container.addView(lessonView);
            }
        });
    }

    private void seedLessonsForCategory(String category) {
        if (isSeeding) return;
        isSeeding = true;

        db.collection("Lessons").whereEqualTo("category", category).get().addOnSuccessListener(snap -> {
            for (DocumentSnapshot doc : snap) {
                doc.getReference().delete();
            }

            String author = "Bởi Hải Nam";
            String[] titles;
            String imageRes = "ve_thien_nhien";

            if (category.contains("thiên nhiên")) {
                author = "Bởi Thu Thủy";
                imageRes = "ve_thien_nhien";
                titles = new String[]{
                        "Vẽ rừng cây mùa thu",
                        "Dòng suối nhỏ trong vắt",
                        "Núi non trùng điệp",
                        "Bãi biển lúc hoàng hôn",
                        "Thảo nguyên xanh mướt",
                        "Đêm trăng sáng trên đồi",
                        "Thung lũng sương mù",
                        "Khu vườn nhiệt đới",
                        "Vẽ thác nước hùng vĩ",
                        "Tổng hợp phong cảnh"
                };
            } else if (category.contains("Origami")) {
                author = "Bởi Minh Khang";
                imageRes = "img_origami_art";
                titles = new String[]{
                        "Gấp hạc giấy cơ bản",
                        "Thuyền buồm ra khơi",
                        "Bông hoa 5 cánh",
                        "Cáo nhỏ xinh xắn",
                        "Ngôi sao may mắn",
                        "Hộp quà tí hon",
                        "Rồng giấy origami",
                        "Khủng long bạo chúa"
                };
            } else if (category.contains("Manga")) {
                author = "Bởi Hương Lan";
                imageRes = "tp_trending_2";
                titles = new String[]{
                        "Core tỷ lệ khuôn mặt",
                        "Vẽ mắt Manga mượt mà",
                        "Kiểu tóc nam và nữ cơ bản",
                        "Mảng biểu cảm vui buồn",
                        "Góc nghiêng thần thánh",
                        "Phác họa nhân vật nữ",
                        "Phác họa nhân vật nam"
                };
            } else if (category.contains("màu nước")) {
                author = "Bởi Tuấn Vũ";
                imageRes = "banner_watercolor";
                titles = new String[]{
                        "Palette pha màu cơ bản",
                        "Kỹ thuật loang màu ẩm",
                        "Vẽ bầu trời gợn mây",
                        "Tĩnh vật cốc cà phê",
                        "Bông cẩm tú cầu",
                        "Sơn thủy hữu tình",
                        "Ánh tà dương hoàng hôn"
                };
            } else { // Người mới
                author = "Bởi Phong Artist";
                imageRes = "ve_hoa_mau_nuoc";
                titles = new String[]{
                        "Làm quen với Brush",
                        "Khái niệm hình học",
                        "Đánh bóng và chiếu sáng",
                        "Kỹ thuật đan nét cọ",
                        "Vẽ tĩnh vật quả táo",
                        "Xây dựng khối 3D",
                        "Luyện tập tổng hợp"
                };
            }

            for (int i = 0; i < titles.length; i++) {
                String title = titles[i];
                java.util.Map<String, Object> data = new java.util.HashMap<>();
                data.put("title", title);
                data.put("authorName", author);
                data.put("imageRes", imageRes);
                data.put("category", category);
                
                // Mấu chốt chặn lặp dữ liệu: Dùng document ID cố định theo Hash để ép ghi đè thay vì tạo mới.
                String safeDocId = "lesson_" + Math.abs(category.hashCode()) + "_" + i;
                db.collection("Lessons").document(safeDocId).set(data);
            }

            // Gọi tải lại dữ liệu đảm bảo không trễ UI
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                isSeeding = false;
                loadAllLessons();
            }, 1000);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isSeeding) {
            loadAllLessons();
        }
    }
}
