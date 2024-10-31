package com.example.mangosweetnessdetection;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.content.SharedPreferences;
import androidx.core.content.ContextCompat;

public class InitialScreen extends AppCompatActivity {
    ImageView mangoImage;
    ProgressBar loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_screen);

        mangoImage = findViewById(R.id.logo);
        loadingSpinner = findViewById(R.id.loading_spinner);

        // Set the color of the ProgressBar
        int color = ContextCompat.getColor(this, R.color.a2); // Replace 'your_desired_color' with an actual color resource
        loadingSpinner.getIndeterminateDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);

        // Delay the function execution by 5 seconds
        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
            String username = prefs.getString("username", null);

            if (username != null && !username.isEmpty()) {
                startActivity(new Intent(InitialScreen.this, MainActivity.class));
            } else {
                startActivity(new Intent(InitialScreen.this, SetUsername.class));
            }

            finish();
        }, 5000); // 5 seconds delay
    }
}
