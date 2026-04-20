package com.example.appdraw.explore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appdraw.R;
import com.example.appdraw.model.Lesson;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateLessonActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 71;

    private ImageView ivThumbnail;
    private MaterialCardView cvThumbnail;
    private Uri filePath;

    private TextInputEditText etTitle, etDesc, etDuration;
    private Spinner spinnerLevel, spinnerCategory;
    private ChipGroup chipGroupMaterials;
    private MaterialButton btnSubmit;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseAuth auth;

    private String authorName = "Giảng viên Vô danh";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_lesson);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            db.collection("Users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    authorName = documentSnapshot.getString("name");
                    if (authorName == null || authorName.isEmpty()) authorName = "Giảng viên";
                }
            });
        }

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        ivThumbnail = findViewById(R.id.iv_thumbnail);
        cvThumbnail = findViewById(R.id.cv_thumbnail);
        etTitle = findViewById(R.id.et_title);
        etDesc = findViewById(R.id.et_desc);
        etDuration = findViewById(R.id.et_duration);
        spinnerLevel = findViewById(R.id.spinner_level);
        spinnerCategory = findViewById(R.id.spinner_category);
        chipGroupMaterials = findViewById(R.id.chip_group_materials);

        loadCategories();
        btnSubmit = findViewById(R.id.btn_submit);

        cvThumbnail.setOnClickListener(v -> chooseImage());
        btnSubmit.setOnClickListener(v -> submitLesson());
    }

    private void loadCategories() {
        db.collection("Categories").orderBy("order").get()
            .addOnSuccessListener(querySnapshot -> {
                List<String> categoriesList = new ArrayList<>();
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot) {
                    String title = doc.getString("title");
                    if (title != null) categoriesList.add(title);
                }
                if (categoriesList.isEmpty()) categoriesList.add("Chung");
                
                android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, categoriesList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);
            });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh bìa"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                ivThumbnail.setImageBitmap(bitmap);
                ivThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void submitLesson() {
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String durationStr = etDuration.getText().toString().trim();
        
        if (title.isEmpty() || desc.isEmpty() || durationStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin cơ bản", Toast.LENGTH_SHORT).show();
            return;
        }

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("ĐANG TẢI LÊN...");

        if (filePath != null) {
            uploadImageAndSaveLesson(title, desc, durationStr);
        } else {
            saveLessonData(title, desc, durationStr, ""); // empty thumbnail
        }
    }

    private void uploadImageAndSaveLesson(String title, String desc, String durationStr) {
        try {
            Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Nén ảnh chất lượng 50% để Base64 không quá nặng
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data = baos.toByteArray();
            
            if (data.length == 0) {
                throw new Exception("Dữ liệu ảnh rỗng");
            }

            // By-pass Firebase Storage, chuyển ảnh thành Base64 lưu thẳng vào Firestore
            String base64Image = android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP);
            String finalImageUrl = "data:image/jpeg;base64," + base64Image;
            
            saveLessonData(title, desc, durationStr, finalImageUrl);

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnSubmit.setEnabled(true);
            btnSubmit.setText("XUẤT BẢN KHÓA HỌC");
        }
    }

    private void saveLessonData(String title, String desc, String durationStr, String thumbUrl) {
        String authorId = auth.getCurrentUser().getUid();
        String level = spinnerLevel.getSelectedItem() != null ? spinnerLevel.getSelectedItem().toString() : "Dễ";
        String category = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "Chung";
        int duration = Integer.parseInt(durationStr);

        List<String> materials = new ArrayList<>();
        for (int i = 0; i < chipGroupMaterials.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupMaterials.getChildAt(i);
            if (chip.isChecked()) {
                materials.add(chip.getText().toString());
            }
        }

        List<Lesson.Step> stepsList = new ArrayList<>();
        String lessonId = db.collection("Lessons").document().getId();
        Lesson lesson = new Lesson(lessonId, title, authorName, level, duration, 5.0f, desc, materials, stepsList);
        lesson.setCreatedAt(System.currentTimeMillis());
        lesson.setAuthorId(authorId);
        lesson.setThumbnailUrl(thumbUrl);
        lesson.setCategory(category);

        db.collection("Lessons").document(lessonId).set(lesson)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(CreateLessonActivity.this, "Đã xuất bản khóa học thành công!", Toast.LENGTH_LONG).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(CreateLessonActivity.this, "Lỗi xuất bản: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                btnSubmit.setEnabled(true);
                btnSubmit.setText("XUẤT BẢN KHÓA HỌC");
            });
    }
}
