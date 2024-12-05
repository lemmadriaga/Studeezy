package com.example.studeezy;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserDetailsActivity extends AppCompatActivity {

    private TextView name, email, premiumStatus, expirationDate, createdAt;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        
        name = findViewById(R.id.textName);
        email = findViewById(R.id.textEmail);
        premiumStatus = findViewById(R.id.textPremiumStatus);
        expirationDate = findViewById(R.id.textExpirationDate);
        createdAt = findViewById(R.id.textCreatedAt);
        btnBack = findViewById(R.id.btnBack);

        
        String userName = getIntent().getStringExtra("name");
        String userEmail = getIntent().getStringExtra("email");
        Boolean isPremium = getIntent().getBooleanExtra("hasPremium", false);
        Long userExpirationDate = getIntent().getLongExtra("expirationDate", 0L);
        Long userCreatedAt = getIntent().getLongExtra("createdAt", 0L);

        
        String formattedExpirationDate = formatDate(userExpirationDate);
        String formattedCreatedAt = formatDate(userCreatedAt);

        
        name.setText("Name: " + userName);
        email.setText("Email: " + userEmail);
        premiumStatus.setText("Premium Status: " + (isPremium ? "Active" : "Inactive"));
        expirationDate.setText("Subscription Expires On: " +
                (userExpirationDate > 0 ? formattedExpirationDate : "N/A"));
        createdAt.setText("Account Created On: " +
                (userCreatedAt > 0 ? formattedCreatedAt : "N/A"));

        
        btnBack.setOnClickListener(v -> finish());
    }

    
    private String formatDate(Long timestamp) {
        if (timestamp > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            Date date = new Date(timestamp);
            return sdf.format(date);
        }
        return "N/A";
    }
}
