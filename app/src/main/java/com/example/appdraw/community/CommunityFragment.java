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

    private boolean isLiked = false;
    private int likeCount = 1200;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        // --- Notifications ---
        View btnNotifications = view.findViewById(R.id.btn_notifications_community);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), NotificationsActivity.class);
                startActivity(intent);
            });
        }



        // --- Post Feed ---
        LinearLayout postContainer = view.findViewById(R.id.ll_post_container);
        if (postContainer != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("Posts")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (!isAdded() || getContext() == null) return;
                    if (error != null) return;
                    postContainer.removeAllViews();
                    if (value == null) return;

                    String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : value) {
                        com.example.appdraw.model.Post post = doc.toObject(com.example.appdraw.model.Post.class);
                        
                        View postView = inflater.inflate(R.layout.item_post, postContainer, false);
                        
                        // Load image if available. Normally item_post should have an ImageView for content
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
                        if (tvContent != null) {
                            tvContent.setText(post.getContent());
                        }

                        // Load time
                        TextView tvTime = postView.findViewById(R.id.tv_post_time);
                        if (tvTime != null) {
                            tvTime.setText(getTimeAgo(post.getCreatedAt()));
                        }

                        // Process Follow Button based on Ownership
                        TextView tvFollowStatus = postView.findViewById(R.id.tv_follow_status);
                        if (tvFollowStatus != null) {
                            if (post.getUid() != null && post.getUid().equals(currentUid)) {
                                tvFollowStatus.setVisibility(View.GONE);
                            } else {
                                tvFollowStatus.setVisibility(View.VISIBLE);
                                
                                // Kiem tra trang thai follow ban dau
                                com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
                                com.google.firebase.firestore.DocumentReference followRef = db.collection("Follows").document(currentUid + "_" + post.getUid());
                                followRef.addSnapshotListener((d, e) -> {
                                    if (e != null) return;
                                    if (d != null && d.exists()) {
                                        tvFollowStatus.setText("Đang theo dõi");
                                        tvFollowStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                                        tvFollowStatus.setTag(true);
                                    } else {
                                        tvFollowStatus.setText("Theo dõi");
                                        tvFollowStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4272D0")));
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

                        // Process Real Comment Count
                        TextView tvCommentCount = postView.findViewById(R.id.tv_comment_count);
                        if (tvCommentCount != null) {
                            tvCommentCount.setText(String.valueOf(post.getCommentsCount()));
                        }

                        // Fetching author data
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
                                            if ("mentor".equals(userDoc.getString("role"))) {
                                                ivMentorBadge.setVisibility(View.VISIBLE);
                                            } else {
                                                ivMentorBadge.setVisibility(View.GONE);
                                            }
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

                        // Xử lý nút Tim (Like)
                        View llLike = postView.findViewById(R.id.ll_like);
                        ImageView ivLike = postView.findViewById(R.id.iv_like);
                        TextView tvLikeCount = postView.findViewById(R.id.tv_like_count);

                        if (llLike != null && ivLike != null && tvLikeCount != null) {
                            boolean isLiked = post.getLikedBy().contains(currentUid);
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
                                        if (p.getLikedBy().contains(currentUid)) {
                                            p.getLikedBy().remove(currentUid);
                                            p.setLikesCount(Math.max(0, p.getLikesCount() - 1));
                                        } else {
                                            p.getLikedBy().add(currentUid);
                                            p.setLikesCount(p.getLikesCount() + 1);
                                        }
                                        transaction.set(postRef, p);
                                    }
                                    return null;
                                }).addOnFailureListener(e -> showToast("Lỗi mạng"));
                            });
                        }

                        // Click image to see Fullscreen
                        if (ivPostImg != null) {
                            ivPostImg.setOnClickListener(v -> {
                                Intent intent = new Intent(getActivity(), FullScreenImageActivity.class);
                                intent.putExtra("IMAGE_URL", post.getImageUrl());
                                startActivity(intent);
                            });
                        }

                        // Click comment to see Detail comments
                        View llComment = postView.findViewById(R.id.ll_comment);
                        if (llComment != null) {
                            llComment.setOnClickListener(v -> {
                                Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                                intent.putExtra("POST_ID", post.getId());
                                startActivity(intent);
                            });
                        }

                        // Click avatar/name to see profile
                        View userHeader = postView.findViewById(R.id.ll_user_header);
                        if (userHeader != null) {
                            userHeader.setOnClickListener(v -> {
                                if (post.getUid() != null && post.getUid().equals(currentUid)) {
                                    Intent intent = new Intent(getActivity(), com.example.appdraw.ProfileActivity.class);
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(getActivity(), OtherUserProfileActivity.class);
                                    intent.putExtra("USER_ID", post.getUid());
                                    startActivity(intent);
                                }
                            });
                        }

                        // Click anywhere else on the post card to see Detail
                        postView.setOnClickListener(v -> {
                            Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                            intent.putExtra("POST_ID", post.getId());
                            startActivity(intent);
                        });

                        postContainer.addView(postView);
                    }
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
}
