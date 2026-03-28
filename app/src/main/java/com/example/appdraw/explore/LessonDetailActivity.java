package com.example.appdraw.explore;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.appdraw.HomeworkActivity;
import com.example.appdraw.R;

public class LessonDetailActivity extends AppCompatActivity {

    private int currentStep = 0; // 0: Overview, 1-4: Steps
    private String lessonStatus = "NOT_STARTED";

    private LinearLayout llStepProgress;
    private LinearLayout llStepActions;
    private LinearLayout llMaterialsSection;
    private LinearLayout llMaterialsContainer;
    private Button btnMainAction;
    private TextView tvToolbarTitle;
    private ProgressBar pbLessonProgress;
    private TextView tvStepIndicator;

    private ImageView[] stepChecks = new ImageView[4];
    private TextView[] stepTexts = new TextView[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_detail);

        // Lấy trạng thái từ Intent
        lessonStatus = getIntent().getStringExtra("LESSON_STATUS");
        if (lessonStatus == null) lessonStatus = "NOT_STARTED";
        String lessonTitle = getIntent().getStringExtra("LESSON_TITLE");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        llStepProgress = findViewById(R.id.ll_step_progress);
        llStepActions = findViewById(R.id.ll_step_actions);
        llMaterialsSection = findViewById(R.id.ll_materials_section);
        llMaterialsContainer = findViewById(R.id.ll_materials_container);
        btnMainAction = findViewById(R.id.btn_main_action);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        pbLessonProgress = findViewById(R.id.pb_lesson_progress);
        tvStepIndicator = findViewById(R.id.tv_step_indicator);

        stepChecks[0] = findViewById(R.id.iv_step1_check);
        stepChecks[1] = findViewById(R.id.iv_step2_check);
        stepChecks[2] = findViewById(R.id.iv_step3_check);
        stepChecks[3] = findViewById(R.id.iv_step4_check);

        stepTexts[0] = findViewById(R.id.tv_step1_text);
        stepTexts[1] = findViewById(R.id.tv_step2_text);
        stepTexts[2] = findViewById(R.id.tv_step3_text);
        stepTexts[3] = findViewById(R.id.tv_step4_text);

        if (lessonTitle != null) {
            tvToolbarTitle.setText(lessonTitle);
            ((TextView) findViewById(R.id.tv_lesson_detail_title)).setText(lessonTitle);
        }

        setupMaterials();

        btnMainAction.setOnClickListener(v -> {
            if ("COMPLETED".equals(lessonStatus)) {
                // Nếu đã hoàn thành, nút này có thể dùng để xem lại hoặc làm lại bài tập
                openHomework();
            } else if (currentStep < 4) {
                currentStep++;
                updateStepUI();
            } else {
                showCompletionDialog();
            }
        });
        
        if ("COMPLETED".equals(lessonStatus)) {
            showCompletedState();
        } else {
            showOverview();
        }
    }

    private void setupMaterials() {
        if (llMaterialsContainer == null) return;
        llMaterialsContainer.removeAllViews();
        String[] names = {"Màu nước", "Giấy vẽ", "Cọ vẽ", "Cốc nước"};
        int[] drawables = {R.drawable.mau_nuoc, R.drawable.giay_ve, R.drawable.co_ve, R.drawable.coc_nuoc};
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < names.length; i++) {
            View itemView = inflater.inflate(R.layout.item_lesson_material, llMaterialsContainer, false);
            ((TextView) itemView.findViewById(R.id.tv_material_name)).setText(names[i]);
            ((ImageView) itemView.findViewById(R.id.iv_material)).setImageResource(drawables[i]);
            llMaterialsContainer.addView(itemView);
        }
    }

    private void showOverview() {
        currentStep = 0;
        llStepProgress.setVisibility(View.GONE);
        llStepActions.setVisibility(View.GONE);
        llMaterialsSection.setVisibility(View.VISIBLE);
        btnMainAction.setText("Bắt đầu học");
        btnMainAction.setBackgroundTintList(ColorStateList.valueOf(0xFF4272D0));
    }

    private void showCompletedState() {
        currentStep = 4;
        llStepProgress.setVisibility(View.GONE);
        llStepActions.setVisibility(View.GONE);
        llMaterialsSection.setVisibility(View.VISIBLE);
        
        btnMainAction.setText("Đã hoàn thành");
        btnMainAction.setBackgroundTintList(ColorStateList.valueOf(0xFF2ECC71)); // Màu xanh lá
        
        // Đánh dấu tất cả các bước đã hoàn thành (tích xanh)
        for (int i = 0; i < 4; i++) {
            stepChecks[i].setImageResource(R.drawable.circle_red_live);
            stepChecks[i].setColorFilter(0xFF2ECC71); // Xanh lá
            stepTexts[i].setTextColor(0xFF333333);
            stepTexts[i].setTypeface(null, Typeface.NORMAL);
        }
    }

    private void updateStepUI() {
        llMaterialsSection.setVisibility(View.GONE);
        llStepProgress.setVisibility(View.VISIBLE);
        llStepActions.setVisibility(View.VISIBLE);
        
        tvToolbarTitle.setText("Step " + currentStep + "/4");
        tvStepIndicator.setText("Step " + currentStep + "/4");
        pbLessonProgress.setProgress(currentStep * 25);
        
        if (currentStep == 4) {
            btnMainAction.setText("Hoàn thành bài học");
        } else {
            btnMainAction.setText("Tiếp tục bước " + (currentStep + 1));
        }

        for (int i = 0; i < 4; i++) {
            int stepNum = i + 1;
            if (stepNum < currentStep) {
                stepChecks[i].setImageResource(R.drawable.circle_red_live);
                stepChecks[i].setColorFilter(0xFF2ECC71);
                stepTexts[i].setTextColor(0xFF888888);
            } else if (stepNum == currentStep) {
                stepChecks[i].setImageResource(R.drawable.circle_red_live);
                stepChecks[i].setColorFilter(0xFF4272D0);
                stepTexts[i].setTypeface(null, Typeface.BOLD);
                stepTexts[i].setTextColor(0xFF1A237E);
            } else {
                stepChecks[i].setImageResource(R.drawable.circle_color);
                stepChecks[i].clearColorFilter();
                stepTexts[i].setTypeface(null, Typeface.NORMAL);
                stepTexts[i].setTextColor(0xFF333333);
            }
        }
    }

    private void showCompletionDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_lesson_complete);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.findViewById(R.id.btn_do_homework_now).setOnClickListener(v -> {
            dialog.dismiss();
            openHomework();
        });

        dialog.findViewById(R.id.btn_later).setOnClickListener(v -> {
            dialog.dismiss();
            lessonStatus = "COMPLETED";
            showCompletedState();
        });
        dialog.show();
    }

    private void openHomework() {
        Intent intent = new Intent(this, HomeworkActivity.class);
        startActivity(intent);
        lessonStatus = "COMPLETED";
        showCompletedState();
    }
}
