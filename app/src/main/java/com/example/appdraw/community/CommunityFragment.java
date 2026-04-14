package com.example.appdraw.community;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.appdraw.NotificationsActivity;
import com.example.appdraw.R;

public class CommunityFragment extends Fragment {

    private String currentFilter = "Tất cả";
    private java.util.List<com.google.firebase.firestore.QueryDocumentSnapshot> cachedDocs = new java.util.ArrayList<>();
    private LinearLayout postContainer;
    private android.view.LayoutInflater cachedInflater;

    private boolean isLiked = false;
    private int likeCount = 1200;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);
        this.cachedInflater = inflater;
        setupFilters(view);






        // --- Post Feed ---
        postContainer = view.findViewById(R.id.ll_post_container);
        if (postContainer != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("Posts")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (!isAdded() || getContext() == null) return;
                    if (error != null) return;
                    if (value == null) return;
                    cachedDocs.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : value) {
                        cachedDocs.add(doc);
                    }
                    renderPosts();
                });
        }

        return view;
    }

    private String getTimeAgo(long time) {
        if (time < 1000000000000L) time *= 1000;
        long now = System.currentTimeMillis();
        if (time > now || time <= 0) return "Vừa xong";

        final long diff = now - time;
        if (diff < 60 * 1000) return "Vừa xong";
        else if (diff < 60 * 60 * 1000) return diff / (60 * 1000) + " phút trước";
        else if (diff < 24 * 60 * 60 * 1000) return diff / (60 * 60 * 1000) + " giờ trước";
        else if (diff < 30L * 24 * 60 * 60 * 1000) return diff / (24 * 60 * 60 * 1000) + " ngày trước";
        else {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            return sdf.format(new java.util.Date(time));
        }
    }

    private void showToast(String msg) {
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupFilters(View view) {
        int[] tvIds = new int[]{R.id.tv_filter_all, R.id.tv_filter_watercolor, R.id.tv_filter_sketch, R.id.tv_filter_handmade};
        for (int id : tvIds) {
            TextView tv = view.findViewById(id);
            if (tv != null) {
                tv.setOnClickListener(v -> {
                    for (int _id : tvIds) {
                        TextView _tv = view.findViewById(_id);
                        if (_tv != null) {
                            _tv.setBackgroundResource(R.drawable.rounded_bg_gray);
                            _tv.setBackgroundTintList(null);
                            _tv.setTextColor(Color.parseColor("#333333"));
                        }
                    }
                    tv.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4272D0")));
                    tv.setTextColor(Color.WHITE);
                    currentFilter = tv.getText().toString();
                    renderPosts();
                });
            }
        }
    }

    private void renderPosts() {
        if (postContainer == null || cachedInflater == null || !isAdded()) return;
        postContainer.removeAllViews();
        String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();

        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : cachedDocs) {
            com.example.appdraw.model.Post post = doc.toObject(com.example.appdraw.model.Post.class);
            
            if (!currentFilter.equals("Tất cả")) {
                if (post.getTopics() == null || !post.getTopics().contains(currentFilter)) continue;
            }

            View postView = cachedInflater.inflate(R.layout.item_post, postContainer, false);
            
            // Load image if available
            ImageView ivPostImg = postView.findViewById(R.id.iv_post_image);
            if (ivPostImg != null && post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                ivPostImg.setVisibility(View.VISIBLE);
                if (post.getImageUrl().startsWith("data:image")) {
                    String base64Str = post.getImageUrl().substring(post.getImageUrl().indexOf(",") + 1);
                    byte[] decodedBytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT);
                    com.bumptech.glide.Glide.with(requireContext()).load(decodedBytes).into(ivPostImg);
                } else {
                    com.bumptech.glide.Glide.with(requireContext()).load(post.getImageUrl()).into(ivPostImg);
                }
            } else if (ivPostImg != null) {
                ivPostImg.setVisibility(View.GONE);
            }

            // Load content
            TextView tvContent = postView.findViewById(R.id.tv_post_content);
            if (tvContent != null) tvContent.setText(post.getContent());

            // Load time
            TextView tvTime = postView.findViewById(R.id.tv_post_time);
            if (tvTime != null) tvTime.setText(getTimeAgo(post.getCreatedAt()));

            // Process Follow Button
            TextView tvFollowStatus = postView.findViewById(R.id.tv_follow_status);
            if (tvFollowStatus != null) {
                if (post.getUid() != null && post.getUid().equals(currentUid)) {
                    tvFollowStatus.setVisibility(View.GONE);
                } else {
                    tvFollowStatus.setVisibility(View.VISIBLE);
                    com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
                    com.google.firebase.firestore.DocumentReference followRef = db.collection("Follows").document(currentUid + "_" + post.getUid());
                    followRef.addSnapshotListener((d, e) -> {
                        if (e != null) return;
                        if (d != null && d.exists()) {
                            tvFollowStatus.setText("Đang theo dõi");
                            tvFollowStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                            tvFollowStatus.setTag(true);
                        } else {
                            tvFollowStatus.setText("Theo dõi");
                            tvFollowStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4272D0")));
                            tvFollowStatus.setTag(false);
                        }
                        tvFollowStatus.setEnabled(true);
                    });

                    tvFollowStatus.setOnClickListener(v -> {
                        tvFollowStatus.setEnabled(false);
                        boolean isFollowing = tvFollowStatus.getTag() != null && (boolean)tvFollowStatus.getTag();
                        if (!isFollowing) {
                            java.util.Map<String, Object> data = new java.util.HashMap<>();
                            data.put("follower", currentUid);
                            data.put("following", post.getUid());
                            data.put("timestamp", System.currentTimeMillis());
                            followRef.set(data).addOnSuccessListener(aVoid -> {
                                db.collection("Users").document(post.getUid()).update("followersCount", com.google.firebase.firestore.FieldValue.increment(1));
                                db.collection("Users").document(currentUid).update("followingCount", com.google.firebase.firestore.FieldValue.increment(1));
                                showToast("Đã theo dõi");
                            });
                        } else {
                            followRef.delete().addOnSuccessListener(aVoid -> {
                                db.collection("Users").document(post.getUid()).update("followersCount", com.google.firebase.firestore.FieldValue.increment(-1));
                                db.collection("Users").document(currentUid).update("followingCount", com.google.firebase.firestore.FieldValue.increment(-1));
                                showToast("Bỏ theo dõi");
                            });
                        }
                    });
                }
            }

            // Comment Count
            TextView tvCommentCount = postView.findViewById(R.id.tv_comment_count);
            if (tvCommentCount != null) {
                tvCommentCount.setText(String.valueOf(post.getCommentsCount()));
            }

            // Fetch Author
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("Users").document(post.getUid())
                .get().addOnSuccessListener(userDoc -> {
                    if (!isAdded() || getContext() == null) return;
                    if (userDoc.exists() && userDoc.contains("profile")) {
                        java.util.Map<String, Object> profile = (java.util.Map<String, Object>) userDoc.get("profile");
                        if (profile != null) {
                            String fullName = (String) profile.get("fullName");
                            String avatarUrl = (String) profile.get("avatarUrl");
                            TextView tvName = postView.findViewById(R.id.tv_user_name);
                            ImageView ivAvatar = postView.findViewById(R.id.iv_user_avatar);
                            ImageView ivMentorBadge = postView.findViewById(R.id.iv_mentor_badge);
                            if (tvName != null) tvName.setText(fullName != null ? fullName : "Người dùng");
                            if (ivMentorBadge != null) {
                                ivMentorBadge.setVisibility("mentor".equals(userDoc.getString("role")) ? View.VISIBLE : View.GONE);
                            }
                            if (ivAvatar != null) {
                                if (avatarUrl != null && !avatarUrl.isEmpty() && avatarUrl.startsWith("data:image")) {
                                    byte[] b = android.util.Base64.decode(avatarUrl.split(",")[1], android.util.Base64.DEFAULT);
                                    com.bumptech.glide.Glide.with(requireContext()).load(b).circleCrop().into(ivAvatar);
                                } else if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                    com.bumptech.glide.Glide.with(requireContext()).load(avatarUrl).circleCrop().into(ivAvatar);
                                } else {
                                    com.bumptech.glide.Glide.with(requireContext()).load(R.drawable.ic_default_user).circleCrop().into(ivAvatar);
                                }
                            }
                        }
                    }
                });

            // Like Logic
            View llLike = postView.findViewById(R.id.ll_like);
            ImageView ivLike = postView.findViewById(R.id.iv_like);
            TextView tvLikeCount = postView.findViewById(R.id.tv_like_count);

            if (llLike != null && ivLike != null && tvLikeCount != null) {
                boolean isLiked = post.getLikedBy() != null && post.getLikedBy().contains(currentUid);
                tvLikeCount.setText(String.valueOf(post.getLikesCount()));
                if (isLiked) {
                    ivLike.setImageResource(R.drawable.ic_heart);
                    ivLike.setColorFilter(Color.parseColor("#E91E63"));
                } else {
                    ivLike.setColorFilter(Color.parseColor("#888888"));
                }

                llLike.setOnClickListener(v -> {
                    if (currentUid == null) {
                        showToast("Vui lòng đăng nhập!");
                        return;
                    }
                    com.google.firebase.firestore.DocumentReference postRef = doc.getReference();
                    com.google.firebase.firestore.FirebaseFirestore.getInstance().runTransaction(transaction -> {
                        com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(postRef);
                        com.example.appdraw.model.Post p = snapshot.toObject(com.example.appdraw.model.Post.class);
                        if (p != null) {
                            if (p.getLikedBy() == null) p.setLikedBy(new java.util.ArrayList<>());
                            if (p.getLikedBy().contains(currentUid)) {
                                p.getLikedBy().remove(currentUid);
                                p.setLikesCount(Math.max(0, p.getLikesCount() - 1));
                            } else {
                                p.getLikedBy().add(currentUid);
                                p.setLikesCount(p.getLikesCount() + 1);
                                if (!p.getUid().equals(currentUid)) {
                                    com.example.appdraw.utils.NotificationHelper.sendNotification(p.getUid(), "LIKE", "đã thích bài viết của bạn.", p.getId());
                                }
                            }
                            transaction.set(postRef, p);
                        }
                        return null;
                    }).addOnFailureListener(e -> showToast("Lỗi mạng"));
                });
            }

            // Click Handlers
            if (ivPostImg != null) {
                ivPostImg.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), FullScreenImageActivity.class);
                    intent.putExtra("IMAGE_URL", post.getImageUrl());
                    startActivity(intent);
                });
            }

            View llComment = postView.findViewById(R.id.ll_comment);
            if (llComment != null) {
                llComment.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                    intent.putExtra("POST_ID", post.getId());
                    startActivity(intent);
                });
            }

            View userHeader = postView.findViewById(R.id.ll_user_header);
            if (userHeader != null) {
                userHeader.setOnClickListener(v -> {
                    if (post.getUid() != null && post.getUid().equals(currentUid)) {
                        startActivity(new Intent(getActivity(), com.example.appdraw.ProfileActivity.class));
                    } else {
                        Intent intent = new Intent(getActivity(), OtherUserProfileActivity.class);
                        intent.putExtra("USER_ID", post.getUid());
                        startActivity(intent);
                    }
                });
            }

            postView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                intent.putExtra("POST_ID", post.getId());
                startActivity(intent);
            });

            postContainer.addView(postView);
        }
    }
}
