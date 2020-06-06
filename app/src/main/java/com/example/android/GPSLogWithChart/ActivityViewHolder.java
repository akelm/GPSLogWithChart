package com.example.android.GPSLogWithChart;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

public class ActivityViewHolder extends RecyclerView.ViewHolder {

    public ImageView actTypeImg;
    public TextView actStartText;
    public MaterialCardView historyView;

    public ActivityViewHolder(@NonNull View itemView) {
        super(itemView);
        actTypeImg = itemView.findViewById(R.id.actTypeImg);
        actStartText = itemView.findViewById(R.id.actStartText);
        historyView = itemView.findViewById(R.id.historyView);
    }

}
