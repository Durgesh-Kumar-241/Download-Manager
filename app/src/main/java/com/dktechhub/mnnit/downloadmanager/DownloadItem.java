package com.dktechhub.mnnit.downloadmanager;

public class DownloadItem {
    DownloadTaskExecuter downloadTaskExecuter;
    String name;
    String size;
    String path;
    String url;
    Observer observer;
    DownloadStatus status;
    DownloadType type;
    int progress;
    int id;
    public DownloadItem(String name,String path,String size)
    {
        this.name=name;
        this.path=path;this.size=size;
        this.status=DownloadStatus.PENDING;
    }
    public  DownloadItem(String name,String path,String size,DownloadStatus status)
    {
       this(name,path,size);
       this.status=status;
    }
    public  DownloadItem(String name,String path,String size,DownloadStatus status,DownloadType type)
    {
        this(name,path,size,status);
        this.type=type;
    }
    public enum DownloadStatus{
            PENDING,COMPLETED,FAILED,DOWNLOADING,CANCELLED
    }
    public enum DownloadType{
        AUDIO,VIDEO,DOCUMENT
    }
    public void startDownloading(Observer observer)
    {   this.observer=observer;
        if(this.downloadTaskExecuter==null||downloadTaskExecuter.isCancelled()) {
            downloadTaskExecuter = new DownloadTaskExecuter(this, new DownloadTaskExecuter.DownloadStatusListener() {

                @Override
                public void updateStatus(DownloadStatus downloadStatus) {
                    status = downloadStatus;
                    observer.notifyParent();
                }

                @Override
                public void updateProgress(int mprogress) {
                    progress = mprogress;
                    observer.notifyParent();
                }

                @Override
                public void onError(Exception e) {
                    observer.notifyError(e);
                }
            });
            downloadTaskExecuter.execute();
        }
    }
    public void pauseDownloading()
    {
        if(downloadTaskExecuter!=null)
            downloadTaskExecuter.cancel(true);
    }

    public interface Observer{
        void notifyParent();
        void notifyError(Exception e);
    }
}
