package com.example.appdraw;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

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

        TextView tvName = findViewById(R.id.tv_other_name);
        String name = getIntent().getStringExtra("USER_NAME");
        if (tvName != null && name != null) {
            tvName.setText(name);
        }

        TextView tvFollow = findViewById(R.id.tv_follow_status);
        if (tvFollow != null) {
            tvFollow.setOnClickListener(v -> {
                if (!isFollowing) {
                    isFollowing = true;
                    tvFollow.setText("Đang theo dõi");
                    tvFollow.setBackgroundResource(R.drawable.rounded_bg_white_border);
                    tvFollow.setTextColor(getResources().getColor(R.color.text_gray));
                } else {
                    isFollowing = false;
                    tvFollow.setText("+ Theo dõi");
                    tvFollow.setBackgroundResource(R.drawable.bg_chip_selected);
                    tvFollow.setTextColor(getResources().getColor(android.R.color.white));
                }
            });
        }
    }
}
