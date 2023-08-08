package com.example.layoutoverlaytest2;

public class UpdateUi {

//        activity.runOnUiThread( runnable = new Runnable() {
//            @Override
//            public void run() {
//                Log.i(TAG + " Current Thread ", Thread.currentThread().getName());
//
////                Play Button UI
//                if (currentSong!= null) {
//                    if (mediaPlayer.isPlaying()) {
//                        buttonMainObject.getPlayBtn().setBackgroundResource(R.drawable.baseline_pause_circle_outline_24);
//                    } else {
//                        buttonMainObject.getPlayBtn().setBackgroundResource(R.drawable.baseline_play_circle_outline_24);
//                    }
//                }
//                /*********************************************************************************************/
//
////                Shuffle Button UI
//                if (isShuffleSongs) {
//                    buttonMainObject.getShuffleBtn().setBackgroundResource(R.drawable.baseline_shuffle_on_24);
//                } else {
//                    buttonMainObject.getShuffleBtn().setBackgroundResource(R.drawable.baseline_shuffle_24);
//                }
//                /*********************************************************************************************/
////                Loop Button UI
//                switch (pressedTimes) {
//                    case 0:
//                        // no loop
//                        loopAllSongs = false;
//                        loopOneSong = false;
//                        Log.d("LoopAllSongs", "No Loop");
//                        buttonMainObject.getLoopBtn().setBackgroundResource(R.drawable.baseline_repeat_24);
//                        break;
//                    case 1:
//                        // loop 1 songs
//                        loopAllSongs = false;
//                        loopOneSong = true;
//                        loopSongOptions();
//                        Log.d("LoopAllSongs", "loopCurrentSongs ");
//                        buttonMainObject.getLoopBtn().setBackgroundResource(R.drawable.baseline_repeat_one_on_24);
//                        break;
//                    case 2:
//                        // loop all song
//                        loopAllSongs = true;
//                        loopOneSong = false;
//                        loopSongOptions();
//                        Log.d("LoopAllSongs", "loopAllSongs: " + loopAllSongs);
//                        buttonMainObject.getLoopBtn().setBackgroundResource(R.drawable.baseline_repeat_on_24);
//                        break;
//                }
//                /*********************************************************************************************/
//
////                Title Song Name
//                textViewMainObject.getSongNameTv().setText(getSongName());
//
//                /*********************************************************************************************/
//
////                Title Start Time
//                if (currentSong != null) {
//                    textViewMainObject.getCurrentTimeTv().setText(formatLongToMMSS(String.valueOf(mediaPlayer.getCurrentPosition())));
//                }
//                /*********************************************************************************************/
//
////                Title End Time
//                if (mediaPlayer != null && currentSong != null) {
//                    textViewMainObject.getEndTimeTv().setText(formatLongToMMSS(String.valueOf(currentSong.getDuration())));
//                }
//                /*********************************************************************************************/
//
////                SeekBar Code Start
//                if (currentSong != null && mediaPlayer != null) {
//
//                    seekBar.setMax(mediaPlayer.getDuration());
//
//                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
//
//                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                        @Override
//                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                            if (b) {
//                                try {
//
//                                    buttonMainObject.getPlayBtn().setBackgroundResource(R.drawable.baseline_play_circle_outline_24);
//                                    mediaPlayer.seekTo(i);
//                                    mediaPlayer.start();
//                                } catch (Exception e){
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onStartTrackingTouch(SeekBar seekBar) {
//
//                        }
//
//                        @Override
//                        public void onStopTrackingTouch(SeekBar seekBar) {
//
//                        }
//                    });
//
//                }
//                /*********************************************************************************************/
//
////                mini UI
//                miniObject.getMini_songName().setText(getSongName());
//
//                miniObject.getMini_playBtn().setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (currentSong != null) {
//                            if (mediaPlayer.isPlaying()) {
//                                mediaPlayer.pause();
//                                Log.d(TAG + "Mini Player", "Pausing");
//                            } else {
//                                mediaPlayer.start();
//                                Log.d(TAG + "Mini Player", "Playing");
//                            }
//                        } else {
//                            setInitialDataSource();
//                        }
//                    }
//                });
//
//                if (currentSong != null) {
//                    if (mediaPlayer.isPlaying()) {
//                        miniObject.getMini_playBtn().setBackgroundResource(R.drawable.baseline_pause_24);
//                    } else {
//                        miniObject.getMini_playBtn().setBackgroundResource(R.drawable.baseline_play_arrow_24);
//                    }
//                }
//                /*********************************************************************************************/
//
////                repeatSection
//                if (isRepeatSection && pressedTimes == 1) {
//                    if (currentSong != null &&
//                            formatLongToMMSS(String.valueOf(mediaPlayer.getCurrentPosition())).equals(formatLongToMMSS(String.valueOf(intEndPoint)))) {
//
//                        Log.d(TAG + "repeat Section State pressTime", pressedTimes + "");
//                        Log.d(TAG + "repeat Section State current Duration", mediaPlayer.getCurrentPosition() + "");
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//                            Log.d(TAG + "repeat Section State is", isRepeatSection + " inside");
//                            mediaPlayer.seekTo(longStartPoint, MediaPlayer.SEEK_CLOSEST_SYNC);
//                        } else {
//                            mediaPlayer.seekTo(intStartPoint);
//                        }
//                    }
//                } else {
//                    isRepeatSection = false;
//                }
//                Log.d(TAG + "repeat Section State is", isRepeatSection + "");
//
//                try {
//                    handler.postDelayed(this, 500);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
}
