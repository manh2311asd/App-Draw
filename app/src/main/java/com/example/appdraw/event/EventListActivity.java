package com.example.appdraw.event;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appdraw.R;

public class EventListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        ImageView ivBack = findViewById(R.id.iv_back_event_list);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }
    }
}
