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
    
    private final java.util.List<String> selectedInterests = new java.util.ArrayList<>();
    
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
            if (selectedInterests.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 sở thích", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Save to Firebase Firestore
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> data = new HashMap<>();
                
                String joinedInterests = android.text.TextUtils.join(", ", selectedInterests);
                data.put("interest", joinedInterests);
                
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
        String item = interests[selectedIndex];
        View line = findViewById(lineIds[selectedIndex]);
        
        if (selectedInterests.contains(item)) {
            selectedInterests.remove(item);
            line.setVisibility(View.INVISIBLE);
        } else {
            selectedInterests.add(item);
            line.setVisibility(View.VISIBLE);
        }
    }
}
