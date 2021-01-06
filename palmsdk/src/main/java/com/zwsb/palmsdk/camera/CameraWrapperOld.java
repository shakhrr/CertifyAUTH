package com.zwsb.palmsdk.camera;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;

import androidx.appcompat.app.AlertDialog;

import com.redrockbiometrics.palm.PalmFrame;
import com.redrockbiometrics.palm.PalmMatchingResultMessage;
import com.redrockbiometrics.palm.PalmMessage;
import com.redrockbiometrics.palm.PalmMessageEnum;
import com.redrockbiometrics.palm.PalmModelingResultMessage;
import com.redrockbiometrics.palm.PalmQuad;
import com.redrockbiometrics.palm.PalmStatus;
import com.redrockbiometrics.palm.PalmsDetectedMessage;
import com.zwsb.palmsdk.R;
import com.zwsb.palmsdk.customViews.ScanView;
import com.zwsb.palmsdk.helpers.BaseUtil;
import com.zwsb.palmsdk.helpers.Constant;
import com.zwsb.palmsdk.helpers.SharedPreferenceHelper;
import com.zwsb.palmsdk.palmApi.PalmAPI;

import org.greenrobot.eventbus.EventBus;

import java.util.logging.Logger;

/**
 * Contains camera object, and manage textureView refresh events
 */
public class CameraWrapperOld extends CameraWrapper {
    public Camera camera;

    private HandlerThread handlerThread;
    private FrameHandler frameHandler;

    public CameraWrapperOld(Context context, TextureView surface, ScanView view, String userName) {
        this.mContext = context;
        this.textureView = surface;
        this.scanView = view;
        this.userName = userName;

        surface.setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        this.surfaceTexture = surfaceTexture;
        attachSurface();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture textureView) {
        if (camera != null) {
            /**
             * Call stopPreview() to stop updating the preview surface.
             */
            camera.stopPreview();
            surfaceTexture = null;
        }

        return true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture textureView, int width, int height) {
        Log.i(BaseUtil.LOG_TAG, "SIZE CHANGED");
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture textureView) {
    }

    /**
     * Attach attachSurface to camera preview
     */
    private void attachSurface() {
        try {
            if (camera != null && surfaceTexture != null) {
                camera.setPreviewTexture(surfaceTexture);
                camera.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startPreview(int cameraId) {
        handlerThread = new HandlerThread(HANDLER_THREAD_NAME);
        handlerThread.start();
        frameHandler = new FrameHandler(handlerThread.getLooper());

        try {
            /**
             * Init camera
             */
//            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//            int cameraCount = Camera.getNumberOfCameras();
//
//            for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
//                Camera.getCameraInfo(camIdx, cameraInfo);
//                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK &&
//                        Constant.DEFAULT_CAMERA == Constant.CameraFacing.BACK) {
//                   // safeCameraOpen(camIdx);
//                }
            safeCameraOpen(cameraId);
//                else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT &&
//                        Constant.DEFAULT_CAMERA == Constant.CameraFacing.FRONT) {
//                    safeCameraOpen(camIdx);
//                }
            //}

            /**
             * Nexus 6 fixes, need Camera2 API
             */
            if (Build.MODEL.equals("Nexus 6P")) {
                BaseUtil.cameraFlipped = true;
                camera.setDisplayOrientation(270);
            } else {
                BaseUtil.cameraFlipped = false;
                camera.setDisplayOrientation(90);
            }
            CamParUtil.initCamPar(camera);

            /**
             * Callback for receive frames, and process them
             */
            camera.setPreviewCallback(previewCallback);
            attachSurface();
        } catch (RuntimeException exception) {
            AlertDialog dialog = new AlertDialog.Builder(mContext).setMessage(R.string.camera_error).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create();

            dialog.show();

            textureView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }
    }

    @Override
    public void stopPreview() {
        if (camera != null) {
            CamParUtil.stopHandlers();
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();

            camera = null;
        }

        if (handlerThread != null) {
            handlerThread.quitSafely();
        }
    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;

        try {
            releaseCameraAndPreview();
            camera = Camera.open(id);
            qOpened = (camera != null);
        } catch (Exception e) {
            Log.e(BaseUtil.LOG_TAG, "failed to open Camera1");
            EventBus.getDefault().post(CameraEvent.ON_ERROR);
        }

        return qOpened;
    }

    private void releaseCameraAndPreview() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    /**
     * callback for process data from camera
     */
    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] bytes, final Camera camera) {
            Message frameDataMessage = frameHandler.obtainMessage();
            frameDataMessage.obj = bytes;
            frameHandler.sendMessage(frameDataMessage);
        }
    };

    /**
     * Handler for process single frame on non-UI thread
     */
    class FrameHandler extends Handler {
        public FrameHandler(Looper myLooper) {
            super(myLooper);
        }

        public void handleMessage(Message msg) {
            byte[] bytes = (byte[]) msg.obj;

            Camera.Size size = camera.getParameters().getPreviewSize();
            PalmFrame frame = PalmAPI.loadFrameFromNV21(bytes, size.width, size.height);
            if (PalmAPI.m_PalmBiometrics == null) return;
            PalmAPI.m_PalmBiometrics.ProcessFrame(frame);
            PalmMessage message = PalmAPI.m_PalmBiometrics.WaitMessage();

            /**
             Carefully loop through the messages in the queue until it is empty.
             */
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

    /**
     * Handler for process PalmMessage on UI Thread
     */
    Handler palmMessageHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            PalmMessage message = (PalmMessage) msg.obj;
            try {
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

                                    float x = (Constant.RESOLUTION_RATIO_WIDTH - centerY) * BaseUtil.matrixWidth;
                                    float y = (Constant.RESOLUTION_RATIO_HEIGHT - centerX) * BaseUtil.matrixHeight;
                                    float radius = ((float) halfEdge) * (BaseUtil.matrixWidth - 0.2f);

                                    Log.i(BaseUtil.LOG_TAG, "CENTER_X = " + x + " CENTER_Y = " + y);

                                    scanView.reDrawGesture(x, y, radius);
                                    CamParUtil.setMetering(camera, (int) x, (int) y);

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
                                Log.d("deep", "deep Matching result old:" + isMatch + "  Score:" + score);

                                atleastOneMatched = isMatch || atleastOneMatched;
                                bestMatchScore = Math.max(bestMatchScore, score);
                                if (++counter == SharedPreferenceHelper.getNumberOfRegisteredPalms(mContext, userName)) {
                                    if (atleastOneMatched) {
                                        CameraEvent e = CameraEvent.ON_SCAN_SUCCESS;
                                        e.score = bestMatchScore;
                                        EventBus.getDefault().post(e);
                                        Log.d("deep score success old",""+bestMatchScore);

                                    } else {
                                        CameraEvent e = CameraEvent.ON_SCAN_FAILURE;
                                        e.score = bestMatchScore;
                                        EventBus.getDefault().post(e);
                                        Log.d("deep score fail old",""+bestMatchScore);

                                    }
                                    Log.i(BaseUtil.LOG_TAG, "Matching result:" + isMatch + "  bestMatchScore:" + bestMatchScore);
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
                                System.out.println("deep old model"+modelResultData);
                                EventBus.getDefault().post(CameraEvent.ON_SAVE_PALM_SUCCESS.setData(message));
                                break;

                            case LivenessResult:
                                EventBus.getDefault().post(CameraEvent.ON_LIVENESS_CHECK_RESULT.setData(message));
                                break;
                            case LivenessStarted:
                                Log.i(BaseUtil.LOG_TAG, "Liveness started");
                                EventBus.getDefault().post(CameraEvent.ON_LIVENESS_STARTED);
                                break;
                            case LivenessFinished:
                                Log.i(BaseUtil.LOG_TAG, "Liveness finished");
                                EventBus.getDefault().post(CameraEvent.ON_LIVENESS_FINISHED);
                                break;
                            default:
                                scanView.reDrawGesture(0, 0, 0);
                                break;
                        }
                    }
                }
            } catch (Exception ignore) {

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
}
