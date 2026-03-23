package com.example.appdraw;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ArtistDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);

        String name = getIntent().getStringExtra("ARTIST_NAME");
        int imageRes = getIntent().getIntExtra("ARTIST_IMAGE", R.drawable.nghe_si_hoang_lam);
        String bio = getIntent().getStringExtra("ARTIST_BIO");

        ImageView ivArtist = findViewById(R.id.iv_artist_large);
        TextView tvName = findViewById(R.id.tv_artist_name_detail);
        TextView tvBio = findViewById(R.id.tv_artist_bio);
        Toolbar toolbar = findViewById(R.id.toolbar);

        if (name != null) tvName.setText(name);
        if (ivArtist != null) ivArtist.setImageResource(imageRes);
        if (bio != null) tvBio.setText(bio);

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }
}
