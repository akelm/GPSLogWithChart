package com.example.android.GPSLogWithChart;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AppDatabaseDao {
    // reading
    @Query("SELECT * from tracks WHERE exerId = :exerId ORDER BY id ASC")
    List<TrackEntity> getTracks(long exerId);

    @Query("SELECT * from tracks ORDER BY id DESC LIMIT 1")
    LiveData<TrackEntity> getLastTrack();

    @Query("SELECT * from tracks ORDER BY id DESC")
    LiveData<List<TrackEntity>> getAllTracks();

    @Query("SELECT * from tracks " +
            "WHERE exerId = :exerId" +
            " ORDER BY time DESC LIMIT 1")
    TrackEntity getExerLastTrack(Long exerId);

    @Query("SELECT * from exercises ORDER BY start DESC")
    LiveData<List<ExerciseEntity>> getExercises();

    @Query("SELECT * from types ORDER BY type DESC")
    List<TypeEntity> getTypes();

    // removing
    @Delete
    void deleteExercise(ExerciseEntity exerciseEntity);

    // insertions
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertType(TypeEntity typeEntity);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertExercise(ExerciseEntity exerciseEntity);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTrack(TrackEntity trackEntity);

    //update
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateExercise(ExerciseEntity exerciseEntity);

}
