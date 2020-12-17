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
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import io.reactivex.disposables.CompositeDisposable;

public class ApplicationWrapper extends Application {
    public static Context context;
    public static Context mcontext;
    CompositeDisposable disposables = new CompositeDisposable();
    public static DBIFaceAdapter mdbIFaceAdapter;
    public static DBCompanyAdapter mdbCompanyAdapter;
    private static DBAdapter mDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        mDatabase = new DBAdapter(this);
        initAppCenter();

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
    private void initAppCenter() {
        AppCenter.start(this, "fb0bbd5c-7f29-4969-9361-dbb7d52f2415",
                Analytics.class, Crashes.class);
        Crashes.setEnabled(true);
    }

}
