package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class EventRegistrationSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_registration_success);

        findViewById(R.id.ll_back).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.btn_back_to_calendar).setOnClickListener(v -> {
            finish();
        });

        findViewById(R.id.btn_view_ticket).setOnClickListener(v -> {
            // Logic to view ticket
        });
    }
}
