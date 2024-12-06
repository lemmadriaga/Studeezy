package com.example.studeezy.userAuth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studeezy.R;
import com.example.studeezy.userDashboard.Dashboard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {
    EditText editTextEmail, editTextPassword, editName;
    Button buttonReg;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    TextView textView;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToDashboard();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        editName = findViewById(R.id.name);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonReg = findViewById(R.id.btn_signup);
        textView = findViewById(R.id.loginNow);

        textView.setOnClickListener(view -> {
            startActivity(new Intent(SignUp.this, Login.class));
            finish();
        });

        buttonReg.setOnClickListener(view -> {
            String name = editName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                editTextEmail.setError("Email is required");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                editTextPassword.setError("Password is required");
                return;
            }
            if (password.length() < 6) {
                editTextPassword.setError("Password must be at least 6 characters");
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                saveUserToFirestore(userId, email, name);
                            }
                        } else {
                            Toast.makeText(SignUp.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void saveUserToFirestore(String userId, String email, String name) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("name", name);
        userMap.put("created_at", System.currentTimeMillis());
        userMap.put("hasPremium", false);
        userMap.put("role", "user");
        userMap.put("downloadCount", 0);

        firestore.collection("users").document(userId).set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignUp.this, "Account created", Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                })
                .addOnFailureListener(e -> {
                    String errorMessage = e != null ? e.getMessage() : "Unknown error";
                    Toast.makeText(SignUp.this, "Failed to save user: " + errorMessage, Toast.LENGTH_LONG).show();
                });
    }


    private void navigateToDashboard() {
        Intent intent = new Intent(SignUp.this, Dashboard.class);
        startActivity(intent);
        finish();
    }
}