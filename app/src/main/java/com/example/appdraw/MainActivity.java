package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private View navHome, navExplore, navCommunity, navProfile;
    private ImageView ivHome, ivExplore, ivCommunity, ivProfile;
    private TextView tvHome, tvExplore, tvCommunity, tvProfile;
    private FloatingActionButton fabDraw;
    private int primaryBlue, textGray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        primaryBlue = ContextCompat.getColor(this, R.color.primary_blue);
        textGray = ContextCompat.getColor(this, R.color.text_gray);

        setupNavigation();

        // Mặc định load HomeFragment
        loadFragment(new HomeFragment());
        updateUI(0);
    }
    
    private void setupNavigation() {
        navHome = findViewById(R.id.nav_home);
        navExplore = findViewById(R.id.nav_explore);
        navCommunity = findViewById(R.id.nav_community);
        navProfile = findViewById(R.id.nav_profile);

        ivHome = findViewById(R.id.iv_home);
        ivExplore = findViewById(R.id.iv_explore);
        ivCommunity = findViewById(R.id.iv_community);
        ivProfile = findViewById(R.id.iv_profile);

        tvHome = findViewById(R.id.tv_home);
        tvExplore = findViewById(R.id.tv_explore);
        tvCommunity = findViewById(R.id.tv_community);
        tvProfile = findViewById(R.id.tv_profile);

        fabDraw = findViewById(R.id.fab_draw);
        
        navHome.setOnClickListener(v -> {
            loadFragment(new HomeFragment());
            updateUI(0);
        });
        navExplore.setOnClickListener(v -> {
            loadFragment(new ExploreFragment());
            updateUI(1);
        });
        navCommunity.setOnClickListener(v -> {
            loadFragment(new CommunityFragment());
            updateUI(2);
        });
        navProfile.setOnClickListener(v -> {
            updateUI(3);
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        if (fabDraw != null) {
            fabDraw.setOnClickListener(v -> {
                showCreatePostOptions();
            });
        }
    }

    private void showCreatePostOptions() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_options, null);
        bottomSheetDialog.setContentView(dialogView);

        dialogView.findViewById(R.id.ll_option_draw).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            startActivity(new Intent(this, DrawingActivity.class));
        });

        dialogView.findViewById(R.id.ll_option_post).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            startActivity(new Intent(this, CreatePostActivity.class));
        });

        bottomSheetDialog.show();
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void updateUI(int index) {
        ivHome.setColorFilter(textGray);
        tvHome.setTextColor(textGray);
        ivExplore.setColorFilter(textGray);
        tvExplore.setTextColor(textGray);
        ivCommunity.setColorFilter(textGray);
        tvCommunity.setTextColor(textGray);
        ivProfile.setColorFilter(textGray);
        tvProfile.setTextColor(textGray);

        switch (index) {
            case 0:
                ivHome.setColorFilter(primaryBlue);
                tvHome.setTextColor(primaryBlue);
                break;
            case 1:
                ivExplore.setColorFilter(primaryBlue);
                tvExplore.setTextColor(primaryBlue);
                break;
            case 2:
                ivCommunity.setColorFilter(primaryBlue);
                tvCommunity.setTextColor(primaryBlue);
                break;
            case 3:
                ivProfile.setColorFilter(primaryBlue);
                tvProfile.setTextColor(primaryBlue);
                break;
        }
    }
}
