package com.example.layoutoverlaytest2.Dialogs;

import static com.example.layoutoverlaytest2.ApplicationClass.ACTION_REPEAT_SECTION;
import static com.example.layoutoverlaytest2.ApplicationClass.MY_COMMAND;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.layoutoverlaytest2.R;
import com.example.layoutoverlaytest2.Services.NotificationService;

import java.util.HashMap;

public class RepeatSectionDialog extends Dialog {

    final String TAG = " RepeatSectionDialog ";
    EditText minStartText, secStartText, minEndText, secEndText;
    ImageButton closeSectionBtn;
    Button loopSectionBtn;



    public RepeatSectionDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, " onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loop_section);

        minStartText = findViewById(R.id.edt_Minute_StartPoint);
        secStartText = findViewById(R.id.edt_Second_StartPoint);

        minEndText = findViewById(R.id.edt_Minute_EndPoint);
        secEndText = findViewById(R.id.edt_Second_EndPoint);

        closeSectionBtn = findViewById(R.id.btn_Close_LoopSectionDialog);
        closeSectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        loopSectionBtn = findViewById(R.id.btn_Ok_LoopSectionDialog);
        loopSectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<Long, Long> hashMap = new HashMap<Long, Long>();
                if (minStartText.length() > 0 && minEndText.length() > 0 && secStartText.length() > 0 && secEndText.length() > 0) {
//                    Start Point
                    long milliStart = ((Long.parseLong(String.valueOf(minStartText.getText()))*60) +
                            Long.parseLong(String.valueOf(secStartText.getText())))*1000;
                    Log.d("RepeatSection Dialog ", milliStart + "");
//                    End Point
                    long milliEnd = ((Long.parseLong(String.valueOf(minEndText.getText()))*60) +
                            Long.parseLong(String.valueOf(secEndText.getText())))*1000;
                    Log.d("RepeatSection Dialog ", milliStart + "");
                    hashMap.put(milliStart,milliEnd);

                    Intent intent = new Intent(getContext(), NotificationService.class);
                    intent.putExtra(MY_COMMAND, ACTION_REPEAT_SECTION);
                    intent.putExtra("PAIR_VALUE", hashMap);
                    getContext().startService(intent);
                } else {
                    Toast.makeText(getContext(), "RepeatSection Dialog "+" No Null", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//    @NonNull
//    @Override
//    public Bundle onSaveInstanceState() {
//        super.onSaveInstanceState();
//        onSaveInstanceState().putString("minStartText", String.valueOf(minStartText.getText()));
//        onSaveInstanceState().putString("secStartText", String.valueOf(secStartText.getText()));
//        onSaveInstanceState().putString("minEndText", String.valueOf(minEndText.getText()));
//        onSaveInstanceState().putString("secEndText", String.valueOf(secEndText.getText()));
//        return onSaveInstanceState();
//    }
//
//    @Override
//    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        minStartText.setText(onSaveInstanceState().getString("minStartText","00"));
//        secStartText.setText(onSaveInstanceState().getString("minStartText","00"));
//        minEndText.setText(onSaveInstanceState().getString("minStartText","00"));
//        secEndText.setText(onSaveInstanceState().getString("minStartText","00"));
//    }

}
