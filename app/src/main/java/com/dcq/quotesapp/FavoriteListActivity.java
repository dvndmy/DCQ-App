package com.dcq.quotesapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FavoriteListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    RelativeLayout NoQuotes;
    List<FavoriteList> favoriteLists = MainActivity.favoriteDatabase.favoriteDao().getFavoriteData();
    private RecyclerView rv;
    private FavoriteAdapter adapter;
    private final ArrayList<FavoriteList> imageArry = new ArrayList<FavoriteList>();
    private SearchView searchView;
    private Toolbar toolbar;
    private boolean isSearchVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Liked Quotes");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        NoQuotes = findViewById(R.id.NoQuotes);

        rv = findViewById(R.id.rec);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));

        getFavData();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_quotes, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_search) {
            isSearchVisible = true;
            searchView.setVisibility(View.VISIBLE);
            toolbar.setTitle("");
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    // Method to filter the list based on search query
    private void filter(String query) {
        ArrayList<FavoriteList> filteredList = new ArrayList<>();

        for (FavoriteList favorite : imageArry) {
            if ((favorite.getName().toLowerCase() + "" + favorite.getPerson().toLowerCase()).contains(query.toLowerCase())) {
                filteredList.add(favorite);
            }
        }

        adapter.filterList(filteredList);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filter(newText);
        return true;
    }

}
