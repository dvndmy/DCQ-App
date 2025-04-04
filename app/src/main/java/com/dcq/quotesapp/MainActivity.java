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
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;


public class MainActivity extends AppCompatActivity {

    // Database instance
    public static FavoriteDatabase favoriteDatabase;

    // UI elements
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
    private LinearLayout ll_liked_quotes, ll_todays_quote, ll_sounds, ll_about, ll_contact_us, ll_rate_app, ll_share_app, ll_like_fb, ll_follow_insta, ll_telegram, ll_youtube, ll_twitter, ll_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        // Initialize toolbar and UI elements
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        relativeLayout = findViewById(R.id.rootPager);
        relativeLayout.setVisibility(View.VISIBLE);


        // Initialize Room database
        initializeRoomDatabase();

        // Set up custom drawer
        setupCustomDrawer();

        // Set up main Fragment
        setupMainFragment();

        // Set up Drawer Menu Click Listener
        setupDrawerMenuClickListener();

// Call setReminder method
        NotificationScheduler.setReminder(this, MainActivity.class);
    }



    // Helper method to initialize Room database
    private void initializeRoomDatabase() {
        favoriteDatabase = Room.databaseBuilder(getApplicationContext(), FavoriteDatabase.class, "myfavdb")
                .allowMainThreadQueries()
                .build();
    }

    // Helper method to set up custom drawer
    private void setupCustomDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();
    }

    // Helper method to set up main Fragment
    private void setupMainFragment() {
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
    }

    // Helper method to set up Drawer Menu Click Listener
    private void setupDrawerMenuClickListener() {
        // Find views
        ll_liked_quotes = findViewById(R.id.ll_liked_quotes);
        ll_todays_quote = findViewById(R.id.ll_todays_quote);
        ll_sounds = findViewById(R.id.ll_sounds);
        ll_about = findViewById(R.id.ll_faqs);
        ll_contact_us = findViewById(R.id.ll_contact_us);
        ll_rate_app = findViewById(R.id.ll_rate_app);
        ll_share_app = findViewById(R.id.ll_share_app);
        ll_like_fb = findViewById(R.id.ll_like_fb);
        ll_follow_insta = findViewById(R.id.ll_follow_insta);
        ll_telegram = findViewById(R.id.ll_join_tg);
        ll_youtube = findViewById(R.id.ll_subscribe_youtube);
        ll_twitter = findViewById(R.id.ll_follow_twitter);
        ll_email = findViewById(R.id.ll_send_email);

        // Set click listeners
        ll_liked_quotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, FavoriteListActivity.class));
            }
        });

        ll_todays_quote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, QuoteOfTheDayActivity.class));
            }
        });

        ll_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
            }
        });

        ll_like_fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFacebookPage();
            }
        });

        ll_follow_insta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInstagramPage();
            }
        });

        ll_telegram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTelegramChannel();
            }
        });

        ll_youtube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openYouTubeChannel();
            }
        });

        ll_twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTwitterPage();
            }
        });

        ll_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });
    }

    // Helper method to show the about dialog
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

    // Helper method to open the Facebook page
    private void openFacebookPage() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.facebook.com/" + Config.usernameFacebook));
        startActivity(browserIntent);
    }

    // Helper method to open the Instagram page
    private void openInstagramPage() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.instagram.com/" + Config.usernameInstagram));
        startActivity(browserIntent);
    }

    // Helper method to open the Telegram channel
    private void openTelegramChannel() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://t.me/dailycatholicqu"));
        startActivity(browserIntent);
    }

    // Helper method to open the YouTube channel
    private void openYouTubeChannel() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/" + Config.youtubeChannel));
        startActivity(browserIntent);
    }

    // Helper method to open the Twitter page
    private void openTwitterPage() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://twitter.com/" + Config.twitterUsername));
        startActivity(browserIntent);
    }

    // Helper method to send an email
    private void sendEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + Config.emailAddress));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback / Inquiry");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, FavoriteListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Helper method to show the exit dialog
    private void showExit() {
        // Check if the activity is finishing or is destroyed
        if (!isFinishing()) {
            final Dialog customDialog;
            LayoutInflater inflater = getLayoutInflater();
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
    }


    @Override
    public void onBackPressed() {
        showExit();
    }
}
