package com.example.appdraw.drawing;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appdraw.R;

public class DrawingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        ImageView ivBack = findViewById(R.id.btnBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }
    }
}
