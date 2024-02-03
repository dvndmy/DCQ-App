package com.dcq.quotesapp;

import android.content.Intent;
import android.os.Bundle;
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

    List<Quote> wallpaperList;
    List<Quote> favList;
    RecyclerView recyclerView;
    WallpapersAdapter adapter;
    AVLoadingIndicatorView progressBar;
    DatabaseReference dbWallpapers, dbFavs;
    String category;
    String categorynum = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotes);

        Toast.makeText(this, "Tap to Change Background", Toast.LENGTH_LONG).show();

        Intent intent = getIntent();
        category = intent.getStringExtra("category");

        if (category.equals("uncategorised")) {
            categorynum = "0";
        } else if (category.equals("Catholic People")) {
            categorynum = "1";
        } else if (category.equals("Bible Verses")) {
            categorynum = "2";
        }else if (category.equals("User Added")) {
            categorynum = "3";
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("yyy");
        setSupportActionBar(toolbar);
        toolbar.setTitle(category);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        favList = new ArrayList<>();
        wallpaperList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WallpapersAdapter(this, wallpaperList);

        recyclerView.setAdapter(adapter);

        progressBar = findViewById(R.id.progressbar);
        dbWallpapers = FirebaseDatabase.getInstance("https://dailycatholicquotes-2ef12-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        fetchWallpapers(category);
    }
    private void fetchWallpapers(final String category) {
        progressBar.setVisibility(View.VISIBLE);
        dbWallpapers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                if (dataSnapshot.exists()) {
                    for (DataSnapshot wallpaperSnapshot : dataSnapshot.getChildren()) {
                        if (!(wallpaperSnapshot.getKey().equals("categories"))) {
                            Quote quote = wallpaperSnapshot.getValue(Quote.class);
                            favList.add(quote);
                        }
                    }
                    for (int i = 0; i < favList.size(); i++) {
                        if (favList.get(i).getQuote_category().equals(categorynum) || (categorynum.equals("3") && "User Added".equals(favList.get(i).getUrl()))) {
                            wallpaperList.add(favList.get(i));
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}
