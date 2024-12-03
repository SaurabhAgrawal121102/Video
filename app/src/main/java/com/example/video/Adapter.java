package com.example.video;

import android.app.Application;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.SimpleExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;



public class Adapter extends RecyclerView.ViewHolder {

    View mview;
    ExoPlayer exoPlayer;
    PlayerView mExoplayerview;
    public Adapter(@NonNull View itemView) {
        super(itemView);
        mview = itemView;
    }
    public void setvideo(Application ct,String title,final String Url)
    {
        TextView textView  = mview.findViewById(R.id.title1);
        mExoplayerview = mview.findViewById(R.id.exoplayer_view);
        textView.setText(title);
        /*


        try {
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(ct).build();
            TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
        }
        */

    }
}
