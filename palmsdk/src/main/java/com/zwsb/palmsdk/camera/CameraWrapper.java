package com.zwsb.palmsdk.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.util.Log;
import android.view.TextureView;

import com.redrockbiometrics.palm.PalmMessage;
import com.zwsb.palmsdk.customViews.ScanView;
import com.zwsb.palmsdk.helpers.BaseUtil;

import static com.zwsb.palmsdk.camera.CameraWrapper.CameraSupportLevel.CAMERA2_FULL;
import static com.zwsb.palmsdk.camera.CameraWrapper.CameraSupportLevel.CAMERA2_NON_FULL;
import static com.zwsb.palmsdk.camera.CameraWrapper.CameraSupportLevel.NO_CAMERA2_ACCESS;


public abstract class CameraWrapper implements TextureView.SurfaceTextureListener {
    public enum CameraEvent {
        ON_SCAN_SUCCESS, ON_SCAN_FAILURE, ON_SAVE_PALM_SUCCESS, ON_LIVENESS_CHECK_RESULT, ON_LIVENESS_STARTED, ON_LIVENESS_FINISHED, ON_ERROR;

        public PalmMessage message;
        public float score = -1;

        public CameraEvent setData(PalmMessage palmMessage) {
            message = palmMessage;
            return this;
        }
    }

    protected SurfaceTexture surfaceTexture;

    public enum CameraSupportLevel {
        NO_CAMERA2_ACCESS("No access to camera2, used camera1 instead"),
        CAMERA2_FULL("Camera2 in SUPPORTED_HARDWARE_LEVEL_FULL mode, used camera1 instead"),
        CAMERA2_NON_FULL("Camera2 in non-full mode, used camera1");

        private final String name;

        CameraSupportLevel(String string) {
            name = string;
        }

        public String toString() {
            return this.name;
        }
    }


    public static CameraSupportLevel getCameraSupportLevel(Activity activity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

            try {
//                String neededCameraId = "";
//
//                for (String cameraId : manager.getCameraIdList()) {
//                    CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
//
//                    /**
//                     * Determine the direction this camera is facing.
//                     */
//                    final int lens = characteristics.get(CameraCharacteristics.LENS_FACING);
//                    if (lens == CameraMetadata.LENS_FACING_FRONT) {
//                        Log.i(BaseUtil.LOG_TAG, cameraId + ": FRONT CAMERA");
//                        if (Constant.DEFAULT_CAMERA == Constant.CameraFacing.FRONT) {
//                            neededCameraId = cameraId;
//                        }
//                    } else if (lens == CameraMetadata.LENS_FACING_BACK) {
//                        Log.i(BaseUtil.LOG_TAG, cameraId + ": REAR CAMERA");
//                        if (Constant.DEFAULT_CAMERA == Constant.CameraFacing.BACK) {
//                            neededCameraId = cameraId;
//                        }r
//                    } else {
//                        Log.i(BaseUtil.LOG_TAG, cameraId + ": EXTERNAL CAMERA");
//                    }
//                }

                CameraCharacteristics characteristics = manager.getCameraCharacteristics(String.valueOf(1));
                int hardwareLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                Log.i(BaseUtil.LOG_TAG, "CAMERA SUPPORT LEVEL = " + hardwareLevel);

                if (hardwareLevel == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL) {
                    return CAMERA2_FULL;
                } else {
                    return CAMERA2_NON_FULL;
                }
            } catch (CameraAccessException exception) {
                Log.e(BaseUtil.LOG_TAG, "getCameraSupportLevel CameraAccessException");
                return NO_CAMERA2_ACCESS;
            } catch (IllegalArgumentException illegalArgumentException) {
                Log.e(BaseUtil.LOG_TAG, "getCameraSupportLevel IllegalArgumentException");
                return NO_CAMERA2_ACCESS;
            }
        } else {
            return NO_CAMERA2_ACCESS;
        }
    }

    protected static final String HANDLER_THREAD_NAME = "HANDLER_THREAD_NAME";

    protected Context mContext = null;

    protected TextureView textureView = null;
    protected ScanView scanView = null;
    protected String userName;

    protected int counter = 0;
    protected boolean atleastOneMatched = false;
    protected float bestMatchScore = 0.0f;

    protected float centerX;
    protected float centerY;

    public abstract void startPreview(int cameraId);

    public abstract void stopPreview();
}
