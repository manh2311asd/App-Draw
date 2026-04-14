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
                if ("GRADED".equals(existingStatus)) {
                    Number scoreObj = doc.getDouble("score");
                    if (scoreObj != null) {
                        ((EditText) findViewById(R.id.edt_grade_score)).setText(String.valueOf(scoreObj.intValue()));
                    }
                    String existingFeedback = doc.getString("feedback");
                    if (existingFeedback != null) {
                        ((EditText) findViewById(R.id.edt_grade_feedback)).setText(existingFeedback);
                    }
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

        Map<String, Object> gradeData = new HashMap<>();
        gradeData.put("score", score);
        gradeData.put("feedback", feedback);
        gradeData.put("status", "GRADED");

        final int finalScore = score;
        db.collection("Challenge_Submissions").document(submissionId).update(gradeData)
            .addOnSuccessListener(aVoid -> {
                // Must also update User's personal status so they see the fireworks
                if (submissionUserId != null && challengeTitle != null) {
                    Map<String, Object> userUpdates = new HashMap<>();
                    userUpdates.put("status", "GRADED");
                    userUpdates.put("score", finalScore);
                    userUpdates.put("feedback", feedback);
                    db.collection("Users").document(submissionUserId)
                      .collection("joinedChallenges").document(challengeTitle)
                      .update(userUpdates);
                }
                
                Toast.makeText(this, "Chấm điểm thành công!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                 Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                 btnSubmitGrade.setEnabled(true);
                 btnSubmitGrade.setText("HOÀN TẤT CHẤM ĐIỂM");
            });
    }
}
