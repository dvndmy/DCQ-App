package com.dcq.quotesapp;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;
import androidx.viewpager.widget.ViewPager;

import com.dcq.quotesapp.adapters.TabAdapter;
import com.google.android.gms.ads.AdView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.onesignal.OneSignal;

public class MainActivity extends AppCompatActivity {

    public static FavoriteDatabase favoriteDatabase;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    RelativeLayout relativeLayout;
    TabLayout tabLayout;
    ViewPager viewPager;
    private AlertDialog dialog;
    private AdView mAdView;
    private LinearLayout ll_liked_quotes, ll_todays_quote, ll_sounds, ll_about, ll_contact_us, ll_rate_app, ll_share_app, ll_like_fb, ll_follow_insta, ll_privacy_policy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        relativeLayout = findViewById(R.id.rootPager);
        relativeLayout.setVisibility(View.VISIBLE);


        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        favoriteDatabase = Room.databaseBuilder(getApplicationContext(), FavoriteDatabase.class, "myfavdb").allowMainThreadQueries().build();

        //Custom Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();


        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        //main Fragment
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        tabLayout.addTab(tabLayout.newTab().setText("Quotes"));
        tabLayout.addTab(tabLayout.newTab().setText("Add Quote"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final TabAdapter adapter = new TabAdapter(this, getSupportFragmentManager(),
                tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        //Drawer Menu Click Listner
        ll_liked_quotes = findViewById(R.id.ll_liked_quotes);
        ll_todays_quote = findViewById(R.id.ll_todays_quote);
        ll_sounds = findViewById(R.id.ll_sounds);
        ll_about = findViewById(R.id.ll_faqs);
        ll_contact_us = findViewById(R.id.ll_contact_us);
        ll_rate_app = findViewById(R.id.ll_rate_app);
        ll_share_app = findViewById(R.id.ll_share_app);
        ll_like_fb = findViewById(R.id.ll_like_fb);
        ll_follow_insta = findViewById(R.id.ll_follow_insta);
        ll_privacy_policy = findViewById(R.id.ll_privacy_policy);

        //show your favorite quotes
        ll_liked_quotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, FavoriteListActivity.class));
            }
        });

        //quote of the day
        ll_todays_quote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, QuoteOfTheDayActivity.class));
            }
        });


        //about my app
        ll_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
            }
        });


        //like my facebook page
        ll_like_fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.facebook.com/" + Config.usernameFacebook));
                startActivity(browserIntent);
            }
        });

        //follow on instagram
        ll_follow_insta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.instagram.com/" + Config.usernameInstagram));
                startActivity(browserIntent);
            }
        });


    }


    private void showAboutDialog() {
        final Dialog dialog = new Dialog(MainActivity.this, R.style.DialogCustomTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.layout_about);

        Button dialog_btn = dialog.findViewById(R.id.btn_done);
        dialog_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, FavoriteListActivity.class));

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showExit() {

        final Dialog customDialog;
        LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
        View customView = inflater.inflate(R.layout.layout_exit, null);
        customDialog = new Dialog(this, R.style.CustomDialog);
        customDialog.setContentView(customView);
        TextView no = customDialog.findViewById(R.id.tv_no);
        TextView yes = customDialog.findViewById(R.id.tv_yes);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Toast.makeText(MainActivity.this, "Exit", Toast.LENGTH_SHORT).show();
            }
        });


        customDialog.show();
    }

    @Override
    public void onBackPressed() {
        showExit();
    }
}
