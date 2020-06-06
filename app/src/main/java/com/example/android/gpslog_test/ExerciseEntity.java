package com.example.android.gpslog_test;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "exercises",
        foreignKeys = @ForeignKey(entity = TypeEntity.class,
                parentColumns = "type",
                childColumns = "typeId",
                onDelete = CASCADE),
        indices = {@Index("typeId")}
)

public class ExerciseEntity {

    @NonNull
    @ColumnInfo(name = "typeId")
    String typeId = "";
    // start in milliseconds since 1970
    @PrimaryKey
    @ColumnInfo(name = "start")
    Long start = -1L;

    public ExerciseEntity() {
    }

    public ExerciseEntity(@NonNull String ti, @NonNull Long s) {
        typeId = ti;
        start = s;
    }

    @NonNull
    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(@NonNull String i) {
        typeId = i;
    }

    @NonNull
    public Long getStart() {
        return start;
    }

    public void setStart(@NonNull Long i) {
        start = i;
    }


}
