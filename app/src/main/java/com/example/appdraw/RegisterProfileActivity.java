package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class RegisterProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_profile);

        MaterialButton btnNext = findViewById(R.id.btn_profile_next);
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterInterestsActivity.class);
            startActivity(intent);
        });
    }
}
