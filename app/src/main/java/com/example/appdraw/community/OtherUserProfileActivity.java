package com.example.appdraw.community;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appdraw.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import android.view.View;
import android.widget.LinearLayout;

public class OtherUserProfileActivity extends AppCompatActivity {

    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            finish();
            return;
        }

        String currentUid = FirebaseAuth.getInstance().getUid();

        // Setup Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_profile);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
        
        // Hide settings button
        View btnSettings = findViewById(R.id.btn_profile_settings);
        if (btnSettings != null) btnSettings.setVisibility(android.view.View.GONE);
        
        // Hide "Vẽ ngay" button
        View btnStartDrawing = findViewById(R.id.btn_start_drawing);
        if (btnStartDrawing != null) btnStartDrawing.setVisibility(android.view.View.GONE);

        // Bind views
        TextView tvOtherName = findViewById(R.id.tv_profile_name);
        ImageView ivAvatar = findViewById(R.id.iv_profile_avatar);
        TextView tvBio = findViewById(R.id.tv_profile_bio);
        com.google.android.material.button.MaterialButton btnFollow = findViewById(R.id.btn_follow);

        TextView tvFollowers = findViewById(R.id.tv_profile_followers);
        TextView tvFollowing = findViewById(R.id.tv_profile_following);
        TextView tvPosts = findViewById(R.id.tv_profile_posts);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Setup Follow Button
        if (btnFollow != null) {
            if (userId.equals(currentUid)) {
                btnFollow.setVisibility(android.view.View.GONE);
            } else {
                btnFollow.setVisibility(android.view.View.VISIBLE);
                if (currentUid != null) {
                    com.google.firebase.firestore.DocumentReference followRef = db.collection("Follows").document(currentUid + "_" + userId);
                    followRef.addSnapshotListener(this, (doc, e) -> {
                        if (e != null) return;
                        if (doc != null && doc.exists()) {
                            isFollowing = true;
                            btnFollow.setText("Đang theo dõi");
                            btnFollow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                        } else {
                            isFollowing = false;
                            btnFollow.setText("+ Theo dõi");
                            btnFollow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4272D0")));
                        }
                        btnFollow.setEnabled(true);
                    });

                    btnFollow.setOnClickListener(v -> {
                        btnFollow.setEnabled(false);
                        if (!isFollowing) {
                            java.util.Map<String, Object> data = new java.util.HashMap<>();
                            data.put("follower", currentUid);
                            data.put("following", userId);
                            data.put("timestamp", System.currentTimeMillis());
                            followRef.set(data).addOnSuccessListener(aVoid -> {
                                db.collection("Users").document(userId).update("followersCount", com.google.firebase.firestore.FieldValue.increment(1));
                                db.collection("Users").document(currentUid).update("followingCount", com.google.firebase.firestore.FieldValue.increment(1));
                                Toast.makeText(this, "Đã theo dõi", Toast.LENGTH_SHORT).show();
                                com.example.appdraw.utils.NotificationHelper.sendNotification(userId, "FOLLOW", "đánh giá cao tác phẩm và bắt đầu theo dõi bạn.", currentUid);
                            });
                        } else {
                            followRef.delete().addOnSuccessListener(aVoid -> {
                                db.collection("Users").document(userId).update("followersCount", com.google.firebase.firestore.FieldValue.increment(-1));
                                db.collection("Users").document(currentUid).update("followingCount", com.google.firebase.firestore.FieldValue.increment(-1));
                                Toast.makeText(this, "Bỏ theo dõi", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }
            }
        }

        // Load User Info
        db.collection("Users").document(userId).addSnapshotListener(this, (doc, e) -> {
            if (e != null || doc == null || !doc.exists()) return;
            Long posts = doc.getLong("postCount");
            String role = doc.getString("role");
            
            long postsVal = posts != null ? Math.max(0, posts) : 0;
            
            ImageView ivMentorBadge = findViewById(R.id.iv_mentor_badge);
            if (ivMentorBadge != null) {
                if ("mentor".equals(role)) {
                    ivMentorBadge.setVisibility(android.view.View.VISIBLE);
                } else {
                    ivMentorBadge.setVisibility(android.view.View.GONE);
                }
            }
            
            if (tvPosts != null) tvPosts.setText(String.valueOf(postsVal));

            // Fetch followers logically by counting 'Follows' sub-documents natively
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("Follows")
                .whereEqualTo("following", userId)
                .count()
                .get(com.google.firebase.firestore.AggregateSource.SERVER)
                .addOnSuccessListener(task -> {
                    if (tvFollowers != null) tvFollowers.setText(String.valueOf(task.getCount()));
                });

            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("Follows")
                .whereEqualTo("follower", userId)
                .count()
                .get(com.google.firebase.firestore.AggregateSource.SERVER)
                .addOnSuccessListener(task -> {
                    if (tvFollowing != null) tvFollowing.setText(String.valueOf(task.getCount()));
                });
            if (tvPosts != null) tvPosts.setText(String.valueOf(postsVal));

                if (doc.contains("profile")) {
                    java.util.Map<String, Object> profile = (java.util.Map<String, Object>) doc.get("profile");
                    if (profile != null) {
                        if (tvOtherName != null) {
                            String fName = (String) profile.get("fullName");
                            tvOtherName.setText(fName);
                            TextView tvToolbar = findViewById(R.id.tv_toolbar_title);
                            if (tvToolbar != null && fName != null) {
                                tvToolbar.setText("Hồ sơ của " + fName);
                            }
                        }
                        if (tvBio != null) tvBio.setText((String) profile.get("bio"));
                        
                        String interest = doc.getString("interest");
                        TextView tvTag1 = findViewById(R.id.tv_tag_1);
                        TextView tvTag2 = findViewById(R.id.tv_tag_2);
                        TextView tvTag3 = findViewById(R.id.tv_tag_3);
                        if (tvTag1 != null) tvTag1.setVisibility(View.GONE);
                        if (tvTag2 != null) tvTag2.setVisibility(View.GONE);
                        if (tvTag3 != null) tvTag3.setVisibility(View.GONE);
                        if (interest != null && !interest.isEmpty()) {
                            String[] interests = interest.split(",");
                            if (interests.length > 0 && tvTag1 != null) {
                                tvTag1.setText(interests[0].trim());
                                tvTag1.setVisibility(View.VISIBLE);
                            }
                            if (interests.length > 1 && tvTag2 != null) {
                                tvTag2.setText(interests[1].trim());
                                tvTag2.setVisibility(View.VISIBLE);
                            }
                            if (interests.length > 2 && tvTag3 != null) {
                                tvTag3.setText(interests[2].trim());
                                tvTag3.setVisibility(View.VISIBLE);
                            }
                        }
                        
                        String avatarUrl = (String) profile.get("avatarUrl");
                        if (ivAvatar != null) {
                            if (avatarUrl != null && !avatarUrl.isEmpty() && avatarUrl.startsWith("data:image")) {
                                byte[] b = android.util.Base64.decode(avatarUrl.split(",")[1], android.util.Base64.DEFAULT);
                                Glide.with(this).load(b).circleCrop().into(ivAvatar);
                            } else if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Glide.with(this).load(avatarUrl).circleCrop().into(ivAvatar);
                            } else {
                                Glide.with(this).load(R.drawable.ic_default_user).circleCrop().into(ivAvatar);
                            }
                        }
                    }
                }
        });
        
        // Fetch their artworks
        LinearLayout llProfilePosts = findViewById(R.id.ll_profile_posts);
        androidx.recyclerview.widget.RecyclerView rvProfileArtworks = findViewById(R.id.rv_profile_artworks);
        java.util.List<com.example.appdraw.model.Post> postList = new java.util.ArrayList<>();
        java.util.List<com.example.appdraw.model.Post> allPostList = new java.util.ArrayList<>();
        final com.example.appdraw.community.PostMediaAdapter[] postAdapter = new com.example.appdraw.community.PostMediaAdapter[1];

        if (rvProfileArtworks != null) {
            rvProfileArtworks.setLayoutManager(new androidx.recyclerview.widget.StaggeredGridLayoutManager(2, androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL));
            postAdapter[0] = new com.example.appdraw.community.PostMediaAdapter(postList, post -> {
                Intent intent = new Intent(this, com.example.appdraw.community.PostDetailActivity.class);
                intent.putExtra("POST_ID", post.getId());
                startActivity(intent);
            });
            rvProfileArtworks.setAdapter(postAdapter[0]);
        }
        
        final int[] activeTab = {1}; // 1 = post, 0 = artwork
        
        db.collection("Posts")
            .whereEqualTo("uid", userId)
            .addSnapshotListener(this, (value, error) -> {
                if (error != null) return;
                postList.clear();
                allPostList.clear();
                if (value != null && !value.isEmpty()) {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot d : value) {
                        com.example.appdraw.model.Post post = d.toObject(com.example.appdraw.model.Post.class);
                        allPostList.add(post);
                        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                            postList.add(post);
                        }
                    }
                    allPostList.sort((p1, p2) -> Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));
                    postList.sort((p1, p2) -> Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));
                    
                    TextView profilePostsTv = findViewById(R.id.tv_profile_posts);
                    if (profilePostsTv != null) profilePostsTv.setText(String.valueOf(allPostList.size()));
                    
                    if (postAdapter[0] != null) postAdapter[0].notifyDataSetChanged();
                    renderTwitterLikePosts(llProfilePosts, allPostList);
                } else {
                    TextView profilePostsTv = findViewById(R.id.tv_profile_posts);
                    if (profilePostsTv != null) profilePostsTv.setText("0");
                }
                
                TextView tPost = findViewById(R.id.tab_post);
                TextView tArt = findViewById(R.id.tab_artwork);
                if (activeTab[0] == 1 && tPost != null) tPost.performClick();
                else if (activeTab[0] == 0 && tArt != null) tArt.performClick();
            });
            
        // Setup Tabs (3 tabs: Bài viết, Tác phẩm, Thành tích)
        TextView tabPost = findViewById(R.id.tab_post);
        TextView tabArtwork = findViewById(R.id.tab_artwork);
        TextView tabProject = findViewById(R.id.tab_project);
        TextView tabSaved = findViewById(R.id.tab_saved);
        
        if (tabPost != null && tabPost.getParent() instanceof LinearLayout) {
            ((LinearLayout)tabPost.getParent()).setVisibility(android.view.View.VISIBLE);
        }
        if (tabPost != null) {
            tabPost.setText("Bài viết");
            tabPost.setTextColor(android.graphics.Color.parseColor("#4272D0"));
            tabPost.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        if (tabArtwork != null) {
            tabArtwork.setText("Tác phẩm");
            tabArtwork.setTextColor(android.graphics.Color.parseColor("#888888"));
            tabArtwork.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
        if (tabProject != null) {
            tabProject.setText("Thành tích");
            tabProject.setTextColor(android.graphics.Color.parseColor("#888888"));
            tabProject.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
        if (tabSaved != null) {
            tabSaved.setVisibility(android.view.View.GONE);
        }
        
        if (tabPost != null && tabArtwork != null) {
            tabPost.setOnClickListener(v -> {
                activeTab[0] = 1;
                tabPost.setTextColor(android.graphics.Color.parseColor("#4272D0"));
                tabPost.setTypeface(null, android.graphics.Typeface.BOLD);
                tabArtwork.setTextColor(android.graphics.Color.parseColor("#888888"));
                tabArtwork.setTypeface(null, android.graphics.Typeface.NORMAL);
                
                if (rvProfileArtworks != null) rvProfileArtworks.setVisibility(android.view.View.GONE);
                LinearLayout emptyView = findViewById(R.id.ll_empty_artworks);
                
                if (allPostList.isEmpty()) {
                    if (llProfilePosts != null) llProfilePosts.setVisibility(android.view.View.GONE);
                    if (emptyView != null) {
                        emptyView.setVisibility(android.view.View.VISIBLE);
                        if (emptyView.getChildCount() > 2 && emptyView.getChildAt(1) instanceof TextView) {
                            ((TextView)emptyView.getChildAt(1)).setText("Người dùng này chưa có bài viết nào");
                        }
                        if (emptyView.getChildCount() > 2 && emptyView.getChildAt(2) instanceof TextView) {
                            ((TextView)emptyView.getChildAt(2)).setVisibility(android.view.View.GONE);
                        }
                    }
                } else {
                    if (llProfilePosts != null) llProfilePosts.setVisibility(android.view.View.VISIBLE);
                    if (emptyView != null) emptyView.setVisibility(android.view.View.GONE);
                }
            });
            tabArtwork.setOnClickListener(v -> {
                activeTab[0] = 0;
                tabArtwork.setTextColor(android.graphics.Color.parseColor("#4272D0"));
                tabArtwork.setTypeface(null, android.graphics.Typeface.BOLD);
                tabPost.setTextColor(android.graphics.Color.parseColor("#888888"));
                tabPost.setTypeface(null, android.graphics.Typeface.NORMAL);
                
                if (llProfilePosts != null) llProfilePosts.setVisibility(android.view.View.GONE);
                LinearLayout emptyView = findViewById(R.id.ll_empty_artworks);
                
                if (postList.isEmpty()) {
                    if (rvProfileArtworks != null) rvProfileArtworks.setVisibility(android.view.View.GONE);
                    if (emptyView != null) {
                        emptyView.setVisibility(android.view.View.VISIBLE);
                        if (emptyView.getChildCount() > 2 && emptyView.getChildAt(1) instanceof TextView) {
                            ((TextView)emptyView.getChildAt(1)).setText("Người dùng này chưa có tác phẩm nào");
                        }
                        if (emptyView.getChildCount() > 2 && emptyView.getChildAt(2) instanceof TextView) {
                            ((TextView)emptyView.getChildAt(2)).setVisibility(android.view.View.GONE);
                        }
                    }
                } else {
                    if (rvProfileArtworks != null) rvProfileArtworks.setVisibility(android.view.View.VISIBLE);
                    if (emptyView != null) emptyView.setVisibility(android.view.View.GONE);
                }
            });
        }
    }

    private void renderTwitterLikePosts(LinearLayout llProfilePosts, java.util.List<com.example.appdraw.model.Post> posts) {
        if (llProfilePosts == null) return;
        llProfilePosts.removeAllViews();
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();

        for (com.example.appdraw.model.Post post : posts) {
            View postView = inflater.inflate(R.layout.item_post, llProfilePosts, false);
            
            ImageView ivPostImg = postView.findViewById(R.id.iv_post_image);
            if (ivPostImg != null) {
                if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                    ivPostImg.setVisibility(android.view.View.VISIBLE);
                    if (post.getImageUrl().startsWith("data:image")) {
                        byte[] decodedBytes = android.util.Base64.decode(post.getImageUrl().split(",")[1], android.util.Base64.DEFAULT);
                        Glide.with(this).load(decodedBytes).into(ivPostImg);
                    } else {
                        Glide.with(this).load(post.getImageUrl()).into(ivPostImg);
                    }
                    ivPostImg.setOnClickListener(v -> {
                        Intent intent = new Intent(this, FullScreenImageActivity.class);
                        intent.putExtra("IMAGE_URL", post.getImageUrl());
                        startActivity(intent);
                    });
                } else {
                    ivPostImg.setVisibility(android.view.View.GONE);
                }
            }

            TextView tvContent = postView.findViewById(R.id.tv_post_content);
            if (tvContent != null) tvContent.setText(post.getContent());

            TextView tvFollowStatus = postView.findViewById(R.id.tv_follow_status);
            if (tvFollowStatus != null) tvFollowStatus.setVisibility(android.view.View.GONE);

            TextView tvCommentCount = postView.findViewById(R.id.tv_comment_count);
            if (tvCommentCount != null) tvCommentCount.setText(String.valueOf(post.getCommentsCount()));

            TextView tvName = postView.findViewById(R.id.tv_user_name);
            ImageView ivAvatar = postView.findViewById(R.id.iv_user_avatar);
            
            FirebaseFirestore.getInstance().collection("Users").document(post.getUid())
                .get().addOnSuccessListener(userDoc -> {
                    if (userDoc.exists() && userDoc.contains("profile")) {
                        java.util.Map<String, Object> profile = (java.util.Map<String, Object>) userDoc.get("profile");
                        if (profile != null) {
                            String fullName = (String) profile.get("fullName");
                            String avatarUrl = (String) profile.get("avatarUrl");
                            if (tvName != null && fullName != null) tvName.setText(fullName);
                            ImageView ivMentorBadge = postView.findViewById(R.id.iv_mentor_badge);
                            if (ivMentorBadge != null) {
                                if ("mentor".equals(userDoc.getString("role"))) {
                                    ivMentorBadge.setVisibility(android.view.View.VISIBLE);
                                } else {
                                    ivMentorBadge.setVisibility(android.view.View.GONE);
                                }
                            }
                            if (ivAvatar != null) {
                                if (avatarUrl != null && !avatarUrl.isEmpty() && avatarUrl.startsWith("data:image")) {
                                    byte[] b = android.util.Base64.decode(avatarUrl.split(",")[1], android.util.Base64.DEFAULT);
                                    Glide.with(this).load(b).circleCrop().into(ivAvatar);
                                } else if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                    Glide.with(this).load(avatarUrl).circleCrop().into(ivAvatar);
                                } else {
                                    Glide.with(this).load(R.drawable.ic_default_user).circleCrop().into(ivAvatar);
                                }
                            }
                        }
                    }
                });

            View llComment = postView.findViewById(R.id.ll_comment);
            if (llComment != null) {
                llComment.setOnClickListener(v -> {
                    Intent intent = new Intent(this, PostDetailActivity.class);
                    intent.putExtra("POST_ID", post.getId());
                    startActivity(intent);
                });
            }

            View llLike = postView.findViewById(R.id.ll_like);
            ImageView ivLike = postView.findViewById(R.id.iv_like);
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
                    FirebaseFirestore.getInstance().collection("Posts").document(post.getId())
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
