package com.dcq.quotesapp.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dcq.quotesapp.BuildConfig;
import com.dcq.quotesapp.FavoriteList;
import com.dcq.quotesapp.MainActivity;
import com.dcq.quotesapp.R;
import com.dcq.quotesapp.models.Quote;
import com.like.LikeButton;
import com.like.OnLikeListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WallpapersAdapter extends RecyclerView.Adapter<WallpapersAdapter.WallpaperViewHolder>
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private final List<Quote> originalList;
    private final Context mCtx;
    private final List<Quote> filteredList;
    private final int STORAGE_PERMISSION_CODE = 1;
    private String[] images;
    private int imagesIndex = 0;
    private ArrayList<String> urlList;
    private List<Quote> wallpaperList;

    public WallpapersAdapter(Context mCtx, List<Quote> wallpaperList) {
        this.mCtx = mCtx;
        this.wallpaperList = wallpaperList;
        this.originalList = new ArrayList<>(wallpaperList);
        this.filteredList = new ArrayList<>(wallpaperList);
    }

    // Method to update the filtered list and notify the adapter
    public void filterList(List<Quote> filteredList) {
        this.wallpaperList = new ArrayList<>(filteredList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WallpaperViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.item_quotes, parent, false);
        return new WallpaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final WallpaperViewHolder holder, int position) {
        final Quote w = wallpaperList.get(position);
        urlList = new ArrayList<>();
        imagesIndex = 0;

        // Setting custom font for the quote text
        Typeface font = Typeface.createFromAsset(mCtx.getAssets(), "fonts/montserrat_bold.ttf");
        holder.txtQuote.setTypeface(font);
        holder.txtQuote.setText(w.quote + "\n\n" + w.author);

        // Execute AsyncTask to fetch image URLs from Unsplash API
        new JsonTask().execute(w.getQuote());

        // Setting the favorite button state based on the database
        holder.favBtn.setLiked(MainActivity.favoriteDatabase.favoriteDao().isFavorite(Integer.parseInt(w.getId())) == 1);

        // Setting listener for the favorite button
        holder.favBtn.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                handleFavoriteButtonClick(w, holder.favBtn);
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                handleFavoriteButtonClick(w, holder.favBtn);
            }
        });

        // Setting listener for changing random backgrounds
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRandomBackgrounds(holder);
            }
        });

        // Setting listener for saving the quote
        holder.ll_quote_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSaveButtonClick(holder, w);
            }
        });

        // Setting listener for copying the quote
        holder.ll_copy_quote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCopyButtonClick(w);
            }
        });

        // Setting listener for sharing the quote
        holder.ll_quote_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(holder);
            }
        });
    }

    // Handle favorite button click
    private void handleFavoriteButtonClick(Quote quote, LikeButton favBtn) {
        FavoriteList favoriteList = new FavoriteList();
        int id = Integer.parseInt(quote.getId());
        String name = quote.getQuote();
        String person = quote.getAuthor();
        favoriteList.setId(id);
        favoriteList.setName(name);
        favoriteList.setPerson(person);

        if (MainActivity.favoriteDatabase.favoriteDao().isFavorite(id) != 1) {
            favBtn.setLiked(true);
            MainActivity.favoriteDatabase.favoriteDao().addData(favoriteList);
        } else {
            favBtn.setLiked(false);
            MainActivity.favoriteDatabase.favoriteDao().delete(favoriteList);
        }
    }

    // Change random backgrounds
    private void changeRandomBackgrounds(WallpaperViewHolder holder) {
        int numOfImages = urlList.size();
        images = new String[numOfImages + 22];
        // Copy existing images
        for (int i = 0; i < numOfImages; i++) {
            images[i] = urlList.get(i);
        }
        // Add default images
        for (int i = 0; i < 21; i++) {
            images[numOfImages + 1] = "https://images.unsplash.com/32/Mc8kW4x9Q3aRR3RkP5Im_IMG_4417.jpg?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80";
            images[numOfImages + 2] = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80";
            images[numOfImages + 3] = "https://images.unsplash.com/photo-1531685250784-7569952593d2?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80";
            images[numOfImages + 4] = "https://images.unsplash.com/photo-1531315630201-bb15abeb1653?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=435&q=80";
            images[numOfImages + 5] = "https://images.unsplash.com/photo-1528459801416-a9e53bbf4e17?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=412&q=80";
            images[numOfImages + 6] = "https://images.unsplash.com/photo-1522441815192-d9f04eb0615c?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=327&q=80";
            images[numOfImages + 7] = "https://images.unsplash.com/photo-1558591710-4b4a1ae0f04d?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80";
            images[numOfImages + 8] = "https://images.unsplash.com/photo-1487147264018-f937fba0c817?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80";
            images[numOfImages + 9] = "https://images.unsplash.com/photo-1476820865390-c52aeebb9891?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=870&q=80";
            images[numOfImages + 10] = "https://images.unsplash.com/photo-1516617442634-75371039cb3a?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80";
            images[numOfImages + 11] = "https://images.unsplash.com/photo-1489549132488-d00b7eee80f1?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80";
            images[numOfImages + 12] = "https://images.unsplash.com/photo-1497250681960-ef046c08a56e?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80";
            images[numOfImages + 13] = "https://images.unsplash.com/photo-1436397543931-01c4a5162bdb?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=580&q=80";
            images[numOfImages + 14] = "https://images.unsplash.com/photo-1513366208864-87536b8bd7b4?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80";
            images[numOfImages + 15] = "https://images.unsplash.com/photo-1507608158173-1dcec673a2e5?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80";
            images[numOfImages + 16] = "https://images.unsplash.com/photo-1519751138087-5bf79df62d5b?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80";
            images[numOfImages + 17] = "https://images.unsplash.com/photo-1483232539664-d89822fb5d3e?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=464&q=80";
            images[numOfImages + 18] = "https://images.unsplash.com/photo-1508615039623-a25605d2b022?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80";
            images[numOfImages + 19] = "https://images.unsplash.com/photo-1487088678257-3a541e6e3922?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80";
            images[numOfImages + 20] = "https://images.unsplash.com/photo-1537420327992-d6e192287183?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=388&q=80";
            images[numOfImages] = "https://images.unsplash.com/photo-1458682625221-3a45f8a844c7?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80";
        }
        // Load the next background image
        Glide.with(mCtx).clear(holder.imgview2);
        Glide.with(mCtx).load(images[imagesIndex]).into(holder.imgview2);
        ++imagesIndex;
        if (imagesIndex == images.length) {
            imagesIndex = 0;
        }
    }

    // Handle save button click
    private void handleSaveButtonClick(WallpaperViewHolder holder, Quote quote) {
        if (ContextCompat.checkSelfPermission(mCtx, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed with saving
            saveImageToStorage(holder, quote);
        } else {
            // Permission not granted, request permission
            requestStoragePermission();
        }
    }

    // Save image to storage
    private void saveImageToStorage(WallpaperViewHolder holder, Quote quote) {
        Bitmap bitmap = createBitmapFromView(holder.relativeLayout);
        saveBitmapToGallery(mCtx, bitmap, quote);
    }

    // Create a bitmap from a view
    private Bitmap createBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    // Save bitmap to gallery
    private void saveBitmapToGallery(Context context, Bitmap bitmap, Quote quote) {
        String fileName = "quote_" + System.currentTimeMillis() + ".png";
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/QuotesApp");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, fileName);
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            galleryAddPic(context, file, quote);
            Toast.makeText(mCtx, "Quote saved to Pictures/QuotesApp", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Add image to gallery
    private void galleryAddPic(Context context, File file, Quote quote) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        contentValues.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        // Notify the system that the image has been added to the gallery
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    // Handle copy button click
    private void handleCopyButtonClick(Quote quote) {
        ClipboardManager clipboard = (ClipboardManager) mCtx.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("quote", quote.getQuote() + "\n -" + quote.getAuthor());
        assert clipboard != null;
        clipboard.setPrimaryClip(clip);
        Toast.makeText(mCtx, "Quote Copied", Toast.LENGTH_SHORT).show();
    }

    // Show popup menu for sharing
    private void showPopupMenu(final WallpaperViewHolder holder) {
        PopupMenu popup = new PopupMenu(mCtx, holder.ll_quote_share);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.sub_text:
                        shareText(holder.qte);
                        return true;
                    case R.id.sub_image:
                        shareImage(holder);
                        return true;
                }
                return false;
            }
        });
        popup.inflate(R.menu.menu_item);
        popup.show();
    }

    // Share text
    private void shareText(Quote quote) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, quote.getQuote() + "\n -" + quote.getAuthor());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Daily Catholic Quotes");
        mCtx.startActivity(Intent.createChooser(shareIntent, "Share Quote"));
        Toast.makeText(mCtx, "Share as Text", Toast.LENGTH_SHORT).show();
    }

    // Share image
    private void shareImage(WallpaperViewHolder holder) {
        holder.tv_quotes_watermark.setVisibility(View.VISIBLE);
        Bitmap bitmap = createBitmapFromView(holder.relativeLayout);
        holder.tv_quotes_watermark.setVisibility(View.INVISIBLE);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");

        // Save the image to a temporary file
        String imagePath = saveImageTemporarily(bitmap);
        Uri imageUri = FileProvider.getUriForFile(mCtx, BuildConfig.APPLICATION_ID + ".provider", new File(imagePath));

        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.putExtra(Intent.EXTRA_TEXT, holder.qte.getQuote() + "\n -" + holder.qte.getAuthor());

        // Grant temporary read permission to the URI
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        mCtx.startActivity(Intent.createChooser(intent, "Daily Catholic Quotes"));
        Toast.makeText(mCtx, "Share as Image", Toast.LENGTH_SHORT).show();
    }

    // Save the image temporarily to a file and return the file path
    private String saveImageTemporarily(Bitmap bitmap) {
        File tempDir = new File(mCtx.getCacheDir(), "temp_images");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        File tempFile = new File(tempDir, "temp_image.png");
        try {
            FileOutputStream out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile.getAbsolutePath();
    }

    // Get URI for a local bitmap
    private Uri getLocalBitmapUri(Bitmap bitmap) {
        // Not implemented in the provided code
        return null;
    }

    // Request storage permission
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mCtx, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Show rationale if needed
            showPermissionRationale();
        } else {
            // Request permission directly
            ActivityCompat.requestPermissions((Activity) mCtx, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    // Show permission rationale
    private void showPermissionRationale() {
        new AlertDialog.Builder(mCtx)
                .setTitle("Permission needed")
                .setMessage("This permission is needed")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) mCtx, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mCtx, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mCtx, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public int getItemCount() {
        return wallpaperList.size();
    }


    class WallpaperViewHolder extends RecyclerView.ViewHolder {


        TextView tv_quotes_watermark, tv_save_quote;
        TextView txtQuote, likeText;
        TextView txtCategory;
        ImageView iv_save_quote;
        RelativeLayout relativeLayout;
        LinearLayout ll_quote_save, ll_copy_quote, ll_quote_share;
        ImageView imgIcon, imgview2;
        LikeButton favBtn;
        private Quote qte;

        public WallpaperViewHolder(View itemView) {
            super(itemView);


            txtQuote = itemView.findViewById(R.id.txtQuote);
            relativeLayout = itemView.findViewById(R.id.llBackground);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            txtQuote = itemView.findViewById(R.id.txtQuote);
            tv_quotes_watermark = itemView.findViewById(R.id.tv_quotes_watermark);
            likeText = itemView.findViewById(R.id.tv_like_quote_text);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            ll_copy_quote = itemView.findViewById(R.id.ll_copy_quote);
            ll_quote_save = itemView.findViewById(R.id.ll_quote_save);
            ll_quote_share = itemView.findViewById(R.id.ll_quote_share);
            tv_save_quote = itemView.findViewById(R.id.tv_save_quote);
            iv_save_quote = itemView.findViewById(R.id.iv_save_quote);
            favBtn = itemView.findViewById(R.id.favBtn);
            imgview2 = itemView.findViewById(R.id.imageView2);

        }
    }

    class JsonTask extends AsyncTask<String, String, String> {
        String data = "";

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL("https", "api.unsplash.com", "/search/photos?query=" + params[0] + "&client_id=TWpsWTMSvOs4W7pH6J713NkqRh9jmXyZrIHrpmFpW-I");
                //URL url = new URL(params[0]);
                Log.d("eeeeeeeeeee: ", String.valueOf(url));
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    data = data + line;
                    //  Log.d("urls: ", "> " + line);   //here u ll get whole response...... :-)

                }

                if (!data.isEmpty()) {
                    JSONObject jsonObject = new JSONObject((data));
                    JSONArray json;
                    json = jsonObject.getJSONArray("results");
                    // Log.d("json: ", "> " + json);
                    //urlList.clear();
                    for (int i = 0; i < json.length(); i++) {
                        JSONObject urls = json.getJSONObject(i);
                        JSONObject imageurls = urls.getJSONObject("urls");
                        String image = imageurls.getString("small");

                        urlList.add(image);
                        if (i == 10) {
                            break;
                        }
                    }
                }

                // Log.d("urllist: ", "> " + urlList);
                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // quoteOfTheDay.setText(quotes);
        }
    }
}