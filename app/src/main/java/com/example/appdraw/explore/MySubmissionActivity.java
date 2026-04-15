package com.example.appdraw.explore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdraw.R;
import com.example.appdraw.HomeworkActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MySubmissionActivity extends AppCompatActivity {

    private String lessonTitle;
    private ImageView ivMySubmission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_submission);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_my_submission);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ivMySubmission = findViewById(R.id.iv_my_submission);
        lessonTitle = getIntent().getStringExtra("LESSON_TITLE");
        if (lessonTitle == null) {
            lessonTitle = "Unknown Lesson";
        }

        findViewById(R.id.btn_study_other).setOnClickListener(v -> finish());
        findViewById(R.id.btn_redraw).setOnClickListener(v -> {
            Intent intent = new Intent(MySubmissionActivity.this, HomeworkActivity.class);
            intent.putExtra("LESSON_TITLE", lessonTitle);
            startActivity(intent);
            finish();
        });
        


        fetchMySubmission();
    }

    private void fetchMySubmission() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(uid)
                .collection("lessonProgress").document(lessonTitle)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String base64Url = documentSnapshot.getString("imageUrl");
                        if (base64Url != null && base64Url.startsWith("data:image")) {
                            String cleanBase64 = base64Url.substring(base64Url.indexOf(",") + 1);
                            byte[] decodedString = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT);
                            android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            ivMySubmission.setImageBitmap(decodedByte);
                            
                            // Handle AI Auto Grading
                            String existingFeedback = documentSnapshot.getString("aiFeedback");
                            String existingTip = documentSnapshot.getString("aiTip");
                            
                            if (existingFeedback != null && !existingFeedback.isEmpty()) {
                                // Already graded, just show it
                                findViewById(R.id.ll_ai_feedback_container).setVisibility(android.view.View.VISIBLE);
                                ((android.widget.TextView) findViewById(R.id.tv_ai_feedback_text)).setText(existingFeedback);
                                ((android.widget.TextView) findViewById(R.id.tv_ai_tip_text)).setText(existingTip != null ? existingTip : "");
                            } else {
                                // Not graded yet, call Gemini
                                findViewById(R.id.pb_ai_loading).setVisibility(android.view.View.VISIBLE);
                                findViewById(R.id.tv_ai_loading_text).setVisibility(android.view.View.VISIBLE);
                                
                                GeminiVisionService geminiService = new GeminiVisionService();
                                geminiService.gradeArtwork(lessonTitle, base64Url, new GeminiVisionService.GeminiCallback() {
                                    @Override
                                    public void onSuccess(String feedback, String tip) {
                                        findViewById(R.id.pb_ai_loading).setVisibility(android.view.View.GONE);
                                        findViewById(R.id.tv_ai_loading_text).setVisibility(android.view.View.GONE);
                                        
                                        findViewById(R.id.ll_ai_feedback_container).setVisibility(android.view.View.VISIBLE);
                                        ((android.widget.TextView) findViewById(R.id.tv_ai_feedback_text)).setText(feedback);
                                        ((android.widget.TextView) findViewById(R.id.tv_ai_tip_text)).setText(tip);
                                        
                                        // Save to Firestore to avoid calling API again
                                        db.collection("Users").document(uid)
                                          .collection("lessonProgress").document(lessonTitle)
                                          .update("aiFeedback", feedback, "aiTip", tip);
                                    }

                                    @Override
                                    public void onError(String error) {
                                        findViewById(R.id.pb_ai_loading).setVisibility(android.view.View.GONE);
                                        findViewById(R.id.tv_ai_loading_text).setVisibility(android.view.View.GONE);
                                        Toast.makeText(MySubmissionActivity.this, "Chấm điểm tạm thời bị lỗi: " + error, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Không thể tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
