package com.certifyglobal.authenticator;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.certifyglobal.async_task.AsyncGetCheckRoot;
import com.certifyglobal.fcm.FirebaseBackgroundService;
import com.certifyglobal.fcm.OnClearFromRecentService;
import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.PreferencesKeys;
import com.certifyglobal.utils.RSAKeypair;
import com.certifyglobal.utils.Utils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import org.json.JSONObject;

import java.util.Date;
import java.util.concurrent.Executor;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;

public class SplashActivity extends AppCompatActivity {
    private static String TAG = "SplashActivity - ";
    Intent intent;
    private static NotificationChannel mChannel;
    Uri data;
    private static Executor executor;
    private static BiometricPrompt biometricPrompt;
    private static BiometricPrompt.PromptInfo promptInfo;
    private boolean linkOpen=false;
    private String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            Intent intent = getIntent();
            if (intent.getData() != null) {
                data = intent.getData();
                linkOpen=true;
                Logger.debug("deep link", data.toString());
            }

            AppCenter.setEnabled(true);

            startService(new Intent(SplashActivity.this, OnClearFromRecentService.class));

            setContentView(R.layout.activity_splash);
            //   ApplicationWrapper.getDBIFaceAdapter().deleteFace();
            //Device checking root or not


            new AsyncGetCheckRoot(null, "").execute();
            // once UUid is available going to HMac validations.
            if (!Utils.readFromPreferences(this, PreferencesKeys.deviceUUid, "").isEmpty())
                Utils.getHMacSecretKey(this);
            //  publicKey is not available getting and shaving in shred preferences.
            if (Utils.readFromPreferences(this, PreferencesKeys.publicKey, "").isEmpty())
                RSAKeypair.getRSAPublic(this);
            Intent intent_o = getIntent();

            // push notification. App is close that time it will call
            if (intent_o.getExtras() != null && intent_o.getStringExtra("pushType") != null && intent.getData()==null) {

                Intent notificationIntent = new Intent(this, PushNotificationActivity.class);
                if (intent_o.getStringExtra("encValue") != null) {
                    String decryptedStr = RSAKeypair.DecryptRSA(Utils.readFromPreferences(this, PreferencesKeys.privateKey, ""), intent_o.getStringExtra("encValue"));
                    if (!decryptedStr.isEmpty()) {
                        JSONObject decryOBJ = new JSONObject(decryptedStr);
                        notificationIntent.putExtra("requestId", decryOBJ.getString("requestId"));
                        notificationIntent.putExtra("user", decryOBJ.getString("user"));
                        notificationIntent.putExtra("userId", decryOBJ.isNull("userId") ? "" : decryOBJ.getString("userId"));
                        notificationIntent.putExtra("timeStamp", decryOBJ.isNull("TimeStamp") ? "" : decryOBJ.getString("TimeStamp"));
                    }
                }
                notificationIntent.putExtra("hostName", intent_o.getStringExtra("HostName") == null ? "" : intent_o.getStringExtra("HostName"));
                notificationIntent.putExtra("pushType", intent_o.getStringExtra("pushType"));
                notificationIntent.putExtra("country", intent_o.getStringExtra("Country") == null ? "" : intent_o.getStringExtra("Country"));
                notificationIntent.putExtra("city", intent_o.getStringExtra("City") == null ? "" : intent_o.getStringExtra("City"));
                notificationIntent.putExtra("state", intent_o.getStringExtra("State") == null ? "" : intent_o.getStringExtra("State"));
                notificationIntent.putExtra("userType", intent_o.getStringExtra("UserType") == null ? "" : intent_o.getStringExtra("UserType"));
                notificationIntent.putExtra("companyName", intent_o.getStringExtra("CompanyName") == null ? "" : intent_o.getStringExtra("CompanyName"));
                notificationIntent.putExtra("liveliness", intent_o.getStringExtra("Liveliness") == null ? "" : intent_o.getStringExtra("Liveliness"));
                notificationIntent.putExtra("applicationName", intent_o.getStringExtra("ApplicationName") == null ? "" : intent_o.getStringExtra("ApplicationName"));
                notificationIntent.putExtra("machineName", intent_o.getStringExtra("MachineName") == null ? "" : intent_o.getStringExtra("MachineName"));
                notificationIntent.putExtra("ip", intent_o.getStringExtra("IP") == null ? "" : intent_o.getStringExtra("IP"));
                notificationIntent.putExtra("timeOut", intent_o.getStringExtra("TimeOut") == null ? "" : intent_o.getStringExtra("TimeOut"));
                notificationIntent.putExtra("correlationId", intent_o.getStringExtra("CorrelationId") == null ? "" : intent_o.getStringExtra("CorrelationId"));
                startActivity(notificationIntent);
                if (Utils.readFromPreferences(SplashActivity.this, PreferencesKeys.appLock, false) && !Utils.readFromPreferences(SplashActivity.this, PreferencesKeys.appLockpref, false)) {
                    Utils.biometricLogin(SplashActivity.this,"splash");
                }else{
                    finish();
                }
            } else if (Utils.readFromPreferences(SplashActivity.this, PreferencesKeys.isLogin, false) && linkOpen==false) {
                startActivity(new Intent(this, UserActivity.class));
                if (Utils.readFromPreferences(this, PreferencesKeys.deviceUUid, "").isEmpty())
                    Utils.getDeviceUUid(this);
                if (Utils.readFromPreferences(this, PreferencesKeys.mobileNumber, "").isEmpty())
                    Utils.getNumberVersion(this);
                if (Utils.readFromPreferences(SplashActivity.this, PreferencesKeys.appLock, false) && !Utils.readFromPreferences(SplashActivity.this, PreferencesKeys.appLockpref, false)) {
                    Utils.biometricLogin(SplashActivity.this,"splash");
                }else{
                    finish();
                }
            } else {
                if(linkOpen){
                    String urlSplit[] = data.toString().split("reactivate/");
                    id=urlSplit[1];
                    Intent intentURL = new Intent(SplashActivity.this, QRUrlScanResults.class);
                    intentURL.putExtra("Url", id);
                    startActivity(intentURL);
                    if (Utils.readFromPreferences(SplashActivity.this, PreferencesKeys.appLock, false) && !Utils.readFromPreferences(SplashActivity.this, PreferencesKeys.appLockpref, false)) {
                        Utils.biometricLogin(SplashActivity.this,"spalsh");

                    }else{
                        finish();
                    }
                }else {
                    startActivity(new Intent(this, MainActivity.class));
                    if (Utils.readFromPreferences(SplashActivity.this, PreferencesKeys.appLock, false) && !Utils.readFromPreferences(SplashActivity.this, PreferencesKeys.appLockpref, false)) {
                        Utils.biometricLogin(SplashActivity.this,"splash");
                    }else{
                        finish();
                    }
                }
            }
            //finish();
        } catch (Exception e) {
            Logger.error(TAG + "onCreate(Bundle savedInstanceState)", e.getMessage());
            Logger.debug("deep Splash",e.getMessage());
        }
    }

}
