
package com.certifyglobal.authenticator.facedetection;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.certifyglobal.authenticator.ApplicationWrapper;
import com.certifyglobal.authenticator.GraphicOverlay;
import com.certifyglobal.authenticator.R;
import com.certifyglobal.authenticator.barcodescanning.VisionProcessorBase;
import com.certifyglobal.callback.CommunicatorImage;
import com.certifyglobal.pojo.FaceSettingInfo;
import com.certifyglobal.pojo.FrameMetadata;
import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.PreferencesKeys;
import com.certifyglobal.utils.Utils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.innovatrics.iface.Face;
import com.innovatrics.iface.FaceHandler;
import com.innovatrics.iface.TemplateInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Face Detector Demo.
 */
public class FaceDetectionProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {

    private static final String TAG = "FaceDetectionProcessor";
    private boolean isOnce = true;
    private boolean firstTime = true;
    private final FirebaseVisionFaceDetector detector;
    private CommunicatorImage communicator;
    public static Bitmap imageBitmap;
    private RelativeLayout rlRedDots;
    private String typeOf;
    private TextView tvLeft;
    private TextView tvRight;
    private TextView tvBottomLeft;
    private TextView tvBottomRight;
    private TextView tvCenter;
    private TextView tvErrorMessage;
    public static boolean timeout = false;
    private CountDownTimer countDownTimer;
    private CountDownTimer countDownTimerTimeOut;
    private int value = 1;
    private int faceTimeCount;
    private boolean faceLiveliness;
    private FocusViewCircle focusViewCircle;
    private FaceHandler faceHandler = new FaceHandler();
    private Face[] iFacesImage;
    private Context context;
    private static final int quality = -1;
    private static final int sharpness = 0;
    private static final int brightness = 1;
    private static final int contrast = 2;
    private static final int roll = 3;
    private static final int yam = 4;
    private static final int pitch = 5;
    private static final int confidence = 6;
    private static final int glassStatus = 7;
    private int errorPosition = -1;
    private String errorMsg = "";
    private ArrayList<FaceSettingInfo> faceSettingInfoArrayList;
    ArrayList<HashMap<String, String>> face_setting_arraylist = new ArrayList<>();
    ArrayList<HashMap<String, String>> face_setting_error_list = new ArrayList<>();

    public FaceDetectionProcessor(Context context, CommunicatorImage communicator, TextView tv_errorMessage, RelativeLayout rlRedDots, TextView tvCenter, TextView tvLeft, TextView tvLeftBottom, TextView tvRight, TextView tvRightBottom, FocusViewCircle focusViewCircle, boolean faceLiveliness, String faceSettings,String version,String requestID,String userId) {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        //.setTrackingEnabled(true)
                        .build();
        this.context = context;
        this.communicator = communicator;
        this.rlRedDots = rlRedDots;
        typeOf = "";
        faceTimeCount = 0;
        this.tvErrorMessage = tv_errorMessage;
        this.tvCenter = tvCenter;
        this.tvLeft = tvLeft;
        this.tvBottomLeft = tvLeftBottom;
        this.tvBottomRight = tvRightBottom;
        this.tvRight = tvRight;
        this.focusViewCircle = focusViewCircle;
        this.faceLiveliness = faceLiveliness;
        face_setting_arraylist.clear();
        face_setting_error_list.clear();
        face_setting_arraylist = ApplicationWrapper.getMdbCompanyAdapter().getSettingList(userId);
        face_setting_error_list = ApplicationWrapper.getMdbCompanyAdapter().getFaceError();
        Log.d(TAG, "face_setting_arraylist " + face_setting_arraylist.size());
        Log.d(TAG, "face_setting_error_list " + face_setting_error_list.size());

        timeout = false;
        imageBitmap = null;
        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
        startCountDownTimerTimeout();
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
        // if (focusViewCircle.getVisibility() == View.VISIBLE) {
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        image.getBitmap().compress(Bitmap.CompressFormat.JPEG, 30, blob); //getBitmapForDebugging()
        iFacesImage = faceHandler.detectFaces(blob.toByteArray(), 25, 200, 1);
        //  faceHandler.detectFacesAtPositions(blob.toByteArray(),5)
        int i;
        if (iFacesImage != null && iFacesImage.length > 0) {
            errorMessageText("");
            if (face_setting_arraylist.size() > 0) {
                boolean imageValidations;
                float[] attribute = Utils.getFaceAttribute(iFacesImage[0]);
                TemplateInfo tem = faceHandler.getTemplateInfo(iFacesImage[0].createTemplate());
                imageValidations = tem.getQuality() > 44;
                // validation face setting
                for (int f = 0; f < face_setting_arraylist.size(); f++) {
                    if (imageValidations) {
                        //  i = errorPosition = faceSettingInfoArrayList.get(f).position;
                        i = errorPosition = f;
                        errorMsg = face_setting_arraylist.get(f).get("name");

                        //    Logger.debug("141414141414141414", "333333333333333333" + attribute[i]);

                        //  imageValidations = faceSettingInfoArrayList.get(f).min < attribute[i] && attribute[i] < faceSettingInfoArrayList.get(f).max; // -5000 to -5000
                        imageValidations = Float.parseFloat(face_setting_arraylist.get(f).get("min")) < attribute[f] && attribute[f] < Float.parseFloat(face_setting_arraylist.get(f).get("max")); // -5000 to -5000
//                    switch (i) {
//                        case sharpness:
//                        case brightness:
//                        case contrast:
//                        case roll:
//                        case yam:
//                        case pitch:
//                            imageValidations = faceSettingInfoArrayList.get(f).min < attribute[i] && attribute[i] < faceSettingInfoArrayList.get(f).max; // -5000 to -5000
//                            break;
//                        case confidence:
//                            imageValidations = attribute[i] > faceSettingInfoArrayList.get(f).min;  // >600
//                            break;
//                        case glassStatus:
//                            imageValidations = attribute[i] < faceSettingInfoArrayList.get(f).min;
//                            break;
//                        case eyeDistance:
//                            imageValidations = faceSettingInfoArrayList.get(f).min < attribute[i] && attribute[i] < faceSettingInfoArrayList.get(f).max;
//                            break;
//                        case faceVerificationConfidence:
//                            imageValidations = faceSettingInfoArrayList.get(f).min < attribute[i] && attribute[i] < faceSettingInfoArrayList.get(f).max;
//                            break;

//
                        //                }
                    }
                }

                if (imageValidations) {
                    imageBitmap = image.getBitmap();
                    errorMessageText("");
                    //   startCountDownTimer();
                } /*else if (errorPosition == -1) {
                    //SetErrorMessage(tem.getQuality());
                    SetErrorMessagenew(-1,errorMsg);
                }*/ else {
                    //  SetErrorMessage((int) attribute[errorPosition]);
                    SetErrorMessagenew(tem.getQuality(), errorMsg);
                }
            } else {
                imageBitmap = image.getBitmap();
                errorMessageText("");
            }
        }

        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess(
            List<FirebaseVisionFace> faces,
            FrameMetadata frameMetadata,
            GraphicOverlay graphicOverlay) {
        try {
            graphicOverlay.clear();
            if (faces.size() == 0)
                errorMessageText(context.getResources().getString(R.string.face_not_found));
            else {
                FirebaseVisionFace face = faces.get(0);
                FaceGraphic faceGraphic = new FaceGraphic(graphicOverlay);
                graphicOverlay.add(faceGraphic);
                faceGraphic.updateFace(face, frameMetadata.getCameraFacing());
                if (firstTime) {
                    typeOf = "";
                    // focusViewCircle.setVisibility(View.VISIBLE);
                    if (!faceLiveliness) {
                        if (iFacesImage != null && iFacesImage.length >= 1 && imageBitmap != null) {
                            //  focusViewCircle.setVisibility(View.VISIBLE);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (isOnce) {
                                        countDownTimerTimeOut.cancel();
                                        communicator.setActionImage(null);
                                    }
                                    isOnce = false;
                                }
                            }, 200);
                        }
                    } else {
                        firstTime = false;
                        startCountDownTimer();
                    }
                }

//                if (iFacesImage != null && iFacesImage[0] != null && imageBitmap != null) {
//
//                    switch (typeOf) {
//                        case "left":
//                            leftFaceFeature = iFacesImage[0].getFeatures(FaceFeatureId.LEFT_EYEBROW_INNER_END);
//                            leftInt = leftFaceFeature[0].getPos().getX();
//                            break;
//                        case "bottomLeft":
//                            leftFaceFeature = iFacesImage[0].getFeatures(FaceFeatureId.LEFT_EYEBROW_INNER_END);
//                            bottomLeftInt = leftFaceFeature[0].getPos().getX();
//                            break;
//                        case "right":
//                            leftFaceFeature = iFacesImage[0].getFeatures(FaceFeatureId.RIGHT_EYEBROW_INNER_END);
//                            rightInt = leftFaceFeature[0].getPos().getX();
//                            break;
//                        case "bottomRight":
//                            leftFaceFeature = iFacesImage[0].getFeatures(FaceFeatureId.RIGHT_EYEBROW_INNER_END);
//                            bottomRightInt = leftFaceFeature[0].getPos().getX();
//                            break;
//                    }
//                }
                //  count++;
            }
        } catch (Exception e) {
            Logger.error(TAG + "onSuccess", e.getMessage());
        }

    }

    private void setViewGone() {
        try {
            this.tvCenter.setVisibility(View.GONE);
            this.tvLeft.setVisibility(View.GONE);
            this.tvBottomRight.setVisibility(View.GONE);
            this.tvBottomLeft.setVisibility(View.GONE);
            this.tvRight.setVisibility(View.GONE);
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }

    @Override
    protected void onFailure(Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }

    private void startCountDownTimer() {

        int timeCountInMilliSeconds = 5000;
        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                value = (int) (millisUntilFinished / 1000);
                if (value == 1) setDotPoints();
            }

            @Override
            public void onFinish() {
                value = 1;
            }

        }.start();
        countDownTimer.start();
    }

    private void setDotPoints() {
        try {
            float leftInt = 0, bottomLeftInt = 0, bottomRightInt = 0, rightInt = 0;
            if (!firstTime) {
                value = 0;
                switch (typeOf) {
                    case "face":
                        focusViewCircle.setVisibility(View.GONE);
                        rlRedDots.setVisibility(View.VISIBLE);
                        typeOf = "redDot";
                        break;
                    case "redDot":
                        typeOf = "left";
                        setViewGone();
                        tvLeft.setVisibility(View.VISIBLE);
                        break;
                    case "left":
                        typeOf = "bottomLeft";
                        setViewGone();
                        tvBottomLeft.setVisibility(View.VISIBLE);
                        break;
                    case "bottomLeft":
                        typeOf = "right";
                        setViewGone();
                        tvRight.setVisibility(View.VISIBLE);
                        break;
                    case "right":
                        typeOf = "bottomRight";
                        setViewGone();
                        tvBottomRight.setVisibility(View.VISIBLE);
                        break;
                    case "bottomRight":
                        Float leftValue = Math.abs(bottomLeftInt - leftInt);
                        float rightValue = Math.abs(bottomRightInt - rightInt);
                        if (leftValue > 10 || 10 < rightValue) {
                            typeOf = "finch";
                            if (isOnce) {
                                countDownTimerTimeOut.cancel();
                                communicator.setActionImage(null);
                            }
                            isOnce = false;
                            countDownTimer.cancel();
                        } else {
                            if (faceTimeCount >= 2) {
                                faceTimeCount = 0;
                                typeOf = "finch";
                                if (isOnce) {
                                    countDownTimerTimeOut.cancel();
                                    communicator.setActionImage(null);
                                }
                                isOnce = false;
                                countDownTimer.cancel();
                            } else {
                                setViewGone();
                                tvCenter.setVisibility(View.VISIBLE);
                                typeOf = "redDot";
                                faceTimeCount++;
                            }

                        }
                        break;
                    case "":
                        typeOf = "face";
                        break;
                }
                if (isOnce)
                    countDownTimer.start();
            }
        } catch (Exception e) {

        }
    }

    private void SetErrorMessage(int score) {
        try {
            String errorMessage = "";
            switch (errorPosition) {
                case quality:
                    errorMessage = context.getString(R.string.captured_image_quality_low);
                    break;
                case sharpness:
                    errorMessage = context.getString(Integer.parseInt("Face is very blur"));
                    break;
                case brightness:
                    if (score < Float.parseFloat(face_setting_arraylist.get(1).get("min"))) {
                        errorMessage = "Move towards light";
                    } else if (score > Float.parseFloat(face_setting_arraylist.get(1).get("max"))) {
                        errorMessage = "Move away from light,face is blur";
                    }
                case contrast:
                    if (score < Float.parseFloat(face_setting_arraylist.get(2).get("min"))) {
                        errorMessage = "Move towards light,face is blur";
                    } else if (score > Float.parseFloat(face_setting_arraylist.get(2).get("max"))) {
                        errorMessage = "Move away from light";
                    }
                    // errorMessage = score <Float.parseFloat(face_setting_arraylist.get(2).get("min")) ? "Move towards light,face is blur" : "Move away from light";
                    break;

                case roll:
                    errorMessage = "Please look straight at the camera";
                case yam:

                case pitch:
                    errorMessage = "Please look straight at the camera";
                    break;
                case confidence:
                    errorMessage = context.getString(R.string.poor_face_quality);
                    break;
                case glassStatus:
                    errorMessage = context.getString(R.string.please_remove_your_glasses);
                    break;
                default:
                    //  errorMessage = context.getString(R.string.captured_image_quality_low);
                    break;
            }

            errorMessageText(errorMessage);
        } catch (Exception e) {
            Logger.error(TAG + "SetErrorMessage(int score)", e.getMessage());
        }
    }

    private void SetErrorMessagenew(int score, String key) {
        try {
            String errorMessage = "";

            for (int i = 0; i < face_setting_error_list.size(); i++) {
                errorMessage = face_setting_error_list.get(i).get(key);

                if (errorMessage != null)

                    if (key.equals("MaxBrightness")) {

                        if (score < Float.parseFloat(face_setting_arraylist.get(i).get("min"))) {
                            errorMessage =  face_setting_error_list.get(i).get(key);//"Move towards light";
                        } else if (score > Float.parseFloat(face_setting_arraylist.get(i).get("max"))) {
                            errorMessage =  face_setting_error_list.get(i).get(key);//"Move away from light,face is blur";
                        }
                        break;
                    }  else if (key.equals("MinBrightness")) {
                        if (score < Float.parseFloat(face_setting_arraylist.get(i).get("min"))) {
                            errorMessage =face_setting_error_list.get(i).get(key);
                        } else if (score > Float.parseFloat(face_setting_arraylist.get(i).get("max"))) {
                            errorMessage = face_setting_error_list.get(i).get(key);
                        }
                        break;

                    }  else if (key.equals("MinContrast")) {
                        if (score < Float.parseFloat(face_setting_arraylist.get(i).get("min"))) {
                            errorMessage =face_setting_error_list.get(i).get(key);
                        } else if (score > Float.parseFloat(face_setting_arraylist.get(i).get("max"))) {
                            errorMessage = face_setting_error_list.get(i).get(key);
                        }
                        break;

                    }
                    else if (key.equals("MaxContrast")) {
                        if (score < Float.parseFloat(face_setting_arraylist.get(i).get("min"))) {
                            errorMessage =face_setting_error_list.get(i).get(key);
                        } else if (score > Float.parseFloat(face_setting_arraylist.get(i).get("max"))) {
                            errorMessage = face_setting_error_list.get(i).get(key);
                        }
                        break;

                    } else if (key.equals("MaxEyeDistance")) {
                        if (score < Float.parseFloat(face_setting_arraylist.get(i).get("min"))) {
                            errorMessage =face_setting_error_list.get(i).get(key);
                        } else if (score > Float.parseFloat(face_setting_arraylist.get(i).get("max"))) {
                            errorMessage = face_setting_error_list.get(i).get(key);
                        }
                        break;

                    }  else if (key.equals("MinEyeDistance")) {
                        if (score < Float.parseFloat(face_setting_arraylist.get(i).get("min"))) {
                            errorMessage =face_setting_error_list.get(i).get(key);
                        } else if (score > Float.parseFloat(face_setting_arraylist.get(i).get("max"))) {
                            errorMessage = face_setting_error_list.get(i).get(key);
                        }
                        break;

                    }  else {
                        errorMessage = face_setting_error_list.get(i).get(key);
                        break;

                    }

            }
            errorMessageText(errorMessage);
        } catch (Exception e) {
            Logger.error(TAG + "SetErrorMessage(int score)", e.getMessage());
        }
    }

    private void startCountDownTimerTimeout() {
        try {
            int timeCountInMilliSecondsTimeout = 100000;
            countDownTimerTimeOut = new CountDownTimer(timeCountInMilliSecondsTimeout, 1) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    timeout = true;
                    countDownTimerTimeOut.cancel();
                    communicator.setActionImage(null);
                }

            }.start();
            //countDownTimer.start();
        } catch (Exception e) {
            Logger.error(TAG + "startCountDownTimerTimeout()", e.getMessage());
        }
    }

    private void errorMessageText(final String errorMessage) {
        try {
            tvErrorMessage.post(new Runnable() {
                @Override
                public void run() {
                    tvErrorMessage.setText(errorMessage);
                }
            });
        } catch (Exception e) {
            Logger.error(TAG + "errorMessageText(final String errorMessage)", e.getMessage());
        }
    }
}
