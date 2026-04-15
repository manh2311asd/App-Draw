package com.example.appdraw.explore;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.appdraw.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ArtistDetailActivity extends AppCompatActivity {
    private String artistId;
    private boolean isFollowing = false;
    private FirebaseFirestore db;
    private String currentUid;

    private MaterialButton btnFollow;
    private TextView tvFollowersCount;

    private com.example.appdraw.community.PostMediaAdapter postAdapter;
    private java.util.List<com.example.appdraw.model.Post> artworkList = new java.util.ArrayList<>();
    private androidx.recyclerview.widget.RecyclerView rvArtworks;
    private LinearLayout llEmptyArtworks;

    private LinearLayout llArtistLessonsContainer;
    private TextView tvEmptyLessons;

    private MaterialButton btnCreateLesson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        artistId = getIntent().getStringExtra("ARTIST_ID");
        String name = getIntent().getStringExtra("ARTIST_NAME");
        String avatarUrl = getIntent().getStringExtra("ARTIST_AVATAR");
        String bio = getIntent().getStringExtra("ARTIST_BIO");

        if (name != null) ((TextView) findViewById(R.id.tv_artist_name_detail)).setText(name);
        if (bio != null) ((TextView) findViewById(R.id.tv_artist_bio)).setText(bio);
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this).load(avatarUrl).circleCrop().into((ImageView) findViewById(R.id.iv_artist_large));
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Setup Tabs
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        LinearLayout llTabLessons = findViewById(R.id.ll_tab_lessons);
        LinearLayout llTabArtworks = findViewById(R.id.ll_tab_artworks);

        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 0) {
                        // Tác phẩm
                        llTabLessons.setVisibility(View.GONE);
                        llTabArtworks.setVisibility(View.VISIBLE);
                    } else {
                        // Khóa học
                        llTabLessons.setVisibility(View.VISIBLE);
                        llTabArtworks.setVisibility(View.GONE);
                    }
                }
                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

        // Setup Artworks Grid
        rvArtworks = findViewById(R.id.rv_artist_artworks);
        llEmptyArtworks = findViewById(R.id.ll_empty_artist_artworks);
        if (rvArtworks != null) {
            rvArtworks.setLayoutManager(new androidx.recyclerview.widget.StaggeredGridLayoutManager(2, androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL));
            postAdapter = new com.example.appdraw.community.PostMediaAdapter(artworkList, post -> {
                android.content.Intent intent = new android.content.Intent(this, com.example.appdraw.community.PostDetailActivity.class);
                intent.putExtra("POST_ID", post.getId());
                startActivity(intent);
            });
            rvArtworks.setAdapter(postAdapter);
            
            fetchArtistPosts();
        }

        llArtistLessonsContainer = findViewById(R.id.ll_artist_lessons_container);
        tvEmptyLessons = findViewById(R.id.tv_empty_lessons);
        fetchArtistLessons();

        // Setup Follow System
        btnFollow = findViewById(R.id.btn_follow);
        tvFollowersCount = findViewById(R.id.tv_followers_count);
        btnCreateLesson = findViewById(R.id.btn_create_lesson);

        if (artistId != null && currentUid != null) {
            getFollowersCount(); // LUÔN LUÔN hiển thị số người theo dõi

            if (artistId.equals(currentUid)) {
                btnFollow.setVisibility(View.GONE);
                checkMentorRoleAndShowCreateButton();
            } else {
                checkFollowStatus();
                btnFollow.setOnClickListener(v -> toggleFollow());
            }
        } else {
            btnFollow.setVisibility(View.GONE);
        }
    }

    private void checkMentorRoleAndShowCreateButton() {
        db.collection("Users").document(currentUid).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String role = documentSnapshot.getString("role");
                    if (role != null && role.equalsIgnoreCase("mentor")) {
                        if (btnCreateLesson != null) {
                            btnCreateLesson.setVisibility(View.VISIBLE);
                            btnCreateLesson.setOnClickListener(v -> {
                                startActivity(new android.content.Intent(ArtistDetailActivity.this, CreateLessonActivity.class));
                            });
                        }
                    }
                }
            });
    }

    private void checkFollowStatus() {
        db.collection("Follows").document(currentUid + "_" + artistId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    isFollowing = true;
                    updateFollowButtonUI();
                }
            });
    }

    private void getFollowersCount() {
        db.collection("Follows")
            .whereEqualTo("following", artistId)
            .count()
            .get(com.google.firebase.firestore.AggregateSource.SERVER)
            .addOnSuccessListener(task -> {
                tvFollowersCount.setText(task.getCount() + " người theo dõi");
            });
    }

    private void toggleFollow() {
        btnFollow.setEnabled(false); // disable while processing
        DocumentReference followRef = db.collection("Follows").document(currentUid + "_" + artistId);

        if (isFollowing) {
            // Unfollow
            followRef.delete().addOnSuccessListener(aVoid -> {
                isFollowing = false;
                
                db.collection("Users").document(artistId).update("followersCount", FieldValue.increment(-1));
                db.collection("Users").document(currentUid).update("followingCount", FieldValue.increment(-1));
                
                updateFollowButtonUI();
                getFollowersCount();
                btnFollow.setEnabled(true);
            }).addOnFailureListener(e -> btnFollow.setEnabled(true));
        } else {
            // Follow
            java.util.Map<String, Object> followData = new java.util.HashMap<>();
            followData.put("follower", currentUid);
            followData.put("following", artistId);
            followData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
            
            followRef.set(followData).addOnSuccessListener(aVoid -> {
                isFollowing = true;
                
                db.collection("Users").document(artistId).update("followersCount", FieldValue.increment(1));
                db.collection("Users").document(currentUid).update("followingCount", FieldValue.increment(1));
                
                com.example.appdraw.utils.NotificationHelper.sendNotification(artistId, "FOLLOW", "rất thích các tác phẩm và đã bắt đầu theo dõi bạn.", currentUid);
                
                updateFollowButtonUI();
                getFollowersCount();
                btnFollow.setEnabled(true);
            }).addOnFailureListener(e -> btnFollow.setEnabled(true));
        }
    }

    private void updateFollowButtonUI() {
        if (isFollowing) {
            btnFollow.setText("Đang theo dõi");
            btnFollow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE0E0E0)); // Grey
            btnFollow.setTextColor(0xFF555555);
        } else {
            btnFollow.setText("Theo dõi");
            btnFollow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4272D0)); // Primary Blue
            btnFollow.setTextColor(0xFFFFFFFF);
        }
    }

    private void fetchArtistPosts() {
        if (artistId == null) return;
        db.collection("Posts").whereEqualTo("uid", artistId).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                artworkList.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    com.example.appdraw.model.Post post = doc.toObject(com.example.appdraw.model.Post.class);
                    if (post != null && post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                        artworkList.add(post);
                    }
                }
                
                // Sắp xếp mới nhất lên đầu
                artworkList.sort((p1, p2) -> Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));
                
                if (artworkList.isEmpty()) {
                    if (rvArtworks != null) rvArtworks.setVisibility(View.GONE);
                    if (llEmptyArtworks != null) llEmptyArtworks.setVisibility(View.VISIBLE);
                } else {
                    if (rvArtworks != null) rvArtworks.setVisibility(View.VISIBLE);
                    if (llEmptyArtworks != null) llEmptyArtworks.setVisibility(View.GONE);
                    if (postAdapter != null) postAdapter.notifyDataSetChanged();
                }
            });
    }

    private void fetchArtistLessons() {
        if (artistId == null) return;
        db.collection("Lessons").whereEqualTo("authorId", artistId).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (llArtistLessonsContainer == null) return;
                llArtistLessonsContainer.removeAllViews();
                
                if (queryDocumentSnapshots.isEmpty()) {
                    if (tvEmptyLessons != null) tvEmptyLessons.setVisibility(View.VISIBLE);
                    return;
                }
                
                if (tvEmptyLessons != null) tvEmptyLessons.setVisibility(View.GONE);
                
                java.util.List<com.google.firebase.firestore.DocumentSnapshot> lessons = new java.util.ArrayList<>();
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    lessons.add(doc);
                }
                
                // Sort by createdAt descending
                lessons.sort((d1, d2) -> {
                    Long c1 = d1.getLong("createdAt");
                    Long c2 = d2.getLong("createdAt");
                    if (c1 != null && c2 != null) return Long.compare(c2, c1);
                    return 0;
                });
                
                android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
                for (com.google.firebase.firestore.DocumentSnapshot doc : lessons) {
                    View lessonView = inflater.inflate(R.layout.item_lesson_list, llArtistLessonsContainer, false);
                    android.widget.TextView tvTitle = lessonView.findViewById(R.id.tv_lesson_title);
                    android.widget.TextView tvAuthor = lessonView.findViewById(R.id.tv_author);
                    android.widget.ImageView ivThumb = lessonView.findViewById(R.id.iv_lesson_thumb);
                    android.widget.TextView tvStatus = lessonView.findViewById(R.id.tv_status);
                    android.widget.TextView tvDuration = lessonView.findViewById(R.id.tv_duration);
                    android.widget.RatingBar rb = lessonView.findViewById(R.id.rating_bar);
                    
                    String title = doc.getString("title");
                    String author = doc.getString("authorName");
                    if (author == null) author = doc.getString("author");
                    String imageUrl = doc.getString("thumbnailUrl");
                    if (imageUrl == null) imageUrl = doc.getString("imageUrl");
                    
                    if (tvTitle != null) tvTitle.setText(title);
                    if (tvAuthor != null) tvAuthor.setText(author != null ? author : "");
                    
                    if (ivThumb != null && imageUrl != null && !imageUrl.isEmpty()) {
                        if (imageUrl.startsWith("data:image")) {
                            try {
                                byte[] imageByteArray = android.util.Base64.decode(imageUrl.split(",")[1], android.util.Base64.DEFAULT);
                                com.bumptech.glide.Glide.with(this).load(imageByteArray).centerCrop().into(ivThumb);
                            } catch (Exception e) {}
                        } else {
                            com.bumptech.glide.Glide.with(this).load(imageUrl).centerCrop().into(ivThumb);
                        }
                    }
                    
                    if (rb != null) {
                        Double rating = doc.getDouble("rating");
                        if (rating != null) rb.setRating(rating.floatValue());
                        else rb.setRating(5.0f);
                    }
                    
                    if (tvDuration != null) {
                        Long duration = doc.getLong("durationMin");
                        if (duration == null) duration = doc.getLong("duration");
                        if (duration != null && duration > 0) {
                            tvDuration.setText(duration + " min");
                        } else {
                            tvDuration.setText("30 min");
                        }
                    }
                    
                    if (tvStatus != null) {
                        tvStatus.setText("Xem chi tiết");
                        tvStatus.setBackgroundResource(R.drawable.bg_badge_completed);
                        tvStatus.setTextColor(android.graphics.Color.WHITE);
                    }
                    
                    final String safeAuthor = author;
                    lessonView.setOnClickListener(v -> {
                        android.content.Intent intent = new android.content.Intent(ArtistDetailActivity.this, LessonDetailActivity.class);
                        intent.putExtra("LESSON_TITLE", title);
                        intent.putExtra("LESSON_ID", doc.getId());
                        intent.putExtra("CATEGORY", doc.getString("category"));
                        intent.putExtra("IMAGE_RES", doc.getString("imageRes"));
                        intent.putExtra("AUTHOR", safeAuthor);
                        startActivity(intent);
                    });
                    
                    llArtistLessonsContainer.addView(lessonView);
                }
            });
    }
}
