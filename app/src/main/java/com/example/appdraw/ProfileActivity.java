package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;

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

        // Check if viewing other user profile (from previous logic)
        if (getIntent().getBooleanExtra("IS_OTHER_USER", false)) {
            String otherName = getIntent().getStringExtra("USER_NAME");
            TextView tvName = findViewById(R.id.tv_profile_name);
            if (tvName != null && otherName != null) {
                tvName.setText(otherName);
            }
            // Hide settings if it's not the current user's profile
            View settingsCard = findViewById(R.id.ll_setting_account).getParent().getParent() instanceof View ? 
                               (View) findViewById(R.id.ll_setting_account).getParent().getParent() : null;
            // Simplified: for demo, we can just show/hide or change logic
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
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupTabs() {
        View.OnClickListener tabClick = v -> {
            resetTabs();
            TextView clickedTab = (TextView) v;
            clickedTab.setTextColor(getResources().getColor(R.color.primary_blue));
            clickedTab.setTypeface(null, android.graphics.Typeface.BOLD);
            
            String tabName = clickedTab.getText().toString();
            Toast.makeText(this, "Đang xem: " + tabName, Toast.LENGTH_SHORT).show();
        };

        tabArtwork.setOnClickListener(tabClick);
        tabPost.setOnClickListener(tabClick);
        tabProject.setOnClickListener(tabClick);
        tabSaved.setOnClickListener(tabClick);
    }

    private void resetTabs() {
        int gray = getResources().getColor(R.color.text_gray);
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
        llAccount.setOnClickListener(v -> 
            Toast.makeText(this, "Cài đặt tài khoản", Toast.LENGTH_SHORT).show());
        
        llEditProfile.setOnClickListener(v -> 
            Toast.makeText(this, "Chỉnh sửa hồ sơ", Toast.LENGTH_SHORT).show());
        
        llLanguage.setOnClickListener(v -> 
            Toast.makeText(this, "Cài đặt ngôn ngữ", Toast.LENGTH_SHORT).show());
        
        llPersonal.setOnClickListener(v -> 
            Toast.makeText(this, "Liên kết cá nhân", Toast.LENGTH_SHORT).show());
        
        llLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
