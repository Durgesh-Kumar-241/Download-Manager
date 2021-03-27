package com.dktechhub.mnnit.downloadmanager;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DownloadService.ObserverActivityInterface {
    public DownloadService downloadService;
    DownloadItemAdapter downloadItemAdapter;
    boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(!isDownloadServiceRunning()){
        Intent intent = new Intent(getBaseContext(),DownloadService.class);
        startService(intent);}
        if(downloadService==null) {
            bindService(new Intent(getBaseContext(), DownloadService.class), downloadServiceConnection, BIND_AUTO_CREATE);
        }
        downloadItemAdapter=new DownloadItemAdapter(new DownloadItemAdapter.DownloadItemAdapterListener() {
            @Override
            public void onButtonClicked(DownloadItem downloadItem) {
                downloadService.manageAction(downloadItem);
            }
        });
        //downloadItemAdapter.mlist=downloadService.items;
        RecyclerView downloadRecyclerView = findViewById(R.id.downloadRecyclerView);
        downloadRecyclerView.setAdapter(downloadItemAdapter);
        downloadRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        //worker=new DatabaseWorker(this,"DownloadManager",null,1);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showDialog());
        /*
        Button start = findViewById(R.id.start);
        Button stop = findViewById(R.id.stop);
        Button bind = findViewById(R.id.bind);
        Button unbind = findViewById(R.id.unbind);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                downloadItem.progress=50;
                downloadItem.status= DownloadItem.DownloadStatus.COMPLETED;
                worker.updateDownload(downloadItem);
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           worker.deleteDownload(downloadItem);
            }
        });

        bind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bindService(new Intent(getBaseContext(),DownloadService.class),downloadServiceConnection,BIND_AUTO_CREATE);
            }
        });

        unbind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBound)
                {
                    unbindService(downloadServiceConnection);
                }
            }
        });

         */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public void showDialog()
    {

        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(this);
        builder.setTitle("Enter a url:");

        // set the custom layout
        final View customLayout
                = getLayoutInflater()
                .inflate(
                        R.layout.layout_dailog,
                        null);
        builder.setView(customLayout);

        // add a button
        builder
                .setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which)
                            {

                                // send data from the
                                // AlertDialog to the Activity
                                EditText editText
                                        = customLayout
                                        .findViewById(
                                                R.id.inputText);
                                onInputReceived(
                                        editText
                                                .getText()
                                                .toString());
                            }
                        });

        // create and show
        // the alert dialog
        AlertDialog dialog
                = builder.create();
        dialog.show();
    }
    DownloadItem downloadItem;
    ProgressDialog progressDialog;
    public void onInputReceived(String input)
    {
     /* downloadItem=  new DownloadItem(input,"path","size", DownloadItem.DownloadStatus.PENDING, DownloadItem.DownloadType.DOCUMENT);
       int id= worker.addDownload(downloadItem);
       downloadItem.id=id;
        Toast.makeText(this, input+id, Toast.LENGTH_SHORT).show();

      */
        LinkAnalyser analyser = new LinkAnalyser(input, new LinkAnalyser.OnAnalyseCompleteListener() {
            @Override
            public void analyseSuccess(DownloadItem downloadItem2) {
                progressDialog.cancel();
                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
               //worker.addDownload(downloadItem2);
               downloadService.addNewTask(downloadItem2);
            }

            @Override
            public void analyseProgress(int progress) {
                progressDialog.setProgress(progress);
            }

            @Override
            public void analyseStart() {
                progressDialog=new ProgressDialog(MainActivity.this);
                progressDialog.setMax(100);
                progressDialog.setMessage("Analysing..please wait");
                progressDialog.setCancelable(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setProgress(0);
                progressDialog.setButton("Cancel" ,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                progressDialog.show();
            }

            @Override
            public void analyseFailed(Exception e) {
                progressDialog.cancel();
                Toast.makeText(MainActivity.this, "failed:"+e.toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
        analyser.execute();
    }


    /*public void manageAction(DownloadItem downloadItem)
    {
        //Toast.makeText(this, downloadItem.name, Toast.LENGTH_SHORT).show();
        if(downloadItem.status== DownloadItem.DownloadStatus.COMPLETED)
        {
            Toast.makeText(this, "Opening:"+downloadItem.name, Toast.LENGTH_SHORT).show();
        }else if(downloadItem.status== DownloadItem.DownloadStatus.DOWNLOADING)
        {
            downloadItem.status= DownloadItem.DownloadStatus.PENDING;
            downloadItem.pauseDownloading();
            downloadItemAdapter.notifyDataSetChanged();
        }else if((downloadItem.status== DownloadItem.DownloadStatus.PENDING)||(downloadItem.status== DownloadItem.DownloadStatus.FAILED)||(downloadItem.status== DownloadItem.DownloadStatus.CANCELLED))
        {
            downloadItem.startDownloading(new DownloadItem.Observer() {
                @Override
                public void notifyParent() {
                    downloadItemAdapter.notifyDataSetChanged();
                }

                @Override
                public void notifyError(Exception e) {
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getBaseContext(),DownloadService.class);
            stopService(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public  ServiceConnection downloadServiceConnection = new ServiceConnection(){


        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadService.DownloadBinder binderBridge = (DownloadService.DownloadBinder) service;
            downloadService=binderBridge.getService();
            downloadItemAdapter.mlist=downloadService.getItems();
            downloadItemAdapter.notifyDataSetChanged();
            downloadService.setObserverActivityInterface(MainActivity.this);
            isBound=true;
            Toast.makeText(MainActivity.this, "service connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            downloadService.setObserverActivityInterface(null);
            isBound=false;
            downloadService=null;
            Toast.makeText(MainActivity.this, "service disconnected", Toast.LENGTH_SHORT).show();
        }
    };
    public boolean isDownloadServiceRunning()
    {   String serviceName=DownloadService.class.getName();
        final ActivityManager activityManager =(ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> runningServiceInfos=activityManager.getRunningServices(Integer.MAX_VALUE);
        for(ActivityManager.RunningServiceInfo temp :runningServiceInfos)
        {
            if(temp.service.getClassName().equals(serviceName))
                return true;
        }
        return false;
    }
    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onItemsUpdate(ArrayList<DownloadItem> items) {
        downloadItemAdapter.notifyDataSetChanged();
    }
}