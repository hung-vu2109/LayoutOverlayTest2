package com.example.layoutoverlaytest2.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.layoutoverlaytest2.MusicFragmentAdapter.SongAdapter;
import com.example.layoutoverlaytest2.Interfaces.DataPassingInterface;
import com.example.layoutoverlaytest2.Models.SongModel;
import com.example.layoutoverlaytest2.R;

import java.io.File;
import java.util.ArrayList;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link MusicFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class MusicFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
    View view;
    ArrayList<SongModel> songModelArrayList = new ArrayList<>();

    RecyclerView fragmentMusicRecyclerView;
    TextView noSong_tv;
    DataPassingInterface dataPassingInterface;
    static final String TAG = "Music Fragment ";

    public MusicFragment() {
        // Required empty public constructor
    }


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

        noSong_tv = getActivity().findViewById(R.id.fragment_music_tv_noSong);


//      Access media files from shared storage
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

        try (Cursor cursor = getContext().getContentResolver().query(collectionUri, projection, selection, null, sortOrder)){

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

                if (songModelArrayList.size() == 0){
                    noSong_tv.setVisibility(View.VISIBLE);
                }else {
//                    fragmentMusicRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//                    fragmentMusicRecyclerView.setAdapter(new SongAdapter(getContext(), songModelArrayList));
                    recyclerViewSetAdapter();
                }
            }
        }

//        passData(songModelArrayList);

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

    }

    public void recyclerViewSetAdapter(){
        fragmentMusicRecyclerView.setLayoutManager(new LinearLayoutManager((getContext())));
        fragmentMusicRecyclerView.setAdapter(new SongAdapter(getContext(), songModelArrayList));
        fragmentMusicRecyclerView.setHasFixedSize(true);
    }
    public void passData(ArrayList<SongModel> songModelArrayList){
        dataPassingInterface.onSetDataPassing(songModelArrayList);
    }

}