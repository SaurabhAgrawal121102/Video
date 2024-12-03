package com.example.video;

import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;


import java.util.ArrayList;

public class Adaptervideo extends RecyclerView.Adapter<Adaptervideo.HolderVideo>{


    private Context context;
    private ArrayList<viewmodal> viewmodalArrayList;

    public Adaptervideo(Context context, ArrayList<viewmodal> viewmodalArrayList) {
        this.context = context;
        this.viewmodalArrayList = viewmodalArrayList;
    }

    @NonNull
    @Override
    public HolderVideo onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.singlerow,parent,false);
        return new HolderVideo(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderVideo holder, int position) {

        viewmodal viewmodal = viewmodalArrayList.get(position);
        String title1 = viewmodal.getTitle();
        String videourl = viewmodal.getVideourl();

        holder.title.setText(title1);
        setVideoUrl(viewmodal,holder);

    }

    private void setVideoUrl(viewmodal viewmodal, HolderVideo holder) {

        holder.progressBar.setVisibility(View.GONE);

        String videourl = viewmodal.getVideourl();
        if (videourl!=null)
        {
            MediaController mediaController = new MediaController(context);
            mediaController.setAnchorView(holder.video);

            Uri videouri = Uri.parse(videourl);
            holder.video.setMediaController(mediaController);
            holder.video.setVideoURI(videouri);
            holder.video.requestFocus();
            holder.video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // video is ready to play
                    mp.start();
                }
            });

            holder.video.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {

                    // to check if buffering,rendering etc

                    switch (what)
                    {
                        case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        {
                            // rendering started

                            holder.progressBar.setVisibility(View.VISIBLE);
                            return true;
                        }
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        {
                            // buffering started
                            holder.progressBar.setVisibility(View.VISIBLE);
                            return true;
                        }
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        {
                            // buffering end
                            holder.progressBar.setVisibility(View.GONE);
                            return true;
                        }
                    }
                    return false;
                }
            });
            holder.video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.start();
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return viewmodalArrayList.size();
    }


    class HolderVideo extends RecyclerView.ViewHolder
    {

        VideoView video;
        TextView title;
        ProgressBar progressBar;


        public HolderVideo(@NonNull View itemView) {
            super(itemView);

            video = (VideoView) itemView.findViewById(R.id.view1);
            title = (TextView) itemView.findViewById(R.id.title);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressbar);
        }
    }


}
