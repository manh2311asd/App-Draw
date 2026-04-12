package com.example.appdraw.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appdraw.MainActivity;
import com.example.appdraw.R;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private JSONObject accountsJson;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        AutoCompleteTextView etUsername = findViewById(R.id.et_username);
        EditText etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView tvRegister = findViewById(R.id.tv_go_to_register);

        // Nạp danh sách đã lưu
        try {
            accountsJson = new JSONObject(prefs.getString("accounts", "{}"));
            List<String> emails = new ArrayList<>();
            Iterator<String> keys = accountsJson.keys();
            while (keys.hasNext()) {
                emails.add(keys.next());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, emails);
            etUsername.setAdapter(adapter);

            // Điền mật khẩu tự động khi chọn email
            etUsername.setOnItemClickListener((parent, view, position, id) -> {
                String selectedEmail = (String) parent.getItemAtPosition(position);
                try {
                    String savedPass = accountsJson.getString(selectedEmail);
                    etPassword.setText(savedPass);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

            // Hiện dropdown ngay khi click
            etUsername.setOnClickListener(v -> etUsername.showDropDown());
            etUsername.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) etUsername.showDropDown();
            });

        } catch (JSONException e) {
            accountsJson = new JSONObject();
        }

        btnLogin.setOnClickListener(v -> {
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Cập nhật danh sách lưu
                                try {
                                    accountsJson.put(email, password);
                                    prefs.edit().putString("accounts", accountsJson.toString()).apply();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                
                                prefs.edit().putLong("last_login_time", System.currentTimeMillis()).apply();

                                Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Tài khoản hoặc mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
