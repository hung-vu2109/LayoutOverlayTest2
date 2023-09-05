package com.example.layoutoverlaytest2.Services;


import static com.example.layoutoverlaytest2.Activities.MainActivity.mainHandler;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_MINI_PLAY;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_NEXT;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_PLAY;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_PREV;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_REPEAT;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_REPEAT_SECTION;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_SHUFFLE;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_STOP;
import static com.example.layoutoverlaytest2.ApplicationClass.CHANNEL_ID_1;
import static com.example.layoutoverlaytest2.ApplicationClass.MY_COMMAND;
import static com.example.layoutoverlaytest2.ApplicationClass.PLAY_FROM_SONG_LIST;
import static com.example.layoutoverlaytest2.ApplicationClass.PLAY_FROM_VIDEO_LIST;
import static com.example.layoutoverlaytest2.ApplicationClass.REMOVE_SONG;
import static com.example.layoutoverlaytest2.ApplicationClass.continuePlayVideo;
import static com.example.layoutoverlaytest2.ApplicationClass.isAliveMainActivity;
import static com.example.layoutoverlaytest2.ApplicationClass.isQueryDone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.example.layoutoverlaytest2.BroadcastReceiver.NotificationReceiver;
import com.example.layoutoverlaytest2.Activities.MainActivity;
import com.example.layoutoverlaytest2.Dialogs.QueryFileWaitingDialog;
import com.example.layoutoverlaytest2.Fragments.VideoTextureViewFragment;
import com.example.layoutoverlaytest2.Models.Song.ButtonMainObject;
import com.example.layoutoverlaytest2.Models.Song.MiniObject;
import com.example.layoutoverlaytest2.Models.Song.SongModel;
import com.example.layoutoverlaytest2.Models.Song.TextViewMainObject;
import com.example.layoutoverlaytest2.Models.Video.VideoModel;
import com.example.layoutoverlaytest2.Callable.QueryMusicFilesCallable;
import com.example.layoutoverlaytest2.Callable.QueryVideoFilesCallable;
import com.example.layoutoverlaytest2.Utils.MyInitialMediaPlayer;
import com.example.layoutoverlaytest2.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class NotificationService extends Service implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener{

    static final String TAG = "NotificationService ";
    static final String BUNDLE_NAME = "MY_BUNDLE";
    static final String DATA_KEY = "SONG_BUNDLE";
    private static final int REQUEST_CODE = 592431;
    public MediaPlayer mediaPlayer = MyInitialMediaPlayer.getInstance();
    private final IBinder mBinder = new MyBinder();
    public Handler handler = new Handler();
    public static volatile ArrayList<VideoModel> videoModelArrayList = new ArrayList<>();
    public static volatile ArrayList<SongModel> songModelArrayList = new ArrayList<>();
    QueryMusicFilesCallable queryMusicFilesCallable = new QueryMusicFilesCallable(this, songModelArrayList);
    QueryVideoFilesCallable queryVideoFilesCallable = new QueryVideoFilesCallable(this, videoModelArrayList);
    Thread repeatSectionThread;
    Thread myNotificationThread;
    SongModel currentSong;
    VideoModel currentVideo;
    boolean isShuffleSongs = false;
    int pressedTimes = 0;
    boolean loopAllSongs = false, loopOneSong = false;
    boolean isRepeatSection = false;
    long longStartPoint = -1;
    int intEndPoint = -1, intStartPoint = -1;
    boolean isShowNotification = true;
    boolean showNotificationStarted = false;
    boolean commandArrive = false;
    boolean setInitialItemNav = false;

    public enum TYPE_OF_MEDIA {
        MUSIC,
        PLAY_VIDEO_AS_AUDIO,
        VIDEO
    }
    public static volatile TYPE_OF_MEDIA media;
    int currentPosition = -1;

    Runnable updateUiRunnable, notificationRunnable;
    MediaSession mediaSession;
    NotificationManagerCompat notificationManagerCompat;
    VideoTextureViewFragment videoTextureViewFragment;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, " onCreate");

//        check song list from adapter
        if (songModelArrayList.isEmpty() || videoModelArrayList.isEmpty()) {
            Log.d(TAG, "songModelArrayList: NULL");
            queryMusicAndVideo();
        } else {
//            for (SongModel i : songModelArrayList) {
//                Log.d(TAG, String.valueOf(i));
//            }
        }
        initialMusicPlayer();
    }


    public synchronized void queryMusicAndVideo(){
        FutureTask<ArrayList<VideoModel>> arrayListFutureTask1 = new FutureTask<ArrayList<VideoModel>>(queryVideoFilesCallable){
            @Override
            protected void set(ArrayList<VideoModel> videoModelArrayList1) {
                super.set(videoModelArrayList);
                videoModelArrayList = videoModelArrayList1;
            }

            @Override
            protected void setException(Throwable t) {
                super.setException(t);
            }
        };
        FutureTask<ArrayList<SongModel>> arrayListFutureTask2 = new FutureTask<ArrayList<SongModel>>(queryMusicFilesCallable){
            @Override
            protected void set(ArrayList<SongModel> songModelArrayList1) {
                super.set(songModelArrayList);
                songModelArrayList = songModelArrayList1;
            }

            @Override
            protected void setException(Throwable t) {
                super.setException(t);
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.execute(arrayListFutureTask1);
        executorService.execute(arrayListFutureTask2);

        while (!isQueryDone){
//            try {
                if (arrayListFutureTask1.isDone() && arrayListFutureTask2.isDone()) {
                    Log.d(TAG + " Query Task", " All is done");
                    executorService.shutdown();

                    if (!videoModelArrayList.isEmpty()) {
                        for (VideoModel i : videoModelArrayList) {
                            Log.d(TAG + " Query Task Result: ", "Object " + i);
                        }
                    }
                    if (!songModelArrayList.isEmpty()) {
                        for (SongModel i : songModelArrayList) {
                            Log.d(TAG + " Query Task Result: ", "Object " + i);
                        }
                    }

                    isQueryDone = true;
                } else {
                    Log.d(TAG + " Query Task", " is Running");
//                    arrayListFutureTask1.get();
//                    arrayListFutureTask2.get();
                }
//            } catch (ExecutionException | InterruptedException e) {
//                throw new RuntimeException(e);
//            }
        }
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

            commandArrive = true;

            switch (actionCommand) {
                case PLAY_FROM_SONG_LIST:
                    Log.d(TAG, "Play from song list");
                    setInitialDataSource();
                    break;
                case PLAY_FROM_VIDEO_LIST:
                    Log.d(TAG, "Play from video list");
                    setInitialDataSource();
                    break;

                case REMOVE_SONG:
                    Log.d(TAG, "Remove song from list");
                    removeSongInPlaylist();
                    break;

                case ACTION_PLAY:
                    Log.d(TAG, "Play Action");
                    playOrPauseSong();
                    updateNotificationView();
                    break;

                case ACTION_NEXT:
                    Log.d(TAG, "Next Action");
                    playNextSong();
                    setInitialDataSource();
                    break;

                case ACTION_PREV:
                    Log.d(TAG, "Prev Action");
                    playPrevSong();
                    setInitialDataSource();
                    break;

                case ACTION_SHUFFLE:
                    Log.d(TAG, "Shuffle Action");
                    isShuffleSongs = !isShuffleSongs;
                    break;

                case ACTION_REPEAT:
                    Log.d(TAG, "Loop Action");
                    pressedTimes += 1;
                    if (pressedTimes == 3) {
                        pressedTimes = 0;
                    }
                    loopSongState();
                    updateNotificationView();
                    Log.d(TAG, "pressedTimes Value: " + pressedTimes);
                    break;

                case ACTION_REPEAT_SECTION:
                    Log.d(TAG, "Repeat Section Action");
                    repeatSectionMethod(intent);
                    repeatSectionRunning();
                    break;

                case ACTION_MINI_PLAY:
                    Log.d(TAG, "Mini Play Action");
                    playOrPauseSong();
                    break;

                case ACTION_STOP:
                    Log.d(TAG, "Stop Action");
                    stopUpdateUi();
                    stopShowNotification();
                    onDestroy();
                    stopMyProcess();
                    break;

            }
        } else {
            Log.e(TAG + "actionCommand", "NULL");
        }


        return super.onStartCommand(intent, flags, startId);
    }


//    if IBinder is not changed then onUnbind+onBind will be called once, cause is IBinder is cached
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnBind");
        updateNotificationView();
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
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        stopUpdateUi();
        if (currentSong != null || currentVideo != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        stopShowNotification();
        MyInitialMediaPlayer.isStarted = false;
        MyInitialMediaPlayer.isPlaying = false;
        super.onDestroy();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.d(TAG + " MediaPlayer", "onError" +"..."+i+"..."+i1);
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG + " MediaPlayer", "onCompletion");
        MyInitialMediaPlayer.isPlaying = false;
        updateNotificationView();
    }

    public void setCurrentPositionMediaToContinue(){
        if (currentVideo != null) {
            currentPosition = mediaPlayer.getCurrentPosition();
            Log.d(TAG, " setCurrentPositionMediaToContinue: " + currentPosition + "");
        }
    }
    public VideoTextureViewFragment getVideoTextureViewFragment(){
        if (currentVideo != null && getTypeOfMedia() == TYPE_OF_MEDIA.VIDEO && isAliveMainActivity) {
            Log.d(TAG, " Start New Video ");
            // start new video
            videoTextureViewFragment = new VideoTextureViewFragment(this, mediaPlayer, currentVideo.getPathVideo(), currentPosition);
            currentPosition = -1;
        }
            return videoTextureViewFragment;
    }

    public void ContinuePlayAsVideoWhenStopActivity(){
        if (currentVideo != null && getTypeOfMedia() == TYPE_OF_MEDIA.VIDEO){
            if (continuePlayVideo){
//                Log.d(TAG, " Continue Play Video ");
//                // continue play Video
//                mediaPlayer.stop();
//                if (currentPosition > -1) {
//                    videoTextureViewFragment = new VideoTextureViewFragment(this, mediaPlayer, currentVideo.getPathVideo(), currentPosition);
//                }
                boolean isPlaying = mediaPlayer.isPlaying();
                sendEmptyMsgToMainActivity(1);
                if (!isPlaying){
                    mediaPlayer.pause();
                }
                continuePlayVideo = false;
            }
        }
    }
    public void ContinuePlayAsAudioWhenActivityDestroy(){

    }

    public class MyBinder extends Binder {
        public NotificationService getService() {
            return NotificationService.this;
        }
    }

    public void initialMusicPlayer() {

        Log.d(TAG, "initialMusicPlayer");
        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
//        mediaPlayer.setOnPreparedListener(this);
//        mediaPlayer.prepare();
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);

    }

    public TYPE_OF_MEDIA getTypeOfMedia(){
        TYPE_OF_MEDIA type_of_media;

        if (MyInitialMediaPlayer.isMusic){
            MyInitialMediaPlayer.playAsAudio = false;
            type_of_media = TYPE_OF_MEDIA.MUSIC;
        } else {
            if (MyInitialMediaPlayer.playAsAudio){
                type_of_media = TYPE_OF_MEDIA.PLAY_VIDEO_AS_AUDIO;
            } else {
                type_of_media = TYPE_OF_MEDIA.VIDEO;
            }
        }
        return type_of_media;
    }
    public void setInitialDataSource() {
        Log.d(TAG, "setInitialDatasource");
        Log.d(TAG + "Media File Position", String.valueOf(MyInitialMediaPlayer.starterIndex));
        if (MyInitialMediaPlayer.isMusic){
            currentSong = songModelArrayList.get(MyInitialMediaPlayer.starterIndex);
        } else {
            currentVideo = videoModelArrayList.get(MyInitialMediaPlayer.starterIndex);
        }

        if (getTypeOfMedia() == TYPE_OF_MEDIA.MUSIC || getTypeOfMedia() == TYPE_OF_MEDIA.PLAY_VIDEO_AS_AUDIO) {
            playMusic();
        }
        if (getTypeOfMedia() == TYPE_OF_MEDIA.VIDEO){
            playVideo();
        }
    }

    public void removeSongInPlaylist() {
        Log.d(TAG, "Remove the song " + MyInitialMediaPlayer.removeIndex);
        Log.d(TAG, "Total songs before " + songModelArrayList.size());
        if (MyInitialMediaPlayer.removeIndex >= 0) {
            songModelArrayList.remove(MyInitialMediaPlayer.removeIndex);
        }
        Log.d(TAG, "Total songs after " + songModelArrayList.size());
    }

    public void updateUiFromService(@NonNull Activity activity,
                                    ButtonMainObject buttonMainObject,
                                    TextViewMainObject textViewMainObject,
                                    SeekBar seekBar,
                                    MiniObject miniObject,
                                    QueryFileWaitingDialog queryFileWaitingDialog,
                                    BottomNavigationView bottomNavigationView) {

        activity.runOnUiThread( updateUiRunnable = new Runnable() {
            @Override
            public void run() {

                if (MyInitialMediaPlayer.isStarted){
                    Log.d(TAG+" updateUiFromService", "Start Update");
                    if (currentSong != null || currentVideo != null) {
                        if (commandArrive) {
                            Log.d(TAG + " updateUiFromService", "Start Update" + " CommandArrive");

                            updateShuffleBtnUi(buttonMainObject.getShuffleBtn());
                            updateTextViewMainObject(textViewMainObject);
                            updateLoopBtnUi(buttonMainObject.getLoopBtn());
                            updateMiniSongNameUi(miniObject.getMini_songName());
                            updateSeekbarUi(seekBar, buttonMainObject.getPlayBtn());

                            commandArrive = false;
                        }

/*************************************************************************  Infinity Loop  ************************************************************************/

                        updatePlayBtnUi(buttonMainObject.getPlayBtn());
                        updateMiniPlayBtnUi(miniObject.getMini_playBtn());
                        updateTextViewCurrentTime(textViewMainObject.getCurrentTimeTv());
                        updateSeekbarUiCurrentPosition(seekBar);
                    }

                } else {

                    if (!setInitialItemNav) {
                        if (!isQueryDone) {
                            queryFileWaitingDialog.show();
                        } else {
                            bottomNavigationView.setSelectedItemId(R.id.nav_fragment_container_music);
                            queryFileWaitingDialog.dismiss();
                            setInitialItemNav = true;
                        }
                    }
                }
                Log.d(TAG + " updateUiFromService", " is Running...");
                Log.d(TAG + " TYPE_OF_MEDIA ", getTypeOfMedia()+"");



                try {
                    handler.postDelayed(updateUiRunnable, 200);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
    public void stopUpdateUi(){
        Log.d(TAG, " stopUpdateUI");
        handler.removeCallbacks(updateUiRunnable);
    }
    private void stopMyProcess(){
        Log.d(TAG, " stopMyProcess");
//        Process.killProcess(Process.myPid());
        System.exit(0);
    }

    private void playMusic() {

        Log.d(TAG, "playMusic method");
        try {
            mediaPlayer.reset();
            if (MyInitialMediaPlayer.isMusic) {
                mediaPlayer.setDataSource(currentSong.getPath());
            } else {
                mediaPlayer.setDataSource(currentVideo.getPathVideo());
            }
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);

            MyInitialMediaPlayer.isStarted = true;
            MyInitialMediaPlayer.isPlaying = true;

            sendEmptyMsgToMainActivity(0);

            updateNotificationView();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void playVideo(){
        Log.d(TAG, "playVideo method");

        sendEmptyMsgToMainActivity(1);

        MyInitialMediaPlayer.isStarted = true;
        MyInitialMediaPlayer.isPlaying = true;
        updateNotificationView();
    }

    private void sendEmptyMsgToMainActivity(int msgWhat){

        if (isAliveMainActivity) {
            Log.d(TAG + " sendEmptyMsgToMainActivity", isAliveMainActivity+"");
            if (msgWhat == 0) {
                // set default image
                mainHandler.sendEmptyMessage(0);
            }
            if (msgWhat == 1) {
                // set video textureView
                mainHandler.sendEmptyMessage(1);
            }
        }

    }
    private void playPrevSong() {
        Log.d(TAG + "playPrevSong Method", "is clicked");
        if (isShuffleSongs) {
            Log.d(TAG, "starting generate Random song");
            generateRandomSong();
        } else {
            if (MyInitialMediaPlayer.starterIndex == 0) {
                MyInitialMediaPlayer.starterIndex = getMediaSizeArrayList();
            }
            MyInitialMediaPlayer.starterIndex -= 1;
        }
    }

    private void playOrPauseSong() {
        if ((currentSong != null || currentVideo != null) && MyInitialMediaPlayer.isStarted) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                MyInitialMediaPlayer.isPlaying = false;
                Log.d(TAG + "pauseOrPlayMethod", "Pausing");
                updateNotificationView();

            } else {
                mediaPlayer.start();
                MyInitialMediaPlayer.isPlaying = true;
                Log.d(TAG + "pauseOrPlayMethod", "Playing");
                updateNotificationView();
            }
        } else {
            setInitialDataSource();
        }
    }

    private void playNextSong() {
        Log.d(TAG + "playNextSong Method", "is clicked");
        if (isShuffleSongs) {
            Log.d(TAG, "starting generate Random song");
            generateRandomSong();
        } else {
            if (MyInitialMediaPlayer.starterIndex == getMediaSizeArrayList() - 1) {
                MyInitialMediaPlayer.starterIndex = -1;
            }
            MyInitialMediaPlayer.starterIndex += 1;
        }
    }


    public void loopSongOptions() {
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            if (loopAllSongs) {
                Log.d(TAG + "", "Repeat all song with shuffle mode");
                playNextSong();
                setInitialDataSource();
                setCommandArrive();
            }
            if (loopOneSong){
                setInitialDataSource();
            }
        });

    }

    private void generateRandomSong() {

        // random position
        Random random = new Random();
        int randPosition = random.nextInt(getMediaSizeArrayList());
        Log.d("Random Position ", "Random Position: " + randPosition);

        MyInitialMediaPlayer.setStarterIndex(randPosition);

    }

    private void repeatSectionMethod(Intent intent) {

        HashMap<Long, Long> hashMap;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                hashMap = intent.getSerializableExtra("PAIR_VALUE", null);
            } else {
                hashMap = (HashMap<Long, Long>) intent.getSerializableExtra("PAIR_VALUE");
            }
            Log.d(TAG + "repeat Section", String.valueOf(hashMap));
            Log.d(TAG + "repeat Section KEY", String.valueOf(hashMap.keySet()));
            Log.d(TAG + "repeat Section VALUE", String.valueOf(hashMap.values()));

            long startValue = -1, endValue = -1;

            if ((currentSong != null || currentVideo != null) && hashMap.size() == 1) {
                for (long i : hashMap.keySet()) {
                    Log.d("start value", i + "");
                    startValue = i;
                    endValue = hashMap.get(i);
                    Log.d("end value", hashMap.get(i) + "");
                }
            } else {
                Log.d("repeat Section", "currentSong = null || currentVideo = null || hashMap.size() != 1");
                isRepeatSection = false;
                return;
            }
            if (startValue >= 0 && endValue >= 0 && startValue != endValue) {
                long longStart = Long.parseLong(String.valueOf(startValue));
                int intStart = Integer.parseInt(String.valueOf(startValue));
                Log.d(TAG + "repeat Section", "longStart: " + longStart + " && " + " intStart: " + intStart);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mediaPlayer.seekTo(longStart, MediaPlayer.SEEK_CLOSEST_SYNC);
                    Log.d(TAG + "repeat Section", "API 26+");

                } else {
                    Log.d(TAG + "repeat Section", "API 24,25");
                    mediaPlayer.seekTo(intStart);
                }
//                State is RepeatSection
                isRepeatSection = true;
                pressedTimes = 1;

                int intEnd = Integer.parseInt(String.valueOf(endValue));
                Log.d(TAG + "repeat Section", "intEnd: " + intEnd);

                longStartPoint = longStart;
                intStartPoint = intStart;
                intEndPoint = intEnd;

            } else {
                isRepeatSection = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    private void repeatSectionRunning(){
        repeatSectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRepeatSection){

                    Log.d(TAG+ "repeatSection", " is running");
                    if (isRepeatSection && pressedTimes == 1) {
                        if ((currentSong != null || currentVideo != null) &&
                                formatLongToMMSS(String.valueOf(mediaPlayer.getCurrentPosition())).equals(formatLongToMMSS(String.valueOf(intEndPoint)))) {

                            Log.d(TAG + "repeat Section State is", isRepeatSection + "");
                            Log.d(TAG + "repeat Section State pressTime", pressedTimes + "");
                            Log.d(TAG + "repeat Section State current Duration", mediaPlayer.getCurrentPosition() + "");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                                Log.d(TAG + "repeat Section State Android O(API 26)+ is", isRepeatSection + " inside");
                                mediaPlayer.seekTo(longStartPoint, MediaPlayer.SEEK_CLOSEST_SYNC);
                            } else {
                                mediaPlayer.seekTo(intStartPoint);
                            }
                        }
                    } else {
                        isRepeatSection = false;
                    }

                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        repeatSectionThread.setName("repeatSectionThread");
        repeatSectionThread.start();
        updateNotificationView();
    }

    public void showNotification(int intPlayIcon, int intLoopIcon) {
        if (isShowNotification && (currentSong != null || currentVideo != null)) {
            Log.d(TAG, " showNotification");
            myNotificationThread = new Thread(notificationRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG + " showNotification", " Current Thread " + Thread.currentThread().getName());
                    getMediaName();
                    Intent mainActivityIntent = new Intent(NotificationService.this, MainActivity.class);
                    // Create the TaskStackBuilder and add the intent, which inflates the back stack
                    TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(NotificationService.this);
                    taskStackBuilder.addNextIntentWithParentStack(mainActivityIntent);
                    // Get the PendingIntent containing the entire back stack
                    PendingIntent mainPendingIntent = taskStackBuilder.getPendingIntent(798, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.thumb1);

                    Intent prevIntent = new Intent(NotificationService.this, NotificationReceiver.class).setAction(ACTION_PREV);
                    PendingIntent prevPending = PendingIntent.getBroadcast(NotificationService.this, REQUEST_CODE, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    Intent playIntent = new Intent(NotificationService.this, NotificationReceiver.class).setAction(ACTION_PLAY);
                    PendingIntent playPending = PendingIntent.getBroadcast(NotificationService.this, REQUEST_CODE, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    Intent nextIntent = new Intent(NotificationService.this, NotificationReceiver.class).setAction(ACTION_NEXT);
                    PendingIntent nextPending = PendingIntent.getBroadcast(NotificationService.this, REQUEST_CODE, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    Intent loopIntent = new Intent(NotificationService.this, NotificationReceiver.class).setAction(ACTION_REPEAT);
                    PendingIntent loopPending = PendingIntent.getBroadcast(NotificationService.this, REQUEST_CODE, loopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    Intent stopIntent = new Intent(NotificationService.this, NotificationReceiver.class).setAction(ACTION_STOP);
                    PendingIntent stopPending = PendingIntent.getBroadcast(NotificationService.this, REQUEST_CODE, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(NotificationService.this, CHANNEL_ID_1);
                    builder.setSmallIcon(R.drawable.baseline_music_note_24);
                    builder.setContentTitle(getMediaName());
                    builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle());
                    builder.setPriority(NotificationCompat.PRIORITY_MAX);
                    builder.setLargeIcon(bitmap);
                    builder.setContentIntent(mainPendingIntent);
                    builder.setOngoing(true);
                    builder.setOnlyAlertOnce(true);
                    builder.setAutoCancel(true);
                    builder.addAction(R.drawable.baseline_skip_previous_24, "Previous Song", prevPending);
                    builder.addAction(intPlayIcon, "Play Song", playPending);
                    builder.addAction(R.drawable.baseline_skip_next_24, "Next Song", nextPending);
                    builder.addAction(intLoopIcon, "Repeat Song", loopPending);
                    builder.addAction(R.drawable.baseline_clear_24, "Stop Service", stopPending);


                    Notification notification = builder.build();
                    notificationManagerCompat = NotificationManagerCompat.from(NotificationService.this);
                    if (ActivityCompat.checkSelfPermission(NotificationService.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    notificationManagerCompat.notify(798, notification);
                }
            });
            myNotificationThread.setName("myNotificationThread");
            myNotificationThread.start();
            showNotificationStarted = true;
        }
    }
    private void stopShowNotification(){
        if (showNotificationStarted) {
            notificationManagerCompat.deleteNotificationChannel(CHANNEL_ID_1);
            showNotificationStarted = false;
        }
    }

    private void updateNotificationView(){

        switch (pressedTimes) {
            case 0:
                // no loop
                if (MyInitialMediaPlayer.isPlaying){
                    showNotification(R.drawable.baseline_pause_24, R.drawable.baseline_repeat_24);
                } else {
                    showNotification(R.drawable.baseline_play_arrow_24, R.drawable.baseline_repeat_24);
                }
                break;
            case 1:
                // loop 1 songs
                if (MyInitialMediaPlayer.isPlaying){
                    showNotification(R.drawable.baseline_pause_24, R.drawable.baseline_repeat_one_on_24);
                } else {
                    showNotification(R.drawable.baseline_play_arrow_24, R.drawable.baseline_repeat_one_on_24);
                }
                break;
            case 2:
                // loop all song
                if (MyInitialMediaPlayer.isPlaying){
                    showNotification(R.drawable.baseline_pause_24, R.drawable.baseline_repeat_on_24);
                } else {
                    showNotification(R.drawable.baseline_play_arrow_24, R.drawable.baseline_repeat_on_24);
                }
                break;
        }

    }
    /*************************************************************************  Update Ui Tag  ************************************************************************/
    private void updatePlayBtnUi(ImageButton playBtn){
//          PlayBtn UI
            if (MyInitialMediaPlayer.isPlaying) {
                playBtn.setBackgroundResource(R.drawable.baseline_pause_circle_outline_24);
            } else {
                playBtn.setBackgroundResource(R.drawable.baseline_play_circle_outline_24);
            }
    }
    private void updateShuffleBtnUi(ImageButton shuffleBtn){
        if (isShuffleSongs) {
            shuffleBtn.setBackgroundResource(R.drawable.baseline_shuffle_on_24);
        } else {
            shuffleBtn.setBackgroundResource(R.drawable.baseline_shuffle_24);
        }
    }
    private void updateTextViewMainObject(TextViewMainObject textViewMainObject){
//        Song Name Tv UI
        textViewMainObject.getSongNameTv().setText(getMediaName());
//        End Time Tv UI
        textViewMainObject.getEndTimeTv().setText(formatLongToMMSS(getCurrentMediaDuration()));
    }
    private void updateTextViewCurrentTime(TextView currentTimeTv){

        currentTimeTv.setText(formatLongToMMSS(String.valueOf(mediaPlayer.getCurrentPosition())));
    }
    private void updateLoopBtnUi(ImageButton loopBtn){

//        LoopBtn UI
        switch (pressedTimes) {
            case 0:
                // no loop
                loopBtn.setBackgroundResource(R.drawable.baseline_repeat_24);
                break;
            case 1:
                // loop 1 songs
                loopBtn.setBackgroundResource(R.drawable.baseline_repeat_one_on_24);
                break;
            case 2:
                // loop all song
                loopBtn.setBackgroundResource(R.drawable.baseline_repeat_on_24);
                break;
        }
    }
    /** adding container to constrain Mini SongName TextView **/
    private void updateMiniSongNameUi(TextView miniSongName){
        miniSongName.setText(getMediaName());
    }
    private void updateMiniPlayBtnUi(ImageView miniPlayBtn){

        if (MyInitialMediaPlayer.isPlaying) {
            miniPlayBtn.setBackgroundResource(R.drawable.baseline_pause_24);
        } else {
            miniPlayBtn.setBackgroundResource(R.drawable.baseline_play_arrow_24);
        }
    }
    private void updateSeekbarUi(SeekBar seekBar, ImageButton playBtn){

//                               SeekBar UI
        if (currentSong != null || currentVideo != null) {
            seekBar.setMax(Integer.parseInt(getCurrentMediaDuration()));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (b) {
                        try {

                            playBtn.setBackgroundResource(R.drawable.baseline_play_circle_outline_24);
                            mediaPlayer.seekTo(i);
                            mediaPlayer.start();
                            commandArrive = true;
                            MyInitialMediaPlayer.isPlaying = true;
                            updateNotificationView();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
    }
    private void updateSeekbarUiCurrentPosition(SeekBar seekBar){

        seekBar.setProgress(mediaPlayer.getCurrentPosition());
    }

    /*************************************************************************  Update Ui Tag  ************************************************************************/
    @SuppressLint("DefaultLocale")
    private String formatLongToMMSS(String duration){
        long milliSec = Long.parseLong(duration);
//        min can be greater than 60, because i want it
        long min = TimeUnit.MILLISECONDS.toMinutes(milliSec);
        long sec = TimeUnit.MILLISECONDS.toSeconds(milliSec) % 60;

        return String.format("%02d:%02d", min, sec);
    }

    public void setCommandArrive(){
        commandArrive = true;
    }
    private void loopSongState(){

//        LoopBtn UI
        switch (pressedTimes) {
            case 0:
                // no loop
                loopAllSongs = false;
                loopOneSong = false;
                loopSongOptions();
                Log.d("LoopAllSongs", "No Loop");
                break;
            case 1:
                // loop 1 songs
                loopAllSongs = false;
                loopOneSong = true;
                loopSongOptions();
                Log.d("LoopAllSongs", "loopCurrentSong ");
                break;
            case 2:
                // loop all song
                loopAllSongs = true;
                loopOneSong = false;
                loopSongOptions();
                Log.d("LoopAllSongs", "loopAllSongs: ");
                break;
        }
    }

    private String getMediaName(){
        String mediaName = "Media Name";
        if (MyInitialMediaPlayer.isMusic && currentSong != null){
            mediaName = currentSong.getTitle();
        }
        if (!MyInitialMediaPlayer.isMusic && currentVideo != null){
            mediaName = currentVideo.getTitleVideo();
        }
        return mediaName;
    }
    private int getMediaSizeArrayList(){
        int size;
        if (MyInitialMediaPlayer.isMusic){
            size = songModelArrayList.size();
        } else {
            size = videoModelArrayList.size();
        }
        return size;
    }
    private String getCurrentMediaDuration(){
        String duration = "Media Duration";
        if (currentSong != null && MyInitialMediaPlayer.isMusic){
            duration = currentSong.getDuration();
        }
        if (currentVideo != null && !MyInitialMediaPlayer.isMusic){
            duration = currentVideo.getDurationVideo();
        }
        return duration;
    }

}
