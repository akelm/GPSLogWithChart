package com.example.android.gpslog_test;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AppViewModel extends AndroidViewModel {

    public enum States {NOTHING, GPS, HISTORY}

    public class AppState {

        States state;

        public AppState(States s) {
            state = s;
        }

        public States getState() {
            return state;
        }
    }

    List<TypeEntity> types;

    private AppRepository mRepository;

    // this is exposed to MainActivity
    MutableLiveData<AppState> state;
    MutableLiveData<TypeEntity> lastType;
    MutableLiveData<Boolean> gpsOK;
    // used by recyclerview
    private LiveData<List<ExerciseEntity>> mExercises;
    // used by chip
    private MutableLiveData<ExerciseEntity> currentExercise;
    // used by chart, map
    private MutableLiveData<List<TrackEntity>> mTracks;
    private LiveData<List<TrackEntity>> allTracks;
    // used by bottomappbar, chart, map
    private MutableLiveData<TrackEntity> lastTrack;
    private LiveData<TrackEntity> absoluteLastTrack;

    public AppViewModel(Application application) throws ExecutionException, InterruptedException {
        super(application);
        // not redirected
        mRepository = new AppRepository(application);
        types = mRepository.getTypes();
        mExercises = mRepository.getExercises();
        allTracks = mRepository.getAllTracks();
        absoluteLastTrack = mRepository.getLastTrack();
        lastType = new MutableLiveData<TypeEntity>();
        if (types != null && types.size() > 0) {
            lastType.postValue(types.get(0));
        } else{
            lastType.postValue(null);
        }
        currentExercise = new MutableLiveData<ExerciseEntity>();
        currentExercise.postValue(null);
        mTracks = new MutableLiveData<List<TrackEntity>>();;
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
    void setCurrentExercise(ExerciseEntity exer) {

        currentExercise.setValue(exer);

    }

    void setLastTrack(TrackEntity tr) {
        if (state.getValue() == null || state.getValue().getState() == States.GPS) {
            lastTrack.postValue(tr);
        }
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

    // wchodzimy to ze start - GPS
    void collectData() {
        // new exercise
        ExerciseEntity newExer = new ExerciseEntity();
        if (lastType.getValue() != null) {
            newExer.setTypeId(lastType.getValue().getTypeName());
        } else {
            newExer.setTypeId("WALK");
        }
        newExer.setStart(new Date().getTime());
        mRepository.insertExercise(newExer);
        // podpinamy pod ostatnie
        currentExercise.setValue(newExer);
        Log.v("gpslog_test", mExercises.getValue().toString());
        mRepository.startGPS(currentExercise.getValue());
    }

    // wchodzimy razem z wejsciem do historii
    public void stopGps() {
        mRepository.stopGps();
    }

    // jak to sie zmieni, uaktualniamy czas chipie u gory
    MutableLiveData<ExerciseEntity> getCurrentExercise() {
        return currentExercise;
    }

    // jak to sie zmieni, zmieniamy caly wykres i mape
    MutableLiveData<List<TrackEntity>> getTracks() {
        return mTracks;
    }

    // jak to sie zmieni, uaktualniamy czas na bottomappbar
    LiveData<TrackEntity> getLastTrack() {
        return lastTrack;
    }

    // jak to sie zmieni, uaktualniamy czas na bottomappbar
    LiveData<TrackEntity> getAbsoluteLastTrack() {
        return absoluteLastTrack;
    }

    // jak to sie zmieni, zaczynamy uaktualniac lastTrack
    public LiveData<Boolean> getGpsOk() {
        return gpsOK;
    }

    public LiveData<List<TrackEntity>> getAllTracks() {
        return allTracks;
    }

    void deleteExercise(ExerciseEntity exer) {
        mRepository.deleteExercise(exer);
    }

    void doNothing() {
        lastTrack.postValue(null);
        currentExercise.postValue(null);
        mTracks.postValue(null);
    }

    void setLastType(int ind) {
        lastType.setValue(types.get(ind % types.size()));
    }

    int getTypeInd(TypeEntity type) {
        if (type!=null) {
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

    public void matchCurrExerWithLastType() {
        if (currentExercise.getValue()!=null && lastType.getValue() !=null) {
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

}