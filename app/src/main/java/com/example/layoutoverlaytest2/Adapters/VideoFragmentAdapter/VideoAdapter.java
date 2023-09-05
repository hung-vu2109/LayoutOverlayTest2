package com.example.layoutoverlaytest2.Adapters.VideoFragmentAdapter;

import static com.example.layoutoverlaytest2.ApplicationClass.MY_COMMAND;
import static com.example.layoutoverlaytest2.ApplicationClass.PLAY_FROM_SONG_LIST;
import static com.example.layoutoverlaytest2.ApplicationClass.PLAY_FROM_VIDEO_LIST;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.layoutoverlaytest2.Callable.GenerateVideoThumbnailCallable;
import com.example.layoutoverlaytest2.Models.Video.VideoModel;
import com.example.layoutoverlaytest2.R;
import com.example.layoutoverlaytest2.Services.NotificationService;
import com.example.layoutoverlaytest2.Utils.MyInitialMediaPlayer;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class VideoAdapter extends RecyclerView.Adapter<VideoViewHolder> {

    private static final String TAG = "Video Adapter ";
    private static final String LRU_CACHE_TAG = " LruCache Tag ";
    Context context;
    List<VideoModel> videoModelArrayList;
    View view;
    boolean isGrid;
    private final LruCache<String, Bitmap> bitmapLruCache;
    public VideoAdapter(Context context, List<VideoModel> videoModelArrayList, Boolean isGrid) {
        this.context = context;
        this.videoModelArrayList = videoModelArrayList;
        this.isGrid = isGrid;

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        Log.d(TAG+LRU_CACHE_TAG, " max Memory: " + maxMemory);

        final int cacheSize = maxMemory / 8;
        Log.d(TAG+LRU_CACHE_TAG, " cache Size: " + cacheSize);

        bitmapLruCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        if (isGrid) {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_video_recycler_item_grid, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_video_recycler_item_linear, parent, false);
        }

        for (VideoModel i : videoModelArrayList) {
            if (i.getThumbnailVideo() != null) {
                addBitmapToMemoryCache(i.getPathVideo(), i.getThumbnailVideo());
            }
        }

        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, @SuppressLint("RecyclerView") int position) {

        VideoModel videoModel = videoModelArrayList.get(position);
        holder.videoFragment_title_tv.setText(videoModel.getTitleVideo());
        holder.videoFragment_duration_tv.setText(formatLongToHHMMSS(videoModel.getDurationVideo()));
        holder.videoFragment_size_tv.setText(formatSize(videoModel.getSizeVideo()));


        if (videoModel.getThumbnailVideo() != null){
            holder.videoFragment_thumbnail_img.setImageBitmap(getBitmapFromMemCache(videoModel.getPathVideo()));
        } else {
            loadBitmap(videoModel.getPathVideo(), holder.videoFragment_thumbnail_img);
        }


        holder.itemView.setOnClickListener(view -> {
            Log.d(TAG+ " onBindViewHolder", videoModel.getTitleVideo()+" is Clicked");
            MyInitialMediaPlayer.starterIndex = position;
            MyInitialMediaPlayer.isMusic = false;

            Log.d("VideoAdapter", String.valueOf(MyInitialMediaPlayer.starterIndex));


            Intent intent = new Intent(context, NotificationService.class);

//            Bundle bundle = new Bundle();
//            bundle.putSerializable("SONG_BUNDLE", songModelArrayList);
//            intent.putExtra("MY_BUNDLE", bundle);
//            intent.putExtra("SONG_LIST", songModelArrayList);

            intent.putExtra(MY_COMMAND, PLAY_FROM_VIDEO_LIST);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startService(intent);
        });
    }

    @Override
    public int getItemCount() {
        return videoModelArrayList.size();
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            bitmapLruCache.put(key, bitmap);
        }
    }
    public Bitmap getBitmapFromMemCache(String key) {
        return bitmapLruCache.get(key);
    }
    private void loadBitmap(String key, ImageView imageView){
        final Bitmap bitmap = getBitmapFromMemCache(key);
        if (bitmap != null){
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.baseline_smart_display_24);
            generateThumbnail(key, imageView);
        }
    }

    private void generateThumbnail(String path, ImageView imageView){
        boolean isRunning = true;
        final Bitmap[] bitmap1 = new Bitmap[1];
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        GenerateVideoThumbnailCallable generateThumbnailCallable = new GenerateVideoThumbnailCallable(path);
        FutureTask<Bitmap> bitmapFutureTask = new FutureTask<Bitmap>(generateThumbnailCallable){
            @Override
            protected void set(Bitmap bitmap) {
                bitmap1[0] = bitmap;
            }
        };

        executorService.execute(bitmapFutureTask);

        while (isRunning) {
            if (bitmapFutureTask.isDone()) {
                Log.d(TAG + " generateThumbnail", bitmap1[0] + "");
                imageView.setImageBitmap(bitmap1[0]);
                executorService.shutdown();
                isRunning = false;
            }
        }
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
