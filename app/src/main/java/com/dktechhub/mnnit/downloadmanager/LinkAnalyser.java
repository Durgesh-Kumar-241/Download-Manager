package com.dktechhub.mnnit.downloadmanager;

import android.os.AsyncTask;
import android.util.Log;

import java.lang.reflect.Executable;
import java.net.HttpURLConnection;
import java.net.URL;

public class LinkAnalyser extends AsyncTask {
    OnAnalyseCompleteListener onAnalyseCompleteListener;
    String url;
    boolean success=false;
    DownloadItem downloadItem;
    Exception exception;
    LinkAnalyser(String url,OnAnalyseCompleteListener onAnalyseCompleteListener)
    {
        this.onAnalyseCompleteListener=onAnalyseCompleteListener;
        this.url=url;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try{
            URL url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
            {
                publishProgress(15);
                DownloadItem.DownloadStatus status= DownloadItem.DownloadStatus.PENDING;
                publishProgress(30);
                DownloadItem.DownloadType type= DownloadItem.DownloadType.DOCUMENT;
                publishProgress(45);

                String name ="";
                try {
                    String mname=connection.getHeaderFields().get("Content-Disposition").get(0);
                    name =mname.substring(mname.indexOf('"')+1,mname.lastIndexOf('"'));
                }
                catch (Exception e){
                    name=this.url.substring(this.url.lastIndexOf('/')+1);
                    this.exception=e;
                }finally {

                }
                publishProgress(60);
                String path="/sdcard/Downloads/"+name;
                String size = getSize(connection.getContentLength());
                int totalLength=connection.getContentLength();
                Log.d("TAG","Total:"+totalLength);
                publishProgress(75);
                this.downloadItem=new DownloadItem(name,path,size,status,type);
                this.downloadItem.url=this.url;
                publishProgress(90);
                this.downloadItem.mime=connection.getContentType();
                publishProgress(100);
                this.success=true;
            }
        }catch (Exception e)
        {
            this.exception=e;
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.onAnalyseCompleteListener.analyseStart();
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if(success)
            this.onAnalyseCompleteListener.analyseSuccess(this.downloadItem);
        else this.onAnalyseCompleteListener.analyseFailed(exception);
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
        onAnalyseCompleteListener.analyseProgress((Integer) values[0]);
    }
    public static String getSize(long num)
    {
        String str;
        int KB = 1000;
        int MB = KB*1000;
        int GB = MB*1000;
        if(num>=GB)
            str=String.format("%.2f GB",(float)num/GB);
        else if(num>=MB)
            str=String.format("%.2f MB",(float)num/MB);
        else if(num>=KB)
            str=String.format("%.2f KB",(float)num/KB);
        else str=String.format("%s B", String.valueOf(num));
        return str;


    }
    public static String getType(String mime)
    {
        if(mime.contains("audio"))
            return "audio";
        else if(mime.contains("video"))
            return "video";
        else if(mime.contains("image"))
            return "image";
        else if(mime.contains("application"))
        {
            if(mime.contains("octet-stream"))
                return "apk";
            else  {
                return "document";

            }
        }
        else return "unknown";
    }
    public interface OnAnalyseCompleteListener{
        void analyseSuccess(DownloadItem downloadItem);
        void analyseProgress(int progress);
        void analyseStart();
        void analyseFailed(Exception e);
    }
}
