package rjt.example.com.macyassessment;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tim on 16/8/31.
 */

//This class provide the scanning progress using notification builder
public class ScannerNotification {

    private static ScannerNotification sScannerNotification = new ScannerNotification();

    public static ScannerNotification getInstance(){return sScannerNotification;}

    private Map<Integer,String> notifications;

    private ScannerNotification(){
        notifications = new HashMap<>();
    }

    public void initialize(int id,String message,Context context){
        notifications.put(id, message);

        Intent intent = new Intent(context,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        Notification notification = mBuilder
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.scanner_notifiaction)
                .setAutoCancel(false)
                .build();

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.progress_scan);
        remoteViews.setTextViewText(R.id.message, message);
        remoteViews.setProgressBar(R.id.progressBar,0,0,true);
        notification.contentView = remoteViews;




        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id,notification);

    }

    public void updateMessage(int id,String message,Context context){
        notifications.put(id,message);
        initialize(id,message,context);


    }

    public void remove(int id){
        notifications.remove(id);
    }


}