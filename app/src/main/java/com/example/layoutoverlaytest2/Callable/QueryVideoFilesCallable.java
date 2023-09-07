package com.example.layoutoverlaytest2.Callable;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class QueryVideoFilesCallable implements Callable<ArrayList<VideoModel>>{

    Context context;
    ArrayList<VideoModel> videoModelArrayList;
    Bitmap bitmap;

    public QueryVideoFilesCallable(Context context, ArrayList<VideoModel> videoModelArrayList) {
        this.context = context;
        this.videoModelArrayList = videoModelArrayList;
    }

    @Override
    public synchronized ArrayList<VideoModel> call() throws Exception {
        videoModelArrayList = queryVideoFiles();
        return videoModelArrayList;
    }
    final String TAG = "Query Video Files ";
    public ArrayList<VideoModel> queryVideoFiles(){
        Log.d(TAG + "queryVideoFiles method", " started");
        videoModelArrayList = new ArrayList<>();
        Uri collectionUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
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
        try(Cursor cursor = context.getContentResolver().query(collectionUri, projection, null, null, sortOrder)){

            Log.d(TAG + "query video files", "starting...");
            Log.d(TAG + "Current Thread ", Thread.currentThread().getName()+" starting...");
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            int widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH);
            int heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT);

            while (cursor.moveToNext()){
                long videoId = cursor.getLong(idColumn);
                String videoName = cursor.getString(titleColumn);
                String videoDuration = cursor.getString(durationColumn);
                String videoSize = cursor.getString(sizeColumn);
                String videoPath = cursor.getString(dataColumn);
                String videoWidth = cursor.getString(widthColumn);
                String videoHeight = cursor.getString(heightColumn);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoId);

                VideoModel videoModel = new VideoModel(contentUri, videoName, null, videoDuration, videoSize, videoPath, videoWidth, videoHeight);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        bitmap = context.getApplicationContext().getContentResolver().loadThumbnail(videoModel.getContentUri(), new Size(800, 400), null);
                        videoModel.setThumbnailVideo(bitmap);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    bitmap = null;
                }
                if (new File(videoModel.getPathVideo()).exists()){
                    videoModelArrayList.add(videoModel);
                }

            }
        }
        Log.d(TAG + "query video files", "finished !!");
        return videoModelArrayList;
    }




}
