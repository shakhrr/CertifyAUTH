package com.zwsb.palmsdk.helpers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;

public class Constant {
    public static final String LOG = "LOG";
    public static final String VIDEO_STATUS = "video_status";

    public static final String VIDEO_ACTION = "video_action";

    /**
     * Specify camera: front or back
     */
    public enum CameraFacing {
        BACK, FRONT
    }

    public static final class permission {
        public static final String[] camera = new String[]{android.Manifest.permission.CAMERA};
        public static final String[] phone = new String[]{android.Manifest.permission.READ_PHONE_STATE};
        public static final String[] storage = new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        public static final String[] all = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        public static final String[] camera_phone = new String[]{android.Manifest.permission.CAMERA,android.Manifest.permission.READ_PHONE_STATE};

    }

    public static final CameraFacing DEFAULT_CAMERA = CameraFacing.FRONT;

    public static final boolean DEBUG_MATCH_SCORES = false;

    public static final int RESOLUTION_RATIO_HEIGHT = 640;
    public static final int RESOLUTION_RATIO_WIDTH = 480;
    public static final float RESOLUTION_RATIO = 1.333f;

    public static final String ZWSB_SHARED_NAME = "zwsb_shared";

    public static final String IS_FIRST = "is_first";

    public static final String USER_EMAIL_KEY = "USER_EMAIL_KEY";

    public enum VideoStatus {

        SHOWUSERLIST, SHOWADMINLIST, SHOWVIDEO, SHOWSUCCESS
    }

    public enum VideoAction {

        NEWUSER, INVALIDATE, RETAKE
    }

    public enum ScreenSwitch {

        PORTRAIT, LANDSCAPE
    }

    public enum HandlerMode {

        LOADFRAME, BGLOADFRAME, VRLOADFRAME
    }

    public static boolean PermissionRequest(Context context, String[] permissions) {
        try {
            if (Build.VERSION.SDK_INT < 23 || permissions == null) return false;
            ArrayList<String> requestPermission = new ArrayList<>();
            for (String permission : permissions) {
                int permissionCheck = context.checkSelfPermission(permission);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED)
                    requestPermission.add(permission);
            }
            if (requestPermission.size() <= 0) return false;
            //  ActivityCompat.requestPermissions(context, requestPermission.toArray(new String[0]), 1);
        } catch (Exception e) {
            Log.e(LOG + "PermissionRequest()", e.getMessage());
        }
        return true;
    }
}
