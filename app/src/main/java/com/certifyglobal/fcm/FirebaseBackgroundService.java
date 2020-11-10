package com.certifyglobal.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.legacy.content.WakefulBroadcastReceiver;

import com.certifyglobal.async_task.ForegroundCheckTask;
import com.certifyglobal.authenticator.PushNotificationActivity;
import com.certifyglobal.authenticator.R;
import com.certifyglobal.authenticator.SplashActivity;
import com.certifyglobal.authenticator.TokenPersistence;
import com.certifyglobal.authenticator.UserActivity;
import com.certifyglobal.authenticator.ble.BleDefinedUUIDs;
import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.PreferencesKeys;
import com.certifyglobal.utils.Utils;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static android.content.Context.NOTIFICATION_SERVICE;

public class FirebaseBackgroundService extends BroadcastReceiver {
    private static final String TAG = "FirebaseBackground";

    private String machineName = "CERTIFYLT-49";
    private String user;
    private String totp = "454545";
    private BluetoothGattCallback gattCallback;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            //sendToken(context);
            if (intent.getExtras() != null) {

                //         JSONObject obj = Utils.pushInfo(intent, context);
//                if (obj.getString("pushType").equals("1")) {
//                    machineName = obj.getString("machineName");
//                    user = obj.getString("userId");
//                    mTokenPersistence = new TokenPersistence(context);
//                    for (int i = 0; i < mTokenPersistence.length(); i++) {
//                        Token tokenTemp = mTokenPersistence.get(i);
//                        if (tokenTemp.getLabel().contains(user)) {
//                            totp = tokenTemp.generateCodes().getCurrentCode();
//                            break;
//                        }
//                    }
//                   // Log.d(TAG, String.format("machineName: %1$s, user: %2$s, totp: %3$s", machineName, user, totp));
//                    if (!machineName.isEmpty() && !totp.isEmpty())
//                    sendToken(context);
//                }
                // addNotify(context,intent);
               /* if (!new ForegroundCheckTask().execute(context).get()) {
                    Logger.debug("backgrounddddddd" ,"background");
                  *//*  String action = intent.getAction();
                    if ("com.certifyglobal.fcm.YES".equals(action)) {
                        Toast.makeText(context, "YES CALLED", Toast.LENGTH_SHORT).show();
                    }
                    else  if ("com.certifyglobal.fcm.NO".equals(action)) {
                        Toast.makeText(context, "STOP CALLED", Toast.LENGTH_SHORT).show();
                    }*/
//                if (!new ForegroundCheckTask().execute(context).get()) {
//                    displayNotification(intent.getExtras().getString("Title"), intent.getExtras().getString("Body"), context);
//                }

                if (!new ForegroundCheckTask().execute(context).get()) {
                    JSONObject obj = Utils.pushInfo(intent, context);
                    int count = Utils.readFromPreferences(context, PreferencesKeys.notificationCount, 0);
                    if (count == 0) {
                        Utils.saveToPreferences(context, PreferencesKeys.notificationData, obj.toString());
                        Utils.saveToPreferences(context, PreferencesKeys.notificationTime, obj.getString("timeStamp"));
                        Utils.saveToPreferences(context, PreferencesKeys.notificationTimeOut, obj.getString("timeOut"));
                    }
                    count = count + 1;
                    Utils.saveToPreferences(context, PreferencesKeys.notificationCount, count);

                }else{
                                        //displayNotification(intent.getExtras().getString("Title"), intent.getExtras().getString("Body"), context);


                }
            }
        } catch (Exception e) {
            Logger.error(TAG + "onReceive(Context context, Intent intent)", e.getMessage());
        }
    }

    public void displayNotification(String title, String body,Context mCtx) {


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mCtx, "1")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        Intent intent = new Intent(mCtx, SplashActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(mCtx,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);


        Intent dismiss_intent=new Intent(mCtx, MediaPlayingService.class);
        dismiss_intent.setAction(MediaPlayingService.ACTION_ACCEPT);
        Intent dismissing_intent=new Intent(mCtx, MediaPlayingService.class);
        dismissing_intent.setAction(MediaPlayingService.DISMISSING_ACTION);

        PendingIntent pending=PendingIntent.getService(mCtx,0,dismiss_intent,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pending2=PendingIntent.getService(mCtx,0,dismissing_intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action action= new NotificationCompat.Action(android.R.drawable.ic_lock_idle_alarm,
                "Accept",pending);
        mBuilder.addAction(action);
        NotificationCompat.Action action2= new NotificationCompat.Action(R.drawable.ic_launcher_foreground,"Deny",pending2);
        mBuilder.addAction(action2);


        NotificationManager mNotificationManager = (NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification=mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(1,notification);

    }

    private static int generateRandomNumber(int max, int min) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    private void sendToken(final Context context) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
           return;
        }

        final BluetoothManager bm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter ba = bm.getAdapter();
        final BluetoothLeScanner bsc = ba.getBluetoothLeScanner();
        Log.d(TAG, String.format("sendToken BluetoothManager: %1$s, BluetoothAdapter: %2$s, BluetoothLeScanner: %3$s", bm, ba, bsc));
        gattCallback = new BluetoothGattCallback() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                Log.d(TAG, "onServicesDiscovered " + status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothGattService service = gatt.getService(BleDefinedUUIDs.Service.AUTHX);
                    BluetoothGattCharacteristic otpCharasteristic = null;
                    if (service == null) {
                        List<BluetoothGattService> services = gatt.getServices();
                        Log.e(TAG, "BluetoothGattService IS NULL, device: " + gatt.getDevice().getName()
                                + ", services: " + services.size()
                                + ", connected: " + bm.getConnectedDevices(BluetoothProfile.GATT).size());
                        for (BluetoothGattService s : services) {
                            Log.d(TAG, "SERVICE : " + s.getUuid());
                        }
                        service = new BluetoothGattService(BleDefinedUUIDs.Service.AUTHX,
                                BluetoothGattService.SERVICE_TYPE_PRIMARY);
                        otpCharasteristic =
                                new BluetoothGattCharacteristic(BleDefinedUUIDs.Characteristic.AUTHX_OTP,
                                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                                        BluetoothGattCharacteristic.PERMISSION_WRITE);
                    } else {
                        otpCharasteristic =
                                service.getCharacteristic(BleDefinedUUIDs.Characteristic.AUTHX_OTP);
                    }
                    Log.d(TAG, "otpCharasteristic");
                    otpCharasteristic.setValue(totp.getBytes());//TODO: CHANGE TO OTP
                    gatt.writeCharacteristic(otpCharasteristic);
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.d(TAG, "onCharacteristicWrite " + status);
                gatt.disconnect();
                gatt.close();
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.i(TAG, "Gatt Connected");
                    gatt.discoverServices();
                }
            }
        };
        ScanFilter uuidFilter = new ScanFilter.Builder().setServiceUuid(
                new ParcelUuid(BleDefinedUUIDs.Service.AUTHX)).build();
        ScanFilter deviceFilter = new ScanFilter.Builder().setDeviceName(machineName).build();//TODO: CHANGE
        List<ScanFilter> filters = Arrays.asList(deviceFilter);
        ScanSettings scanSettings = new ScanSettings.Builder().build();
        final ScanCallback scanCallback = new ScanCallback() {
            boolean sentMessage = false;

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                if (sentMessage) return;
                sentMessage = true;
                Log.d(TAG, String.format("onScanResult %1$s", result.getScanRecord().getDeviceName()));
                bsc.stopScan(new ScanCallback() {
                    @Override
                    public void onScanFailed(int errorCode) {
                        Log.e(TAG, "onScanResult::stopScaon::onScanFailed " + errorCode);
                    }

                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        Log.e(TAG, "onScanResult::stopScaon::onScanResult " + result);
                    }
                });
                result.getDevice().connectGatt(context, false, gattCallback);
//                boolean ret = gatt.connect();
//                Log.d(TAG, "gatt.connect(): "+ret);
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e(TAG, String.format("onScanFailed errorCode: %d", errorCode));
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                Log.d(TAG, String.format("onBatchScanResults %d", results.size()));
            }
        };
        Log.d(TAG, String.format("startScan %1$s", machineName));
        bsc.startScan(filters, scanSettings, scanCallback);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                //TODO: cleanup
//                Log.d(TAG, "Handler postDelayed");
//                bsc.stopScan(new ScanCallback() {
//                });
//            }
//        }, 50000);
    }
}