package com.example.layoutoverlaytest2.Utils;

import android.media.MediaPlayer;

public class MyInitialMediaSongPlayer {

    static MediaPlayer instance;

    public static MediaPlayer getInstance(){
        if (instance == null){
            instance = new MediaPlayer();
        }
        return instance;
    }

    public static int starterIndex = -0;
    public static int removeIndex = -1;
    public static boolean isStarted = false;

    public static void setStarterIndex(int starterIndex) {
        MyInitialMediaSongPlayer.starterIndex = starterIndex;
    }
}
