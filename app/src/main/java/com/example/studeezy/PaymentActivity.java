package com.example.studeezy;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.studeezy.R;
import com.google.android.material.button.MaterialButton;

public class PaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        MaterialButton proceedButton = findViewById(R.id.proceedToPaymentButton);
        proceedButton.setOnClickListener(view -> {
            Intent intent = new Intent(PaymentActivity.this, PaymentProcess.class);
            startActivity(intent);
        });
    }
}
