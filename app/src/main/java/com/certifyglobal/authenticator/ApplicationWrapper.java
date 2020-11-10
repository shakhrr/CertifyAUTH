package com.certifyglobal.authenticator;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.certifyglobal.database.DBAdapter;
import com.certifyglobal.database.DBCompanyAdapter;
import com.certifyglobal.database.DBIFaceAdapter;
import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.PreferencesKeys;
import com.certifyglobal.utils.Utils;
import com.crashlytics.android.Crashlytics;


import io.fabric.sdk.android.Fabric;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.internal.Util;

public class ApplicationWrapper extends Application {
    public static Context context;
    CompositeDisposable disposables = new CompositeDisposable();
    public static DBIFaceAdapter mdbIFaceAdapter;
    public static DBCompanyAdapter mdbCompanyAdapter;
    private static DBAdapter mDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        context = getApplicationContext();
      //  PalmSDK.context = context;
        mDatabase = new DBAdapter(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel("1", "authx", importance);
            mChannel.setDescription("Authx");
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public void onTerminate() {
        if (null != disposables) {
            disposables.clear();
        }
        super.onTerminate();
    }

    public synchronized static DBAdapter getWritableDatabase() {
        if (mDatabase == null) mDatabase = new DBAdapter(context);
        return mDatabase;
    }

    public static DBIFaceAdapter getDBIFaceAdapter() {
        if (mdbIFaceAdapter == null)
            mdbIFaceAdapter = new DBIFaceAdapter();
        return mdbIFaceAdapter;
    }

    public static DBCompanyAdapter getMdbCompanyAdapter() {
        if (mdbCompanyAdapter == null)
            mdbCompanyAdapter = new DBCompanyAdapter();
        return mdbCompanyAdapter;
    }
    public static String BaseUrl (String domain,String endPoint){
        return String.format("https://%s.authx.com/%s",domain,endPoint);
    }
}
