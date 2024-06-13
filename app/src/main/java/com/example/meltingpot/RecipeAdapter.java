package com.example.meltingpot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RecipeAdapter extends BaseAdapter {

    private Context context;
    private List<Recipe> recipeList;
    private OnRecipeClickListener listener;

    public RecipeAdapter(Context context, List<Recipe> recipeList, OnRecipeClickListener listener) {
        this.context = context;
        this.recipeList = recipeList;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return recipeList.size();
    }

    @Override
    public Object getItem(int position) {
        return recipeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.recipe_item, parent, false);
            holder = new ViewHolder();
            holder.recipeImageView = convertView.findViewById(R.id.recipeImageView);
            holder.recipeNameTextView = convertView.findViewById(R.id.recipeNameTextView);
            holder.userNameTextView = convertView.findViewById(R.id.userNameTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Recipe recipe = recipeList.get(position);

        holder.recipeNameTextView.setText(recipe.getName());
        holder.userNameTextView.setText(recipe.getUserName());


        Glide.with(context)
                .load(recipe.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.recipeImageView);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRecipeClick(recipe);
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        ImageView recipeImageView;
        TextView recipeNameTextView;
        TextView userNameTextView;
    }

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }
}
