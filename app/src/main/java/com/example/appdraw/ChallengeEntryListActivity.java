package com.example.appdraw;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ChallengeEntryListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_entry_list);

        Toolbar toolbar = findViewById(R.id.toolbar_entry_list);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        String title = getIntent().getStringExtra("CHALLENGE_TITLE");
        if (title != null) {
            TextView tvTitle = findViewById(R.id.tv_toolbar_challenge_title);
            if (tvTitle != null) {
                tvTitle.setText(title);
            }
        }
    }
}
