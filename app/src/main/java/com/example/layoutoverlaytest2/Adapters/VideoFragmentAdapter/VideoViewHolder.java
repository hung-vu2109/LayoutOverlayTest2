package com.example.layoutoverlaytest2.Adapters.VideoFragmentAdapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.layoutoverlaytest2.R;

public class VideoViewHolder extends RecyclerView.ViewHolder {

    TextView videoFragment_title_tv;
    ImageView videoFragment_thumbnail_img;
    TextView videoFragment_duration_tv;
    TextView videoFragment_size_tv;

    public VideoViewHolder(@NonNull View itemView) {
        super(itemView);
        videoFragment_title_tv = itemView.findViewById(R.id.fragment_video_tv_item_videoName);
        videoFragment_thumbnail_img = itemView.findViewById(R.id.fragment_video_img_item_icon);
        videoFragment_duration_tv = itemView.findViewById(R.id.fragment_video_tv_item_durationVideo);
        videoFragment_size_tv = itemView.findViewById(R.id.fragment_video_tv_item_sizeVideo);
    }
}
