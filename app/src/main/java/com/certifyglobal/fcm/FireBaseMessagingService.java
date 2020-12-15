package com.certifyglobal.fcm;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.certifyglobal.async_task.ForegroundCheckTask;
import com.certifyglobal.authenticator.BuildConfig;
import com.certifyglobal.authenticator.PushNotificationActivity;
import com.certifyglobal.authenticator.R;
import com.certifyglobal.authenticator.SplashActivity;
import com.certifyglobal.utils.EndPoints;
import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.PreferencesKeys;
import com.certifyglobal.utils.RSAKeypair;
import com.certifyglobal.utils.Utils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Map;
import java.util.Random;

public class FireBaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FireBaseMessagingService -> ";
    private static NotificationChannel mChannel;
    private static NotificationManager notifManager;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
             String title=remoteMessage.getData().get("Title");
             String body=remoteMessage.getData().get("Body");
                sendNotification(title,
                       body, remoteMessage.getData(), remoteMessage.getNotification().getClickAction());

                Log.d( "deep From: " , remoteMessage.getFrom());
                Log.d( "deep Notification Message Body: " , remoteMessage.getNotification().getBody());
                Log.d( "deep Notification Click Action: " , remoteMessage.getNotification().getClickAction());
                Log.d( "deep dataaaaaaaaa" , remoteMessage.getData().toString());

            }
          /*  if (remoteMessage.getNotification() == null) {
                sendNotification("", "", remoteMessage.getData(), "");
                Logger.debug(TAG, "remoteMessage.getNotification() " + "nullll");
            } else {
                sendNotification(remoteMessage.getNotification().getTitle(),
                        remoteMessage.getNotification().getBody(), remoteMessage.getData(), remoteMessage.getNotification().getClickAction());
                Logger.debug(TAG, " sendNotification(remoteMessage.getNotification().getTitle(),\n" +
                        "                        remoteMessage.getNotification().getBody(), remoteMessage.getData(), remoteMessage.getNotification().getClickAction()); " + remoteMessage.getNotification().getClickAction());
            }*/

        } catch (Exception e) {
            Logger.error(TAG + "onMessageReceived(RemoteMessage remoteMessage)", e.getMessage());
        }
    }



    //This method is only generating push notification
    @SuppressLint("WrongConstant")
    private void sendNotification(String messageTitle, String messageBody, Map<String, String> row,String click_action) {
        try {
            NotificationCompat.Builder builder=null;//open notification
            Intent notificationIntent = new Intent(this, PushNotificationActivity.class);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            notificationIntent.putExtra("messageTitle", messageTitle);
            notificationIntent.putExtra("messageBody", messageBody);

            if (row.get("encValue") != null) {
                String decryptedStr = RSAKeypair.DecryptRSA(Utils.readFromPreferences(FireBaseMessagingService.this, PreferencesKeys.privateKey, ""), row.get("encValue") == null ? "" : row.get("encValue"));
                if (!decryptedStr.isEmpty()) {
                    JSONObject decryOBJ = new JSONObject(decryptedStr);
                    notificationIntent.putExtra("requestId", decryOBJ.getString("requestId"));
                    notificationIntent.putExtra("user", decryOBJ.getString("user"));
                    notificationIntent.putExtra("userId", decryOBJ.isNull("userId") ? "" : decryOBJ.getString("userId"));
                    notificationIntent.putExtra("timeStamp", decryOBJ.isNull("TimeStamp") ? "" : decryOBJ.getString("TimeStamp"));
                    notificationIntent.putExtra("hostName", row.get("HostName") == null ? "" : row.get("HostName"));
                    notificationIntent.putExtra("pushType", row.get("pushType"));
                    notificationIntent.putExtra("country", row.get("Country") == null ? "" : row.get("Country"));
                    notificationIntent.putExtra("city", row.get("City") == null ? "" : row.get("City"));
                    notificationIntent.putExtra("state", row.get("State") == null ? "" : row.get("State"));
                    notificationIntent.putExtra("userType", row.get("UserType") == null ? "" : row.get("UserType"));
                    notificationIntent.putExtra("companyName", row.get("CompanyName") == null ? "" : row.get("CompanyName"));
                    notificationIntent.putExtra("liveliness", row.get("Liveliness") == null ? "" : row.get("Liveliness"));
                    notificationIntent.putExtra("faceSettings", row.get("FaceSettings") == null ? "" : row.get("FaceSettings"));
                    notificationIntent.putExtra("applicationName", row.get("ApplicationName") == null ? "" : row.get("ApplicationName"));
                    notificationIntent.putExtra("machineName", row.get("MachineName") == null ? "" : row.get("MachineName"));
                    notificationIntent.putExtra("ip", row.get("IP")== null ? "" : row.get("IP"));
                    notificationIntent.putExtra("timeOut", row.get("TimeOut")== null ? "" : row.get("TimeOut"));
                    notificationIntent.putExtra("correlationId", row.get("CorrelationId")== null ? "" : row.get("CorrelationId"));
                    notificationIntent.putExtra("Version", row.get("Version")== null ? "" : row.get("Version"));
                    notificationIntent.putExtra("UserVersion", row.get("UserVersion")== null ? "" : row.get("UserVersion"));
                    notificationIntent.putExtra("SettingVersion", row.get("SettingVersion")== null ? "" : row.get("SettingVersion"));
                    Logger.debug("deep ","user version:"+ row.get("UserVersion") +"Setting version:"+ row.get("SettingVersion"));



                    //startActivity(notificationIntent);
                }
            }

            if (new ForegroundCheckTask().execute(this).get()) {
                 startActivity(notificationIntent);
                if(Settings.System.canWrite(getApplicationContext())){
                    Uri path = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.authxnotification);
                    RingtoneManager.setActualDefaultRingtoneUri(
                            getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION,
                            path);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), path);
                    r.play();
                }else {
                    Intent intent1 = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent1.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                    startActivity(intent1);
                }

            } else { //close notification
                Logger.debug("closeeeeeeeeeeeeeeeeeeeeeeeeee","splash");
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent intent =
                        PendingIntent.getActivity(this, (int) new Date().getTime(), notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel(messageTitle, "1", messageBody);
                    builder = new NotificationCompat.Builder(this, "1");
                } else {
                    builder = new NotificationCompat.Builder(this);
                    notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                }

                // final Uri NOTIFICATION_SOUND_URI = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + BuildConfig.APPLICATION_ID + "/" + R.raw.);
                builder.setContentTitle(messageTitle)
                        .setColor(getResources().getColor(R.color.blue)).setSmallIcon(R.drawable.ic_authx_logo_green) // required
                        .setContentText(messageBody)  // required
                       // .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setLargeIcon(BitmapFactory.decodeResource
                                (getResources(), R.drawable.ic_launcher))
                        .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                        .setContentIntent(intent)
                        .addAction(R.mipmap.ic_launcher,"Accept",intent)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.authxnotification));


                notifManager.notify(generateRandomNumber(200, 101), builder.build());
            }
        } catch (Exception e) {
            Logger.error(TAG + "sendNotification(String messageTitle, String messageBody, Map<String, String> row)", e.getMessage());
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(String title, String channel, String description) {
        try {
            if (notifManager == null) {
                notifManager = (NotificationManager) getSystemService
                        (Context.NOTIFICATION_SERVICE);
            }
            if (mChannel == null) {
                mChannel = new NotificationChannel
                        (channel, title, NotificationManager.IMPORTANCE_HIGH);
                mChannel.setDescription(description);
                mChannel.enableVibration(true);
                notifManager.createNotificationChannel(mChannel);
            }
        } catch (Exception e) {
            Logger.error(TAG + "createNotificationChannel(String title, String channel, String description)", e.getMessage());
        }
    }

    private static int generateRandomNumber(int max, int min) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }
}