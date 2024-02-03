package com.dcq.quotesapp;


import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FavoriteListActivity extends AppCompatActivity {
    RelativeLayout NoQuotes;
    List<FavoriteList> favoriteLists = MainActivity.favoriteDatabase.favoriteDao().getFavoriteData();
    private RecyclerView rv;
    private FavoriteAdapter adapter;
    private ArrayList<FavoriteList> imageArry = new ArrayList<FavoriteList>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Favourite");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        NoQuotes = findViewById(R.id.NoQuotes);

        rv = (RecyclerView) findViewById(R.id.rec);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));

        getFavData();
    }

    private void getFavData() {


        for (FavoriteList cn : favoriteLists) {

            imageArry.add(cn);
        }

        if (imageArry.isEmpty()) {

            NoQuotes.setVisibility(View.VISIBLE);

        }

        adapter = new FavoriteAdapter(favoriteLists, getApplicationContext());
        rv.setAdapter(adapter);
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
