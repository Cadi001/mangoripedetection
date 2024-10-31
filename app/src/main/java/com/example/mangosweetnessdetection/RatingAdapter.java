package com.example.mangosweetnessdetection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.RatingViewHolder> {

    private List<RatingModel> ratingList;

    public RatingAdapter(List<RatingModel> ratingList) {
        this.ratingList = ratingList;
    }

    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rating, parent, false);
        return new RatingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        RatingModel rating = ratingList.get(position);
        holder.ratingBar.setRating(rating.getRating());
        holder.feedbackTextView.setText(rating.getFeedback());
        holder.usernameTextView.setText(rating.getUsername()); // Bind username
    }

    @Override
    public int getItemCount() {
        return ratingList.size();
    }

    static class RatingViewHolder extends RecyclerView.ViewHolder {
        RatingBar ratingBar;
        TextView feedbackTextView;
        TextView usernameTextView; // Add username TextView

        RatingViewHolder(View itemView) {
            super(itemView);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            feedbackTextView = itemView.findViewById(R.id.feedbackTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView); // Initialize username TextView
        }
    }
}
