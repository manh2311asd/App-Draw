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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.MediaController;
import android.widget.FrameLayout;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;
import com.example.appdraw.HomeworkActivity;
import com.example.appdraw.R;

public class LessonDetailActivity extends AppCompatActivity {

    private int currentStep = 0; // 0: Overview, 1-4: Steps
    private String lessonStatus = "NOT_STARTED";
    private FirebaseFirestore db;
    private String uid;
    private String lessonTitle;
    private String lessonCategory;
    private String lessonAuthor;

    private LinearLayout llStepProgress;
    private LinearLayout llStepActions;
    private LinearLayout llMaterialsSection;
    private LinearLayout llMaterialsContainer;
    private Button btnMainAction;
    private TextView tvToolbarTitle;
    private ProgressBar pbLessonProgress;
    private TextView tvStepIndicator;

    private java.util.List<ImageView> stepChecks = new java.util.ArrayList<>();
    private java.util.List<TextView> stepTexts = new java.util.ArrayList<>();

    private VideoView videoView;
    private FrameLayout flVideoThumbnail;
    private ImageView ivPlayButton;
    private ProgressBar pbVideoLoading;
    private MediaController mediaController;

    private com.example.appdraw.model.Lesson currentLesson;
    private java.util.List<String> dynamicVideoUrls = new java.util.ArrayList<>();

    // Notes variables
    private java.util.List<com.example.appdraw.model.Note> noteList = new java.util.ArrayList<>();
    private NoteAdapter noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_detail);

        lessonTitle = getIntent().getStringExtra("LESSON_TITLE");
        if (lessonTitle == null)
            lessonTitle = "Unknown Lesson";

        lessonCategory = getIntent().getStringExtra("CATEGORY");

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            uid = "guest";
        }

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

        // Static steps removed, handled dynamically in renderLessonData

        if (lessonTitle != null) {
            tvToolbarTitle.setText(lessonTitle);
            ((TextView) findViewById(R.id.tv_lesson_detail_title)).setText(lessonTitle);
        }

        ImageView ivSave = findViewById(R.id.iv_save_lesson);
        if (ivSave != null) {
            ivSave.setOnClickListener(v -> toggleSaveLesson());
        }

        findViewById(R.id.btn_checklist).setOnClickListener(v -> showChecklistDialog());

        videoView = findViewById(R.id.video_view);
        flVideoThumbnail = findViewById(R.id.fl_video_thumbnail);
        ivPlayButton = findViewById(R.id.iv_play_button);
        ivPlayButton.setVisibility(View.GONE);
        pbVideoLoading = findViewById(R.id.pb_video_loading);

        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        ivPlayButton.setOnClickListener(v -> {
            if (currentStep == 0)
                currentStep = 1;
            handleStepClick(currentStep); // Start current step
        });

        // Dynamic steps will be assigned listeners in renderLessonData

        View btnDownload = findViewById(R.id.btn_download_materials);
        if (btnDownload != null) {
            btnDownload.setOnClickListener(v -> downloadMaterials());
        }

        fetchLessonData();
        checkSavedState();

        btnMainAction.setOnClickListener(v -> {
            if ("COMPLETED".equals(lessonStatus)) {
                openHomework();
            } else if ("WAITING_FOR_HOMEWORK".equals(lessonStatus)) {
                openHomework();
            } else if (currentStep < stepChecks.size()) {
                currentStep++;
                lessonStatus = "IN_PROGRESS";
                saveProgressToFirestore();
                updateStepUI();
                playStepVideo(currentStep - 1);
            } else {
                showCompletionDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (db != null) {
            fetchProgressFromFirestore();
        }
    }

    private boolean isLessonSaved = false;

    private void checkSavedState() {
        if ("guest".equals(uid))
            return;
        db.collection("Users").document(uid).collection("savedLessons").document(lessonTitle)
                .get().addOnSuccessListener(doc -> {
                    isLessonSaved = doc.exists();
                    updateSaveIcon();
                });
    }

    private void toggleSaveLesson() {
        if ("guest".equals(uid)) {
            android.widget.Toast.makeText(this, "Vui lòng đăng nhập để lưu", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        ImageView ivSave = findViewById(R.id.iv_save_lesson);
        if (ivSave != null) {
            ivSave.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150)
                    .withEndAction(() -> ivSave.animate().scaleX(1f).scaleY(1f).setDuration(150)).start();
        }

        if (isLessonSaved) {
            isLessonSaved = false;
            updateSaveIcon();
            db.collection("Users").document(uid).collection("savedLessons").document(lessonTitle).delete();
            android.widget.Toast.makeText(this, "Đã bỏ lưu bài học", android.widget.Toast.LENGTH_SHORT).show();
        } else {
            isLessonSaved = true;
            updateSaveIcon();
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("title", lessonTitle);
            data.put("category", lessonCategory);
            data.put("author", lessonAuthor);
            data.put("imageRes", getIntent().getStringExtra("IMAGE_RES"));
            data.put("savedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
            db.collection("Users").document(uid).collection("savedLessons").document(lessonTitle).set(data);
            android.widget.Toast.makeText(this, "Đã lưu vào bộ sưu tập", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSaveIcon() {
        ImageView ivSave = findViewById(R.id.iv_save_lesson);
        if (ivSave == null)
            return;
        if (isLessonSaved) {
            ivSave.setColorFilter(android.graphics.Color.parseColor("#FFC107")); // Gold Star
        } else {
            ivSave.setColorFilter(android.graphics.Color.parseColor("#B0BEC5")); // Gray Outline
        }
    }

    private void fetchProgressFromFirestore() {
        if ("guest".equals(uid)) {
            applyLessonStatus();
            return;
        }

        db.collection("Users").document(uid).collection("lessonProgress").document(lessonTitle)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        lessonStatus = documentSnapshot.getString("status");
                        if (lessonStatus == null)
                            lessonStatus = "NOT_STARTED";

                        Long stepObj = documentSnapshot.getLong("currentStep");
                        if (stepObj != null && !"COMPLETED".equals(lessonStatus)
                                && !"WAITING_FOR_HOMEWORK".equals(lessonStatus)) {
                            currentStep = stepObj.intValue();
                        }
                    } else {
                        lessonStatus = "NOT_STARTED";
                    }
                    applyLessonStatus();
                })
                .addOnFailureListener(e -> {
                    lessonStatus = "NOT_STARTED";
                    applyLessonStatus();
                });
    }

    private void saveProgressToFirestore() {
        if ("guest".equals(uid))
            return;
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("status", lessonStatus);
        data.put("currentStep", currentStep);
        data.put("lastUpdated", System.currentTimeMillis());

        db.collection("Users").document(uid).collection("lessonProgress").document(lessonTitle)
                .set(data);
    }

    private void applyLessonStatus() {
        if ("COMPLETED".equals(lessonStatus)) {
            showCompletedState();
        } else if ("WAITING_FOR_HOMEWORK".equals(lessonStatus)) {
            showWaitingForHomeworkState();
        } else if (currentStep > 0 && currentStep <= 4) {
            updateStepUI();
        } else {
            showOverview();
        }
    }

    private java.util.Set<String> checkedMaterials = new java.util.HashSet<>();

    private void setupMaterials() {
        if ("guest".equals(uid)) {
            setupMaterialsDynamic();
            return;
        }
        db.collection("Users").document(uid).collection("lessonChecklists").document(lessonTitle)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        java.util.List<String> list = (java.util.List<String>) documentSnapshot.get("checkedItems");
                        if (list != null) {
                            checkedMaterials.clear();
                            checkedMaterials.addAll(list);
                        }
                    }
                    setupMaterialsDynamic();
                })
                .addOnFailureListener(e -> setupMaterialsDynamic());
    }

    private void setupMaterialsUI() {
        if (llMaterialsContainer == null)
            return;
        llMaterialsContainer.removeAllViews();

        String[] names;
        int[] drawables;
        if (lessonCategory != null && (lessonCategory.contains("bắt đầu") || lessonCategory.contains("sơ cấp"))) {
            names = new String[] { "Giấy A4 trơn", "Bút chì HB", "Gôm tẩy" };
            drawables = new int[] { R.drawable.giay_ve, R.drawable.co_ve, R.drawable.coc_nuoc };
        } else if (lessonCategory != null && lessonCategory.contains("Chibi")) {
            names = new String[] { "Bút chì kim", "Bút Line", "Màu Marker", "Giấy trơn" };
            drawables = new int[] { R.drawable.co_ve, R.drawable.co_ve, R.drawable.mau_nuoc, R.drawable.giay_ve };
        } else {
            names = new String[] { "Màu nước", "Giấy vẽ", "Cọ lông mềm", "Băng dính" };
            drawables = new int[] { R.drawable.mau_nuoc, R.drawable.giay_ve, R.drawable.co_ve, R.drawable.coc_nuoc };
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < names.length; i++) {
            final String materialName = names[i];
            View itemView = inflater.inflate(R.layout.item_lesson_material, llMaterialsContainer, false);
            ((TextView) itemView.findViewById(R.id.tv_material_name)).setText(materialName);
            ((ImageView) itemView.findViewById(R.id.iv_material)).setImageResource(drawables[i]);

            com.google.android.material.card.MaterialCardView cv = itemView.findViewById(R.id.cv_material_card);
            View overlay = itemView.findViewById(R.id.view_overlay);
            ImageView ivCheck = itemView.findViewById(R.id.iv_check);

            boolean isChecked = checkedMaterials.contains(materialName);
            updateMaterialCheckUI(cv, overlay, ivCheck, isChecked);

            itemView.setOnClickListener(v -> {
                if ("guest".equals(uid)) {
                    Toast.makeText(this, "Vui lòng đăng nhập để lưu dụng cụ", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean newChecked = !checkedMaterials.contains(materialName);
                if (newChecked) {
                    checkedMaterials.add(materialName);
                } else {
                    checkedMaterials.remove(materialName);
                }
                updateMaterialCheckUI(cv, overlay, ivCheck, newChecked);

                db.collection("Users").document(uid).collection("lessonChecklists").document(lessonTitle)
                        .set(java.util.Collections.singletonMap("checkedItems",
                                new java.util.ArrayList<>(checkedMaterials)));
            });

            llMaterialsContainer.addView(itemView);
        }
    }

    private void updateMaterialCheckUI(com.google.android.material.card.MaterialCardView cv, View overlay,
            ImageView ivCheck, boolean isChecked) {
        if (isChecked) {
            cv.setStrokeWidth(4);
            overlay.setVisibility(View.VISIBLE);
            ivCheck.setVisibility(View.VISIBLE);
        } else {
            cv.setStrokeWidth(0);
            overlay.setVisibility(View.GONE);
            ivCheck.setVisibility(View.GONE);
        }
    }

    private void handleStepClick(int step) {
        currentStep = step;
        updateStepUI();
        playStepVideo(step - 1);
    }

    private void playStepVideo(int index) {
        if (index < 0 || index >= dynamicVideoUrls.size())
            return;

        videoView.stopPlayback();
        videoView.setVisibility(View.GONE);
        flVideoThumbnail.setVisibility(View.VISIBLE);
        ivPlayButton.setVisibility(View.GONE);
        pbVideoLoading.setVisibility(View.VISIBLE);

        Uri videoUri = Uri.parse(dynamicVideoUrls.get(index));
        videoView.setVideoURI(videoUri);

        videoView.setOnPreparedListener(mp -> {
            pbVideoLoading.setVisibility(View.GONE);
            flVideoThumbnail.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            videoView.start();
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            pbVideoLoading.setVisibility(View.GONE);
            ivPlayButton.setVisibility(View.VISIBLE);
            android.widget.Toast.makeText(LessonDetailActivity.this, "Lỗi tải video", android.widget.Toast.LENGTH_SHORT)
                    .show();
            return true;
        });
    }

    private void showOverview() {
        currentStep = 0;
        llStepProgress.setVisibility(View.GONE);
        llStepActions.setVisibility(View.GONE);
        llMaterialsSection.setVisibility(View.VISIBLE);
        btnMainAction.setText("Bắt đầu học");
        btnMainAction.setBackgroundTintList(ColorStateList.valueOf(0xFF4272D0));
    }

    private void showWaitingForHomeworkState() {
        currentStep = 4;
        llStepProgress.setVisibility(View.GONE);
        llStepActions.setVisibility(View.GONE);
        llMaterialsSection.setVisibility(View.VISIBLE);

        btnMainAction.setText("Làm bài tập ngay");
        btnMainAction.setBackgroundTintList(ColorStateList.valueOf(0xFFFF9800)); // Màu cam

        for (int i = 0; i < stepChecks.size(); i++) {
            stepChecks.get(i).setImageResource(R.drawable.circle_red_live);
            stepChecks.get(i).setColorFilter(0xFF2ECC71); // Xanh lá
            stepTexts.get(i).setTextColor(0xFF333333);
            stepTexts.get(i).setTypeface(null, Typeface.NORMAL);
        }
    }

    private void showCompletedState() {
        currentStep = 4;
        llStepProgress.setVisibility(View.GONE);
        llStepActions.setVisibility(View.GONE);
        llMaterialsSection.setVisibility(View.VISIBLE);

        btnMainAction.setText("Xem lại bài nộp");
        btnMainAction.setBackgroundTintList(ColorStateList.valueOf(0xFF2ECC71)); // Màu xanh lá

        // Đánh dấu tất cả các bước đã hoàn thành (tích xanh)
        for (int i = 0; i < stepChecks.size(); i++) {
            stepChecks.get(i).setImageResource(R.drawable.circle_red_live);
            stepChecks.get(i).setColorFilter(0xFF2ECC71); // Xanh lá
            stepTexts.get(i).setTextColor(0xFF333333);
            stepTexts.get(i).setTypeface(null, Typeface.NORMAL);
        }
    }

    private void updateStepUI() {
        llMaterialsSection.setVisibility(View.GONE);
        llStepProgress.setVisibility(View.VISIBLE);
        llStepActions.setVisibility(View.VISIBLE);

        int totalSteps = stepChecks.size() > 0 ? stepChecks.size() : 4;
        tvToolbarTitle.setText("Step " + currentStep + "/" + totalSteps);
        tvStepIndicator.setText("Step " + currentStep + "/" + totalSteps);
        pbLessonProgress.setProgress((int) (((float) currentStep / totalSteps) * 100));

        if (currentStep >= totalSteps) {
            btnMainAction.setText("Hoàn thành bài học");
        } else {
            btnMainAction.setText("Tiếp tục bước " + (currentStep + 1));
        }

        for (int i = 0; i < stepChecks.size(); i++) {
            int stepNum = i + 1;
            if (stepNum < currentStep) {
                stepChecks.get(i).setImageResource(R.drawable.circle_red_live);
                stepChecks.get(i).setColorFilter(0xFF2ECC71);
                stepTexts.get(i).setTextColor(0xFF888888);
            } else if (stepNum == currentStep) {
                stepChecks.get(i).setImageResource(R.drawable.circle_red_live);
                stepChecks.get(i).setColorFilter(0xFF4272D0);
                stepTexts.get(i).setTypeface(null, Typeface.BOLD);
                stepTexts.get(i).setTextColor(0xFF1A237E);
            } else {
                stepChecks.get(i).setImageResource(R.drawable.ic_step_pending);
                stepChecks.get(i).clearColorFilter();
                stepTexts.get(i).setTypeface(null, Typeface.NORMAL);
                stepTexts.get(i).setTextColor(0xFF192A56);
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
            lessonStatus = "WAITING_FOR_HOMEWORK";
            saveProgressToFirestore();
            openHomework();
        });

        dialog.findViewById(R.id.btn_later).setOnClickListener(v -> {
            dialog.dismiss();
            lessonStatus = "WAITING_FOR_HOMEWORK";
            saveProgressToFirestore();
            applyLessonStatus();
        });
        dialog.show();
    }

    private void openHomework() {
        Intent intent = new Intent(this, HomeworkActivity.class);
        intent.putExtra("LESSON_TITLE", lessonTitle);
        startActivity(intent);
    }

    private void fetchLessonData() {
        String lessonId = getIntent().getStringExtra("LESSON_ID");
        if (lessonId == null)
            lessonId = lessonTitle;

        db.collection("Lessons").document(lessonId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentLesson = documentSnapshot.toObject(com.example.appdraw.model.Lesson.class);
                        if (currentLesson != null) {
                            if (currentLesson.getSteps() == null || currentLesson.getSteps().isEmpty() ||
                                    (currentLesson.getDescription() != null && currentLesson.getDescription().equals(
                                            "Hướng dẫn chi tiết cách hoàn thành tác phẩm tuyệt đẹp dành cho người mới bắt đầu."))) {
                                createDummyLessonAndRender();
                            } else {
                                renderLessonData();
                            }
                        }
                    } else {
                        createDummyLessonAndRender();
                    }
                })
                .addOnFailureListener(e -> android.widget.Toast
                        .makeText(this, "Lỗi tải bài học", android.widget.Toast.LENGTH_SHORT).show());
    }

    private void createDummyLessonAndRender() {
        String category = getIntent().getStringExtra("CATEGORY");
        if (category == null)
            category = "Beginner";

        currentLesson = new com.example.appdraw.model.Lesson();
        currentLesson.setId(lessonTitle);
        currentLesson.setTitle(lessonTitle);
        currentLesson.setRating(4.5f);
        currentLesson
                .setDescription("Hướng dẫn chi tiết cách hoàn thành tác phẩm tuyệt đẹp dành cho người mới bắt đầu.");

        String intentAuthor = getIntent().getStringExtra("AUTHOR");

        java.util.List<com.example.appdraw.model.Lesson.Step> steps = new java.util.ArrayList<>();
        java.util.List<String> materials = new java.util.ArrayList<>();

        category = category.toLowerCase();
        if (category.contains("người mới bắt đầu") || category.contains("beginner")) {
            currentLesson.setAuthor(intentAuthor != null ? intentAuthor.replace("Bởi ", "") : "Phong Artist");
            currentLesson.setLevel("Beginner");
            currentLesson.setDurationMin(20);
            materials.addAll(java.util.Arrays.asList("Bút chì HB", "Giấy A4 trơn", "Gôm tẩy"));
            steps.add(new com.example.appdraw.model.Lesson.Step("Bước 1", "Làm quen hình khối",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"));
            steps.add(new com.example.appdraw.model.Lesson.Step("Bước 2", "Luyện nét & đánh bóng",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"));
            steps.add(new com.example.appdraw.model.Lesson.Step("Bước 3", "Thực hành ngắn",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"));
        } else if (category.contains("thiên nhiên") || category.contains("màu nước")) {
            currentLesson.setAuthor(intentAuthor != null ? intentAuthor.replace("Bởi ", "") : "Tuấn Vũ Watercolor");
            currentLesson.setLevel("Intermediate");
            currentLesson.setDurationMin(45);
            materials.addAll(java.util.Arrays.asList("Màu nước", "Giấy 300gsm", "Cọ dạng tròn", "Băng dính dán"));
            steps.add(new com.example.appdraw.model.Lesson.Step("Bước 1", "Phác họa mảng khung cảnh",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"));
            steps.add(new com.example.appdraw.model.Lesson.Step("Bước 2", "Loang màu nền (Wet on wet)",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"));
            steps.add(new com.example.appdraw.model.Lesson.Step("Bước 3", "Chi tiết tiền cảnh",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"));
            steps.add(new com.example.appdraw.model.Lesson.Step("Bước 4", "Nhấn cường độ sáng tối",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"));
        } else {
            currentLesson.setAuthor(intentAuthor != null ? intentAuthor.replace("Bởi ", "") : "Hương Lan Manga");
            currentLesson.setLevel("Advanced");
            currentLesson.setDurationMin(60);
            materials.addAll(java.util.Arrays.asList("Bút chì kim", "Bút Line", "Màu Marker", "Giấy trơn"));
            steps.add(new com.example.appdraw.model.Lesson.Step("Bước 1", "Phác khung tỷ lệ cơ thể",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"));
            steps.add(new com.example.appdraw.model.Lesson.Step("Bước 2", "Dựng khuôn mặt & hướng mắt",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"));
            steps.add(new com.example.appdraw.model.Lesson.Step("Bước 3", "Vẽ tóc & trang phục",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"));
            steps.add(new com.example.appdraw.model.Lesson.Step("Bước 4", "Đi viền nét đen (Line art)",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"));
            steps.add(new com.example.appdraw.model.Lesson.Step("Bước 5", "Lên màu và đổ bóng hoàn thiện",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"));
        }

        currentLesson.setMaterials(materials);
        currentLesson.setSteps(steps);

        db.collection("Lessons").document(lessonTitle).set(currentLesson);
        renderLessonData();
    }

    private void renderLessonData() {
        if (currentLesson == null)
            return;

        tvToolbarTitle.setText(currentLesson.getTitle());
        ((TextView) findViewById(R.id.tv_lesson_detail_title)).setText(currentLesson.getTitle());
        TextView tvDesc = findViewById(R.id.tv_lesson_description);
        if (tvDesc != null)
            tvDesc.setText(currentLesson.getDescription());

        TextView tvAuthor = findViewById(R.id.tv_lesson_author);
        if (tvAuthor != null && currentLesson.getAuthor() != null)
            tvAuthor.setText(currentLesson.getAuthor());

        TextView tvLevel = findViewById(R.id.tv_lesson_level);
        if (tvLevel != null && currentLesson.getLevel() != null)
            tvLevel.setText(currentLesson.getLevel());

        TextView tvDuration = findViewById(R.id.tv_lesson_duration);
        if (tvDuration != null)
            tvDuration.setText(currentLesson.getDurationMin() + " min");

        String imgResStr = getIntent().getStringExtra("IMAGE_RES");
        if (imgResStr != null && !imgResStr.isEmpty()) {
            ImageView ivThumb = findViewById(R.id.iv_lesson_thumbnail);
            if (ivThumb != null) {
                int resId = getResources().getIdentifier(imgResStr, "drawable", getPackageName());
                if (resId != 0)
                    ivThumb.setImageResource(resId);
            }
        }

        LinearLayout llStepsContainer = findViewById(R.id.ll_dynamic_steps_container);
        if (llStepsContainer != null)
            llStepsContainer.removeAllViews();
        stepChecks.clear();
        stepTexts.clear();
        dynamicVideoUrls.clear();

        if (currentLesson.getSteps() != null && llStepsContainer != null) {
            LayoutInflater inflater = LayoutInflater.from(this);
            for (int i = 0; i < currentLesson.getSteps().size(); i++) {
                com.example.appdraw.model.Lesson.Step step = currentLesson.getSteps().get(i);
                dynamicVideoUrls.add(step.getVideoUrl());

                View stepView = inflater.inflate(R.layout.item_lesson_step, llStepsContainer, false);
                TextView tvNum = stepView.findViewById(R.id.tv_step_number);
                TextView tvText = stepView.findViewById(R.id.tv_step_text);
                ImageView ivCheck = stepView.findViewById(R.id.iv_step_check);

                tvNum.setText(step.getTitle() != null ? step.getTitle() : "Bước " + (i + 1));
                tvText.setText(step.getDescription());

                stepChecks.add(ivCheck);
                stepTexts.add(tvText);

                final int finalI = i;
                stepView.setOnClickListener(v -> {
                    if ("COMPLETED".equals(lessonStatus) || "WAITING_FOR_HOMEWORK".equals(lessonStatus)) {
                        android.widget.Toast.makeText(LessonDetailActivity.this,
                                "Bạn không thể xem lại khi bài học đã kết thúc", android.widget.Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    handleStepClick(finalI + 1);
                });

                llStepsContainer.addView(stepView);
            }
        }
        setupMaterials();
    }

    private int getMaterialImageResource(String name) {
        if (name == null)
            return R.drawable.mau_nuoc;
        String lower = name.toLowerCase().trim();
        if (lower.contains("bút chì hb"))
            return R.drawable.but_chi_hb;
        if (lower.contains("giấy a4 trơn"))
            return R.drawable.giay_a4_tron;
        if (lower.contains("gôm tẩy") || lower.contains("tẩy"))
            return R.drawable.gom_tay;
        if (lower.contains("bút chì kim"))
            return R.drawable.but_chi_kim;
        if (lower.contains("bút line"))
            return R.drawable.but_line;
        if (lower.contains("màu marker") || lower.contains("màu market"))
            return R.drawable.mau_market;
        if (lower.contains("giấy trơn"))
            return R.drawable.giay_tron;
        if (lower.contains("giấy 300gsm") || lower.contains("300gsm"))
            return R.drawable.giay_300gsm;
        if (lower.contains("cọ dạng tròn") || lower.contains("tròn"))
            return R.drawable.co_dang_tron;
        if (lower.contains("băng dính"))
            return R.drawable.bang_dinh;

        // Fallbacks for older categories
        if (lower.contains("cọ") || lower.contains("lông mềm"))
            return R.drawable.co_ve;
        if (lower.contains("giấy"))
            return R.drawable.giay_ve;

        return R.drawable.mau_nuoc;
    }

    private void setupMaterialsDynamic() {
        if (llMaterialsContainer == null || currentLesson == null)
            return;
        llMaterialsContainer.removeAllViews();
        java.util.List<String> materials = currentLesson.getMaterials();
        if (materials == null)
            return;

        LayoutInflater inflater = LayoutInflater.from(this);
        for (String materialName : materials) {
            View itemView = inflater.inflate(R.layout.item_lesson_material, llMaterialsContainer, false);
            ((TextView) itemView.findViewById(R.id.tv_material_name)).setText(materialName);
            ((ImageView) itemView.findViewById(R.id.iv_material))
                    .setImageResource(getMaterialImageResource(materialName));

            com.google.android.material.card.MaterialCardView cv = itemView.findViewById(R.id.cv_material_card);
            View overlay = itemView.findViewById(R.id.view_overlay);
            ImageView ivCheck = itemView.findViewById(R.id.iv_check);

            boolean isChecked = checkedMaterials.contains(materialName);
            updateMaterialCheckUI(cv, overlay, ivCheck, isChecked);

            itemView.setOnClickListener(v -> {
                if ("guest".equals(uid)) {
                    android.widget.Toast
                            .makeText(this, "Vui lòng đăng nhập để lưu dụng cụ", android.widget.Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                boolean newChecked = !checkedMaterials.contains(materialName);
                if (newChecked) {
                    checkedMaterials.add(materialName);
                } else {
                    checkedMaterials.remove(materialName);
                }
                updateMaterialCheckUI(cv, overlay, ivCheck, newChecked);

                db.collection("Users").document(uid).collection("lessonChecklists").document(lessonTitle)
                        .set(java.util.Collections.singletonMap("checkedItems",
                                new java.util.ArrayList<>(checkedMaterials)));
            });

            llMaterialsContainer.addView(itemView);
        }
    }

    private void downloadMaterials() {
        android.widget.Toast.makeText(this, "Đang chuẩn bị tải ảnh bìa...", android.widget.Toast.LENGTH_SHORT).show();

        String imageResStr = getIntent().getStringExtra("IMAGE_RES");
        if (imageResStr == null || imageResStr.isEmpty()) {
            imageResStr = "banner_watercolor";
        }

        int resId = getResources().getIdentifier(imageResStr, "drawable", getPackageName());
        if (resId == 0) {
            android.widget.Toast.makeText(this, "Không tìm thấy ảnh", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeResource(getResources(), resId);
        if (bitmap == null)
            return;

        try {
            String savedImageURL = android.provider.MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    bitmap,
                    lessonTitle + "_cover",
                    "Ảnh bìa bài học");

            if (savedImageURL != null) {
                android.widget.Toast
                        .makeText(this, "Đã tải xuống thành công mục Album Ảnh", android.widget.Toast.LENGTH_LONG)
                        .show();
            } else {
                android.widget.Toast.makeText(this, "Lỗi tải. Hãy kiểm tra Quyền lưu trữ trong Cài đặt",
                        android.widget.Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Lỗi khi lưu ảnh: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void showChecklistDialog() {
        if (llMaterialsSection != null) {
            if (llMaterialsSection.getVisibility() == View.GONE) {
                llMaterialsSection.setVisibility(View.VISIBLE);
                View parent = (View) llMaterialsSection.getParent();
                if (parent != null && parent.getParent() instanceof androidx.core.widget.NestedScrollView) {
                    ((androidx.core.widget.NestedScrollView) parent.getParent()).smoothScrollTo(0,
                            llMaterialsSection.getTop() - 100);
                }
            } else {
                llMaterialsSection.setVisibility(View.GONE);
            }
        }
    }
}
