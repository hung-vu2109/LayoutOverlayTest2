package com.example.layoutoverlaytest2.BroadcastReceiver;

import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_NEXT;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_PLAY;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_PREV;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_REPEAT;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_STOP;
import static com.example.layoutoverlaytest2.ApplicationClass.MY_COMMAND;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.example.layoutoverlaytest2.Services.NotificationService;

public class NotificationReceiver extends BroadcastReceiver {
    static final String TAG = "Notification Receiver ";
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("Notification Receiver", "onReceive");

        Intent intent1 = new Intent(context, NotificationService.class);
        if (intent.getAction() != null){
            switch (intent.getAction()){
                case ACTION_PREV:
                    Log.d(TAG, "notification prev clicked");
                    intent1.putExtra(MY_COMMAND, intent.getAction());
                    Log.e(TAG+"onReceive", intent1 +"");
                    context.startService(intent1);
                    break;

                case ACTION_PLAY:
                    Log.d(TAG, "notification play clicked");
                    intent1.putExtra(MY_COMMAND, intent.getAction());
                    Log.e(TAG+"onReceive", intent1 +"");
                    context.startService(intent1);
                    break;

                case ACTION_NEXT:
                    Log.d(TAG, "notification next clicked");
                    intent1.putExtra(MY_COMMAND, intent.getAction());
                    Log.e(TAG+"onReceive", intent1 +"");
                    context.startService(intent1);
                    break;

                case ACTION_REPEAT:
                    Log.d(TAG, "notification repeat clicked");
                    intent1.putExtra(MY_COMMAND, intent.getAction());
                    Log.e(TAG+"onReceive", intent1 +"");
                    context.startService(intent1);
                    break;

                case ACTION_STOP:
                    Log.d(TAG, "notification stop clicked");
                    intent1.putExtra(MY_COMMAND, intent.getAction());
                    Log.e(TAG+"onReceive", intent1 +"");
                    context.startService(intent1);
                    break;
            }
        }
    }
}
