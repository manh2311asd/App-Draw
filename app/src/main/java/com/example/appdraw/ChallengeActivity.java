package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

public class ChallengeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        Toolbar toolbar = findViewById(R.id.toolbar_challenge);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ImageView btnAdd = findViewById(R.id.btn_add_challenge);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> {
                Intent intent = new Intent(this, CreateChallengeActivity.class);
                startActivity(intent);
            });
        }

        TabLayout tabLayout = findViewById(R.id.tab_layout_challenge);
        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 3) { // Chấm điểm
                        Intent intent = new Intent(ChallengeActivity.this, ChallengeGradingActivity.class);
                        startActivity(intent);
                    }
                }
                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}
                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

        setupChallenges();
    }

    private void setupChallenges() {
        // Challenge 1: Vẽ ký họa phố cổ (Chưa tham gia)
        setupChallengeItem(R.id.challenge_1, R.drawable.banner_watercolor, "Vẽ ký họa phố cổ", "Kết thúc trong 3 ngày tới", "258 người đã tham gia", "Tham gia", "NEW");

        // Challenge 2: Nặn đất sét sáng tạo (Đã tham gia)
        setupChallengeItem(R.id.challenge_2, R.drawable.tp_trending_1, "Nặn đất sét sáng tạo", "Kết thúc trong 15 giờ tới", "120 người đã tham gia", "Đã tham gia", "SUBMITTED");

        // Challenge 3: Vẽ cảnh biển cả (Chưa tham gia)
        setupChallengeItem(R.id.challenge_3, R.drawable.ve_thien_nhien, "Vẽ cảnh biển cả", "Kết thúc trong 3 ngày tới", "36 người đã tham gia", "Tham gia", "NEW");

        // Challenge 4: Vẽ tranh ngày Trái Đất (Chưa tham gia)
        setupChallengeItem(R.id.challenge_4, R.drawable.img_challenge_tree, "Vẽ tranh ngày Trái Đất", "Kết thúc trong 3 ngày tới", "1000 người đã tham gia", "Tham gia", "NEW");
    }

    private void setupChallengeItem(int layoutId, int imageRes, String title, String deadline, String participants, String actionText, String status) {
        View view = findViewById(layoutId);
        if (view == null) return;

        ImageView ivThumb = view.findViewById(R.id.iv_challenge_thumb);
        TextView tvTitle = view.findViewById(R.id.tv_challenge_title);
        TextView tvDeadline = view.findViewById(R.id.tv_challenge_deadline);
        TextView tvParticipants = view.findViewById(R.id.tv_challenge_participants);
        MaterialButton btnAction = view.findViewById(R.id.btn_challenge_action);

        if (ivThumb != null) ivThumb.setImageResource(imageRes);
        if (tvTitle != null) tvTitle.setText(title);
        if (tvDeadline != null) tvDeadline.setText(deadline);
        if (tvParticipants != null) tvParticipants.setText(participants);
        if (btnAction != null) {
            btnAction.setText(actionText);
            if ("Đã tham gia".equals(actionText)) {
                btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF5C6BC0));
            }
            btnAction.setOnClickListener(v -> openDetail(status));
        }

        view.setOnClickListener(v -> openDetail(status));
    }

    private void openDetail(String status) {
        Intent intent = new Intent(this, ChallengeDetailActivity.class);
        intent.putExtra("CHALLENGE_STATUS", status);
        startActivity(intent);
    }
}
