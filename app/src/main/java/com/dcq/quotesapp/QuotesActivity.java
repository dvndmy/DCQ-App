package com.dcq.quotesapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dcq.quotesapp.adapters.WallpapersAdapter;
import com.dcq.quotesapp.models.Quote;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

public class QuotesActivity extends AppCompatActivity {

    // Firebase Database URL
    private static final String FIREBASE_DATABASE_URL = "https://dailycatholicquotes-2ef12-default-rtdb.europe-west1.firebasedatabase.app/";

    private List<Quote> categoryQuoteList;
    private RecyclerView recyclerView;
    private WallpapersAdapter adapter;
    private AVLoadingIndicatorView progressBar;
    private DatabaseReference dbQuotes;
    private String category;
    private String categoryNum = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotes);

        // Display a toast to guide the user
        Toast.makeText(this, "Tap to Change Background", Toast.LENGTH_LONG).show();

        // Retrieve category information from Intent
        Intent intent = getIntent();
        category = intent.getStringExtra("category");

        // Map category names to category numbers
        mapCategoryNumbers();

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(category);

        // Enable the back button in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize lists and RecyclerView
        categoryQuoteList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WallpapersAdapter(this, categoryQuoteList);
        recyclerView.setAdapter(adapter);

        // Initialize progress bar
        progressBar = findViewById(R.id.progressbar);

        // Initialize Firebase Database reference
        dbQuotes = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("quotes");

        // Fetch quotes based on the selected category
        fetchQuotes(categoryNum);
    }

    // Map category names to category numbers
    private void mapCategoryNumbers() {
        if (category.equals(getString(R.string.category_uncategorised))) {
            categoryNum = "0";
        } else if (category.equals(getString(R.string.category_catholic_people))) {
            categoryNum = "1";
        } else if (category.equals(getString(R.string.category_bible_verses))) {
            categoryNum = "2";
        } else if (category.equals(getString(R.string.category_user_added))) {
            categoryNum = "3";
        }
    }

    // Fetch quotes from Firebase Database
    private void fetchQuotes(final String category) {
        progressBar.setVisibility(View.VISIBLE);
        dbQuotes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                if (dataSnapshot.exists()) {
                    // Loop through quotes in the database
                    for (DataSnapshot quoteSnapshot : dataSnapshot.getChildren()) {
                        // Deserialize Quote object from the database
                        Quote quote = quoteSnapshot.getValue(Quote.class);

                        // Filter quotes based on the selected category
                        if (quote != null && (quote.getQuote_category().equals(category) || (category.equals("3") && "1".equals(quote.getUserAdded())))) {
                            categoryQuoteList.add(quote);
                        }
                    }

                    // Notify the adapter that the data set has changed
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error, log, or notify the user
                Log.e("QuotesActivity", "Database Error: " + databaseError.getMessage());
            }
        });
    }

    // Handle the back button in the toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Close this activity and return to the previous one
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
