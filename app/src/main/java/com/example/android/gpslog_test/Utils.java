package com.example.android.gpslog_test;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Utils {
    static boolean requestSinglePermission(Activity activity, String permission) {
        if (ContextCompat.checkSelfPermission(activity,
                permission) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(activity,
                    new String[]{permission},
                    0);


            // check again the response
            return ContextCompat.checkSelfPermission(activity,
                    permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }


    }

    static boolean requestMultiplePermissions(Activity activity, String[] permissions) {
        boolean output = true;
        for (String perm : permissions) {
            output = output && requestSinglePermission(activity, perm);
        }
        return output;
    }


}
