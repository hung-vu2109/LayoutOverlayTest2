package com.example.layoutoverlaytest2;

import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_NEXT;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_PLAY;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_PREV;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_REPEAT;
import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_SHUFFLE;
import static com.example.layoutoverlaytest2.ApplicationClass.MY_COMMAND;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
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
import com.example.layoutoverlaytest2.Models.SongModel;
import com.example.layoutoverlaytest2.Models.TextViewMainObject;
import com.example.layoutoverlaytest2.Services.NotificationService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener, ServiceConnection{

    private static final String TAG = "MainActivity.java ";
    private static final int REQUEST_CODE = 592431;
    static final String[] RUNTIME_PERMISSION = { Manifest.permission.READ_EXTERNAL_STORAGE };
    ArrayList<SongModel> songModelArrayList;
    TextView title_songName,currentTime,endTime;
    ImageView thumbnail_imageView;
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

        pauseBtn = findViewById(R.id.btn_player_pauseSong);
        nextBtn = findViewById(R.id.btn_player_nextSong);
        prevBtn = findViewById(R.id.btn_player_prevSong);

        loopBtn = findViewById(R.id.btn_player_loop);

        shuffleBtn = findViewById(R.id.btn_player_shuffle);

        seekBar = findViewById(R.id.seekBar_player);


        if(!checkOverlayPer()){
             requestOverlayPer();
        }
       if(!checkReadStoragePer()){
           requestReadStoragePer();
       }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        if (songModelArrayList == null) {
            Log.d(TAG+"Data from fragment when itemView is Clicked", "NULL");
        } else {

            Log.d(TAG+"Song list Size", songModelArrayList.size()+"");
        }

    }

    private boolean checkOverlayPer() {
        if (Settings.canDrawOverlays(MainActivity.this)){
            Toast.makeText(this, "Overlay Permission OK", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }

    private void requestOverlayPer() {

        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));
        startActivity(intent);
//        finish();
    }

    private boolean checkReadStoragePer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()){
                return true;
            } else {
                return false;
            }
        } else {
            if (ContextCompat.checkSelfPermission(MainActivity.this, RUNTIME_PERMISSION[0]) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        }
    }


    private void requestReadStoragePer() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
//           or
//            Intent intent = new Intent();
//            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
//            intent.setData(uri);
//            startActivity(intent);
        } else {
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_fragment_container_music:
                // music fragment
                MusicFragment musicFragment = new MusicFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentToReplace_container, musicFragment).addToBackStack("SteveOverlay").commit();
                Toast.makeText(this, "Music Tap", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_fragment_container_video:
                // video fragment
                VideoFragment videoFragment = new VideoFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentToReplace_container, videoFragment).addToBackStack("SteveOverlay").commit();
                Toast.makeText(this, "Video Tap", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, NotificationService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        NotificationService.MyBinder myBinder = (NotificationService.MyBinder) iBinder;
        notificationService = myBinder.getService();
        Log.e(TAG+"Service Connection", notificationService +"");


        pauseBtn.setOnClickListener(view -> {
            Log.d(TAG, "play btn clicked");
            playBtnClicked();
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
                seekBar);

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

    private void showRepeatSectionDialog(){}
}