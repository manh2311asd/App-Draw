package com.example.appdraw;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class DoingProjectDetailActivity extends AppCompatActivity {

    private LinearProgressIndicator progressBar;
    private TextView tvPercent;
    private CheckBox[] checkBoxes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doing_project_detail);

        String projectName = getIntent().getStringExtra("PROJECT_NAME");
        
        TextView tvTitle = findViewById(R.id.tv_doing_title);
        if (projectName != null) {
            tvTitle.setText(projectName);
        }

        progressBar = findViewById(R.id.pb_checklist_progress);
        tvPercent = findViewById(R.id.tv_checklist_percent);

        checkBoxes = new CheckBox[]{
                findViewById(R.id.cb_step1),
                findViewById(R.id.cb_step2),
                findViewById(R.id.cb_step3),
                findViewById(R.id.cb_step4),
                findViewById(R.id.cb_step5)
        };

        for (CheckBox cb : checkBoxes) {
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> updateProgress());
        }

        findViewById(R.id.btn_back_doing).setOnClickListener(v -> onBackPressed());
        
        findViewById(R.id.btn_continue_drawing).setOnClickListener(v -> {
            // Logic to continue drawing
            onBackPressed();
        });

        updateProgress(); // Initial calculation
    }

    private void updateProgress() {
        int checkedCount = 0;
        for (CheckBox cb : checkBoxes) {
            if (cb.isChecked()) checkedCount++;
        }
        int percent = (checkedCount * 100) / checkBoxes.length;
        progressBar.setProgress(percent);
        tvPercent.setText(percent + "%");
    }
}
