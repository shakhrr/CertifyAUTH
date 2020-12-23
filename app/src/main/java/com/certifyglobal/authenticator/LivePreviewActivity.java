
package com.certifyglobal.authenticator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.certifyglobal.authenticator.barcodescanning.BarcodeScanningProcessor;
import com.certifyglobal.authenticator.facedetection.FaceDetectionProcessor;
import com.certifyglobal.authenticator.facedetection.FocusView;
import com.certifyglobal.authenticator.facedetection.FocusViewCircle;
import com.certifyglobal.callback.Communicator;
import com.certifyglobal.callback.CommunicatorImage;
import com.certifyglobal.callback.JSONObjectCallback;
import com.certifyglobal.callback.JSONObjectCallbackSetting;
import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.PreferencesKeys;
import com.certifyglobal.utils.Utils;
import com.google.android.gms.common.annotation.KeepName;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Demo app showing the various features of ML Kit for Firebase. This class is used to
 * set up continuous frame processing on frames from a camera source.
 */
@KeepName
public final class LivePreviewActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback,
        OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener, Communicator, CommunicatorImage, JSONObjectCallbackSetting {
    private static final String FACE_DETECTION = "Face Detection";
    private static final String TEXT_DETECTION = "Text Detection";
    private static final String BARCODE_DETECTION = "Barcode Detection";
    private static final String IMAGE_LABEL_DETECTION = "Label Detection";
    private static final String CLASSIFICATION = "Classification";
    private static final String TAG = "LivePreviewActivity - ";
    private static final int PERMISSION_REQUESTS = 1;
    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private String selectedModel = BARCODE_DETECTION;
    private String pushType;
    private String user;
    private String requestId;
    private String correlationId;
    private String version;
    private String userId;
    private String faceSettings;
    private RelativeLayout rlRedDots;
    private TextView tvLeft;
    private TextView tvRight;
    private TextView tvBottomLeft;
    private TextView tvBottomRight;
    private TextView tvCenter;
    TextView tvCenterText;
    private String type;
    private FocusViewCircle fv_view_circle;
    private TokenPersistence mTokenPersistence;
    String companyID="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_live_preview);
            if (!Utils.readFromPreferences(LivePreviewActivity.this, PreferencesKeys.isLogin, false)) {
                Utils.getDeviceUUid(this);
                Utils.getNumberVersion(this);
                Utils.getHMacSecretKey(this);
            }
            preview = findViewById(R.id.firePreview);
            graphicOverlay = findViewById(R.id.fireFaceOverlay);
            FocusView fv_view = findViewById(R.id.fv_view);
            fv_view_circle = findViewById(R.id.fv_view_circle);
            tvCenterText = findViewById(R.id.tv_center_text);
            rlRedDots = findViewById(R.id.rl_red_dots);
            tvCenter = findViewById(R.id.tv_zero);
            tvLeft = findViewById(R.id.tv_left);
            tvRight = findViewById(R.id.tv_right);
            tvBottomLeft = findViewById(R.id.tv_bottom_left);
            tvBottomRight = findViewById(R.id.tv_bottom_right);
            Intent intentGet = getIntent();
            type = intentGet.getStringExtra("type");
            user = intentGet.getStringExtra("user");
            pushType = intentGet.getStringExtra("pushType");
            requestId = intentGet.getStringExtra("requestId");
            correlationId = intentGet.getStringExtra("correlationId");
            version = intentGet.getStringExtra("version");
            userId = intentGet.getStringExtra("userId") == null ? "" : intentGet.getStringExtra("userId");
            faceSettings = intentGet.getStringExtra("faceSettings");
            faceSettingCall();

            switch (type) {
                case "Barcode":
                case "BioSign":
                    selectedModel = BARCODE_DETECTION;
                    //        circleAnimationView.setVisibility(View.GONE);
                    fv_view.setVisibility(View.GONE);
                    tvCenterText.setVisibility(View.GONE);
                    rlRedDots.setVisibility(View.GONE);
                    break;
                case "Face":
                    selectedModel = FACE_DETECTION;
                    //    circleAnimationView.setFistStepColor();
                    fv_view.setVisibility(View.GONE);
                    tvCenterText.setVisibility(View.VISIBLE);
                    rlRedDots.setVisibility(View.GONE);
                    break;
            }

            if (!Utils.PermissionRequest(LivePreviewActivity.this, Utils.permission.camera)) {
                createCameraSource(selectedModel);
            } else {
                getRuntimePermissions();
            }


        } catch (Exception e) {
            Logger.error(TAG + "onCreate(Bundle savedInstanceState)", e.getMessage());
        }
    }

    private void faceSettingCall() {
        try {
            mTokenPersistence = new TokenPersistence(LivePreviewActivity.this);

            for (int i = 0; i < mTokenPersistence.length(); i++) {
                Token tokenTemp = mTokenPersistence.get(i);
                if (tokenTemp.getLabel().contains(userId)) {

                    String[] labelU = tokenTemp.getLabel().split("\\|");
                    companyID = labelU.length >= 4 ? labelU[3] : "";

                }
            }

            ArrayList<HashMap<String, String>>  face_setting_arraylist_temp = ApplicationWrapper.getMdbCompanyAdapter().getSettingList(companyID);

            if(face_setting_arraylist_temp.size()==0){
                Utils.faceSetting(this, requestId, userId, this);

            }
            for (int j=0;j<face_setting_arraylist_temp.size();j++){
                String companyIDdb=face_setting_arraylist_temp.get(j).get("userid");
                String versiondb=face_setting_arraylist_temp.get(j).get("version");
                if(!versiondb.equals(version) && companyIDdb.equals(companyID)){

                    Utils.faceSetting(this, requestId, userId, this);


                }
            }


        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }
    @Override
    public synchronized void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        selectedModel = parent.getItemAtPosition(pos).toString();
        preview.stop();
        if (!Utils.PermissionRequest(LivePreviewActivity.this, Utils.permission.camera)) {
            createCameraSource(selectedModel);
            startCameraSource();
        } else {
            getRuntimePermissions();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing.
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            } else {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            }
        }
        preview.stop();
        startCameraSource();
    }


    private void createCameraSource(String model) {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }

        try {
            switch (model) {
                case CLASSIFICATION:
                    //  cameraSource.setMachineLearningFrameProcessor(new CustomImageClassifierProcessor(this));
                    break;
                case TEXT_DETECTION:
                    //   cameraSource.setMachineLearningFrameProcessor(new TextRecognitionProcessor());
                    break;
                case FACE_DETECTION:
                    cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
                    cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor(this, this, tvCenterText, rlRedDots, tvCenter, tvLeft, tvBottomLeft, tvRight, tvBottomRight, fv_view_circle, Utils.readFromPreferences(LivePreviewActivity.this, PreferencesKeys.faceLiveliness, false),faceSettings,version,requestId,companyID));

                    break;
                case BARCODE_DETECTION:
                    cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
                    cameraSource.setMachineLearningFrameProcessor(new BarcodeScanningProcessor(this, type));
                    break;
                case IMAGE_LABEL_DETECTION:

                    //   cameraSource.setMachineLearningFrameProcessor(new ImageLabelingProcessor());
                    break;
                default:

            }
        } catch (Exception e) {
            createCameraSource(selectedModel);
            Log.e(TAG, "can not create camera source: " + model);
        }
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        allNeededPermissions.add(android.Manifest.permission.CAMERA);
       /* for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }
*/
        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        // Log.i(TAG, "Permission granted!");
//        if (allPermissionsGranted()) {
//            createCameraSource(selectedModel);
//        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void setAction(String data, int position) {
        try {
            switch (type) {
                case "Barcode":
                    Intent intent = new Intent(LivePreviewActivity.this, QRUrlScanResults.class);
                    intent.putExtra("Url", data);
                    startActivity(intent);
                    break;
                case "BioSign":
                    Intent intent2 = new Intent(LivePreviewActivity.this, BioSignActivity.class);
                    intent2.putExtra("Url", data);
                    startActivity(intent2);
                    break;
            }

            finish();
        } catch (Exception e) {
            Logger.error(TAG + "setAction(String data, int position)", e.getMessage());
        }
    }

    @Override
    public void setActionImage(byte[] bytes) {
        try {
            Intent intent = new Intent(LivePreviewActivity.this, ImageActivity.class);
            intent.putExtra("pushType", pushType);
            intent.putExtra("user", user);
            intent.putExtra("requestId", requestId);
            intent.putExtra("userId", userId);
            intent.putExtra("correlationId", correlationId);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Logger.error(TAG + "setActionImage(byte[] bytes)", e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListenerSetting(JSONObject report, String status, JSONObject req) {
        try {
            String faceSetting = report.isNull("response_data") ? "" : report.getString("response_data");
            Utils.StringToJSon(this, faceSetting, version, companyID,userId);
            Logger.debug("versionnnnnnnnnnnnnnnnnnnnnnnnnnn", version+companyID);

        }catch (Exception e){
            e.printStackTrace();
            Logger.error("onJSONObjectListenerSetting(JSONObject report, String status, JSONObject req)",e.getMessage());
        }
    }

}
