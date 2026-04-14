package com.example.appdraw.challenge;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.appdraw.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class GradeSubmissionActivity extends AppCompatActivity {

    private String submissionId;
    private FirebaseFirestore db;
    
    private String submissionUserId;
    private String challengeTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_submission);

        submissionId = getIntent().getStringExtra("SUBMISSION_ID");
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar_grade_submission);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        if (submissionId == null) {
            Toast.makeText(this, "Lỗi ID bài nộp", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadSubmissionDetails();
        
        MaterialButton btnSubmitGrade = findViewById(R.id.btn_submit_grade);
        if (btnSubmitGrade != null) {
            btnSubmitGrade.setOnClickListener(v -> submitGrade());
        }
    }

    private void loadSubmissionDetails() {
        db.collection("Challenge_Submissions").document(submissionId).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) return;
                
                submissionUserId = doc.getString("userId");
                challengeTitle = doc.getString("challengeTitle");
                
                String studentName = doc.getString("userName");
                String studentAvatar = doc.getString("userAvatar");
                String note = doc.getString("note");
                String imageUrl = doc.getString("imageUrl");
                
                // Pre-fill existing grades if any
                String existingStatus = doc.getString("status");
                String currentUserUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
                java.util.List<Map<String, Object>> grades = (java.util.List<Map<String, Object>>) doc.get("grades");
                
                android.widget.LinearLayout llPreviousFeedbacks = findViewById(R.id.ll_previous_mentor_feedbacks);
                android.widget.LinearLayout llPreviousFeedbacksContainer = findViewById(R.id.ll_previous_feedbacks_container);
                if (llPreviousFeedbacks != null) llPreviousFeedbacks.removeAllViews();
                
                boolean hasOtherFeedbacks = false;
                
                if (grades != null) {
                    for (Map<String, Object> g : grades) {
                        String mId = (String) g.get("mentorId");
                        String mName = (String) g.get("mentorName");
                        Number mScore = (Number) g.get("score");
                        String mFeedback = (String) g.get("feedback");
                        
                        if (currentUserUid != null && currentUserUid.equals(mId)) {
                            // Của chính mình thì tự điền vào Form
                            if (mScore != null) {
                                ((EditText) findViewById(R.id.edt_grade_score)).setText(String.valueOf(mScore.intValue()));
                            }
                            if (mFeedback != null) {
                                ((EditText) findViewById(R.id.edt_grade_feedback)).setText(mFeedback);
                            }
                        } else {
                            // Của Mentor khác -> add vào View
                            hasOtherFeedbacks = true;
                            if (llPreviousFeedbacks != null) {
                                TextView tvFb = new TextView(this);
                                tvFb.setText("• " + mName + " chấm " + (mScore != null ? mScore.intValue() : 0) + " điểm: \"" + mFeedback + "\"");
                                tvFb.setTextColor(android.graphics.Color.parseColor("#444444"));
                                tvFb.setTextSize(13);
                                tvFb.setPadding(0, 4, 0, 4);
                                llPreviousFeedbacks.addView(tvFb);
                            }
                        }
                    }
                } else if ("GRADED".equals(existingStatus)) {
                    // Fallback tương thích ngược (nếu dữ liệu cũ)
                    Number scoreObj = doc.getDouble("score");
                    if (scoreObj != null) {
                        ((EditText) findViewById(R.id.edt_grade_score)).setText(String.valueOf(scoreObj.intValue()));
                    }
                    String existingFeedback = doc.getString("feedback");
                    if (existingFeedback != null) {
                        ((EditText) findViewById(R.id.edt_grade_feedback)).setText(existingFeedback);
                    }
                }
                
                if (hasOtherFeedbacks && llPreviousFeedbacksContainer != null) {
                    llPreviousFeedbacksContainer.setVisibility(android.view.View.VISIBLE);
                }
                
                android.widget.LinearLayout llProfile = findViewById(R.id.ll_grade_student_profile);
                if (llProfile != null) {
                     llProfile.setOnClickListener(v -> {
                         android.content.Intent intent = new android.content.Intent(this, com.example.appdraw.community.OtherUserProfileActivity.class);
                         intent.putExtra("USER_ID", submissionUserId);
                         startActivity(intent);
                     });
                }

                TextView tvName = findViewById(R.id.tv_grade_student_name);
                TextView tvTitle = findViewById(R.id.tv_grade_challenge_title);
                TextView tvNote = findViewById(R.id.tv_grade_artwork_note);
                ImageView ivAvatar = findViewById(R.id.iv_grade_student_avatar);
                ImageView ivArtwork = findViewById(R.id.iv_grade_artwork);

                if (tvName != null) tvName.setText(studentName);
                if (tvTitle != null) tvTitle.setText("Thử thách: " + challengeTitle);
                if (tvNote != null) tvNote.setText((note != null && !note.trim().isEmpty()) ? note : "Không có học viên mô tả.");

                if (ivArtwork != null && imageUrl != null && !imageUrl.isEmpty()) {
                    byte[] decodedString = android.util.Base64.decode(imageUrl.split(",")[1], android.util.Base64.DEFAULT);
                    Glide.with(this).load(decodedString).centerCrop().into(ivArtwork);
                }
                
                if (ivAvatar != null) {
                    if (studentAvatar != null && !studentAvatar.isEmpty()) {
                        if (studentAvatar.startsWith("data:image")) {
                             byte[] decodedString = android.util.Base64.decode(studentAvatar.split(",")[1], android.util.Base64.DEFAULT);
                             Glide.with(this).load(decodedString).circleCrop().into(ivAvatar);
                        } else {
                             Glide.with(this).load(studentAvatar).circleCrop().into(ivAvatar);
                        }
                    } else {
                        Glide.with(this).load(R.drawable.ic_default_user).circleCrop().into(ivAvatar);
                    }
                }
            });
    }

    private void submitGrade() {
        EditText edtScore = findViewById(R.id.edt_grade_score);
        EditText edtFeedback = findViewById(R.id.edt_grade_feedback);
        
        String scoreStr = edtScore.getText().toString().trim();
        String feedback = edtFeedback.getText().toString().trim();
        
        if (scoreStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập điểm!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int score = 0;
        try {
            score = Integer.parseInt(scoreStr);
            if (score < 0 || score > 100) {
                 Toast.makeText(this, "Điểm phải từ 0 - 100", Toast.LENGTH_SHORT).show();
                 return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Điểm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        MaterialButton btnSubmitGrade = findViewById(R.id.btn_submit_grade);
        btnSubmitGrade.setEnabled(false);
        btnSubmitGrade.setText("Đang lưu...");

        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        final int finalScore = score;
        final String finalFeedback = feedback;
        
        db.collection("Users").document(currentUser.getUid()).get()
            .addOnSuccessListener(mentorDoc -> {
                String mentorName = "Mentor";
                if (mentorDoc.exists()) {
                     java.util.Map<String, Object> profile = (java.util.Map<String, Object>) mentorDoc.get("profile");
                     if (profile != null && profile.containsKey("fullName")) {
                          mentorName = (String) profile.get("fullName");
                     }
                }
                
                Map<String, Object> gradeEntry = new HashMap<>();
                gradeEntry.put("mentorId", currentUser.getUid());
                gradeEntry.put("mentorName", mentorName);
                gradeEntry.put("score", finalScore);
                gradeEntry.put("feedback", finalFeedback);
                
                db.runTransaction(transaction -> {
                    com.google.firebase.firestore.DocumentReference submissionRef = db.collection("Challenge_Submissions").document(submissionId);
                    com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(submissionRef);
                    
                    if (!snapshot.exists()) return null;
                    
                    java.util.List<Map<String, Object>> gradesList = (java.util.List<Map<String, Object>>) snapshot.get("grades");
                    java.util.List<Map<String, Object>> grades = new java.util.ArrayList<>();
                    if (gradesList != null) {
                        grades.addAll(gradesList);
                    }
                    
                    // Xóa điểm cũ của chính mentor này (nếu họ sửa điểm)
                    for (int i = 0; i < grades.size(); i++) {
                        if (currentUser.getUid().equals(grades.get(i).get("mentorId"))) {
                            grades.remove(i);
                            break;
                        }
                    }
                    
                    grades.add(gradeEntry);
                    
                    // Tính điểm trung bình mới
                    long totalScore = 0;
                    for (Map<String, Object> g : grades) {
                        totalScore += ((Number) g.get("score")).longValue();
                    }
                    long newAverageScore = totalScore / grades.size();
                    
                    transaction.update(submissionRef, "grades", grades);
                    transaction.update(submissionRef, "score", newAverageScore);
                    transaction.update(submissionRef, "status", "GRADED");
                    return newAverageScore;
                }).addOnSuccessListener(avgScore -> {
                    if (submissionUserId != null && challengeTitle != null) {
                        Map<String, Object> userUpdates = new HashMap<>();
                        userUpdates.put("status", "GRADED");
                        userUpdates.put("score", avgScore); // Save average
                        userUpdates.put("feedback", finalFeedback); 
                        db.collection("Users").document(submissionUserId)
                          .collection("joinedChallenges").document(challengeTitle)
                          .update(userUpdates);
                    }
                    
                    Toast.makeText(this, "Chấm điểm thành công (TB: " + avgScore + " điểm)!", Toast.LENGTH_SHORT).show();
                    finish();
                }).addOnFailureListener(e -> {
                     Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                     btnSubmitGrade.setEnabled(true);
                     btnSubmitGrade.setText("HOÀN TẤT CHẤM ĐIỂM");
                });
            });
    }
}
