package com.example.mangosweetnessdetection;
public class RatingModel {
    private float rating;
    private String feedback;
    private String username; // Add username

    // Empty constructor for Firestore
    public RatingModel() {}

    public RatingModel(float rating, String feedback, String username) {
        this.rating = rating;
        this.feedback = feedback;
        this.username = username;
    }

    public float getRating() {
        return rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getUsername() {
        return username;
    }
}
