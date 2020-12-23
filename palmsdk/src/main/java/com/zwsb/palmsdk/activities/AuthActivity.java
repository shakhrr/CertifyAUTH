package com.zwsb.palmsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.redrockbiometrics.palm.PalmLivenessResultMessage;
import com.redrockbiometrics.palm.PalmModelingResultMessage;
import com.zwsb.palmsdk.R;
import com.zwsb.palmsdk.camera.CameraWrapper;
import com.zwsb.palmsdk.camera.CameraWrapperNew;
import com.zwsb.palmsdk.camera.CameraWrapperOld;
import com.zwsb.palmsdk.customViews.CircleAnimationView;
import com.zwsb.palmsdk.customViews.GradientView;
import com.zwsb.palmsdk.customViews.ScanCircleGradientView;
import com.zwsb.palmsdk.customViews.ScanView;
import com.zwsb.palmsdk.helpers.BaseUtil;
import com.zwsb.palmsdk.helpers.Constant;
import com.zwsb.palmsdk.helpers.SharedPreferenceHelper;
import com.zwsb.palmsdk.palmApi.PalmAPI;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;


public class AuthActivity extends AppCompatActivity {

    public static final int ON_SCAN_RESULT_OK = 42;
    public static final int ON_SCAN_RESULT_ERROR = 43;

    public static final int ON_CLOSE_RESULT_CODE = 11;

    public static final String USER_ACTION_KEY = "USER_ACTION_KEY";
    public static final String USER_NAME_KEY = "USER_NAME_KEY";
    public static final String IS_RIGHT_PALM_NEEDED_KEY = "IS_RIGHT_PALM_NEEDED_KEY";
    public static final String IS_LOCK_NEEDED_KEY = "IS_LOCK_NEEDED_KEY";
    public static final String IS_PIN_NEEDED_KEY = "IS_PIN_NEEDED_KEY";

    public static final int NEW_USER_ACTION = 0;
    public static final int READ_USER_ACTION = 1;
    public static final int TEST_ACTION = 2;
    private int timeCountInMilliSeconds = 110000;
    public static final int SCAN_FAILED_ANIMATION_DELAY = 1000;

    TextureView surfaceView;
    ScanView scanView;
    FrameLayout scanRootLayout;
    ImageView palmImageView;
    LinearLayout infoLayout;
    TextView testCapTextView;
    TextView palmTextView;

    ImageView closeButton;

    CircleAnimationView circleAnimationView;
    ScanCircleGradientView circleGradientView;
    GradientView gradientView;
    TextView resultTextView;
    TextView tv_front;
    TextView tv_back;
    FrameLayout scanInfoLayout;
    FrameLayout scanLayout;
    ImageView palmButton;
    private CountDownTimer countDownTimer;
    private static int cameraFacing = 1;

    private CameraWrapper cameraWrapper;

    private int userAction = NEW_USER_ACTION;
    private boolean isRightPalm = false;
    private String userName = "";
    private boolean isLockNeeded = false;
    private boolean isPinEnabled = false;
    public static String timeStamp = "";
    public static int timeOut = 0;

    /**
     * @param context      current context
     * @param isLockNeeded is onBackPressed() lock needed
     * @param userName     user id for image processing
     * @return intent for call startActivityForResult() with READ_USER_ACTION
     */
    public static Intent getIntent(Context context, String userName, boolean isLockNeeded, boolean isPinEnabled) {
        Intent intent = new Intent(context, AuthActivity.class);
        intent.putExtra(AuthActivity.USER_ACTION_KEY, AuthActivity.READ_USER_ACTION);
        intent.putExtra(AuthActivity.USER_NAME_KEY, userName);
        intent.putExtra(AuthActivity.IS_LOCK_NEEDED_KEY, isLockNeeded);
        intent.putExtra(AuthActivity.IS_PIN_NEEDED_KEY, isPinEnabled);
        return intent;
    }

    public static Intent getIntentForRightPalm(Context context, String userName) {
        Intent intent = new Intent(context, AuthActivity.class);
        intent.putExtra(AuthActivity.USER_ACTION_KEY, AuthActivity.NEW_USER_ACTION);
        intent.putExtra(AuthActivity.IS_RIGHT_PALM_NEEDED_KEY, true);
        intent.putExtra(AuthActivity.USER_NAME_KEY, userName);

        return intent;
    }

    public static Intent getIntentForLeftPalm(Context context, String userName) {
        Intent intent = new Intent(context, AuthActivity.class);
        intent.putExtra(AuthActivity.USER_ACTION_KEY, AuthActivity.NEW_USER_ACTION);
        intent.putExtra(AuthActivity.IS_RIGHT_PALM_NEEDED_KEY, false);
        intent.putExtra(AuthActivity.USER_NAME_KEY, userName);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_auth);
            surfaceView = findViewById(R.id.surfaceView);
            scanView = findViewById(R.id.scanView);
            scanRootLayout = findViewById(R.id.scanRootLayout);
            palmImageView = findViewById(R.id.palmImageView);
            infoLayout = findViewById(R.id.infoLayout);
            testCapTextView = findViewById(R.id.testCapTextView);
            palmTextView = findViewById(R.id.palmTextView);
            circleAnimationView = findViewById(R.id.circleAnimationView);
            circleGradientView = findViewById(R.id.circleGradientView);
            gradientView = findViewById(R.id.gradientView);
            resultTextView = findViewById(R.id.resultTextView);
            closeButton = findViewById(R.id.closeButton);
            scanInfoLayout = findViewById(R.id.scanInfoLayout);
            scanLayout = findViewById(R.id.scanLayout);
            palmButton = findViewById(R.id.palmButton);
            tv_front = findViewById(R.id.tv_front);
            tv_back = findViewById(R.id.tv_back);
            tv_front.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        tv_front.setBackgroundColor(getResources().getColor(R.color.orange));
                        tv_back.setBackgroundColor(getResources().getColor(R.color.transparent_color));
                        tv_back.setTextColor(getResources().getColor(R.color.black));
                        tv_front.setTextColor(getResources().getColor(R.color.white));
                        if (null != cameraWrapper) {
                            cameraWrapper.stopPreview();
                        }
                        cameraWrapper.startPreview(cameraFacing = 1);
                        Log.d("deep AuthActivity",""+cameraFacing);


                        //   init(1);  //cameraWrapper = getCameraWrapper(1);
                        // cameraWrapper.startPreview();
                    } catch (Exception e) {
                        Toast.makeText(AuthActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            tv_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        tv_back.setBackgroundColor(getResources().getColor(R.color.orange));
                        tv_front.setBackgroundColor(getResources().getColor(R.color.transparent_color));
                        tv_back.setTextColor(getResources().getColor(R.color.white));
                        tv_front.setTextColor(getResources().getColor(R.color.black));

                        try {
                            if (null != cameraWrapper) {
                                cameraWrapper.stopPreview();
                            }
                        } catch (Exception ignore) {

                        }
                        cameraWrapper.startPreview(cameraFacing = 0);
                        Log.d("deep AuthActivity",""+cameraFacing);

//                    cameraWrapper.stopPreview();
                        //   init(0);//   cameraWrapper = getCameraWrapper(0);
                        //      cameraWrapper.startPreview();
                    } catch (Exception e) {
                        cameraWrapper.startPreview(cameraFacing = 1);
                        Toast.makeText(AuthActivity.this, getResources().getString(R.string.palm_camera_error), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            if (Constant.PermissionRequest(AuthActivity.this, Constant.permission.camera_phone)) {
                Toast.makeText(this, R.string.permissions_alert, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                userAction = getIntent().getIntExtra(USER_ACTION_KEY, NEW_USER_ACTION);
                isRightPalm = getIntent().getBooleanExtra(IS_RIGHT_PALM_NEEDED_KEY, false);
                userName = getIntent().getStringExtra(USER_NAME_KEY);
                isLockNeeded = getIntent().getBooleanExtra(IS_LOCK_NEEDED_KEY, false);
                isPinEnabled = getIntent().getBooleanExtra(IS_PIN_NEEDED_KEY, false);

                if (userAction == READ_USER_ACTION && !SharedPreferenceHelper.isLockEnabled()) {
                    Intent intent = new Intent();
                    setResult(ON_SCAN_RESULT_OK, intent);
                    finish();
                }

                EventBus.getDefault().register(this);
                init();
                initPalmAPI();
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(AuthActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (countDownTimer != null)
            countDownTimer.cancel();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (null != cameraWrapper) {
                cameraWrapper.startPreview(1);
                Log.d("deep Auth 0back",""+cameraFacing);
            }
            timeCountInMilliSeconds = getTimerTimeStamp(timeStamp);
            startCountDownTimer();
        } catch (Exception ignore) {

        }
    }
    @Override
    protected void onPause() {
        if (null != cameraWrapper) {
            cameraWrapper.stopPreview();
        }
        super.onPause();
    }

    private void init() {
        try {
            BaseUtil.initScreenSize(this);
            ViewGroup.LayoutParams params = scanRootLayout.getLayoutParams();
            params.width = BaseUtil.screenWidth;
            params.height = (int) (BaseUtil.screenWidth * Constant.RESOLUTION_RATIO);
            scanRootLayout.setLayoutParams(params);

            cameraWrapper = getCameraWrapper();

            scanView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    scanView.reDrawGesture(scanView.getWidth() / 2, scanView.getY() / 2, 1);
                }
            });
        } catch (Exception ignore) {

        }
    }

    private CameraWrapper getCameraWrapper() {
        switch (CameraWrapper.getCameraSupportLevel(this)) {
            case NO_CAMERA2_ACCESS: {
                return new CameraWrapperOld(AuthActivity.this, surfaceView, scanView, userName);
            }

            case CAMERA2_NON_FULL: {
                return new CameraWrapperOld(AuthActivity.this, surfaceView, scanView, userName);
            }

            case CAMERA2_FULL: {
                return new CameraWrapperNew(AuthActivity.this, surfaceView, scanView, userName);
            }
        }

        return new CameraWrapperOld(AuthActivity.this, surfaceView, scanView, userName);
    }

    private void initPalmAPI() {
        switch (userAction) {

            case NEW_USER_ACTION:
                startModel();
                palmTextView.setVisibility(View.VISIBLE);
                infoLayout.setVisibility(View.VISIBLE);
                closeButton.setVisibility(View.GONE);

                if (isRightPalm) {
                    BaseUtil.CURRENT_PALM_PATH = BaseUtil.RIGHT_PALM_PATH;
                    palmTextView.setText(getString(R.string.right_palm));
                    palmImageView.setImageResource(R.drawable.left_palm);
                } else {
                    BaseUtil.CURRENT_PALM_PATH = BaseUtil.LEFT_PALM_PATH;
                    palmTextView.setText(getString(R.string.left_palm));
                    palmImageView.setImageResource(R.drawable.right_palm);
                }
                break;
            case READ_USER_ACTION:
                scanView.reDrawGesture(0, 0, 0);
                infoLayout.setVisibility(View.VISIBLE);
                //            pinButton.setVisibility(isPinEnabled ? View.VISIBLE : View.GONE);
                infoLayout.setVisibility(View.GONE);
                closeButton.setVisibility(View.GONE);
                palmTextView.setText("");

                new Thread() {
                    @Override
                    public void run() {
                        startMatch();
                    }
                }.start();
                break;
            case TEST_ACTION:
                scanView.reDrawGesture(0, 0, 0);
                infoLayout.setVisibility(View.VISIBLE);
                palmImageView.setImageResource(R.drawable.button_goto_palm);
                //            pinButton.setVisibility(isPinEnabled ? View.VISIBLE : View.GONE);
                infoLayout.setVisibility(View.GONE);
                closeButton.setVisibility(View.GONE);
                palmTextView.setText("");

                new Thread() {
                    @Override
                    public void run() {
                        startMatch();
                    }
                }.start();

                break;
        }
    }

    @Subscribe
    public void processCameraEvent(final CameraWrapper.CameraEvent event) {
        switch (event) {
            case ON_ERROR:
                //String status = (event.message != null && event.message.status != null) ? event.message.status.toString() : null;
                //Toast.makeText(getApplicationContext(), "ERROR " + status, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent();
                setResult(ON_SCAN_RESULT_ERROR, intent);
                finish();

                break;
            case ON_SCAN_FAILURE:
                onScanFailed();
                break;

            case ON_SCAN_SUCCESS:
                if (!SharedPreferenceHelper.getLivenessCheck(getApplicationContext(), SharedPreferenceHelper.LIVENESS_CHECK_KEY)) {
                    onScanSuccess();
                }
                break;
            case ON_LIVENESS_STARTED:
                if (SharedPreferenceHelper.getLivenessCheck(getApplicationContext(), SharedPreferenceHelper.LIVENESS_CHECK_KEY)) {
                    scanView.clear(true);
                    circleAnimationView.setFistStepColor();
                    resultTextView.setText(R.string.make_fist);
                    resultTextView.animate().alpha(1).start();
                }
                break;

            case ON_LIVENESS_CHECK_RESULT:
                if (SharedPreferenceHelper.getLivenessCheck(getApplicationContext(), SharedPreferenceHelper.LIVENESS_CHECK_KEY)) {
                    PalmLivenessResultMessage livenessResult = (PalmLivenessResultMessage) event.message;
                    if (livenessResult.result) {
                        onScanSuccess();
                    } else {
                        onScanFailed();
                    }
                }
                break;

            case ON_SAVE_PALM_SUCCESS:
                scanView.reDrawGesture(0, 0, 0);

                if (isRightPalm) {
                    SharedPreferenceHelper.setRightPalmEnabled(AuthActivity.this, true, userName);
                    SharedPreferenceHelper.setSavedPalmId(((PalmModelingResultMessage) event.message).modelID, SharedPreferenceHelper.RIGHT_PALM_ID_KEY, userName);
                    Log.d("deep right palm key","model idd:"+  ((PalmModelingResultMessage) event.message).modelID+ "RIGHT_PALM_ID_KEY"+ SharedPreferenceHelper.RIGHT_PALM_ID_KEY+ "username:"+userName);
                } else {
                    SharedPreferenceHelper.setLeftPalmEnabled(AuthActivity.this, true, userName);
                    SharedPreferenceHelper.setSavedPalmId(((PalmModelingResultMessage) event.message).modelID, SharedPreferenceHelper.LEFT_PALM_ID_KEY, userName);
                    Log.d("deep left palm key","model idd:"+  ((PalmModelingResultMessage) event.message).modelID+ "LEFT_PALM_ID_KEY"+ SharedPreferenceHelper.LEFT_PALM_ID_KEY+ "username:"+userName);
                }

                finish();
        }
    }

    private void startMatch() {
        PalmAPI.testMatch(AuthActivity.this, userName);
    }

    private void startModel() {
        PalmAPI.testModel();
    }

    private void onScanSuccess() {
        scanView.clear(true);
        circleAnimationView.setSuccess(true);
        resultTextView.setText(R.string.check_success);

        resultTextView.animate().alpha(1).start();

        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                Intent intent = new Intent();
                setResult(ON_SCAN_RESULT_OK, intent);
                finish();
                return true;
            }
        }).sendEmptyMessageDelayed(0, 2000);
    }

    private void onScanFailed() {
        scanView.clear(true);
        circleAnimationView.setSuccess(false);
        resultTextView.setText(R.string.check_failure);

        resultTextView.animate().alpha(1).start();

        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Intent intent = new Intent();
                setResult(ON_SCAN_RESULT_ERROR, intent);
                finish();

                return true;
            }
        }).sendEmptyMessageDelayed(0, 2000);
    }

    @Override
    public void onBackPressed() {
        if (!isLockNeeded) {
//            Intent intent = new Intent();
//            setResult(ON_CLOSE_RESULT_CODE, intent);
          //  this.finish();
            countDownTimer.cancel();
            try {
                Intent myIntent = new Intent(this,Class.forName("com.certifyglobal.authenticator.UserActivity"));
                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(myIntent );
               finishAndRemoveTask();
               finishAffinity();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
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
                    onScanFailed();
                }

            }.start();
            //countDownTimer.start();
        } catch (Exception e) {

        }
    }

    public int getTimerTimeStamp(String timestamp) {
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

            return (int) (temp > timeOut ? timeOut : temp);
        } catch (Exception e) {
        }
        return timeOut;
    }
}
