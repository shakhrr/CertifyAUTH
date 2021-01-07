package com.certifyglobal.utils;

import android.app.Application;

import com.certifyglobal.authenticator.ApplicationWrapper;
import com.certifyglobal.authenticator.Settings;

@SuppressWarnings("WeakerAccess")
//dev Environment:
//
//        https://admindev.authx.com/  - Portal
//        https://apidev.certifyauth.com/  - Auth API
//        https://admintest.certifyauth.com/ep - Web API
//Test Environment:
//
//        https://admintest.certifyauth.com/  - Portal
//        https://apitest.certifyauth.com/  - Auth API
//        https://admintest.certifyauth.com/ep - Web API
//
//        demo Environment:
//
//        https://admindemo.certifyauth.com/  - Portal
//        https://apidemo.certifyauth.com/  - Auth API
//        https://admindemo.certifyauth.com/ep - Web API
//
//
//        Prod Environment:
//
//        https://admin.certifyauth.com/  - Portal
//        https://api.certifyauth.com/  - Auth API
//        https://admin.certifyauth.com/ep - Web API
public class EndPoints {

    public static final Mode deployment = Mode.Prod;
    public static String prod_url = "https://api.authx.com/";//"https://apidemo.certifyauth.com/mobile/";
    public static String dev_url = "https://api.authx.com/";

    private static String GetServerURL() {
        return (deployment == Mode.Local ? dev_url : prod_url);
    }

    public static String domainUrl =  "mobile/";
    public static final String  activateMobile = "Mobile/ActivateMobile?Id=";
    //public static final String codePost = domainUrl + "GetUserDetails/post";
    public static final String codePost = domainUrl + "ActivateUser";
    public static final String pushFace = domainUrl + "FacepushEnroll";
    public static final String facePushVerify = domainUrl + "FacePushVerify";
    public static final String deactivateUser = domainUrl + "DeactivateUser";
    // public static final String pushAuthenticationStatus = domainUrl + "PushAuthenticationStatus";
    public static final String pushAuthenticationStatus = domainUrl + "MobileAuthStatus";
    public static String getOsDetails = GetServerURL()+ "Mobile/GetOSDetails";

    public static final String addMobileApp = domainUrl + "AddMobileApp";
    public static final String deactivateThird = domainUrl + "DeactivateMobileApp";
    public static final String sendUserTOtp =  "SendUserTOtp"; //2402924249
    public static final String validateUserCode =  "ValidateUserCode";
    public static String payLoadNotification = "GetPayLoadNotification";
    public static String faceSetting = domainUrl+"getBioSettingsForMobile";
    public static final String companyImageUpdate = domainUrl + "getCompanyUpdates";
    public static final String LICENSE_FILE = "res/raw/iengine.lic"; public enum Mode {Prod, Local}
    public static final String palmEnroll = domainUrl + "PalmPushEnroll";
    public static final String palmVerify = domainUrl + "PalmPushVerify";


}
