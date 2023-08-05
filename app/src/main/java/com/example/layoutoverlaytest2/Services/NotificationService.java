package com.example.layoutoverlaytest2.Services;


import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_NEXT;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_PLAY;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_PREV;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_REPEAT;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_REPEAT_SECTION;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_SHUFFLE;
import static com.example.layoutoverlaytest2.ApplicationClass.CHANNEL_ID_1;
import static com.example.layoutoverlaytest2.ApplicationClass.MY_COMMAND;
import static com.example.layoutoverlaytest2.ApplicationClass.PLAY_FROM_SONG_LIST;
import static com.example.layoutoverlaytest2.ApplicationClass.REMOVE_SONG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.example.layoutoverlaytest2.MainActivity;
import com.example.layoutoverlaytest2.Models.ButtonMainObject;
import com.example.layoutoverlaytest2.Models.MiniObject;
import com.example.layoutoverlaytest2.Models.SongModel;
import com.example.layoutoverlaytest2.Models.TextViewMainObject;
import com.example.layoutoverlaytest2.MyInitialMediaSongPlayer;
import com.example.layoutoverlaytest2.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class NotificationService extends Service implements MediaPlayer.OnErrorListener {

    static final String TAG = "NotificationService ";
    static final String BUNDLE_NAME = "MY_BUNDLE";
    static final String DATA_KEY = "SONG_BUNDLE";
    private static final int REQUEST_CODE = 592431;
    static final String[] RUNTIME_PERMISSION = {Manifest.permission.READ_EXTERNAL_STORAGE};
    public MediaPlayer mediaPlayer = MyInitialMediaSongPlayer.getInstance();
    ArrayList<SongModel> songModelArrayList = new ArrayList<>();
    ArrayList<SongModel> currentArrayList = new ArrayList<>();
    private IBinder mBinder = new MyBinder();
    public Handler handler = new Handler();
    SongModel currentSong;
    boolean isShuffleSongs = false;
    int pressedTimes = 0;
    boolean loopAllSongs = false, loopOneSong = false;
    boolean isRepeatSection = false;
    long longStartPoint = -1;
    int intEndPoint = -1, intStartPoint = -1;
    boolean isShowNotification = true;

    Runnable runnable;

    public NotificationService() {
    }


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
                    pressedTimes += 1;
                    if (pressedTimes == 3) {
                        pressedTimes = 0;
                    }

                    Log.d(TAG, "pressedTimes Value: " + pressedTimes);
                    break;

                case ACTION_REPEAT_SECTION:

                    Log.d(TAG, "Repeat Section clicked");
                    repeatSectionMethod(intent);
                    break;
            }
        } else {
            Log.e(TAG + "actionCommand", "NULL");
        }


        return START_STICKY;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnBind");
        showNotification();
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
        handler.removeCallbacks(runnable);
        if (currentSong != null && mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        super.onDestroy();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.d(TAG + " MediaPlayer", "onError");
        mediaPlayer.reset();
        return false;
    }


    public class MyBinder extends Binder {
        public NotificationService getService() {
            return NotificationService.this;
        }
    }


    private void queryMusicFiles() {

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

        Log.d(TAG + "query files", "starting...");
        try (Cursor cursor = this.getContentResolver().query(collectionUri, projection, selection, null, sortOrder)) {

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
        Log.d(TAG + "query files", "finished !!");
    }

    public void initialMusicPlayer() {

        Log.d(TAG, "initialMusicPlayer");
//        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
//        mediaPlayer.setOnPreparedListener(this);
//        mediaPlayer.prepareAsync();
        mediaPlayer.setOnErrorListener(this);
//        mediaPlayer.setOnCompletionListener(this);

    }

    public void setInitialDataSource() {
        Log.d(TAG, "setInitialDatasource");
        Log.d(TAG + "Song Position", String.valueOf(MyInitialMediaSongPlayer.starterIndex));
        currentSong = songModelArrayList.get(MyInitialMediaSongPlayer.starterIndex);


        playMusic();
    }

    public void removeSongInPlaylist() {
        Log.d(TAG, "Remove the song " + MyInitialMediaSongPlayer.removeIndex);
        Log.d(TAG, "Total songs before " + songModelArrayList.size());
        if (MyInitialMediaSongPlayer.removeIndex >= 0) {
            songModelArrayList.remove(MyInitialMediaSongPlayer.removeIndex);
        }
        Log.d(TAG, "Total songs after " + songModelArrayList.size());
    }

    public void updateUiFromService(Activity activity, ButtonMainObject buttonMainObject, TextViewMainObject textViewMainObject, SeekBar seekBar, MiniObject miniObject) {

        activity.runOnUiThread( runnable = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG + " Current Thread ", Thread.currentThread().getName());

//                Play Button UI
                if (currentSong!= null) {
                    if (mediaPlayer.isPlaying()) {
                        buttonMainObject.getPlayBtn().setBackgroundResource(R.drawable.baseline_pause_circle_outline_24);
                    } else {
                        buttonMainObject.getPlayBtn().setBackgroundResource(R.drawable.baseline_play_circle_outline_24);
                    }
                }
                /*********************************************************************************************/

//                Shuffle Button UI
                if (isShuffleSongs) {
                    buttonMainObject.getShuffleBtn().setBackgroundResource(R.drawable.baseline_shuffle_on_24);
                } else {
                    buttonMainObject.getShuffleBtn().setBackgroundResource(R.drawable.baseline_shuffle_24);
                }
                /*********************************************************************************************/
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
                        loopSongOptions();
                        Log.d("LoopAllSongs", "loopCurrentSongs ");
                        buttonMainObject.getLoopBtn().setBackgroundResource(R.drawable.baseline_repeat_one_on_24);
                        break;
                    case 2:
                        // loop all song
                        loopAllSongs = true;
                        loopOneSong = false;
                        loopSongOptions();
                        Log.d("LoopAllSongs", "loopAllSongs: " + loopAllSongs);
                        buttonMainObject.getLoopBtn().setBackgroundResource(R.drawable.baseline_repeat_on_24);
                        break;
                }
                /*********************************************************************************************/

//                Title Song Name
//                String songName = null;
//                if (currentSong != null) {
//                    songName = currentSong.getTitle();
//                }
//                if (songName == null) {
//                    songName = "Song Name";
//                }

                textViewMainObject.getSongNameTv().setText(getSongName());
                /*********************************************************************************************/

//                Title Start Time
                if (currentSong != null) {
                    textViewMainObject.getCurrentTimeTv().setText(formatLongToMMSS(String.valueOf(mediaPlayer.getCurrentPosition())));
                }
                /*********************************************************************************************/

//                Title End Time
                if (mediaPlayer != null && currentSong != null) {
                    textViewMainObject.getEndTimeTv().setText(formatLongToMMSS(String.valueOf(currentSong.getDuration())));
                }
                /*********************************************************************************************/

//                SeekBar Code Start
                if (currentSong != null && mediaPlayer != null) {

                    seekBar.setMax(mediaPlayer.getDuration());

                    seekBar.setProgress(mediaPlayer.getCurrentPosition());

                    if (mediaPlayer.isPlaying()) {
                        buttonMainObject.getPlayBtn().setBackgroundResource(R.drawable.baseline_pause_circle_outline_24);
                    } else {
                        buttonMainObject.getPlayBtn().setBackgroundResource(R.drawable.baseline_play_circle_outline_24);
                    }

                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            if (b) {
                                try {
                                    mediaPlayer.seekTo(i);
                                    mediaPlayer.start();
                                } catch (Exception e){
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
                /*********************************************************************************************/

//                mini UI
                miniObject.getMini_songName().setText(getSongName());

                miniObject.getMini_playBtn().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (currentSong != null) {
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.pause();
                                Log.d(TAG + "Mini Player", "Pausing");
                            } else {
                                mediaPlayer.start();
                                Log.d(TAG + "Mini Player", "Playing");
                            }
                        } else {
                            setInitialDataSource();
                        }
                    }
                });

                if (currentSong != null) {
                    if (mediaPlayer.isPlaying()) {
                        miniObject.getMini_playBtn().setBackgroundResource(R.drawable.baseline_pause_24);
                    } else {
                        miniObject.getMini_playBtn().setBackgroundResource(R.drawable.baseline_play_arrow_24);
                    }
                }
                /*********************************************************************************************/

//                repeatSection
                if (isRepeatSection && pressedTimes == 1) {
                    if (currentSong != null &&
                            formatLongToMMSS(String.valueOf(mediaPlayer.getCurrentPosition())).equals(formatLongToMMSS(String.valueOf(intEndPoint)))) {

                        Log.d(TAG + "repeat Section State pressTime", pressedTimes + "");
                        Log.d(TAG + "repeat Section State current Duration", mediaPlayer.getCurrentPosition() + "");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                            Log.d(TAG + "repeat Section State is", isRepeatSection + " inside");
                            mediaPlayer.seekTo(longStartPoint, MediaPlayer.SEEK_CLOSEST_SYNC);
                        } else {
                            mediaPlayer.seekTo(intStartPoint);
                        }
                    }
                } else {
                    isRepeatSection = false;
                }
                Log.d(TAG + "repeat Section State is", isRepeatSection + "");

                try {
                    handler.postDelayed(this, 500);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }


    private void playMusic() {

        Log.d(TAG, "playMusic method");
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            // set Max seekbar
//            seekBar.setMax(mediaPlayer.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playPrevSong() {
        Log.d(TAG + "playPrevSong Method", "is clicked");
        if (isShuffleSongs) {
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

    private void playOrPauseSong() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Log.d(TAG + "pauseOrPlayMethod", "Pausing");

        } else {
            mediaPlayer.start();
            Log.d(TAG + "pauseOrPlayMethod", "Playing");
        }
    }

    private void playNextSong() {
        Log.d(TAG + "playNextSong Method", "is clicked");
        if (isShuffleSongs) {
            Log.d(TAG, "starting generate Random song");
            generateRandomSong();
        } else {
            if (MyInitialMediaSongPlayer.starterIndex == songModelArrayList.size() - 1) {
                MyInitialMediaSongPlayer.starterIndex = -1;
            }
            MyInitialMediaSongPlayer.starterIndex += 1;
        }
    }


    private void loopSongOptions() {
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            if (loopAllSongs) {
                Log.d(TAG + "", "Repeat all song with shuffle mode");
                playNextSong();
                setInitialDataSource();
            }
        });

        if (loopOneSong){
            mediaPlayer.setOnCompletionListener(mediaPlayer1 -> {
                setInitialDataSource();
            });
        }
    }

    private void generateRandomSong() {

        // random position
        Random random = new Random();
        int randPosition = random.nextInt(songModelArrayList.size());
        Log.d("Random Position ", "Random Position: " + randPosition);

        MyInitialMediaSongPlayer.setStarterIndex(randPosition);

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

            for (long i : hashMap.keySet()) {
                Log.d("start value", i + "");
                startValue = i;
                endValue = hashMap.get(i);
                Log.d("end value", hashMap.get(i) + "");
            }
            if (mediaPlayer != null && !hashMap.isEmpty() && startValue >= 0 && endValue >= 0 && hashMap.size() == 1 && startValue != endValue) {
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

    public void showNotification() {
        Log.d(TAG, " showNotification");
        Thread myThread;
        if (isShowNotification && currentSong != null) {
            myThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG + " showNotification", " Current Thread " + Thread.currentThread().getName());

                    Intent mainActivityIntent = new Intent(NotificationService.this, MainActivity.class);
                    // Create the TaskStackBuilder and add the intent, which inflates the back stack
                    TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(NotificationService.this);
                    taskStackBuilder.addNextIntentWithParentStack(mainActivityIntent);
                    // Get the PendingIntent containing the entire back stack
                    PendingIntent mainPendingIntent = taskStackBuilder.getPendingIntent(798, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.thumb1);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(NotificationService.this, CHANNEL_ID_1);
                    builder.setSmallIcon(R.drawable.baseline_music_note_24);
                    builder.setContentTitle(getSongName());
                    builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle());
                    builder.setPriority(NotificationCompat.PRIORITY_MAX);
                    builder.setLargeIcon(bitmap);
                    builder.setContentIntent(mainPendingIntent);

                    Notification notification = builder.build();
                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(NotificationService.this);
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

            myThread.start();
        }
    }
    @SuppressLint("DefaultLocale")
    private String formatLongToMMSS(String duration){
        long milliSec = Long.parseLong(duration);
//        min can be greater than 60, because i want it
        long min = TimeUnit.MILLISECONDS.toMinutes(milliSec);
        long sec = TimeUnit.MILLISECONDS.toSeconds(milliSec) % 60;

        return String.format("%02d:%02d", min, sec);
    }
    private String getSongName(){
        String songName = "Song Name";
        if (currentSong != null){
            songName = currentSong.getTitle();
        }
        return songName;
    }

}
