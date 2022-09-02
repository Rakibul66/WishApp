package com.muththamizh.wishes.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.muththamizh.wishes.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static int NOTIFY_ID = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> dataMap;
        dataMap = remoteMessage.getData();

        for (String key : dataMap.keySet())
            Log.e("FIREBASE DATA", "FIRE " + key + " = " + dataMap.get(key));

        /*
      if (remoteMessage.getNotification() != null) {
            Log.e("FIREBASE", "FIRE1 " + remoteMessage.getNotification().getTitle());
            Log.e("FIREBASE", "FIRE1 " + remoteMessage.getNotification().getBody());
            Log.e("FIREBASE", "FIRE1 " + remoteMessage.getNotification().getTag());
        }*/

//        if (SharedPreferenceUtil.getUserModel() == null)
//            return;

        createNotification(dataMap);

    }

    public void createNotification(Map<String, String> dataMap) {
        String channelId = getString(R.string.notification_channel_id_message);
        String title = getString(R.string.app_name);
        String sendername = dataMap.get("title");
        String msg = dataMap.get("body");
        Bitmap image = null;
        if (dataMap.containsKey("image")) {
            image = getBitmapFromURL(dataMap.get("image"));
        }

        NotificationCompat.Builder builder;
        NotificationManager notifManager;
        notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = notifManager.getNotificationChannel(channelId);
            if (mChannel == null) {
                mChannel = new NotificationChannel(channelId, title, importance);
                mChannel.enableVibration(false);
                notifManager.createNotificationChannel(mChannel);
            }
        }

        builder = new NotificationCompat.Builder(this, channelId);
        builder.setContentTitle(sendername)
                .setSmallIcon((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ? R.mipmap.ic_launcher : R.mipmap.ic_launcher)
                .setColor(getResources().getColor(R.color.white))
                .setContentText(msg)
                .setDefaults(Notification.DEFAULT_ALL)
                .setVibrate(new long[]{1000L, 500L, 300L, 1000L})
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_DEFAULT);

        if (image != null) {
            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(image));
        }

      /*  TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(HomeActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
*/
        Notification notification = builder.build();
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notifManager.notify(NOTIFY_ID, notification);
        NOTIFY_ID++;
    }

    @Override
    public void onNewToken(@NotNull String refreshedToken) {
        super.onNewToken(refreshedToken);
        if (!TextUtils.isEmpty(refreshedToken)) {
            Log.e("TOKEN>>", refreshedToken);
//            SharedPreferenceUtil.setFCMToken(refreshedToken);
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}