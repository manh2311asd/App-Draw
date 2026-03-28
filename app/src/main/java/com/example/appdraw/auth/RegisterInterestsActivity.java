package com.example.appdraw.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appdraw.R;

public class RegisterInterestsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_interests);

        findViewById(R.id.btn_interest_next).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterLevelActivity.class));
        });
    }
}
