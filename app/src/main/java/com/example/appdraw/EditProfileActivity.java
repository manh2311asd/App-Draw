package com.example.appdraw;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private EditText etFullName, etBio;
    private MaterialButton btnSave;
    private View layoutLoading;

    private String currentAvatarBase64 = null;
    private FirebaseFirestore db;
    private FirebaseUser user;

    private final ActivityResultLauncher<Intent> avatarLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    processImage(result.getData().getData());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        initViews();
        loadCurrentData();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_edit_profile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ivAvatar = findViewById(R.id.iv_edit_avatar);
        etFullName = findViewById(R.id.et_edit_fullname);
        etBio = findViewById(R.id.et_edit_bio);
        btnSave = findViewById(R.id.btn_save_profile);
        layoutLoading = findViewById(R.id.layout_loading);

        ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            avatarLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadCurrentData() {
        // Pre-fill
        if (user == null) return;
        layoutLoading.setVisibility(View.VISIBLE);
        db.collection("Users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    layoutLoading.setVisibility(View.GONE);
                    if (documentSnapshot.exists() && documentSnapshot.contains("profile")) {
                        Map<String, Object> profile = (Map<String, Object>) documentSnapshot.get("profile");
                        if (profile != null) {
                            String fullName = (String) profile.get("fullName");
                            String bio = (String) profile.get("bio");
                            String avatarUrl = (String) profile.get("avatarUrl");

                            if (fullName != null) etFullName.setText(fullName);
                            if (bio != null) etBio.setText(bio);

                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                currentAvatarBase64 = avatarUrl;
                                if (avatarUrl.startsWith("data:image")) {
                                    byte[] decodedBytes = Base64.decode(avatarUrl.split(",")[1], Base64.DEFAULT);
                                    Glide.with(this).load(decodedBytes).circleCrop().into(ivAvatar);
                                } else {
                                    Glide.with(this).load(avatarUrl).circleCrop().into(ivAvatar);
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> layoutLoading.setVisibility(View.GONE));
    }

    private void processImage(Uri uri) {
        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= 28) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, buffer);
            byte[] fileBytes = buffer.toByteArray();

            currentAvatarBase64 = "data:image/jpeg;base64," + Base64.encodeToString(fileBytes, Base64.DEFAULT);
            byte[] decodedBytes = Base64.decode(currentAvatarBase64.split(",")[1], Base64.DEFAULT);
            Glide.with(this).load(decodedBytes).circleCrop().into(ivAvatar);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi đọc ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfile() {
        if (user == null) return;
        String newName = etFullName.getText().toString().trim();
        String newBio = etBio.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        layoutLoading.setVisibility(View.VISIBLE);
        
        Map<String, Object> profileUpdates = new HashMap<>();
        profileUpdates.put("fullName", newName);
        profileUpdates.put("bio", newBio);
        if (currentAvatarBase64 != null) {
            profileUpdates.put("avatarUrl", currentAvatarBase64);
        }

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("profile", profileUpdates);

        db.collection("Users").document(user.getUid())
                .set(userUpdates, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Đã lưu hồ sơ", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
