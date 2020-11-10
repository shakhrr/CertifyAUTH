package com.certifyglobal.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        String action = intent.getAction();
        if ("com.certifyglobal.fcm.YES".equals(action)) {
            Toast.makeText(context, "YES CALLED", Toast.LENGTH_SHORT).show();
        } else if ("com.certifyglobal.fcm.NO".equals(action)) {
            Toast.makeText(context, "STOP CALLED", Toast.LENGTH_SHORT).show();
        }
    }
}