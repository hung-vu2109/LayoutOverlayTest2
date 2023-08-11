package com.example.layoutoverlaytest2.MusicFragmentAdapter;

import static com.example.layoutoverlaytest2.ApplicationClass.MY_COMMAND;
import static com.example.layoutoverlaytest2.ApplicationClass.PLAY_FROM_SONG_LIST;
import static com.example.layoutoverlaytest2.ApplicationClass.REMOVE_SONG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.layoutoverlaytest2.Models.SongModel;
import com.example.layoutoverlaytest2.Utils.MyInitialMediaSongPlayer;
import com.example.layoutoverlaytest2.R;
import com.example.layoutoverlaytest2.Services.NotificationService;

import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongViewHolder> {

    Context context;
    ArrayList<SongModel> songModelArrayList;

    public SongAdapter(Context context, ArrayList<SongModel> songModelArrayList) {
        this.context = context;
        this.songModelArrayList = songModelArrayList;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_music_recycler_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, @SuppressLint("RecyclerView") int position) {
        SongModel songData = songModelArrayList.get(position);
        holder.musicFragment_title_tv.setText(songData.getTitle());

        holder.itemView.setOnClickListener(view -> {
            Toast.makeText(context, "item "+songData.getTitle()+" is Clicked", Toast.LENGTH_SHORT).show();

            MyInitialMediaSongPlayer.getInstance().reset();
            MyInitialMediaSongPlayer.starterIndex = position;

            Log.d("SongAdapter", String.valueOf(MyInitialMediaSongPlayer.starterIndex));


            Intent intent = new Intent(context, NotificationService.class);

//            Bundle bundle = new Bundle();
//            bundle.putSerializable("SONG_BUNDLE", songModelArrayList);
//            intent.putExtra("MY_BUNDLE", bundle);
//            intent.putExtra("SONG_LIST", songModelArrayList);

            intent.putExtra(MY_COMMAND, PLAY_FROM_SONG_LIST);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startService(intent);
        });


        holder.itemView.setOnLongClickListener(view -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle("Delete Item");
            alertDialog.setMessage("Delete item "+ songData.getTitle() + " from your play list");
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.d("Delete song from playlist",  songData.getTitle()+"");
//                    remove song from playlist on RecyclerView
                    songModelArrayList.remove(songData);
                    Toast.makeText(context, "Deleted item", Toast.LENGTH_SHORT).show();
                    notifyItemRemoved(holder.getAdapterPosition());
                    notifyItemRangeChanged(position, songModelArrayList.size());

//                    remove song in service
                    MyInitialMediaSongPlayer.removeIndex = position;
                    Intent intent = new Intent(context, NotificationService.class);
                    intent.putExtra(MY_COMMAND, REMOVE_SONG);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startService(intent);

                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    alertDialog.setView(View.GONE);
                }
            });
            AlertDialog dialog = alertDialog.create();
            dialog.show();
            dialog.setCanceledOnTouchOutside(true);
            return false;
        });

    }

    @Override
    public int getItemCount() {
        return songModelArrayList.size();
    }

}
