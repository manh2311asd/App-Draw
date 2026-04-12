package com.example.appdraw.community;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.appdraw.R;
import com.example.appdraw.model.Post;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.UUID;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etContent;
    private ImageView ivImage, btnRemoveImage;
    private View btnPickGallery;
    private Uri selectedLocalUri;

    private RadioGroup rgCategory;
    private Switch swShare, swComment;

    private FirebaseFirestore db;
    private String currentUid;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedLocalUri = result.getData().getData();
                    showSelectedImage(selectedLocalUri.toString());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUid = FirebaseAuth.getInstance().getUid();
        }

        Toolbar toolbar = findViewById(R.id.toolbar_create_post);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        etContent = findViewById(R.id.et_post_content);
        
        // Populate text if coming from Homework Submission
        String prefillText = getIntent().getStringExtra("PREFILL_TEXT");
        if (prefillText != null && !prefillText.isEmpty()) {
            etContent.setText(prefillText);
        }
        ivImage = findViewById(R.id.iv_post_image);
        btnPickGallery = findViewById(R.id.btn_pick_gallery);
        btnRemoveImage = findViewById(R.id.btn_remove_image);
        rgCategory = findViewById(R.id.rg_category);
        swShare = findViewById(R.id.sw_share);
        swComment = findViewById(R.id.sw_comment);

        btnPickGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        btnRemoveImage.setOnClickListener(v -> removeImage());

        MaterialButton btnPublish = findViewById(R.id.btn_publish);
        btnPublish.setOnClickListener(v -> publishPost());
    }

    private void showSelectedImage(String uri) {
        btnPickGallery.setVisibility(View.GONE);
        View cvImagePreview = findViewById(R.id.cv_image_preview);
        if (cvImagePreview != null) cvImagePreview.setVisibility(View.VISIBLE);
        else ivImage.setVisibility(View.VISIBLE);
        btnRemoveImage.setVisibility(View.VISIBLE);
        Glide.with(this).load(uri).into(ivImage);
    }

    private void removeImage() {
        selectedLocalUri = null;
        ivImage.setImageDrawable(null);
        View cvImagePreview = findViewById(R.id.cv_image_preview);
        if (cvImagePreview != null) cvImagePreview.setVisibility(View.GONE);
        else ivImage.setVisibility(View.GONE);
        btnRemoveImage.setVisibility(View.GONE);
        btnPickGallery.setVisibility(View.VISIBLE);
    }

    private void publishPost() {
        String content = etContent.getText().toString().trim();
        if (content.isEmpty() && selectedLocalUri == null) {
            Toast.makeText(this, "Bài viết cần có nội dung hoặc màn vẽ!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUid == null) {
            Toast.makeText(this, "Yêu cầu đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        findViewById(R.id.btn_publish).setEnabled(false);
        Toast.makeText(this, "Đang đăng...", Toast.LENGTH_SHORT).show();

        if (selectedLocalUri != null) {
            try {
                android.graphics.Bitmap bitmap;
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    android.graphics.ImageDecoder.Source source = android.graphics.ImageDecoder.createSource(getContentResolver(), selectedLocalUri);
                    bitmap = android.graphics.ImageDecoder.decodeBitmap(source);
                } else {
                    bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), selectedLocalUri);
                }
                
                java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, buffer);
                byte[] fileBytes = buffer.toByteArray();
                
                if (fileBytes.length == 0) {
                    throw new Exception("Dữ liệu ảnh rỗng hoặc lỗi khi đọc ảnh");
                }

                // By-pass hoàn toàn Firebase Storage để không cần thẻ Visa
                // Chuyển ảnh thành chuỗi văn bản Base64 rồi lưu trực tiếp vào Firestore Document.
                String base64Image = android.util.Base64.encodeToString(fileBytes, android.util.Base64.DEFAULT);
                String finalImageUrl = "data:image/jpeg;base64," + base64Image;
                
                savePostToFirestore(content, finalImageUrl);
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi nén ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                findViewById(R.id.btn_publish).setEnabled(true);
            }
        } else {
            savePostToFirestore(content, null);
        }
    }

    private void savePostToFirestore(String content, String finalImageUrl) {
        String category = "Tác phẩm";
        if (rgCategory != null) {
            int checkedId = rgCategory.getCheckedRadioButtonId();
            if (checkedId == R.id.rb_tips) category = "Tips";
            else if (checkedId == R.id.rb_progress) category = "Tiến độ";
            else if (checkedId == R.id.rb_handmade) category = "Thủ công";
        }

        boolean share = swShare != null && swShare.isChecked();
        boolean allowComment = swComment != null && swComment.isChecked();

        String docId = db.collection("Posts").document().getId();
        Post post = new Post(docId, currentUid, content, finalImageUrl, category, new ArrayList<>(), System.currentTimeMillis());
        // For our logic, if share=false, it still creates but won't show on community feed (we can add field isPublic if needed, for simplicity we trust 'share' implies isPublic. Let's not add too many fields unless breaking).
        // Let's add allowComments to model if needed, but the model has commentsCount.
        // It's mostly UI for Mockup fidelity. Let's just save.

        db.collection("Posts").document(docId).set(post)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, com.example.appdraw.MainActivity.class);
                    // Dùng cờ này để MainActivity nhận onNewIntent (nếu đã mở) thay vì tạo mới activity
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("NAVIGATE_TO_COMMUNITY", true);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi đăng bài!", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.btn_publish).setEnabled(true);
                });
    }
}
