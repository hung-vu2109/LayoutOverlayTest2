package com.example.layoutoverlaytest2.Services;


import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_NEXT;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_PLAY;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_PREV;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_REPEAT;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_SHUFFLE;
import static com.example.layoutoverlaytest2.ApplicationClass.MY_COMMAND;
import static com.example.layoutoverlaytest2.ApplicationClass.PLAY_FROM_SONG_LIST;
import static com.example.layoutoverlaytest2.ApplicationClass.REMOVE_SONG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import com.example.layoutoverlaytest2.Models.ButtonMainObject;
import com.example.layoutoverlaytest2.Models.SongModel;
import com.example.layoutoverlaytest2.Models.TextViewMainObject;
import com.example.layoutoverlaytest2.MyInitialMediaSongPlayer;
import com.example.layoutoverlaytest2.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class NotificationService extends Service implements  MediaPlayer.OnErrorListener{

    static final String TAG = "NotificationService ";
    static final String BUNDLE_NAME = "MY_BUNDLE";
    static final String DATA_KEY = "SONG_BUNDLE";
    private static final int REQUEST_CODE = 592431;
    static final String[] RUNTIME_PERMISSION = { Manifest.permission.READ_EXTERNAL_STORAGE };
    MediaPlayer mediaPlayer = MyInitialMediaSongPlayer.getInstance();
    ArrayList<SongModel> songModelArrayList = new ArrayList<>();
    ArrayList<SongModel> currentArrayList = new ArrayList<>();
    private IBinder mBinder = new MyBinder();
    SongModel currentSong;
    boolean isShuffleSongs = false;
    int pressedTimes = 0;
    boolean loopAllSongs = false, loopOneSong = false;


    public NotificationService(){}



        @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, " onCreate");

//        check song list from adapter
            if (songModelArrayList == null || songModelArrayList.size() == 0) {
                Log.d(TAG, "songModelArrayList: NULL");
                queryMusicFiles();
            } else {
//            for (SongModel i : songModelArrayList) {
//                Log.d(TAG, String.valueOf(i));
//            }
            }


            initialMusicPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

////        get song list from MusicFragment
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//
//                /// ArrayList<? extends ArrayList>
//                Bundle intentBundleExtra = intent.getBundleExtra(BUNDLE_NAME);
//                songModelArrayList = (ArrayList<SongModel>) intentBundleExtra.getSerializable(DATA_KEY, null);
//            } else {
//
////            The Bundle class is highly optimized for marshalling and unmarshalling using parcels.
//                Bundle intentBundleExtra = intent.getBundleExtra("MY_BUNDLE");
//                songModelArrayList = (ArrayList<SongModel>) intentBundleExtra.getSerializable(DATA_KEY);
//            }
//        } catch (Exception e){
//            e.printStackTrace();
//        }





                String actionCommand = intent.getStringExtra(MY_COMMAND);
                if (actionCommand != null) {

                    switch (actionCommand) {
                        case PLAY_FROM_SONG_LIST:
                            Log.d(TAG, "Play from song list");
                            setInitialDataSource();
                            break;
                        case REMOVE_SONG:
                            Log.d(TAG, "Remove song from list");
                            removeSongInPlaylist();
                            break;
                        case ACTION_PLAY:


                                Log.d(TAG, "Play clicked");

                                playOrPauseSong();
                                if (currentSong == null) {
                                    setInitialDataSource();
                                }

                            break;
                        case ACTION_NEXT:

                                Log.d(TAG, "Next clicked");

                                playNextSong();
                                setInitialDataSource();

                            break;
                        case ACTION_PREV:

                                Log.d(TAG, "Prev clicked");

                                playPrevSong();
                                setInitialDataSource();
                            break;
                        case ACTION_SHUFFLE:

                            Log.d(TAG, "Shuffle clicked");
                            isShuffleSongs = !isShuffleSongs;
                            break;

                        case ACTION_REPEAT:

                            Log.d(TAG, "Loop clicked");
                            pressedTimes+=1;
                            if (pressedTimes == 3){
                                pressedTimes = 0;
                            }

                            Log.d(TAG, "pressedTimes Value: "+ pressedTimes);
                            break;

                    }
                } else {
                    Log.e(TAG+"actionCommand", "NULL");
                }



        return START_STICKY;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnBind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onReBind");
        super.onRebind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

//    @Override
//    public void onCompletion(MediaPlayer mediaPlayer) {
//        Log.d(TAG+" MediaPlayer", "onCompletion");
//    }

//    @Override
//    public void onPrepared(MediaPlayer mediaPlayer) {
//        Log.d(TAG+" MediaPlayer", "onPrepared");
//    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.d(TAG+" MediaPlayer", "onError");
        mediaPlayer.reset();
        return false;
    }


    public class MyBinder extends Binder{
        public NotificationService getService(){
            return NotificationService.this;
        }
    }


    private void queryMusicFiles(){

        Log.d(TAG+"queryMusicFiles method", " started");
//      Query media files from shared storage
        Uri collectionUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
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

        Log.d(TAG+"query files", "starting...");
        try (Cursor cursor = this.getContentResolver().query(collectionUri, projection, selection, null, sortOrder)){

//            int thumbnailColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
//            int _thumbnailId = cursor.getInt(thumbnailColumn);
//            Uri thumbnailUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, _thumbnailId);
//            Bitmap bitmap = getContext().getContentResolver().loadThumbnail(thumbnailUri, new Size(64, 64), null);

            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int  pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);


            while (cursor.moveToNext()){
                String nameSong = cursor.getString(titleColumn);
                String duration = cursor.getString(durationColumn);
                String path = cursor.getString(pathColumn);

                SongModel songData = new SongModel(path, nameSong, duration);
                if (new File(songData.getPath()).exists()){
                    songModelArrayList.add(songData);
                }
            }

        }
        Log.d(TAG+"query files", "finished !!");
    }
    public void initialMusicPlayer() {

        Log.d(TAG, "initialMusicPlayer");
//        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
//        mediaPlayer.setOnPreparedListener(this);
//        mediaPlayer.prepareAsync();
        mediaPlayer.setOnErrorListener(this);
//        mediaPlayer.setOnCompletionListener(this);

    }

    public void setInitialDataSource(){
        Log.d(TAG, "setInitialDatasource");
        Log.d(TAG+"Song Position", String.valueOf(MyInitialMediaSongPlayer.starterIndex));
        currentSong = songModelArrayList.get(MyInitialMediaSongPlayer.starterIndex);


        playMusic();
    }

    public void removeSongInPlaylist(){
        Log.d(TAG, "Remove the song "+ MyInitialMediaSongPlayer.removeIndex);
        Log.d(TAG, "Total songs before "+ songModelArrayList.size());
        if (MyInitialMediaSongPlayer.removeIndex >= 0){
            songModelArrayList.remove(MyInitialMediaSongPlayer.removeIndex);
        }
        Log.d(TAG, "Total songs after "+ songModelArrayList.size());
    }
    public void updateUiFromService(Activity activity, ButtonMainObject buttonMainObject, TextViewMainObject textViewMainObject, SeekBar seekBar){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

//                Play Button UI
                if (mediaPlayer.isPlaying()) {
                    buttonMainObject.getPlayBtn().setBackgroundResource(R.drawable.baseline_pause_circle_outline_24);
                } else {
                    buttonMainObject.getPlayBtn().setBackgroundResource(R.drawable.baseline_play_circle_outline_24);
                }

//                Shuffle Button UI
                if (isShuffleSongs) {
                    buttonMainObject.getShuffleBtn().setBackgroundResource(R.drawable.baseline_shuffle_on_24);
                } else {
                    buttonMainObject.getShuffleBtn().setBackgroundResource(R.drawable.baseline_shuffle_24);
                }

//                Loop Button UI
                switch (pressedTimes) {
                    case 0:
                        // no loop
                        loopAllSongs = false;
                        loopOneSong = false;
                        Log.d("LoopAllSongs", "No Loop");
                        buttonMainObject.getLoopBtn().setBackgroundResource(R.drawable.baseline_repeat_24);
                        break;
                    case 1:
                        // loop 1 songs
                        loopAllSongs = false;
                        loopOneSong = true;
                        Log.d("LoopAllSongs", "loopCurrentSongs "+"loopAllSong: "+ "false");
                        buttonMainObject.getLoopBtn().setBackgroundResource(R.drawable.baseline_repeat_one_on_24);
                        break;
                    case 2:
                        // loop all song
                        loopAllSongs = true;
                        loopOneSong = false;
                        loopAllSong();
                        Log.d("LoopAllSongs", "loopAllSongs: "+ loopAllSongs);
                        buttonMainObject.getLoopBtn().setBackgroundResource(R.drawable.baseline_repeat_on_24);
                        break;
                }

//                Title Song Name
                String songName = null;
                if (currentSong != null){
                     songName = currentSong.getTitle();
                }
                if (songName == null){
                    songName = "Song Name";
                }

                textViewMainObject.getSongNameTv().setText(songName);


//                Title Start Time
                if (mediaPlayer != null){
                    textViewMainObject.getCurrentTimeTv().setText(formatLongToMMSS(String.valueOf(mediaPlayer.getCurrentPosition())));
                }

//                Title End Time
                if (mediaPlayer != null && currentSong != null){
                    textViewMainObject.getEndTimeTv().setText(formatLongToMMSS(String.valueOf(currentSong.getDuration())));
                }


//                SeekBar
                if (mediaPlayer != null){
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {

                    seekBar.setProgress(0);
                    seekBar.setMax(mediaPlayer.getDuration());
                        }
                    });

                    seekBar.setProgress(mediaPlayer.getCurrentPosition());

                    if (mediaPlayer.isPlaying()){
                        buttonMainObject.getPlayBtn().setBackgroundResource(R.drawable.baseline_pause_circle_outline_24);
                    } else {
                        buttonMainObject.getPlayBtn().setBackgroundResource(R.drawable.baseline_play_circle_outline_24);
                    }

                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            if (b) {
                                mediaPlayer.seekTo(i);
                                mediaPlayer.start();
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                }
                new Handler().postDelayed(this, 1000);
            }
        });

    }


    private void playMusic() {

        Log.d(TAG, "playMusic method");
//        mediaPlayer.reset();
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setLooping(loopOneSong);
            // set Min Max seekbar
//            seekBar.setProgress(0);
//            seekBar.setMax(mediaPlayer.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playPrevSong(){
        Log.d(TAG + "playPrevSong Method", "is clicked");
        if(isShuffleSongs){
            Log.d(TAG, "starting generate Random song");
            generateRandomSong();
        } else {
            if (MyInitialMediaSongPlayer.starterIndex == 0) {
               MyInitialMediaSongPlayer.starterIndex = songModelArrayList.size();
            }
            MyInitialMediaSongPlayer.starterIndex -= 1;
        }


//        currentSong = songModelArrayList.get(MyInitialMediaSongPlayer.starterIndex);
    }
    private void playOrPauseSong(){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            Log.d(TAG+ "pauseOrPlayMethod", "Pausing");

        } else {
            mediaPlayer.start();
            Log.d(TAG+ "pauseOrPlayMethod", "Playing");
        }
    }
    private void playNextSong(){
        Log.d(TAG + "playNextSong Method", "is clicked");
        if (isShuffleSongs){
            Log.d(TAG, "starting generate Random song");
            generateRandomSong();
        } else {
            if (MyInitialMediaSongPlayer.starterIndex == songModelArrayList.size() - 1) {
                MyInitialMediaSongPlayer.starterIndex = -1;
            }
            MyInitialMediaSongPlayer.starterIndex += 1;
        }
    }


    private void loopAllSong(){
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            if (loopAllSongs) {
                    Log.d(TAG+"", "Repeat all song with shuffle mode");
                    playNextSong();
                    setInitialDataSource();
            }
        });
    }
    private void generateRandomSong(){

        // random position
        Random random = new Random();
        int randPosition = random.nextInt(songModelArrayList.size());
        Log.d("Random Position ","Random Position: " +randPosition);

        MyInitialMediaSongPlayer.setStarterIndex(randPosition);

    }

    @SuppressLint("DefaultLocale")
    private String formatLongToMMSS(String duration){
        long milliSec = Long.parseLong(duration);
//        min can be greater than 60, because i want it
        long min = TimeUnit.MILLISECONDS.toMinutes(milliSec);
        long sec = TimeUnit.MILLISECONDS.toSeconds(milliSec) % 60;

        return String.format("%02d:%02d", min, sec);
    }
}
