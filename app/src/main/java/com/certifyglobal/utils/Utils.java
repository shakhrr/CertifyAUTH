package com.certifyglobal.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.certifyglobal.async_task.AsyncJSONObjectHeader;
import com.certifyglobal.async_task.AsyncJSONObjectImageUpdate;
import com.certifyglobal.async_task.AsyncJSONObjectSender;
import com.certifyglobal.async_task.AsyncJSONObjectSenderSetting;
import com.certifyglobal.authenticator.ApplicationWrapper;
import com.certifyglobal.authenticator.MainActivity;
import com.certifyglobal.authenticator.PushNotificationActivity;
import com.certifyglobal.authenticator.R;
import com.certifyglobal.authenticator.SplashActivity;
import com.certifyglobal.authenticator.UserActivity;
import com.certifyglobal.callback.Communicator;
import com.certifyglobal.callback.JSONObjectCallback;
import com.certifyglobal.callback.JSONObjectCallbackImage;
import com.certifyglobal.callback.JSONObjectCallbackSetting;
import com.certifyglobal.pojo.FaceSettingInfo;
import com.google.android.gms.common.util.Hex;
import com.google.firebase.iid.FirebaseInstanceId;

import com.google.gson.JsonArray;
import com.innovatrics.iface.Face;
import com.innovatrics.iface.IFace;
import com.innovatrics.iface.enums.FaceAttributeId;
import com.zwsb.palmsdk.PalmSDK;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import butterknife.ButterKnife;

public class Utils {
    private static final String LOG = "Utils - ";
    private static Executor executor;
    private static BiometricPrompt biometricPrompt;
    private static BiometricPrompt.PromptInfo promptInfo;


    public static final class permission {
        public static final String[] camera = new String[]{android.Manifest.permission.CAMERA};
        static final String[] phone = new String[]{android.Manifest.permission.READ_PHONE_STATE};
        public static final String[] storage = new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        public static final String[] all = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};
    }

    public static void QRCodeSender(String code, String userId, int companyId, JSONObjectCallback callback, Context context, String domainValue,String header) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("code_id", code);
            obj.put("user_id", userId);
           // obj.put("CompanyId", companyId);
            obj.put("push_token", Utils.readFromPreferences(context, PreferencesKeys.fireBasePushToken, "").isEmpty() ? FirebaseInstanceId.getInstance().getToken() : Utils.readFromPreferences(context, PreferencesKeys.fireBasePushToken, ""));
            obj.put("device_data", MobileDetailsNew(context));
            obj.put("authenitcate_hash", Utils.getHMacSecretKey(context));
            obj.put("public_key", Utils.readFromPreferences(context, PreferencesKeys.publicKey, ""));
            new AsyncJSONObjectHeader(obj, callback, ApplicationWrapper.BaseUrl(domainValue, EndPoints.codePost),header).execute();

        } catch (Exception e) {
            Logger.error(LOG + "QRCodeSender(String code, int userId, int companyId, JSONObjectCallback callback, Context context,String domainValue)", e.getMessage());
        }
    }

    // Mobile Info
    private static JSONObject MobileDetailsNew(Context context) {
        JSONObject obj2 = new JSONObject();
        try {
            obj2.put("mobile_number", Utils.readFromPreferences(context, PreferencesKeys.mobileNumber, "+1"));
            obj2.put("device_model", Build.MODEL);
            obj2.put("app_version", Utils.readFromPreferences(context, PreferencesKeys.MobileAppVersion, ""));
            obj2.put("mobile_platform", "Android");
            obj2.put("os_version", "Android - " + Build.VERSION.RELEASE);
            obj2.put("unique_device_id", Utils.readFromPreferences(context, PreferencesKeys.deviceUUid, ""));
            KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            boolean lockBoolean = myKM != null && myKM.isKeyguardSecure();
            obj2.put("lock_screen", "" + lockBoolean);
            obj2.put("jail_broken", Utils.readFromPreferences(context, PreferencesKeys.checkRoot, false));
            obj2.put("device_encryption", "" + isEncrypted(context));
            obj2.put("mobile_ip", Utils.getLocalIpAddress());

        } catch (Exception e) {
            Logger.error(LOG + "MobileDetails(Context context)", e.getMessage());
        }
        return obj2;
    }



    private static JSONObject MobileDetails(Context context, boolean publicKeyYN) {
        JSONObject obj2 = new JSONObject();
        try {
            obj2.put("MobileNumber", Utils.readFromPreferences(context, PreferencesKeys.mobileNumber, "+1"));
            obj2.put("Model", Build.MODEL);
            obj2.put("MobileAppVersion", Utils.readFromPreferences(context, PreferencesKeys.MobileAppVersion, ""));
            obj2.put("MobilePlatform", "Android");
            obj2.put("MobileOS", "Android - " + Build.VERSION.RELEASE);
            obj2.put("DeviceID", Utils.readFromPreferences(context, PreferencesKeys.deviceUUid, ""));
            KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            boolean lockBoolean = myKM != null && myKM.isKeyguardSecure();
            obj2.put("LockScreen", "" + lockBoolean);
            obj2.put("JailBroken", Utils.readFromPreferences(context, PreferencesKeys.checkRoot, false));
            obj2.put("Encryption", "" + isEncrypted(context));
            if (publicKeyYN)
                obj2.put("PublicKey", Utils.readFromPreferences(context, PreferencesKeys.publicKey, ""));
        } catch (Exception e) {
            Logger.error(LOG + "MobileDetails(Context context)", e.getMessage());
        }
        return obj2;
    }


    public static JSONObject getQRCodeUrl(String url,String domain) {
        JSONObject objMessage;
        try {
            String responseTemp = Requestor.getRequest(url,domain);
            if (responseTemp != null && !responseTemp.equals(""))
                if (responseTemp.equals("The service is unavailable") || responseTemp.equals("A Server with the specified hostname could not be found.")) {
                    objMessage = new JSONObject();
                    objMessage.put("Message", responseTemp);
                    return objMessage;
                }
            return new JSONObject(responseTemp);
        } catch (Exception e) {
            try {
                Logger.error(LOG + "getQRCodeUrl(String url)" +
                        "url = " + url, e.getMessage());
                objMessage = new JSONObject();
                objMessage.put("Message", "Server not Responding");
            } catch (Exception ignore) {
                objMessage = new JSONObject();
            }
        }
        return objMessage;
    }

    public static void PushAuthenticationStatus(String pushType, boolean isAuthenticated, JSONObjectCallback callback, String requestId, String userId, Context context, int deniedType, boolean isSameFace,String correlationId) {
        try {

            JSONObject obj = new JSONObject();
            obj.put("request_id", requestId);
            obj.put("user_id", userId);
            obj.put("status", isAuthenticated);
           // obj.put("PurposelyDenied", !isAuthenticated);
           // obj.put("isSameFace", isSameFace);
            obj.put("push_type", pushType);
            if (deniedType != 0)
                obj.put("denied_type", deniedType);
            obj.put("device_data", MobileDetailsNew(context));
            obj.put("authenitcate_hash", Utils.getHMacSecretKey(context));
            obj.put("request_signature", RSAKeypair.signData(Utils.readFromPreferences(context, PreferencesKeys.deviceUUid, ""), requestId, Utils.readFromPreferences(context, PreferencesKeys.privateKey, "")));
            obj.put("correlation_id", correlationId);

            new AsyncJSONObjectSender(obj, callback, ApplicationWrapper.BaseUrl(PushNotificationActivity.hostName, EndPoints.pushAuthenticationStatus)).execute();
            Logger.debug("push Log",obj.toString());
            Logger.debug("push Loguuid ","deviceuuid   "+Utils.readFromPreferences(context, PreferencesKeys.deviceUUid, "")+"request ID  "+ requestId+ "private key:  "+ Utils.readFromPreferences(context, PreferencesKeys.privateKey, "")+"public key:   "+Utils.readFromPreferences(context, PreferencesKeys.publicKey, ""));

        } catch (Exception e) {
            Logger.error(LOG + "PushAuthenticationStatus(String userEmail, boolean isAuthenticated, JSONObjectCallback callback, String requestId)", e.getMessage());
        }
    }

    public static void deactivateUser(String userId, String companyId, JSONObjectCallback callback, Context context, String hostName) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("user_id", userId);
            obj.put("device_data", MobileDetailsNew(context));
            obj.put("authenitcate_hash", Utils.getHMacSecretKey(context));
            new AsyncJSONObjectSender(obj, callback, ApplicationWrapper.BaseUrl(hostName, EndPoints.deactivateUser)).execute();

        } catch (Exception e) {
            Logger.error(LOG + "deactivateUser(String userEmail, JSONObjectCallback callback)", e.getMessage());
        }
    }


    // 3rd Party App Totp link sent api
    public static void addMobileApp(String userEmail, String applicationName, String url, String SecretKey, JSONObjectCallback callback, Context context) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("Username", userEmail);
            obj.put("MobileApplicationName", applicationName);
            obj.put("MobileDetails", MobileDetails(ApplicationWrapper.context, false));
            obj.put("IsAuthorize", true);
            obj.put("URL", url);
            obj.put("SecretKey", SecretKey);
            new AsyncJSONObjectSender(obj, null, com.certifyglobal.authenticator.Settings.getAPI() + EndPoints.addMobileApp).execute();

        } catch (Exception e) {
            Logger.error(LOG + "deactivateUser(String userEmail, JSONObjectCallback callback)", e.getMessage());
        }
    }


    // app update version,isCritical fine outing
    public static void getOSDetails(JSONObjectCallback callback, Context context) {
        try {
            if (Utils.isConnectingToInternet(context)) {
                JSONObject obj = new JSONObject();
                obj.put("os", "android");
                new AsyncJSONObjectSender(obj, callback, EndPoints.getOsDetails).execute();
            }

        } catch (Exception e) {
            Logger.error(LOG + "deactivateUser(String userEmail, JSONObjectCallback callback)", e.getMessage());
        }
    }

    // Phone Number veryfication. number have register get all accounts
    public static void sendUserTOtp(JSONObjectCallback callback, String phoneNumber) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("PhoneNumber", phoneNumber);
            new AsyncJSONObjectSender(obj, callback, com.certifyglobal.authenticator.Settings.getAPI() + EndPoints.sendUserTOtp).execute();

        } catch (Exception e) {
            Logger.error(LOG + "deactivateUser(String userEmail, JSONObjectCallback callback)", e.getMessage());
        }
    }

    // Phone Number veryfication. number have register get all accounts
    public static void validateUserCode(JSONObjectCallback callback, String otp, Context context) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("PhoneNumber", Utils.readFromPreferences(context, PreferencesKeys.mobileNumber, ""));
            obj.put("Totp", otp);
            obj.put("MobileDetails", MobileDetails(context, true));
            obj.put("PushNotificationToken", Utils.readFromPreferences(context, PreferencesKeys.fireBasePushToken, "").isEmpty() ? FirebaseInstanceId.getInstance().getToken() : Utils.readFromPreferences(context, PreferencesKeys.fireBasePushToken, ""));
            obj.put("SecretKey", Utils.getHMacSecretKey(context));
            new AsyncJSONObjectSender(obj, callback, com.certifyglobal.authenticator.Settings.getAPI() + EndPoints.validateUserCode).execute();

        } catch (Exception e) {
            Logger.error(LOG + "deactivateUser(String userEmail, JSONObjectCallback callback)", e.getMessage());
        }
    }


    public static void faceSetting(JSONObjectCallbackSetting callback, String requestId, String userId, Context context) {
        try {

            JSONObject obj = new JSONObject();
            obj.put("request_id", requestId);
            obj.put("user_id", userId);
            obj.put("device_data", MobileDetailsNew(context));
            obj.put("authenitcate_hash", Utils.getHMacSecretKey(context));
            obj.put("request_signature", RSAKeypair.signData(Utils.readFromPreferences(context, PreferencesKeys.deviceUUid, ""), requestId, Utils.readFromPreferences(context, PreferencesKeys.privateKey, "")));


            new AsyncJSONObjectSenderSetting(obj, callback, ApplicationWrapper.BaseUrl(PushNotificationActivity.hostName, EndPoints.faceSetting)).execute();

        } catch (Exception e) {
            Logger.error(LOG + "PushAuthenticationStatus(String userEmail, boolean isAuthenticated, JSONObjectCallback callback, String requestId)", e.getMessage());
        }
    }
    public static byte[] getByteArray(JSONArray jsonArray) {
        byte[] byteArrayJson = new byte[jsonArray.length()];
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                byteArrayJson[i] = (byte) ((int) jsonArray.get(i) & 0xFF);
            }
        } catch (Exception e) {
            return null;
        }
        return byteArrayJson;
    }

    // face Enrolling image send
    public static void PushFace(String userName, byte[] byteArray, JSONObjectCallback callback, String requestId,String userId, String pushType, Context context,String corelationId) {
        try {
            JSONArray byteArrayJson = new JSONArray();
            if (byteArray != null) {
                for (byte b : byteArray) {
                    int val = b & 0xFF;
                    byteArrayJson.put(val);

                }
            }

            JSONObject obj = new JSONObject();
            obj.put("request_id", requestId);
            obj.put("user_id", userId);
            obj.put("push_type", pushType);
            obj.put("device_data", MobileDetailsNew(context));
            obj.put("authenitcate_hash", Utils.getHMacSecretKey(context));
            obj.put("request_signature", RSAKeypair.signData(Utils.readFromPreferences(context, PreferencesKeys.deviceUUid, ""), requestId, Utils.readFromPreferences(context, PreferencesKeys.privateKey, "")));
            obj.put("face_bio", byteArrayJson);
            obj.put("correlation_id", corelationId);
            obj.put("public_key", Utils.readFromPreferences(context, PreferencesKeys.publicKey, ""));


            new AsyncJSONObjectSender(obj, callback, ApplicationWrapper.BaseUrl(PushNotificationActivity.hostName, pushType.equals("2") ? EndPoints.pushFace : EndPoints.facePushVerify)).execute();

        } catch (Exception e) {
            Logger.error(LOG + "PushFace(String userName, byte[] byteArray, JSONObjectCallback callback, String requestId, String userId, String pushType)", e.getMessage());
        }
    }

    public static JSONObject getJSONObject(JSONObject req, String url,String header) {
        try {
            String responseTemp = Requestor.requestJson(url, req, header);
            if (responseTemp != null && !responseTemp.equals(""))
                return new JSONObject(responseTemp);
        } catch (Exception e) {

            Logger.error(LOG + "getJSONObject(JSONObject req, String url): req = " + req
                    + ", url = " + url, e.getMessage());
            return null;

        }
        return null;
    }

    public static String getJSONObjectImage(JSONObject req, String url,String header) {
        try {
            String responseTemp = Requestor.requestJson(url, req, header);
            if (responseTemp != null && !responseTemp.equals(""))
                return new String(responseTemp);
        } catch (Exception e) {

            Logger.error(LOG + "getJSONObject(JSONObject req, String url): req = " + req
                    + ", url = " + url, e.getMessage());
            return null;

        }
        return null;
    }


    public static JSONArray getJSONObjectInArray(JSONObject req, String url) {
        try {

            String responseTemp = Requestor.requestJson(url, req, "POST");
            if (responseTemp != null && !responseTemp.equals("")) {
                return new JSONArray(responseTemp);
            }
        } catch (Exception e) {

            Logger.error(LOG + "getJSONObject(JSONObject req, String url): req = " + req
                    + ", url = " + url, e.getMessage());
            return null;

        }
        return null;
    }

    public static boolean isConnectingToInternet(Context context) {
        try {
            ConnectivityManager cm = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
            if (cm != null)
                for (NetworkInfo ni : cm.getAllNetworkInfo()) {
                    switch (ni.getTypeName().trim().toUpperCase()) {
                        case "WIFI":
                        case "MOBILE":
                            if (ni.isConnected() && !(ni.getSubtypeName() != null && ni.getSubtypeName().trim().toUpperCase().equals("LTE") && ni.getExtraInfo() != null && ni.getExtraInfo().trim().toUpperCase().equals("IMS"))) {
                                // MyApplication.noInternet = 0;
                                return true;
                            }
                            break;
                    }
                }
        } catch (Exception e) {
            Logger.error(LOG + "isConnectingToInternet()", e.getMessage());
        }
        return false;
    }

    public static void saveToPreferences(Context context, String preferenceName, String preferenceValue) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(preferenceName, preferenceValue == null ? "" : preferenceValue);
            editor.apply();
        } catch (Exception e) {
            Logger.error(LOG + "saveToPreferences(Context context, String preferenceName, String preferenceValue)", e.getMessage());
        }
    }

    public static String readFromPreferences(Context context, String preferenceName, String defaultValue) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            return sharedPreferences.getString(preferenceName, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static void saveToPreferences(Context context, String preferenceName, int preferenceValue) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(preferenceName, preferenceValue);
            editor.apply();
        } catch (Exception e) {
            Logger.error(LOG + "saveToPreferences(Context context, String preferenceName, String preferenceValue)", e.getMessage());
        }
    }

    public static void saveToPreferences(Context context, String preferenceName, Boolean preferenceValue) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(preferenceName, preferenceValue);
            editor.apply();
        } catch (Exception e) {
            Logger.error(LOG + "saveToPreferences(Context context, String preferenceName, Boolean preferenceValue)", e.getMessage());
        }
    }

    public static Boolean readFromPreferences(Context context, String preferenceName, Boolean defaultValue) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            return sharedPreferences.getBoolean(preferenceName, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static int readFromPreferences(Context context, String preferenceName, int defaultValue) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            return sharedPreferences.getInt(preferenceName, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String getCurrentTimeMMM_dd_yyyy() {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
            return simpleDateFormat.format(new Date());
        } catch (Exception e) {
            Logger.error(LOG, e.toString());
        }
        return "";
    }

    public static String getCurentTime_h_mm_a() {
        try {
            SimpleDateFormat writeDate = new SimpleDateFormat("h:mm:ss aa z", Locale.getDefault());
            writeDate.setTimeZone(TimeZone.getDefault());
            return writeDate.format(new Date());
        } catch (Exception e) {
            Logger.error(LOG, e.toString());
        }
        return "";
    }

    public static void setServerTime(String timestamp, TextView tvDate, TextView tvTime) {
        try {
            if (timestamp == null || timestamp.isEmpty()) return;
            SimpleDateFormat serverDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.getDefault());
            serverDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date serverDate = serverDateFormat.parse(timestamp);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            tvDate.setText(simpleDateFormat.format(serverDate));
            SimpleDateFormat writeDate = new SimpleDateFormat("h:mm:ss aa z", Locale.getDefault());
            tvTime.setText(writeDate.format(serverDate));
        } catch (Exception e) {
            Logger.error(LOG + " setServerTime(String timestamp, TextView tvDate, TextView tvTime)", e.toString());
        }
    }

    public static int getTimerTimeStamp(String timestamp, int timeOut) {
        try {
            if (timestamp == null || timestamp.isEmpty()) return timeOut;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.ENGLISH);
            Date serverDate = simpleDateFormat.parse(timestamp);
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.ENGLISH);
            simpleDateFormat2.setTimeZone(TimeZone.getTimeZone("UTC"));
            String localTime = simpleDateFormat2.format(new Date());
            Date localDate = simpleDateFormat.parse(localTime);
            long serverDateLong = serverDate.getTime();
            long localDateLong = localDate.getTime();
            long temp = (localDateLong - serverDateLong);
            temp = timeOut - temp;
            if (EndPoints.deployment == EndPoints.Mode.Local)
                Logger.debug("getTimerTimeStamp", "timestamp " + timestamp + " localTime " + localTime + " temp " + temp);

            return (int) (temp > timeOut ? timeOut : temp);
        } catch (Exception e) {
            Logger.error(LOG, e.toString());
        }
        return timeOut;
    }

    public static boolean PermissionRequest(android.app.Activity context, String[] permissions) {
        try {
            if (permissions == null) return false;
            ArrayList<String> requestPermission = new ArrayList<>();
            for (String permission : permissions) {
                int permissionCheck = context.checkSelfPermission(permission);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED)
                    requestPermission.add(permission);
            }
            if (requestPermission.size() <= 0) return false;
            context.requestPermissions(requestPermission.toArray(new String[0]), 1);
        } catch (Exception e) {
            Logger.error(LOG + "PermissionRequest(android.app.Activity context, String[] permissions", e.getMessage());
        }
        return true;
    }

    public static AlertDialog PermissionDialog(final Activity context) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Failed to get permission, try re-start the app and grant permission. Application will close now.");
            AlertDialog alert = builder.create();
            alert.setButton(DialogInterface.BUTTON_POSITIVE, "Close", new DialogInterface.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                public void onClick(DialogInterface dialog, int which) {
                    context.finish();
                }
            });
            alert.setCancelable(false);
            alert.show();
            //noinspection deprecation
            alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.colorPrimary));
            return alert;
        } catch (Exception e) {
            Logger.error(LOG + "PermissionDialog(final Activity context)", e.getMessage());
        }
        return null;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static void getDeviceUUid(Activity activity) {
        Logger.debug("uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu","splash");

        if (!Utils.readFromPreferences(activity.getApplication(), PreferencesKeys.deviceUUid, "").isEmpty())
            return;
        String deviceUUid = null;
        try {
            try {
                if (!Utils.PermissionRequest(activity, permission.phone))
                    //noinspection ConstantConditions
                    deviceUUid = ((TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

            } catch (Exception ignored) {
            }
            try {
                if (deviceUUid == null)
                    deviceUUid = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
            } catch (Exception ignored) {
                deviceUUid = UUID.randomUUID().toString();
                Utils.saveToPreferences(activity, PreferencesKeys.deviceUUid, deviceUUid);
            }
            Utils.saveToPreferences(activity, PreferencesKeys.fireBasePushToken, FirebaseInstanceId.getInstance().getToken());

        } catch (Exception e) {
            deviceUUid = UUID.randomUUID().toString();
            Utils.saveToPreferences(activity, PreferencesKeys.deviceUUid, deviceUUid);
        }
        Utils.saveToPreferences(activity, PreferencesKeys.deviceUUid, deviceUUid);
    }

    @SuppressLint("MissingPermission")
    public static void getNumberVersion(Activity activity) {
        try {
            if (!Utils.readFromPreferences(activity.getApplication(), PreferencesKeys.mobileNumber, "").isEmpty())
                return;
            TelephonyManager tMgr = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
            if (tMgr != null) {
                if (!Utils.PermissionRequest(activity, permission.phone))
                    Utils.saveToPreferences(activity, PreferencesKeys.mobileNumber, tMgr.getLine1Number());
            }
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            Utils.saveToPreferences(activity, PreferencesKeys.MobileAppVersion, packageInfo.versionName);
        } catch (Exception e) {
            Logger.error(LOG + "getNumberVersion()", e.getMessage());
        }
    }

    public static Dialog showDialog(Dialog dialog, Context context) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.prograss_dialog);
        if (dialog.getWindow() == null) return null;
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return dialog;
    }

    public static void ShowDialog(final Context context, final String userName, final int position, final Communicator communicator) {
        try {
            if (context == null) return;
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.logout_dialog);
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            if (dialog.getWindow() == null) return;
            dialog.getWindow().setLayout(displayMetrics.widthPixels - 20, LinearLayout.LayoutParams.WRAP_CONTENT);
            Button bt_ok = dialog.findViewById(R.id.bt_ok);
            Button bt_cancel = dialog.findViewById(R.id.bt_cancel);
            bt_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            bt_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    communicator.setAction(userName, position);
                }
            });
            dialog.show();
        } catch (Exception e) {
            Logger.error(LOG + "ShowDialog(final CouponsInfo couponsInfo, final String type)", e.getMessage());
        }
    }

    private static byte[] readFileResource(Class<?> resourceClass, String resourcePath) throws IOException {
        InputStream is = resourceClass.getClassLoader().getResourceAsStream(resourcePath);
        if (is == null)
            throw new IOException("cannot find resource: " + resourcePath);
        return getBytesFromInputStream(is);
    }

    private static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        for (int len; (len = is.read(buffer)) != -1; ) {
            os.write(buffer, 0, len);
        }
        os.flush();
        return os.toByteArray();
    }

    public static void keyValidations(Context context, Activity activity) {
        try {
            if (!Utils.isConnectingToInternet(activity.getApplication())) return;
            try {

                IFace iface = IFace.getInstance();
                iface.initWithLicence(Utils.readFileResource(activity.getClass(), EndPoints.LICENSE_FILE));
            } catch (UnsatisfiedLinkError e) {
                PushNotificationActivity.authenticationBooleanError = true;
                Logger.error(LOG + "keyValidations(Context context)", e.getMessage());
            } catch (IOException e) {
                Logger.error(LOG + "keyValidations()11", e.getMessage());
                //   PushNotificationActivity.authenticationBooleanError = true;
            }
            PushNotificationActivity.authenticationBooleanError = false;

            if (context == null) {
                PushNotificationActivity.authenticationBoolean = true;
                return;
            }
            ButterKnife.bind(activity);
            palmSDK(context);
        } catch (Exception e) {
            if (context == null && e.getMessage().contains("License is expired"))
                Logger.toast(activity.getApplicationContext(), "License is expired.");
            Logger.error(LOG + "keyValidations()22", e.getMessage());
            //   PushNotificationActivity.authenticationBooleanError = true;
        }
    }

    private static void palmSDK(final Context context) {
        try {
            PalmSDK.init(context, "certifyglobal-ge5", new PalmSDK.InitSDKCallback() {//windows_demo@certifyglobal.com old license key
                @Override
                public void onSuccess() {
                    PushNotificationActivity.authenticationBoolean = true;
                    PushNotificationActivity.authenticationBooleanError = false;
                }

                @Override
                public void onError() {
                    Logger.toast(context, context.getResources().getString(R.string.palm_dev_error));
                    //  onSuccessListener(null);
                }
            });
        } catch (Exception e) {
            Logger.toast(context, context.getResources().getString(R.string.palm_dev_error));
            Logger.error(LOG + "palmSDK()", e.getMessage());
        }
    }

    public static int dp2px(int dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    //  HmacSHA256 SecretKey creating
    public static String getHMacSecretKey(Context context) {
        try {
            if (Utils.readFromPreferences(context, PreferencesKeys.secretKeyHMac, "").isEmpty() || Utils.readFromPreferences(context, PreferencesKeys.hMacFirst, true)) {
                String data = Utils.readFromPreferences(context, PreferencesKeys.deviceUUid, "");
                String key = String.format("%s:%s", Build.MODEL, data);
                Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
                SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                sha256_HMAC.init(secret_key);
                String hmacSecret = Hex.bytesToStringUppercase(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
                Utils.saveToPreferences(context, PreferencesKeys.secretKeyHMac, hmacSecret.toLowerCase());
                Utils.saveToPreferences(context, PreferencesKeys.hMacFirst, false);
                return hmacSecret.toLowerCase();
            } else return Utils.readFromPreferences(context, PreferencesKeys.secretKeyHMac, "");
        } catch (Exception e) {
            Logger.error(LOG + "getHMacSecretKey(Context context)", e.getMessage());
        }
        return "";
    }

    public static float[] getFaceAttribute(Face iFacesImage) {
        float[] attributeValues = new float[22];
        try {
            attributeValues[0] = iFacesImage.getAttribute(FaceAttributeId.SHARPNESS);
            attributeValues[1] = iFacesImage.getAttribute(FaceAttributeId.BRIGHTNESS);
            attributeValues[2] = iFacesImage.getAttribute(FaceAttributeId.CONTRAST);
            attributeValues[3] = iFacesImage.getAttribute(FaceAttributeId.ROLL);
            attributeValues[4] = iFacesImage.getAttribute(FaceAttributeId.YAW);
            attributeValues[5] = iFacesImage.getAttribute(FaceAttributeId.PITCH);
            attributeValues[6] = iFacesImage.getAttribute(FaceAttributeId.FACE_CONFIDENCE);
            attributeValues[7] = iFacesImage.getAttribute(FaceAttributeId.GLASS_STATUS);
            attributeValues[8] = iFacesImage.getAttribute(FaceAttributeId.EYE_DISTANCE);
            attributeValues[9] = iFacesImage.getAttribute(FaceAttributeId.UNIQUE_INTENSITY_LEVELS);
            attributeValues[10] = iFacesImage.getAttribute(FaceAttributeId.SHADOW);
          //  attributeValues[11] = iFacesImage.getAttribute(FaceAttributeId.NOSE_SHADOW);
            attributeValues[11] = iFacesImage.getAttribute(FaceAttributeId.SPECULARITY);
          //  attributeValues[13] = iFacesImage.getAttribute(FaceAttributeId.EYE_GAZE);
            attributeValues[12] = iFacesImage.getAttribute(FaceAttributeId.EYE_STATUS_R);
            attributeValues[13] = iFacesImage.getAttribute(FaceAttributeId.EYE_STATUS_L);
           // attributeValues[16] = iFacesImage.getAttribute(FaceAttributeId.HEAVY_FRAME);
            attributeValues[14] = iFacesImage.getAttribute(FaceAttributeId.MOUTH_STATUS);
            attributeValues[15] = iFacesImage.getAttribute(FaceAttributeId.BACKGROUND_UNIFORMITY);
           // attributeValues[19] = iFacesImage.getAttribute(FaceAttributeId.RED_EYE_R);
         //   attributeValues[20] = iFacesImage.getAttribute(FaceAttributeId.RED_EYE_L);
            attributeValues[16] = iFacesImage.getAttribute(FaceAttributeId.FACE_VERIFICATION_CONFIDENCE);

        } catch (Exception e) {
            Logger.error(LOG + "getFaceAttribute()", e.toString());
        }
        return attributeValues;

    }

    public static String getDecrypted(String encryptedText, Context context) {
        try {
            String[] values = encryptedText.split("::");
            if (values.length < 2) return "";
            String data = Utils.readFromPreferences(context, PreferencesKeys.deviceUUid, "");
            String key = String.format("%s:%s", Build.MODEL, data);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            byte[] sharedSecret = mac.doFinal(data.getBytes());
            SecretKeySpec secretKeySpec = new SecretKeySpec(sharedSecret, "AES");
            //decrypt
            //TODO: handle exception
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(fromBase64(values[1]));

            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encryptedBytes = fromBase64(values[0]);
            byte[] decrypted = cipher.doFinal(encryptedBytes);
            return new String(decrypted);
        } catch (Exception e) {
            Logger.error(LOG + "getDecrypted(String serverIV, String encryptedText, Context context)", e.toString());
        }
        return "";
    }

    private static byte[] fromBase64(String base64) {
        //  return Base64.getDecoder().decode(base64);
        return android.util.Base64.decode(base64.getBytes(),
                android.util.Base64.DEFAULT);
    }

    public static boolean isEncrypted(Context context) {
        try {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context
                    .getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (devicePolicyManager == null) return false;
            int status = devicePolicyManager.getStorageEncryptionStatus();
           //DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED == status ||
            if (DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE == status) {
                return false;
            }
        } catch (Exception e) {
            Logger.error(LOG + "isEncrypted(Context context)", e.toString());
        }
        return true;
    }

    //
    public static  ArrayList<HashMap<String, String>> StringToJSon(Context context,String faceSetting,String version,String userId,String id) {
        ArrayList<HashMap<String, String>> face_setting_arraylist=new ArrayList<>();
        ArrayList<HashMap<String, String>>  face_setting_arraylist_temp = ApplicationWrapper.getMdbCompanyAdapter().getSettingList(userId);
        try {
            if (faceSetting == null || faceSetting.isEmpty()) return face_setting_arraylist;
            //JSONObject obj = new JSONObject(faceSetting);
            JSONObject obj = new JSONObject(faceSetting);
            JSONObject  jsonObject = null;
            String faceParameters[] = {"Sharpness", "Brightness", "Contrast", "Roll", "Yaw", "Pitch", "FaceConfidence", "GlassStatus", "EyeDistance","UniqueIntensityLevels","Shadow","NoseShadow","Specularity","EyeGaze","EyeStatusR","EyeStatusL","HeavyFrame","MouthStatus","BackgroundUniformity","RedEyeR","RedEyeL","FaceVerificationConfidence"};
            jsonObject=obj.getJSONObject("Mobile").getJSONObject("FaceSettings");
            for (int i = 0; i < faceParameters.length; i++) {
                //if (obj.isNull(faceParameters[i])) continue;

                JSONObject objParams = jsonObject.getJSONObject(faceParameters[i]);
                FaceSettingInfo info = new FaceSettingInfo();
                info.position = i;

                info.min = objParams.getString("Min");
                info.max =objParams.getString("Max");
                info.IsConfigured =objParams.getString("IsConfigured");
                info.matchScore =jsonObject.getString("MatchScore");
                if (info.IsConfigured.equals("false")) continue;
                if(face_setting_arraylist_temp.size()==0) {
                    ApplicationWrapper.getMdbCompanyAdapter().insertFaceSetting(userId, version, faceParameters[i], info.min, info.max, info.IsConfigured, info.matchScore,id);
                    Logger.debug("dbbbbbbbbbbbbbbbbbbbbbbb","insert0000000000000");

                }else {
                    for (int j=0;j<face_setting_arraylist_temp.size();j++){
                        String companyIDdb=face_setting_arraylist_temp.get(j).get("userid");
                        String versiondb=face_setting_arraylist_temp.get(j).get("version");
                        if(!versiondb.equals(version) && companyIDdb.equals(userId)){

                            ApplicationWrapper.getMdbCompanyAdapter().updateFaceSetting(userId, version, faceParameters[i], info.min, info.max, info.IsConfigured, info.matchScore,id);
                            Logger.debug("dbbbbbbbbbbbbbbbb","updateeeeeeeee");

                        }/*else if(versiondb.equals(version) && companyIDdb.equals(userId)){
                            Logger.debug("dbbbbbbbbbbbbbbbbb","nothinggggggggggggg");

                        }else{
                            ApplicationWrapper.getMdbCompanyAdapter().insertFaceSetting(userId, version, faceParameters[i], info.min, info.max, info.IsConfigured, info.matchScore,id);
                            Logger.debug("dbbbbbbbbbbbbbbb","insertttttttt");

                        }*/
                    }
                }

            }
            SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(context);
            boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUN", true);
            String versionPref=wmbPreference.getString("version","");
            if(isFirstRun) {

                    for(Iterator<String> iter = jsonObject.getJSONObject("FaceError").keys(); iter.hasNext();) {
                        String key = iter.next();
                        String value = jsonObject.getJSONObject("FaceError").optString(key);
                        ApplicationWrapper.getMdbCompanyAdapter().insertFaceError(key,value);
                        // ApplicationWrapper.getMdbCompanyAdapter().insertFaceErrorNew(userId,key,value);


                    //api brightness
                    SharedPreferences.Editor editor = wmbPreference.edit();
                     editor.putBoolean("FIRSTRUN", false);
                    editor.putString("version", version);
                    editor.commit();
                }
            }

            return face_setting_arraylist;

        } catch (Exception e) {
            Logger.error(LOG + "StringToJSon(String faceSetting)", e.toString());
        }
        return face_setting_arraylist;
    }



    public static String getIssuer(String issuer) {

        if (issuer.contains("Amazon")) {
            return "Amazon";
        } else if (issuer.contains("Dropbox")) {
            return "Dropbox";
        }
        if (issuer.contains("Google")) {
            return "Google";
        } else if (issuer.contains("Facebook")) {
            return "Facebook";
        }
        if (issuer.contains("Microsoft")) {
            return "Microsoft";
        } else if (issuer.contains("Github")) {
            return "Github";
        }
        if (issuer.contains("Slack")) {
            return "Slack";
        } else if (issuer.contains("Twitter")) {
            return "Twitter";
        }
        if (issuer.contains("Openvpn")) {
            return "Openvpn";
        }
        return ApplicationWrapper.context.getString(R.string.third_party);
    }

    public static boolean compareTowString(String oldVersion, String newVersion) {
        //   boolean booleanVal = false;
        try {
            String[] oldArrayString = oldVersion.split("[.]");
            String[] newArrayString = newVersion.split("[.]");
            for (int i = 0; i < newArrayString.length; i++) {
                if (i < oldArrayString.length) {
                    if (Integer.parseInt(newArrayString[i]) > Integer.parseInt(oldArrayString[i])) {
                        return true;
                    }
                } else if (Integer.parseInt(newArrayString[i]) > 0) return true;
            }

        } catch (Exception e) {
            Logger.error("compareTowString(String oldVersion, String newVersion)", e.getMessage());
        }
        return false;
    }

    public static JSONObject pushInfo(Intent intent_o, Context context) {
        try {
            JSONObject obj = new JSONObject();
            if (intent_o.getStringExtra("encValue") != null) {
                String decryptedStr = RSAKeypair.DecryptRSA(Utils.readFromPreferences(context, PreferencesKeys.privateKey, ""), intent_o.getStringExtra("encValue"));
                if (!decryptedStr.isEmpty()) {
                    JSONObject decryOBJ = new JSONObject(decryptedStr);

                    obj.put("requestId", decryOBJ.getString("requestId"));
                    obj.put("user", decryOBJ.getString("user"));
                    obj.put("userId", decryOBJ.isNull("userId") ? "" : decryOBJ.getString("userId"));
                    obj.put("timeStamp", decryOBJ.isNull("TimeStamp") ? "" : decryOBJ.getString("TimeStamp"));
                }
            }
            obj.put("hostName", intent_o.getStringExtra("HostName") == null ? "" : intent_o.getStringExtra("HostName"));
            obj.put("pushType", intent_o.getStringExtra("pushType"));
            obj.put("country", intent_o.getStringExtra("Country") == null ? "" : intent_o.getStringExtra("Country"));
            obj.put("city", intent_o.getStringExtra("City") == null ? "" : intent_o.getStringExtra("City"));
            obj.put("state", intent_o.getStringExtra("State") == null ? "" : intent_o.getStringExtra("State"));
            obj.put("userType", intent_o.getStringExtra("UserType") == null ? "" : intent_o.getStringExtra("UserType"));
            obj.put("companyName", intent_o.getStringExtra("CompanyName") == null ? "" : intent_o.getStringExtra("CompanyName"));
            obj.put("liveliness", intent_o.getStringExtra("Liveliness") == null ? "" : intent_o.getStringExtra("Liveliness"));
            obj.put("applicationName", intent_o.getStringExtra("ApplicationName") == null ? "" : intent_o.getStringExtra("ApplicationName"));
            obj.put("machineName", intent_o.getStringExtra("MachineName") == null ? "" : intent_o.getStringExtra("MachineName"));
            obj.put("ip", intent_o.getStringExtra("IP") == null ? "" : intent_o.getStringExtra("IP"));
            obj.put("timeOut", intent_o.getStringExtra("TimeOut") == null ? "" : intent_o.getStringExtra("TimeOut"));
            obj.put("correlationId", intent_o.getStringExtra("CorrelationId")== null ? "" : intent_o.getStringExtra("CorrelationId"));

            return obj;
        } catch (Exception e) {
            Logger.error(LOG + "JSONObject pushInfo(Intent intent_o, Context context)", e.getMessage());
        }
        return new JSONObject();
    }


    public static Intent attachIntent(Intent notificationIntent, JSONObject decryOBJ) {
        try {
            notificationIntent.putExtra("requestId", decryOBJ.getString("requestId"));
            notificationIntent.putExtra("user", decryOBJ.getString("user"));
            notificationIntent.putExtra("userId", decryOBJ.getString("userId"));
            //  notificationIntent.putExtra("companyId", decryOBJ.getString("companyId"));
            notificationIntent.putExtra("ip", decryOBJ.getString("ip"));
            notificationIntent.putExtra("timeStamp", decryOBJ.getString("timeStamp"));
            notificationIntent.putExtra("hostName", decryOBJ.getString("hostName"));
            notificationIntent.putExtra("pushType", decryOBJ.getString("pushType"));
            notificationIntent.putExtra("country", decryOBJ.getString("country"));
            notificationIntent.putExtra("city", decryOBJ.getString("city"));
            notificationIntent.putExtra("state", decryOBJ.getString("state"));
            notificationIntent.putExtra("userType", decryOBJ.getString("userType"));
            notificationIntent.putExtra("companyName", decryOBJ.getString("companyName"));
            notificationIntent.putExtra("liveliness", decryOBJ.getString("liveliness"));
            notificationIntent.putExtra("applicationName", decryOBJ.getString("applicationName"));
            notificationIntent.putExtra("machineName", decryOBJ.getString("machineName"));
            notificationIntent.putExtra("timeOut", decryOBJ.getString("timeOut"));
            notificationIntent.putExtra("correlationId", decryOBJ.getString("correlationId"));
        } catch (Exception e) {
            Logger.error(LOG + "attachIntent(Intent notificationIntent, JSONObject decryOBJ)", e.getMessage());
        }
        return notificationIntent;
    }

    public static String notificationPayload(JSONObject decryOBJ, Context context) {
        JSONObject obj = new JSONObject();
        try {
            String decryptedStr = RSAKeypair.DecryptRSA(Utils.readFromPreferences(context, PreferencesKeys.privateKey, ""), decryOBJ.getString("encValue"));
            if (!decryptedStr.isEmpty()) {
                JSONObject decryEnc = new JSONObject(decryptedStr);
                obj.put("requestId", decryEnc.getString("requestId"));
                obj.put("user", decryEnc.getString("user"));
                obj.put("userId", decryEnc.isNull("userId") ? "" : decryEnc.getString("userId"));
                obj.put("timeStamp", decryEnc.isNull("TimeStamp") ? "" : decryEnc.getString("TimeStamp"));
            }
            obj.put("hostName", decryOBJ.getString("hostName"));
            obj.put("pushType", decryOBJ.getString("pushType"));
            obj.put("country", decryOBJ.isNull("Country") ? "" : decryOBJ.getString("Country"));
            obj.put("city", decryOBJ.isNull("City") ? "" : decryOBJ.getString("City"));
            obj.put("state", decryOBJ.isNull("State") ? "" : decryOBJ.getString("State"));
            obj.put("userType", decryOBJ.isNull("UserType") ? "" : decryOBJ.getString("UserType"));
            obj.put("companyName", decryOBJ.isNull("CompanyName") ? "" : decryOBJ.getString("CompanyName"));
            obj.put("liveliness", decryOBJ.isNull("Liveliness") ? "" : decryOBJ.getString("Liveliness"));
            obj.put("applicationName", decryOBJ.isNull("ApplicationName") ? "" : decryOBJ.getString("ApplicationName"));
            obj.put("machineName", decryOBJ.isNull("MachineName") ? "" : decryOBJ.getString("MachineName"));
            obj.put("ip", decryOBJ.isNull("IP") ? "" : decryOBJ.getString("IP"));
            obj.put("timeOut", decryOBJ.isNull("timeOut") ? "" : decryOBJ.getString("timeOut"));
            if (obj.getString("timeOut").isEmpty())
                UserActivity.timeOut = 110000;
            else UserActivity.timeOut = Integer.parseInt(obj.getString("timeOut"));
            Utils.saveToPreferences(context, PreferencesKeys.notificationData, obj.toString());
            Utils.saveToPreferences(context, PreferencesKeys.notificationTime, obj.getString("timeStamp"));
            return obj.isNull("timeStamp") ? "" : obj.getString("timeStamp");
        } catch (Exception e) {
            Logger.error(LOG + "notificationPayload(JSONObject decryOBJ, Context context)", e.getMessage());
            return "";
        }
    }

    public static void showAlertWhatsNew(Context context) {
        String message = "Alert message to notify users of new app update in AppStore.<br><br>" +
                "Users will get an update on what is new is current version.<br><br>"
                + "Denying a request now requires user to select a reason.<br><br>"
                + "RSA Encryption is integrated to ensure security while making requests to the app.";
//        String message = "<b>App update</b> <br>PlayStore app update is live app will show alert<br><br><b>What's New </b><br>After App Update showing what's New<br><br> " +
//                "<b>Denied</b> <br>notification Denied time select fraudulent or mistake denied";
        try {
            final android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(context);
            alertDialog.setTitle(context.getString(R.string.whats_new));
            alertDialog.setCancelable(false);
            alertDialog.setMessage(Html.fromHtml(message));
            alertDialog.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            android.app.AlertDialog builder = alertDialog.create();
            builder.show();
        } catch (Exception e) {

        }
    }


    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
            Logger.error("getLocalIpAddress()",ex.getMessage());
        }
        return null;
    }

    public static void companyImageUpdate(String pushType, boolean isAuthenticated, JSONObjectCallbackImage callback, String requestId, String userId, Context context, int deniedType, boolean isSameFace, String correlationId) {
        try {

            JSONObject obj = new JSONObject();
            obj.put("request_id", requestId);
            obj.put("user_id", userId);
            obj.put("status", isAuthenticated);
            // obj.put("PurposelyDenied", !isAuthenticated);
            // obj.put("isSameFace", isSameFace);
            obj.put("push_type", pushType);
            if (deniedType != 0)
                obj.put("denied_type", deniedType);
            obj.put("device_data", MobileDetailsNew(context));
            obj.put("authenitcate_hash", Utils.getHMacSecretKey(context));
            obj.put("request_signature", RSAKeypair.signData(Utils.readFromPreferences(context, PreferencesKeys.deviceUUid, ""), requestId, Utils.readFromPreferences(context, PreferencesKeys.privateKey, "")));
            obj.put("correlation_id", correlationId);

            new AsyncJSONObjectImageUpdate(obj, callback, ApplicationWrapper.BaseUrl(PushNotificationActivity.hostName, EndPoints.companyImageUpdate)).execute();

        } catch (Exception e) {
            Logger.error(LOG + "PushAuthenticationStatus(String userEmail, boolean isAuthenticated, JSONObjectCallback callback, String requestId)", e.getMessage());
        }
    }

    public static void closeApp(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }catch(Exception e){
            Logger.error(LOG,"close APP" +e.getMessage());
        }
    }


    public static void biometricLogin(Context context,String act) {
        try {

            executor = ContextCompat.getMainExecutor(context);
            biometricPrompt = new BiometricPrompt( ((SplashActivity)context),
                    executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode,
                                                  @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Logger.debug("deep error","onAuthenticationError"+errorCode);

                    if(errorCode==BiometricConstants.ERROR_USER_CANCELED){
                        Utils.saveToPreferences(context, PreferencesKeys.appLockpref,false);
                        Utils.closeApp(context);
                        ((Activity)context).finish();
                    }else if(errorCode== BiometricConstants.ERROR_TIMEOUT){
                        Utils.saveToPreferences(context, PreferencesKeys.appLockpref,false);
                        Utils.closeApp(context);
                        ((Activity)context).finish();
                    }else if(errorCode==BiometricConstants.ERROR_NO_DEVICE_CREDENTIAL){
                        Utils.saveToPreferences(context,PreferencesKeys.appLock,false);
                        ((Activity)context).finish();
                    }else if(errorCode==BiometricConstants.ERROR_NO_BIOMETRICS){
                        Utils.saveToPreferences(context,PreferencesKeys.appLock,false);
                        ((Activity)context).finish();
                    }else if(errorCode==BiometricConstants.ERROR_HW_NOT_PRESENT || errorCode==BiometricConstants.ERROR_HW_UNAVAILABLE){
                        Utils.saveToPreferences(context,PreferencesKeys.appLock,false);
                        ((Activity)context).finish();
                    }
                }

                @Override
                public void onAuthenticationSucceeded(
                        @NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Utils.saveToPreferences(context,PreferencesKeys.appLockpref,true);
                    ((Activity)context).finish();
                    Logger.debug("deep SplashActivity","onAuthenticationSucceeded"+result);

                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Utils.closeApp(context);
                    ((Activity)context).finish();
                    Logger.debug("deep SplashActivity","onAuthenticationFailed");

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
            Logger.error(LOG, e.getMessage());
        }


    }

}
