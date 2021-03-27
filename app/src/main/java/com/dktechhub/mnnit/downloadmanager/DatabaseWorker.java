package com.dktechhub.mnnit.downloadmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DatabaseWorker extends SQLiteOpenHelper {
    final String databaseName="DownloadManager";
    final String tablename="Downloads";

    public DatabaseWorker(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE if Not exists Downloads (id INT,name TEXT,progress INT,path TEXT,size TEXT,type TEXT,status TEXT,url TEXT,mime TEXT, PRIMARY KEY(id))";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String DROP_TABLE="DROP TABLE IF EXISTS Downloads";
        db.execSQL(DROP_TABLE);
        this.onCreate(db);
    }
    public int addDownload(DownloadItem downloadItem)
    {    SQLiteDatabase db = this.getWritableDatabase();
        int id = getItemCount()+1;
        ContentValues contentValues = new ContentValues();
        contentValues.put("name",downloadItem.name);
        contentValues.put("progress",downloadItem.progress);
        contentValues.put("path",downloadItem.path);
        contentValues.put("size",downloadItem.size);
        contentValues.put("url",downloadItem.url);
        contentValues.put("mime",downloadItem.mime);
        contentValues.put("status", String.valueOf(downloadItem.status));
        contentValues.put("id",id);
        contentValues.put("type", String.valueOf(downloadItem.type));
        db.insert("Downloads",null,contentValues);
        return id;
    }

    public ArrayList<DownloadItem> loadItems()
    {
        ArrayList<DownloadItem> all = new ArrayList<>();
        String SELECT_QWERY = "SELECT * FROM Downloads";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(SELECT_QWERY, null);
        if (cursor.moveToFirst()) {
            do {
                DownloadItem downloadItem=new DownloadItem(cursor.getString(1),cursor.getString(3),cursor.getString(4), DownloadItem.DownloadStatus.valueOf(cursor.getString(6)), DownloadItem.DownloadType.valueOf(cursor.getString(5)));
                downloadItem.id=cursor.getInt(0);
                downloadItem.url=cursor.getString(7);
                downloadItem.mime=cursor.getString(8);
                downloadItem.progress=cursor.getInt(2);
                all.add(downloadItem);
            } while (cursor.moveToNext());
        }
        return all;
    }
    public void deleteDownload(DownloadItem downloadItem)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String DELETE_DOWNLOAD="DELETE FROM Downloads WHERE id="+downloadItem.id;
        db.execSQL(DELETE_DOWNLOAD);
    }

    public void updateDownload(DownloadItem downloadItem)
    {   SQLiteDatabase db = this.getWritableDatabase();
        String UPDATE_STR = "UPDATE Downloads set status='"+downloadItem.status+"',progress="+downloadItem.progress+" WHERE id="+downloadItem.id;
        db.execSQL(UPDATE_STR);
    }

    public void saveToDatabase(ArrayList<DownloadItem> toUpdate)
    {
        for(DownloadItem temp:toUpdate)
        {
            updateDownload(temp);
        }
    }
    public int getItemCount()
    {   int id = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        String COUNT_QWERY ="SELECT MAX(id) FROM Downloads";
        Cursor cursor = db.rawQuery(COUNT_QWERY, null);
        if(cursor.moveToFirst())
        {
           id=  cursor.getInt(0);
        }
        if(cursor!=null)
        cursor.close();
        return id;
    }
}
