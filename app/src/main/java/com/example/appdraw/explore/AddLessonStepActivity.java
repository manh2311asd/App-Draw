package com.example.appdraw.explore;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.example.appdraw.R;
import com.example.appdraw.model.Lesson;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddLessonStepActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDesc, etVideoUrl;
    private MaterialButton btnSave;
    private String lessonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lesson_step);

        lessonId = getIntent().getStringExtra("LESSON_ID");
        if (lessonId == null || lessonId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID Khóa Học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etTitle = findViewById(R.id.et_step_title);
        etDesc = findViewById(R.id.et_step_desc);
        etVideoUrl = findViewById(R.id.et_step_video_url);
        btnSave = findViewById(R.id.btn_save_step);

        btnSave.setOnClickListener(v -> saveStep());
    }

    private void saveStep() {
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String videoUrl = etVideoUrl.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Tên tập", Toast.LENGTH_SHORT).show();
            return;
        }
        if (videoUrl.isEmpty()) {
            Toast.makeText(this, "Vui lòng dán Link Video", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");

        Lesson.Step step = new Lesson.Step(title, desc, videoUrl);

        FirebaseFirestore.getInstance()
            .collection("Lessons")
            .document(lessonId)
            .update("steps", FieldValue.arrayUnion(step))
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Đã thêm tập mới thành công!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
                btnSave.setText("LƯU TẬP PHIM");
            });
    }
}
