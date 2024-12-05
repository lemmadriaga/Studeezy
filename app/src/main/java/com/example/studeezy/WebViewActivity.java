package com.example.studeezy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studeezy.userDashboard.Dashboard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WebViewActivity extends AppCompatActivity {

    private static final String TAG = "WebViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        String url = getIntent().getStringExtra("url");

        WebView webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Simulate successful payment when returning from the WebView
                if (url.contains("success")) { // Assume the URL contains "success" on successful payment
                    updateUserPremiumStatus();
                }
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }

    private void updateUserPremiumStatus() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Set `hasPremium` to true and calculate expiration date (30 days from now)
        long expirationDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000);

        userRef.child("hasPremium").setValue(true);
        userRef.child("expirationDate").setValue(expirationDate)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Premium status updated successfully.");
                        // Redirect back to the dashboard
                        Intent intent = new Intent(WebViewActivity.this, Dashboard.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e(TAG, "Failed to update premium status.");
                    }
                });
    }
}
