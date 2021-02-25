package com.certifyglobal.authenticator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.certifyglobal.authenticator.facedetection.FaceDetectionProcessor;
import com.certifyglobal.callback.JSONObjectCallback;
import com.certifyglobal.utils.AESCrypt;
import com.certifyglobal.utils.EndPoints;
import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.Utils;
import com.innovatrics.iface.Face;
import com.innovatrics.iface.FaceHandler;
import com.microsoft.appcenter.analytics.Analytics;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class ImageActivity extends AppCompatActivity implements JSONObjectCallback {
    private String TAG = "ImageActivity - ";
    private String pushType;
    private String user;
    private String requestId;
    private String correlationId;
    private String userId;
    private final static String FACE_AES_CRYPT = "FaceAesCrypt";
    private boolean pushNotificationValidation = true;
    private LinearLayout llBg;
    private TextView tvMessage;
    ByteArrayOutputStream byteArrayImage;
    private boolean isSameFace = true;
    private boolean isFirstTime = false;
    private String defaultText = "AuthX";
    private byte[] facesByteTemplate;
    private View includeProgress;
    private ImageView img_test;
    FaceHandler faceHandler;
    float matchingConfidence = 0;
    boolean localMatch=false;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            ImageActivity.this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            isSameFace = true;
            isFirstTime = false;
            setContentView(R.layout.activity_image);
            llBg = findViewById(R.id.ll_bg);
            tvMessage = findViewById(R.id.tv_message);
            img_test = findViewById(R.id.img_test);
            includeProgress = findViewById(R.id.include_progress);
            Intent intentGet = getIntent();
            if (intentGet.getStringExtra("user") != null) {
                user = intentGet.getStringExtra("user");
                pushType = intentGet.getStringExtra("pushType");
                requestId = intentGet.getStringExtra("requestId");
                correlationId = intentGet.getStringExtra("correlationId");
                userId = intentGet.getStringExtra("userId") == null ? "" : intentGet.getStringExtra("userId");
                if (FaceDetectionProcessor.timeout) {
                    setValues(-1);
                } else if (FaceDetectionProcessor.imageBitmap == null)
                    Utils.PushAuthenticationStatus(pushType, false, this, requestId, userId, this, 0, true,correlationId);
                else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                                faceHandler = new FaceHandler();
                                byteArrayImage = new ByteArrayOutputStream();
                                FaceDetectionProcessor.imageBitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayImage);
                                Face[] faces = faceHandler.detectFaces(byteArrayImage.toByteArray(), 30, 200, 1);
                                facesByteTemplate = faces[0].createTemplate();
                                byte[] enrolledFace = null;
                                // ArrayList<byte[]> multipleFaces=null;
                                HashMap<String, byte[]> multipleFaces = null;
                                multipleFaces = ApplicationWrapper.getDBIFaceAdapter().readIFaceData("");

                                if (multipleFaces.size() == 0) {
                                    sendReq(byteArrayImage.toByteArray());
                                } else {
                                    byte[] oldImage;
                                    try {
                                        //image and template
                                        if (pushType.equals("5")) {
                                            //  enrolledFace = ApplicationWrapper.getDBIFaceAdapter().readIFaceData(userId);//user
                                            //  multipleFaces = ApplicationWrapper.getDBIFaceAdapter().readIFaceData(userId);//user
                                            if(localMatch) {//regular flow
                                                if (multipleFaces.get(userId) != null) {
                                                    oldImage = AESCrypt.decrypt(FACE_AES_CRYPT, multipleFaces.get(userId));
                                                    faceValidation(oldImage);//multipleFaces.get(userId);
                                                    isSameFace = pushNotificationValidation;
                                                    Utils.PushAuthenticationStatus(pushType, pushNotificationValidation, ImageActivity.this, requestId, userId, ImageActivity.this, 0, isSameFace, correlationId);
                                                } else {
                                                    sendReq(byteArrayImage.toByteArray());
                                                }
                                            }else if(pushType.equals("5") && localMatch==false){//matching image in server api
                                                sendReq(byteArrayImage.toByteArray());

                                            }

                                        } else {
                                            if (multipleFaces.get(userId) == null) {
                                                for (byte[] value : multipleFaces.values()) {
                                                    oldImage = AESCrypt.decrypt(FACE_AES_CRYPT, value);
                                                    faceValidation(oldImage);
                                                    if (!pushNotificationValidation) {
                                                        break;
                                                    }
                                                }
                                                sendReq(byteArrayImage.toByteArray());
                                            } else {
                                                oldImage = AESCrypt.decrypt(FACE_AES_CRYPT, multipleFaces.get(userId));
                                                faceValidation(oldImage);
                                                if (pushNotificationValidation)
                                                    sendReq(byteArrayImage.toByteArray());
                                                else {
                                                    isSameFace = pushNotificationValidation;
                                                    Utils.PushAuthenticationStatus(pushType, pushNotificationValidation, ImageActivity.this, requestId, userId, ImageActivity.this, 0, isSameFace, correlationId);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        Logger.error(TAG, e.getMessage());
                                    }
                                    if (EndPoints.deployment == EndPoints.Mode.Local)
                                        Logger.debug("Image Size", byteArrayImage.size() + " matchingConfidence " + matchingConfidence);
                                }

                        }
                    });
                }
            }
        } catch (Exception e) {
            setValues(0);
            Logger.error(TAG + "onCreate(Bundle savedInstanceState)", e.getMessage());
        }
    }

    private void faceValidation(byte[] oldImage) {
        ByteArrayOutputStream oldByte = new ByteArrayOutputStream();
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(oldImage);
        Bitmap oldBitmap = BitmapFactory.decodeStream(arrayInputStream);
        oldBitmap.compress(Bitmap.CompressFormat.JPEG, 100, oldByte);
        ByteArrayOutputStream oldByteArrayImage = new ByteArrayOutputStream();
        oldBitmap.compress(Bitmap.CompressFormat.JPEG, 30, oldByteArrayImage);
        Face[] oldFaces = faceHandler.detectFaces(oldByteArrayImage.toByteArray(), 30, 200, 1);
        matchingConfidence = faceHandler.matchTemplate(facesByteTemplate, oldFaces[0].createTemplate());
        pushNotificationValidation = matchingConfidence > 100;
    }

    public void sendReq(byte[] byteArray) {
        try {
            if (Utils.isConnectingToInternet(this)) {
                includeProgress.setVisibility(View.VISIBLE);
                Utils.PushFace(user, byteArray, this, requestId, userId, pushType, this,correlationId);
            } else
                Logger.toast(ImageActivity.this, getResources().getString(R.string.network_error));
        } catch (Exception e) {
            Logger.error("sendReq(byte[] byteArray)", e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListener(JSONObject report, String status,JSONObject req) {
        try {
            includeProgress.setVisibility(View.GONE);
            tvMessage.setVisibility(View.VISIBLE);
            if (report == null) {
                setValues(-1);
                return;
            }
//            if (!report.isNull("ExceptionMessage")) {
//                Logger.toast(ImageActivity.this, report.getString("ExceptionMessage"));
//            }
           /* if (!report.isNull("Message")) {
                Logger.toast(ImageActivity.this, report.getString("Message"));
                setValues(0);
            } else*/ if (!report.isNull("response_code")) {
                if (!isSameFace) {
                    Analytics.trackEvent(getResources().getString(R.string.enrollment_failed)+"URL:"+status+"Response:"+report.toString());
                    tvMessage.setText(pushType.equals("2") ? getResources().getString(R.string.enrollment_failed) : getResources().getString(R.string.denied));
                    llBg.setBackgroundColor(getResources().getColor(R.color.orange));
                    tvMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_deny, 0, 0);
                } else {
                    int code = report.getInt("response_code");
                    String requestIdServer = report.isNull("request_id") ? "" : report.getString("request_id");
                    if (code == 1 && requestId.equals(requestIdServer)) {
                        llBg.setBackgroundColor(getResources().getColor(R.color.green));
                        tvMessage.setText(pushType.equals("5") ? getResources().getString(R.string.authenticated) : getResources().getString(R.string.enrollment_successful));

                        if (pushType.equals("5") && !report.isNull("face_bio") && localMatch==true) {
                            byte[] imageBytes = Base64.decode(report.getString("face_bio"), Base64.DEFAULT);
                            //template
                          //  FaceHandler faceHandler = new FaceHandler();
                           // Face[] facesServer = faceHandler.detectFaces(imageBytes, 30, 200, 1);
                            ApplicationWrapper.getDBIFaceAdapter().insertIFace(userId, AESCrypt.encryptFace(FACE_AES_CRYPT, imageBytes));
                        } else if (pushType.equals("2")) {
                            ApplicationWrapper.getDBIFaceAdapter().insertIFace(userId, AESCrypt.encryptFace(FACE_AES_CRYPT, byteArrayImage.toByteArray()));
                        }else if(pushType.equals("5") && localMatch==false){
                            setValues(code);
                        }
                    } else {
                        setValues(code);
                        String[] endPoint=status.split(".com/");

                        Map<String, String> properties = new HashMap<>();
                        for(Iterator<String> iter = req.keys(); iter.hasNext();) {
                            String key = iter.next();
                            String value = req.optString(key);
                            properties.put(key,value);
                        }
                        properties.put("URL:",status);
                        properties.put("Response:",report.toString());
                        Analytics.trackEvent(endPoint[1], properties);
                    }
                }
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    Intent  intent=new Intent(ImageActivity.this,SplashActivity.class);
                    intent.putExtra("push",true);
                    startActivity(intent);
                }
            }, 2000);

        } catch (Exception e) {
            Logger.error("onJSONObjectListener(JSONObject report, String status)", e.getMessage());
        }
    }

    public void setValues(int code) {
        if (code == 1) {
            llBg.setBackgroundColor(getResources().getColor(R.color.green));
            tvMessage.setText(pushType.equals("5") ? getResources().getString(R.string.authenticated) : getResources().getString(R.string.enrollment_successful));
        } else {
            if (code == -1) {
                tvMessage.setText(getResources().getString(R.string.time_out));
            } else if (code == 0)
                tvMessage.setText(getResources().getString(R.string.internal_error));
            else if (code == 2)
                tvMessage.setText(getResources().getString(R.string.user_not_exists));// tvMessage.setText(getResources().getString(R.string.face_not_found));
            else if (code == 3)
                tvMessage.setText(getResources().getString(R.string.face_quality_poor));
            else if (code == 4)
                tvMessage.setText(getResources().getString(R.string.user_not_exists));
            else if (code == 5)
                tvMessage.setText(getResources().getString(R.string.user_already_exists));
            else if (code == 6)
                tvMessage.setText(getResources().getString(R.string.spoofing_found_face));
            else if (code == 7)
                tvMessage.setText(getResources().getString(R.string.too_many_face_found));
            llBg.setBackgroundColor(getResources().getColor(R.color.orange));
            tvMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_deny, 0, 0);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                Intent  intent=new Intent(ImageActivity.this,SplashActivity.class);
                intent.putExtra("push",true);
                startActivity(intent);
            }
        }, 2000);

    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
