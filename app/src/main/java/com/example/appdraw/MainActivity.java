package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.appdraw.community.CommunityFragment;
import com.example.appdraw.drawing.DrawingActivity;
import com.example.appdraw.explore.ExploreFragment;
import com.example.appdraw.main.HomeFragment;

public class MainActivity extends AppCompatActivity {

    private View navHome, navExplore, navCommunity, navProfile;
    private ImageView ivHome, ivExplore, ivCommunity, ivProfile;
    private TextView tvHome, tvExplore, tvCommunity, tvProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupNavigation();

        // Default fragment
        loadFragment(new HomeFragment());
    }

    private void initViews() {
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

        findViewById(R.id.fab_draw).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DrawingActivity.class);
            startActivity(intent);
        });
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> {
            loadFragment(new HomeFragment());
            updateNavUI(0);
        });

        navExplore.setOnClickListener(v -> {
            loadFragment(new ExploreFragment());
            updateNavUI(1);
        });

        navCommunity.setOnClickListener(v -> {
            loadFragment(new CommunityFragment());
            updateNavUI(2);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void updateNavUI(int selectedIndex) {
        int activeColor = ContextCompat.getColor(this, R.color.primary_blue);
        int inactiveColor = ContextCompat.getColor(this, R.color.text_gray);

        ivHome.setColorFilter(selectedIndex == 0 ? activeColor : inactiveColor);
        tvHome.setTextColor(selectedIndex == 0 ? activeColor : inactiveColor);

        ivExplore.setColorFilter(selectedIndex == 1 ? activeColor : inactiveColor);
        tvExplore.setTextColor(selectedIndex == 1 ? activeColor : inactiveColor);

        ivCommunity.setColorFilter(selectedIndex == 2 ? activeColor : inactiveColor);
        tvCommunity.setTextColor(selectedIndex == 2 ? activeColor : inactiveColor);

        // Profile is an activity, so we don't necessarily "select" it in the same way here 
        // if it stays on top of MainActivity, but we can reset the colors anyway.
        ivProfile.setColorFilter(inactiveColor);
        tvProfile.setTextColor(inactiveColor);
    }
}
