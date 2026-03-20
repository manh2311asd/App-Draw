package com.example.appdraw;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivMediaPreview;
    private View llPlaceholder;
    private EditText etCaption;
    private Uri selectedMediaUri;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        ivMediaPreview = findViewById(R.id.iv_media_preview);
        llPlaceholder = findViewById(R.id.ll_add_media_placeholder);
        etCaption = findViewById(R.id.et_caption);
        
        setupToolbar();

        findViewById(R.id.card_add_media).setOnClickListener(v -> openGallery());

        findViewById(R.id.btn_post).setOnClickListener(v -> uploadPost());

        findViewById(R.id.tv_choose_lesson).setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng chọn bài học đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_create_post);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/* video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh hoặc video"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedMediaUri = data.getData();
            ivMediaPreview.setImageURI(selectedMediaUri);
            ivMediaPreview.setVisibility(View.VISIBLE);
            llPlaceholder.setVisibility(View.GONE);
        }
    }

    private void uploadPost() {
        String caption = etCaption.getText().toString().trim();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để đăng bài", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedMediaUri == null && caption.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm nội dung hoặc hình ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        findViewById(R.id.btn_post).setEnabled(false);
        Toast.makeText(this, "Đang tải bài viết lên cộng đồng...", Toast.LENGTH_SHORT).show();

        // Tạo dữ liệu bài viết
        String postId = mDatabase.child("posts").push().getKey();
        Map<String, Object> postData = new HashMap<>();
        postData.put("postId", postId);
        postData.put("userId", user.getUid());
        postData.put("userName", user.getDisplayName() != null ? user.getDisplayName() : "Người dùng mới");
        postData.put("caption", caption);
        postData.put("timestamp", System.currentTimeMillis());
        
        // Lưu vào Firebase Database
        if (postId != null) {
            mDatabase.child("posts").child(postId).setValue(postData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(CreatePostActivity.this, "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            findViewById(R.id.btn_post).setEnabled(true);
                            Toast.makeText(CreatePostActivity.this, "Lỗi khi đăng bài", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
