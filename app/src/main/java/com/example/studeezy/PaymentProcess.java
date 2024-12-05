package com.example.studeezy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PaymentProcess extends AppCompatActivity {

    private static final String TAG = "PaymentProcess";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_process);

        createPaymentLink();
    }

    private void createPaymentLink() {
        Thread thread = new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType,
                        "{\"data\":{\"attributes\":{\"amount\":15000,\"description\":\"Get all the modules in a month\",\"remarks\":\"Thank you for purchasing!\"}}}");
                Request request = new Request.Builder()
                        .url("https://api.paymongo.com/v1/links")
                        .post(body)
                        .addHeader("accept", "application/json")
                        .addHeader("content-type", "application/json")
                        .addHeader("authorization", "Basic c2tfdGVzdF9kQ2VtbmhpZE5Xd05UeHFhVWkyWVFjRVI6")
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Payment link created: " + responseBody);


                    String checkoutUrl = extractCheckoutUrl(responseBody);


                    if (checkoutUrl != null) {
                        Intent intent = new Intent(PaymentProcess.this, WebViewActivity.class);
                        intent.putExtra("url", checkoutUrl);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e(TAG, "Failed to extract checkout URL.");
                    }
                } else {
                    Log.e(TAG, "Error creating payment link: " + response.code() + " " + response.message());
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception occurred while creating payment link", e);
            }
        });

        thread.start();
    }

    private String extractCheckoutUrl(String responseBody) {
        try {

            JSONObject jsonObject = new JSONObject(responseBody);
            return jsonObject.getJSONObject("data")
                    .getJSONObject("attributes")
                    .getString("checkout_url");
        } catch (Exception e) {
            Log.e(TAG, "Error parsing checkout URL from response", e);
            return null;
        }
    }
}
