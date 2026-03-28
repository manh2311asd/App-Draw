package com.example.appdraw.community;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appdraw.R;

public class OtherUserProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        String userName = getIntent().getStringExtra("USER_NAME");
        if (userName != null) {
            TextView tvOtherName = findViewById(R.id.tv_other_name);
            if (tvOtherName != null) {
                tvOtherName.setText(userName);
            }
        }

        ImageView ivBack = findViewById(R.id.iv_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }
    }
}
