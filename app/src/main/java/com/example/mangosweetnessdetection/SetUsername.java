package com.example.mangosweetnessdetection;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.content.SharedPreferences;

import android.widget.TextView;
import android.widget.Toast;

public class SetUsername extends AppCompatActivity {
    EditText username;
    Button setUsernameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_username);


        username = findViewById(R.id.usernameText);
        setUsernameButton = findViewById(R.id.setUsernameButton);

        setUsernameButton.setOnClickListener(v -> {
            String enteredUsername = username.getText().toString().trim();

            if (!enteredUsername.isEmpty()) {
                // Store the username in SharedPreferences
                SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("username", enteredUsername); // Save the username
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
        });


    }

}