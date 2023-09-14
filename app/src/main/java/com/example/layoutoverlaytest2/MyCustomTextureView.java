package com.example.layoutoverlaytest2;

import static com.example.layoutoverlaytest2.Fragments.VideoTextureViewFragment.currentSizeTextureView_width;
import static com.example.layoutoverlaytest2.Fragments.VideoTextureViewFragment.currentSizeTextureView_height;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyCustomTextureView extends TextureView {

    final String TAG = " MyCustomTextureView ";

    public MyCustomTextureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG+" Constructor",String.valueOf(attrs));
    }
//    public MyCustomTextureView(@NonNull Context context) {
//        super(context);
//    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG+" onSizeChange","curW: "+w +" "+ "curH: "+h + " "+ "oldW: "+oldw + " "+ "oldH: "+oldh);

//        if (oldh > 0 && oldw > 0) {
//            Size size = getSizeToFitScreen(oldw, oldh, 16, 9);
//            w = size.getWidth();
//            h = size.getHeight();
//        }
        currentSizeTextureView_width = w;
        currentSizeTextureView_height = h;
        Log.e(TAG + " onSizeChange", " currentSizeTextureView_width: "+ currentSizeTextureView_width + " " + " currentSizeTextureView_height: "+currentSizeTextureView_height);
    }
    private Size getSizeToFitScreen(int widthFrame, int heightFrame, int widthOrigin, int heightOrigin){
        double widthResult = 0;
        double heightResult = 0;

        double widthTestWidth;
        double heightTestWidth;

        double widthTestHeight;
        double heightTestHeight;

        double widthFrameDouble = (double) widthFrame;
        double heightFrameDouble = (double) heightFrame;

        double widthOriginDouble = (double) widthOrigin;
        double heightOriginDouble = (double) heightOrigin;

        if (widthFrameDouble > 0 && heightFrameDouble > 0 && widthOriginDouble > 0 && heightOriginDouble > 0) {
            double ratio = widthOriginDouble / heightOriginDouble;
            Log.d(TAG + " getSize", " ratio: " + ratio);

            // scale with width
            widthTestWidth = widthFrameDouble;
            heightTestWidth = widthFrameDouble / ratio;
            Log.d(TAG + " getSize", " heightTestWidth: " + heightTestWidth);

            // scale with height
            widthTestHeight = heightFrameDouble * ratio;
            heightTestHeight = heightFrameDouble;
            Log.d(TAG + " getSize", " widthTestHeight: " + widthTestHeight);

            if (heightTestWidth <= heightFrame) {
                Log.d("scale with width", " start");
                widthResult = widthTestWidth;
                heightResult = heightTestWidth;
            } else if (widthTestHeight <= widthFrame) {
                Log.d("scale with height", " start");
                widthResult = widthTestHeight;
                heightResult = heightTestHeight;
            }
            Log.d(TAG + " getSizeToFitScreen", "Frame: " + widthFrame + " - " + heightFrame + "...." + "Origin: " + widthOrigin + " - " + heightOrigin);
            Log.d(TAG + " getSizeToFitScreen", "Result: " + widthResult + " - " + heightResult);
            Log.d(TAG + " getSizeToFitScreen", "FinalResult: " + (int) Math.round(widthResult) + " - " + (int) Math.round(heightResult));
        } else {
            Log.d(TAG + " getSizeToFitScreen", " size = 0");
        }

        return new Size((int) Math.round(widthResult), (int) Math.round(heightResult));
    }
}
