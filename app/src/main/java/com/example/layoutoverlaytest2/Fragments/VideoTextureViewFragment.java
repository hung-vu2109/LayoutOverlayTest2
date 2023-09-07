package com.example.layoutoverlaytest2.Fragments;

import static com.example.layoutoverlaytest2.ApplicationClass.isAliveMainActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.layoutoverlaytest2.R;

import java.io.IOException;

public class VideoTextureViewFragment extends Fragment implements TextureView.SurfaceTextureListener {
    final String TAG = "VideoTextureViewFragment ";
    Context context;
    MediaPlayer mediaPlayer;
    TextureView textureView;
    String pathVideo;
    int currentPosition;
    boolean isPlayingVideo;
    boolean seekTo = true;


    public VideoTextureViewFragment(Context context, MediaPlayer mediaPlayer, String pathVideo, int currentPosition, boolean isPlayingVideo) {
        this.context = context;
        this.mediaPlayer = mediaPlayer;
        this.pathVideo = pathVideo;
        this.currentPosition = currentPosition;
        this.isPlayingVideo = isPlayingVideo;
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
        textureView = view.findViewById(R.id.textureView_video_fragment);
        textureView.setSurfaceTextureListener(this);
        Log.d(TAG, " CurrentPositionToContinue: "+currentPosition+"");

        return view;
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        Log.d(TAG, " onSurfaceTextureAvailable");
        startNewVideo(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
        Log.d(TAG, " onSurfaceTextureSizeChanged");
        if (!isAliveMainActivity) {
            onSurfaceTextureDestroyed(surface);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        Log.d(TAG, " onSurfaceTextureDestroyed");
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
        Log.d(TAG, " onSurfaceTextureUpdated");
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
            mediaPlayer.setSurface(new Surface(surface));
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
//            textureView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
