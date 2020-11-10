package com.certifyglobal.fcm;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.certifyglobal.authenticator.SplashActivity;

public class MediaPlayingService extends Service {
    private MediaPlayer mp;
    public static final String URI_BASE = MediaPlayingService.class.getName() + ".";
    public static final String ACTION_ACCEPT = URI_BASE + "ACTION_ACCEPT";
    public static final String DISMISSING_ACTION = URI_BASE + "DISMISSING_ACTION";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {


        String action = intent.getAction();
        if (ACTION_ACCEPT.equals(action)) {
            accept();
        } else if (DISMISSING_ACTION.equals(action)) {
            snooze();
        }
        return START_NOT_STICKY;
    }


    private void accept() {
        Toast.makeText(this, "accept", Toast.LENGTH_SHORT).show();
        Intent intent=new Intent(this, SplashActivity.class);
        startActivity(intent);

    }

    public void snooze() {
        Toast.makeText(this, "snooze", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mp.stop();
    }
}