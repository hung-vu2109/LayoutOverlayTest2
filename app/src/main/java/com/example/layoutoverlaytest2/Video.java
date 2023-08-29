package com.example.layoutoverlaytest2;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;

import com.example.layoutoverlaytest2.Models.Video.VideoModel;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Video {

    static final String TAG = "VideoService";
    Context context;
    Runnable myRunnable1;
    VideoModel videoModel;
    Bitmap bitmap;

    public VideoModel queryFiles() {
        Log.d(TAG + "queryVideoFiles method", " started");
        Uri collectionUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collectionUri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collectionUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT
        };
        //        String selection =
        String sortOrder = MediaStore.Video.Media.TITLE + " ASC";

//        try catch with resource java 7
        try (Cursor cursor = context.getContentResolver().query(collectionUri, projection, null, null, sortOrder)) {

            Log.d(TAG + "query video files", "starting...");
            Log.d(TAG + "Current Thread ", Thread.currentThread().getName() + " starting...");
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            int widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH);
            int heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT);

            while (cursor.moveToNext()) {
                long videoId = cursor.getLong(idColumn);
                String videoName = cursor.getString(titleColumn);
                String videoDuration = cursor.getString(durationColumn);
                String videoSize = cursor.getString(sizeColumn);
                String videoPath = cursor.getString(dataColumn);
                String videoWidth = cursor.getString(widthColumn);
                String videoHeight = cursor.getString(heightColumn);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoId);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        bitmap = context.getApplicationContext().getContentResolver().loadThumbnail(videoModel.getContentUri(), new Size(800, 400), null);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    bitmap = null;
                }
                videoModel = new VideoModel(contentUri, videoName, bitmap, formatLongToHHMMSS(videoDuration), formatSize(videoSize), videoPath, videoWidth, videoHeight);


            }
        }
        return videoModel;
    }

    @SuppressLint("DefaultLocale")
    private String formatLongToHHMMSS(String duration){
        long milliSec = Long.parseLong(duration);
//        hours can be greater than 60, because i want it
        long hour = TimeUnit.MILLISECONDS.toHours(milliSec);
        long min = TimeUnit.MILLISECONDS.toMinutes(milliSec) % 60;
        long sec = TimeUnit.MILLISECONDS.toSeconds(milliSec) % 60;
        if (hour>0) {
            return String.format("%02d:%02d:%02d", hour, min, sec);
        } else {
            return String.format("%02d:%02d", min, sec);
        }
    }
    private String formatSize(String size){
        String sizeVideo = "size video";
        double bytes = Double.parseDouble(size);
        int i=0;
        double sizeResult = bytes;
        while (sizeResult >= 1024){
            Log.d("i values before", i+"");
            sizeResult = sizeResult / 1024;
            i+=1;

            Log.d("i values after", i+" "+" sizeResult "+sizeResult+"");
        }
        switch (i){
            case 0:
                return new DecimalFormat("###.##").format(sizeResult) + " B";
            case 1:
                return new DecimalFormat("###.##").format(sizeResult) + " KB";
            case 2:
                return new DecimalFormat("###.##").format(sizeResult) + " MB";
            case 3:
                return new DecimalFormat("###.##").format(sizeResult) + " GB";
            case 4:
                return new DecimalFormat("###.##").format(sizeResult) + " TB";
        }
        Log.d("gi do", "gi gi do");
        return sizeVideo;
    }
}
