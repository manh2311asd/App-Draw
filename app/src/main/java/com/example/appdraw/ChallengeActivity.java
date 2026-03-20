package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

        // Click on "Vẽ tranh ngày Trái Đất" to see detail
        View cardChallenge = findViewById(R.id.card_challenge_earth);
        if (cardChallenge != null) {
            cardChallenge.setOnClickListener(v -> {
                Intent intent = new Intent(ChallengeActivity.this, ChallengeDetailActivity.class);
                startActivity(intent);
            });
        }
    }
}
