package com.example.android.gpslog_test;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

//
//import java.util.ArrayList;
//
@Entity(tableName = "exercises",
        foreignKeys = @ForeignKey(entity = TypeEntity.class,
                parentColumns = "type",
                childColumns = "typeId",
                onDelete = CASCADE),
        indices = {@Index("typeId")}
)

public class ExerciseEntity {

//    @PrimaryKey(autoGenerate = true)
//    @ColumnInfo(name = "id")
//    private Integer id;
//
//    @NonNull
//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(@NonNull Integer i) {
//        id = i;
//    }

    //    public ExerciseEntity(@NonNull Integer ti, @NonNull Long s, @NonNull Double dis, @NonNull Long dur) {
//        typeId=ti;
//        start=s;
//        distance=dis;
//        duration=dur;
//    }
    public ExerciseEntity() {
    }


    public ExerciseEntity(@NonNull String ti, @NonNull Long s) {
        typeId = ti;
        start = s;
    }

    @NonNull
    @ColumnInfo(name = "typeId")
    String typeId = "";

    @NonNull
    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(@NonNull String i) {
        typeId = i;
    }


    // start in milliseconds since 1970
    @PrimaryKey
    @ColumnInfo(name = "start")
    Long start = -1L;


    @NonNull
    public Long getStart() {
        return start;
    }

    public void setStart(@NonNull Long i) {
        start = i;
    }

    //    @NonNull
//    @ColumnInfo(name = "distance")
//    Double distance=-1d;
//
//    @NonNull
//    public Double getDistance() {
//        return distance;
//    }
//
//    public void setDistance(@NonNull Double i) {
//        distance = i;
//    }
//
//    @NonNull
//    @ColumnInfo(name = "duration")
//    Long duration = -1L;
//
//    @NonNull
//    public Long getDuration() {
//        return duration;
//    }
//
//    public void setDuration(@NonNull Long i) {
//        duration = i;
//    }

}
