package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.tabs.TabLayout;

public class ChallengeGradingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_grading);

        Toolbar toolbar = findViewById(R.id.toolbar_grading);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ImageView btnAdd = findViewById(R.id.btn_add_challenge_grading);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> {
                Intent intent = new Intent(this, CreateChallengeActivity.class);
                startActivity(intent);
            });
        }

        setupGradingItems();

        TabLayout tabLayout = findViewById(R.id.tab_layout_grading);
        if (tabLayout != null) {
            TabLayout.Tab tab = tabLayout.getTabAt(3);
            if (tab != null) tab.select();
            
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() != 3) {
                        finish();
                    }
                }
                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}
                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }
    }

    private void setupGradingItems() {
        // Item 1: Vẽ tranh ngày Trái Đất
        View card1 = findViewById(R.id.card_grading_earth_day);
        if (card1 != null) {
            card1.setOnClickListener(v -> openEntryList("Vẽ tranh ngày Trái Đất"));
        }
        View btn1 = findViewById(R.id.btn_grade_earth_day);
        if (btn1 != null) {
            btn1.setOnClickListener(v -> openEntryList("Vẽ tranh ngày Trái Đất"));
        }

        // Item 2: Vẽ vật thể cái cốc
        View card2 = findViewById(R.id.card_grading_cup);
        if (card2 != null) {
            card2.setOnClickListener(v -> openEntryList("Vẽ vật thể: cái cốc"));
        }
        View btn2 = findViewById(R.id.btn_grade_cup);
        if (btn2 != null) {
            btn2.setOnClickListener(v -> openEntryList("Vẽ vật thể: cái cốc"));
        }
    }

    private void openEntryList(String title) {
        Intent intent = new Intent(this, ChallengeEntryListActivity.class);
        intent.putExtra("CHALLENGE_TITLE", title);
        startActivity(intent);
    }
}
