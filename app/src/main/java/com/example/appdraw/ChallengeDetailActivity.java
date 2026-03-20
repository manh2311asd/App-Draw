package com.example.appdraw;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChallengeDetailActivity extends AppCompatActivity {

    private LinearLayout llJoinedStatus;
    private MaterialButton btnJoin;
    private MaterialButton btnSubmit;
    
    private ImageView ivSelectedImage;
    private View llUploadPlaceholder;
    private Uri selectedImageUri;

    // Firebase
    private FirebaseStorage storage;
    private FirebaseFirestore db;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    if (ivSelectedImage != null && llUploadPlaceholder != null) {
                        ivSelectedImage.setImageURI(uri);
                        ivSelectedImage.setVisibility(View.VISIBLE);
                        llUploadPlaceholder.setVisibility(View.GONE);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_detail);

        // Initialize Firebase
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar_challenge_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        llJoinedStatus = findViewById(R.id.ll_joined_status);
        btnJoin = findViewById(R.id.btn_join_challenge);
        btnSubmit = findViewById(R.id.btn_submit_challenge);

        btnJoin.setOnClickListener(v -> {
            btnJoin.setVisibility(View.GONE);
            llJoinedStatus.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Bạn đã tham gia thử thách!", Toast.LENGTH_SHORT).show();
        });

        btnSubmit.setOnClickListener(v -> showSubmitDialog());
    }

    private void showSubmitDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_submit_challenge, null);
        
        MaterialCardView cardUpload = view.findViewById(R.id.card_upload_image);
        ivSelectedImage = view.findViewById(R.id.iv_selected_image);
        llUploadPlaceholder = view.findViewById(R.id.ll_upload_placeholder);
        MaterialButton btnSend = view.findViewById(R.id.btn_send_submit_challenge);
        com.google.android.material.textfield.TextInputEditText etComment = view.findViewById(R.id.et_submit_comment);

        if (selectedImageUri != null) {
            ivSelectedImage.setImageURI(selectedImageUri);
            ivSelectedImage.setVisibility(View.VISIBLE);
            llUploadPlaceholder.setVisibility(View.GONE);
        }

        cardUpload.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        if (btnSend != null) {
            btnSend.setOnClickListener(v -> {
                if (selectedImageUri == null) {
                    Toast.makeText(this, "Vui lòng tải ảnh tác phẩm lên!", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                String comment = etComment != null ? etComment.getText().toString() : "";
                uploadToFirebase(selectedImageUri, comment, bottomSheetDialog);
            });
        }

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void uploadToFirebase(Uri fileUri, String comment, BottomSheetDialog dialog) {
        Toast.makeText(this, "Đang tải bài lên...", Toast.LENGTH_SHORT).show();
        
        String fileName = "submissions/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference().child(fileName);

        ref.putFile(fileUri)
            .addOnSuccessListener(taskSnapshot -> {
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveToFirestore(uri.toString(), comment, dialog);
                });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void saveToFirestore(String imageUrl, String comment, BottomSheetDialog dialog) {
        Map<String, Object> submission = new HashMap<>();
        submission.put("imageUrl", imageUrl);
        submission.put("comment", comment);
        submission.put("timestamp", System.currentTimeMillis());
        submission.put("challengeId", "earth_day_2024"); // Ví dụ ID thử thách

        db.collection("submissions")
            .add(submission)
            .addOnSuccessListener(documentReference -> {
                dialog.dismiss();
                Toast.makeText(this, "Nộp bài thành công!", Toast.LENGTH_LONG).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
