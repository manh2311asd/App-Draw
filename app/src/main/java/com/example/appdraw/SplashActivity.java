package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appdraw.auth.LoginOptionsActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            android.content.SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            long lastLoginTime = prefs.getLong("last_login_time", 0);
            long THREE_DAYS_MS = 3 * 24 * 60 * 60 * 1000L;
            Intent intent;
            if (System.currentTimeMillis() - lastLoginTime < THREE_DAYS_MS) {
                intent = new Intent(SplashActivity.this, com.example.appdraw.MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginOptionsActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2000);
    }
}
