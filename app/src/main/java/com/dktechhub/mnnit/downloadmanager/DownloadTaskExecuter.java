package com.dktechhub.mnnit.downloadmanager;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTaskExecuter extends AsyncTask<Void,Integer,Void> {
    boolean success=false;
    Exception exception;
    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL(this.downloadItem.url);
            long readtotal=0;
            File outfile = new File(downloadItem.path);
            Log.d("TAG","new Download---///"+downloadItem.path);
            if(outfile.exists())
                readtotal=outfile.length();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Range","bytes="+readtotal+"-");
            int resCode=connection.getResponseCode();
            Log.d("TAG","new Download---///"+connection.getResponseCode()+connection.getHeaderFields().toString());
            if(resCode==206||resCode==200)
            {   OutputStream outputStream;
                if(resCode==206){
                    outputStream=new FileOutputStream(outfile,true);
                }else {
                    if(connection.getContentLength()==readtotal)
                    {   publishProgress(100);
                        downloadItem.status= DownloadItem.DownloadStatus.COMPLETED;
                        success=true;
                        return null;}
                    outputStream = new FileOutputStream(outfile);
                    Log.d("TAG", "resuming Download///");

                }
               int totalLength=connection.getContentLength()+(int)readtotal;
                Log.d("TAG","Total:"+totalLength+" Existing:"+readtotal);
                InputStream inputStream = connection.getInputStream();
                byte[] data =new byte[1024*1024*6];
                int count;

                while ((readtotal<totalLength)&&(count=inputStream.read(data))!=-1)
                {

                    readtotal+=count;
                    if(totalLength>0)
                        publishProgress((int)((readtotal*100)/totalLength));
                    outputStream.write(data,0,count);
                    System.out.println("Existing:"+readtotal+" total:"+totalLength);

                }
                downloadItem.status= DownloadItem.DownloadStatus.COMPLETED;
                success=true;
            }

        }catch (Exception e)
        {
            downloadItem.status= DownloadItem.DownloadStatus.FAILED;
            exception=e;
        }

        return null;
    }
    DownloadItem downloadItem;
    DownloadStatusListener downloadStatusListener;
    public DownloadTaskExecuter(DownloadItem downloadItem,DownloadStatusListener downloadStatusListener)
    {
        this.downloadItem=downloadItem;
        this.downloadStatusListener=downloadStatusListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        downloadItem.status= DownloadItem.DownloadStatus.DOWNLOADING;
        downloadStatusListener.updateStatus(DownloadItem.DownloadStatus.DOWNLOADING);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(success)
            downloadStatusListener.updateStatus(DownloadItem.DownloadStatus.COMPLETED);
        else {
            downloadStatusListener.updateStatus(DownloadItem.DownloadStatus.FAILED);
            downloadStatusListener.onError(exception);
        }

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        downloadStatusListener.updateStatus(DownloadItem.DownloadStatus.PAUSED);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        downloadItem.progress=values[0];
        downloadStatusListener.updateProgress(values[0]);
    }

    public interface DownloadStatusListener{
        void updateStatus(DownloadItem.DownloadStatus downloadStatus);
        void updateProgress(int mprogress);
        void  onError(Exception e);
    }

}
