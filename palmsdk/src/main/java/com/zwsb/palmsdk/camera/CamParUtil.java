package com.zwsb.palmsdk.camera;

import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.zwsb.palmsdk.helpers.BaseUtil;
import com.zwsb.palmsdk.helpers.Constant;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class CamParUtil {
    private static final float MAX_EXPOSURE_COMPENSATION = 1.5f;
    private static final float MIN_EXPOSURE_COMPENSATION = 0.0f;

    private static final int AREA_RADIUS = 300;
    private static final int METERING_AREAS_DELAY = 250;
    private static final int METERING_AREAS_TO_CENTER_DELAY = 3000;

    private static boolean isMeteringAreasBlocked = false;

    private static int savedMeteringX = 0;
    private static int savedMeteringY = 0;

    public static void initCamPar(Camera camera) {
        if (camera != null) {
            Parameters params = camera.getParameters();
            Log.i("LOG", params.flatten());

            //set camera resolution
            List<Size> listPreviewSizes = params.getSupportedPreviewSizes();
            for (Size size : listPreviewSizes) {
                if (size.width == Constant.RESOLUTION_RATIO_HEIGHT && size.height == Constant.RESOLUTION_RATIO_WIDTH) {
                    Log.i("LOG", "set resolution");
                    params.setPreviewSize(Constant.RESOLUTION_RATIO_HEIGHT, Constant.RESOLUTION_RATIO_WIDTH);
                }
            }

            //set camera FPS to 30
            params.setPreviewFrameRate(30);

            /**
             * Set iso parameters
             */

            String flat = params.flatten();
            String valuesKeyword=null;
            String isoKeyword=null;
            if(flat.contains("iso-values")) {
                // most used keywords
                valuesKeyword="iso-values";
                isoKeyword="iso";
            } else if(flat.contains("iso-mode-values")) {
                // google galaxy nexus keywords
                valuesKeyword="iso-mode-values";
                isoKeyword="iso";
            } else if(flat.contains("iso-speed-values")) {
                // micromax a101 keywords
                valuesKeyword="iso-speed-values";
                isoKeyword="iso-speed";
            } else if(flat.contains("nv-picture-iso-values")) {
                // LG dual p990 keywords
                valuesKeyword="nv-picture-iso-values";
                isoKeyword="nv-picture-iso";
            }

            /**
             * set manual iso
             */
            String isoParams = params.get(valuesKeyword);
            if (isoParams != null) {
                String[] values = isoParams.split(",");
                if (values != null) {
                    HashMap<Integer, String> valuesMap = new HashMap<>();
                    for (String value : values) {
                        String onlyDigitValue = value.replaceAll("\\D+","");
                        if (!onlyDigitValue.isEmpty()) {
                            valuesMap.put(Integer.valueOf(onlyDigitValue), value.trim());
                        }
                    }

                    Log.i(BaseUtil.LOG_TAG, "VALUES MAP: " + valuesMap.toString());
                    Integer biggestIsoKey = 0;
                    for (Integer isokey : valuesMap.keySet()) {
                        if (isokey > biggestIsoKey) {
                            biggestIsoKey = isokey;
                        }
                    }

                    Log.i(BaseUtil.LOG_TAG, "SELECTED ISO: " + biggestIsoKey + " AND VALUE: " + valuesMap.get(biggestIsoKey));
                    if (biggestIsoKey != 0) {
                        String iso = valuesMap.get(biggestIsoKey);
                        params.set(isoKeyword, iso);
                        Log.i(BaseUtil.LOG_TAG, "ISO WAS SET TO: " + iso);
                    }
                }
            }

            /**
             * set camera parameters for better performance
             */
            setMetering(params);
            setFocusArea(params);
            setBestExposure(params, true);

            try {
                camera.setParameters(params);
            } catch (RuntimeException runtimeException) {
                Log.e(BaseUtil.LOG_TAG, "Error: ", runtimeException);
            }
        }
    }

    public static void setMetering(Camera.Parameters parameters) {
        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> middleArea = buildMiddleArea(AREA_RADIUS);
            Log.i(BaseUtil.LOG_TAG, "Setting metering area to : " + toString(middleArea));
            parameters.setMeteringAreas(middleArea);
        } else {
            Log.i(BaseUtil.LOG_TAG, "Device does not support metering areas");
        }
    }

    public static void setMetering(Camera camera, int x, int y) {
        if (camera != null) {
            if (!isMeteringAreasBlocked) {
                isMeteringAreasBlocked = true;
                Camera.Parameters parameters = camera.getParameters();

                if (parameters.getMaxNumMeteringAreas() > 0) {
                    List<Camera.Area> area = buildArea(x, y);
                    Log.i(BaseUtil.LOG_TAG, "Setting palm metering area to : " + toString(area));
                    parameters.setMeteringAreas(area);
                } else {
                    Log.i(BaseUtil.LOG_TAG, "Device does not support metering areas");
                }

                try {
                    camera.setParameters(parameters);
                } catch (RuntimeException runtimeException) {
                    Log.e(BaseUtil.LOG_TAG, "Error: ", runtimeException);
                }
                meteringAreasBlockHandler.sendEmptyMessageDelayed(0, METERING_AREAS_DELAY);

                /**
                 * Handler for setting metering areas to center, in case of no palms detected
                 */
                meteringAreasToCenterHandler.removeMessages(1);
                Message message = meteringAreasToCenterHandler.obtainMessage(1, savedMeteringX, savedMeteringY, camera);
                meteringAreasToCenterHandler.sendMessageDelayed(message, METERING_AREAS_TO_CENTER_DELAY);
            }
        }
    }

    private static final Handler meteringAreasBlockHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            isMeteringAreasBlocked = false;
            return true;
        }
    });

    private static final Handler meteringAreasToCenterHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if ((savedMeteringX == msg.arg1 && savedMeteringY == msg.arg2) && (msg.obj != null)) {
                Camera camera = (Camera)msg.obj;
                setMetering(camera.getParameters());
                Log.i(BaseUtil.LOG_TAG, "Metering areas was set to center");
            }
            return true;
        }
    });

    public static void stopHandlers() {
        meteringAreasBlockHandler.removeMessages(0);
        meteringAreasToCenterHandler.removeMessages(1);
    }

    public static void setFocusArea(Camera.Parameters parameters) {
        if (parameters.getMaxNumFocusAreas() > 0) {
            Log.i(BaseUtil.LOG_TAG, "Old focus areas: " + toString(parameters.getFocusAreas()));
            List<Camera.Area> middleArea = buildMiddleArea(AREA_RADIUS);
            Log.i(BaseUtil.LOG_TAG, "Setting focus area to : " + toString(middleArea));
            parameters.setFocusAreas(middleArea);
        } else {
            Log.i(BaseUtil.LOG_TAG, "Device does not support focus areas");
        }
    }

    public static void setBestExposure(Camera.Parameters parameters, boolean lightOn) {
        int minExposure = parameters.getMinExposureCompensation();
        int maxExposure = parameters.getMaxExposureCompensation();
        float step = parameters.getExposureCompensationStep();
        if ((minExposure != 0 || maxExposure != 0) && step > 0.0f) {
            // Set low when light is on
            float targetCompensation = lightOn ? MIN_EXPOSURE_COMPENSATION : MAX_EXPOSURE_COMPENSATION;
            int compensationSteps = Math.round(targetCompensation / step);
            float actualCompensation = step * compensationSteps;
            // Clamp value:
            compensationSteps = Math.max(Math.min(compensationSteps, maxExposure), minExposure);
            if (parameters.getExposureCompensation() == compensationSteps) {
                Log.i(BaseUtil.LOG_TAG, "Exposure compensation already set to " + compensationSteps + " / " + actualCompensation);
            } else {
                Log.i(BaseUtil.LOG_TAG, "Setting exposure compensation to " + compensationSteps + " / " + actualCompensation);
                parameters.setExposureCompensation(compensationSteps);
            }
        } else {
            Log.i(BaseUtil.LOG_TAG, "Camera does not support exposure compensation");
        }
    }

    private static List<Camera.Area> buildMiddleArea(int areaPer1000) {
        return Collections.singletonList(
                new Camera.Area(new Rect(-areaPer1000, -areaPer1000, areaPer1000, areaPer1000), 1));
    }

    private static List<Camera.Area> buildArea(int x, int y) {
        savedMeteringX = x;
        savedMeteringY = y;

        Rect palmRect = new Rect(
                (x - AREA_RADIUS),
                (y - AREA_RADIUS),
                (x + AREA_RADIUS),
                (y + AREA_RADIUS));

        final Rect targetRect = new Rect(
                palmRect.left * 2000/BaseUtil.screenWidth - 1000,
                palmRect.top * 2000/BaseUtil.screenHeight - 1000,
                palmRect.right * 2000/BaseUtil.screenWidth - 1000,
                palmRect.bottom * 2000/BaseUtil.screenHeight - 1000);

        return Collections.singletonList(
                new Camera.Area(targetRect, 1));
    }

    private static String toString(Iterable<Camera.Area> areas) {
        if (areas == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (Camera.Area area : areas) {
            result.append(area.rect).append(':').append(area.weight).append(' ');
        }
        return result.toString();
    }

    private static String toString(Collection<int[]> arrays) {
        if (arrays == null || arrays.isEmpty()) {
            return "[]";
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        Iterator<int[]> it = arrays.iterator();
        while (it.hasNext()) {
            buffer.append(Arrays.toString(it.next()));
            if (it.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append(']');
        return buffer.toString();
    }
}
