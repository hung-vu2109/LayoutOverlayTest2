package com.example.layoutoverlaytest2;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class ApplicationClass extends Application {

    public static final String CHANNEL_ID_1="CHANNEL_1";
    public static final String CHANNEL_ID_2="CHANNEL_2";
    public static final String MY_COMMAND="MY_COMMAND";
    public static final String PLAY_FROM_SONG_LIST="PLAY_FROM_SONG_LIST";
    public static final String REMOVE_SONG = "REMOVE SONG";
    public static final String ACTION_NEXT="NEXT";
    public static final String ACTION_PREV="PREV";
    public static final String ACTION_PLAY="PLAY";
    public static final String ACTION_REPEAT="REPEAT";
    public static final String ACTION_SHUFFLE="SHUFFLE";
    public static final String ACTION_REPEAT_SECTION="REPEAT_SECTION";
    public static final String ACTION_MINI_PLAY = "ACTION_MINI_PLAY";
    public static final String ACTION_STOP = "ACTION_STOP";


    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel1 = new NotificationChannel(CHANNEL_ID_1, "Channel 1", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel1.setDescription("Channel 1 Des");

            NotificationChannel notificationChannel2 = new NotificationChannel(CHANNEL_ID_2, "Channel 2", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel2.setDescription("Channel 2 Des");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel1);
            notificationManager.createNotificationChannel(notificationChannel2);
        }
    }
}
