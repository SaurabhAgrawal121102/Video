package com.example.video.convertvideointovideobitrate;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.video.R;

import java.util.List;

public class Adapter_data extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    Context context;
    List<Model_data> modelDatalist;

    public Adapter_data(Context context, List<Model_data> modelDatalist) {
        this.context = context;
        this.modelDatalist = modelDatalist;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.row_timestamp_for_playvideo,parent,false);

        return new Timestamp(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        String videoId = modelDatalist.get(position).getVideoId();
        String description  = modelDatalist.get(position).getDescription();
        String timestamp = modelDatalist.get(position).getTimeStamp();
        String hlsurl =modelDatalist.get(position).getHlsUrl();
        String videoUrl =modelDatalist.get(position).getVideoUrl();

        Timestamp timestampHolder = (Timestamp) holder;

        // Set the data to the TextView
        timestampHolder.textview_one.setText(timestamp);

        ((Timestamp) holder).textview_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Click on timestamp"+timestamp, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, PlayVideoActivity.class);
                intent.putExtra("timestamp",timestamp);
                intent.putExtra("videourl",hlsurl);
              //  intent.putExtra("hlsurl",hlsurl);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return modelDatalist.size();
    }

    class  Timestamp extends  RecyclerView.ViewHolder
    {
        TextView textview_one;

        public Timestamp(@NonNull View itemView) {
            super(itemView);

            textview_one = (TextView) itemView.findViewById(R.id.textview);
        }
    }
}
