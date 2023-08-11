package com.example.layoutoverlaytest2.MusicFragmentAdapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.layoutoverlaytest2.R;

public class SongViewHolder extends RecyclerView.ViewHolder {
    TextView musicFragment_title_tv;
    ImageView musicFragment_thumbnail_imgView;
    public SongViewHolder(@NonNull View itemView) {
        super(itemView);
        musicFragment_title_tv = itemView.findViewById(R.id.fragment_music_tv_item_songName);
        musicFragment_thumbnail_imgView = itemView.findViewById(R.id.fragment_music_img_item_icon);

    }
}
