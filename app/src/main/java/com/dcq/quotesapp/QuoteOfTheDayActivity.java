package com.dcq.quotesapp;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class QuoteOfTheDayActivity extends AppCompatActivity {
    private final int REQUEST_STORAGE_PERMISSION_CODE = 1;

    TextView quoteOfTheDay, quoteOfTheDayAuthor, categoryTextView;
    TextView tv_quotes_watermark, saveButton, likeText;
    LinearLayout layout_quote_header;
    String categorytext, quoteperson, quotes;
    int quoteid;
    ImageView imgIcon, iv_save_quote, backgroundImageView;
    Context context;
    LinearLayout saveLayout, copyLayout, shareLayout;
    RelativeLayout quoteContainerLayout;
    LikeButton likeButton;
    ArrayList<String> preloadedImageUrls;
    private UnsplashApi unsplashApiService;
    private DatabaseReference dbCategories, quote;
    private final int STORAGE_PERMISSION_CODE = 1;
    private String[] images;
    private int imagesIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_of_the_day);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.quote_of_the_day);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.quote_of_the_day);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        initialiseViews();


        quote = FirebaseDatabase.getInstance("https://dailycatholicquotes-2ef12-default-rtdb.europe-west1.firebasedatabase.app").getReference("qotd");
        quote.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("jjjjjjjjjjjjjjjjjj: ", "> " + dataSnapshot);
                if (dataSnapshot.exists()) {
                    try {
                        quotes = dataSnapshot.child("quote").getValue(String.class);
                        quoteperson = dataSnapshot.child("author").getValue(String.class);
                        //quoteOfTheDay.setText(quotes + "\n\n" + quoteperson);
                        if (quotes != null && quoteperson != null) {
                            quoteOfTheDay.setText(quotes);
                            quoteOfTheDayAuthor.setText(String.format(quoteperson));
                        } else {
                            Toast.makeText(QuoteOfTheDayActivity.this, "Quote not found.", Toast.LENGTH_SHORT).show();
                        }
                        fetchPhotosFromUnsplash((quotes));

                    } catch (Exception e) {
                        // Handle database operation errors
                        Log.e("FetchQuotes", "Error fetching quotes: " + e.getMessage());
                        Toast.makeText(QuoteOfTheDayActivity.this, "Failed to fetch quotes. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle empty data snapshot or missing data
                    Log.e("FetchQuotes", "Data snapshot does not exist or is empty.");
                    Toast.makeText(QuoteOfTheDayActivity.this, "No quotes found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error, log, or notify the user
                Log.e("FetchQuotes", "Database Error: " + databaseError.getMessage());
                Toast.makeText(QuoteOfTheDayActivity.this, "Failed to fetch quotes. Please try again later.", Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void initialiseViews() {
        quoteOfTheDay = findViewById(R.id.txtQuote);
        quoteOfTheDayAuthor = findViewById(R.id.txtAuthor);
        Typeface fontQuote = Typeface.createFromAsset(getAssets(),
                "fonts/montserrat_bold.ttf");
        imagesIndex = 0;

        copyLayout = findViewById(R.id.ll_copy_quote);
        saveLayout = findViewById(R.id.ll_quote_save);
        shareLayout = findViewById(R.id.ll_quote_share);
        quoteContainerLayout = findViewById(R.id.llBackground);
        iv_save_quote = findViewById(R.id.iv_save_quote);
        saveButton = findViewById(R.id.tv_save_quote);
        tv_quotes_watermark = findViewById(R.id.tv_quotes_watermark);
        quoteOfTheDay.setTypeface(fontQuote);
        categoryTextView = findViewById(R.id.txtCategory);
        categoryTextView.setText(R.string.dailycatholicqu);
        imgIcon = findViewById(R.id.imgIcon);
        backgroundImageView = findViewById(R.id.imageView2);
        likeButton = findViewById(R.id.favBtn);
        layout_quote_header = findViewById(R.id.layout_quote_header);
        layout_quote_header.setVisibility(View.VISIBLE);
        preloadedImageUrls = new ArrayList<>();
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.unsplash.com/").addConverterFactory(GsonConverterFactory.create()).build();
        unsplashApiService = retrofit.create(UnsplashApi.class);

        likeButton.setLiked(MainActivity.favoriteDatabase.favoriteDao().isFavorite(quoteid) == 1);

        likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                toggleFavorite(true);
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                toggleFavorite(false);
            }
        });

        // Click listeners
        quoteContainerLayout.setOnClickListener(v -> changeBackgroundImage());
        saveLayout.setOnClickListener(v -> handleSaveButtonClick());
        copyLayout.setOnClickListener(v -> copyQuoteToClipboard());
        shareLayout.setOnClickListener(v -> showShareOptionsMenu());
    }

    // Helper method to toggle favorite
    private void toggleFavorite(boolean isLiked) {
        //Toast.makeText(this, "Liked = "+isLiked, Toast.LENGTH_SHORT).show();
        FavoriteList favoriteList = new FavoriteList();
        favoriteList.setId(quoteid);
        favoriteList.setName(quotes);
        favoriteList.setPerson(quoteperson);

        if (isLiked) {
            MainActivity.favoriteDatabase.favoriteDao().addData(favoriteList);
        } else {
            MainActivity.favoriteDatabase.favoriteDao().delete(favoriteList);
        }
    }

    private void handleSaveButtonClick() {
        if (ContextCompat.checkSelfPermission(QuoteOfTheDayActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            saveImageToStorage();
        } else {
            requestStoragePermission();
        }
    }

    private void copyQuoteToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", "\"" + quotes + "\"\n -" + quoteperson);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Quote Copied to Clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    private void showShareOptionsMenu() {
        PopupMenu popup = new PopupMenu(this, shareLayout);
        popup.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.sub_text) {
                shareAsText();
                return true;
            } else if (menuItem.getItemId() == R.id.sub_image) {
                shareAsImage();
                return true;
            }
            return false;
        });
        popup.inflate(R.menu.menu_item);
        popup.show();
    }

    private void shareAsText() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "\"" + quotes + "\"\n -" + quoteperson);
        startActivity(Intent.createChooser(shareIntent, "Share Quote"));
    }

    private void shareAsImage() {
        showWatermark();

        int width = quoteContainerLayout.getWidth();
        int height = quoteContainerLayout.getHeight();

        if (width <= 0 || height <= 0) {
            Toast.makeText(this, "Error: Layout size is invalid", Toast.LENGTH_SHORT).show();
            hideWatermark();
            return;
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        quoteContainerLayout.draw(canvas);

        Uri imageUri = getLocalBitmapUri(bitmap);
        if (imageUri != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            intent.putExtra(Intent.EXTRA_TEXT, "\"" + quotes + "\"\n -" + quoteperson);
            startActivity(Intent.createChooser(intent, "Share Quote"));
        } else {
            Toast.makeText(this, "Failed to generate image", Toast.LENGTH_SHORT).show();
        }

        hideWatermark();
    }


    private void showWatermark() {
        tv_quotes_watermark.setVisibility(View.VISIBLE);
    }

    private void hideWatermark() {
        tv_quotes_watermark.setVisibility(View.INVISIBLE);
    }


    private void saveImageToStorage() {
        tv_quotes_watermark.setVisibility(View.VISIBLE);
        Bitmap bitmap = createBitmapFromView(quoteContainerLayout);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveImageToScopedStorage(bitmap);
            } else {
                saveImageToLegacyStorage(bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(QuoteOfTheDayActivity.this, "Failed to save image. Please try again later.", Toast.LENGTH_SHORT).show();
        } finally {
            tv_quotes_watermark.setVisibility(View.INVISIBLE);
        }
    }

    // Extracted method to create a bitmap from a view
    private Bitmap createBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    // For Android 10+ (Scoped Storage)
    private void saveImageToScopedStorage(Bitmap bitmap) throws IOException {
        ContentResolver resolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LatestQuotes");

        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (imageUri != null) {
            try (OutputStream fos = resolver.openOutputStream(imageUri)) {
                if (fos != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                }
            }
            notifySaveSuccess();
        } else {
            throw new IOException("Failed to create image file");
        }
    }

    // For Android 9 and below (Legacy Storage)
    private void saveImageToLegacyStorage(Bitmap bitmap) throws IOException {
        File directory = new File(Environment.getExternalStorageDirectory(), "/Latest Quotes");
        if (!directory.exists()) {
            directory.mkdir();
        }

        String filename = System.currentTimeMillis() + ".jpg";
        File outFile = new File(directory, filename);

        try (FileOutputStream outputStream = new FileOutputStream(outFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
        }

        // Notify gallery to scan the saved image
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(outFile));
        sendBroadcast(intent);

        notifySaveSuccess();
    }

    // Notify user and update UI
    private void notifySaveSuccess() {
        runOnUiThread(() -> {
            Toast.makeText(QuoteOfTheDayActivity.this, "Image saved successfully", Toast.LENGTH_SHORT).show();
            saveButton.setText("Saved");
            iv_save_quote.setImageResource(R.drawable.ic_menu_check);
        });
    }

    // Updated Permission Request Method
    private void requestStoragePermission() {
        String permission = (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) ? Manifest.permission.WRITE_EXTERNAL_STORAGE : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            new AlertDialog.Builder(this).setTitle("Permission Needed").setMessage("Storage permission is required to save images.").setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_STORAGE_PERMISSION_CODE)).setNegativeButton("Cancel", (dialog, which) -> Toast.makeText(this, "Permission denied. Some features may not work.", Toast.LENGTH_SHORT).show()).create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_STORAGE_PERMISSION_CODE);
        }
    }


    private void preloadedImages() {// Define the list of URLs

        List<String> defaultImageUrls = Arrays.asList("https://images.unsplash.com/32/Mc8kW4x9Q3aRR3RkP5Im_IMG_4417.jpg?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80", "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80", "https://images.unsplash.com/photo-1531685250784-7569952593d2?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80", "https://images.unsplash.com/photo-1531315630201-bb15abeb1653?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=435&q=80", "https://images.unsplash.com/photo-1528459801416-a9e53bbf4e17?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=412&q=80", "https://images.unsplash.com/photo-1522441815192-d9f04eb0615c?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=327&q=80", "https://images.unsplash.com/photo-1558591710-4b4a1ae0f04d?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80", "https://images.unsplash.com/photo-1487147264018-f937fba0c817?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80", "https://images.unsplash.com/photo-1476820865390-c52aeebb9891?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=870&q=80", "https://images.unsplash.com/photo-1516617442634-75371039cb3a?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80", "https://images.unsplash.com/photo-1489549132488-d00b7eee80f1?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80", "https://images.unsplash.com/photo-1497250681960-ef046c08a56e?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80", "https://images.unsplash.com/photo-1436397543931-01c4a5162bdb?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=580&q=80", "https://images.unsplash.com/photo-1513366208864-87536b8bd7b4?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80", "https://images.unsplash.com/photo-1507608158173-1dcec673a2e5?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80", "https://images.unsplash.com/photo-1519751138087-5bf79df62d5b?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80", "https://images.unsplash.com/photo-1483232539664-d89822fb5d3e?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=464&q=80", "https://images.unsplash.com/photo-1508615039623-a25605d2b022?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80", "https://images.unsplash.com/photo-1487088678257-3a541e6e3922?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80", "https://images.unsplash.com/photo-1537420327992-d6e192287183?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=388&q=80", "https://images.unsplash.com/photo-1458682625221-3a45f8a844c7?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80");

// Add the new images to preloadedImageUrls
        preloadedImageUrls.addAll(defaultImageUrls);
    }

    public void fetchPhotosFromUnsplash(String query) {
        Call<ApiResponse> call = unsplashApiService.searchPhotos(query, "TWpsWTMSvOs4W7pH6J713NkqRh9jmXyZrIHrpmFpW-I");
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    logApiError(response);
                    return;
                }

                List<Result> results = response.body().getResults();
                if (results == null || results.isEmpty()) {
                    Log.e("SearchPhotos", "No results found.");
                    showToast("No photos found.");
                    return;
                }

                // Use Set to prevent duplicates
                Set<String> uniqueUrls = new LinkedHashSet<>();
                for (Result result : results) {
                    if (result.getUrls() != null && result.getUrls().getSmall() != null) {
                        uniqueUrls.add(result.getUrls().getSmall());
                    }
                }

                preloadedImageUrls = new ArrayList<>(uniqueUrls);
                runOnUiThread(() -> preloadedImages()); // Update UI safely
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("SearchPhotos", "Error fetching photos: " + t.getMessage());
                showToast("Failed to fetch photos. Check your internet connection.");
            }
        });
    }

    // Helper method to log API errors
    private void logApiError(Response<ApiResponse> response) {
        try {
            String errorMessage = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e("SearchPhotos", "API Error: " + response.code() + " - " + errorMessage);
        } catch (IOException e) {
            Log.e("SearchPhotos", "Error reading errorBody", e);
        }
        showToast("Failed to fetch photos. Please try again later.");
    }

    // Helper method to show a toast
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(QuoteOfTheDayActivity.this, message, Toast.LENGTH_SHORT).show());
    }


    private void changeBackgroundImage() {
        if (preloadedImageUrls == null || preloadedImageUrls.isEmpty()) {
            Log.e("changeBackgroundImage", "No images available to set as background.");
            return; // Prevent crashes due to empty list
        }

        // Load image using Glide
        Glide.with(this).load(preloadedImageUrls.get(imagesIndex)).placeholder(R.drawable.gradient_black) // Placeholder while loading
                .error(R.drawable.img6) // Error fallback
                .into(backgroundImageView);

        // Update index for next background image
        imagesIndex = (imagesIndex + 1) % preloadedImageUrls.size();
    }

    private Uri getLocalBitmapUriNew(Bitmap bitmap) {
        File cachePath = new File(getCacheDir(), "images");
        try {
            cachePath.mkdirs();
            File file = new File(cachePath, "Daily Catholic Quotes" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Uri getLocalBitmapUri(Bitmap bitmap) {
        Uri bmpUri = null;
        try {
            File file = new File(QuoteOfTheDayActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Daily Catholic Quotes" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            bmpUri = FileProvider.getUriForFile(QuoteOfTheDayActivity.this, com.wang.avi.BuildConfig.APPLICATION_ID + ".provider", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    //Permisssion for save images
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // ✅ Permission granted, proceed with saving the image
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                saveImageToStorage();  // Call the function directly
            } else {
                // ❌ Permission denied
                boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);

                if (!showRationale) {
                    // User selected "Don't Ask Again"
                    new AlertDialog.Builder(this).setTitle("Permission Denied").setMessage("Storage permission is required to save images. Please enable it in app settings.").setPositiveButton("Go to Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).show();
                } else {
                    Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


}
