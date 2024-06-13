package com.example.meltingpot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class DietFeedAdapter extends RecyclerView.Adapter<DietFeedAdapter.DietViewHolder> {

    private List<Diet> dietList;
    private OnDietClickListener onDietClickListener;

    public interface OnDietClickListener {
        void onDietClick(Diet diet);
    }

    public DietFeedAdapter(List<Diet> dietList, OnDietClickListener onDietClickListener) {
        this.dietList = dietList;
        this.onDietClickListener = onDietClickListener;
    }

    @NonNull
    @Override
    public DietViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item, parent, false);
        return new DietViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DietViewHolder holder, int position) {
        Diet diet = dietList.get(position);
        holder.bind(diet, onDietClickListener);
    }

    @Override
    public int getItemCount() {
        return dietList.size();
    }

    static class DietViewHolder extends RecyclerView.ViewHolder {
        ImageView dietImageView;
        TextView dietTitleTextView;
        TextView dietAuthorTextView;
        TextView dietPriceTextView;

        public DietViewHolder(@NonNull View itemView) {
            super(itemView);
            dietImageView = itemView.findViewById(R.id.recipeImageView);
            dietTitleTextView = itemView.findViewById(R.id.recipeNameTextView);
            dietAuthorTextView = itemView.findViewById(R.id.userNameTextView);
        }

        public void bind(Diet diet, OnDietClickListener onDietClickListener) {
            dietTitleTextView.setText(diet.getName());
            dietAuthorTextView.setText(diet.getUserName());

            Glide.with(itemView.getContext())
                    .load(R.drawable.health_placeholder)
                    .placeholder(R.drawable.health_placeholder)
                    .error(R.drawable.error)
                    .into(dietImageView);

            itemView.setOnClickListener(v -> onDietClickListener.onDietClick(diet));
        }
    }
}
