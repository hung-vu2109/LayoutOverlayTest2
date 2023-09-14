package com.example.layoutoverlaytest2.Fragments;



import static com.example.layoutoverlaytest2.Services.NotificationService.songModelArrayList;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.layoutoverlaytest2.Activities.MainActivity;
import com.example.layoutoverlaytest2.Adapters.MusicFragmentAdapter.SongAdapter;
import com.example.layoutoverlaytest2.Interfaces.DataPassingInterface;
import com.example.layoutoverlaytest2.Models.Song.SongModel;
import com.example.layoutoverlaytest2.R;

import java.util.ArrayList;


public class MusicFragment extends Fragment{

    View view;
    SongAdapter songAdapter;
    RecyclerView fragmentMusicRecyclerView;
    TextView noSong_tv;
    DataPassingInterface dataPassingInterface;
//    ArrayList<SongModel> songModelArrayList;
    static final String TAG = "Music Fragment ";
    MainActivity mainActivity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_music, container, false);
        fragmentMusicRecyclerView = view.findViewById(R.id.fragment_music_recyclerView);
        noSong_tv = view.findViewById(R.id.fragment_music_tv_noSong);

//        passData(songModelArrayList);
        if (songModelArrayList != null) {
            if (songModelArrayList.isEmpty()) {
                noSong_tv.setVisibility(View.VISIBLE);
            } else {
                recyclerViewSetAdapter();
            }
        }
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        try {
//            dataPassingInterface = (DataPassingInterface) context;
//        } catch (ClassCastException e){
//            e.printStackTrace();
//        }

        mainActivity = (MainActivity) getActivity();
    }

    public void recyclerViewSetAdapter(){
        if (mainActivity != null) {
            fragmentMusicRecyclerView.setLayoutManager(new LinearLayoutManager((getContext())));
            fragmentMusicRecyclerView.setAdapter(songAdapter = new SongAdapter(getContext(), songModelArrayList, mainActivity));
            fragmentMusicRecyclerView.setHasFixedSize(true);
        }
    }
    public void resetAdapter(){
        fragmentMusicRecyclerView.removeAllViews();
        fragmentMusicRecyclerView.setAdapter(songAdapter);
    }
    public void passData(ArrayList<SongModel> songModelArrayList){
        dataPassingInterface.onSetDataPassing(songModelArrayList);
    }

}