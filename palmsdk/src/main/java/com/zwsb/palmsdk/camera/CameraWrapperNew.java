package com.zwsb.palmsdk.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.RequiresApi;

import com.redrockbiometrics.palm.PalmFrame;
import com.redrockbiometrics.palm.PalmMatchingResultMessage;
import com.redrockbiometrics.palm.PalmMessage;
import com.redrockbiometrics.palm.PalmMessageEnum;
import com.redrockbiometrics.palm.PalmModelingResultMessage;
import com.redrockbiometrics.palm.PalmQuad;
import com.redrockbiometrics.palm.PalmStatus;
import com.redrockbiometrics.palm.PalmsDetectedMessage;
import com.zwsb.palmsdk.customViews.ScanView;
import com.zwsb.palmsdk.helpers.BaseUtil;
import com.zwsb.palmsdk.helpers.Constant;
import com.zwsb.palmsdk.helpers.SharedPreferenceHelper;
import com.zwsb.palmsdk.palmApi.PalmAPI;

import org.greenrobot.eventbus.EventBus;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.zwsb.palmsdk.helpers.Constant.RESOLUTION_RATIO_WIDTH;


public class CameraWrapperNew extends CameraWrapper {
    private CameraManager mCamManager;
    private CameraDevice mCamDevice;
    private String[] mCamIdList;
    private CameraCaptureSession mCameraCaptureSession;
    private ImageReader mImageReader;
    private Integer selectedReductionMode = CaptureRequest.NOISE_REDUCTION_MODE_OFF;
    private int useCamIx = 0;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    public CameraWrapperNew(Context context, TextureView surface, ScanView view, String userName) {
        this.mContext = context;
        this.textureView = surface;
        this.scanView = view;
        this.userName = userName;

        surface.setSurfaceTextureListener(this);

        /**
         * Common camera init actions
         */

        try {
            mCamManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            mCamIdList = mCamManager.getCameraIdList();
            for (int camIx = 0; camIx < mCamIdList.length; ++camIx) {
                final String camId = mCamIdList[camIx];
                final CameraCharacteristics camCharacter = mCamManager.getCameraCharacteristics(camId);

                /**
                 * Determine the direction this camera is facing.
                 */
                final int lens = camCharacter.get(CameraCharacteristics.LENS_FACING);
                if (lens == CameraMetadata.LENS_FACING_FRONT) {
                    Log.i(BaseUtil.LOG_TAG, camId + ": FRONT CAMERA");
                    if (Constant.DEFAULT_CAMERA == Constant.CameraFacing.FRONT) {
                        useCamIx = camIx;
                    }
                } else if (lens == CameraMetadata.LENS_FACING_BACK) {
                    Log.i(BaseUtil.LOG_TAG, camId + ": REAR CAMERA");
                    if (Constant.DEFAULT_CAMERA == Constant.CameraFacing.BACK) {
                        useCamIx = camIx;
                    }
                } else {
                    Log.i(BaseUtil.LOG_TAG, camId + ": EXTERNAL CAMERA");
                }

                /**
                 * Determine what level of hardware support there is for the manual settings.
                 */
                final int level = camCharacter.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                if (level == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_3) {
                    Log.i(BaseUtil.LOG_TAG, camId + ": FULL+3");
                } else if (level == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL) {
                    Log.i(BaseUtil.LOG_TAG, camId + ": FULL");
                } else if (level == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED) {
                    Log.i(BaseUtil.LOG_TAG, camId + ": LIMITED");
                } else {
                    Log.i(BaseUtil.LOG_TAG, camId + ": NONE");
                }

                /**
                 * Check to see if some of the important settings are available.
                 */
                final int[] caps = camCharacter.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
                for (int cap : caps) {
                    if (cap == CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR) {
                        Log.i(BaseUtil.LOG_TAG, "  " + camId + ": HAS MANUAL SENSOR");
                    } else if (cap == CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW) {
                        Log.i(BaseUtil.LOG_TAG, "  " + camId + ": HAS RAW");
                    } else if (cap == CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS) {
                        Log.i(BaseUtil.LOG_TAG, "  " + camId + ": HAS READ SENSOR");
                    }
                }

                /**
                 * Determine if the camera has an adjustable focus.
                 */
                final Float minDist = camCharacter.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
                if (minDist == null || minDist <= 0.0) {
                    Log.i(BaseUtil.LOG_TAG, "  FIXED FOCUS");
                } else {
                    Log.i(BaseUtil.LOG_TAG, "  ADJUSTABLE FOCUS: " + minDist);
                    final Integer calib = camCharacter.get(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION);
                    if (calib != null && calib == CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_CALIBRATED) {
                        Log.i(BaseUtil.LOG_TAG, "  LENS CALIBRATION: CALIBRATED");
                    } else if (calib != null && calib == CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_APPROXIMATE) {
                        Log.i(BaseUtil.LOG_TAG, "  LENS CALIBRATION: APPROXIMATE");
                    } else {
                        Log.i(BaseUtil.LOG_TAG, "  LENS CALIBRATION: UNCALIBRATED");
                    }
                }

                /**
                 * Determine the smallest ISO level that maxes out the analog gain.
                 */
                final Integer maxISO = camCharacter.get(CameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY);
                if (maxISO != null) {
                    Log.i(BaseUtil.LOG_TAG, "  MAX ANALOG ISO: " + maxISO);
                }

                ArrayList<Integer> reductionModes = new ArrayList<>();
                for (int mode : camCharacter.get(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES)) {
                    Log.i(BaseUtil.LOG_TAG, "AVAILABLE REDUCTION MODE: " + mode);
                    reductionModes.add(mode);
                }

                selectedReductionMode = CaptureRequest.NOISE_REDUCTION_MODE_OFF;
            }
        } catch (CameraAccessException cameraAccessException) {
            cameraAccessException.printStackTrace();
            Log.e(BaseUtil.LOG_TAG, "CameraAccessException3");
        }

        mImageReader = ImageReader.newInstance(
                RESOLUTION_RATIO_WIDTH,
                Constant.RESOLUTION_RATIO_HEIGHT,
                ImageFormat.YUV_420_888, /* maxImages */ 2);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onSurfaceTextureAvailable(final SurfaceTexture surfaceTexture, int width, int height) {
        this.surfaceTexture = surfaceTexture;
        surfaceTexture.setDefaultBufferSize(Constant.RESOLUTION_RATIO_WIDTH, Constant.RESOLUTION_RATIO_HEIGHT);

        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture textureView, int width, int height) {
        Log.i(BaseUtil.LOG_TAG, "SURFACE SizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture textureView) {
        Log.i(BaseUtil.LOG_TAG, "SURFACE Destroyed");
        surfaceTexture = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture textureView) {
        Log.i(BaseUtil.LOG_TAG, "SURFACE TextureUpdated");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onImageAvailable(ImageReader reader) {
            try (Image image = reader.acquireNextImage()) {
                Image.Plane[] planes = image.getPlanes();
                if (planes.length > 0) {
                    ByteBuffer buffer = planes[0].getBuffer();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);

                    PalmFrame frame = new PalmFrame(0, 0, 50, image.getWidth(), image.getHeight(), 8, data);
                    PalmAPI.m_PalmBiometrics.ProcessFrame(frame);
                    PalmMessage message = PalmAPI.m_PalmBiometrics.WaitMessage();

                    // Carefully loop through the messages in the queue until it is empty.
                    if (message == null) {
                        Message resultMessage = palmMessageHandler.obtainMessage();
                        resultMessage.obj = new PalmMessage(PalmMessageEnum.None);
                        palmMessageHandler.sendMessage(resultMessage);
                    } else {
                        while (message != null) {
                            Message resultMessage = palmMessageHandler.obtainMessage();
                            resultMessage.obj = message;
                            palmMessageHandler.sendMessage(resultMessage);

                            message = PalmAPI.m_PalmBiometrics.WaitMessage();
                        }
                    }
                }
            }
        }
    };

    /**
     * Handler for process PalmMessage on UI Thread
     */
    Handler palmMessageHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            PalmMessage message = (PalmMessage) msg.obj;

            if (message != null) {
                if (message.status != PalmStatus.Success) {
                    if (message.status != null) {
                        CameraEvent event = CameraEvent.ON_ERROR;
                        event.message = message;
                        EventBus.getDefault().post(event);
                    }
                } else {
                    switch (message.type) {
                        /**
                         * Just palm detected event, draw circle where the palm is
                         */
                        case None:
                            scanView.clear(true);
                            break;
                        case PalmsDetected:
                            PalmQuad quad = ((PalmsDetectedMessage) message).palms[0].quad;

                            float newCenterX = (quad.cx + quad.ax) / 2;
                            float newCenterY = (quad.cy + quad.ay) / 2;

                            if (centerX != newCenterX && centerY != newCenterY) {
                                double diag2 = (quad.cx - quad.ax) * (quad.cx - quad.ax) + (quad.cy - quad.ay) * (quad.cy - quad.ay);
                                double halfEdge = Math.sqrt(diag2) / 2.0;

                                centerX = newCenterX;
                                centerY = newCenterY;

                                scanView.centerX = centerX;
                                scanView.centerY = centerY;
                                scanView.disableClear();

                                float x = (RESOLUTION_RATIO_WIDTH - centerY) * BaseUtil.matrixWidth;
                                float y = (Constant.RESOLUTION_RATIO_HEIGHT - centerX) * BaseUtil.matrixHeight;
                                float radius = ((float) halfEdge) * (BaseUtil.matrixWidth - 0.2f);

                                //Log.i(BaseUtil.LOG_TAG, "CENTER_X = " + x + " CENTER_Y = " + y);

                                scanView.reDrawGesture(x, y, radius);
                                //FIXME: Renable metering
                                //CamParUtil.setMetering(camera, (int)x, (int)y);

                                clearHandler.removeMessages(0);
                            }

                            clearHandler.sendEmptyMessageDelayed(0, 200);

                            break;
                        /**
                         * Compare received data with saved user profile
                         */
                        case MatchingResult:
                            PalmMatchingResultMessage matchResult = (PalmMatchingResultMessage) message;
                            boolean isMatch = matchResult.result;
                            float score = matchResult.score;
                            Log.d("deep", "deep Matching result new:" + isMatch + "  Score:" + score);


                            atleastOneMatched = isMatch || atleastOneMatched;
                            bestMatchScore = Math.max(bestMatchScore, score);
                            if (++counter == SharedPreferenceHelper.getNumberOfRegisteredPalms(mContext, userName)) {
                                if (atleastOneMatched) {
                                    CameraEvent e = CameraEvent.ON_SCAN_SUCCESS;
                                    e.score = bestMatchScore;
                                    EventBus.getDefault().post(e);
                                    Log.d("deep score success",""+bestMatchScore);

                                } else {
                                    CameraEvent e = CameraEvent.ON_SCAN_FAILURE;
                                    e.score = bestMatchScore;
                                    EventBus.getDefault().post(e);
                                    Log.d("deep score fail",""+bestMatchScore);

                                }
                                counter = 0;
                                atleastOneMatched = false;
                                bestMatchScore = 0.0f;
                            }
                            break;
                        /**
                         * Save new user data
                         */
                        case ModelingResult:
                            System.out.println("modeling result");
                            byte[] modelResultData = ((PalmModelingResultMessage) message).data;
                            PalmAPI.saveModel(mContext, modelResultData, userName);
                            System.out.println("deep result data"+modelResultData.toString());
                            EventBus.getDefault().post(CameraEvent.ON_SAVE_PALM_SUCCESS.setData(message));
                            break;
                        case LivenessResult:
                            EventBus.getDefault().post(CameraEvent.ON_LIVENESS_CHECK_RESULT.setData(message));
                            break;
                        case LivenessStarted:
                                EventBus.getDefault().post(CameraEvent.ON_LIVENESS_STARTED);
                            break;
                        case LivenessFinished:
                            EventBus.getDefault().post(CameraEvent.ON_LIVENESS_FINISHED);
                            break;
                        default:
                            scanView.reDrawGesture(0, 0, 0);
                            break;
                    }
                }
            }
            return true;
        }
    });

    Handler clearHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (centerX == scanView.centerX && centerY == scanView.centerY) {
                scanView.clear(true);
            }
            return true;
        }
    });

    final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(final CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();

            mCamDevice = cameraDevice;
            Surface surface = new Surface(surfaceTexture);

            try {
                final CaptureRequest.Builder captureBuilder = mCamDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                captureBuilder.addTarget(surface);

                final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
                    }

                    @Override
                    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    }
                };

                captureBuilder.addTarget(mImageReader.getSurface());

                // Here, we create a CameraCaptureSession for camera preview.
                mCamDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                        new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                                mCameraCaptureSession = cameraCaptureSession;
                                if (null == mCamDevice) {
                                    Log.e(BaseUtil.LOG_TAG, "The camera is already closed");
                                } else {
                                    try {
                                        // Set the focus to fixed manual at ~15cm.
                                        captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                                        captureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, 7.0f); // (1.0 / 7.0) = 14.2cm

                                        // Disable any noise correction so we can get a sharper image.
                                        captureBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, selectedReductionMode);

                                        // Set ISO to the smallest level that maximizes the analog gain.
                                        final CameraCharacteristics camCharacter = mCamManager.getCameraCharacteristics(mCamDevice.getId());
                                        final Integer maxISO = camCharacter.get(CameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY);
                                        if (maxISO != null) {
                                            captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, maxISO);
                                            Log.i(BaseUtil.LOG_TAG, "Set ISO to " + maxISO);
                                        }

                                        // Finally, we start displaying the camera preview.
                                        CaptureRequest captureRequest = captureBuilder.build();
                                        mCameraCaptureSession.setRepeatingRequest(captureRequest, captureCallback, null);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Log.e(BaseUtil.LOG_TAG, "CameraAccessException2", e);
                                    }
                                }
                            }

                            @Override
                            public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                                Log.e(BaseUtil.LOG_TAG, "onConfigureFailed");
                            }
                        }, null
                );
            } catch (Exception e) {
                Log.e(BaseUtil.LOG_TAG, "onOpened failed", e);
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCamDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            Log.e(BaseUtil.LOG_TAG, "onError " + error);
            cameraDevice.close();
            mCamDevice = null;
        }
    };

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        if (!Constant.PermissionRequest(mContext, Constant.permission.camera)) {
            try {
                if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException("Time out waiting to lock camera opening.");
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mCamManager.openCamera(mCamIdList[useCamIx], stateCallback, mBackgroundHandler);
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
                Log.e(BaseUtil.LOG_TAG, "CameraAccessException1");
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
            }
        } else {
            Log.e(BaseUtil.LOG_TAG, "CAMERA permission rejected");
        }
    }

    public void startPreview(int startPreview) {
        startBackgroundThread();

        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(this);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void stopPreview() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCameraCaptureSession) {
                mCameraCaptureSession.close();
                mCameraCaptureSession = null;
            }
            if (null != mCamDevice) {
                mCamDevice.close();
                mCamDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }

        stopBackgroundThread();

        Log.i(BaseUtil.LOG_TAG, "CAMERA_CLOSED");
    }
}
