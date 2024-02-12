package com.dcq.quotesapp.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
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
import com.dcq.quotesapp.ApiResponse;
import com.dcq.quotesapp.FavoriteList;
import com.dcq.quotesapp.MainActivity;
import com.dcq.quotesapp.R;
import com.dcq.quotesapp.Result;
import com.dcq.quotesapp.UnsplashApi;
import com.dcq.quotesapp.Urls;
import com.dcq.quotesapp.models.Quote;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.wang.avi.BuildConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WallpapersAdapter extends RecyclerView.Adapter<WallpapersAdapter.WallpaperViewHolder>
        implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int REQUEST_STORAGE_PERMISSION_CODE = 1;
    private final Context mCtx;
    private final UnsplashApi unsplashApi;
    private List<Quote> wallpaperList;
    private final HashMap<Integer, ArrayList<String>> imageUrlList = new HashMap<>();
    private final HashMap<Integer, Integer> imgIndexMap = new HashMap<>();
    private ArrayList<String> imageUrls;
    private String[] images;
    private int imageIndex = 0;

    public WallpapersAdapter(Context context, List<Quote> wallpaperList) {
        this.mCtx = context;
        this.wallpaperList = wallpaperList;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.unsplash.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        unsplashApi = retrofit.create(UnsplashApi.class);
    }

    // Remove common words from input string
    public static String removeCommonWords(String input) {
        Set<String> commonWords = new HashSet<>(Arrays.asList(
                // Common words to remove
        ));
        String[] words = input.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!commonWords.contains(word.toLowerCase())) {
                result.append(word).append(" ");
            }
        }
        return result.toString().trim();
    }

    // Update the filtered list and notify the adapter
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
        final Quote quote = wallpaperList.get(position);
        imgIndexMap.put(position, 0);
        Typeface font = Typeface.createFromAsset(mCtx.getAssets(), "fonts/montserrat_bold.ttf");
        holder.txtQuote.setTypeface(font);
        holder.txtQuote.setText(quote.getQuote() + "\n\n" + quote.getAuthor());
        searchPhotos(removeCommonWords(quote.getQuote() + " " + quote.getAuthor()), position);
        holder.favBtn.setLiked(MainActivity.favoriteDatabase.favoriteDao().isFavorite(Integer.parseInt(quote.getId())) == 1);
        holder.favBtn.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                handleFavoriteButtonClick(quote, holder.favBtn);
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                handleFavoriteButtonClick(quote, holder.favBtn);
            }
        });

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBackground(holder, position);
            }
        });

        holder.ll_quote_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSaveButtonClick();
            }

            private void handleSaveButtonClick() {
                if (ContextCompat.checkSelfPermission(mCtx, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveImageToStorage(holder);
                } else {
                    requestStoragePermission();
                }
            }

            private void saveImageToStorage(WallpaperViewHolder holder) {
                holder.tv_quotes_watermark.setVisibility(View.VISIBLE);
                Bitmap bitmap = Bitmap.createBitmap(holder.relativeLayout.getWidth(), holder.relativeLayout.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                holder.relativeLayout.draw(canvas);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentResolver resolver = mCtx.getContentResolver();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                    Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

                    Toast.makeText(mCtx, "File Saved", Toast.LENGTH_SHORT).show();
                    holder.tv_save_quote.setText("Saved");
                    holder.iv_save_quote.setImageResource(R.drawable.ic_menu_check);
                    try (OutputStream fos = resolver.openOutputStream(Objects.requireNonNull(imageUri))) {
                        if (fos != null) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    holder.tv_quotes_watermark.setVisibility(View.INVISIBLE);
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
                        mCtx.sendBroadcast(intent);

                        Toast.makeText(mCtx, "Saved", Toast.LENGTH_SHORT).show();
                        holder.tv_save_quote.setText("Saved");
                        holder.iv_save_quote.setImageResource(R.drawable.ic_menu_check);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    holder.tv_quotes_watermark.setVisibility(View.INVISIBLE);
                }
            }

            private void requestStoragePermission() {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mCtx, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Explain why the permission is needed
                    new AlertDialog.Builder(mCtx)
                            .setTitle("Permission needed")
                            .setMessage("This permission is needed to save images.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Request the permission again
                                    ActivityCompat.requestPermissions((Activity) mCtx, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION_CODE);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Handle permission denial
                                    Toast.makeText(mCtx, "Permission denied. Some features may not work.", Toast.LENGTH_SHORT).show();
                                }
                            }).create().show();
                } else {
                    // Request the permission for the first time
                    ActivityCompat.requestPermissions((Activity) mCtx, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION_CODE);
                }
            }
        });

        holder.ll_copy_quote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(quote);
            }

            private void copyToClipboard(Quote quote) {
                ClipboardManager clipboard = (ClipboardManager) mCtx.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", "\"" + quote.getQuote() + "\"\n -" + quote.getAuthor());
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(mCtx, "Quote Copied to Clipboard", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.ll_quote_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu();
            }

            private void showPopupMenu() {
                PopupMenu popup = new PopupMenu(mCtx, holder.ll_quote_share);
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
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "\"" + quote.getQuote() + "\"\n -" + quote.getAuthor());
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Daily Catholic Quotes");
                        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mCtx.startActivity(Intent.createChooser(shareIntent, "Share Quote").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        Toast.makeText(mCtx, "Share as Text", Toast.LENGTH_SHORT).show();
                    }

                    private void shareAsImage() {
                        holder.tv_quotes_watermark.setVisibility(View.VISIBLE);
                        Bitmap bitmap = Bitmap.createBitmap(holder.relativeLayout.getWidth(), holder.relativeLayout.getHeight(),
                                Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        holder.relativeLayout.draw(canvas);
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("*/*");
                        intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap));
                        intent.putExtra(Intent.EXTRA_TEXT, "\"" + quote.getQuote() + "\"\n -" + quote.getAuthor());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mCtx.startActivity(Intent.createChooser(intent, "Daily Catholic Quotes").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        holder.tv_quotes_watermark.setVisibility(View.INVISIBLE);
                        Toast.makeText(mCtx, "Share as Image", Toast.LENGTH_SHORT).show();

                    }
                });
                popup.inflate(R.menu.menu_item);
                popup.show();
            }
        });
    }

    private void changeBackground(WallpaperViewHolder holder, int position) {
        imageIndex = imgIndexMap.get(position);
        ArrayList<String> imageUrls2 = imageUrlList.get(position);
        int numOfImages = imageUrls2.size();
        images = new String[numOfImages];
        for (int i = 0; i < numOfImages; i++) {
            images[i] = imageUrls2.get(i);
        }
        Glide.with(mCtx).clear(holder.imgview2);
        Glide.with(mCtx).load(images[imageIndex]).into(holder.imgview2);

        ++imageIndex;  // update index, so that next time it points to next resource
        if (imageIndex == images.length)
            imageIndex = 0; // if we have reached at last index of array, simply restart from beginning
        imgIndexMap.put(position, imageIndex);
    }

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

    private void preloadedImages() {
        imageUrls.add("https://images.unsplash.com/32/Mc8kW4x9Q3aRR3RkP5Im_IMG_4417.jpg?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1531685250784-7569952593d2?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1531315630201-bb15abeb1653?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=435&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1528459801416-a9e53bbf4e17?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=412&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1522441815192-d9f04eb0615c?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=327&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1558591710-4b4a1ae0f04d?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1487147264018-f937fba0c817?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1476820865390-c52aeebb9891?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=870&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1516617442634-75371039cb3a?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1489549132488-d00b7eee80f1?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1497250681960-ef046c08a56e?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1436397543931-01c4a5162bdb?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=580&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1513366208864-87536b8bd7b4?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1507608158173-1dcec673a2e5?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1519751138087-5bf79df62d5b?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1483232539664-d89822fb5d3e?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=464&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1508615039623-a25605d2b022?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1487088678257-3a541e6e3922?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1537420327992-d6e192287183?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=388&q=80");
        imageUrls.add("https://images.unsplash.com/photo-1458682625221-3a45f8a844c7?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80");

    }

    private Uri getLocalBitmapUri(Bitmap bitmap) {
        Uri bmpUri = null;
        try {
            File file = new File(mCtx.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "Daily Catholic Quotes" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            bmpUri = FileProvider.getUriForFile(mCtx, BuildConfig.APPLICATION_ID + ".provider", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(mCtx, "Permission granted. You can now save images.", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(mCtx, "Permission denied. Some features may not work.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public int getItemCount() {
        return wallpaperList.size();
    }

    public void searchPhotos(String query, int id) {
        Call<ApiResponse> call = unsplashApi.searchPhotos(query, "TWpsWTMSvOs4W7pH6J713NkqRh9jmXyZrIHrpmFpW-I");
        Log.d("eeeeeeeeeee: ", query);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse != null) {
                        List<Result> results = apiResponse.getResults();
                        if (results != null) {
                            imageUrls = new ArrayList<>();
                            for (Result result : results) {
                                Urls urls = result.getUrls();
                                if (urls != null) {
                                    String imageUrl = urls.getSmall();
                                    if (imageUrl != null) {
                                        imageUrls.add(0, imageUrl);
                                    }
                                }
                            }
                            preloadedImages();
                            imageUrlList.put(id, imageUrls);
                        }
                    }
                } else {
                    Log.e("FavoriteAdapter", "Failed to fetch photos: " + response.message());
                }
            }


            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("FavoriteAdapter", "Error fetching photos: " + t.getMessage());
            }
        });

    }

    static class WallpaperViewHolder extends RecyclerView.ViewHolder {
        TextView txtQuote;
        RelativeLayout relativeLayout;
        LikeButton favBtn;
        LinearLayout ll_quote_save, ll_copy_quote, ll_quote_share;
        TextView tv_quotes_watermark, tv_save_quote;
        ImageView iv_save_quote;
        ImageView imgview2;

        public WallpaperViewHolder(View itemView) {
            super(itemView);
            txtQuote = itemView.findViewById(R.id.txtQuote);
            relativeLayout = itemView.findViewById(R.id.llBackground);
            favBtn = itemView.findViewById(R.id.favBtn);
            ll_quote_save = itemView.findViewById(R.id.ll_quote_save);
            ll_copy_quote = itemView.findViewById(R.id.ll_copy_quote);
            ll_quote_share = itemView.findViewById(R.id.ll_quote_share);
            tv_quotes_watermark = itemView.findViewById(R.id.tv_quotes_watermark);
            tv_save_quote = itemView.findViewById(R.id.tv_save_quote);
            iv_save_quote = itemView.findViewById(R.id.iv_save_quote);
            imgview2 = itemView.findViewById(R.id.imageView2);
        }
    }
}
