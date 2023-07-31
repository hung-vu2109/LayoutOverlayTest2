package com.example.layoutoverlaytest2.Models;

import android.widget.TextView;

public class TextViewMainObject {
    TextView songNameTv;
    TextView currentTimeTv;
    TextView endTimeTv;

    public TextViewMainObject(TextView songNameTv, TextView currentTimeTv, TextView endTimeTv) {
        this.songNameTv = songNameTv;
        this.currentTimeTv = currentTimeTv;
        this.endTimeTv = endTimeTv;
    }

    public TextView getSongNameTv() {
        return songNameTv;
    }

    public void setSongNameTv(TextView songNameTv) {
        this.songNameTv = songNameTv;
    }

    public TextView getCurrentTimeTv() {
        return currentTimeTv;
    }

    public void setCurrentTimeTv(TextView currentTimeTv) {
        this.currentTimeTv = currentTimeTv;
    }

    public TextView getEndTimeTv() {
        return endTimeTv;
    }

    public void setEndTimeTv(TextView endTimeTv) {
        this.endTimeTv = endTimeTv;
    }
}
