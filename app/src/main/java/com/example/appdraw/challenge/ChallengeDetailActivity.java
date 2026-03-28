package com.example.appdraw.challenge;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.appdraw.R;

public class ChallengeDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_detail);

        String title = getIntent().getStringExtra("CHALLENGE_TITLE");
        if (title != null) {
            ((TextView) findViewById(R.id.tv_challenge_detail_title)).setText(title);
        }

        Toolbar toolbar = findViewById(R.id.toolbar_challenge_detail);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }
}
