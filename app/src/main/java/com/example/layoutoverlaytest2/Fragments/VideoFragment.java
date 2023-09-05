package com.example.layoutoverlaytest2.Fragments;


import static com.example.layoutoverlaytest2.Services.NotificationService.videoModelArrayList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.layoutoverlaytest2.Adapters.VideoFragmentAdapter.VideoAdapter;
import com.example.layoutoverlaytest2.Models.Video.VideoModel;
import com.example.layoutoverlaytest2.R;


public class VideoFragment extends Fragment {

    private static final String TAG = "Video Fragment ";
    private RecyclerView fragmentVideoRecyclerView;
    TextView noVideoTv;
    View view;
    enum MyLayoutManager{
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }
    MyLayoutManager myLayoutManager = MyLayoutManager.GRID_LAYOUT_MANAGER;



    public VideoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);



    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionMenu");
        inflater.inflate(R.menu.fragment_video_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.gridView:
                myLayoutManager = MyLayoutManager.GRID_LAYOUT_MANAGER;
                break;
            case R.id.linearView:
                myLayoutManager = MyLayoutManager.LINEAR_LAYOUT_MANAGER;
                break;
        }
        if (fragmentVideoRecyclerView != null) {
            fragmentVideoRecyclerView.removeAllViews();
            setAdapterToRecyclerView();
        }

        if (item.getItemId() == R.id.popMode_Option){
            Log.d(TAG, "Pop mode Option");
            if(!checkOverlayPer()){
                requestOverlayPer();
            } else {
//            startPopModeService();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOptionsMenuClosed(@NonNull Menu menu) {
        super.onOptionsMenuClosed(menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");
        view = inflater.inflate(R.layout.fragment_video, container, false);
        fragmentVideoRecyclerView = view.findViewById(R.id.fragment_video_recyclerView);
        noVideoTv = view.findViewById(R.id.fragment_video_tv_noVideo);

        printVideoModelArrayList();
        setAdapterToRecyclerView();


        // return view that be Inflated layout for this fragment
        return view;
    }


    private void printVideoModelArrayList(){
        if (videoModelArrayList.size() > 0){
            Log.d(TAG+"size of video array", videoModelArrayList.size()+"");
            for ( VideoModel i : videoModelArrayList){
                Log.d(TAG+"print video array", i+"");
            }
        } else {
            noVideoTv.setVisibility(View.VISIBLE);
        }
    }
    private void setAdapterToRecyclerView(){
        Log.d(TAG, "setAdapterMethod");
        if (myLayoutManager == MyLayoutManager.LINEAR_LAYOUT_MANAGER) {
            fragmentVideoRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            fragmentVideoRecyclerView.setAdapter(new VideoAdapter(getContext(), videoModelArrayList, false));
        } else {
            fragmentVideoRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            fragmentVideoRecyclerView.setAdapter(new VideoAdapter(getContext(), videoModelArrayList, true));
        }
        fragmentVideoRecyclerView.setHasFixedSize(true);
    }

    private boolean checkOverlayPer() {
        Log.d(TAG, "Check Overlay Permission");
        return Settings.canDrawOverlays(getContext());
    }

    private void requestOverlayPer() {
        Log.d(TAG, "Request Overlay Permission");
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+ requireContext().getPackageName()));
        startActivity(intent);
//        finish();
    }
}
