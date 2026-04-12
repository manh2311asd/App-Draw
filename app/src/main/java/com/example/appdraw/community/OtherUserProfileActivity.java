package com.example.appdraw.community;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appdraw.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class OtherUserProfileActivity extends AppCompatActivity {

    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        ImageView ivBack = findViewById(R.id.iv_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        String userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            String userName = getIntent().getStringExtra("USER_NAME"); // backward compatibility
            if (userName != null) {
                TextView tvOtherName = findViewById(R.id.tv_other_name);
                if (tvOtherName != null) tvOtherName.setText(userName);
            }
            return;
        }

        String currentUid = FirebaseAuth.getInstance().getUid();

        TextView tvOtherName = findViewById(R.id.tv_other_name);
        ImageView ivAvatar = findViewById(R.id.iv_other_avatar);
        TextView tvFollowStatus = findViewById(R.id.tv_follow_status);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Load User Info
        db.collection("Users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists() && doc.contains("profile")) {
                java.util.Map<String, Object> profile = (java.util.Map<String, Object>) doc.get("profile");
                if (profile != null) {
                    if (tvOtherName != null) tvOtherName.setText((String) profile.get("fullName"));
                    String avatarUrl = (String) profile.get("avatarUrl");
                    if (ivAvatar != null && avatarUrl != null) {
                        Glide.with(this).load(avatarUrl).into(ivAvatar);
                    }
                }
            }
        });

        // Check follow status (Simulated by checking a document in Follows collection)
        if (currentUid != null && tvFollowStatus != null) {
            DocumentReference followRef = db.collection("Follows").document(currentUid + "_" + userId);
            followRef.get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    isFollowing = true;
                    tvFollowStatus.setText("Đang theo dõi");
                    tvFollowStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                }
            });

            tvFollowStatus.setOnClickListener(v -> {
                tvFollowStatus.setEnabled(false);
                if (!isFollowing) {
                    // Follow
                    followRef.set(new java.util.HashMap<>()).addOnSuccessListener(aVoid -> {
                        isFollowing = true;
                        tvFollowStatus.setText("Đang theo dõi");
                        tvFollowStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                        
                        db.collection("Users").document(userId).update("followersCount", FieldValue.increment(1));
                        db.collection("Users").document(currentUid).update("followingCount", FieldValue.increment(1));
                        
                        Toast.makeText(this, "Đã theo dõi", Toast.LENGTH_SHORT).show();
                        tvFollowStatus.setEnabled(true);
                    });
                } else {
                    // Unfollow
                    followRef.delete().addOnSuccessListener(aVoid -> {
                        isFollowing = false;
                        tvFollowStatus.setText("+ Theo dõi");
                        tvFollowStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#B0BEC5")));
                        
                        db.collection("Users").document(userId).update("followersCount", FieldValue.increment(-1));
                        db.collection("Users").document(currentUid).update("followingCount", FieldValue.increment(-1));
                        
                        Toast.makeText(this, "Bỏ theo dõi", Toast.LENGTH_SHORT).show();
                        tvFollowStatus.setEnabled(true);
                    });
                }
            });
        }
    }
}
