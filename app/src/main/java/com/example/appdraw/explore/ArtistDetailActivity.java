package com.example.appdraw.explore;

import android.os.Bundle;
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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ArtistDetailActivity extends AppCompatActivity {
    private String artistId;
    private boolean isFollowing = false;
    private FirebaseFirestore db;
    private String currentUid;

    private MaterialButton btnFollow;
    private TextView tvFollowersCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        artistId = getIntent().getStringExtra("ARTIST_ID");
        String name = getIntent().getStringExtra("ARTIST_NAME");
        String avatarUrl = getIntent().getStringExtra("ARTIST_AVATAR");
        String bio = getIntent().getStringExtra("ARTIST_BIO");

        if (name != null) ((TextView) findViewById(R.id.tv_artist_name_detail)).setText(name);
        if (bio != null) ((TextView) findViewById(R.id.tv_artist_bio)).setText(bio);
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this).load(avatarUrl).circleCrop().into((ImageView) findViewById(R.id.iv_artist_large));
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Setup Tabs
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        LinearLayout llTabLessons = findViewById(R.id.ll_tab_lessons);
        LinearLayout llTabArtworks = findViewById(R.id.ll_tab_artworks);

        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 0) {
                        llTabLessons.setVisibility(View.VISIBLE);
                        llTabArtworks.setVisibility(View.GONE);
                    } else {
                        llTabLessons.setVisibility(View.GONE);
                        llTabArtworks.setVisibility(View.VISIBLE);
                    }
                }
                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

        // Setup Follow System
        btnFollow = findViewById(R.id.btn_follow);
        tvFollowersCount = findViewById(R.id.tv_followers_count);

        if (artistId != null && currentUid != null) {
            getFollowersCount(); // LUÔN LUÔN hiển thị số người theo dõi

            if (artistId.equals(currentUid)) {
                btnFollow.setVisibility(View.GONE);
            } else {
                checkFollowStatus();
                btnFollow.setOnClickListener(v -> toggleFollow());
            }
        } else {
            btnFollow.setVisibility(View.GONE);
        }
    }

    private void checkFollowStatus() {
        db.collection("Users").document(artistId).collection("followers").document(currentUid)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    isFollowing = true;
                    updateFollowButtonUI();
                }
            });
    }

    private void getFollowersCount() {
        db.collection("Users").document(artistId).collection("followers")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int count = queryDocumentSnapshots.size();
                tvFollowersCount.setText(count + " người theo dõi");
            });
    }

    private void toggleFollow() {
        btnFollow.setEnabled(false); // disable while processing
        DocumentReference artistFollowerRef = db.collection("Users").document(artistId).collection("followers").document(currentUid);
        DocumentReference myFollowingRef = db.collection("Users").document(currentUid).collection("following").document(artistId);

        if (isFollowing) {
            // Unfollow
            artistFollowerRef.delete().addOnSuccessListener(aVoid -> {
                myFollowingRef.delete();
                isFollowing = false;
                updateFollowButtonUI();
                getFollowersCount();
                btnFollow.setEnabled(true);
            }).addOnFailureListener(e -> btnFollow.setEnabled(true));
        } else {
            // Follow
            artistFollowerRef.set(new HashMap<>()).addOnSuccessListener(aVoid -> {
                myFollowingRef.set(new HashMap<>());
                isFollowing = true;
                updateFollowButtonUI();
                getFollowersCount();
                btnFollow.setEnabled(true);
            }).addOnFailureListener(e -> btnFollow.setEnabled(true));
        }
    }

    private void updateFollowButtonUI() {
        if (isFollowing) {
            btnFollow.setText("Đang theo dõi");
            btnFollow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE0E0E0)); // Grey
            btnFollow.setTextColor(0xFF555555);
        } else {
            btnFollow.setText("Theo dõi");
            btnFollow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4272D0)); // Primary Blue
            btnFollow.setTextColor(0xFFFFFFFF);
        }
    }
}
