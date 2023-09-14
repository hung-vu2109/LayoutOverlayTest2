package com.example.layoutoverlaytest2.Fragments;

import static com.example.layoutoverlaytest2.ApplicationClass.isAliveMainActivity;
import static com.example.layoutoverlaytest2.Services.NotificationService.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.layoutoverlaytest2.MyCustomTextureView;
import com.example.layoutoverlaytest2.R;
import com.example.layoutoverlaytest2.Services.NotificationService;

import java.io.IOException;

public class VideoTextureViewFragment extends Fragment implements TextureView.SurfaceTextureListener{
    final String TAG = "VideoTextureViewFragment ";
    Context context;
    MediaPlayer mediaPlayer;
    MyCustomTextureView textureView;
    String pathVideo;
    int currentPosition;
    boolean isPlayingVideo;
    boolean seekTo = true;
    int widthOrigin;
    int heightOrigin;

    RelativeLayout.LayoutParams layoutParams;
    public static int currentSizeTextureView_width;
    public static int currentSizeTextureView_height;
    int widthChange = 0;
    int heightChange = 0;


    public VideoTextureViewFragment(Context context, MediaPlayer mediaPlayer, String pathVideo, int currentPosition, boolean isPlayingVideo, String widthOrigin, String heightOrigin) {
        this.context = context;
        this.mediaPlayer = mediaPlayer;
        this.pathVideo = pathVideo;
        this.currentPosition = currentPosition;
        this.isPlayingVideo = isPlayingVideo;
        this.widthOrigin = Integer.parseInt(widthOrigin);
        this.heightOrigin = Integer.parseInt(heightOrigin);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, " onCreate");
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, " onCreateView");
        View view = inflater.inflate(R.layout.fragment_video_texture_view, container, false);
        // keep Screen On
        view.setKeepScreenOn(true);
        textureView = (MyCustomTextureView) (view.findViewById(R.id.textureView_video_fragment));
        textureView.setSurfaceTextureListener(this);
        Log.d(TAG, " CurrentPositionToContinue: "+currentPosition+"");



        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, " onStart");
        super.onStart();

        int w = 1080;
        int h = 870;
        Size size = getSizeToFitScreen(w, h, widthOrigin, heightOrigin);
        widthChange = size.getWidth();
        heightChange = size.getHeight();
        layoutParams = new RelativeLayout.LayoutParams(widthChange, heightChange);
        textureView.setLayoutParams(layoutParams);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Log.d(TAG, " onConfigurationChanged");
        super.onConfigurationChanged(newConfig);

        int currentOrientation = newConfig.orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT){
            Log.d(TAG+ " onConfigurationChanged", " PORTRAIT");
            int w = 1080;
            int h = 870;
            Size size = getSizeToFitScreen(w, h, widthOrigin, heightOrigin);
            widthChange = size.getWidth();
            heightChange = size.getHeight();
            layoutParams = new RelativeLayout.LayoutParams(widthChange, heightChange);
            textureView.setLayoutParams(layoutParams);

        } else if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            Log.d(TAG+ " onConfigurationChanged", " LANDSCAPE");

            int w = 2198;
            int h = 870;
            Size size = getSizeToFitScreen(w, h, widthOrigin, heightOrigin);
            widthChange = size.getWidth();
            heightChange = size.getHeight();
            layoutParams = new RelativeLayout.LayoutParams(widthChange, heightChange);
            textureView.setLayoutParams(layoutParams);
        }

    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        Log.d(TAG, " onSurfaceTextureAvailable"+ " width: "+width+ " "+ " height: "+ height);
        startNewVideo(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
        Log.d(TAG, " onSurfaceTextureSizeChanged" + " width: "+width+ " "+ " height: "+ height);
        Size size = getSizeToFitScreen(width, height, widthOrigin, heightOrigin);
        surface.setDefaultBufferSize(size.getWidth(), size.getHeight());
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        Log.d(TAG, " onSurfaceTextureDestroyed");
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
//        Log.d(TAG, " onSurfaceTextureUpdated");
        if (currentPosition > -1  && isAliveMainActivity && seekTo){
            Log.d(TAG, " Seek To Current Position");
            mediaPlayer.seekTo(currentPosition);
            if (!isPlayingVideo){
                mediaPlayer.pause();
            }
            seekTo = false;
        }
    }

    private void startNewVideo(@NonNull SurfaceTexture surface){
        Log.d(TAG, " startNewVideo");
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(pathVideo);
            Log.d(TAG, " start from "+ TAG);
            mediaPlayer.setSurface(new Surface(surface));
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Size getSizeToFitScreen(int widthFrame, int heightFrame, int widthOrigin, int heightOrigin){
        double widthResult = 0;
        double heightResult = 0;

        double widthTestWidth;
        double heightTestWidth;

        double widthTestHeight;
        double heightTestHeight;

        if ((double) widthFrame > 0 && (double) heightFrame > 0 && (double) widthOrigin > 0 && (double) heightOrigin > 0) {
            double ratio = (double) widthOrigin / (double) heightOrigin;

            // scale with width
            widthTestWidth = (double) widthFrame;
            heightTestWidth = (double) widthFrame / ratio;

            // scale with height
            widthTestHeight = (double) heightFrame * ratio;
            heightTestHeight = (double) heightFrame;

            if (heightTestWidth <= heightFrame) {
                widthResult = widthTestWidth;
                heightResult = heightTestWidth;
            } else if (widthTestHeight <= widthFrame) {
                widthResult = widthTestHeight;
                heightResult = heightTestHeight;
            }

        } else {
            Log.d(TAG + " getSizeToFitScreen", " size = 0");
        }

        return new Size((int) Math.round(widthResult), (int) Math.round(heightResult));
    }

}
