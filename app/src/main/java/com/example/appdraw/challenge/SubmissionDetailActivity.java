package com.example.appdraw.challenge;

import android.graphics.Color;
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
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SubmissionDetailActivity extends AppCompatActivity {
    private String submissionId;
    private FirebaseFirestore db;
    private String currentUid;
    private LinearLayout llCommentsContainer;
    private EditText etComment;
    private ImageView btnSendComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_detail);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUid = user != null ? user.getUid() : null;
        submissionId = getIntent().getStringExtra("SUBMISSION_ID");

        if (submissionId == null) {
            Toast.makeText(this, "Không tìm thấy bài dự thi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar_submission_detail);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        llCommentsContainer = findViewById(R.id.ll_comments_container);
        etComment = findViewById(R.id.et_comment);
        btnSendComment = findViewById(R.id.btn_send_comment);

        loadSubmissionDetails();
        loadComments();

        btnSendComment.setOnClickListener(v -> postComment());
    }

    private void loadSubmissionDetails() {
        View includedView = findViewById(R.id.included_submission);
        if (includedView == null) return;

        db.collection("Challenge_Submissions").document(submissionId).addSnapshotListener((doc, e) -> {
            if (e != null || doc == null || !doc.exists()) return;

            String authorName = doc.getString("userName");
            String authorAvatar = doc.getString("userAvatar");
            String imageUrl = doc.getString("imageUrl");
            String status = doc.getString("status");
            long commentsCount = doc.getLong("commentsCount") != null ? doc.getLong("commentsCount") : 0;

            // Header Count
            TextView tvHeader = findViewById(R.id.tv_comment_header);
            if (tvHeader != null) {
                tvHeader.setText("Bình luận (" + commentsCount + ")");
            }

            // Author text
            TextView tvName = includedView.findViewById(R.id.tv_public_user_name);
            if (tvName != null) tvName.setText(authorName != null ? authorName : "Học viên");

            // Avatar
            ShapeableImageView ivAvatar = includedView.findViewById(R.id.iv_public_user_avatar);
            if (ivAvatar != null) {
                if (authorAvatar != null && !authorAvatar.isEmpty()) {
                    if (authorAvatar.startsWith("data:image")) {
                        byte[] decodedString = android.util.Base64.decode(authorAvatar.split(",")[1], android.util.Base64.DEFAULT);
                        Glide.with(this).load(decodedString).circleCrop().into(ivAvatar);
                    } else {
                        Glide.with(this).load(authorAvatar).circleCrop().into(ivAvatar);
                    }
                }
            }

            // Artwork
            ImageView ivArtwork = includedView.findViewById(R.id.iv_public_artwork);
            if (ivArtwork != null && imageUrl != null && !imageUrl.isEmpty()) {
                byte[] decodedString = android.util.Base64.decode(imageUrl.split(",")[1], android.util.Base64.DEFAULT);
                Glide.with(this).load(decodedString).centerCrop().into(ivArtwork);
            }

            // Score logic
            if ("GRADED".equals(status)) {
                View scoreLayout = includedView.findViewById(R.id.ll_public_score);
                TextView tvScore = includedView.findViewById(R.id.tv_public_score);
                if (scoreLayout != null) scoreLayout.setVisibility(View.VISIBLE);
                if (tvScore != null) {
                    Number score = doc.getDouble("score");
                    tvScore.setText((score != null ? score.intValue() : 0) + "/100");
                }
            }

            android.widget.LinearLayout llFeedbacks = includedView.findViewById(R.id.ll_public_feedbacks);
            if (llFeedbacks != null) {
                llFeedbacks.removeAllViews();
                java.util.List<Map<String, Object>> grades = (java.util.List<Map<String, Object>>) doc.get("grades");
                
                if (grades != null && !grades.isEmpty()) {
                    llFeedbacks.setVisibility(View.VISIBLE);
                    TextView tvFbTitle = new TextView(this);
                    tvFbTitle.setText("Góc nhận xét của Mentor:");
                    tvFbTitle.setTextColor(Color.parseColor("#E67E22"));
                    tvFbTitle.setTextSize(13);
                    tvFbTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                    tvFbTitle.setPadding(0, 0, 0, 4);
                    llFeedbacks.addView(tvFbTitle);
                    
                    for (Map<String, Object> g : grades) {
                        String mName = (String) g.get("mentorName");
                        Number mScore = (Number) g.get("score");
                        String mFeedback = (String) g.get("feedback");
                        
                        TextView tvFb = new TextView(this);
                        tvFb.setText("• " + mName + " (" + (mScore != null ? mScore.intValue() : 0) + " điểm): \"" + mFeedback + "\"");
                        tvFb.setTextColor(Color.parseColor("#444444"));
                        tvFb.setTextSize(13);
                        tvFb.setPadding(0, 2, 0, 6);
                        
                        llFeedbacks.addView(tvFb);
                    }
                } else if ("GRADED".equals(status)) {
                    // Fallback tương thích dữ liệu cũ
                    String existingFeedback = doc.getString("feedback");
                    if (existingFeedback != null && !existingFeedback.isEmpty()) {
                        llFeedbacks.setVisibility(View.VISIBLE);
                        TextView tvFbTitle = new TextView(this);
                        tvFbTitle.setText("Khuyến nghị từ Mentor:");
                        tvFbTitle.setTextColor(Color.parseColor("#E67E22"));
                        tvFbTitle.setTextSize(13);
                        tvFbTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                        tvFbTitle.setPadding(0, 0, 0, 4);
                        llFeedbacks.addView(tvFbTitle);
                        
                        TextView tvFb = new TextView(this);
                        tvFb.setText("\"" + existingFeedback + "\"");
                        tvFb.setTextColor(Color.parseColor("#444444"));
                        tvFb.setTextSize(13);
                        tvFb.setPadding(0, 2, 0, 6);
                        llFeedbacks.addView(tvFb);
                    } else {
                        llFeedbacks.setVisibility(View.GONE);
                    }
                } else {
                    llFeedbacks.setVisibility(View.GONE);
                }
            }

            // Comment count inside card
            TextView tvInnerCommentCount = includedView.findViewById(R.id.tv_public_comment_count);
            if (tvInnerCommentCount != null) tvInnerCommentCount.setText(String.valueOf(commentsCount));

            // Like logic
            View btnLike = includedView.findViewById(R.id.btn_public_like);
            ImageView ivLikeIcon = includedView.findViewById(R.id.iv_public_like_icon);
            TextView tvLikeCount = includedView.findViewById(R.id.tv_public_like_count);

            long likesCount = doc.getLong("likesCount") != null ? doc.getLong("likesCount") : 0;
            if (tvLikeCount != null) tvLikeCount.setText(String.valueOf(likesCount));

            if (btnLike != null) {
                List<String> likedBy = (List<String>) doc.get("likedBy");
                boolean initiallyLiked = likedBy != null && likedBy.contains(currentUid);

                if (initiallyLiked) {
                    ivLikeIcon.setColorFilter(Color.parseColor("#E74C3C")); // Red
                    btnLike.setTag(true);
                } else {
                    ivLikeIcon.setColorFilter(Color.parseColor("#888888")); // Gray
                    btnLike.setTag(false);
                }

                btnLike.setOnClickListener(v -> {
                    if (currentUid == null || currentUid.isEmpty()) return;
                    boolean isLiked = (v.getTag() != null && (boolean)v.getTag());

                    try {
                        long currentUiCount = Long.parseLong(tvLikeCount.getText().toString());
                        if (!isLiked) {
                            ivLikeIcon.setColorFilter(Color.parseColor("#E74C3C"));
                            tvLikeCount.setText(String.valueOf(currentUiCount + 1));
                            v.setTag(true);

                            db.collection("Challenge_Submissions").document(doc.getId()).update(
                                    "likedBy", FieldValue.arrayUnion(currentUid),
                                    "likesCount", FieldValue.increment(1)
                            );
                        } else {
                            ivLikeIcon.setColorFilter(Color.parseColor("#888888"));
                            tvLikeCount.setText(String.valueOf(Math.max(0, currentUiCount - 1)));
                            v.setTag(false);

                            db.collection("Challenge_Submissions").document(doc.getId()).update(
                                    "likedBy", FieldValue.arrayRemove(currentUid),
                                    "likesCount", FieldValue.increment(-1)
                            );
                        }
                    } catch (Exception ignored) {}
                });
            }
        });
    }

    private void loadComments() {
        db.collection("Challenge_Submissions").document(submissionId).collection("Comments")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    llCommentsContainer.removeAllViews();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Comment comment = doc.toObject(Comment.class);
                            View commentView = getLayoutInflater().inflate(R.layout.item_comment, llCommentsContainer, false);

                            TextView tvContent = commentView.findViewById(R.id.tv_comment_content);
                            TextView tvName = commentView.findViewById(R.id.tv_comment_name);
                            ImageView ivAvatar = commentView.findViewById(R.id.iv_comment_avatar);

                            tvContent.setText(comment.getContent());

                            db.collection("Users").document(comment.getUid()).get().addOnSuccessListener(userDoc -> {
                                if (userDoc.exists()) {
                                    String role = userDoc.getString("role");
                                    Map<String, Object> profile = (Map<String, Object>) userDoc.get("profile");
                                    if (profile != null) {
                                        String fullName = (String) profile.get("fullName");
                                        if ("mentor".equals(role)) {
                                            tvName.setText(fullName + " (Mentor)");
                                            tvName.setTextColor(Color.parseColor("#E67E22")); // Mentor color
                                        } else {
                                            tvName.setText(fullName);
                                            tvName.setTextColor(Color.parseColor("#1A1A1A"));
                                        }

                                        String avatarUrl = (String) profile.get("avatarUrl");
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

        DocumentReference submissionRef = db.collection("Challenge_Submissions").document(submissionId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(submissionRef);
            if (snapshot.exists()) {
                long currentCount = snapshot.getLong("commentsCount") != null ? snapshot.getLong("commentsCount") : 0;
                transaction.update(submissionRef, "commentsCount", currentCount + 1);
                transaction.set(submissionRef.collection("Comments").document(commentId), comment);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            etComment.setText("");
            btnSendComment.setEnabled(true);
        }).addOnFailureListener(e -> {
            btnSendComment.setEnabled(true);
            Toast.makeText(this, "Lỗi gửi bình luận: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
