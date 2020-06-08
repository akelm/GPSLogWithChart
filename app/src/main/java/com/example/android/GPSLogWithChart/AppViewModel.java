package com.example.android.GPSLogWithChart;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class AppViewModel extends AndroidViewModel {

    List<TypeEntity> types;
    // this is exposed to MainActivity
    MutableLiveData<AppState> state;
    MutableLiveData<TypeEntity> lastType;
    MutableLiveData<Boolean> gpsOK;
    private AppRepository mRepository;
    // used by recyclerview
    private LiveData<List<ExerciseEntity>> mExercises;
    // used by chip
    private MutableLiveData<ExerciseEntity> currentExercise;
    // used by chart, map
    private MutableLiveData<List<TrackEntity>> mTracks;
    // used by bottomappbar, chart, map
    private MutableLiveData<TrackEntity> lastTrack;
    private LiveData<TrackEntity> absoluteLastTrack;

    public AppViewModel(Application application) throws ExecutionException, InterruptedException {
        super(application);
        // not redirected
        mRepository = new AppRepository(application);
        types = mRepository.getTypes();
        mExercises = mRepository.getExercises();
        absoluteLastTrack = mRepository.getLastTrack();
        lastType = new MutableLiveData<TypeEntity>();
        if (types != null && types.size() > 0) {
            lastType.postValue(types.get(0));
        } else {
            lastType.postValue(null);
        }
        currentExercise = new MutableLiveData<ExerciseEntity>();
        currentExercise.postValue(null);
        mTracks = new MutableLiveData<List<TrackEntity>>();
        mTracks.postValue(null);
        lastTrack = new MutableLiveData<>();
        lastTrack.postValue(null);
        gpsOK = mRepository.getGpsOk();

        // initial vals
        state = new MutableLiveData<AppState>();
        state.postValue(new AppState(States.NOTHING));

    }

    LiveData<List<ExerciseEntity>> getExercises() {
        return mExercises;
    }

    // recyclerview - history
    void loadHistoryTracks() throws ExecutionException, InterruptedException {
        if (currentExercise.getValue() != null) {
            mTracks.postValue(mRepository.getTracks(currentExercise.getValue()));
            lastTrack.postValue(mRepository.getExerLastTrack(currentExercise.getValue()));
            lastType.postValue(new TypeEntity(currentExercise.getValue().getTypeId()));

        }
    }

    MutableLiveData<TrackEntity> getExerLastTrack(ExerciseEntity exer) throws ExecutionException, InterruptedException {
        lastTrack.postValue(mRepository.getExerLastTrack(exer));
        return lastTrack;
    }

    // called from change to GPS after play button
    void collectData() {
        // new exercise
        if (isAppStateEqual(States.GPS)) {
            ExerciseEntity newExer = new ExerciseEntity();
            if (lastType.getValue() != null) {
                newExer.setTypeId(lastType.getValue().getTypeName());
            } else {
                newExer.setTypeId("WALK");
            }
            newExer.setStart(new Date().getTime());
            mRepository.insertExercise(newExer);
            currentExercise.setValue(newExer);
            Log.v("GPSLogWithChart", Objects.requireNonNull(mExercises.getValue()).toString());
            mRepository.startGPS(currentExercise.getValue());
        }
    }

    // upon entering HISTORY
    public void stopGps() {
        mRepository.stopGps();
    }

    // update of bottomappbar duration time when this updates
    MutableLiveData<ExerciseEntity> getCurrentExercise() {
        return currentExercise;
    }

    // recyclerview - history
    void setCurrentExercise(ExerciseEntity exer) {

        currentExercise.setValue(exer);

    }

    // update of map and chart
    MutableLiveData<List<TrackEntity>> getTracks() {
        return mTracks;
    }

    // update of bottomappbar duration time when this updates
    LiveData<TrackEntity> getLastTrack() {
        return lastTrack;
    }

    void setLastTrack(TrackEntity tr) {
        if (state.getValue() == null || state.getValue().getState() == States.GPS) {
            lastTrack.postValue(tr);
        }
    }

    // update of bottomappbar duration time when this updates
    LiveData<TrackEntity> getAbsoluteLastTrack() {
        return absoluteLastTrack;
    }

    // when this changes we start updating last track
    public LiveData<Boolean> getGpsOk() {
        return gpsOK;
    }

    void deleteExercise(ExerciseEntity exer) {
        mRepository.deleteExercise(exer);
    }

    void doNothing() {
        lastTrack.postValue(null);
        currentExercise.postValue(null);
        mTracks.postValue(null);
    }

    int getTypeInd(TypeEntity type) {
        if (type != null) {
            for (int i = 0; i < types.size(); i++) {
                if (types.get(i).getTypeName().equals(type.getTypeName())) {
                    return i;
                }
            }
        }
        return 0;
    }

    int getLastTypeInd() {
        if (types != null && lastType.getValue() != null) {
            return getTypeInd(lastType.getValue());
        } else {
            return 0;
        }
    }

    MutableLiveData<TypeEntity> getLastType() {
        return lastType;
    }

    void setLastType(int ind) {
        lastType.setValue(types.get(ind % types.size()));
    }

    public void matchCurrExerWithLastType() {
        if (currentExercise.getValue() != null && lastType.getValue() != null) {
            if (!currentExercise.getValue().getTypeId().equals(lastType.getValue().getTypeName())) {
                ExerciseEntity exer = currentExercise.getValue();
                exer.setTypeId(lastType.getValue().getTypeName());
                mRepository.updateExercise(exer);
            }
        }
    }

    public MutableLiveData<AppState> getState() {
        return state;
    }

    public void setState(States s) {
        state.postValue(new AppState(s));
    }

    public void forceState(States s) {
        state.setValue(new AppState(s));
    }

    public boolean isAppStateEqual(AppState appState) {
        return state.getValue()!=null && state.getValue().isEqual(appState);
    }

    public boolean isAppStateEqual(States s) {
        return state.getValue()!=null && (state.getValue()).getState() == s;
    }

    public void nextState() {
        if (state.getValue() != null) {
            switch (state.getValue().state) {
                case GPS:
                    state.postValue(new AppState(States.HISTORY));
                    break;
                case HISTORY:
                    state.postValue(new AppState(States.NOTHING));
                    break;
                case NOTHING:
                    state.postValue(new AppState(States.GPS));
                    break;
            }
        } else {
            state.postValue(new AppState(States.NOTHING));
        }
    }

    public enum States {NOTHING, GPS, HISTORY}

    public static class AppState {

        States state;

        public AppState(States s) {
            state = s;
        }

        public States getState() {
            return state;
        }

        public boolean isEqual(AppState s) {
            if (s != null) {
                return state == s.getState();
            } else {
                return false;
            }
        }
    }

}
