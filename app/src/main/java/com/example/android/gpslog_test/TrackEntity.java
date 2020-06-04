package com.example.android.gpslog_test;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.github.mikephil.charting.data.Entry;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "tracks",
        foreignKeys = @ForeignKey(entity = ExerciseEntity.class,
                parentColumns = "start",
                childColumns = "exerId",
                onDelete = CASCADE),
        indices = {@Index("exerId")})
public class TrackEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Integer id;

    @NonNull
    public Integer getId() {
        return id;
    }

    public void setId(@NonNull Integer i) {
        id = i;
    }

    public TrackEntity() {

    }

    public TrackEntity(@NonNull  Long ei,@NonNull  Long t,@NonNull  Double lo,@NonNull  Double la,@NonNull  Double al,@NonNull  Float v) {
        exerId = ei;
        time = t;
        lon = lo;
        lat = la;
        alt = al;
        vel = v;
    }

    @ColumnInfo(name = "exerId")
    Long exerId;

    @NonNull
    public Long getExerId() {
        return exerId;
    }

    public void setExerId(@NonNull Long i) {
        exerId = i;
    }

    @NonNull
    @ColumnInfo(name = "time")
    Long time = 0L;

    @NonNull
    public Long getTime() {
        return time;
    }

    public void setTime(@NonNull Long i) {
        time = i;
    }

    @NonNull
    @ColumnInfo(name = "lon")
    Double lon = 0d;

    @NonNull
    public Double getLon() {
        return lon;
    }

    public void setLon(@NonNull Double i) {
        lon = i;
    }


    @NonNull
    @ColumnInfo(name = "lat")
    Double lat = 0d;

    @NonNull
    public Double getLat() {
        return lat;
    }

    public void setLat(@NonNull Double i) {
        lat = i;
    }


    @NonNull
    @ColumnInfo(name = "alt")
    Double alt = 0d;

    @NonNull
    public Double getAlt() {
        return alt;
    }

    public void setAlt(@NonNull Double i) {
        alt = i;
    }


    @NonNull
    @ColumnInfo(name = "vel")
    Float vel = 0f;

    @NonNull
    public Float getVel() {
        return vel;
    }

    public void setVel(@NonNull Float i) {
        vel = i;
    }
}
