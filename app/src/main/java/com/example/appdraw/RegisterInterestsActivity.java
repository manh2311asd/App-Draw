package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class RegisterInterestsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_interests);

        MaterialButton btnNext = findViewById(R.id.btn_interest_next);
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterLevelActivity.class);
            startActivity(intent);
        });
    }
}
