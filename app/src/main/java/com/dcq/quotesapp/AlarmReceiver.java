package com.dcq.quotesapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "onReceive()");
        // Fetch the quote of the day from Firebase database
        DatabaseReference quoteRef = FirebaseDatabase.getInstance("https://dailycatholicquotes-2ef12-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("qotd");
        quoteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("FirebaseTest", "onDataChange: dataSnapshot = " + dataSnapshot.child("quote").getValue());
                    String quote = dataSnapshot.child("quote").getValue(String.class);
                    String author = dataSnapshot.child("author").getValue(String.class);
                    if (quote != null && author != null) {
                        showNotification(context, quote, author);
                    } else {
                        // Handle case where quote or author is null
                        Log.e("AlarmReceiver", "Quote or author is null");
                    }
                } else {
                    // Handle case where dataSnapshot does not exist
                    Log.e("AlarmReceiver", "DataSnapshot does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
                Log.e("AlarmReceiver", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void showNotification(Context context, String quote, String author) {
        // Create a notification channel (required for Android 8.0 and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("qotd_channel", "Quote of the Day", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create a notification with the quote and author
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "qotd_channel")
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("Quote of the Day")
                .setContentText(quote + "\n\n" + "-" + author)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }
}
