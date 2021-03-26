package com.dktechhub.mnnit.downloadmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class DownloadService extends Service {
    private final IBinder localBinder = new DownloadBinder();
    public DownloadService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.


        return localBinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager= (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        showNotification("Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        showNotification("on start command");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(1234);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        showNotification("Unbinded");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        showNotification("rebinded");
    }
    NotificationManager notificationManager;
    NotificationCompat.Builder notificationBuilder;
    public void showNotification(String text)
    { int id=1;
    String CHANNEL_ID="DURGESH";
    CharSequence name = "My channel";

        PendingIntent pendingIntent= PendingIntent.getActivity(this,0, new Intent(this, MainActivity.class),0);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,name,NotificationManager.IMPORTANCE_HIGH);
            Notification  n = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker("Download service running")
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("Service running")
                    .setContentText(text)
                    .setChannelId(CHANNEL_ID)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setContentIntent(pendingIntent).build();
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(1234,n);
        }
        else
        {   Notification  n = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Download service running")
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setContentTitle("Service running")
                .setAutoCancel(false)
                .setContentText(text)

                .setContentIntent(pendingIntent).build();
            notificationManager.notify(1234,n);
        }

        

        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    public class DownloadBinder extends Binder{
        public DownloadService getService()
        {
            return DownloadService.this;
        }
    }
}