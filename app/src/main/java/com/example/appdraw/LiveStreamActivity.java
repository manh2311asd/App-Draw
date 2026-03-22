package com.example.appdraw;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class LiveStreamActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_stream);

        ImageView btnBack = findViewById(R.id.btn_back_live);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
}
