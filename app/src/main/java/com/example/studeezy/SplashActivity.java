package com.example.studeezy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.logo);
        TextView tagline = findViewById(R.id.tagline);
        
        Animation fadeInLogo = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUpFadeInTagline = AnimationUtils.loadAnimation(this, R.anim.tagline_slide_up_fade_in);

        logo.startAnimation(fadeInLogo);
        
        new Handler().postDelayed(() -> {
            tagline.setVisibility(TextView.VISIBLE);
            tagline.startAnimation(slideUpFadeInTagline);
        }, 1000);
        
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 2500);
    }
}
