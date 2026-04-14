package com.example.appdraw.challenge;

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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appdraw.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class SubmitChallengeActivity extends AppCompatActivity {

    private ImageView ivArtworkPreview;
    private LinearLayout llUploadPlaceholder;
    private Uri selectedImageUri = null;
    private String challengeTitle;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivArtworkPreview.setImageURI(selectedImageUri);
                    ivArtworkPreview.setVisibility(View.VISIBLE);
                    llUploadPlaceholder.setVisibility(View.GONE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_challenge);

        challengeTitle = getIntent().getStringExtra("CHALLENGE_TITLE");
        if (challengeTitle == null) {
            challengeTitle = "Thử thách";
        }

        TextView tvTitle = findViewById(R.id.tv_submit_challenge_title);
        if (tvTitle != null) {
            tvTitle.setText(challengeTitle);
        }

        Toolbar toolbar = findViewById(R.id.toolbar_submit_challenge);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        ivArtworkPreview = findViewById(R.id.iv_artwork_preview);
        llUploadPlaceholder = findViewById(R.id.ll_upload_placeholder);

        View cardUpload = findViewById(R.id.card_upload_artwork);
        if (cardUpload != null) {
            cardUpload.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                galleryLauncher.launch(intent);
            });
        }

        EditText edtNote = findViewById(R.id.edt_artwork_note);
        MaterialButton btnSubmit = findViewById(R.id.btn_submit_artwork);

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                if (selectedImageUri == null) {
                    Toast.makeText(this, "Vui lòng tải ảnh tác phẩm lên!", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnSubmit.setEnabled(false);
                Toast.makeText(this, "Đang nộp bài...", Toast.LENGTH_SHORT).show();

                String note = edtNote != null ? edtNote.getText().toString().trim() : "";
                
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Users").document(user.getUid()).get().addOnSuccessListener(userDoc -> {
                    String authorName = "Học viên";
                    String userAvatar = "";
                    if (userDoc.exists() && userDoc.contains("profile")) {
                        Map<String, Object> profile = (Map<String, Object>) userDoc.get("profile");
                        if (profile != null) {
                            if (profile.containsKey("fullName")) authorName = (String) profile.get("fullName");
                            if (profile.containsKey("avatarUrl")) userAvatar = (String) profile.get("avatarUrl");
                        }
                    }

                    String finalImageUrl = "";
                    try {
                        Bitmap bitmap;
                        if (Build.VERSION.SDK_INT >= 28) {
                            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), selectedImageUri);
                            bitmap = ImageDecoder.decodeBitmap(source);
                        } else {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                        }
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, buffer);
                        byte[] fileBytes = buffer.toByteArray();
                        String base64Image = Base64.encodeToString(fileBytes, Base64.DEFAULT);
                        finalImageUrl = "data:image/jpeg;base64," + base64Image;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Map<String, Object> submissionData = new HashMap<>();
                    submissionData.put("userId", user.getUid());
                    submissionData.put("userName", authorName);
                    submissionData.put("userAvatar", userAvatar);
                    submissionData.put("note", note);
                    submissionData.put("imageUrl", finalImageUrl);
                    submissionData.put("status", "PENDING"); // Waiting for grade
                    submissionData.put("score", 0);
                    submissionData.put("feedback", "");
                    submissionData.put("timestamp", System.currentTimeMillis());

                    // To uniquely identify the challenge document, we can use title as ID or query by title. 
                    // Let's assume standard Challenges structure stores title as ID or we query it.
                    // To keep it safe and since we don't have ID, we'll store in Users/uid/submissions and also globally.
                    // Let's store globally in Challenges_Submissions collection.
                    
                    db.collection("Challenge_Submissions")
                        .add(submissionData)
                        .addOnSuccessListener(documentReference -> {
                            // Link it
                            documentReference.update("challengeTitle", challengeTitle);
                            
                            // Update local user status
                            Map<String, Object> localStatus = new HashMap<>();
                            localStatus.put("status", "SUBMITTED");
                            localStatus.put("submissionId", documentReference.getId());
                            db.collection("Users").document(user.getUid()).collection("joinedChallenges").document(challengeTitle).set(localStatus);

                            Toast.makeText(this, "Nộp bài thành công! Đang chờ chấm điểm.", Toast.LENGTH_LONG).show();
                            setResult(RESULT_OK);
                            finish();
                        })
                        .addOnFailureListener(e -> btnSubmit.setEnabled(true));
                });
            });
        }
    }
}
