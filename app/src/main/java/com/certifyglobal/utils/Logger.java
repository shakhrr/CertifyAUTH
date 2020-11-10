package com.certifyglobal.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.microsoft.appcenter.analytics.Analytics;

public class Logger {
    public static void debug(String classname, String message) {
        Log.d(classname, "" + message);
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context, message + "", Toast.LENGTH_SHORT).show();
    }

    public static void error(String classname, String message) {
        if (EndPoints.deployment == EndPoints.Mode.Local) {
            if (message == null) message = "null";
            Log.e(classname,message);
        } else  Analytics.trackEvent("Error:"+message);

    }
}
