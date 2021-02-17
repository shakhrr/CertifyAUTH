package com.certifyglobal.fcm;

import androidx.annotation.NonNull;

import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.Utils;
import com.google.firebase.iid.FirebaseInstanceId;


public class FireBaseInstanceIDService extends FireBaseMessagingService {
    private static final String TAG = "MyFirebaseIIDService";


    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

    }
}