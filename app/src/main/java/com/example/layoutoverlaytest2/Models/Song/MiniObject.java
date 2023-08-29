package com.example.layoutoverlaytest2.Models.Song;

import android.widget.ImageView;
import android.widget.TextView;

public class MiniObject {
    TextView mini_songName;
    ImageView mini_playBtn;

    public MiniObject(TextView mini_songName,ImageView mini_playBtn) {
        this.mini_songName = mini_songName;
        this.mini_playBtn = mini_playBtn;
    }

    public TextView getMini_songName() {
        return mini_songName;
    }

    public ImageView getMini_playBtn() {
        return mini_playBtn;
    }
}
