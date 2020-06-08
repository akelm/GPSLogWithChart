package com.example.android.GPSLogWithChart;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;


public class LocManage {

    static LocationManager locationManager = null;
    static LocationListener locationListener = null;
    static LocManage instance = null;
    static long start = 0;
    static long duration = 0;

    static MutableLiveData<Boolean> gpsOk;
    static Application application;
    private static AppRepository mRepository;

    // singleton
    private LocManage() {
    }

    public static MutableLiveData<Boolean> getGpsOk() {
        return gpsOk;
    }

    public static void setup(AppRepository appRepo, Application app) {
        gpsOk = new MutableLiveData<Boolean>();
        gpsOk.postValue(false);
        mRepository = appRepo;
        application = app;
        if (instance == null) {
            instance = new LocManage();
        }
    }

    public static LocManage getInstance() {
        if (instance == null) {
            instance = new LocManage();
        }
        return instance;
    }

    public static void stopGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        gpsOk.postValue(false);

    }

    static void startGPS(ExerciseEntity exer) {

        locationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);

        Log.v("GPSLogWithChart", "startGPS");
        locationListener = new MyLocationListener(exer);
        Log.v("GPSLogWithChart", "after log lis");

        Log.v("GPSLogWithChart", "loc man");
        if (ActivityCompat.checkSelfPermission(application,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);

    }

    /*----------Listener class to get coordinates ------------- */
    public static class MyLocationListener implements LocationListener {
        ExerciseEntity exer;

        public MyLocationListener(ExerciseEntity exerciseEntity) {
            exer = exerciseEntity;
            gpsOk.postValue(false);

        }

        @Override
        public void onLocationChanged(Location loc) {

            double longitude = loc.getLongitude();
            double latitude = loc.getLatitude();
            // meters/second from loc.getSpeed()
            // x 3600/1000=3.6 to get km/h
            float velocity = loc.getSpeed() * 3.6f;
            Log.v("GPSLogWithChart", "loc.hasSpeed() " + loc.hasSpeed());
            Log.v("GPSLogWithChart", "loc.getSpeed() " + loc.getSpeed());

            double altitude = loc.getAltitude();
            long time = loc.getElapsedRealtimeNanos();

            if (start == 0) {
                gpsOk.postValue(true);
                start = time;
            }
            duration = time - start;

            TrackEntity newTrack = new TrackEntity(exer.getStart(), duration, longitude, latitude, altitude, velocity);
            mRepository.insertTrack(newTrack);

        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

}
