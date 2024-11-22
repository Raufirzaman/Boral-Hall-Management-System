package com.raufir.bauethms;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class splash extends AppCompatActivity {
    private static final int SPLASH_SCREEN_TIMEOUT = 3000; // Duration for splash screen (3 seconds)
    private static final int TEXT_FADE_IN_DELAY = 2000; // Delay before starting fade-in animation (1 second)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Find the TextView in the layout
        TextView appNameTextView = findViewById(R.id.appName);

        // Load the fade-in animation
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Delay the fade-in effect for the TextView
        new Handler().postDelayed(() -> {
            appNameTextView.setVisibility(View.VISIBLE);
            appNameTextView.startAnimation(fadeInAnimation);
        }, TEXT_FADE_IN_DELAY);

        // Navigate to the main activity after the splash screen timeout
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(splash.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close SplashActivity so it won't appear when pressing back
        }, SPLASH_SCREEN_TIMEOUT);
    }
}
