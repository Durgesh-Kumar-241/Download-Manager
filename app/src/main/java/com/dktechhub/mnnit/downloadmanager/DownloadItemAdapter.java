package com.dktechhub.mnnit.downloadmanager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DownloadItemAdapter extends RecyclerView.Adapter<DownloadItemAdapter.ViewHolder> {
    public ArrayList<DownloadItem> mlist=new ArrayList<>();
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context=parent.getContext();
        LayoutInflater inflator = LayoutInflater.from(context);
        View DownloadView=inflator.inflate(R.layout.item_download,parent,false);
        ViewHolder viewHolder =new ViewHolder(DownloadView);
        return viewHolder;
    }

    public void addItems(ArrayList<DownloadItem> items)
    {
        this.mlist.addAll(items);
        this.notifyDataSetChanged();
    }
    public void addItem(DownloadItem item)
    {
        this.mlist.add(item);
        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DownloadItem downloadItem = mlist.get(position);
        holder.downloadName.setText(downloadItem.name);
        holder.downloadSize.setText(downloadItem.size);
        holder.downloadProgress.setProgress(downloadItem.progress);

        if(downloadItem.status== DownloadItem.DownloadStatus.PENDING)
            holder.actionButton.setImageResource(android.R.drawable.ic_media_play);
        else if(downloadItem.status== DownloadItem.DownloadStatus.COMPLETED){
            holder.actionButton.setImageResource(android.R.drawable.ic_menu_view);
            holder.downloadProgress.setVisibility(View.INVISIBLE);}
        else if(downloadItem.status== DownloadItem.DownloadStatus.DOWNLOADING)
            holder.actionButton.setImageResource(android.R.drawable.ic_media_pause);
        else if(downloadItem.status== DownloadItem.DownloadStatus.FAILED||downloadItem.status== DownloadItem.DownloadStatus.CANCELLED)
            holder.actionButton.setImageResource(android.R.drawable.stat_notify_sync);
        holder.actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadItemAdapterListener.onButtonClicked(downloadItem);
            }
        });

    }



    @Override
    public int getItemCount() {
        return mlist.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView downloadName,downloadSize;
        public ImageView actionButton;
        public ProgressBar downloadProgress;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            downloadName=itemView.findViewById(R.id.downloadName);
            downloadProgress=itemView.findViewById(R.id.downloadProgress);
            downloadSize=itemView.findViewById(R.id.downloadSize);
            actionButton=itemView.findViewById(R.id.actionButton);

        }
    }
    DownloadItemAdapterListener downloadItemAdapterListener;
    DownloadItemAdapter(DownloadItemAdapterListener listener)
    {
        this.downloadItemAdapterListener=listener;
    }
    public interface DownloadItemAdapterListener{
        void onButtonClicked(DownloadItem downloadItem);
    }
}
