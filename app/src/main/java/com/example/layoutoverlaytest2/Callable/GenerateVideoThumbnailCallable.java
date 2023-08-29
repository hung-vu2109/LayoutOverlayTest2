package com.example.layoutoverlaytest2.Callable;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import java.io.IOException;
import java.util.concurrent.Callable;

public class GenerateVideoThumbnailCallable implements Callable<Bitmap> {
    Bitmap bitmap;
    String path;

    public GenerateVideoThumbnailCallable(String path) {
        this.path = path;
    }

    @Override
    public Bitmap call() throws Exception {
        bitmap = createThumbnail(path);
        return bitmap;
    }

    public static synchronized Bitmap createThumbnail(String path) {
        MediaMetadataRetriever mediaMetadataRetriever = null;
        Bitmap bitmap = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(path);
            bitmap = mediaMetadataRetriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mediaMetadataRetriever != null) {
                try {
                    mediaMetadataRetriever.release();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bitmap;
    }
}
