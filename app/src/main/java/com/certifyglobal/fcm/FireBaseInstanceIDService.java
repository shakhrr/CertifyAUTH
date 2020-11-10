package com.certifyglobal.fcm;

import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.Utils;
import com.google.firebase.iid.FirebaseInstanceId;


public class FireBaseInstanceIDService extends FireBaseMessagingService {
    private static final String TAG = "MyFirebaseIIDService";




    public void onTokenRefresh() {
//Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
//Displaying token on logcat
    }

}