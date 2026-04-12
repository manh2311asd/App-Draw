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

        db.collection("Posts").document(postId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Post post = doc.toObject(Post.class);
                if (post != null) {
                    TextView tvHeader = findViewById(R.id.tv_comment_header);
                    if (tvHeader != null) {
                        tvHeader.setText("Bình luận(" + post.getCommentsCount() + ")");
                    }

                    TextView tvContent = includedPost.findViewById(R.id.tv_post_content);
                    if (tvContent != null) tvContent.setText(post.getContent());

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

                    TextView tvLikeCount = includedPost.findViewById(R.id.tv_like_count);
                    if (tvLikeCount != null) tvLikeCount.setText(String.valueOf(post.getLikesCount()));

                    TextView tvCommentCount = includedPost.findViewById(R.id.tv_comment_count);
                    if (tvCommentCount != null) tvCommentCount.setText(String.valueOf(post.getCommentsCount()));

                    TextView tvFollowStatus = includedPost.findViewById(R.id.tv_follow_status);
                    if (tvFollowStatus != null) {
                        if (post.getUid() != null && post.getUid().equals(currentUid)) {
                            tvFollowStatus.setVisibility(View.GONE);
                        } else {
                            tvFollowStatus.setVisibility(View.VISIBLE);
                            tvFollowStatus.setText("Theo dõi");
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
                                if (tvName != null) tvName.setText(fullName);
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
                        
                        db.collection("Users").document(comment.getUid()).get().addOnSuccessListener(userDoc -> {
                            if (userDoc.exists() && userDoc.contains("profile")) {
                                Map<String, Object> profile = (Map<String, Object>) userDoc.get("profile");
                                if (profile != null) {
                                    TextView tvName = commentView.findViewById(R.id.tv_comment_name);
                                    tvName.setText((String) profile.get("fullName"));
                                    String avatarUrl = (String) profile.get("avatarUrl");
                                    ImageView ivAvatar = commentView.findViewById(R.id.iv_comment_avatar);
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
                        });
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
}
