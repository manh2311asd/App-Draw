package com.example.appdraw.challenge;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.appdraw.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.Map;

public class UserScoreDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String challengeTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_score_detail);

        challengeTitle = getIntent().getStringExtra("CHALLENGE_TITLE");
        if (challengeTitle == null) {
            Toast.makeText(this, "Không tìm thấy thử thách", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar_user_score);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        loadSubmissionDetails();
    }

    private void loadSubmissionDetails() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("Challenge_Submissions")
            .whereEqualTo("userId", user.getUid())
            .whereEqualTo("challengeTitle", challengeTitle)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    Toast.makeText(this, "Chưa tìm thấy bài nộp của bạn", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                
                // Content
                String imageUrl = doc.getString("imageUrl");
                String note = doc.getString("note");
                
                ImageView ivArtwork = findViewById(R.id.iv_score_artwork);
                TextView tvNote = findViewById(R.id.tv_score_artwork_note);
                
                if (note != null && !note.trim().isEmpty()) {
                    tvNote.setText(note);
                } else {
                    tvNote.setText("Không có mô tả.");
                }

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    byte[] decodedString = android.util.Base64.decode(imageUrl.split(",")[1], android.util.Base64.DEFAULT);
                    Glide.with(this).load(decodedString).centerCrop().into(ivArtwork);
                }

                // Average Score
                Number scoreObj = doc.getDouble("score");
                TextView tvScoreAvg = findViewById(R.id.tv_score_average);
                if (scoreObj != null) {
                    tvScoreAvg.setText(String.valueOf(scoreObj.intValue()));
                }

                // Grades List
                List<Map<String, Object>> grades = (List<Map<String, Object>>) doc.get("grades");
                LinearLayout container = findViewById(R.id.ll_mentor_grades_container);
                TextView tvNoFeedbacks = findViewById(R.id.tv_no_feedbacks);

                container.removeAllViews();
                
                if (grades == null || grades.isEmpty()) {
                    tvNoFeedbacks.setVisibility(android.view.View.VISIBLE);
                } else {
                    tvNoFeedbacks.setVisibility(android.view.View.GONE);
                    for (Map<String, Object> g : grades) {
                        String mName = (String) g.get("mentorName");
                        Number mScore = (Number) g.get("score");
                        String mFeedback = (String) g.get("feedback");

                        // Actually, I should just build a clean Layout programmatically to avoid creating too many XML files.
                        
                        LinearLayout itemLayout = new LinearLayout(this);
                        itemLayout.setOrientation(LinearLayout.VERTICAL);
                        itemLayout.setBackgroundResource(android.R.color.white);
                        
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(0, 0, 0, 32);
                        itemLayout.setLayoutParams(params);
                        itemLayout.setPadding(32, 24, 32, 24);
                        
                        // Border radius and elevation fallback natively
                        itemLayout.setBackgroundColor(android.graphics.Color.WHITE);

                        TextView tvMentor = new TextView(this);
                        tvMentor.setText(mName != null ? mName : "Mentor");
                        tvMentor.setTextColor(android.graphics.Color.parseColor("#4272D0"));
                        tvMentor.setTextSize(14f);
                        tvMentor.setTypeface(null, android.graphics.Typeface.BOLD);
                        
                        LinearLayout rowScore = new LinearLayout(this);
                        rowScore.setOrientation(LinearLayout.HORIZONTAL);
                        rowScore.setPadding(0, 8, 0, 8);
                        
                        TextView tvScoreLabel = new TextView(this);
                        tvScoreLabel.setText("Điểm: ");
                        tvScoreLabel.setTextColor(android.graphics.Color.parseColor("#333333"));
                        tvScoreLabel.setTextSize(14f);
                        
                        TextView tvScoreVal = new TextView(this);
                        tvScoreVal.setText((mScore != null ? mScore.intValue() : 0) + "/100");
                        tvScoreVal.setTextColor(android.graphics.Color.parseColor("#2ECC71"));
                        tvScoreVal.setTextSize(14f);
                        tvScoreVal.setTypeface(null, android.graphics.Typeface.BOLD);
                        
                        rowScore.addView(tvScoreLabel);
                        rowScore.addView(tvScoreVal);
                        
                        TextView tvFb = new TextView(this);
                        tvFb.setText("\"" + (mFeedback != null ? mFeedback : "Không có nhận xét") + "\"");
                        tvFb.setTextColor(android.graphics.Color.parseColor("#555555"));
                        tvFb.setTextSize(14f);
                        tvFb.setPadding(0, 4, 0, 0);

                        itemLayout.addView(tvMentor);
                        itemLayout.addView(rowScore);
                        itemLayout.addView(tvFb);

                        // Card wrapper for shadow
                        androidx.cardview.widget.CardView card = new androidx.cardview.widget.CardView(this);
                        card.setRadius(24f);
                        card.setCardElevation(4f);
                        card.setUseCompatPadding(true);
                        card.addView(itemLayout);

                        container.addView(card);
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi tải kết quả", Toast.LENGTH_SHORT).show();
            });
    }
}
