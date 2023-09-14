package com.example.layoutoverlaytest2.Activities;

import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_MINI_PLAY;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_NEXT;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_PLAY;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_PREV;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_REPEAT;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_SHUFFLE;
import static com.example.layoutoverlaytest2.ApplicationClass.MY_COMMAND;
import static com.example.layoutoverlaytest2.ApplicationClass.isAliveMainActivity;
import static com.example.layoutoverlaytest2.Services.NotificationService.media;
import static com.example.layoutoverlaytest2.Services.NotificationService.updateFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.layoutoverlaytest2.Dialogs.QueryFileWaitingDialog;
import com.example.layoutoverlaytest2.Dialogs.RepeatSectionDialog;
import com.example.layoutoverlaytest2.Fragments.MusicFragment;
import com.example.layoutoverlaytest2.Fragments.VideoFragment;
import com.example.layoutoverlaytest2.Fragments.VideoTextureViewFragment;
import com.example.layoutoverlaytest2.Models.Song.ButtonMainObject;
import com.example.layoutoverlaytest2.Models.Song.MiniObject;
import com.example.layoutoverlaytest2.Models.Song.TextViewMainObject;
import com.example.layoutoverlaytest2.R;
import com.example.layoutoverlaytest2.Services.NotificationService;
import com.example.layoutoverlaytest2.Utils.MyInitialMediaPlayer;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener, ServiceConnection {

    private static final String TAG = "MainActivity.java ";
    private static final int REQUEST_CODE = 592431;
    static final String[] RUNTIME_PERMISSION = { Manifest.permission.READ_EXTERNAL_STORAGE };
    TextView title_songName,currentTime,endTime;
    TextView mini_songName;
    ImageView mini_playBtn, mini_closeBtn;
    ImageButton pauseBtn, nextBtn, prevBtn, loopBtn, shuffleBtn;
    SeekBar seekBar;
    ImageView thumbnail_imageView;
    NotificationService notificationService;
    FragmentManager fragmentManager = getSupportFragmentManager();
    MusicFragment musicFragment = new MusicFragment();
    VideoFragment videoFragment = new VideoFragment();
    VideoTextureViewFragment videoTextureViewFragment;
    BottomNavigationView bottomNavigationView;
    QueryFileWaitingDialog queryFileWaitingDialog;
//    OrientationListener orientationListener;
    public static volatile Handler mainHandler;
    public static boolean requestPermissionResult = false;
    RelativeLayout relativeLayout;
    MotionLayout motionLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnItemSelectedListener(this);
//        bottomNavigationView.setSelectedItemId(R.id.nav_fragment_container_music);


        title_songName = findViewById(R.id.tv_player_songName);
        currentTime = findViewById(R.id.tv_player_currentTime);
        endTime = findViewById(R.id.tv_player_endTime);

        thumbnail_imageView = findViewById(R.id.thumbnail_mainImage);

//        Button
        pauseBtn = findViewById(R.id.btn_player_pauseSong);
        nextBtn = findViewById(R.id.btn_player_nextSong);
        prevBtn = findViewById(R.id.btn_player_prevSong);

        loopBtn = findViewById(R.id.btn_player_loop);
        shuffleBtn = findViewById(R.id.btn_player_shuffle);

        seekBar = findViewById(R.id.seekBar_player);


//        Mini UI
        mini_songName = findViewById(R.id.minimalTextView_songName);
        mini_playBtn = findViewById(R.id.image_play);
        mini_closeBtn = findViewById(R.id.image_clear);

        mini_songName.setSelected(true);

        queryFileWaitingDialog = new QueryFileWaitingDialog(this);

        if(!checkReadStoragePer()){
            requestReadStoragePer();
        }

        relativeLayout = findViewById(R.id.top_image);
        motionLayout = findViewById(R.id.motionLayout);
//        orientationListener = new OrientationListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionMenu");
        getMenuInflater().inflate(R.menu.top_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.repeatSection_Option){
            Log.d(TAG, "repeatSection Option");
            showRepeatSectionDialog();
        }



        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
//        if (songModelArrayList == null) {
//            Log.d(TAG+"Data from fragment when itemView is Clicked", "NULL");
//        } else {
//            Log.d(TAG+"Song list Size", songModelArrayList.size()+"");
//        }
        Intent intent = new Intent(this, NotificationService.class);
        bindService(intent, this, BIND_AUTO_CREATE);

        isAliveMainActivity = true;
        MyInitialMediaPlayer.playAsAudio = false;

//        orientationListener.enable();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, " onRestart");
        super.onRestart();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, " onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Log.d(TAG, " onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onStateNotSaved() {
        Log.d(TAG, " onStateNotSaved");
        super.onStateNotSaved();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Log.d(TAG, " onConfigurationChanged");
//        Log.d(TAG, "onConfigurationChanged newConfig: " + newConfig.screenWidthDp + "x" + newConfig.screenHeightDp + " root: "+root.getWidth() + "x" + root.getHeight() + " view: " + myView.getWidth() + "x" + myView.getHeight());
        super.onConfigurationChanged(newConfig);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
//            int orientationState = orientationListener.getCurrentOrientation();
//            if (orientationState == 2 || orientationState == 4) {
//                actionBar.hide();
//            } else if (orientationState == 1 || orientationState == 3) {
//                actionBar.show();
//            }

            int newOrientation = newConfig.orientation;
            if (newOrientation == Configuration.ORIENTATION_PORTRAIT) {
                actionBar.show();
                showButtonPortraitMode();
            } else if (newOrientation == Configuration.ORIENTATION_LANDSCAPE){
                actionBar.hide();
                hideButtonLandscapeMode();
            }
        }
    }

    private boolean checkReadStoragePer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.d(TAG, "Check Reading Storage Permission for API 30");
            return Environment.isExternalStorageManager();
        } else {
            Log.d(TAG, "Check Reading Storage Permission");
            return ContextCompat.checkSelfPermission(MainActivity.this, RUNTIME_PERMISSION[0]) == PackageManager.PERMISSION_GRANTED;
        }
    }


    private void requestReadStoragePer() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.d(TAG, "ActivityCompat.requestPermissions");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, REQUEST_CODE);
//           or
//            Intent intent = new Intent();
//            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
//            intent.setData(uri);
//            startActivity(intent);
        } else {
            Log.d(TAG, "Permission Launcher");
            ActivityResultLauncher<String> requestPermissionLauncher =
                    registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                        @Override
                        public void onActivityResult(Boolean result) {
                            if (result) {
                                requestPermissionResult = true;
                                clearMusicFragment();
                                Toast.makeText(MainActivity.this, "Read permission Ok", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Error!! Can't access to storage", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            requestPermissionLauncher.launch(RUNTIME_PERMISSION[0]);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG + " onRequestPermissionResult ", "Granted");


            } else {
                Log.d(TAG + " onRequestPermissionResult", "Denied");
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_fragment_container_music:
                // music fragment
                if (fragmentManager.getBackStackEntryCount()>1){
                    fragmentManager.popBackStack();
                }
                setMusicFragment();
                break;
            case R.id.nav_fragment_container_video:
                // video fragment
                if (fragmentManager.getBackStackEntryCount()>1){
                    fragmentManager.popBackStack();
                }
                setVideoFragment();
                break;
        }
        return true;
    }

    private void setMusicFragment(){
        Log.d(TAG, " setMusicFragment");
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.replace(R.id.fragmentToReplace_container, musicFragment).addToBackStack("Steve").commit();
//        Toast.makeText(this, "Music Tap", Toast.LENGTH_SHORT).show();
    }
    private void setVideoFragment(){
        Log.d(TAG, " setVideoFragment");
        FragmentTransaction fragmentTransaction1 = fragmentManager.beginTransaction();
        fragmentTransaction1.setReorderingAllowed(true);
        fragmentTransaction1.replace(R.id.fragmentToReplace_container, videoFragment).addToBackStack("Steve").commit();
//        Toast.makeText(this, "Video Tap", Toast.LENGTH_SHORT).show();
    }
    private void clearMusicFragment(){
        Log.d(TAG, " clearMusicFragment");
        FragmentTransaction fragmentTransaction2 = fragmentManager.beginTransaction();
        fragmentTransaction2.remove(musicFragment).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (fragmentManager.getBackStackEntryCount() > 1){
            fragmentManager.popBackStack();
        } else {
            finish();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();

    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        NotificationService.MyBinder myBinder = (NotificationService.MyBinder) iBinder;
        notificationService = myBinder.getService();
        Log.e(TAG+"Service Connection", notificationService +"");


//        update ui after activity is destroyed
        notificationService.setCommandArrive();

        pauseBtn.setOnClickListener(view -> {
            Log.d(TAG, "play btn clicked");
            playBtnClicked();       // send intent
        });

        nextBtn.setOnClickListener(view -> {
            Log.d(TAG, "next btn clicked");
            nextBtnClicked();
        });

        prevBtn.setOnClickListener(view -> {
            Log.d(TAG, "prev btn clicked");
            prevBtnClicked();
        });

        loopBtn.setOnClickListener(view -> {
            Log.d(TAG, "loop btn clicked");
            loopBtnClicked();
        });

        shuffleBtn.setOnClickListener(view -> {
            Log.d(TAG, "loop btn clicked");
            shuffleBtnClicked();
        });

        notificationService.updateUiFromService(this,
                new ButtonMainObject(loopBtn, prevBtn, pauseBtn, nextBtn, shuffleBtn),
                new TextViewMainObject(title_songName, currentTime, endTime),
                seekBar,
                new MiniObject(mini_songName, mini_playBtn),
                queryFileWaitingDialog,
                bottomNavigationView);

        mini_songName.setSelected(true);

        mini_playBtn.setOnClickListener(view -> mini_playBtnClicked());

        mini_closeBtn.setOnClickListener(view -> finish());



        notificationService.getCurrentPositionMediaToContinue();
        mainHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (media == NotificationService.TYPE_OF_MEDIA.VIDEO) {
                    videoTextureViewFragment = notificationService.getVideoTextureViewFragment();

                    if (videoTextureViewFragment != null && isAliveMainActivity) {
                        if (msg.what == 1) {
                            // set video textureView
                            Log.d(TAG + " handler ", " msg.what = 1");
                            if (!getSupportFragmentManager().isDestroyed()) {
                                getSupportFragmentManager().beginTransaction().replace(R.id.top_image, videoTextureViewFragment).commit();
                                thumbnail_imageView.setVisibility(View.GONE);
                            }
                        }
                        if (msg.what == 0) {
                            // set default thumbnail
                            Log.d(TAG + " handler ", " msg.what = 0");
                            resetVisibilityThumbnail();
                        }
                    }
                }
            }
        };
        notificationService.ContinuePlayAsVideoWhenStopActivity();
    }

    public void resetVisibilityThumbnail() {
        if (videoTextureViewFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(videoTextureViewFragment).commit();
            thumbnail_imageView.setVisibility(View.VISIBLE);
            videoTextureViewFragment = null;
        }
    }


    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.e(TAG+"Service Connection", "Disconnected");
        notificationService = null;
    }

//    @Override
//    public void onSetDataPassing(ArrayList<SongModel> songModelArrayList) {
//
//        this.songModelArrayList = songModelArrayList;
//        if (songModelArrayList == null) {
//            Log.d("Data from fragment to "+TAG, "NULL");
//        } else {
//
//            for (SongModel i : songModelArrayList) {
//                Log.d("Data from fragment to "+TAG, String.valueOf(i));
//            }
//        }
//    }


    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();

        if (notificationService != null) {
            notificationService.stopUpdateUi();
        }
        isAliveMainActivity = false;
        MyInitialMediaPlayer.playAsAudio = true;
        updateFragment = true;
        unbindService(this);
//        orientationListener.disable();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    private void playBtnClicked(){
        Intent intent = new Intent(this, NotificationService.class);
        intent.putExtra(MY_COMMAND, ACTION_PLAY);
        startService(intent);
    }

    private void prevBtnClicked(){
        Intent intent = new Intent(this, NotificationService.class);
        intent.putExtra(MY_COMMAND, ACTION_PREV);
        startService(intent);
    }

    private void nextBtnClicked(){
        Intent intent = new Intent(this, NotificationService.class);
        intent.putExtra(MY_COMMAND, ACTION_NEXT);
        startService(intent);
    }

    private void loopBtnClicked(){
        Intent intent = new Intent(this, NotificationService.class);
        intent.putExtra(MY_COMMAND, ACTION_REPEAT);
        startService(intent);
    }

    private void shuffleBtnClicked(){
        Intent intent = new Intent(this, NotificationService.class);
        intent.putExtra(MY_COMMAND, ACTION_SHUFFLE);
        startService(intent);
    }

    private void mini_playBtnClicked(){
        Intent intent = new Intent(this, NotificationService.class);
        intent.putExtra(MY_COMMAND, ACTION_MINI_PLAY);
        startService(intent);
    }

    private void showRepeatSectionDialog(){
        RepeatSectionDialog dialog = new RepeatSectionDialog(this);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void getSizeFrameLayout(){
        int w = relativeLayout.getWidth();
        int h = relativeLayout.getHeight();
        Log.d(TAG, " getSizeFrameLayout:" + " w: "+w + " " + " h: "+h);
    }
    private void hideButtonLandscapeMode(){
        Log.d(TAG ," hideButtonLandscapeMode");
        motionLayout.getConstraintSet(R.id.start).setAlpha(R.id.containerBtn_player, 0);
        motionLayout.getConstraintSet(R.id.start).setAlpha(R.id.container_seekBar, 0);
    }
    private void showButtonPortraitMode(){
        Log.d(TAG ," showButtonPortraitMode");
        motionLayout.getConstraintSet(R.id.start).setAlpha(R.id.containerBtn_player, 1);
        motionLayout.getConstraintSet(R.id.start).setAlpha(R.id.container_seekBar, 1);
    }
    private static class OrientationListener extends OrientationEventListener{

        final int PORTRAIT = 1;
        final int LANDSCAPE = 2;
        final int REVERT_PORTRAIT = 3;
        final int REVERT_LANDSCAPE = 4;

        int currentOrientation = 0;
        Context context;

        public OrientationListener(Context context) {
            super(context);
            this.context = context;
        }


        @Override
        public void onOrientationChanged(int orientation) {
            Log.d("OrientationListener", " onOrientationChanged");

            if ((orientation < 45 || orientation > 315) && currentOrientation != PORTRAIT){
                currentOrientation = PORTRAIT;
            } else if ((orientation >= 45 && orientation < 135) && currentOrientation != LANDSCAPE){
                currentOrientation = LANDSCAPE;
            } else if ((orientation >= 135 && orientation < 225) && currentOrientation != REVERT_PORTRAIT){
                currentOrientation = REVERT_PORTRAIT;
            } else if ((orientation >= 225 && orientation <= 315) && currentOrientation != REVERT_LANDSCAPE){
                currentOrientation = REVERT_LANDSCAPE;
            }
        }
    }
}