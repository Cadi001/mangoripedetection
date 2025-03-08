package com.example.mangosweetnessdetection;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.content.SharedPreferences;

import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class SetUsername extends AppCompatActivity {
    EditText username;
    Button setUsernameButton;
    TextView skipButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_username);


        username = findViewById(R.id.usernameText);
        setUsernameButton = findViewById(R.id.setUsernameButton);
        skipButton = findViewById(R.id.skipButton);
        Random random = new Random();

        skipButton.setOnClickListener(v -> {

            int min = 11111, max = 99999;
            String enteredUsername = "User" + Integer.toString((random.nextInt((max - min) + 1) + min));
            navigateToNextPage(enteredUsername, true);

        });
        setUsernameButton.setOnClickListener(v -> {
            String enteredUsername = username.getText().toString().trim();
            navigateToNextPage(enteredUsername, false);

        });


    }
    private void navigateToNextPage(String username, Boolean skipped){

        if (!username.isEmpty() || skipped == true) {
            // Store the username in SharedPreferences
            SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("username", username); // Save the username
            editor.apply(); // Apply changes

            // Optionally show a message
            Toast.makeText(this, "Username saved!", Toast.LENGTH_SHORT).show();

            // Redirect to the main activity after saving the username
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish(); // Close SetUsernameActivity
        } else {
            Toast.makeText(this, "Please enter a valid username", Toast.LENGTH_SHORT).show();
        }

    }

}