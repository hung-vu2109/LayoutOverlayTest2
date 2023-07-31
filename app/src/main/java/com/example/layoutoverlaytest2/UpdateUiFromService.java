package com.example.layoutoverlaytest2;

import android.content.Context;
import android.widget.ImageButton;

public class UpdateUiFromService implements Runnable {
    Context context;
    ImageButton imageButton;
    public UpdateUiFromService(Context context, ImageButton imageButton) {
        this.context = context;
        this.imageButton = imageButton;
    }

    @Override
    public void run() {

    }
}
