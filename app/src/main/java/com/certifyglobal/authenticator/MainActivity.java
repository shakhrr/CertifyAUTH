package com.certifyglobal.authenticator;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.certifyglobal.callback.JSONObjectCallback;
import com.certifyglobal.fcm.OnClearFromRecentService;
import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.PreferencesKeys;
import com.certifyglobal.utils.Utils;


import org.json.JSONObject;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements JSONObjectCallback {
    private static final String TAG = "MainActivity - ";
    CardView cardView_qr;
    ImageView imageAdd;
    ImageView imageMenu;
    private String versionCode;
    private PopupMenu mPopupMenu;
    private static Executor executor;
    private static BiometricPrompt biometricPrompt;
    private static BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            Logger.debug("mainnnnnnnnnnnnnnnnnnn","shkah");
            //noto2();
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            cardView_qr = findViewById(R.id.cardView_qr);
            imageAdd = findViewById(R.id.image_add);
            imageMenu = findViewById(R.id.image_menu);

            mPopupMenu = new PopupMenu(this, imageMenu);
            mPopupMenu.getMenuInflater().inflate(R.menu.menu, mPopupMenu.getMenu());
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            Utils.PermissionRequest(MainActivity.this, Utils.permission.all);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    Utils.keyValidations(this, this);
                } catch (Exception ignore) {

                }
            }
            cardView_qr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scanQR();
                }
            });
            imageAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scanQR();
                }
            });
            imageMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  //  mPopupMenu.show();
                    startActivity(new Intent(MainActivity.this, Settings.class));
                }
            });
            mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_settings:
                            startActivity(new Intent(MainActivity.this, Settings.class));
                            break;
                        case R.id.menu_about:
                            startActivity(new Intent(MainActivity.this, Passcode.class));
                            break;
                        case R.id.menu_help:
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://authx.com"));
                            startActivity(browserIntent);
                            break;
                    }
                    return false;
                }
            });


        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }

    private void scanQR() {
        try {
            if (Utils.isConnectingToInternet(this)) {
                Intent livePreIntent = new Intent(this, ScanActivity.class);
                livePreIntent.putExtra("type", "Barcode");
                startActivity(livePreIntent);
             //   finish();
            } else
                Logger.toast(MainActivity.this, getResources().getString(R.string.network_error));

        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }


    @Override
    public void onJSONObjectListener(JSONObject report, String status,JSONObject req) {
        try {
            if (report != null) {
                Utils.saveToPreferences(MainActivity.this, PreferencesKeys.osVersion, report.isNull("MobileOSVersion") ? "9" : report.getString("MobileOSVersion"));
                if (!report.isNull("OsVersion")) {
                    String apiVersion = report.getString("OsVersion");
                    if (Utils.compareTowString(versionCode, apiVersion)) {
                        boolean isCritical = report.getBoolean("IsCritical");
                        if (isCritical || Utils.compareTowString(Utils.readFromPreferences(MainActivity.this, PreferencesKeys.appVersion, "2.4"), apiVersion))
                            showAlert(isCritical, apiVersion);
                        Utils.saveToPreferences(MainActivity.this, PreferencesKeys.appVersion, apiVersion);
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    private void showAlert(boolean isCritical, String version) {
        try {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(getResources().getString(R.string.new_version));
            alertDialog.setCancelable(false);
            alertDialog.setMessage(String.format("Version %s is available on the PlayStore.", version));
            alertDialog.setPositiveButton(getResources().getString(R.string.update), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    String packageName = getPackageName();

                    String url;
                    try {
                        getPackageManager().getPackageInfo(packageName, 0);
                        url = "market://details?id=" + packageName;
                    } catch (Exception ignored) {
                        url = "http://play.google.com/store/apps/details?id=" + packageName;
                    }
                    try {
                        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        //noinspection deprecation
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        startActivity(intent);
                    } catch (Exception ignored) {
                    }
                }
            });
            if (!isCritical)
                alertDialog.setNegativeButton(getResources().getString(R.string.later), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

            AlertDialog builder = alertDialog.create();
            builder.show();
        } catch (Exception e) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
       if (Utils.readFromPreferences(this, PreferencesKeys.updatePermissions, false)) {
          /*  if (Utils.compareTowString(Utils.readFromPreferences(MainActivity.this, PreferencesKeys.whatNewVersion, "2.4"), versionCode)) {
                Utils.saveToPreferences(MainActivity.this, PreferencesKeys.whatNewVersion, versionCode);
                Utils.showAlertWhatsNew(this);
            } else*/
                Utils.getOSDetails(this,this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
          /*  Utils.saveToPreferences(this, PreferencesKeys.updatePermissions, true);
            if (Utils.compareTowString(Utils.readFromPreferences(MainActivity.this, PreferencesKeys.whatNewVersion, "2.4"), versionCode)) {
                Utils.saveToPreferences(MainActivity.this, PreferencesKeys.whatNewVersion, versionCode);
                Utils.showAlertWhatsNew(this);
            } else*/
                Utils.getOSDetails(this,this);
        }
    }

    public void noto2() // paste in activity
    {
        try {
         NotificationManager   notifManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
         NotificationChannel mChannel=null;

            if (notifManager == null) {
                notifManager = (NotificationManager)getSystemService
                        (Context.NOTIFICATION_SERVICE);
            }
            if (mChannel == null) {
                mChannel = new NotificationChannel
                        ("1", "qa", NotificationManager.IMPORTANCE_HIGH);
                mChannel.setDescription("welcome");
                mChannel.enableVibration(true);
                notifManager.createNotificationChannel(mChannel);
            }
        } catch (Exception e) {
            Logger.error(TAG + "createNotificationChannel(String title, String channel, String description)", e.getMessage());
        }
        NotificationCompat.Builder notif;
        NotificationManager nm;
        notif = new NotificationCompat.Builder(this, "1");
        notif.setSmallIcon(R.drawable.ic_defalt_icon);
        notif.setContentTitle("");
        Uri path = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notif.setSound(path);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent yesReceive = new Intent();
        yesReceive.setAction("com.certifyglobal.fcm.YES");
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(this, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.addAction(R.drawable.ic_defalt_icon, "Yes", pendingIntentYes);


        Intent yesReceive2 = new Intent();
        yesReceive2.setAction("com.certifyglobal.fcm.NO");
        PendingIntent pendingIntentYes2 = PendingIntent.getBroadcast(this, 12345, yesReceive2, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.addAction(R.drawable.ic_defalt_icon, "No", pendingIntentYes2);



        nm.notify(10, notif.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.debug("deep destroy","Mainactivity");
     //   Utils.saveToPreferences(MainActivity.this, PreferencesKeys.appLockpref,false);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.debug("deep stop","Mainactivity");

        //    Utils.saveToPreferences(MainActivity.this, PreferencesKeys.appLockpref,false);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Logger.debug("deep restart","Mainactivity");
       // Utils.biometricLogin(MainActivity.this,"main");
      //  biometricLogin();
//        finish();
//       startActivity(new Intent(this,SplashActivity.class));


    }

    /*public void biometricLogin() {
        try {

            executor = ContextCompat.getMainExecutor(this);
            biometricPrompt = new BiometricPrompt(MainActivity.this,
                    executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode,
                                                  @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    if(errorCode==10){
                        Utils.saveToPreferences(MainActivity.this, PreferencesKeys.appLockpref,false);
                    }
                    Utils.closeApp(MainActivity.this);
                    finish();
                }

                @Override
                public void onAuthenticationSucceeded(
                        @NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Utils.saveToPreferences(MainActivity.this,PreferencesKeys.appLockpref,true);
                    finish();
                    Logger.debug("deep SplashActivity","onAuthenticationSucceeded"+result);

                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Utils.closeApp(MainActivity.this);
                    finish();
                }
            });

            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Unlock AuthX")
                    .setSubtitle("Confirm your screen lock pattern, Password, Face or Fingerprint")
                    .setNegativeButtonText("")
                    .setDeviceCredentialAllowed(true)
                    .build();
            biometricPrompt.authenticate(promptInfo);
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }


    }*/

    @Override
    public void onBackPressed() {
        finish();
    }
}
