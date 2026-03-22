package com.example.appdraw;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class ChallengeDetailActivity extends AppCompatActivity {

    private LinearLayout llJoinedStatus;
    private TextView tvSubmissionStatus;
    private MaterialButton btnJoin;
    private MaterialButton btnSubmit;
    
    private ImageView ivSelectedImage;
    private View llUploadPlaceholder;
    private Uri selectedImageUri;

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

        Toolbar toolbar = findViewById(R.id.toolbar_challenge_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        llJoinedStatus = findViewById(R.id.ll_joined_status);
        tvSubmissionStatus = findViewById(R.id.tv_submission_status);
        btnJoin = findViewById(R.id.btn_join_challenge);
        btnSubmit = findViewById(R.id.btn_submit_challenge);

        // Xử lý click vào ảnh banner để xem danh sách bài dự thi
        View cardBanner = findViewById(R.id.card_challenge_banner);
        if (cardBanner != null) {
            cardBanner.setOnClickListener(v -> openEntryList());
        }

        // Xem thêm bài dự thi
        View btnViewMore = findViewById(R.id.btn_view_more_entries);
        if (btnViewMore != null) {
            btnViewMore.setOnClickListener(v -> openEntryList());
        }

        // Xử lý trạng thái từ Intent
        String status = getIntent().getStringExtra("CHALLENGE_STATUS");
        updateUI(status);

        if (btnJoin != null) {
            btnJoin.setOnClickListener(v -> showJoinSuccessDialog());
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> showSubmitDialog());
        }
    }

    private void openEntryList() {
        Intent intent = new Intent(this, ChallengeEntryListActivity.class);
        intent.putExtra("CHALLENGE_TITLE", "Nặn đất sét sáng tạo");
        startActivity(intent);
    }

    private void updateUI(String status) {
        if ("SUBMITTED".equals(status)) {
            if (llJoinedStatus != null) llJoinedStatus.setVisibility(View.VISIBLE);
            if (tvSubmissionStatus != null) tvSubmissionStatus.setText("Bài của bạn : Đã nộp . Đang chờ kết quả");
            if (btnJoin != null) btnJoin.setVisibility(View.GONE);
            if (btnSubmit != null) btnSubmit.setVisibility(View.GONE);
        } else if ("JOINED".equals(status)) {
            if (llJoinedStatus != null) llJoinedStatus.setVisibility(View.VISIBLE);
            if (tvSubmissionStatus != null) tvSubmissionStatus.setText("Bài của bạn : Chưa nộp");
            if (btnJoin != null) btnJoin.setVisibility(View.GONE);
            if (btnSubmit != null) btnSubmit.setVisibility(View.VISIBLE);
            if (btnSubmit != null) btnSubmit.setText("NỘP BÀI");
        } else {
            if (llJoinedStatus != null) llJoinedStatus.setVisibility(View.GONE);
            if (btnJoin != null) btnJoin.setVisibility(View.VISIBLE);
            if (btnSubmit != null) btnSubmit.setVisibility(View.GONE);
        }
    }

    private void showJoinSuccessDialog() {
        Dialog dialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_notice_challenge, null);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        view.findViewById(R.id.btn_submit_now).setOnClickListener(v -> {
            dialog.dismiss();
            updateUI("JOINED");
            showSubmitDialog();
        });

        view.findViewById(R.id.btn_later).setOnClickListener(v -> {
            dialog.dismiss();
            updateUI("JOINED");
        });

        dialog.show();
    }

    private void showSubmitDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_submit_challenge, null);
        
        MaterialCardView cardUpload = view.findViewById(R.id.card_upload_image);
        ivSelectedImage = view.findViewById(R.id.iv_selected_image);
        llUploadPlaceholder = view.findViewById(R.id.ll_upload_placeholder);
        MaterialButton btnSend = view.findViewById(R.id.btn_send_submit_challenge);

        cardUpload.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        if (btnSend != null) {
            btnSend.setOnClickListener(v -> {
                if (selectedImageUri == null) {
                    Toast.makeText(this, "Vui lòng tải ảnh tác phẩm lên!", Toast.LENGTH_SHORT).show();
                    return;
                }
                bottomSheetDialog.dismiss();
                showSubmitSuccessDialog();
            });
        }

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void showSubmitSuccessDialog() {
        Dialog dialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_notice_submit_success, null);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        view.findViewById(R.id.btn_ok).setOnClickListener(v -> {
            dialog.dismiss();
            updateUI("SUBMITTED");
        });

        dialog.show();
    }
}
