package com.example.android.gpslog_test;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ActivityViewHolder extends RecyclerView.ViewHolder
{

    public ImageView actTypeImg, clockImg;
//    public ImageView actTypeImg, deleteActivity, clockImg;
    public TextView actStartText, actDurText;
    public MaterialCardView historyView, deleteView;


    public ActivityViewHolder(@NonNull View itemView) {
        super(itemView);
        actTypeImg = itemView.findViewById(R.id.actTypeImg);
        actStartText = itemView.findViewById(R.id.actStartText);
//        actDurText = itemView.findViewById(R.id.actDurText);
//        clockImg = itemView.findViewById(R.id.clock);
//        deleteActivity = itemView.findViewById(R.id.deleteActImg);
        historyView = itemView.findViewById(R.id.historyView);
//        deleteView = itemView.findViewById(R.id.deleteView);
    }

}
