package com.example.mangosweetnessdetection;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int TAKE_PHOTO = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private ImageView imageView;
    private Button buttonSelectFromGallery, buttonTakePhoto, buttonDetect;
    private String base64Image;
    private Uri photoUri;
    private TextView outputTxt;
    private String currentPhotoPath;
    int detectionCounter;
    private final String[] mangoTips = {
            "1. Look for mangoes with a vibrant color and a pleasant fruity aroma.",
            "2. Gently squeeze the mango; ripe ones will give slightly without being mushy.",
            "3. Check for firmness; avoid overly soft or shriveled mangoes.",
            "4. Avoid mangoes with dark spots, bruises, or signs of decay.",
            "5. Smell the stem end; a sweet, fragrant aroma indicates ripeness."
    };
    private int tipIndex = 0;
    private Handler handler = new Handler();

    private ProgressDialog progressDialog;
    TextView greetings;
    Button gotoRatings;
    FirebaseFirestore db;
    String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = "Anonymous";
        imageView = findViewById(R.id.imageView);
        buttonSelectFromGallery = findViewById(R.id.buttonSelectFromGallery);
        buttonTakePhoto = findViewById(R.id.buttonTakePhoto);
        buttonDetect = findViewById(R.id.buttonDetect);
        outputTxt = findViewById(R.id.outputTxt);
        db = FirebaseFirestore.getInstance();
        gotoRatings = findViewById(R.id.gotoRatings);
        greetings = findViewById(R.id.greetings);
        // Check if the username is already set
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        username = prefs.getString("username", null); // Default value is null if not set
        greetings.setText("Howdy, " + username);

        detectionCounter = 1;
// Initialize ProgressDialog
        progressDialog = new ProgressDialog(this, R.style.CustomProgressDialog);
        progressDialog.setCancelable(false);
        startTipRotation(); // Start rotating tips when progress dialog is shown

        gotoRatings.setOnClickListener(v -> {
            startActivity(new Intent(this, RatingsActivity.class));
        });
        // Button for selecting an image from the gallery
        buttonSelectFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromGallery();
            }
        });

        // Button for taking a photo with the camera
        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCameraPermission();
            }
        });

        // Button for detecting mango ripeness
        buttonDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (base64Image != null) {
                    // Show progress dialog
                    //FOR PRESENTATION PURPOSES THIS METHOD IS CALLED EVERYTIME A USER DETECT A MANGO IN PROD IT MUST BE AFTER 3 TEST AND EVERY AFTER 20 TEST
                    progressDialog.show();
                    detectMangoRipeness(base64Image);
                } else {
                    Toast.makeText(MainActivity.this, "Please select an image first", Toast.LENGTH_LONG).show();
                }
            }
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

                if (feedback.isEmpty()) {
                    feedbackEditText.setError("Please enter feedback");
                    return;
                }

                // Save the rating and feedback to Firestore
                saveFeedbackToFirestore(rating, feedback, username);

                // Show a confirmation message
                Toast.makeText(MainActivity.this, "Thanks for your feedback!", Toast.LENGTH_SHORT).show();

                // Dismiss the dialog
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
                    Toast.makeText(MainActivity.this, "Feedback saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to save feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void startTipRotation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Set the current tip message in the progress dialog
                progressDialog.setMessage("Detecting ripeness... \n\n" + mangoTips[tipIndex]);

                // Cycle through the tips
                tipIndex = (tipIndex + 1) % mangoTips.length;

                // Schedule the next tip update after 2 seconds
                handler.postDelayed(this, 2000);
            }
        }, 0); // Start immediately
    }

    private void stopTipRotation() {
        // Stop the handler from updating the tips
        handler.removeCallbacksAndMessages(null);
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            takePhotoFromCamera();
        }
    }

    private void takePhotoFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(this, "com.yourpackage.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(takePictureIntent, TAKE_PHOTO);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                Toast.makeText(this, "Error occurred while creating the file", Toast.LENGTH_LONG).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            handleImageSelection(imageUri);
        } else if (requestCode == TAKE_PHOTO && resultCode == RESULT_OK) {
            handleImageSelection(photoUri);
        }
    }

    private void handleImageSelection(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            imageView.setImageBitmap(bitmap);
            base64Image = encodeImageToBase64(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_LONG).show();
        }
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void detectMangoRipeness(String base64Image) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), base64Image);

        Call<ResponseBody> call = apiService.detectMangoRipeness("BRj3tf23AYCWdXz7qQjg", requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Dismiss the progress dialog when the response is received
                progressDialog.dismiss();
                stopTipRotation(); // Stop rotating tips


                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Intent intent = new Intent(MainActivity.this, OutputActivity.class);
                        // Parse the response JSON to extract top prediction details
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray predictions = jsonResponse.getJSONArray("predictions");
                        String outputMessage = "";
                        if (predictions.length() > 0) {
                            JSONObject topPrediction = predictions.getJSONObject(0); // Get the first (top) prediction
                            String predictionClass = topPrediction.getString("class");
                            double confidence = topPrediction.getDouble("confidence");
                            if(confidence <= 0.5){
//                                outputTxt.setText("Probability: "+confidence +"\nThe image provided is less likely identified as carabao mango.");
//                                outputTxt.setVisibility(View.VISIBLE);
                                outputMessage = "Probability: "+confidence +"\nThe image provided is less likely identified as carabao mango.";
                            }
                            else if(predictionClass.equals("Unripe"))
                            {
//                                outputTxt.setText("Probability: "+confidence +"\nSweet level: Sour, tangy, and slightly astringent with a firm \nTips:\nThese are best for savory dishes like salads, chutneys, or pickles. " +
//                                        "Avoid eating them raw as they can be very sour and astringent. Add salt or spices to balance the tartness.");
//                                outputTxt.setVisibility(View.VISIBLE);
                                outputMessage = "Probability: "+confidence +"\nSweet level: Sour, tangy, and slightly astringent with a firm \nTips:\nThese are best for savory dishes like salads, chutneys, or pickles. " +
                                        "Avoid eating them raw as they can be very sour and astringent. Add salt or spices to balance the tartness.";

                            }
                            else if(predictionClass.equals("Early Ripe"))
                            {
//                                outputTxt.setText("Probability: "+confidence +"\nSweet level: Slightly sweet with a mild tartness \nTips:\nPerfect for slicing and adding to fruit salads or eating as a slightly tart snack. " +
//                                        "If you prefer sweeter mangoes, let them sit at room temperature for a couple more days to ripen further.");
//                                outputTxt.setVisibility(View.VISIBLE);
                                outputMessage = "Probability: "+confidence +"\nSweet level: Slightly sweet with a mild tartness \nTips:\nPerfect for slicing and adding to fruit salads or eating as a slightly tart snack. " +
                                        "If you prefer sweeter mangoes, let them sit at room temperature for a couple more days to ripen further.";

                            }
                            else if(predictionClass.equals("Partially Ripe"))
                            {
//                                outputTxt.setText("Probability: "+confidence +"\nSweet level: Balanced between sweet and tart with a juicy \nTips:\nThese are great for smoothies, salsas, or desserts that need a balance of sweet" +
//                                        " and tangy flavors. Their firmer texture makes them ideal for slicing without becoming too soft.");
//                                outputTxt.setVisibility(View.VISIBLE);
                                outputMessage = "Probability: "+confidence +"\nSweet level: Balanced between sweet and tart with a juicy \nTips:\nThese are great for smoothies, salsas, or desserts that need a balance of sweet" +
                                        " and tangy flavors. Their firmer texture makes them ideal for slicing without becoming too soft.";

                            }
                            else if(predictionClass.equals("Ripe"))
                            {
//                                outputTxt.setText("Probability: "+confidence +"\nSweet level: Sweet, rich, and aromatic with a juicy, soft, and smooth texture \nTips:\nEnjoy these fresh, in desserts, or as a juice. Ripe mangoes are " +
//                                        "ideal for snacking, fruit salads, or as a topping for yogurt and ice cream. Store them in the refrigerator if not consuming immediately to preserve their flavor" +
//                                        " and texture.");
//                                outputTxt.setVisibility(View.VISIBLE);
                                outputMessage = "Probability: "+confidence +"\nSweet level: Sweet, rich, and aromatic with a juicy, soft, and smooth texture \nTips:\nEnjoy these fresh, in desserts, or as a juice. Ripe mangoes are " +
                                        "ideal for snacking, fruit salads, or as a topping for yogurt and ice cream. Store them in the refrigerator if not consuming immediately to preserve their flavor" +
                                        " and texture.";

                            }
                            else if(confidence >= 0.9 && predictionClass.equals("Over Ripe"))
                            {
//                                outputTxt.setText("Probability: "+confidence +"\nSweet level: Sweet & most likely bitter \nThis mango is OVER RIPE \nTips:\nOverripe mangoes are unsafe to eat if they have a sour or fermented smell, " +
//                                        "visible mold, dark spots, slimy texture, or an off-taste, as these indicate spoilage and potential harmful bacteria.");
//                                outputTxt.setVisibility(View.VISIBLE);
                                outputMessage = "Probability: "+confidence +"\nSweet level: Sweet & most likely bitter \nThis mango is OVER RIPE \nTips:\nOverripe mangoes are unsafe to eat if they have a sour or fermented smell, " +
                                        "visible mold, dark spots, slimy texture, or an off-taste, as these indicate spoilage and potential harmful bacteria.";

                            }
                            else if(predictionClass.equals("Over Ripe"))
                            {
//                                outputTxt.setText("Probability: "+confidence +"\nSweet level: Very sweet, almost syrupy with an intense mango flavor \nTips:\nUse these in recipes like jams, chutneys, or smoothies where their mushy texture " +
//                                        "and intense sweetness can be an advantage. However, if they have a fermented smell or mold, discard them as they are no longer safe to eat.");
//                                outputTxt.setVisibility(View.VISIBLE);
                                outputMessage = "Probability: "+confidence +"\nSweet level: Very sweet, almost syrupy with an intense mango flavor \nTips:\nUse these in recipes like jams, chutneys, or smoothies where their mushy texture " +
                                        "and intense sweetness can be an advantage. However, if they have a fermented smell or mold, discard them as they are no longer safe to eat.";

                            }
                            else if(confidence >= 0.6)
                            {
                                // Display the filtered result
//                                String result = "Class: " + predictionClass + "\nConfidence: " + confidence;
//                                Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
//                                outputTxt.setText(result);
//                                outputTxt.setVisibility(View.VISIBLE);
                                outputMessage = "Class: " + predictionClass + "\nConfidence: " + confidence;

                            }
                            else
                            {
//                                outputTxt.setText("The image provided is less likely identified as carabao mango.");
//                                outputTxt.setVisibility(View.VISIBLE);
                                outputMessage = "The image provided is less likely identified as carabao mango.";

                            }

                            showRateDialog();
                            intent.putExtra("output", outputMessage); // Put the string in the Intent
                            startActivity(intent); // Start the activity
//                            if(detectionCounter == 1){
//                                showRateDialog();
//                            }else{
//                                detectionCounter++;
//                            }

                        } else
                        {
                            Toast.makeText(MainActivity.this, "No predictions found", Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error parsing response", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to get a response", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Dismiss the progress dialog when an error occurs
                progressDialog.dismiss();
                stopTipRotation(); // Stop rotating tips

                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }
}
