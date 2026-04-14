package com.example.appdraw.challenge;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.appdraw.R;

public class ChallengeDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_detail);

        String title = getIntent().getStringExtra("CHALLENGE_TITLE");
        String imageUrl = getIntent().getStringExtra("CHALLENGE_IMAGE_URL");
        String rulesStr = getIntent().getStringExtra("CHALLENGE_RULES");
        String rewardsStr = getIntent().getStringExtra("CHALLENGE_REWARDS");
        String deadlineStr = getIntent().getStringExtra("CHALLENGE_DEADLINE");

        if (title != null && !title.isEmpty()) {
            ((TextView) findViewById(R.id.tv_challenge_detail_title)).setText(title);
        }
        if (rulesStr != null && !rulesStr.isEmpty()) ((TextView) findViewById(R.id.tv_challenge_rules)).setText(rulesStr);
        if (rewardsStr != null && !rewardsStr.isEmpty()) ((TextView) findViewById(R.id.tv_challenge_rewards)).setText(rewardsStr);
        if (deadlineStr != null && !deadlineStr.isEmpty()) {
            String endDate = deadlineStr.contains("-") ? deadlineStr.split("-")[1].trim() : deadlineStr;
            ((TextView) findViewById(R.id.tv_challenge_deadline)).setText("Deadline: " + endDate);
        }

        android.widget.ImageView ivBanner = findViewById(R.id.iv_challenge_banner_img);
        if (ivBanner != null && imageUrl != null && !imageUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(this).load(imageUrl).centerCrop().into(ivBanner);
        }

        Toolbar toolbar = findViewById(R.id.toolbar_challenge_detail);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> finish());

        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("Challenges").whereEqualTo("title", title).get().addOnSuccessListener(shots -> {
            boolean isEnded = false;
            if (!shots.isEmpty()) {
                com.google.firebase.firestore.DocumentSnapshot doc = shots.getDocuments().get(0);
                Long endTime = doc.getLong("endTimeMillis");
                if (endTime != null && System.currentTimeMillis() > endTime) {
                    isEnded = true;
                }
                String authorId = doc.getString("authorId");
                String author = doc.getString("author");
                setupMoreMenu(doc.getId(), authorId, author);
            }
            checkUserChallengeState(title, isEnded);
            loadPublicSubmissions(title, isEnded);
        }).addOnFailureListener(e -> {
            checkUserChallengeState(title, false);
            loadPublicSubmissions(title, false);
        });
    }

    private void setupMoreMenu(String challengeId, String authorId, String author) {
        android.widget.ImageView ivMore = findViewById(R.id.iv_challenge_more);
        if (ivMore == null) return;
        
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        boolean isAuthor = false;
        if (authorId != null && authorId.equals(user.getUid())) {
            isAuthor = true;
        } else if (authorId == null && author != null) {
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            db.collection("Users").document(user.getUid()).get().addOnSuccessListener(userDoc -> {
                if (userDoc.exists()) {
                    java.util.Map<String, Object> profile = (java.util.Map<String, Object>) userDoc.get("profile");
                    if (profile != null && profile.containsKey("fullName")) {
                        String mentorName = "Mentor: " + profile.get("fullName");
                        if (author.equals(mentorName)) {
                            ivMore.setVisibility(android.view.View.VISIBLE);
                            ivMore.setOnClickListener(v -> showMoreMenu(v, challengeId));
                        }
                    }
                }
            });
            return;
        }
        
        if (isAuthor) {
            ivMore.setVisibility(android.view.View.VISIBLE);
            ivMore.setOnClickListener(v -> showMoreMenu(v, challengeId));
        }
    }

    private void showMoreMenu(android.view.View view, String challengeId) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, view);
        popup.getMenu().add(0, 1, 0, "Chỉnh sửa");
        popup.getMenu().add(0, 2, 1, "Xóa thử thách");
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                android.widget.Toast.makeText(this, "Tính năng chỉnh sửa đang phát triển, sắp ra mắt!", android.widget.Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == 2) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Xóa thử thách")
                    .setMessage("Bạn có chắc chắn muốn xóa thử thách này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("Challenges").document(challengeId).delete()
                            .addOnSuccessListener(aVoid -> {
                                android.widget.Toast.makeText(this, "Đã xóa thử thách", android.widget.Toast.LENGTH_SHORT).show();
                                finish();
                            });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            }
            return true;
        });
        popup.show();
    }

    private void checkUserChallengeState(String title, boolean isEnded) {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || title == null) return;

        android.widget.LinearLayout llJoinedStatus = findViewById(R.id.ll_joined_status);
        android.widget.LinearLayout llTopStatusSection = findViewById(R.id.ll_top_status_section);
        android.widget.TextView tvSubmissionStatus = findViewById(R.id.tv_submission_status);
        android.widget.TextView tvSubmissionStatusInfo = findViewById(R.id.tv_submission_status_info); // The one in the middle of screen
        com.google.android.material.button.MaterialButton btnSubmit = findViewById(R.id.btn_submit_challenge);
        com.google.android.material.button.MaterialButton btnJoin = findViewById(R.id.btn_join_challenge);

        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("Users").document(user.getUid()).get().addOnSuccessListener(userDoc -> {
            String role = userDoc.getString("role");
            if ("mentor".equals(role)) {
                // MENTOR VIEW
                btnJoin.setVisibility(android.view.View.VISIBLE);
                btnJoin.setText("Xem danh sách Bài thi");
                btnJoin.setOnClickListener(v -> {
                    android.content.Intent intent = new android.content.Intent(this, ChallengeSubmissionsActivity.class);
                    intent.putExtra("CHALLENGE_TITLE", title);
                    startActivity(intent);
                });
            } else {
                if (isEnded) {
                    btnJoin.setVisibility(android.view.View.GONE);
                    llJoinedStatus.setVisibility(android.view.View.GONE);
                    if (llTopStatusSection != null) llTopStatusSection.setVisibility(android.view.View.GONE);
                    return;
                }
                // USER VIEW
                db.collection("Users").document(user.getUid()).collection("joinedChallenges").document(title)
                    .addSnapshotListener((doc, e) -> {
                        if (e != null) return;
                        if (doc != null && doc.exists()) {
                            String status = doc.getString("status");
                            btnJoin.setVisibility(android.view.View.GONE);
                            llJoinedStatus.setVisibility(android.view.View.VISIBLE);
                            if (llTopStatusSection != null) llTopStatusSection.setVisibility(android.view.View.VISIBLE);
                            if (tvSubmissionStatusInfo != null) tvSubmissionStatusInfo.setText("Bài của bạn: Tham gia thử thách thành công");

                            if ("JOINED".equals(status)) {
                                tvSubmissionStatus.setText("Bài của bạn : Chưa nộp");
                                tvSubmissionStatus.setTextColor(android.graphics.Color.parseColor("#E67E22"));
                                btnSubmit.setText("NỘP BÀI");
                                btnSubmit.setEnabled(true);
                                btnSubmit.setOnClickListener(v -> {
                                    android.content.Intent intent = new android.content.Intent(this, SubmitChallengeActivity.class);
                                    intent.putExtra("CHALLENGE_TITLE", title);
                                    startActivity(intent);
                                });
                            } else if ("SUBMITTED".equals(status)) {
                                tvSubmissionStatus.setText("Bài của bạn : Đang chờ chấm");
                                tvSubmissionStatus.setTextColor(android.graphics.Color.parseColor("#4272D0"));
                                if (tvSubmissionStatusInfo != null) tvSubmissionStatusInfo.setText("Bài của bạn : Đã nộp. Đang chờ kết quả");
                                btnSubmit.setText("ĐÃ NỘP - XEM BÀI");
                                btnSubmit.setEnabled(true);
                                btnSubmit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                                btnSubmit.setOnClickListener(v -> {
                                    android.content.Intent intent = new android.content.Intent(this, UserScoreDetailActivity.class);
                                    intent.putExtra("CHALLENGE_TITLE", title);
                                    startActivity(intent);
                                });
                            } else if ("GRADED".equals(status)) {
                                Number score = doc.getDouble("score");
                                tvSubmissionStatus.setText("Điểm: " + (score != null ? score : 0) + "/100 XP - Nhấn để xem chi tiết");
                                tvSubmissionStatus.setTextColor(android.graphics.Color.parseColor("#2ECC71"));
                                if (tvSubmissionStatusInfo != null) tvSubmissionStatusInfo.setText("Tuyệt vời! Bài của bạn đã có điểm.");
                                btnSubmit.setText("XEM ĐIỂM CHI TIẾT");
                                btnSubmit.setEnabled(true);
                                btnSubmit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2ECC71")));
                                btnSubmit.setOnClickListener(v -> {
                                    android.content.Intent intent = new android.content.Intent(this, UserScoreDetailActivity.class);
                                    intent.putExtra("CHALLENGE_TITLE", title);
                                    startActivity(intent);
                                });
                            }
                        } else {
                            // NOT JOINED
                            llJoinedStatus.setVisibility(android.view.View.GONE);
                            if (llTopStatusSection != null) llTopStatusSection.setVisibility(android.view.View.GONE);
                            btnJoin.setVisibility(android.view.View.VISIBLE);
                            btnJoin.setText("THAM GIA THỬ THÁCH");
                            btnJoin.setOnClickListener(v -> {
                                java.util.Map<String, Object> data = new java.util.HashMap<>();
                                data.put("status", "JOINED");
                                db.collection("Users").document(user.getUid()).collection("joinedChallenges").document(title).set(data);
                            });
                        }
                    });
            }
        });
    }

    private void loadPublicSubmissions(String title, boolean isEnded) {
        android.widget.LinearLayout container = findViewById(R.id.ll_public_submissions_container);
        if (container == null) return;

        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("Challenge_Submissions")
            .whereEqualTo("challengeTitle", title)
            .addSnapshotListener((queryDocumentSnapshots, error) -> {
                if (error != null || queryDocumentSnapshots == null) return;
                
                container.removeAllViews();
                
                android.widget.LinearLayout llPodiumSection = findViewById(R.id.ll_podium_section);
                android.view.View vPodiumDivider = findViewById(R.id.v_podium_divider);
                
                if (queryDocumentSnapshots.isEmpty()) {
                    android.widget.TextView tvEmpty = new android.widget.TextView(this);
                    tvEmpty.setText("Chưa có bài dự thi nào.");
                    tvEmpty.setTextColor(android.graphics.Color.parseColor("#888888"));
                    tvEmpty.setPadding(32, 16, 32, 16);
                    container.addView(tvEmpty);
                    return;
                }

                java.util.List<com.google.firebase.firestore.DocumentSnapshot> publicList = new java.util.ArrayList<>(queryDocumentSnapshots.getDocuments());
                
                if (isEnded && llPodiumSection != null && vPodiumDivider != null) {
                    java.util.List<com.google.firebase.firestore.DocumentSnapshot> gradedList = new java.util.ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : publicList) {
                        if ("GRADED".equals(doc.getString("status"))) {
                            gradedList.add(doc);
                        }
                    }
                    if (gradedList.size() > 0) {
                        gradedList.sort((d1, d2) -> {
                            Number s1 = d1.getDouble("score");
                            Number s2 = d2.getDouble("score");
                            double s1Val = s1 != null ? s1.doubleValue() : 0;
                            double s2Val = s2 != null ? s2.doubleValue() : 0;
                            return Double.compare(s2Val, s1Val);
                        });
                        
                        llPodiumSection.setVisibility(android.view.View.VISIBLE);
                        vPodiumDivider.setVisibility(android.view.View.VISIBLE);
                        
                        if (gradedList.size() > 0) populatePodiumItem(gradedList.get(0), R.id.iv_top1_avatar, R.id.tv_top1_name, R.id.tv_top1_score);
                        if (gradedList.size() > 1) populatePodiumItem(gradedList.get(1), R.id.iv_top2_avatar, R.id.tv_top2_name, R.id.tv_top2_score);
                        if (gradedList.size() > 2) populatePodiumItem(gradedList.get(2), R.id.iv_top3_avatar, R.id.tv_top3_name, R.id.tv_top3_score);
                    }
                }

                android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
                for (com.google.firebase.firestore.DocumentSnapshot doc : publicList) {
                    android.view.View itemView = inflater.inflate(R.layout.item_challenge_submission_public, container, false);
                    
                    String authorName = doc.getString("userName");
                    String authorAvatar = doc.getString("userAvatar");
                    String imageUrl = doc.getString("imageUrl");
                    String status = doc.getString("status");
                    
                    // Author text
                    android.widget.TextView tvName = itemView.findViewById(R.id.tv_public_user_name);
                    if (tvName != null) tvName.setText(authorName != null ? authorName : "Học viên");

                    // Avatar
                    android.widget.ImageView ivAvatar = itemView.findViewById(R.id.iv_public_user_avatar);
                    if (ivAvatar != null) {
                        if (authorAvatar != null && !authorAvatar.isEmpty()) {
                            if (authorAvatar.startsWith("data:image")) {
                                byte[] decodedString = android.util.Base64.decode(authorAvatar.split(",")[1], android.util.Base64.DEFAULT);
                                com.bumptech.glide.Glide.with(this).load(decodedString).circleCrop().into(ivAvatar);
                            } else {
                                com.bumptech.glide.Glide.with(this).load(authorAvatar).circleCrop().into(ivAvatar);
                            }
                        }
                    }

                    // Artwork
                    android.widget.ImageView ivArtwork = itemView.findViewById(R.id.iv_public_artwork);
                    if (ivArtwork != null && imageUrl != null && !imageUrl.isEmpty()) {
                        byte[] decodedString = android.util.Base64.decode(imageUrl.split(",")[1], android.util.Base64.DEFAULT);
                        com.bumptech.glide.Glide.with(this).load(decodedString).centerCrop().into(ivArtwork);
                    }

                    // Score logic
                    if ("GRADED".equals(status)) {
                        android.view.View scoreLayout = itemView.findViewById(R.id.ll_public_score);
                        android.widget.TextView tvScore = itemView.findViewById(R.id.tv_public_score);
                        if (scoreLayout != null) scoreLayout.setVisibility(android.view.View.VISIBLE);
                        if (tvScore != null) {
                            Number score = doc.getDouble("score");
                            tvScore.setText((score != null ? score.intValue() : 0) + "/100");
                        }
                    }

                    // Like button logic
                    android.view.View btnLike = itemView.findViewById(R.id.btn_public_like);
                    android.widget.ImageView ivLikeIcon = itemView.findViewById(R.id.iv_public_like_icon);
                    android.widget.TextView tvLikeCount = itemView.findViewById(R.id.tv_public_like_count);
                    
                    long likesCount = doc.getLong("likesCount") != null ? doc.getLong("likesCount") : 0;
                    if (tvLikeCount != null) tvLikeCount.setText(String.valueOf(likesCount));
                    
                    if (btnLike != null) {
                        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                        String currentUid = currentUser != null ? currentUser.getUid() : "";
                        
                        java.util.List<String> likedBy = (java.util.List<String>) doc.get("likedBy");
                        boolean initiallyLiked = likedBy != null && likedBy.contains(currentUid);
                        
                        // Initial UI state
                        if (initiallyLiked) {
                            ivLikeIcon.setColorFilter(android.graphics.Color.parseColor("#E74C3C")); // Red
                            btnLike.setTag(true);
                        } else {
                            ivLikeIcon.setColorFilter(android.graphics.Color.parseColor("#888888")); // Gray
                            btnLike.setTag(false);
                        }
                        
                        btnLike.setOnClickListener(v -> {
                            if (currentUid.isEmpty()) return; // User not logged in, theoretically impossible here
                            
                            boolean isLiked = (v.getTag() != null && (boolean)v.getTag());
                            
                            try {
                                long currentUiCount = Long.parseLong(tvLikeCount.getText().toString());
                                if (!isLiked) {
                                    // Optimistic UI update: Like
                                    ivLikeIcon.setColorFilter(android.graphics.Color.parseColor("#E74C3C"));
                                    tvLikeCount.setText(String.valueOf(currentUiCount + 1));
                                    v.setTag(true);
                                    
                                    db.collection("Challenge_Submissions").document(doc.getId()).update(
                                        "likedBy", com.google.firebase.firestore.FieldValue.arrayUnion(currentUid),
                                        "likesCount", com.google.firebase.firestore.FieldValue.increment(1)
                                    );
                                } else {
                                    // Optimistic UI update: Unlike
                                    ivLikeIcon.setColorFilter(android.graphics.Color.parseColor("#888888"));
                                    tvLikeCount.setText(String.valueOf(Math.max(0, currentUiCount - 1)));
                                    v.setTag(false);
                                    
                                    db.collection("Challenge_Submissions").document(doc.getId()).update(
                                        "likedBy", com.google.firebase.firestore.FieldValue.arrayRemove(currentUid),
                                        "likesCount", com.google.firebase.firestore.FieldValue.increment(-1)
                                    );
                                }
                            } catch (Exception e) {}
                        });
                    }

                    // Comment button logic
                    android.view.View btnComment = itemView.findViewById(R.id.btn_public_comment);
                    android.widget.TextView tvCommentCount = itemView.findViewById(R.id.tv_public_comment_count);
                    
                    long commentsCount = doc.getLong("commentsCount") != null ? doc.getLong("commentsCount") : 0;
                    if (tvCommentCount != null) tvCommentCount.setText(String.valueOf(commentsCount));

                    if (btnComment != null) {
                        btnComment.setOnClickListener(v -> {
                            android.content.Intent intent = new android.content.Intent(this, SubmissionDetailActivity.class);
                            intent.putExtra("SUBMISSION_ID", doc.getId());
                            startActivity(intent);
                        });
                    }

                    container.addView(itemView);
                }
            });
    }

    private void populatePodiumItem(com.google.firebase.firestore.DocumentSnapshot doc, int ivId, int tvNameId, int tvScoreId) {
        String authorName = doc.getString("userName");
        String authorAvatar = doc.getString("userAvatar");
        Number score = doc.getDouble("score");
        
        android.widget.TextView tvName = findViewById(tvNameId);
        android.widget.TextView tvScore = findViewById(tvScoreId);
        android.widget.ImageView ivAvatar = findViewById(ivId);
        
        if (tvName != null) tvName.setText(authorName != null ? authorName : "Học viên");
        if (tvScore != null) tvScore.setText((score != null ? score.intValue() : 0) + " xp");
        if (ivAvatar != null) {
            if (authorAvatar != null && !authorAvatar.isEmpty()) {
                if (authorAvatar.startsWith("data:image")) {
                     byte[] decodedString = android.util.Base64.decode(authorAvatar.split(",")[1], android.util.Base64.DEFAULT);
                     com.bumptech.glide.Glide.with(this).load(decodedString).circleCrop().into(ivAvatar);
                } else {
                     com.bumptech.glide.Glide.with(this).load(authorAvatar).circleCrop().into(ivAvatar);
                }
            } else {
                com.bumptech.glide.Glide.with(this).load(R.drawable.ic_default_user).circleCrop().into(ivAvatar);
            }
        }
    }
}
