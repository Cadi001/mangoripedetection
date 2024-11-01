package com.example.mangosweetnessdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class OutputActivity extends AppCompatActivity {
    TextView outputTextView;
    Button scanAnotherMangoButton;
    ImageView passedImage;
    FirebaseFirestore db;
    String username;
    TextView showRatingsDialogButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);
        outputTextView = findViewById(R.id.outputTextView);
        scanAnotherMangoButton = findViewById(R.id.scanAnotherMangoButton);
        showRatingsDialogButton = findViewById(R.id.showRatingsDialogButton);
        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        username = prefs.getString("username", "Anonymous"); // Default value is null if not set


        Intent intent = getIntent();

        String message = intent.getStringExtra("output"); // Retrieve the string
        outputTextView.setText(message);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showRateDialog();
            }
        }, 60000);

        scanAnotherMangoButton.setOnClickListener(v -> {
          startActivity(new Intent(OutputActivity.this, MainActivity.class));
        });
        showRatingsDialogButton.setOnClickListener(v -> {
            showRateDialog();
        });
    }

    private void showRateDialog() {
        // Create an AlertDialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_rate_app, null);
        builder.setView(dialogView);

        // Find RatingBar, EditText, and Button
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText feedbackEditText = dialogView.findViewById(R.id.feedbackEditText);
        Button submitButton = dialogView.findViewById(R.id.submitRatingButton);

        // Create the dialog
        final androidx.appcompat.app.AlertDialog dialog = builder.create();

        // Set submit button click listener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get rating and feedback
                float rating = ratingBar.getRating();
                String feedback = feedbackEditText.getText().toString();

                // Ensure rating is selected and feedback is not empty
                if (rating == 0.0f) {
                    Toast.makeText(OutputActivity.this, "Please provide a rating", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (feedback.isEmpty()) {
                    feedbackEditText.setError("Please enter feedback");
                    return;
                }

                // Save the rating and feedback to Firestore
                saveFeedbackToFirestore(rating, feedback, username);

                // Show a confirmation message
                Toast.makeText(OutputActivity.this, "Thanks for your feedback!", Toast.LENGTH_SHORT).show();

                // Dismiss the dialog after submitting
                dialog.dismiss();
            }
        });

        // Show the dialog
        dialog.show();
    }


    private void saveFeedbackToFirestore(float rating, String feedback, String username) {
        // Create a map to store the rating, feedback, and username
        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("rating", rating);
        feedbackData.put("feedback", feedback);
        feedbackData.put("username", username); // Add username to the data
        feedbackData.put("timestamp", System.currentTimeMillis());  // Optional: Add timestamp

        // Add data to Firestore
        db.collection("feedbacks")
                .add(feedbackData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(OutputActivity.this, "Feedback saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OutputActivity.this, "Failed to save feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}