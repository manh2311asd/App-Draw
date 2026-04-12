package com.example.appdraw.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appdraw.MainActivity;
import com.example.appdraw.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.Collections;

public class RegisterLevelActivity extends AppCompatActivity {
    
    private String selectedRole = null;
    private MaterialCardView cardBasic, cardAdvanced;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_level);

        cardBasic = findViewById(R.id.card_level_basic);
        cardAdvanced = findViewById(R.id.card_level_advanced);

        cardBasic.setOnClickListener(v -> selectLevel("user"));
        cardAdvanced.setOnClickListener(v -> selectLevel("mentor"));

        findViewById(R.id.btn_level_start).setOnClickListener(v -> {
            if (selectedRole == null) {
                Toast.makeText(this, "Vui lòng chọn trình độ của bạn", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                findViewById(R.id.btn_level_start).setEnabled(false);
                FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                    .set(Collections.singletonMap("role", selectedRole), SetOptions.merge())
                    .addOnCompleteListener(task -> {
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    });
            } else {
                Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            }
        });
    }

    private void selectLevel(String role) {
        selectedRole = role;
        int activeColor = Color.parseColor("#4272D0");
        int inactiveColor = Color.parseColor("#CCCCCC");
        
        cardBasic.setStrokeColor(role.equals("user") ? activeColor : inactiveColor);
        cardBasic.setStrokeWidth(role.equals("user") ? 6 : 2); // 6px ~= 2dp
        
        cardAdvanced.setStrokeColor(role.equals("mentor") ? activeColor : inactiveColor);
        cardAdvanced.setStrokeWidth(role.equals("mentor") ? 6 : 2);
    }
}
