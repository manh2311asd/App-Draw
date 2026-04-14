package com.example.appdraw.challenge;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.appdraw.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChallengeSubmissionsActivity extends AppCompatActivity {

    private String challengeTitle;
    private LinearLayout container;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_submissions);

        challengeTitle = getIntent().getStringExtra("CHALLENGE_TITLE");
        if (challengeTitle == null) challengeTitle = "Thử thách";

        Toolbar toolbar = findViewById(R.id.toolbar_challenge_submissions);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        container = findViewById(R.id.ll_submissions_container);
        db = FirebaseFirestore.getInstance();

        loadSubmissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSubmissions();
    }

    private void loadSubmissions() {
        if (container == null) return;
        
        db.collection("Challenge_Submissions")
            .whereEqualTo("challengeTitle", challengeTitle)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                container.removeAllViews();
                
                if (queryDocumentSnapshots.isEmpty()) {
                    TextView tvEmpty = new TextView(this);
                    tvEmpty.setText("Chưa có bài dự thi nào.");
                    tvEmpty.setPadding(32, 32, 32, 32);
                    container.addView(tvEmpty);
                    return;
                }

                LayoutInflater inflater = LayoutInflater.from(this);
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    View itemView = inflater.inflate(R.layout.item_challenge_submission, container, false);
                    
                    String id = doc.getId();
                    String author = doc.getString("userName");
                    String status = doc.getString("status");
                    String imageUrl = doc.getString("imageUrl");

                    TextView tvAuthor = itemView.findViewById(R.id.tv_submission_author);
                    TextView tvStatus = itemView.findViewById(R.id.tv_submission_status);
                    ImageView ivThumb = itemView.findViewById(R.id.iv_submission_thumb);
                    MaterialButton btnGrade = itemView.findViewById(R.id.btn_grade);

                    if (tvAuthor != null) tvAuthor.setText(author != null ? author : "Học viên");
                    
                    if (tvStatus != null) {
                        if ("GRADED".equals(status)) {
                            tvStatus.setText("Đã chấm");
                            tvStatus.setTextColor(android.graphics.Color.parseColor("#2ECC71"));
                            tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#E8F8F5"));
                            if (btnGrade != null) btnGrade.setText("Xem lại");
                        } else {
                            tvStatus.setText("Đang chờ duyệt");
                            tvStatus.setTextColor(android.graphics.Color.parseColor("#E67E22"));
                            tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#FEF5E7"));
                        }
                    }

                    if (ivThumb != null && imageUrl != null && !imageUrl.isEmpty()) {
                        byte[] decodedString = android.util.Base64.decode(imageUrl.split(",")[1], android.util.Base64.DEFAULT);
                        Glide.with(this).load(decodedString).centerCrop().into(ivThumb);
                    }

                    if (btnGrade != null) {
                        btnGrade.setOnClickListener(v -> {
                            Intent intent = new Intent(this, GradeSubmissionActivity.class);
                            intent.putExtra("SUBMISSION_ID", id);
                            startActivity(intent);
                        });
                    }
                    
                    container.addView(itemView);
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi tải danh sách: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
