package com.dcq.quotesapp;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.wang.avi.BuildConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewQuoteActivity extends AppCompatActivity {
    private static final String FIREBASE_DATABASE_URL = "https://dailycatholicquotes-2ef12-default-rtdb.europe-west1.firebasedatabase.app/";

    private final int REQUEST_STORAGE_PERMISSION_CODE = 1;
    TextView tv_quotes_watermark, saveButton, likeText;
    LinearLayout layout_quote_header;
    String quoteperson, quotes;
    int quoteid;
    ImageView imgIcon, iv_save_quote, backgroundImageView;
    ArrayList<String> preloadedImageUrls;
    LinearLayout saveLayout, copyLayout, shareLayout;
    RelativeLayout quoteContainerLayout;
    LikeButton likeButton;
    TextView quoteTextView, categoryTextView;
    private UnsplashApi unsplashApiService;
    private DatabaseReference dbQuotes;
    private String[] images;
    private int imagesIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_quote);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Your Custom Quote");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        quoteTextView = findViewById(R.id.txtQuote);
        Typeface fontQuote = Typeface.createFromAsset(getAssets(), "fonts/montserrat_bold.ttf");
        imagesIndex = 0;

        copyLayout = findViewById(R.id.ll_copy_quote);
        saveLayout = findViewById(R.id.ll_quote_save);
        shareLayout = findViewById(R.id.ll_quote_share);
        quoteContainerLayout = findViewById(R.id.llBackground);
        iv_save_quote = findViewById(R.id.iv_save_quote);
        saveButton = findViewById(R.id.tv_save_quote);
        tv_quotes_watermark = findViewById(R.id.tv_quotes_watermark);
        quoteTextView.setTypeface(fontQuote);
        categoryTextView = findViewById(R.id.txtCategory);
        imgIcon = findViewById(R.id.imgIcon);
        backgroundImageView = findViewById(R.id.imageView2);
        likeButton = findViewById(R.id.favBtn);
        layout_quote_header = findViewById(R.id.layout_quote_header);
        layout_quote_header.setVisibility(View.VISIBLE);
        preloadedImageUrls = new ArrayList<>();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.unsplash.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        unsplashApiService = retrofit.create(UnsplashApi.class);
        SharedPreferences sharedPref = getSharedPreferences("search", MODE_PRIVATE);
        String searchTerm = sharedPref.getString("searchterm", "");
        SharedPreferences sharedPreferences = getSharedPreferences("dbmax", MODE_PRIVATE);
        String no = sharedPreferences.getString("max", "3000");
        // Fetch quotes
        dbQuotes = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("quotes").child(no);
        fetchQuotes(searchTerm);


        //quote = FirebaseDatabase.getInstance("https://dailycatholicquotes-2ef12-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("quotes").child(no);
        likeButton.setLiked(MainActivity.favoriteDatabase.favoriteDao().isFavorite(quoteid) == 1);

        likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                FavoriteList favoriteList = new FavoriteList();
                int id = quoteid;
                String name = quotes;
                String person = quoteperson;
                favoriteList.setId(id);
                favoriteList.setName(name);
                favoriteList.setPerson(person);

                if (MainActivity.favoriteDatabase.favoriteDao().isFavorite(id) != 1) {
                    NewQuoteActivity.this.likeButton.setLiked(true);
                    MainActivity.favoriteDatabase.favoriteDao().addData(favoriteList);

                } else {
                    NewQuoteActivity.this.likeButton.setLiked(false);
                    MainActivity.favoriteDatabase.favoriteDao().delete(favoriteList);
                }
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                FavoriteList favoriteList = new FavoriteList();
                int id = quoteid;
                String name = quotes;
                String person = quoteperson;
                favoriteList.setId(id);
                favoriteList.setName(name);
                favoriteList.setPerson(person);

                if (MainActivity.favoriteDatabase.favoriteDao().isFavorite(id) != 1) {
                    NewQuoteActivity.this.likeButton.setLiked(true);
                    MainActivity.favoriteDatabase.favoriteDao().addData(favoriteList);

                } else {
                    NewQuoteActivity.this.likeButton.setLiked(false);
                    MainActivity.favoriteDatabase.favoriteDao().delete(favoriteList);
                }
            }
        });

        quoteContainerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBackgroundImage();
            }
        });

        saveLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSaveButtonClick();
            }

            private void handleSaveButtonClick() {
                if (ContextCompat.checkSelfPermission(NewQuoteActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveImageToStorage();
                } else {
                    requestStoragePermission();
                }
            }

            private void saveImageToStorage() {
                try {
                    tv_quotes_watermark.setVisibility(View.VISIBLE);
                    Bitmap bitmap = Bitmap.createBitmap(quoteContainerLayout.getWidth(), quoteContainerLayout.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    quoteContainerLayout.draw(canvas);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentResolver resolver = NewQuoteActivity.this.getContentResolver();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

                        if (imageUri != null) {
                            try (OutputStream fos = resolver.openOutputStream(imageUri)) {
                                if (fos != null) {
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                    fos.flush();
                                }
                                Toast.makeText(NewQuoteActivity.this, "File Saved", Toast.LENGTH_SHORT).show();
                                saveButton.setText("Saved");
                                iv_save_quote.setImageResource(R.drawable.ic_menu_check);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            throw new IOException("Failed to create image file");
                        }
                        tv_quotes_watermark.setVisibility(View.INVISIBLE);
                    } else {
                        FileOutputStream outputStream = null;
                        try {
                            File sdCard = Environment.getExternalStorageDirectory();
                            File directory = new File(sdCard.getAbsolutePath() + "/Latest Quotes");
                            directory.mkdir();
                            String filename = String.format("%d.jpg", System.currentTimeMillis());
                            File outFile = new File(directory, filename);

                            outputStream = new FileOutputStream(outFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                            outputStream.flush();

                            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            intent.setData(Uri.fromFile(outFile));
                            NewQuoteActivity.this.sendBroadcast(intent);

                            Toast.makeText(NewQuoteActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                            saveButton.setText("Saved");
                            iv_save_quote.setImageResource(R.drawable.ic_menu_check);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(NewQuoteActivity.this, "Failed to save image. Please try again later.", Toast.LENGTH_SHORT).show();

                        } finally {
                            if (outputStream != null) {
                                try {
                                    outputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        tv_quotes_watermark.setVisibility(View.INVISIBLE);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle IOException
                    Toast.makeText(NewQuoteActivity.this, "Failed to save image. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }

            private void requestStoragePermission() {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) NewQuoteActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Explain why the permission is needed
                    new AlertDialog.Builder(NewQuoteActivity.this)
                            .setTitle("Permission needed")
                            .setMessage("This permission is needed to save images.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Request the permission again
                                    ActivityCompat.requestPermissions((Activity) NewQuoteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION_CODE);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Handle permission denial
                                    Toast.makeText(NewQuoteActivity.this, "Permission denied. Some features may not work.", Toast.LENGTH_SHORT).show();
                                }
                            }).create().show();
                } else {
                    // Request the permission for the first time
                    ActivityCompat.requestPermissions((Activity) NewQuoteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION_CODE);
                }
            }
        });

        copyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyQuoteToClipboard();
            }

            private void copyQuoteToClipboard() {
                ClipboardManager clipboard = (ClipboardManager) NewQuoteActivity.this.getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", "\"" + quotes + "\"\n -" + quoteperson);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(NewQuoteActivity.this, "Quote Copied to Clipboard", Toast.LENGTH_SHORT).show();
                }
            }
        });

        shareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShareOptionsMenu();
            }

            private void showShareOptionsMenu() {
                PopupMenu popup = new PopupMenu(NewQuoteActivity.this, shareLayout);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.sub_text:
                                shareAsText();
                                return true;
                            case R.id.sub_image:
                                shareAsImage();
                                return true;
                        }
                        return false;
                    }

                    private void shareAsText() {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "\"" + quotes + "\"\n -" + quoteperson);
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Daily Catholic Quotes");
                        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        NewQuoteActivity.this.startActivity(Intent.createChooser(shareIntent, "Share Quote").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        Toast.makeText(NewQuoteActivity.this, "Share as Text", Toast.LENGTH_SHORT).show();
                    }

                    private void shareAsImage() {
                        tv_quotes_watermark.setVisibility(View.VISIBLE);
                        Bitmap bitmap = Bitmap.createBitmap(quoteContainerLayout.getWidth(), quoteContainerLayout.getHeight(),
                                Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        quoteContainerLayout.draw(canvas);
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("*/*");
                        intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap));
                        intent.putExtra(Intent.EXTRA_TEXT, "\"" + quotes + "\"\n -" + quoteperson);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        NewQuoteActivity.this.startActivity(Intent.createChooser(intent, "Daily Catholic Quotes").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        tv_quotes_watermark.setVisibility(View.INVISIBLE);
                        Toast.makeText(NewQuoteActivity.this, "Share as Image", Toast.LENGTH_SHORT).show();

                    }
                });
                popup.inflate(R.menu.menu_item);
                popup.show();
            }
        });
    }


    private void preloadedImages() {
        preloadedImageUrls.add("https://images.unsplash.com/32/Mc8kW4x9Q3aRR3RkP5Im_IMG_4417.jpg?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1531685250784-7569952593d2?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1531315630201-bb15abeb1653?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=435&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1528459801416-a9e53bbf4e17?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=412&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1522441815192-d9f04eb0615c?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=327&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1558591710-4b4a1ae0f04d?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1487147264018-f937fba0c817?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1476820865390-c52aeebb9891?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=870&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1516617442634-75371039cb3a?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1489549132488-d00b7eee80f1?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1497250681960-ef046c08a56e?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1436397543931-01c4a5162bdb?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=580&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1513366208864-87536b8bd7b4?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1507608158173-1dcec673a2e5?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1519751138087-5bf79df62d5b?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1483232539664-d89822fb5d3e?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=464&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1508615039623-a25605d2b022?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1487088678257-3a541e6e3922?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1537420327992-d6e192287183?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=388&q=80");
        preloadedImageUrls.add("https://images.unsplash.com/photo-1458682625221-3a45f8a844c7?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80");

    }

    public void fetchPhotosFromUnsplash(String query) {
        Call<ApiResponse> call = unsplashApiService.searchPhotos(query, "TWpsWTMSvOs4W7pH6J713NkqRh9jmXyZrIHrpmFpW-I");
        Log.d("eeeeeeeeeee: ", query);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse != null) {
                        List<Result> results = apiResponse.getResults();
                        if (results != null) {
                            preloadedImageUrls = new ArrayList<>();
                            for (Result result : results) {
                                Urls urls = result.getUrls();
                                if (urls != null) {
                                    String imageUrl = urls.getSmall();
                                    if (imageUrl != null) {
                                        preloadedImageUrls.add(0, imageUrl);
                                    }
                                }
                            }
                            preloadedImages();
                        } else {
                            // Handle empty results
                            Log.e("SearchPhotos", "No results found.");
                            Toast.makeText(NewQuoteActivity.this, "No photos found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle null response body
                        Log.e("SearchPhotos", "Null response body.");
                        Toast.makeText(NewQuoteActivity.this, "Failed to fetch photos. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle unsuccessful response
                    Log.e("SearchPhotos", "Unsuccessful response: " + response.message());
                    Toast.makeText(NewQuoteActivity.this, "Failed to fetch photos. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Handle network request failure
                Log.e("SearchPhotos", "Error fetching photos: " + t.getMessage());
                Toast.makeText(NewQuoteActivity.this, "Failed to fetch photos. Please check your internet connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void changeBackgroundImage() {

        int numOfImages = preloadedImageUrls.size();
        images = new String[numOfImages];
        for (int i = 0; i < numOfImages; i++) {
            images[i] = preloadedImageUrls.get(i);
        }
        Glide.with(NewQuoteActivity.this).clear(backgroundImageView);
        Glide.with(NewQuoteActivity.this).load(images[imagesIndex]).into(backgroundImageView);

        ++imagesIndex;  // update index, so that next time it points to next resource
        if (imagesIndex == images.length)
            imagesIndex = 0; // if we have reached at last index of array, simply restart from beginning
    }

    private Uri getLocalBitmapUri(Bitmap bitmap) {
        Uri bmpUri = null;
        try {
            File file = new File(NewQuoteActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "Daily Catholic Quotes" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            bmpUri = FileProvider.getUriForFile(NewQuoteActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    // Fetch quotes from Firebase Database
    private void fetchQuotes(String searchterm) {
        dbQuotes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        quotes = dataSnapshot.child("quote").getValue(String.class);
                        quoteperson = dataSnapshot.child("author").getValue(String.class);
                        quoteTextView.setText(quotes + "\n\n" + quoteperson);
                        if (searchterm.isEmpty()) {
                            fetchPhotosFromUnsplash((quotes));
                        } else {
                            fetchPhotosFromUnsplash((searchterm));
                        }
                    } catch (Exception e) {
                        // Handle database operation errors
                        Log.e("FetchQuotes", "Error fetching quotes: " + e.getMessage());
                        Toast.makeText(NewQuoteActivity.this, "Failed to fetch quotes. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle empty data snapshot or missing data
                    Log.e("FetchQuotes", "Data snapshot does not exist or is empty.");
                    Toast.makeText(NewQuoteActivity.this, "No quotes found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error, log, or notify the user
                Log.e("FetchQuotes", "Database Error: " + databaseError.getMessage());
                Toast.makeText(NewQuoteActivity.this, "Failed to fetch quotes. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void deleteQuote(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("dbmax", MODE_PRIVATE);
        String no = sharedPreferences.getString("max", "3000");
        Log.d("qqq7: ", sharedPreferences.getString("max", "lol"));
        no = String.valueOf((Integer.parseInt(no)));
        FirebaseDatabase.getInstance("https://dailycatholicquotes-2ef12-default-rtdb.europe-west1.firebasedatabase.app").getReference("quotes").child(no).removeValue();
        Toast.makeText(NewQuoteActivity.this, "Quote Deleted", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
