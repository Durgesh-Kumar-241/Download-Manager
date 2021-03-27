package com.dktechhub.mnnit.downloadmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;

public class DownloadService extends Service {
    DatabaseWorker worker;
    ArrayList<DownloadItem> items;
    NotificationManager notificationManager;
    ObserverActivityInterface observerActivityInterface;
    //HashMap<DownloadItem,DownloadTaskExecuter> running= new HashMap<>();
    public void setObserverActivityInterface(ObserverActivityInterface observerActivityInterface) {
        this.observerActivityInterface = observerActivityInterface;
    }

    private final IBinder localBinder = new DownloadBinder();
    public DownloadService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        notifyObserver();
        return localBinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        worker=new DatabaseWorker(this,"DownloadManager2",null,1);
        notificationManager= (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        showNotification("Service created");
        loadItemsFromDatabase();
        loadInitial();
        Toast.makeText(this, "service created", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveToDatabase();
        closeNotification();
        Toast.makeText(this, "service destroyed", Toast.LENGTH_SHORT).show();


    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "service unbounded", Toast.LENGTH_SHORT).show();
        saveToDatabase();
        return super.onUnbind(intent);

    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Toast.makeText(this, "service rebinded", Toast.LENGTH_SHORT).show();

        //showNotification("rebinded");
    }


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
    public void closeNotification()
    {
        notificationManager.cancel(1234);
    }


    public void loadItemsFromDatabase()
    {   if(this.items==null)
        this.items=worker.loadItems();
    }
    public ArrayList<DownloadItem> getItems()
    {
        if(this.items==null)
            loadItemsFromDatabase();
        return this.items;
    }
    public void saveToDatabase()
    {
        if(this.items!=null)
        {
            worker.saveToDatabase(items);
        }
    }
    public void addNewTask(DownloadItem downloadItem)
    {
        worker.addDownload(downloadItem);
        this.items.add(downloadItem);
        notifyObserver();

    }

    public void notifyObserver()
    {
        if(this.observerActivityInterface!=null)
            observerActivityInterface.onItemsUpdate(this.items);
    }
    public class DownloadBinder extends Binder{
        public DownloadService getService()
        {
            return DownloadService.this;
        }
    }
    public void manageAction(DownloadItem downloadItem)
    {

        switch (downloadItem.status)
        {
            case COMPLETED:
                Toast.makeText(this, "opening"+downloadItem.name, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Intent.ACTION_VIEW);
                File f =new File(downloadItem.path);
                Uri uri = Uri.fromFile(f);
                //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setDataAndType(Uri.parse(downloadItem.path),downloadItem.mime);
                try{
                    startActivity(i);
                }catch (Exception e)
                {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                }


                break;
            case PENDING:

            case FAILED:
            case PAUSED:
                downloadItem.startDownloading(new DownloadItem.Observer() {
                    @Override
                    public void notifyParent() {
                    notifyObserver();
                    saveToDatabase();
                    }

                    @Override
                    public void notifyError(Exception e) {
                        Toast.makeText(DownloadService.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case DOWNLOADING:
                downloadItem.pauseDownloading();

        }
        notifyObserver();

    }
    public void loadInitial()
    {
        for(DownloadItem downloadItem:items)
        {
            if(downloadItem.status==DownloadItem.DownloadStatus.DOWNLOADING)
            {
                downloadItem.startDownloading(new DownloadItem.Observer() {
                    @Override
                    public void notifyParent() {
                        notifyObserver();
                    }

                    @Override
                    public void notifyError(Exception e) {
                        Toast.makeText(DownloadService.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
    public interface ObserverActivityInterface{
        void onItemsUpdate(ArrayList<DownloadItem> items);
    }
}