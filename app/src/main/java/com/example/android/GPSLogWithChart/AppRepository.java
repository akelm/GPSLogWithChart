package com.example.android.GPSLogWithChart;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AppRepository {

    MutableLiveData<Boolean> gpsOk;
    private AppDatabaseDao appDatabaseDao;
    private LiveData<List<ExerciseEntity>> mExercises;
    private LiveData<List<TrackEntity>> allTracks;
    private LiveData<TrackEntity> lastTrack;

    AppRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        appDatabaseDao = db.appDatabaseDao();
        mExercises = appDatabaseDao.getExercises();
        lastTrack = appDatabaseDao.getLastTrack();
        LocManage.setup(this, application);
        gpsOk = LocManage.getGpsOk();
        allTracks = appDatabaseDao.getAllTracks();

    }

    LiveData<List<ExerciseEntity>> getExercises() {
        return mExercises;
    }


    LiveData<TrackEntity> getLastTrack() {
        return lastTrack;
    }

    TrackEntity getExerLastTrack(ExerciseEntity exer) throws ExecutionException, InterruptedException {
        if (exer != null) {
            Callable<TrackEntity> callable = () -> appDatabaseDao.getExerLastTrack(exer.getStart());
            Future<TrackEntity> futureList = AppDatabase.databaseWriteExecutor.submit(callable);
            return futureList.get();
        } else {
            return null;
        }
    }

    LiveData<List<TrackEntity>> getAllTracks() {
        return allTracks;
    }

    List<TrackEntity> getTracks(ExerciseEntity exer) throws ExecutionException, InterruptedException {
        if (exer != null) {
            Callable<List<TrackEntity>> callable = () -> appDatabaseDao.getTracks(exer.getStart());
            Future<List<TrackEntity>> futureList = AppDatabase.databaseWriteExecutor.submit(callable);
            return futureList.get();
        } else {
            return null;
        }
    }


    List<TypeEntity> getTypes() throws ExecutionException, InterruptedException {
        Callable<List<TypeEntity>> callable = () -> appDatabaseDao.getTypes();
        Future<List<TypeEntity>> futureList = AppDatabase.databaseWriteExecutor.submit(callable);
        return futureList.get();
    }

    void insertExercise(final ExerciseEntity exer) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            appDatabaseDao.insertExercise(exer);
        });
    }

    void insertTrack(final TrackEntity track) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            appDatabaseDao.insertTrack(track);
        });
    }

    void deleteExercise(final ExerciseEntity exer) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            appDatabaseDao.deleteExercise(exer);
        });
    }

    void updateExercise(final ExerciseEntity exer) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            appDatabaseDao.updateExercise(exer);
        });
    }

    void startGPS(ExerciseEntity exer) {
        LocManage.startGPS(exer);
    }

    MutableLiveData<Boolean> getGpsOk() {
        return gpsOk;
    }

    public void stopGps() {
        LocManage.stopGPS();
    }
}
