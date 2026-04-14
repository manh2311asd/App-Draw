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
import com.example.appdraw.HomeworkActivity;
import com.example.appdraw.R;

public class LessonDetailActivity extends AppCompatActivity {

    private int currentStep = 0; // 0: Overview, 1-4: Steps
    private String lessonStatus = "NOT_STARTED";
    private FirebaseFirestore db;
    private String uid;
    private String lessonTitle;

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
        if (lessonTitle == null) lessonTitle = "Unknown Lesson";

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

        findViewById(R.id.btn_checklist).setOnClickListener(v -> showChecklistDialog());
        findViewById(R.id.btn_notes).setOnClickListener(v -> showNotesBottomSheet());

        videoView = findViewById(R.id.video_view);
        flVideoThumbnail = findViewById(R.id.fl_video_thumbnail);
        ivPlayButton = findViewById(R.id.iv_play_button);
        pbVideoLoading = findViewById(R.id.pb_video_loading);

        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        ivPlayButton.setOnClickListener(v -> {
            if (currentStep == 0) currentStep = 1;
            handleStepClick(currentStep); // Start current step
        });

        findViewById(R.id.ll_step1).setOnClickListener(v -> handleStepClick(1));
        findViewById(R.id.ll_step2).setOnClickListener(v -> handleStepClick(2));
        findViewById(R.id.ll_step3).setOnClickListener(v -> handleStepClick(3));
        findViewById(R.id.ll_step4).setOnClickListener(v -> handleStepClick(4));

        View btnDownload = findViewById(R.id.btn_download_materials);
        if (btnDownload != null) {
            btnDownload.setOnClickListener(v -> downloadMaterials());
        }

        fetchLessonData();

        btnMainAction.setOnClickListener(v -> {
            if ("COMPLETED".equals(lessonStatus)) {
                openHomework();
            } else if ("WAITING_FOR_HOMEWORK".equals(lessonStatus)) {
                openHomework();
            } else if (currentStep < 4) {
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
                        if (lessonStatus == null) lessonStatus = "NOT_STARTED";

                        Long stepObj = documentSnapshot.getLong("currentStep");
                        if (stepObj != null && !"COMPLETED".equals(lessonStatus) && !"WAITING_FOR_HOMEWORK".equals(lessonStatus)) {
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
        if ("guest".equals(uid)) return;
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

    private void handleStepClick(int step) {
        currentStep = step;
        updateStepUI();
        playStepVideo(step - 1);
    }

    private void playStepVideo(int index) {
        if (index < 0 || index >= dynamicVideoUrls.size()) return;
        
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
            android.widget.Toast.makeText(LessonDetailActivity.this, "Lỗi tải video", android.widget.Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void showChecklistDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Checklist Dụng Cụ");
        String[] items = {"Màu nước nước/tuýp", "Giấy vẽ 300gsm", "Cọ vẽ nét tròn", "Cốc nước & Khăn lau thấm"};
        boolean[] checkedItems = {false, false, false, false};
        builder.setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
            checkedItems[which] = isChecked;
        });
        builder.setPositiveButton("Xong", (dialog, which) -> dialog.dismiss());
        builder.show();
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

        for (int i = 0; i < 4; i++) {
            stepChecks[i].setImageResource(R.drawable.circle_red_live);
            stepChecks[i].setColorFilter(0xFF2ECC71); // Xanh lá
            stepTexts[i].setTextColor(0xFF333333);
            stepTexts[i].setTypeface(null, Typeface.NORMAL);
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
                stepChecks[i].setImageResource(R.drawable.ic_step_pending);
                stepChecks[i].clearColorFilter();
                stepTexts[i].setTypeface(null, Typeface.NORMAL);
                stepTexts[i].setTextColor(0xFF192A56);
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
        db.collection("Lessons").document(lessonTitle)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentLesson = documentSnapshot.toObject(com.example.appdraw.model.Lesson.class);
                        if (currentLesson != null) {
                            renderLessonData();
                        }
                    } else {
                        createDummyLessonAndRender();
                    }
                })
                .addOnFailureListener(e -> android.widget.Toast.makeText(this, "Lỗi tải bài học", android.widget.Toast.LENGTH_SHORT).show());
    }

    private void createDummyLessonAndRender() {
        currentLesson = new com.example.appdraw.model.Lesson();
        currentLesson.setId(lessonTitle);
        currentLesson.setTitle(lessonTitle);
        currentLesson.setAuthor("Phong Artist");
        currentLesson.setLevel("Beginner");
        currentLesson.setDurationMin(25);
        currentLesson.setRating(4.5f);
        currentLesson.setDescription("Hướng dẫn chi tiết cách hoàn thành tác phẩm tuyệt đẹp dành cho người mới bắt đầu.");
        currentLesson.setMaterials(java.util.Arrays.asList("Màu nước", "Giấy vẽ 300gsm", "Cọ vẽ", "Cốc nước"));
        
        java.util.List<com.example.appdraw.model.Lesson.Step> steps = new java.util.ArrayList<>();
        steps.add(new com.example.appdraw.model.Lesson.Step("Bước 1", "Chuẩn bị dụng cụ", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"));
        steps.add(new com.example.appdraw.model.Lesson.Step("Bước 2", "Phác thảo cơ bản", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"));
        steps.add(new com.example.appdraw.model.Lesson.Step("Bước 3", "Tô màu nền", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"));
        steps.add(new com.example.appdraw.model.Lesson.Step("Bước 4", "Hoàn thiện chi tiết", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"));
        currentLesson.setSteps(steps);

        db.collection("Lessons").document(lessonTitle).set(currentLesson);
        renderLessonData();
    }

    private void renderLessonData() {
        tvToolbarTitle.setText(currentLesson.getTitle());
        ((TextView) findViewById(R.id.tv_lesson_detail_title)).setText(currentLesson.getTitle());
        TextView tvDesc = findViewById(R.id.tv_lesson_description);
        if (tvDesc != null) tvDesc.setText(currentLesson.getDescription());
        
        dynamicVideoUrls.clear();
        if (currentLesson.getSteps() != null) {
            for (int i = 0; i < currentLesson.getSteps().size() && i < 4; i++) {
                com.example.appdraw.model.Lesson.Step step = currentLesson.getSteps().get(i);
                dynamicVideoUrls.add(step.getVideoUrl());
                stepTexts[i].setText(step.getDescription());
            }
        }
        setupMaterialsDynamic();
    }

    private void setupMaterialsDynamic() {
        if (llMaterialsContainer == null || currentLesson == null) return;
        llMaterialsContainer.removeAllViews();
        java.util.List<String> materials = currentLesson.getMaterials();
        if (materials == null) return;
        
        LayoutInflater inflater = LayoutInflater.from(this);
        for (String name : materials) {
            View itemView = inflater.inflate(R.layout.item_lesson_material, llMaterialsContainer, false);
            ((TextView) itemView.findViewById(R.id.tv_material_name)).setText(name);
            ((ImageView) itemView.findViewById(R.id.iv_material)).setImageResource(R.drawable.mau_nuoc);
            llMaterialsContainer.addView(itemView);
        }
    }

    private void downloadMaterials() {
        android.widget.Toast.makeText(this, "Đang tải xuống tài liệu...", android.widget.Toast.LENGTH_SHORT).show();
        
        String dummyPdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf";
        android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(Uri.parse(dummyPdfUrl));
        request.setTitle("Tài liệu bài học - " + currentLesson.getTitle());
        request.setDescription("Đang tải dữ liệu tham khảo...");
        request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, lessonTitle + "_materials.pdf");
        
        android.app.DownloadManager manager = (android.app.DownloadManager) getSystemService(android.content.Context.DOWNLOAD_SERVICE);
        if (manager != null) {
            manager.enqueue(request);
        }
    }

    private void showNotesBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_lesson_notes, null);
        bottomSheetDialog.setContentView(dialogView);

        androidx.recyclerview.widget.RecyclerView rvNotes = dialogView.findViewById(R.id.rv_notes);
        rvNotes.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        
        noteAdapter = new NoteAdapter(noteList, note -> {
            bottomSheetDialog.dismiss();
            videoView.seekTo(note.getTimestampMs());
            videoView.start();
        });
        rvNotes.setAdapter(noteAdapter);

        fetchNotesFromFirestore(rvNotes);

        android.widget.EditText etNoteInput = dialogView.findViewById(R.id.et_note_input);
        dialogView.findViewById(R.id.btn_send_note).setOnClickListener(v -> {
            String content = etNoteInput.getText().toString().trim();
            if (content.isEmpty()) return;
            
            int currentPos = videoView.getCurrentPosition(); // in milliseconds
            int seconds = currentPos / 1000;
            int mins = seconds / 60;
            int secs = seconds % 60;
            String formattedTime = String.format("%02d:%02d", mins, secs);

            com.example.appdraw.model.Note newNote = new com.example.appdraw.model.Note(
                java.util.UUID.randomUUID().toString(), content, currentStep, currentPos, formattedTime);
                
            db.collection("Users").document(uid).collection("notes")
                .document(lessonTitle).collection("userNotes").document(newNote.getId())
                .set(newNote)
                .addOnSuccessListener(aVoid -> {
                    noteList.add(newNote);
                    noteAdapter.notifyItemInserted(noteList.size() - 1);
                    rvNotes.scrollToPosition(noteList.size() - 1);
                    etNoteInput.setText("");
                });
        });

        bottomSheetDialog.show();
    }

    private void fetchNotesFromFirestore(androidx.recyclerview.widget.RecyclerView rvNotes) {
        if ("guest".equals(uid)) return;
        db.collection("Users").document(uid).collection("notes")
            .document(lessonTitle).collection("userNotes")
            .orderBy("timestampMs")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                noteList.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    com.example.appdraw.model.Note note = doc.toObject(com.example.appdraw.model.Note.class);
                    if (note != null) noteList.add(note);
                }
                noteAdapter.notifyDataSetChanged();
                if (!noteList.isEmpty()) {
                    rvNotes.scrollToPosition(noteList.size() - 1);
                }
            });
    }
}
