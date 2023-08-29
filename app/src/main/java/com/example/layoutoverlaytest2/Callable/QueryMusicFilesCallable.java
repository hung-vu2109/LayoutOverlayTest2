package com.example.layoutoverlaytest2.Callable;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.example.layoutoverlaytest2.Models.Song.SongModel;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class QueryMusicFilesCallable implements Callable<ArrayList<SongModel>>{

    Context context;
    ArrayList<SongModel> songModelArrayList;

    public QueryMusicFilesCallable(Context context, ArrayList<SongModel> songModelArrayList) {
        this.context = context;
        this.songModelArrayList = songModelArrayList;
    }

    @Override
    public synchronized ArrayList<SongModel> call() throws Exception {
        songModelArrayList = queryMusicFiles();
        return songModelArrayList;
    }
    final String TAG = "Query Song Files";
    private ArrayList<SongModel> queryMusicFiles(){

        songModelArrayList = new ArrayList<>();

        Log.d(TAG + "queryMusicFiles method", " started");
//      Query media files from shared storage
        Uri collectionUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collectionUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collectionUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        String[] projection = new String[]{
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Log.d(TAG + "query song files", "starting...");
        try (Cursor cursor = context.getContentResolver().query(collectionUri, projection, selection, null, sortOrder)) {

//            int thumbnailColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
//            int _thumbnailId = cursor.getInt(thumbnailColumn);
//            Uri thumbnailUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, _thumbnailId);
//            Bitmap bitmap = getContext().getContentResolver().loadThumbnail(thumbnailUri, new Size(64, 64), null);

            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

            while (cursor.moveToNext()) {
                String nameSong = cursor.getString(titleColumn);
                String duration = cursor.getString(durationColumn);
                String path = cursor.getString(pathColumn);

                SongModel songData = new SongModel(path, nameSong, duration);
                if (new File(songData.getPath()).exists()) {
                    songModelArrayList.add(songData);
                }
            }

        }

        Log.d(TAG + "query song files", "finished !!");

        return songModelArrayList;
    }

}
