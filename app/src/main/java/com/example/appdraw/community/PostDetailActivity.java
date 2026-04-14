package com.example.appdraw.community;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.example.appdraw.R;
import com.example.appdraw.model.Comment;
import com.example.appdraw.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;
import java.util.UUID;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {
    private String postId;
    private FirebaseFirestore db;
    private String currentUid;
    private LinearLayout llCommentsContainer;
    private EditText etComment;
    private ImageView btnSendComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getUid();
        postId = getIntent().getStringExtra("POST_ID");

        if (postId == null) {
            Toast.makeText(this, "Không tìm thấy bài viết", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar_post_detail);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        llCommentsContainer = findViewById(R.id.ll_comments_container);
        etComment = findViewById(R.id.et_comment);
        btnSendComment = findViewById(R.id.btn_send_comment);

        loadPostDetails();
        loadComments();

        btnSendComment.setOnClickListener(v -> postComment());
    }

    private void loadPostDetails() {
        View includedPost = findViewById(R.id.included_post);
        if (includedPost == null) return;

        db.collection("Posts").document(postId).addSnapshotListener((doc, e) -> {
            if (e != null || doc == null || !doc.exists()) return;
                Post post = doc.toObject(Post.class);
                if (post != null) {
                    if (!post.isAllowComment()) {
                        etComment.setEnabled(false);
                        etComment.setHint("Tác giả đã tắt bình luận");
                        btnSendComment.setEnabled(false);
                        btnSendComment.setAlpha(0.5f);
                    } else {
                        etComment.setEnabled(true);
                        etComment.setHint("Thêm bình luận...");
                        btnSendComment.setEnabled(true);
                        btnSendComment.setAlpha(1.0f);
                    }

                    TextView tvHeader = findViewById(R.id.tv_comment_header);
                    if (tvHeader != null) {
                        tvHeader.setText("Bình luận(" + post.getCommentsCount() + ")");
                    }

                    TextView tvContent = includedPost.findViewById(R.id.tv_post_content);
                    if (tvContent != null) tvContent.setText(post.getContent());

                    TextView tvTime = includedPost.findViewById(R.id.tv_post_time);
                    if (tvTime != null) {
                        tvTime.setText(getTimeAgo(post.getCreatedAt()));
                    }

                    ImageView ivPostImg = includedPost.findViewById(R.id.iv_post_image);
                    if (ivPostImg != null && post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                        ivPostImg.setVisibility(View.VISIBLE);
                        if (post.getImageUrl().startsWith("data:image")) {
                            byte[] decodedBytes = android.util.Base64.decode(post.getImageUrl().split(",")[1], android.util.Base64.DEFAULT);
                            Glide.with(this).load(decodedBytes).into(ivPostImg);
                        } else {
                            Glide.with(this).load(post.getImageUrl()).into(ivPostImg);
                        }
                    } else if (ivPostImg != null) {
                        ivPostImg.setVisibility(View.GONE);
                    }

                    View llLike = includedPost.findViewById(R.id.ll_like);
                    ImageView ivLike = includedPost.findViewById(R.id.iv_like);
                    TextView tvLikeCount = includedPost.findViewById(R.id.tv_like_count);
                    
                    if (llLike != null && ivLike != null && tvLikeCount != null) {
                        boolean isLiked = post.getLikedBy() != null && post.getLikedBy().contains(currentUid);
                        tvLikeCount.setText(String.valueOf(post.getLikesCount()));
                        if (isLiked) {
                            ivLike.setImageResource(R.drawable.ic_heart);
                            ivLike.setColorFilter(android.graphics.Color.parseColor("#E91E63"));
                        } else {
                            ivLike.setImageResource(R.drawable.ic_heart);
                            ivLike.setColorFilter(android.graphics.Color.parseColor("#888888"));
                        }
                        
                        llLike.setOnClickListener(v -> {
                            if (currentUid == null) return;
                            db.collection("Posts").document(post.getId())
                                .get().addOnSuccessListener(d -> {
                                    if (!d.exists()) return;
                                    Post p = d.toObject(Post.class);
                                    if (p != null) {
                                        if (p.getLikedBy() == null) p.setLikedBy(new java.util.ArrayList<>());
                                        if (p.getLikedBy().contains(currentUid)) {
                                            p.getLikedBy().remove(currentUid);
                                            p.setLikesCount(Math.max(0, p.getLikesCount() - 1));
                                        } else {
                                            p.getLikedBy().add(currentUid);
                                            p.setLikesCount(p.getLikesCount() + 1);
                                        }
                                        d.getReference().set(p);
                                    }
                                });
                        });
                    }

                    TextView tvCommentCount = includedPost.findViewById(R.id.tv_comment_count);
                    if (tvCommentCount != null) tvCommentCount.setText(String.valueOf(post.getCommentsCount()));

                    TextView tvFollowStatus = includedPost.findViewById(R.id.tv_follow_status);
                    if (tvFollowStatus != null) {
                        if (post.getUid() != null && post.getUid().equals(currentUid)) {
                            tvFollowStatus.setVisibility(View.GONE);
                        } else {
                            tvFollowStatus.setVisibility(View.VISIBLE);
                            
                            DocumentReference followRef = db.collection("Follows").document(currentUid + "_" + post.getUid());
                            followRef.get().addOnSuccessListener(d -> {
                                if (d.exists()) {
                                    tvFollowStatus.setText("Đang theo dõi");
                                    tvFollowStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                                } else {
                                    tvFollowStatus.setText("Theo dõi");
                                    tvFollowStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4272D0")));
                                }
                            });

                            tvFollowStatus.setOnClickListener(v -> {
                                tvFollowStatus.setEnabled(false);
                                if (tvFollowStatus.getText().toString().equals("Theo dõi")) {
                                    java.util.Map<String, Object> data = new java.util.HashMap<>();
                                    data.put("follower", currentUid);
                                    data.put("following", post.getUid());
                                    data.put("timestamp", System.currentTimeMillis());
                                    followRef.set(data).addOnSuccessListener(aVoid -> {
                                        tvFollowStatus.setText("Đang theo dõi");
                                        tvFollowStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                                        db.collection("Users").document(post.getUid()).update("followersCount", com.google.firebase.firestore.FieldValue.increment(1));
                                        db.collection("Users").document(currentUid).update("followingCount", com.google.firebase.firestore.FieldValue.increment(1));
                                        Toast.makeText(PostDetailActivity.this, "Đã theo dõi", Toast.LENGTH_SHORT).show();
                                        tvFollowStatus.setEnabled(true);
                                    });
                                } else {
                                    followRef.delete().addOnSuccessListener(aVoid -> {
                                        tvFollowStatus.setText("Theo dõi");
                                        tvFollowStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4272D0")));
                                        db.collection("Users").document(post.getUid()).update("followersCount", com.google.firebase.firestore.FieldValue.increment(-1));
                                        db.collection("Users").document(currentUid).update("followingCount", com.google.firebase.firestore.FieldValue.increment(-1));
                                        Toast.makeText(PostDetailActivity.this, "Bỏ theo dõi", Toast.LENGTH_SHORT).show();
                                        tvFollowStatus.setEnabled(true);
                                    });
                                }
                            });
                        }
                    }

                    // Tải thông tin người dùng
                    db.collection("Users").document(post.getUid()).get().addOnSuccessListener(userDoc -> {
                        if (userDoc.exists() && userDoc.contains("profile")) {
                            Map<String, Object> profile = (Map<String, Object>) userDoc.get("profile");
                            if (profile != null) {
                                String fullName = (String) profile.get("fullName");
                                String avatarUrl = (String) profile.get("avatarUrl");
                                TextView tvName = includedPost.findViewById(R.id.tv_user_name);
                                ImageView ivAvatar = includedPost.findViewById(R.id.iv_user_avatar);
                                ImageView ivMentorBadge = includedPost.findViewById(R.id.iv_mentor_badge);
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
                }
        });
    }

    private void loadComments() {
        db.collection("Posts").document(postId).collection("Comments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Toast.makeText(this, "Lỗi tải bình luận: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                llCommentsContainer.removeAllViews();
                if (value != null) {
                    for (QueryDocumentSnapshot doc : value) {
                        Comment comment = doc.toObject(Comment.class);
                        View commentView = getLayoutInflater().inflate(R.layout.item_comment, llCommentsContainer, false);
                        
                        TextView tvContent = commentView.findViewById(R.id.tv_comment_content);
                        tvContent.setText(comment.getContent());
                        
                        TextView tvTime = commentView.findViewById(R.id.tv_comment_time);
                        if (tvTime != null) {
                            tvTime.setText(getTimeAgo(comment.getCreatedAt()));
                        }
                        
                        db.collection("Users").document(comment.getUid()).get().addOnSuccessListener(userDoc -> {
                            if (userDoc.exists() && userDoc.contains("profile")) {
                                Map<String, Object> profile = (Map<String, Object>) userDoc.get("profile");
                                if (profile != null) {
                                    TextView tvName = commentView.findViewById(R.id.tv_comment_name);
                                    String fullName = (String) profile.get("fullName");
                                    tvName.setText(fullName != null ? fullName : "Người dùng");
                                    String avatarUrl = (String) profile.get("avatarUrl");
                                    ImageView ivAvatar = commentView.findViewById(R.id.iv_comment_avatar);
                                    ImageView ivCommentMentorBadge = commentView.findViewById(R.id.iv_comment_mentor_badge);
                                    if (ivCommentMentorBadge != null) {
                                        if ("mentor".equals(userDoc.getString("role"))) {
                                            ivCommentMentorBadge.setVisibility(View.VISIBLE);
                                        } else {
                                            ivCommentMentorBadge.setVisibility(View.GONE);
                                        }
                                    }
                                    if (avatarUrl != null && !avatarUrl.isEmpty() && avatarUrl.startsWith("data:image")) {
                                        byte[] b = android.util.Base64.decode(avatarUrl.split(",")[1], android.util.Base64.DEFAULT);
                                        Glide.with(this).load(b).circleCrop().into(ivAvatar);
                                    } else if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                        Glide.with(this).load(avatarUrl).circleCrop().into(ivAvatar);
                                    } else {
                                        Glide.with(this).load(R.drawable.ic_default_user).circleCrop().into(ivAvatar);
                                    }
                                    
                                    View.OnClickListener goProfile = v -> {
                                        android.content.Intent intent = new android.content.Intent(PostDetailActivity.this, OtherUserProfileActivity.class);
                                        intent.putExtra("IS_OTHER_USER", true);
                                        intent.putExtra("USER_ID", comment.getUid());
                                        intent.putExtra("USER_NAME", fullName != null ? fullName : "Người dùng");
                                        startActivity(intent);
                                    };
                                    tvName.setOnClickListener(goProfile);
                                    ivAvatar.setOnClickListener(goProfile);
                                }
                            }
                        });
                        
                        TextView tvLike = commentView.findViewById(R.id.tv_comment_like);
                        if (tvLike != null) {
                            com.google.firebase.firestore.DocumentReference likeRef = db.collection("Posts")
                                .document(postId).collection("Comments")
                                .document(comment.getId()).collection("Likes").document(currentUid);
                            
                            likeRef.get().addOnSuccessListener(d -> {
                                if (d.exists()) {
                                    tvLike.setTextColor(android.graphics.Color.parseColor("#4272D0"));
                                    tvLike.setTag(true);
                                } else {
                                    tvLike.setTextColor(android.graphics.Color.parseColor("#65676B"));
                                    tvLike.setTag(false);
                                }
                            });

                            com.google.firebase.firestore.CollectionReference likesCol = db.collection("Posts")
                                .document(postId).collection("Comments")
                                .document(comment.getId()).collection("Likes");
                            
                            likesCol.count().get(com.google.firebase.firestore.AggregateSource.SERVER).addOnSuccessListener(t -> {
                                long count = t.getCount();
                                if (count > 0) tvLike.setText("Thích (" + count + ")");
                                else tvLike.setText("Thích");
                            });

                            tvLike.setOnClickListener(v -> {
                                boolean isLiked = tvLike.getTag() != null && (boolean) tvLike.getTag();
                                tvLike.setEnabled(false);
                                if (isLiked) {
                                    likeRef.delete().addOnSuccessListener(a -> {
                                        tvLike.setTag(false);
                                        tvLike.setTextColor(android.graphics.Color.parseColor("#65676B"));
                                        likesCol.count().get(com.google.firebase.firestore.AggregateSource.SERVER).addOnSuccessListener(t -> {
                                            long count = t.getCount();
                                            if (count > 0) tvLike.setText("Thích (" + count + ")");
                                            else tvLike.setText("Thích");
                                        });
                                        tvLike.setEnabled(true);
                                    }).addOnFailureListener(e -> tvLike.setEnabled(true));
                                } else {
                                    java.util.Map<String, Object> data = new java.util.HashMap<>();
                                    data.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
                                    likeRef.set(data).addOnSuccessListener(a -> {
                                        tvLike.setTag(true);
                                        tvLike.setTextColor(android.graphics.Color.parseColor("#4272D0"));
                                        likesCol.count().get(com.google.firebase.firestore.AggregateSource.SERVER).addOnSuccessListener(t -> {
                                            long count = t.getCount();
                                            if (count > 0) tvLike.setText("Thích (" + count + ")");
                                            else tvLike.setText("Thích");
                                        });
                                        tvLike.setEnabled(true);
                                    }).addOnFailureListener(e -> tvLike.setEnabled(true));
                                }
                            });
                        }

                        llCommentsContainer.addView(commentView);
                    }
                }
            });
    }

    private void postComment() {
        String text = etComment.getText().toString().trim();
        if (text.isEmpty() || currentUid == null) return;

        btnSendComment.setEnabled(false);
        String commentId = UUID.randomUUID().toString();
        Comment comment = new Comment(commentId, currentUid, text, System.currentTimeMillis());

        DocumentReference postRef = db.collection("Posts").document(postId);
        
        db.runTransaction(transaction -> {
            Post post = transaction.get(postRef).toObject(Post.class);
            if (post != null) {
                // Tăng bộ đếm bình luận
                post.setCommentsCount(post.getCommentsCount() + 1);
                transaction.set(postRef, post);
                transaction.set(postRef.collection("Comments").document(commentId), comment);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            etComment.setText("");
            btnSendComment.setEnabled(true);
            Toast.makeText(this, "Đã gửi bình luận", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            btnSendComment.setEnabled(true);
            Toast.makeText(this, "Lỗi gửi bình luận: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
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
}
