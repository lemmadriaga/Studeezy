package com.example.studeezy.payment;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface PaymentApi {

    @Headers("Authorization: Basic YOUR_API_KEY_BASE64_ENCODED")
    @POST("v1/payments")
    Call<PaymentResponse> createPayment(@Body PaymentRequest request);
}
