package com.certifyglobal.async_task;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import java.util.List;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static android.content.Context.ACTIVITY_SERVICE;

public class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Context... params) {
        final Context context = params[0].getApplicationContext();
        return isAppOnForeground(context);
    }

    private boolean isAppOnForeground(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            assert am != null;
            ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
            String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();

            return foregroundTaskPackageName.toLowerCase().equals(context.getPackageName().toLowerCase());
        } else {
            ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
            ActivityManager.getMyMemoryState(appProcessInfo);
            if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE) {
                return true;
            }

            KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            // App is foreground, but screen is locked, so show notification
            return km != null && km.inKeyguardRestrictedInputMode();
        }
//      ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
//    if (activityManager == null)
//      return false;
//    List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
//    if (appProcesses == null) {
//      return false;
//    }
//    final String packageName = context.getPackageName();
//    for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
//      if (appProcess.importance == IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
//        return true;
//      }
//    }
//    return false;

    }
}