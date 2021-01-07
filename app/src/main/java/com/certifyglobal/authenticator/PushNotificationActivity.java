package com.certifyglobal.authenticator;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.certifyglobal.callback.JSONObjectCallback;
import com.certifyglobal.callback.JSONObjectCallbackImage;
import com.certifyglobal.callback.JSONObjectCallbackSetting;
import com.certifyglobal.utils.Base32String;
import com.certifyglobal.utils.EndPoints;
import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.PreferencesKeys;
import com.certifyglobal.utils.Utils;
import com.zwsb.palmsdk.PalmSDK;
import com.zwsb.palmsdk.helpers.SharedPreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;

import static com.certifyglobal.authenticator.UserActivity.ACTION_IMAGE_SAVED;


public class PushNotificationActivity extends AppCompatActivity implements JSONObjectCallback,JSONObjectCallbackImage {
    public static String TAG = "PushNotificationActivity - ";
    private String pushType;
    private String userName;
    private String requestId;
    //  private String companyId;
    private String userId;
    private String faceSettings;
    private boolean liveliness;
    private RelativeLayout rlMessage;
    private TextView tvMessage;
    private Dialog dialog;
    private boolean statusBoolean;
    private int timeCountInMilliSeconds = 110000;
    private int timeOut;
    private CountDownTimer countDownTimer;
    public static boolean authenticationBoolean;
    public static boolean authenticationBooleanError = false;
    private String timeStamp = "";
    public static String hostName = "";

    private TextView tvYes;
    private TokenPersistence mTokenPersistence;
    private String user;
    private String oldLabel = "";
    private String correlationId = "";
    private String version = "";
    private boolean isExist = false;
    String companyID = "";
    String companyName = "";
    String role = "";
    String userVersion = "";
    String SettingVersion = "";
    private LinearLayout push_layout;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.push_notification_new);
            TextView tvTitle = findViewById(R.id.tv_title);
            TextView tvComName = findViewById(R.id.tv_com_name);
            TextView tvUserType = findViewById(R.id.tv_user_type);
            TextView tvUser = findViewById(R.id.tv_user);
            TextView tvApplication = findViewById(R.id.tv_application);
            ImageView imageMachine = findViewById(R.id.image_machine);
            ImageView imageLocation = findViewById(R.id.image_location);
            TextView tvMachineName = findViewById(R.id.tv_machine_name);
            TextView tvIp = findViewById(R.id.tv_ip_address);
            TextView tvAddress = findViewById(R.id.tv_address);
            TextView tvDate = findViewById(R.id.tv_date);
            TextView tvTime = findViewById(R.id.tv_time);
            ImageView imageLogo = findViewById(R.id.image_logo);
            ImageView image_back = findViewById(R.id.img_ic_back);
            image_back.setVisibility(View.GONE);
            rlMessage = findViewById(R.id.rl_message);
            tvMessage = findViewById(R.id.tv_message_notification);
            push_layout = findViewById(R.id.push_layout);
            tvYes = findViewById(R.id.tv_yes);
            TextView tvNo = findViewById(R.id.tv_no);
            tvTitle.setText(getResources().getString(R.string.login_request));
            Intent intent = getIntent();
            rlMessage.setVisibility(View.GONE);
            isExist = false;
            //if (intent.getStringExtra("inUser") != null) {
            Utils.saveToPreferences(this, PreferencesKeys.notificationCount, 0);
            Utils.saveToPreferences(this, PreferencesKeys.notificationData, "");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.cancelAll();
            //}

            if (intent.getStringExtra("companyName").trim().isEmpty())
                tvComName.setVisibility(View.GONE);
            tvComName.setText(intent.getStringExtra("companyName"));
            tvUserType.setText(intent.getStringExtra("userType"));
            tvUser.setText(userName = intent.getStringExtra("user"));
            String ipAddress = intent.getStringExtra("ip");

            StringBuffer address = new StringBuffer();
            String city = intent.getStringExtra("city").trim();
            String state = intent.getStringExtra("state").trim();
            String country = intent.getStringExtra("country").trim();
            timeStamp = intent.getStringExtra("timeStamp").trim();
            hostName = intent.getStringExtra("hostName");
            tvApplication.setText(intent.getStringExtra("applicationName").trim());
            liveliness = Boolean.parseBoolean(intent.getStringExtra("liveliness") == null || intent.getStringExtra("liveliness").isEmpty() ? "false" : intent.getStringExtra("liveliness").toLowerCase());
            if (intent.getStringExtra("timeOut") == null || intent.getStringExtra("timeOut").isEmpty())
                timeOut = 110000;
            else timeOut = Integer.parseInt(intent.getStringExtra("timeOut"));
            timeOut = timeOut * 1000;
            timeCountInMilliSeconds = Utils.getTimerTimeStamp(timeStamp, timeOut);
            if (!city.trim().isEmpty() && !state.trim().isEmpty() && !country.trim().isEmpty())
                address.append(city).append(", ").append(state).append(", ").append(country);
            else if (country.isEmpty() && (!city.isEmpty() && !state.isEmpty()))
                address.append(city).append(", ").append(state);
            else if (!country.isEmpty() && !city.isEmpty())
                address.append(city).append(", ").append(country);
            tvAddress.setText(address.toString());
            tvIp.setText(ipAddress);
            if (address.toString().isEmpty() && ipAddress.trim().isEmpty())
                imageLocation.setVisibility(View.GONE);
            Utils.setServerTime(timeStamp, tvDate, tvTime);
            pushType = intent.getStringExtra("pushType");
            requestId = intent.getStringExtra("requestId");
            correlationId = intent.getStringExtra("correlationId");
            version = intent.getStringExtra("Version");
            companyName = intent.getStringExtra("companyName");
            userVersion = intent.getStringExtra("UserVersion");
            SettingVersion = intent.getStringExtra("SettingVersion");
            role = intent.getStringExtra("userType");
            String GuidUserId = intent.getStringExtra("userId") == null ? "" : intent.getStringExtra("userId");
            String[] values = GuidUserId.split(":");
            userId = values[0];
            String oldId = values.length > 1 ? values[1] : "";
            Utils.PermissionRequest(PushNotificationActivity.this, Utils.permission.location);
            this.runOnUiThread(new Runnable() {
                public void run() {
                    mTokenPersistence = new TokenPersistence(PushNotificationActivity.this);
                    int position = -1;
                    String temp = "";
                    String oldtemp = "";
                    for (int i = 0; i < mTokenPersistence.length(); i++) {
                        Token tokenTemp = mTokenPersistence.get(i);
                        if (tokenTemp.getLabel().contains(userId)) {
                            position=i;
                            isExist = true;
                            try {
                                if ((!userVersion.equals(Utils.readFromPreferences(PushNotificationActivity.this, PreferencesKeys.userVersion, ""))) || !SettingVersion.equals(Utils.readFromPreferences(PushNotificationActivity.this, PreferencesKeys.imageVersion, ""))) {
                                    String label = String.format("%s|%s|%s|0|%s|%s", companyName, userName, role, userId, hostName);
                                    oldtemp = String.format("otpauth://totp/%s:%s?secret=%s&digits=6&period=30", tokenTemp.getIssuer(), label, tokenTemp.getSecret());
                                    addTokenAndFinish(oldtemp, position);
                                    Utils.saveToPreferences(PushNotificationActivity.this, PreferencesKeys.userVersion, userVersion);
                                    getLatestIcon();
                                }
                            }catch (Exception e){
                                Logger.error(TAG,"user version null");
                            }
                            break;
                        }
                    }
                    if (!isExist) {
                        for (int i = 0; i < mTokenPersistence.length(); i++) {
                            Token tokenTemp = mTokenPersistence.get(i);
                            if (tokenTemp.getLabel().contains(oldId)) {
                                position = i;

                                String[] labelU = tokenTemp.getLabel().split("\\|");
                                String companyName = labelU.length >= 1 ? labelU[0] : "";
                                String userName = labelU.length >= 2 ? labelU[1] : "";
                                String role = labelU.length >= 3 ? labelU[2] : "";
                                String label = String.format("%s|%s|%s|0|%s|%s", companyName, userName, role, userId, hostName);
                                temp = String.format("otpauth://totp/%s:%s?secret=%s&digits=6&period=30", tokenTemp.getIssuer(), label, tokenTemp.getSecret());
                                Logger.debug("mTokenPersistencenewwwwwww", temp);
                                Logger.debug("mTokenPersistenceoldddddddddddd", tokenTemp.toString());
                                //  mTokenPersistence.d
                                break;
                            }
                        }
                        if (position == -1)
                            addTokenAndFinish(temp, position);
                    }

                    if (!isExist && position == -1) {
                        finish();
                    }
                }
            });


            String machineName = intent.getStringExtra("machineName");
            if (machineName.trim().isEmpty()) {
                imageMachine.setVisibility(View.GONE);
            }
            tvMachineName.setText(machineName);
            byte[] enrolledFace = ApplicationWrapper.getMdbCompanyAdapter().readCompanyLogo(hostName);
            if (enrolledFace == null) {
                imageLogo.setVisibility(View.GONE);
            } else {
                imageLogo.setVisibility(View.VISIBLE);
                imageLogo.setImageBitmap(BitmapFactory.decodeByteArray(enrolledFace, 0, enrolledFace.length));
            }
            faceSettings = intent.getStringExtra("faceSettings");
            if (EndPoints.deployment == EndPoints.Mode.Local)
                Logger.debug(TAG, "userName : " + userName + ", requestId = " + requestId + ", pushType : " + pushType + " liveliness " + liveliness + "  hostName   " + hostName);
            switch (pushType) {
                case "6"://normal push
                    tvYes.setText(getResources().getString(R.string.approve));
                    tvYes.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_approve, 0, 0);
                    tvNo.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_deny, 0, 0);
                    tvTitle.setText(getResources().getString(R.string.login_request));
                    break;
                case "2":
                case "5":
                    tvYes.setEnabled(true);
                    //  String faceSetting = intent.getStringExtra("companyId") == null ? "" : intent.getStringExtra("companyId");
                    try {
                        if (!authenticationBoolean)
                            Utils.keyValidations(null, this);
                    } catch (Exception ignore) {
                    }
                    Utils.saveToPreferences(this, PreferencesKeys.faceLiveliness, liveliness);
                    if (pushType.equals("2"))
                        tvTitle.setText(getResources().getString(R.string.face_enrollment_request));
                    else
                        tvTitle.setText(getResources().getString(R.string.face_login_request));
                    tvYes.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_face_auth, 0, 0);
                    Utils.cameraPermission(PushNotificationActivity.this);
                    break;
                case "3":
                case "4":
                    Utils.cameraPermission(PushNotificationActivity.this);
                    SharedPreferenceHelper.setLivenessCheck(this, SharedPreferenceHelper.LIVENESS_CHECK_KEY, true);
                    if (!authenticationBoolean) {
                        tvYes.setEnabled(false);
                        ButterKnife.bind(this);
                        palmSDK();
                    }
                    if (pushType.equals("4"))
                        tvTitle.setText(getResources().getString(R.string.palm_enrollment_request));
                    else
                        tvTitle.setText(getResources().getString(R.string.palm_login_request));
                    tvYes.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_palm, 0, 0);
                    break;
                default:
                    setResultUI(getResources().getString(R.string.push_error), getResources().getColor(R.color.orange), R.drawable.ic_deny);
                    break;
            }
            startCountDownTimer();
            tvYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (!Utils.isConnectingToInternet(PushNotificationActivity.this)) {
                            Logger.toast(PushNotificationActivity.this, getResources().getString(R.string.network_error));
                            return;
                        }
                        statusBoolean = true;
                        Intent palmIntent;
                        switch (pushType) {//normal push
                            case "6":
                                try {
                                    if (ContextCompat.checkSelfPermission(PushNotificationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&  ContextCompat.checkSelfPermission(PushNotificationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                                        dialog = new Dialog(PushNotificationActivity.this);
                                        dialog = Utils.showDialog(dialog, PushNotificationActivity.this);
                                        if (dialog != null) dialog.show();
                                        Utils.PushAuthenticationStatus(pushType, true, PushNotificationActivity.this, requestId, userId, PushNotificationActivity.this, 0, true, correlationId);
                                        //  push_layout.setVisibility(View.GONE);
                                        // finish();
                                        break;
                                    }else {
                                        Utils.PermissionRequest(PushNotificationActivity.this, Utils.permission.location);
                                    }

                                }catch (Exception e){
                                    Logger.error(TAG,e.getMessage());
                                }
                            case "2"://face
                            case "5":
                                if (authenticationBooleanError) {
                                    Logger.toast(PushNotificationActivity.this, getResources().getString(R.string.lic_error));
                                    return;
                                }
                                if (ContextCompat.checkSelfPermission(PushNotificationActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(PushNotificationActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&  ContextCompat.checkSelfPermission(PushNotificationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&  ContextCompat.checkSelfPermission(PushNotificationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    Intent livePreIntent = new Intent(PushNotificationActivity.this, LivePreviewActivity.class);
                                    livePreIntent.putExtra("type", "Face");
                                    livePreIntent.putExtra("pushType", pushType);
                                    livePreIntent.putExtra("user", userName);
                                    livePreIntent.putExtra("requestId", requestId);
                                    livePreIntent.putExtra("userId", userId);
                                    livePreIntent.putExtra("faceSettings", faceSettings);
                                    livePreIntent.putExtra("correlationId", correlationId);
                                    livePreIntent.putExtra("version", version);
                                    //   livePreIntent.putExtra("userId", userId);
                                    startActivity(livePreIntent);
                                    finish();
                                }else{
                                    Utils.PermissionRequest(PushNotificationActivity.this, Utils.permission.camera_phone);
                                }
                                break;
                            case "3"://palm
                            case "4":
                                if (authenticationBooleanError) {
                                    Logger.toast(PushNotificationActivity.this, getResources().getString(R.string.lic_error));
                                    return;
                                }
                                if (ContextCompat.checkSelfPermission(PushNotificationActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(PushNotificationActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&  ContextCompat.checkSelfPermission(PushNotificationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&  ContextCompat.checkSelfPermission(PushNotificationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                                    palmIntent = new Intent(PushNotificationActivity.this, PalmValidations.class);
                                    palmIntent.putExtra("pushType", pushType);
                                    palmIntent.putExtra("user", userName);
                                    palmIntent.putExtra("requestId", requestId);
                                    palmIntent.putExtra("userId", userId);
                                    palmIntent.putExtra("timeStamp", timeStamp);
                                    palmIntent.putExtra("timeOut", timeOut);
                                    palmIntent.putExtra("correlationId", correlationId);
                                    palmIntent.putExtra("hostName", hostName);
                                    startActivity(palmIntent);
                                    finish();
                                    break;
                                }else{
                                    Utils.PermissionRequest(PushNotificationActivity.this, Utils.permission.camera_phone);
                                }
                        }
                    } catch (Exception e) {
                        Logger.error(TAG + "tvYes - > onClick(View v)", e.getMessage());
                    }
                }
            });
            tvNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        DenyingDialog();
                    } catch (Exception e) {
                        Logger.error(TAG + "tvNo - > onClick(View v)", e.getMessage());
                    }
                }
            });
            TokenPersistence mTokenPersistence = new TokenPersistence(this);
            boolean accountStatus = false;
            for (int i = 0; i < mTokenPersistence.length(); i++) {
                Token tokenTemp = mTokenPersistence.get(i);
                if (tokenTemp.getLabel().contains(userId)) {
                    accountStatus = true;
                    break;
                }
            }
            if (!accountStatus)
                Utils.PushAuthenticationStatus(pushType, false, PushNotificationActivity.this, requestId, userId, PushNotificationActivity.this, 0, true, correlationId);

        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }
    private void getLatestIcon() {
        try {
         //   if (!SettingVersion.equals(Utils.readFromPreferences(PushNotificationActivity.this, PreferencesKeys.imageVersion, ""))) {
                Utils.companyImageUpdate(pushType, true, PushNotificationActivity.this, requestId, userId, PushNotificationActivity.this, 0, true, correlationId);
                Utils.saveToPreferences(PushNotificationActivity.this, PreferencesKeys.imageVersion, SettingVersion);
           // }
        }catch (Exception e){
            Logger.error("version null",e.getMessage());
        }

    }

    private void addTokenAndFinish(String temp, int pos) {
        try {
            Token token = new Token(temp);
            new TokenPersistence(this).delete(pos);
            if (new TokenPersistence(this).tokenExists(token)) {
                TokenPersistence mTokenPersistence = new TokenPersistence(this);
                for (int i = 0; i < mTokenPersistence.length(); i++) {
                    Token tokenTemp = mTokenPersistence.get(i);
                    if (tokenTemp.getLabel().equals(token.getLabel())) {
                        new TokenPersistence(this).delete(i);
                        break;

                    }
                }
            }

            TokenPersistence.saveAsync(this, token);
        } catch (Exception e) {
            Logger.error(" addTokenAndFinish(String temp)", e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListener(JSONObject report, String status, JSONObject req) {
        try {

            Logger.debug("push api response", report.toString());

            if (report == null) {
                setResultUI(getResources().getString(R.string.denied), getResources().getColor(R.color.orange), R.drawable.ic_deny);
                return;
            }
            String requestIdServer = report.isNull("request_id") ? "" : report.getString("request_id");
            if (report.isNull("ExceptionMessage")) {
                if (!report.isNull("response_text")) {
                    Logger.toast(PushNotificationActivity.this, report.getString("response_text"));
                    setResultUI(getResources().getString(R.string.denied), getResources().getColor(R.color.orange), R.drawable.ic_deny);
                } else if (statusBoolean && requestId.equals(requestIdServer)) {
                    setResultUI(getResources().getString(R.string.authenticated), getResources().getColor(R.color.green), R.drawable.ic_approve);
                } else {
                    setResultUI(getResources().getString(R.string.denied), getResources().getColor(R.color.orange), R.drawable.ic_deny);

                }
            } else {
                setResultUI(getResources().getString(R.string.denied), getResources().getColor(R.color.orange), R.drawable.ic_deny);
                Logger.toast(PushNotificationActivity.this, report.getString("ExceptionMessage"));
            }

        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
            if (dialog != null) dialog.dismiss();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void faceSettingCall() {

        try {


//            SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
//            boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUN", true);
//            String versionPref=wmbPreference.getString("version","");
//            if(isFirstRun) {
//                if (!version.equals(versionPref)) {
//                    Utils.faceSetting(this, requestId, userId, this);
//
//                    //api brightness
//                    SharedPreferences.Editor editor = wmbPreference.edit();
//                   // editor.putBoolean("FIRSTRUN", false);
//                    editor.putString("version", version);
//                    editor.commit();
//                } else {
//
//
//                }
//            }


        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }

    public void palmSDK() {
        try {
            PalmSDK.init(this, "certifyglobal-ge5", new PalmSDK.InitSDKCallback() {
                @Override
                public void onSuccess() {
                    authenticationBoolean = true;
                    PushNotificationActivity.authenticationBooleanError = false;
                    tvYes.setEnabled(true);
                }

                @Override
                public void onError() {
                    Logger.toast(PushNotificationActivity.this, getResources().getString(R.string.palm_dev_error));
                    finish();
                    //  onSuccessListener(null);
                }
            });
        } catch (Exception e) {
            Logger.toast(PushNotificationActivity.this, getResources().getString(R.string.palm_dev_error));
            finish();
            Logger.error(TAG + "palmSDK()", e.getMessage());
        }
    }

    private void setResultUI(String message, int backGroundColor, int drawableIcon) {
        try {
            rlMessage.setVisibility(View.VISIBLE);
            PushNotificationActivity.this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            rlMessage.setBackgroundColor(backGroundColor);
            tvMessage.setText(message);
            tvMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(0, drawableIcon, 0, 0);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    if (dialog != null) dialog.dismiss();
                    Intent  intent=new Intent(PushNotificationActivity.this,SplashActivity.class);
                    intent.putExtra("push",true);
                    startActivity(intent);
                  //  startActivity(new Intent(PushNotificationActivity.this, SplashActivity.class));
                }
            }, 1000);

        } catch (Exception e) {
            Logger.error(TAG + "setResultUI(String message, int backGroundColor, int drawableIcon)", e.getMessage());
        }
    }

    private void startCountDownTimer() {
        try {
            countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
//                    setResultUI(getResources().getString(R.string.time_out), getResources().getColor(R.color.orange), R.drawable.ic_deny);
//                    sendDenying(3);

                    new AlertDialog.Builder(PushNotificationActivity.this)
                            .setTitle(Html.fromHtml("<b>" + "Authentication Timeout" + "</b>"))
                            .setMessage("The authentication could not be completed. Please try again.")
                            .setCancelable(false)

                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    sendDenying(3);
                                  /*  Intent  intent=new Intent(PushNotificationActivity.this,SplashActivity.class);
                                    intent.putExtra("push",true);
                                    startActivity(intent);*/
                                }
                            })
//                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }

            }.start();
            //countDownTimer.start();
        } catch (Exception e) {
            Logger.error(TAG + "startCountDownTimer()", e.getMessage());
        }
    }

    private void DenyingDialog() {
        try {
            final Dialog dialog = new Dialog(PushNotificationActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.denying_dialog);
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            if (dialog.getWindow() == null) return;
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.setCancelable(false);
            dialog.getWindow().setLayout(displayMetrics.widthPixels - 20, LinearLayout.LayoutParams.WRAP_CONTENT);
            TextView tvFraudulent = dialog.findViewById(R.id.tv_fraudulent);
            TextView tvMistake = dialog.findViewById(R.id.tv_mistake);
            TextView tvCancel = dialog.findViewById(R.id.tv_cancel);
            tvFraudulent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendDenying(1);
                    dialog.cancel();
                }
            });
            tvMistake.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendDenying(2);
                    dialog.cancel();
                }
            });
            tvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });
            dialog.show();

        } catch (Exception e) {
            Logger.error(TAG + "DenyingDialog()", e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            countDownTimer.cancel();
            dialog.dismiss();
        } catch (Exception e) {
            Logger.error(TAG + "onDestroy()", e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        try {
            countDownTimer.cancel();
        } catch (Exception e) {
            Logger.error(TAG + "onBackPressed()", e.getMessage());
        }
    }

    private void sendDenying(int type) {
        try {
            statusBoolean = false;
            if (Utils.isConnectingToInternet(PushNotificationActivity.this)) {
                dialog = new Dialog(PushNotificationActivity.this);
                dialog = Utils.showDialog(dialog, PushNotificationActivity.this);
                if (dialog != null) dialog.show();
                Utils.PushAuthenticationStatus(pushType, false, PushNotificationActivity.this, requestId, userId, PushNotificationActivity.this, type, true, correlationId);
            } else
                Logger.toast(PushNotificationActivity.this, getResources().getString(R.string.network_error));
        } catch (Exception e) {
            Logger.error(TAG + "sendDenying(int type)", e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListenerImage(String report, String status, JSONObject req) {
        try {
            if (report == null) return;
            byte[] imageBytes=null;
            JSONObject json1=null;
            try {
                String formatedString = report.substring(1, report.length() - 1);
                json1 = new JSONObject(formatedString.replace("\\",""));

            }catch (Exception e){
                e.printStackTrace();
                json1 = new JSONObject(report);
            }

            if (json1.getInt("response_code") == 1) {
                JSONObject objJson = json1.getJSONObject("user_data");
                String companyName = objJson.isNull("company_name") ? "" : objJson.getString("company_name");
                if (!objJson.isNull("company_icon") && !objJson.getString("company_icon").isEmpty()) {
                   imageBytes = Base64.decode(objJson.getString("company_icon"), Base64.DEFAULT);
//                    UserActivity.RefreshListBroadcastReceiver refreshListBroadcastReceiver=new UserActivity.RefreshListBroadcastReceiver();
//                    registerReceiver(refreshListBroadcastReceiver, new IntentFilter(ACTION_IMAGE_SAVED));
                }
                ApplicationWrapper.getMdbCompanyAdapter().insertCompany(hostName, userId, companyName, imageBytes);

            }
        } catch (JSONException e) {
            Logger.error(TAG, e.getMessage());
        }
    }
}
