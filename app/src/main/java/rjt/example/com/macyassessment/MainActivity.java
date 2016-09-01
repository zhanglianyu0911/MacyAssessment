package rjt.example.com.macyassessment;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import rjt.example.com.macyassessment.fragment.ExtentionFragment;
import rjt.example.com.macyassessment.fragment.FileFragment;

/**
 * Created by Tim on 16/8/31.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private BroadcastReceiver scanBroadcastReceiver;

    private Rescan mostRecent;

    private Button startBtn,shareBtn;
    private TextView percView;
    private TextView avgView;

    private FileFragment fileFragment;
    private ExtentionFragment extentionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!isScannerActive()){startService(new Intent(this,ScanService.class));}

        startBtn = (Button) findViewById(R.id.button_start);
        startBtn.setOnClickListener(this);

        Button pauseButton = (Button) findViewById(R.id.button_pause);
        pauseButton.setOnClickListener(this);

        shareBtn = (Button) findViewById(R.id.button_share);
        shareBtn.setOnClickListener(this);
        shareBtn.setVisibility(View.INVISIBLE);

        percView = (TextView)findViewById(R.id.perc_view);
        avgView  = (TextView)findViewById(R.id.avg_view);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.TAG_SCAN+".UPDATE");

        scanBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mostRecent = intent.getParcelableExtra(Constant.TAG_UPDATE);
                refreshUI();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(scanBroadcastReceiver,intentFilter);

        fileFragment = (FileFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_file_data);
        extentionFragment  = (ExtentionFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_ext_data);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(Constant.TAG_UPDATE, mostRecent);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mostRecent = savedInstanceState.getParcelable(Constant.TAG_UPDATE);
        if(mostRecent!=null){refreshUI();}
    }

    /**
     * Send local broadcast to service for start scanning files from SD card
     */
    public void startScan(){
        Intent intent = new Intent(Constant.TAG_MAIN+".START");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Send local broadcast to service to pause scanning files from SD card
     */
    public void pauseScan(){
        Intent intent = new Intent(Constant.TAG_MAIN+".PAUSE");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Send local broadcast to service for start for rescanning files from SD card
     */
    public void requestUpdate(){
        Intent intent = new Intent(Constant.TAG_MAIN+".UPDATE");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Send local broadcast to service for stop scanning
     */
    public void stopScan(){
        Intent intent = new Intent(Constant.TAG_MAIN+".STOP");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Share scanning details via email using intent
     */
    public void shareResults(){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"Receipts"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Scan External Storage");
        i.putExtra(Intent.EXTRA_TEXT   , mostRecent.totalFileSizeInMB + "MBs of data has been scanned.");

        startActivity(Intent.createChooser(i, "Send mail..."));

    }

    /**
     * Check scanning service is running or not
     */
    private boolean isScannerActive(){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ScanService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * After rescan refresh the UI with new updated data
     */
    private void refreshUI() {

        fileFragment.updateData(mostRecent);
        extentionFragment.updateData(mostRecent);

        if (mostRecent.isDone == 1) {shareBtn.setVisibility(View.VISIBLE);
        }else{shareBtn.setVisibility(View.INVISIBLE);}

        percView.setText("Total:" + mostRecent.totalFileSizeInMB + "MBs");
        avgView.setText("Avg:" + (float) mostRecent.aveFileSize + "MBs");

    }

    /**
     * Click events for buttons
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_start:
                startScan();
                break;
            case R.id.button_pause:
                pauseScan();
                break;
            case R.id.button_share:
                shareResults();
                break;
        }
    }

    /**
     * When user press back button then service stop for scanning and also kill activity
     */
    @Override
    public void onBackPressed() {
        stopScan();
        finish();
    }

}

