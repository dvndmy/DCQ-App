package com.dcq.quotesapp;

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
import android.os.AsyncTask;
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
import com.dcq.quotesapp.models.Quote;
import com.like.LikeButton;
import com.like.OnLikeListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> implements ActivityCompat.OnRequestPermissionsResultCallback {
    Context mCtx;
    ArrayList<String> urlList;
    private List<FavoriteList> favoriteLists;
    private String[] images;
    private int imagesIndex = 0;
    private int STORAGE_PERMISSION_CODE = 1;

    public FavoriteAdapter(List<FavoriteList> favoriteLists, Context mCtx) {
        this.favoriteLists = favoriteLists;
        this.mCtx = mCtx;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_quotes, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int i) {
        final FavoriteList fl = favoriteLists.get(i);
        urlList = new ArrayList<>();
        Typeface font = Typeface.createFromAsset(mCtx.getAssets(),
                "fonts/montserrat_bold.ttf");
        holder.txtQuote.setTypeface(font);
        imagesIndex = 0;
        holder.txtQuote.setText(fl.getName() + "\n\n" + fl.getPerson());
        if (MainActivity.favoriteDatabase.favoriteDao().isFavorite(fl.getId()) == 1)
            holder.favBtn.setLiked(true);
        else
            holder.favBtn.setLiked(false);

        holder.favBtn.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                FavoriteList favoriteList = new FavoriteList();
                int id = fl.getId();
                String name = fl.getName();
                String person = fl.getPerson();
                favoriteList.setId(id);
                favoriteList.setName(name);
                favoriteList.setPerson(person);

                if (MainActivity.favoriteDatabase.favoriteDao().isFavorite(id) != 1) {
                    holder.favBtn.setLiked(true);
                    MainActivity.favoriteDatabase.favoriteDao().addData(favoriteList);

                } else {
                    holder.favBtn.setLiked(false);
                    MainActivity.favoriteDatabase.favoriteDao().delete(favoriteList);
                }
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                FavoriteList favoriteList = new FavoriteList();
                int id = fl.getId();
                String name = fl.getName();
                String person = fl.getPerson();
                favoriteList.setId(id);
                favoriteList.setName(name);
                favoriteList.setPerson(person);

                if (MainActivity.favoriteDatabase.favoriteDao().isFavorite(id) != 1) {
                    holder.favBtn.setLiked(true);
                    MainActivity.favoriteDatabase.favoriteDao().addData(favoriteList);

                } else {
                    holder.favBtn.setLiked(false);
                    MainActivity.favoriteDatabase.favoriteDao().delete(favoriteList);
                }
            }
        });


        //Change Random Backgrounds
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new JsonTask().execute(fl.getName());

                int numOfImages = urlList.size();
                images = new String[numOfImages + 22];
                for (int i = 0; i < numOfImages; i++) {
                    images[i] = urlList.get(i);
                }
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
                Glide.with(mCtx).clear(holder.imgview2);
                Glide.with(mCtx).load(images[imagesIndex]).into(holder.imgview2);
                //relativeLayout.setBackgroundResource(images[imagesIndex]);
                ++imagesIndex;  // update index, so that next time it points to next resource
                if (imagesIndex == images.length)
                    imagesIndex = 0; // if we have reached at last index of array, simply restart from beginning
            }
        });

        //when you press save button
        holder.ll_quote_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(mCtx,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    holder.tv_quotes_watermark.setVisibility(View.VISIBLE);
                    Bitmap bitmap = Bitmap.createBitmap(holder.relativeLayout.getWidth(), holder.relativeLayout.getHeight(),
                            Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    holder.relativeLayout.draw(canvas);

                    OutputStream fos;

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
                        try {
                            fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                            fos.flush();
                            fos.close();


                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        holder.tv_quotes_watermark.setVisibility(View.INVISIBLE);
                    } else {

                        FileOutputStream outputStream = null;

                        File sdCard = Environment.getExternalStorageDirectory();

                        File directory = new File(sdCard.getAbsolutePath() + "/Latest Quotes");
                        directory.mkdir();

                        String filename = String.format("%d.jpg", System.currentTimeMillis());

                        File outFile = new File(directory, filename);

                        Toast.makeText(mCtx, "Saved", Toast.LENGTH_SHORT).show();
                        holder.tv_save_quote.setText("Saved");
                        holder.iv_save_quote.setImageResource(R.drawable.ic_menu_check);


                        try {
                            outputStream = new FileOutputStream(outFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                            outputStream.flush();
                            outputStream.close();

                            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            intent.setData(Uri.fromFile(outFile));
                            mCtx.sendBroadcast(intent);


                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        holder.tv_quotes_watermark.setVisibility(View.INVISIBLE);

                    }

                } else {
                    //show permission popup
                    requestStoragePermission();
                }

            }
        });

        //copy button
        holder.ll_copy_quote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) mCtx.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", fl.getName() + " -" + fl.getPerson());
                assert clipboard != null;
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mCtx, "Quote Copied", Toast.LENGTH_SHORT).show();
            }
        });

        //When You Press Share Button
        holder.ll_quote_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup();

            }

            private void popup() {
                PopupMenu popup = new PopupMenu(mCtx, holder.ll_quote_share);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.sub_text:
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                shareIntent.putExtra(Intent.EXTRA_TEXT, fl.getName() + " -" + fl.getPerson());
                                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Daily Catholic Quotes");
                                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mCtx.startActivity(Intent.createChooser(shareIntent, "Share Quote").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                Toast.makeText(mCtx, "Share as Text", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.sub_image:
                                holder.tv_quotes_watermark.setVisibility(View.VISIBLE);
                                Bitmap bitmap = Bitmap.createBitmap(holder.relativeLayout.getWidth(), holder.relativeLayout.getHeight(),
                                        Bitmap.Config.ARGB_8888);
                                Canvas canvas = new Canvas(bitmap);
                                holder.relativeLayout.draw(canvas);
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("*/*");
                                intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap));
                                intent.putExtra(Intent.EXTRA_TEXT, fl.getName() + " -" + fl.getPerson());
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mCtx.startActivity(Intent.createChooser(intent, "Daily Catholic Quotes").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                holder.tv_quotes_watermark.setVisibility(View.INVISIBLE);
                                Toast.makeText(mCtx, "Share as Image", Toast.LENGTH_SHORT).show();

                                return true;
                        }
                        return false;
                    }
                });
                popup.inflate(R.menu.menu_item);

                popup.show();
            }
        });
    }

    //Share image tool
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

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mCtx, Manifest.permission.READ_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(mCtx)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) mCtx, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();

        } else {
            ActivityCompat.requestPermissions((Activity) mCtx, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    //Permisssion for save images
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(mCtx, "Permission ok", Toast.LENGTH_SHORT).show();

            } else

                Toast.makeText(mCtx, "Permission not allow", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return favoriteLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_quotes_watermark, tv_save_quote;
        TextView txtQuote, likeText;
        TextView txtCategory;
        ImageView iv_save_quote;
        RelativeLayout relativeLayout;
        LinearLayout ll_quote_save, ll_copy_quote, ll_quote_share;
        ImageView imgIcon, imgview2;
        LikeButton favBtn;
        private Quote qte;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

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
                    //      Log.d("urls: ", "> " + line);   //here u ll get whole response...... :-)

                }

                if (!data.isEmpty()) {
                    JSONObject jsonObject = new JSONObject((data));
                    JSONArray json;
                    json = jsonObject.getJSONArray("results");
                    //      Log.d("json: ", "> " + json);
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

                //    Log.d("urllist: ", "> " + urlList);
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
