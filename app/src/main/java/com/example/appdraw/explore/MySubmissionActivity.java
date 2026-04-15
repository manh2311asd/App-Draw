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

        fetchMySubmission();

        findViewById(R.id.btn_study_other).setOnClickListener(v -> {
            finish();
        });

        findViewById(R.id.btn_redraw).setOnClickListener(v -> {
            // They can go back and re-submit/redraw via HomeworkActivity
            Intent intent = new Intent(MySubmissionActivity.this, HomeworkActivity.class);
            intent.putExtra("LESSON_TITLE", lessonTitle);
            startActivity(intent);
            finish();
        });
    }

    private void fetchMySubmission() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("Users").document(uid)
                .collection("lessonProgress").document(lessonTitle)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String base64Url = documentSnapshot.getString("imageUrl");
                        if (base64Url != null && base64Url.startsWith("data:image")) {
                            String cleanBase64 = base64Url.substring(base64Url.indexOf(",") + 1);
                            byte[] decodedString = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT);
                            android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory
                                    .decodeByteArray(decodedString, 0, decodedString.length);
                            ivMySubmission.setImageBitmap(decodedByte);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Không thể tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
