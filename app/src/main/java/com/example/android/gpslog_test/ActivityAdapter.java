package com.example.android.gpslog_test;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityViewHolder> {

    private Activity activity;
    private List<ExerciseEntity> listActivity;
    private OnItemClickListener listener;

    //
    public ActivityAdapter(Activity activ) {
        activity =activ;
        listActivity = new ArrayList<ExerciseEntity>();

    }

    //
    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_list_layout, parent, false);
        return new ActivityViewHolder(view);
    }

    void setExercises(List<ExerciseEntity> e) {
        listActivity = e;
        notifyDataSetChanged();
    }

    ExerciseEntity getExercise(int ind) {
        return listActivity.get(ind);
    }

    @Override
    public void onBindViewHolder(ActivityViewHolder holder, int position) {
        final ExerciseEntity activityRC = listActivity.get(position);

        SimpleDateFormat sdfStart = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        String StartText = sdfStart.format(activityRC.getStart());

        holder.actStartText.setText(StartText);

        int imRes = -1;
        switch (activityRC.getTypeId()) {
            case "WALK":
                imRes = R.drawable.ic_directions_walk_black_24dp;
                break;
            case "RUN":
                imRes = R.drawable.ic_directions_run_black_24dp;
                break;
            case "BIKE":
                imRes = R.drawable.ic_directions_bike_black_24dp;
                break;
        }
        if (imRes > -1)
            holder.actTypeImg.setImageResource(imRes);
            holder.actTypeImg.getDrawable().setTint(activity.getColor(android.R.color.white));

        holder.historyView.setOnClickListener(v -> {
            if (listener != null && position != RecyclerView.NO_POSITION) {
                try {
                    listener.onItemClick(getExercise(position));
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        if (listActivity != null)
            return listActivity.size();
        else return 0;

    }

    public void setOnItemClickListener(OnItemClickListener lis) {
        listener = lis;
    }
    public interface OnItemClickListener {
        void onItemClick(ExerciseEntity note) throws ExecutionException, InterruptedException;
    }

}
