package com.example.appdraw.explore;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.appdraw.R;

public class ExploreFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        // --- Search Bar ---
        View cardSearch = view.findViewById(R.id.card_search);
        if (cardSearch != null) {
            cardSearch.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            });
        }

        // --- Category Chips ---
        setupCategoryChips(view);

        // --- Banner ---
        View btnExploreNow = view.findViewById(R.id.btn_explore_now);
        if (btnExploreNow != null) {
            btnExploreNow.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LessonListActivity.class);
                intent.putExtra("TITLE", "Khám phá màu nước");
                startActivity(intent);
            });
        }

        setupDynamicCategories(view);
        setupTrendingData(view);
        setupDynamicMentors(view);

        // --- View All Handlers ---
        View tvViewAllCategories = view.findViewById(R.id.tv_view_all_categories);
        if (tvViewAllCategories != null) {
            tvViewAllCategories.setOnClickListener(v -> startActivity(new Intent(getActivity(), AllCategoriesActivity.class)));
        }

        return view;
    }

    private void setupCategoryChips(View view) {
        View chipTopic = view.findViewById(R.id.chip_topic);
        View chipTechnique = view.findViewById(R.id.chip_technique);
        View chipMaterials = view.findViewById(R.id.chip_materials);
        View chipLevel = view.findViewById(R.id.chip_level);

        if (chipTopic != null)
            chipTopic.setOnClickListener(v -> Toast.makeText(getContext(), "Chọn Chủ đề", Toast.LENGTH_SHORT).show());
        if (chipTechnique != null)
            chipTechnique
                    .setOnClickListener(v -> Toast.makeText(getContext(), "Chọn Kỹ thuật", Toast.LENGTH_SHORT).show());
        if (chipMaterials != null)
            chipMaterials
                    .setOnClickListener(v -> Toast.makeText(getContext(), "Chọn Vật liệu", Toast.LENGTH_SHORT).show());
        if (chipLevel != null)
            chipLevel.setOnClickListener(v -> Toast.makeText(getContext(), "Chọn Level", Toast.LENGTH_SHORT).show());
    }

    private void setupDynamicCategories(View view) {
        android.widget.LinearLayout container = view.findViewById(R.id.ll_categories_container);
        if (container == null)
            return;

        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore
                .getInstance();
        db.collection("Categories").orderBy("order").get().addOnSuccessListener(queryDocumentSnapshots -> {
            boolean isCorrupted = !queryDocumentSnapshots.isEmpty()
                    && queryDocumentSnapshots.getDocuments().get(0).getString("imageRes") != null
                    && queryDocumentSnapshots.getDocuments().get(0).getString("imageRes").matches("-?\\d+");

            boolean hasOrigami = false;
            boolean hasOrderProp = false;
            if (!queryDocumentSnapshots.isEmpty()) {
                hasOrderProp = queryDocumentSnapshots.getDocuments().get(0).contains("order");
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    if ("Nghệ thuật gấp giấy Origami".equals(doc.getString("title"))) {
                        hasOrigami = true;
                        break;
                    }
                }
            }

            if (queryDocumentSnapshots.isEmpty() || isCorrupted || hasOrigami || !hasOrderProp) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                }

                // Auto seed default categories
                java.util.Map<String, Object> c1 = new java.util.HashMap<>();
                c1.put("title", "Dành cho người mới bắt đầu");
                c1.put("courseCount", "7 bài học");
                c1.put("imageRes", "ve_hoa_mau_nuoc");
                c1.put("order", 1);

                java.util.Map<String, Object> c2 = new java.util.HashMap<>();
                c2.put("title", "Nghệ thuật vẽ Chibi");
                c2.put("courseCount", "8 bài học");
                c2.put("imageRes", "tp_trending_3");
                c2.put("order", 2);

                java.util.Map<String, Object> c3 = new java.util.HashMap<>();
                c3.put("title", "Vẽ thiên nhiên");
                c3.put("courseCount", "10 bài học");
                c3.put("imageRes", "ve_thien_nhien");
                c3.put("order", 3);

                java.util.Map<String, Object> c4 = new java.util.HashMap<>();
                c4.put("title", "Khám phá màu nước");
                c4.put("courseCount", "7 bài học");
                c4.put("imageRes", "banner_watercolor");
                c4.put("order", 4);

                java.util.Map<String, Object> c5 = new java.util.HashMap<>();
                c5.put("title", "Chân dung Manga");
                c5.put("courseCount", "7 bài học");
                c5.put("imageRes", "tp_trending_2");
                c5.put("order", 5);

                db.collection("Categories").add(c1);
                db.collection("Categories").add(c2);
                db.collection("Categories").add(c3);
                db.collection("Categories").add(c4);
                db.collection("Categories").add(c5)
                        .addOnSuccessListener(dr -> container.postDelayed(() -> setupDynamicCategories(view), 2500)); // Re-fetch
                return;
            }

            if (getContext() == null)
                return;
            LayoutInflater inflater = LayoutInflater.from(getContext());
            container.removeAllViews();

            for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                String title = doc.getString("title");
                String courseCount = doc.getString("courseCount");
                String imageResStr = doc.getString("imageRes");
                String imageUrl = doc.getString("imageUrl");

                View categoryView = inflater.inflate(R.layout.item_explore_category, container, false);
                TextView tvTitle = categoryView.findViewById(R.id.tv_category_name);
                TextView tvCount = categoryView.findViewById(R.id.tv_category_count);
                ImageView ivCat = categoryView.findViewById(R.id.iv_category);

                if (tvTitle != null)
                    tvTitle.setText(title);
                if (tvCount != null)
                    tvCount.setText(courseCount != null ? courseCount : "0 bài học");
                if (ivCat != null) {
                    if (imageResStr != null && !imageResStr.isEmpty() && !imageResStr.matches("-?\\d+")) {
                        try {
                            int resId = getResources().getIdentifier(imageResStr, "drawable",
                                    getContext().getPackageName());
                            if (resId != 0)
                                ivCat.setImageResource(resId);
                        } catch (Exception e) {
                        }
                    } else if (imageUrl != null && !imageUrl.isEmpty()) {
                        com.bumptech.glide.Glide.with(this).load(imageUrl).centerCrop().into(ivCat);
                    }
                }

                categoryView.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), LessonListActivity.class);
                    intent.putExtra("TITLE", title);
                    startActivity(intent);
                });

                container.addView(categoryView);
            }
        });
    }

    private void setupTrendingData(View view) {
        android.widget.LinearLayout container = view.findViewById(R.id.ll_trending_container);
        if (container == null) return;

        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        
        // Lấy 10 bài viết để đảm bảo lọc ra được 5 bài có ảnh
        db.collection("Posts")
            .orderBy("likesCount", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (getContext() == null) return;
                LayoutInflater inflater = LayoutInflater.from(getContext());
                container.removeAllViews();
                
                int count = 0;
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    if (count >= 5) break; // Chỉ hiển thị 5 tác phẩm
                    
                    com.example.appdraw.model.Post post = doc.toObject(com.example.appdraw.model.Post.class);
                    if (post == null) continue;
                    
                    String imageUrl = post.getImageUrl();
                    // Tác phẩm trending phải có ảnh
                    if (imageUrl == null || imageUrl.isEmpty()) continue;
                    
                    count++;
                    
                    View artworkView = inflater.inflate(R.layout.item_trending_artwork, container, false);
                    TextView tvTitle = artworkView.findViewById(R.id.tv_trending_title);
                    TextView tvAuthor = artworkView.findViewById(R.id.tv_trending_author);
                    TextView tvLikes = artworkView.findViewById(R.id.tv_likes_count);
                    ImageView ivArt = artworkView.findViewById(R.id.iv_trending_art);
                    
                    if (tvTitle != null) {
                        String content = post.getContent();
                        tvTitle.setText((content != null && !content.isEmpty()) ? content : "Không có tiêu đề");
                    }
                    
                    if (tvLikes != null) {
                        long likes = post.getLikesCount();
                        if (likes >= 1000) {
                            tvLikes.setText(String.format(java.util.Locale.US, "%.1fk", likes / 1000.0));
                        } else {
                            tvLikes.setText(String.valueOf(likes));
                        }
                    }
                    
                    if (ivArt != null) {
                        if (imageUrl.startsWith("data:image")) {
                            try {
                                byte[] decodedBytes = android.util.Base64.decode(imageUrl.split(",")[1], android.util.Base64.DEFAULT);
                                com.bumptech.glide.Glide.with(this).load(decodedBytes).centerCrop().into(ivArt);
                            } catch (Exception e) {}
                        } else {
                            com.bumptech.glide.Glide.with(this).load(imageUrl).centerCrop().into(ivArt);
                        }
                    }
                    
                    // Fetch Author Name
                    if (post.getUid() != null) {
                        db.collection("Users").document(post.getUid()).get().addOnSuccessListener(userDoc -> {
                            if (userDoc.exists() && userDoc.contains("profile")) {
                                java.util.Map<String, Object> profile = (java.util.Map<String, Object>) userDoc.get("profile");
                                if (profile != null && profile.containsKey("fullName")) {
                                    if (tvAuthor != null) tvAuthor.setText("Bởi " + profile.get("fullName"));
                                } else {
                                    if (tvAuthor != null) tvAuthor.setText("Bởi Người dùng");
                                }
                            }
                        });
                    }
                    
                    artworkView.setOnClickListener(v -> {
                        Intent intent = new Intent(getActivity(), com.example.appdraw.community.PostDetailActivity.class);
                        intent.putExtra("POST_ID", post.getId());
                        startActivity(intent);
                    });
                    
                    container.addView(artworkView);
                }
            });
    }

    private void setupDynamicMentors(View view) {
        android.widget.LinearLayout container = view.findViewById(R.id.ll_mentors_container);
        if (container == null)
            return;

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("Users")
                .whereEqualTo("role", "mentor")
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (getContext() == null)
                        return;
                    LayoutInflater inflater = LayoutInflater.from(getContext());

                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        java.util.Map<String, Object> profile = (java.util.Map<String, Object>) doc.get("profile");
                        if (profile == null)
                            continue;

                        String name = (String) profile.get("fullName");
                        String bio = (String) profile.get("bio");
                        String avatarUrl = (String) profile.get("avatarUrl");
                        String artistId = doc.getId();

                        View artistView = inflater.inflate(R.layout.item_artist, container, false);
                        TextView tvName = artistView.findViewById(R.id.tv_artist_name);
                        ImageView ivArtist = artistView.findViewById(R.id.iv_artist);
                        ImageView ivVerified = artistView.findViewById(R.id.iv_verified);

                        if (tvName != null)
                            tvName.setText(name != null ? name : "Chuyên gia");
                        if (ivVerified != null)
                            ivVerified.setVisibility(View.VISIBLE); // Hiển thị tích xanh

                        if (ivArtist != null && avatarUrl != null && !avatarUrl.isEmpty()) {
                            com.bumptech.glide.Glide.with(this).load(avatarUrl).circleCrop().into(ivArtist);
                        }

                        artistView.setOnClickListener(v -> {
                            Intent intent = new Intent(getActivity(), ArtistDetailActivity.class);
                            intent.putExtra("ARTIST_ID", artistId);
                            intent.putExtra("ARTIST_NAME", name);
                            intent.putExtra("ARTIST_BIO", bio);
                            intent.putExtra("ARTIST_AVATAR", avatarUrl);
                            startActivity(intent);
                        });

                        container.addView(artistView);
                    }
                });
    }
}
