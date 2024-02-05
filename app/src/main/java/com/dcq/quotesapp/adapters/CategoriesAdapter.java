package com.dcq.quotesapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dcq.quotesapp.QuotesActivity;
import com.dcq.quotesapp.R;
import com.dcq.quotesapp.models.Category;

import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder> {

    // Constant for passing category data between activities
    public static final String EXTRA_CATEGORY = "category";

    private final Context mCtx;
    private final List<Category> categoryList;

    // Constructor to initialize the adapter with data
    public CategoriesAdapter(Context mCtx, List<Category> categoryList) {
        this.mCtx = mCtx;
        this.categoryList = categoryList;
    }

    // Create a new ViewHolder instance by inflating the layout for each item in the RecyclerView
    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    // Bind data to each item in the RecyclerView
    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        Category c = categoryList.get(position);
        // Set the category name to the corresponding TextView
        holder.textView.setText(c != null ? c.name : "");

        // Load the category thumbnail using Glide library
        Glide.with(mCtx)
                .load(c != null ? c.thumb : null)
                .into(holder.imageView);
    }

    // Return the total number of items in the RecyclerView
    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    // ViewHolder class for each item in the RecyclerView
    class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textView;
        ImageView imageView;

        // Constructor to initialize the ViewHolder
        public CategoryViewHolder(View itemView) {
            super(itemView);

            // Initialize UI elements for each item
            textView = itemView.findViewById(R.id.text_view_cat_name);
            imageView = itemView.findViewById(R.id.image_view);

            // Set click listener on the item view
            itemView.setOnClickListener(this);
        }

        // Handle click events on items in the RecyclerView
        @Override
        public void onClick(View view) {
            // Get the clicked item position
            int position = getAdapterPosition();

            // Check if the position is valid
            if (position != RecyclerView.NO_POSITION) {
                // Retrieve the corresponding Category object
                Category c = categoryList.get(position);

                // Log the clicked category name
                Log.d("category", c != null ? c.name : "null");

                // Create an Intent to start the QuotesActivity with the selected category
                Intent intent = new Intent(mCtx, QuotesActivity.class);
                intent.putExtra(EXTRA_CATEGORY, c != null ? c.name : "");

                // Start the QuotesActivity
                mCtx.startActivity(intent);
            }
        }
    }
}
