package com.example.appdraw.explore;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.appdraw.R;

public class ArtistDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);

        String name = getIntent().getStringExtra("ARTIST_NAME");
        int imageRes = getIntent().getIntExtra("ARTIST_IMAGE", 0);
        String bio = getIntent().getStringExtra("ARTIST_BIO");

        if (name != null) ((TextView) findViewById(R.id.tv_artist_name_detail)).setText(name);
        if (imageRes != 0) ((ImageView) findViewById(R.id.iv_artist_large)).setImageResource(imageRes);
        if (bio != null) ((TextView) findViewById(R.id.tv_artist_bio)).setText(bio);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }
}
