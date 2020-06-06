package com.example.android.GPSLogWithChart;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "types")
public class TypeEntity {


    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "type")
    private String typeName = "";


    public TypeEntity() {
    }

    public TypeEntity(@NonNull String s) {
        typeName = s;
    }


    @NonNull
    public String getTypeName() {
        return this.typeName;
    }

    public void setTypeName(@NonNull String s) {
        this.typeName = s;
    }


}
