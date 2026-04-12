package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appdraw.model.Project;
import com.example.appdraw.project.ProjectDetailActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateProjectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        Toolbar toolbar = findViewById(R.id.toolbar_create_project);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        EditText etName = findViewById(R.id.et_project_name);
        EditText etGoal = findViewById(R.id.et_project_goal);
        EditText etDescription = findViewById(R.id.et_project_description);
        MaterialButton btnStart = findViewById(R.id.btn_start_now);
        
        btnStart.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String goal = etGoal.getText().toString().trim();
            String description = etDescription != null ? etDescription.getText().toString().trim() : "";
            
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Vui lòng nhập tên dự án!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) {
                Toast.makeText(this, "Yêu cầu đăng nhập!", Toast.LENGTH_SHORT).show();
                return;
            }

            btnStart.setEnabled(false);
            btnStart.setText("Đang tạo Dự Án...");

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String docId = db.collection("Projects").document().getId();

            Project newProj = new Project(docId, uid, name, goal, description, "", 0, System.currentTimeMillis());

            db.collection("Projects").document(docId).set(newProj)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Tạo Dự án thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, ProjectDetailActivity.class);
                        intent.putExtra("PROJECT_ID", docId);
                        intent.putExtra("PROJECT_NAME", name);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnStart.setEnabled(true);
                        btnStart.setText("Bắt đầu ngay");
                        Toast.makeText(this, "Lỗi tạo dự án: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
