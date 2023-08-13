package com.example.layoutoverlaytest2;

import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_MINI_PLAY;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_NEXT;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_PLAY;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_PREV;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_REPEAT;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_SHUFFLE;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_STOP;
import static com.example.layoutoverlaytest2.ApplicationClass.MY_COMMAND;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.layoutoverlaytest2.Fragments.MusicFragment;
import com.example.layoutoverlaytest2.Fragments.VideoFragment;
import com.example.layoutoverlaytest2.Models.ButtonMainObject;
import com.example.layoutoverlaytest2.Models.MiniObject;
import com.example.layoutoverlaytest2.Models.TextViewMainObject;
import com.example.layoutoverlaytest2.Services.NotificationService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener, ServiceConnection {

    private static final String TAG = "MainActivity.java ";
    private static final int REQUEST_CODE = 592431;
    static final String[] RUNTIME_PERMISSION = { Manifest.permission.READ_EXTERNAL_STORAGE };
    MusicFragment musicFragment = new MusicFragment();
    TextView title_songName,currentTime,endTime;
    TextView mini_songName;
    ImageView thumbnail_imageView;
    ImageView mini_playBtn, mini_closeBtn;
    ImageButton pauseBtn, nextBtn, prevBtn, loopBtn, shuffleBtn;
    SeekBar seekBar;
    NotificationService notificationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.nav_fragment_container_music);


        title_songName = findViewById(R.id.tv_player_songName);
        currentTime = findViewById(R.id.tv_player_currentTime);
        endTime = findViewById(R.id.tv_player_endTime);

        thumbnail_imageView = findViewById(R.id.top_image);

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

        if(!checkOverlayPer()){
             requestOverlayPer();
        }
       if(!checkReadStoragePer()){
           requestReadStoragePer();
       }

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

        if (itemId == R.id.pipMode_Option){
            Log.d(TAG, "Pip mode Option");
//            startPipModeService();
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
    }

    private boolean checkOverlayPer() {
        Log.d(TAG, "Check Overlay Permission");
        return Settings.canDrawOverlays(MainActivity.this);
    }

    private void requestOverlayPer() {

        Log.d(TAG, "Request Overlay Permission");
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));
        startActivity(intent);
//        finish();
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
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
        if (grantResults.length > 0 &&
        grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG+ " onRequestPermissionResult ", "Granted");
            musicFragment.resetAdapter();
            musicFragment.recyclerViewSetAdapter();

        } else {
            Log.d(TAG+ " onRequestPermissionResult", "Denied");
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_fragment_container_music:
                // music fragment
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentToReplace_container, musicFragment).addToBackStack("MusicSteveOverlay").commit();
//                Toast.makeText(this, "Music Tap", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_fragment_container_video:
                // video fragment
                VideoFragment videoFragment = new VideoFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentToReplace_container, videoFragment).addToBackStack("VideoSteveOverlay").commit();
//                Toast.makeText(this, "Video Tap", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        if (notificationService != null){
            notificationService.stopUpdateUi();
        }
        unbindService(this);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        Intent intent = new Intent(this, NotificationService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
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
                new MiniObject(mini_songName, mini_playBtn));

        mini_songName.setSelected(true);

        mini_playBtn.setOnClickListener(view -> {
            mini_playBtnClicked();
        });

        mini_closeBtn.setOnClickListener(view -> {
            finish();
        });
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

}