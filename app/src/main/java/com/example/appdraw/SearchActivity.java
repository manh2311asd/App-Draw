package com.example.appdraw;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ImageView ivBack = findViewById(R.id.iv_back_search);
        ivBack.setOnClickListener(v -> finish());
    }
}
