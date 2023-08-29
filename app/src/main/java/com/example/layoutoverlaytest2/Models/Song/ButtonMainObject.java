package com.example.layoutoverlaytest2.Models.Song;

import android.widget.ImageButton;

public class ButtonMainObject {
    ImageButton loopBtn;
    ImageButton prevBtn;
    ImageButton playBtn;
    ImageButton nextBtn;
    ImageButton shuffleBtn;

    public ButtonMainObject(ImageButton loopBtn, ImageButton prevBtn, ImageButton playBtn, ImageButton nextBtn, ImageButton shuffleBtn) {
        this.loopBtn = loopBtn;
        this.prevBtn = prevBtn;
        this.playBtn = playBtn;
        this.nextBtn = nextBtn;
        this.shuffleBtn = shuffleBtn;
    }

    public ImageButton getLoopBtn() {
        return loopBtn;
    }

    public void setLoopBtn(ImageButton loopBtn) {
        this.loopBtn = loopBtn;
    }

    public ImageButton getPrevBtn() {
        return prevBtn;
    }

    public void setPrevBtn(ImageButton prevBtn) {
        this.prevBtn = prevBtn;
    }

    public ImageButton getPlayBtn() {
        return playBtn;
    }

    public void setPlayBtn(ImageButton playBtn) {
        this.playBtn = playBtn;
    }

    public ImageButton getNextBtn() {
        return nextBtn;
    }

    public void setNextBtn(ImageButton nextBtn) {
        this.nextBtn = nextBtn;
    }

    public ImageButton getShuffleBtn() {
        return shuffleBtn;
    }

    public void setShuffleBtn(ImageButton shuffleBtn) {
        this.shuffleBtn = shuffleBtn;
    }
}
