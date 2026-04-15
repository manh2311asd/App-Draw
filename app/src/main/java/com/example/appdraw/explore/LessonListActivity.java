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
        if (titleHeader == null)
            titleHeader = "Bài học gợi ý";
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
        if (container == null)
            return;
        container.removeAllViews();

        String titleHeader = getIntent().getStringExtra("TITLE");
        if (titleHeader == null)
            titleHeader = "Bài học gợi ý";

        com.google.firebase.firestore.Query query;
        java.util.List<String> homeSuggestedTitles = java.util.Arrays.asList(
                "Làm quen với Brush", "Đêm trăng sáng trên đồi", "Palette pha màu cơ bản",
                "Phác thảo khuôn mặt Chibi", "Core tỷ lệ khuôn mặt");

        if ("Bài học gợi ý".equals(titleHeader)) {
            query = db.collection("Lessons").whereIn("title", homeSuggestedTitles);
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

            // Patch ảnh đúng cho bài đã seed nếu cần (chạy nền, không ảnh hưởng UI)
            patchLessonImages(finalTitleHeader);

            // Process and Sort documents
            java.util.List<DocumentSnapshot> displayDocs = new java.util.ArrayList<>();
            if ("Bài học gợi ý".equals(finalTitleHeader)) {
                for (String t : homeSuggestedTitles) {
                    for (DocumentSnapshot d : queryDocumentSnapshots) {
                        if (t.equals(d.getString("title"))) {
                            displayDocs.add(d);
                            break;
                        }
                    }
                }
            } else {
                for (DocumentSnapshot d : queryDocumentSnapshots) {
                    displayDocs.add(d);
                }
            }

            LayoutInflater inflater = LayoutInflater.from(this);
            String uid = auth.getUid();

            for (DocumentSnapshot doc : displayDocs) {
                String title = doc.getString("title");

                // SuggestedLessons uses "author", but Lessons uses "authorName". Let's handle
                // both:
                String author = doc.getString("author");
                if (author == null)
                    author = doc.getString("authorName");

                String imageResStr = doc.getString("imageRes");
                String imageUrl = doc.getString("imageUrl");

                // Override ảnh theo title - đảm bảo luôn đúng bất kể data Firestore
                if ("Đêm trăng sáng trên đồi".equals(title)) {
                    imageResStr = "dem_trang_sang_tren_doi";
                } else if ("Khu vườn nhiệt đới".equals(title)) {
                    imageResStr = "khu_vuon_nhiet_doi";
                } else if ("Thung lũng sương mù".equals(title)) {
                    imageResStr = "thung_lung_suong_mu";
                } else if ("Vẽ rừng cây mùa thu".equals(title)) {
                    imageResStr = "ve_rung_cay_mua_thu";
                } else if ("Tổng hợp phong cảnh".equals(title)) {
                    imageResStr = "tong_hop_phong_canh";
                } else if ("Bãi biển lúc hoàng hôn".equals(title)) {
                    imageResStr = "bai_bien_luc_hoang_hon";
                } else if ("Núi non trùng điệp".equals(title)) {
                    imageResStr = "nui_non_trung_diep";
                } else if ("Dòng suối nhỏ trong vắt".equals(title)) {
                    imageResStr = "dong_suoi_nho_trong_vat";
                } else if ("Thảo nguyên xanh mướt".equals(title)) {
                    imageResStr = "thao_nguyen_xanh_muot";
                } else if ("Vẽ thác nước hùng vĩ".equals(title)) {
                    imageResStr = "ve_thac_nuoc_hung_vi";
                    // --- Dành cho người mới bắt đầu ---
                } else if ("Làm quen với Brush".equals(title)) {
                    imageResStr = "lam_quen_voi_brush";
                } else if ("Khái niệm hình học".equals(title)) {
                    imageResStr = "khai_niem_hinh_hoc";
                } else if ("Đánh bóng và chiếu sáng".equals(title)) {
                    imageResStr = "danh_bong_va_chieu_sang";
                } else if ("Kỹ thuật đan nét cọ".equals(title)) {
                    imageResStr = "ki_thuat_dan_net_co";
                } else if ("Vẽ tĩnh vật quả táo".equals(title)) {
                    imageResStr = "ve_tinh_vat_qua_tao";
                } else if ("Xây dựng khối 3D".equals(title)) {
                    imageResStr = "xay_dung_khoi_3d";
                } else if ("Luyện tập tổng hợp".equals(title)) {
                    imageResStr = "luyen_tap_tong_hop";
                    // --- Khám phá màu nước ---
                } else if ("Palette pha màu cơ bản".equals(title)) {
                    imageResStr = "palette_pha_mau_co_ban";
                } else if ("Kỹ thuật loang màu ẩm".equals(title)) {
                    imageResStr = "ki_thuat_loang_mau_am";
                } else if ("Vẽ bầu trời gợn mây".equals(title)) {
                    imageResStr = "ve_bau_troi_gon_may";
                } else if ("Tĩnh vật cốc cà phê".equals(title)) {
                    imageResStr = "tinh_vat_coc_ca_phe";
                } else if ("Bông cẩm tú cầu".equals(title)) {
                    imageResStr = "bong_cam_tu_cau";
                } else if ("Sơn thủy hữu tình".equals(title)) {
                    imageResStr = "son_thuy_huu_tinh";
                } else if ("Ánh tà dương hoàng hôn".equals(title)) {
                    imageResStr = "anh_ta_duong_hoang_hon";
                    // --- Nghệ thuật vẽ Chibi ---
                } else if ("Phác thảo khuôn mặt Chibi".equals(title)) {
                    imageResStr = "phac_thao_khuon_mat_chibi";
                } else if ("Tỷ lệ cơ thể đầu to".equals(title)) {
                    imageResStr = "ty_le_co_the_dau_to";
                } else if ("Vẽ mắt to tròn đáng yêu".equals(title)) {
                    imageResStr = "ve_mat_to_tron_dang_yeu";
                } else if ("Biểu cảm khuôn mặt dễ thương".equals(title)) {
                    imageResStr = "bieu_cam_khuon_mat_de_thuong";
                } else if ("Vẽ tóc bồng bềnh".equals(title)) {
                    imageResStr = "ve_toc_bong_benh";
                } else if ("Phối đồ phong cách basic".equals(title)) {
                    imageResStr = "phoi_do_phong_cach_basic";
                } else if ("Lên màu pastel cơ bản".equals(title)) {
                    imageResStr = "len_mau_pastel_co_ban";
                } else if ("Hoàn thiện nhân vật".equals(title)) {
                    imageResStr = "hoan_thien_nhan_vat";
                    // --- Chân dung manga ---
                } else if ("Core tỷ lệ khuôn mặt".equals(title)) {
                    imageResStr = "core_ty_le_khuon_mat";
                } else if ("Vẽ mắt Manga mượt mà".equals(title)) {
                    imageResStr = "ve_mat_manga_muot_ma";
                } else if ("Kiểu tóc nam và nữ cơ bản".equals(title)) {
                    imageResStr = "kieu_toc_nam_va_nu_co_ban";
                } else if ("Mảng biểu cảm vui buồn".equals(title)) {
                    imageResStr = "mang_bieu_cam_vui_buon";
                } else if ("Góc nghiêng thần thánh".equals(title)) {
                    imageResStr = "goc_nghieng_than_thanh";
                } else if ("Phác họa nhân vật nữ".equals(title)) {
                    imageResStr = "phac_hoa_nhan_vat_nu";
                } else if ("Phác họa nhân vật nam".equals(title)) {
                    imageResStr = "phac_hoa_nhan_vat_nam";
                }
                View lessonView = inflater.inflate(R.layout.item_lesson_list, container, false);

                TextView tvTitle = lessonView.findViewById(R.id.tv_lesson_title);
                TextView tvAuthor = lessonView.findViewById(R.id.tv_author);
                ImageView ivThumb = lessonView.findViewById(R.id.iv_lesson_thumb);
                TextView tvStatus = lessonView.findViewById(R.id.tv_status);
                TextView tvDuration = lessonView.findViewById(R.id.tv_duration);

                if (tvTitle != null)
                    tvTitle.setText(title);
                if (tvAuthor != null) {
                    if (author != null && !author.toLowerCase().startsWith("bởi")) {
                        tvAuthor.setText("Bởi " + author);
                    } else {
                        tvAuthor.setText(author);
                    }
                }

                if (ivThumb != null) {
                    if (imageResStr != null && !imageResStr.isEmpty() && !imageResStr.matches("-?\\d+")) {
                        try {
                            int resId = getResources().getIdentifier(imageResStr, "drawable", getPackageName());
                            if (resId != 0)
                                ivThumb.setImageResource(resId);
                        } catch (Exception e) {
                        }
                    } else if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this).load(imageUrl).centerCrop().into(ivThumb);
                    }
                }

                tvStatus.setText("Chưa học");
                tvStatus.setBackgroundResource(R.drawable.rounded_bg_gray);
                tvStatus.setTextColor(Color.parseColor("#808080"));

                RatingBar rb = lessonView.findViewById(R.id.rating_bar);
                if (rb != null) {
                    rb.setRating(4.5f);
                }

                if (tvDuration != null) {
                    String catCheck = finalTitleHeader.toLowerCase();
                    if (catCheck.contains("mới bắt đầu") || catCheck.contains("beginner")) {
                        tvDuration.setText("20 min");
                    } else if (catCheck.contains("thiên nhiên") || catCheck.contains("màu nước")) {
                        tvDuration.setText("45 min");
                    } else {
                        tvDuration.setText("60 min");
                    }
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
                                    } else if ("IN_PROGRESS".equals(status) || "WAITING_FOR_HOMEWORK".equals(status)) {
                                        tvStatus.setText("Đang học");
                                        tvStatus.setBackgroundResource(R.drawable.bg_badge_in_progress);
                                        tvStatus.setTextColor(Color.WHITE);
                                    }
                                }
                            });
                }

                final String finalImageRes = imageResStr;
                final String finalAuthor = author;
                final String finalDocId = doc.getId();
                lessonView.setOnClickListener(v -> {
                    String currentStatus = tvStatus.getText().toString();
                    if ("Hoàn thành".equals(currentStatus)) {
                        Intent intent = new Intent(this, com.example.appdraw.explore.MySubmissionActivity.class);
                        intent.putExtra("LESSON_TITLE", title);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this, LessonDetailActivity.class);
                        intent.putExtra("LESSON_TITLE", title);
                        intent.putExtra("CATEGORY", finalTitleHeader);
                        intent.putExtra("IMAGE_RES", finalImageRes);
                        intent.putExtra("AUTHOR", finalAuthor);
                        intent.putExtra("LESSON_ID", finalDocId);
                        startActivity(intent);
                    }
                });

                container.addView(lessonView);
            }
        });
    }

    private void seedLessonsForCategory(String category) {
        if (isSeeding)
            return;
        isSeeding = true;

        db.collection("Lessons").whereEqualTo("category", category).get().addOnSuccessListener(snap -> {
            for (DocumentSnapshot doc : snap) {
                doc.getReference().delete();
            }

            String author = "Bởi Hải Nam";
            String[] titles;
            String[] images; // Mỗi bài một ảnh riêng

            if (category.contains("thiên nhiên")) {
                author = "Bởi Thu Thủy";
                titles = new String[] {
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
                images = new String[] {
                        "ve_thien_nhien",
                        "ve_thien_nhien",
                        "ve_thien_nhien",
                        "ve_thien_nhien",
                        "ve_thien_nhien",
                        "dem_trang_sang_tren_doi",
                        "ve_thien_nhien",
                        "ve_thien_nhien",
                        "ve_thien_nhien",
                        "ve_thien_nhien"
                };
            } else if (category.contains("Chibi")) {
                author = "Bởi Minh Khang";
                titles = new String[] {
                        "Phác thảo khuôn mặt Chibi",
                        "Tỷ lệ cơ thể đầu to",
                        "Vẽ mắt to tròn đáng yêu",
                        "Biểu cảm khuôn mặt dễ thương",
                        "Vẽ tóc bồng bềnh",
                        "Phối đồ phong cách basic",
                        "Lên màu pastel cơ bản",
                        "Hoàn thiện nhân vật"
                };
                images = new String[] { "tp_trending_3", "tp_trending_3", "tp_trending_3", "tp_trending_3",
                        "tp_trending_3", "tp_trending_3", "tp_trending_3", "tp_trending_3" };
            } else if (category.contains("Manga")) {
                author = "Bởi Hương Lan";
                titles = new String[] {
                        "Core tỷ lệ khuôn mặt",
                        "Vẽ mắt Manga mượt mà",
                        "Kiểu tóc nam và nữ cơ bản",
                        "Mảng biểu cảm vui buồn",
                        "Góc nghiêng thần thánh",
                        "Phác họa nhân vật nữ",
                        "Phác họa nhân vật nam"
                };
                images = new String[] { "tp_trending_2", "tp_trending_2", "tp_trending_2", "tp_trending_2",
                        "tp_trending_2", "tp_trending_2", "tp_trending_2" };
            } else if (category.contains("màu nước")) {
                author = "Bởi Tuấn Vũ";
                titles = new String[] {
                        "Palette pha màu cơ bản",
                        "Kỹ thuật loang màu ẩm",
                        "Vẽ bầu trời gợn mây",
                        "Tĩnh vật cốc cà phê",
                        "Bông cẩm tú cầu",
                        "Sơn thủy hữu tình",
                        "Ánh tà dương hoàng hôn"
                };
                images = new String[] { "banner_watercolor", "banner_watercolor", "banner_watercolor",
                        "banner_watercolor", "banner_watercolor", "banner_watercolor", "banner_watercolor" };
            } else { // Người mới
                author = "Bởi Phong Artist";
                titles = new String[] {
                        "Làm quen với Brush",
                        "Khái niệm hình học",
                        "Đánh bóng và chiếu sáng",
                        "Kỹ thuật đan nét cọ",
                        "Vẽ tĩnh vật quả táo",
                        "Xây dựng khối 3D",
                        "Luyện tập tổng hợp"
                };
                images = new String[] { "ve_hoa_mau_nuoc", "ve_hoa_mau_nuoc", "ve_hoa_mau_nuoc", "ve_hoa_mau_nuoc",
                        "ve_hoa_mau_nuoc", "ve_hoa_mau_nuoc", "ve_hoa_mau_nuoc" };
            }

            for (int i = 0; i < titles.length; i++) {
                String title = titles[i];
                java.util.Map<String, Object> data = new java.util.HashMap<>();
                data.put("title", title);
                data.put("authorName", author);
                data.put("imageRes", images[i]);
                data.put("category", category);

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

    /** Patch ảnh đúng cho các bài học đã seed trước đó (chạy 1 lần khi load) */
    private void patchLessonImages(String category) {
        if (!category.contains("thiên nhiên"))
            return;
        // Bài "Đêm trăng sáng trên đồi" = index 5 → docId cố định
        String docId = "lesson_" + Math.abs(category.hashCode()) + "_5";
        db.collection("Lessons").document(docId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String currentImg = doc.getString("imageRes");
                if (!"dem_trang_sang_tren_doi".equals(currentImg)) {
                    doc.getReference().update("imageRes", "dem_trang_sang_tren_doi");
                }
            }
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
