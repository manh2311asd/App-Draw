package com.example.appdraw;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ProfileActivity extends AppCompatActivity {

    private TextView tabArtwork, tabPost, tabProject, tabSaved;
    private LinearLayout llAccount, llEditProfile, llLanguage, llPersonal, llLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupToolbar();
        setupTabs();
        setupSettings();

        // Check if viewing other user profile
        if (getIntent().getBooleanExtra("IS_OTHER_USER", false)) {
            String otherName = getIntent().getStringExtra("USER_NAME");
            TextView tvName = findViewById(R.id.tv_profile_name);
            if (tvName != null && otherName != null) {
                tvName.setText(otherName);
            }
        }

        // Xử lý nút Trực tiếp
        View llLiveStatus = findViewById(R.id.ll_live_status);
        if (llLiveStatus != null) {
            llLiveStatus.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, LiveStreamActivity.class);
                startActivity(intent);
            });
        }
    }

    private void initViews() {
        tabArtwork = findViewById(R.id.tab_artwork);
        tabPost = findViewById(R.id.tab_post);
        tabProject = findViewById(R.id.tab_project);
        tabSaved = findViewById(R.id.tab_saved);

        llAccount = findViewById(R.id.ll_setting_account);
        llEditProfile = findViewById(R.id.ll_setting_edit_profile);
        llLanguage = findViewById(R.id.ll_setting_language);
        llPersonal = findViewById(R.id.ll_setting_personal);
        llLogout = findViewById(R.id.ll_logout);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void setupTabs() {
        tabArtwork.setOnClickListener(v -> {
            resetTabs();
            tabArtwork.setTextColor(getResources().getColor(R.color.primary_blue));
            tabArtwork.setTypeface(null, android.graphics.Typeface.BOLD);
            findViewById(R.id.gl_artworks).setVisibility(View.VISIBLE);
        });

        tabPost.setOnClickListener(v -> {
            resetTabs();
            tabPost.setTextColor(getResources().getColor(R.color.primary_blue));
            tabPost.setTypeface(null, android.graphics.Typeface.BOLD);
            findViewById(R.id.gl_artworks).setVisibility(View.GONE);
            Toast.makeText(this, "Mục Bài viết", Toast.LENGTH_SHORT).show();
        });

        tabProject.setOnClickListener(v -> {
            resetTabs();
            tabProject.setTextColor(getResources().getColor(R.color.primary_blue));
            tabProject.setTypeface(null, android.graphics.Typeface.BOLD);
            findViewById(R.id.gl_artworks).setVisibility(View.GONE);
            
            // Chuyển sang trang danh sách dự án
            Intent intent = new Intent(ProfileActivity.this, ProjectListActivity.class);
            startActivity(intent);
        });

        tabSaved.setOnClickListener(v -> {
            resetTabs();
            tabSaved.setTextColor(getResources().getColor(R.color.primary_blue));
            tabSaved.setTypeface(null, android.graphics.Typeface.BOLD);
            findViewById(R.id.gl_artworks).setVisibility(View.GONE);
            Toast.makeText(this, "Mục Đã lưu", Toast.LENGTH_SHORT).show();
        });
    }

    private void resetTabs() {
        int gray = Color.parseColor("#888888");
        tabArtwork.setTextColor(gray);
        tabArtwork.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabPost.setTextColor(gray);
        tabPost.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabProject.setTextColor(gray);
        tabProject.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabSaved.setTextColor(gray);
        tabSaved.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    private void setupSettings() {
        if (llAccount != null) llAccount.setOnClickListener(v -> 
            Toast.makeText(this, "Cài đặt tài khoản", Toast.LENGTH_SHORT).show());
        
        if (llEditProfile != null) llEditProfile.setOnClickListener(v -> 
            Toast.makeText(this, "Chỉnh sửa hồ sơ", Toast.LENGTH_SHORT).show());
        
        if (llLanguage != null) llLanguage.setOnClickListener(v -> 
            Toast.makeText(this, "Cài đặt ngôn ngữ", Toast.LENGTH_SHORT).show());
        
        if (llPersonal != null) llPersonal.setOnClickListener(v -> 
            Toast.makeText(this, "Liên kết cá nhân", Toast.LENGTH_SHORT).show());
        
        if (llLogout != null) llLogout.setOnClickListener(v -> {
            // FirebaseAuth.getInstance().signOut(); // Tạm thời bỏ xác thực
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
