package com.example.appdraw.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appdraw.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class RegisterInterestsActivity extends AppCompatActivity {
    
    private String selectedInterest = null;
    
    // Arrays for IDs
    private final int[] blockIds = {
        R.id.ll_interest_1, R.id.ll_interest_2, R.id.ll_interest_3, R.id.ll_interest_4,
        R.id.ll_interest_5, R.id.ll_interest_6, R.id.ll_interest_7, R.id.ll_interest_8
    };
    private final int[] lineIds = {
        R.id.view_line_1, R.id.view_line_2, R.id.view_line_3, R.id.view_line_4,
        R.id.view_line_5, R.id.view_line_6, R.id.view_line_7, R.id.view_line_8
    };
    private final String[] interests = {
        "Vẽ chì màu", "Vẽ màu nước", "Vẽ màu sáp", "Vẽ phong cảnh", 
        "Vẽ chân dung", "Tranh sơn dầu", "Kí họa", "Đồ thủ công"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_interests);

        // Setup selection click listeners
        for (int i = 0; i < blockIds.length; i++) {
            final int index = i;
            findViewById(blockIds[i]).setOnClickListener(v -> {
                selectInterest(index);
            });
        }

        findViewById(R.id.btn_interest_next).setOnClickListener(v -> {
            if (selectedInterest == null) {
                Toast.makeText(this, "Vui lòng chọn 1 sở thích", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Save to Firebase Firestore
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> data = new HashMap<>();
                data.put("interest", selectedInterest);
                
                db.collection("Users").document(user.getUid()).set(data, SetOptions.merge())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(this, RegisterLevelActivity.class));
                        } else {
                            Toast.makeText(this, "Lỗi lưu dữ liệu: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            } else {
                Toast.makeText(this, "Bạn chưa đăng nhập or phiên kết thúc", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            }
        });
    }
    
    private void selectInterest(int selectedIndex) {
        // Clear all
        for (int id : lineIds) {
            findViewById(id).setVisibility(View.INVISIBLE);
        }
        // Show selection line
        findViewById(lineIds[selectedIndex]).setVisibility(View.VISIBLE);
        selectedInterest = interests[selectedIndex];
    }
}
