package com.example.appdraw.explore;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appdraw.R;

public class TrendingDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Assuming there might be a layout or we can use a generic one if not found.
        // Let's check for activity_trending_detail.xml first.
        setContentView(R.layout.activity_trending_detail);

        String title = getIntent().getStringExtra("TITLE");
        if (title != null) {
            TextView tvTitle = findViewById(R.id.tv_trending_detail_title);
            if (tvTitle != null) tvTitle.setText(title);
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }
}
