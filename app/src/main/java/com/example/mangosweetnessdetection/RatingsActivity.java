package com.example.mangosweetnessdetection;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class RatingsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private RatingAdapter ratingAdapter;
    private List<RatingModel> ratingList;
    ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings);

        // Initialize Firestore and RecyclerView
        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerViewRatings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ratingList = new ArrayList<>();
        ratingAdapter = new RatingAdapter(ratingList);
        recyclerView.setAdapter(ratingAdapter);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
        });

        // Fetch ratings from Firestore
        fetchRatingsFromFirestore();
    }

    private void fetchRatingsFromFirestore() {
        db.collection("feedbacks")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Order by timestamp in descending order (newest first)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }

                        ratingList.clear(); // Clear old data
                        for (QueryDocumentSnapshot doc : value) {
                            RatingModel rating = doc.toObject(RatingModel.class);
                            ratingList.add(rating); // Add new items in the order they are fetched (newest first)
                        }
                        ratingAdapter.notifyDataSetChanged(); // Notify adapter of data changes
                        recyclerView.scrollToPosition(0); // Scroll to the top of the list
                    }
                });
    }



}
