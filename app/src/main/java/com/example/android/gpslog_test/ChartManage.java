package com.example.android.gpslog_test;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.List;

public class ChartManage implements OnChartValueSelectedListener {
    public enum TimeUnit {SECONDS, MINUTES, HOURS}

    public static TimeUnit timeUnit = TimeUnit.SECONDS;
    public static long TOMINS = 120 * 1000000000L; // 120 seconds
    public static long TOHOURS = 120 * 60 * 1000000000L; // 120 mins

    public static LineChart chart = null;
    public static LineData data = null;
    private static ChartManage instance = null;
    static Activity activity = null;

    private List<ExerciseEntity> listActivity;

    // singleton
    private ChartManage() {
    }

    public static ChartManage getInstance() {
        if (instance == null) {
            instance = new ChartManage();
        }
        return instance;
    }

    public void setup(Activity act) {
        if (instance == null) {
            instance = new ChartManage();
        }
        data = new LineData();
        chart = act.findViewById(R.id.chart1);
        chartStyleSetup();
        activity = act;
    }

    private static LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "DataSet 1");
        set.setLineWidth(2.5f);
        set.setCircleRadius(4.5f);
        set.setColor(Color.rgb(240, 99, 99));
        set.setCircleColor(Color.rgb(240, 99, 99));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        set.setHighLightColor(Color.BLACK);
        set.setDrawValues(false);

        return set;
    }
    

    public static void setData(List<TrackEntity> tracks) {
        clearChart();
        if (tracks!=null) {
            for (TrackEntity tr : tracks) {
                addTrack(tr);
            }
        }

    }

    public static void clearChart() {
        while (data.getDataSetCount() > 0) {
            data.removeDataSet(0);
        }
        chart.setDrawMarkers(false);
        data.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();

    }

    public static void addTrack(TrackEntity track) {
        if (track != null) {
            addEntry(track.getTime(), track.getVel());
        }
    }

    public static void addEntry(long duration, float velocity) {
        if (data.getDataSetCount() == 0) {
            data.addDataSet(createSet());
        }

        float dur = timeToCurrentUnits(duration);

        data.addEntry(new Entry(dur, velocity), 0);
        chart.getXAxis().setAxisMaximum(dur);
        data.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.setScaleX(1);
        chart.setScaleY(1);
        chart.invalidate();

    }

    public static float timeToCurrentUnits(long duration) {
        if (duration <= TOMINS) { // seconds
            return duration / 1e9f;
        }
        if (duration <= TOHOURS) { // minutes
            if (timeUnit == TimeUnit.SECONDS) { // change xdata to mins
                for (int i = 0; i < data.getDataSetByIndex(0).getEntryCount(); i++) {
                    Entry e = data.getDataSetByIndex(0).getEntryForIndex(i);
                    e.setX(e.getX() / 60f); // seconds to minutes
                }
                TextView tv = activity.findViewById(R.id.xlabel);
                tv.setText("time, min");
                timeUnit = TimeUnit.MINUTES;
            }
            return duration / 1e9f / 60f;
        }
        if (timeUnit == TimeUnit.MINUTES) {
            for (int i = 0; i < data.getDataSetByIndex(0).getEntryCount(); i++) {
                Entry e = data.getDataSetByIndex(0).getEntryForIndex(i);
                e.setX(e.getX() / 60f); // minutes to hours
            }
            TextView tv = activity.findViewById(R.id.xlabel);
            tv.setText("time, h");
            timeUnit = TimeUnit.HOURS;
        }
        return duration / 1e9f / 60f / 60f;
    }

    private void chartStyleSetup() {
        // background color
        chart.setBackgroundColor(Color.WHITE);

        // disable description text
        chart.getDescription().setEnabled(false);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // set listeners
        chart.setOnChartValueSelectedListener(this);
        chart.setDrawGridBackground(false);

        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        chart.getLegend().setEnabled(false);

        chart.setData(data);

        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        chart.getAxisRight().setEnabled(false);

        chart.getXAxis().setDrawLabels(true);
        chart.getXAxis().setCenterAxisLabels(true);

        chart.getAxisLeft().setDrawLabels(true);

        chart.setDrawMarkers(false);
        // draw points over time
        chart.animateX(1500);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        int ind = data.getDataSetByIndex(0).getEntryIndex(e);

        MapManage.setSelected(ind);

    }

    public static void setSelected(int ind) {
        Entry e = data.getDataSetByIndex(0).getEntryForIndex(ind);
        chart.highlightValue(e.getX(), e.getY(), 0);
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

}

