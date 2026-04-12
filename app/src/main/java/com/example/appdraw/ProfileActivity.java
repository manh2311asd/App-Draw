package com.example.appdraw;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.appdraw.project.ProjectListActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView tabArtwork, tabPost, tabProject, tabSaved;
    private androidx.recyclerview.widget.RecyclerView rvProfileArtworks;
    private LinearLayout llEmptyArtworks;
    private com.example.appdraw.community.PostMediaAdapter postAdapter;
    private java.util.List<com.example.appdraw.model.Post> postList = new java.util.ArrayList<>();
    private java.util.List<com.example.appdraw.model.Post> allPostList = new java.util.ArrayList<>();
    private LinearLayout llProfilePosts;
    private int activeTab = 1;

    private final androidx.activity.result.ActivityResultLauncher<Intent> avatarLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    uploadNewAvatar(result.getData().getData());
                }
            }
    );

    private void pickNewAvatar() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        avatarLauncher.launch(intent);
    }

    private void uploadNewAvatar(android.net.Uri uri) {
        try {
            android.graphics.Bitmap bitmap;
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                android.graphics.ImageDecoder.Source source = android.graphics.ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = android.graphics.ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
            
            // Resize to standard avatar size (200x200) to keep base64 extremely small
            android.graphics.Bitmap scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 200, 200, true);
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, buffer);
            byte[] fileBytes = buffer.toByteArray();
            
            String base64Image = "data:image/jpeg;base64," + android.util.Base64.encodeToString(fileBytes, android.util.Base64.DEFAULT);
            
            String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
            if (uid != null) {
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("Users").document(uid)
                    .update("profile.avatarUrl", base64Image)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show();
                        android.widget.ImageView ivProfileAvatar = findViewById(R.id.iv_profile_avatar);
                        byte[] decodedBytes = android.util.Base64.decode(base64Image.split(",")[1], android.util.Base64.DEFAULT);
                        com.bumptech.glide.Glide.with(this).load(decodedBytes).circleCrop().into(ivProfileAvatar);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi đọc ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupToolbar();
        setupTabs();

        // Kiểm tra xem có yêu cầu mở tab Dự án không
        if (getIntent().getBooleanExtra("OPEN_PROJECT_TAB", false)) {
            openProjectTab();
        }

        // Check if viewing other user profile
        if (getIntent().getBooleanExtra("IS_OTHER_USER", false)) {
            String otherName = getIntent().getStringExtra("USER_NAME");
            TextView tvName = findViewById(R.id.tv_profile_name);
            if (tvName != null && otherName != null) {
                tvName.setText(otherName);
            }
        } else {
            loadCurrentUserProfile();
            
            // Allow changing avatar
            View ivAvatar = findViewById(R.id.iv_profile_avatar);
            if (ivAvatar != null) {
                ivAvatar.setOnClickListener(v -> pickNewAvatar());
            }
        }

        // Xử lý nút Trực tiếp
        View llLiveStatus = findViewById(R.id.ll_live_status);
        if (llLiveStatus != null) {
            llLiveStatus.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, LiveStreamActivity.class);
                startActivity(intent);
            });
        }
    }

    private void initViews() {
        tabArtwork = findViewById(R.id.tab_artwork);
        tabPost = findViewById(R.id.tab_post);
        tabProject = findViewById(R.id.tab_project);
        tabSaved = findViewById(R.id.tab_saved);

        rvProfileArtworks = findViewById(R.id.rv_profile_artworks);
        llEmptyArtworks = findViewById(R.id.ll_empty_artworks);

        llProfilePosts = findViewById(R.id.ll_profile_posts);
        
        if (rvProfileArtworks != null) {
            rvProfileArtworks.setLayoutManager(new androidx.recyclerview.widget.StaggeredGridLayoutManager(2, androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL));
            postAdapter = new com.example.appdraw.community.PostMediaAdapter(postList, post -> {
                Intent intent = new Intent(this, com.example.appdraw.community.PostDetailActivity.class);
                intent.putExtra("POST_ID", post.getId());
                startActivity(intent);
            });
            rvProfileArtworks.setAdapter(postAdapter);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        android.widget.ImageView btnSettings = findViewById(R.id.btn_profile_settings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> showSettingsDialog());
        }
    }

    private void setupTabs() {

        View btnStartDrawing = findViewById(R.id.btn_start_drawing);
        if (btnStartDrawing != null) {
            btnStartDrawing.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, com.example.appdraw.drawing.DrawingActivity.class);
                startActivity(intent);
            });
        }

        tabArtwork.setOnClickListener(v -> {
            activeTab = 0;
            resetTabs();
            tabArtwork.setTextColor(getResources().getColor(R.color.primary_blue));
            tabArtwork.setTypeface(null, android.graphics.Typeface.BOLD);
            if (llProfilePosts != null) llProfilePosts.setVisibility(View.GONE);
            if (postList.isEmpty()) {
                llEmptyArtworks.setVisibility(View.VISIBLE);
                rvProfileArtworks.setVisibility(View.GONE);
                if (llEmptyArtworks.getChildCount() > 2 && llEmptyArtworks.getChildAt(1) instanceof TextView) {
                    ((TextView)llEmptyArtworks.getChildAt(1)).setText("Chưa có tác phẩm nào");
                }
            } else {
                llEmptyArtworks.setVisibility(View.GONE);
                rvProfileArtworks.setVisibility(View.VISIBLE);
            }
        });

        tabPost.setOnClickListener(v -> {
            activeTab = 1;
            resetTabs();
            tabPost.setTextColor(getResources().getColor(R.color.primary_blue));
            tabPost.setTypeface(null, android.graphics.Typeface.BOLD);
            if (rvProfileArtworks != null) rvProfileArtworks.setVisibility(View.GONE);
            if (allPostList.isEmpty()) {
                llEmptyArtworks.setVisibility(View.VISIBLE);
                if (llProfilePosts != null) llProfilePosts.setVisibility(View.GONE);
                if (llEmptyArtworks.getChildCount() > 2 && llEmptyArtworks.getChildAt(1) instanceof TextView) {
                    ((TextView)llEmptyArtworks.getChildAt(1)).setText("Chưa có bài viết nào");
                }
            } else {
                llEmptyArtworks.setVisibility(View.GONE);
                if (llProfilePosts != null) llProfilePosts.setVisibility(View.VISIBLE);
            }
        });

        tabProject.setOnClickListener(v -> {
            openProjectTab();
            // Chuyển sang trang danh sách dự án
            Intent intent = new Intent(ProfileActivity.this, ProjectListActivity.class);
            startActivity(intent);
        });

        tabSaved.setOnClickListener(v -> {
            resetTabs();
            tabSaved.setTextColor(getResources().getColor(R.color.primary_blue));
            tabSaved.setTypeface(null, android.graphics.Typeface.BOLD);
            if (llEmptyArtworks != null) llEmptyArtworks.setVisibility(View.GONE);
            if (rvProfileArtworks != null) rvProfileArtworks.setVisibility(View.GONE);
            Toast.makeText(this, "Mục Đã lưu", Toast.LENGTH_SHORT).show();
        });
    }

    private void openProjectTab() {
        resetTabs();
        tabProject.setTextColor(getResources().getColor(R.color.primary_blue));
        tabProject.setTypeface(null, android.graphics.Typeface.BOLD);
        if (llEmptyArtworks != null) llEmptyArtworks.setVisibility(View.GONE);
        if (rvProfileArtworks != null) rvProfileArtworks.setVisibility(View.GONE);
    }

    private void resetTabs() {
        int gray = Color.parseColor("#888888");
        tabArtwork.setTextColor(gray);
        tabArtwork.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabPost.setTextColor(gray);
        tabPost.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabProject.setTextColor(gray);
        tabProject.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabSaved.setTextColor(gray);
        tabSaved.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    private void showSettingsDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_profile_settings, null);
        dialog.setContentView(view);
        
        view.findViewById(R.id.ll_setting_account).setOnClickListener(v -> {
            Toast.makeText(this, "Cài đặt tài khoản", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        
        view.findViewById(R.id.ll_setting_edit_profile).setOnClickListener(v -> {
            Toast.makeText(this, "Chỉnh sửa hồ sơ", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        
        view.findViewById(R.id.ll_setting_language).setOnClickListener(v -> {
            Toast.makeText(this, "Cài đặt ngôn ngữ", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        
        view.findViewById(R.id.ll_setting_personal).setOnClickListener(v -> {
            Toast.makeText(this, "Liên kết cá nhân", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        
        view.findViewById(R.id.ll_logout).setOnClickListener(v -> {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, com.example.appdraw.auth.LoginOptionsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            dialog.dismiss();
        });
        
        dialog.show();
    }

    private void loadCurrentUserProfile() {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Tải số liệu thực tế Followers, Following, Post
                        Long followers = documentSnapshot.getLong("followersCount");
                        Long following = documentSnapshot.getLong("followingCount");
                        Long posts = documentSnapshot.getLong("postCount");
                        
                        TextView tvFollowers = findViewById(R.id.tv_profile_followers);
                        TextView tvFollowing = findViewById(R.id.tv_profile_following);
                        TextView tvPosts = findViewById(R.id.tv_profile_posts);
                        
                        if (tvFollowers != null) tvFollowers.setText(followers != null ? String.valueOf(followers) : "0");
                        if (tvFollowing != null) tvFollowing.setText(following != null ? String.valueOf(following) : "0");
                        if (tvPosts != null) tvPosts.setText(posts != null ? String.valueOf(posts) : "0");

                        // Tải Avatar
                        String photoUrl = documentSnapshot.getString("photoUrl");
                        if (photoUrl == null || photoUrl.isEmpty()) photoUrl = documentSnapshot.getString("avatar");
                        if (photoUrl == null || photoUrl.isEmpty()) {
                            if (user.getPhotoUrl() != null) photoUrl = user.getPhotoUrl().toString();
                        }
                        
                        android.widget.ImageView ivAvatar = findViewById(R.id.iv_profile_avatar);
                        
                        // Default flag indicating if an avatar was found
                        boolean hasAvatar = false;
                        
                        if (ivAvatar != null) {
                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                hasAvatar = true;
                                if (photoUrl.startsWith("data:image")) {
                                    byte[] decodedBytes = android.util.Base64.decode(photoUrl.split(",")[1], android.util.Base64.DEFAULT);
                                    com.bumptech.glide.Glide.with(this).load(decodedBytes).circleCrop().into(ivAvatar);
                                } else {
                                    com.bumptech.glide.Glide.with(this).load(photoUrl).circleCrop().into(ivAvatar);
                                }
                            }
                        }

                        // Tải tags (Kinh nghiệm, Sở thích)
                        String interest = documentSnapshot.getString("interest");
                        String level = documentSnapshot.getString("level");

                        TextView tvTag1 = findViewById(R.id.tv_tag_1);
                        TextView tvTag2 = findViewById(R.id.tv_tag_2);
                        TextView tvTag3 = findViewById(R.id.tv_tag_3);

                        if (tvTag1 != null) {
                            if (interest != null && !interest.isEmpty()) {
                                tvTag1.setText(interest);
                                tvTag1.setVisibility(View.VISIBLE);
                            }
                        }
                        if (tvTag2 != null) {
                            if (level != null && !level.isEmpty()) {
                                tvTag2.setText(level);
                                tvTag2.setVisibility(View.VISIBLE);
                            }
                        }

                        if (documentSnapshot.contains("profile")) {
                            java.util.Map<String, Object> profile = (java.util.Map<String, Object>) documentSnapshot.get("profile");
                            if (profile != null) {
                                String fullName = (String) profile.get("fullName");
                                String bio = (String) profile.get("bio");
                                String avatarUrl = (String) profile.get("avatarUrl");
                                
                                TextView tvName = findViewById(R.id.tv_profile_name);
                                TextView tvBio = findViewById(R.id.tv_profile_bio);
                                android.widget.ImageView ivProfileAvatar = findViewById(R.id.iv_profile_avatar);

                                if (tvName != null && fullName != null && !fullName.isEmpty()) tvName.setText(fullName);
                                if (tvBio != null && bio != null && !bio.isEmpty()) tvBio.setText(bio);
                                if (ivProfileAvatar != null && avatarUrl != null && !avatarUrl.isEmpty()) {
                                    hasAvatar = true;
                                    if (avatarUrl.startsWith("data:image")) {
                                        byte[] b = android.util.Base64.decode(avatarUrl.split(",")[1], android.util.Base64.DEFAULT);
                                        com.bumptech.glide.Glide.with(this).load(b).circleCrop().into(ivProfileAvatar);
                                    } else {
                                        com.bumptech.glide.Glide.with(this).load(avatarUrl).circleCrop().into(ivProfileAvatar);
                                    }
                                }
                            }
                        }
                        
                        // Nếu vẫn không có ảnh (photoUrl rỗng và avatarUrl rỗng) => dùng màu trắng làm fallback
                        if (!hasAvatar && ivAvatar != null) {
                            com.bumptech.glide.Glide.with(this).load(R.drawable.ic_default_user).circleCrop().into(ivAvatar);
                        }
                    }
                });

            // Tải danh sách Posts có hình ảnh giống Twitter Media
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("Posts")
                .whereEqualTo("uid", user.getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    postList.clear();
                    allPostList.clear();
                    if (value != null && !value.isEmpty()) {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : value) {
                            com.example.appdraw.model.Post post = doc.toObject(com.example.appdraw.model.Post.class);
                            allPostList.add(post);
                            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                                postList.add(post);
                            }
                        }
                    }
                    postList.sort((p1, p2) -> Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));
                    allPostList.sort((p1, p2) -> Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));
                    
                    if (postAdapter != null) postAdapter.notifyDataSetChanged();
                    renderTwitterLikePosts(allPostList);

                    if (activeTab == 0 && tabArtwork != null) {
                        tabArtwork.performClick();
                    } else if (activeTab == 1 && tabPost != null) {
                        tabPost.performClick();
                    }
                });
        }
    }

    private void renderTwitterLikePosts(java.util.List<com.example.appdraw.model.Post> posts) {
        if (llProfilePosts == null) return;
        llProfilePosts.removeAllViews();
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();

        for (com.example.appdraw.model.Post post : posts) {
            View postView = inflater.inflate(R.layout.item_post, llProfilePosts, false);
            
            android.widget.ImageView ivPostImg = postView.findViewById(R.id.iv_post_image);
            if (ivPostImg != null) {
                if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                    ivPostImg.setVisibility(View.VISIBLE);
                    if (post.getImageUrl().startsWith("data:image")) {
                        byte[] decodedBytes = android.util.Base64.decode(post.getImageUrl().split(",")[1], android.util.Base64.DEFAULT);
                        com.bumptech.glide.Glide.with(this).load(decodedBytes).into(ivPostImg);
                    } else {
                        com.bumptech.glide.Glide.with(this).load(post.getImageUrl()).into(ivPostImg);
                    }
                    ivPostImg.setOnClickListener(v -> {
                        Intent intent = new Intent(ProfileActivity.this, com.example.appdraw.community.FullScreenImageActivity.class);
                        intent.putExtra("IMAGE_URL", post.getImageUrl());
                        startActivity(intent);
                    });
                } else {
                    ivPostImg.setVisibility(View.GONE);
                }
            }

            TextView tvContent = postView.findViewById(R.id.tv_post_content);
            if (tvContent != null) tvContent.setText(post.getContent());

            TextView tvFollowStatus = postView.findViewById(R.id.tv_follow_status);
            if (tvFollowStatus != null) tvFollowStatus.setVisibility(View.GONE); // Hide "Theo dõi" in self profile

            TextView tvCommentCount = postView.findViewById(R.id.tv_comment_count);
            if (tvCommentCount != null) tvCommentCount.setText(String.valueOf(post.getCommentsCount()));

            // User Info (Fetch from Firebase to avoid race conditions with Profile header loading)
            TextView tvName = postView.findViewById(R.id.tv_user_name);
            android.widget.ImageView ivAvatar = postView.findViewById(R.id.iv_user_avatar);
            
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("Users").document(post.getUid())
                .get().addOnSuccessListener(userDoc -> {
                    if (userDoc.exists() && userDoc.contains("profile")) {
                        java.util.Map<String, Object> profile = (java.util.Map<String, Object>) userDoc.get("profile");
                        if (profile != null) {
                            String fullName = (String) profile.get("fullName");
                            String avatarUrl = (String) profile.get("avatarUrl");
                            if (tvName != null && fullName != null) tvName.setText(fullName);
                            if (ivAvatar != null) {
                                if (avatarUrl != null && !avatarUrl.isEmpty() && avatarUrl.startsWith("data:image")) {
                                    byte[] b = android.util.Base64.decode(avatarUrl.split(",")[1], android.util.Base64.DEFAULT);
                                    com.bumptech.glide.Glide.with(this).load(b).circleCrop().into(ivAvatar);
                                } else if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                    com.bumptech.glide.Glide.with(this).load(avatarUrl).circleCrop().into(ivAvatar);
                                } else {
                                    com.bumptech.glide.Glide.with(this).load(R.drawable.ic_default_user).circleCrop().into(ivAvatar);
                                }
                            }
                        }
                    }
                });

            View llComment = postView.findViewById(R.id.ll_comment);
            if (llComment != null) {
                llComment.setOnClickListener(v -> {
                    Intent intent = new Intent(ProfileActivity.this, com.example.appdraw.community.PostDetailActivity.class);
                    intent.putExtra("POST_ID", post.getId());
                    startActivity(intent);
                });
            }

            // Likes
            View llLike = postView.findViewById(R.id.ll_like);
            android.widget.ImageView ivLike = postView.findViewById(R.id.iv_like);
            TextView tvLikeCount = postView.findViewById(R.id.tv_like_count);
            if (llLike != null && ivLike != null && tvLikeCount != null) {
                boolean isLiked = post.getLikedBy().contains(currentUid);
                tvLikeCount.setText(String.valueOf(post.getLikesCount()));
                if (isLiked) {
                    ivLike.setImageResource(R.drawable.ic_heart);
                    ivLike.setColorFilter(android.graphics.Color.parseColor("#E91E63"));
                } else {
                    ivLike.setColorFilter(android.graphics.Color.parseColor("#888888"));
                }
                llLike.setOnClickListener(v -> {
                    if (currentUid == null) return;
                    com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("Posts").document(post.getId())
                        .get().addOnSuccessListener(doc -> {
                            if (!doc.exists()) return;
                            com.example.appdraw.model.Post p = doc.toObject(com.example.appdraw.model.Post.class);
                            if (p != null) {
                                if (p.getLikedBy().contains(currentUid)) {
                                    p.getLikedBy().remove(currentUid);
                                    p.setLikesCount(Math.max(0, p.getLikesCount() - 1));
                                } else {
                                    p.getLikedBy().add(currentUid);
                                    p.setLikesCount(p.getLikesCount() + 1);
                                }
                                doc.getReference().set(p);
                            }
                        });
                });
            }
            llProfilePosts.addView(postView);
        }
    }
}
