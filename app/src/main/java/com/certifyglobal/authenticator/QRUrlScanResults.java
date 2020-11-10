package com.certifyglobal.authenticator;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.certifyglobal.async_task.AsyncGetJsonObject;
import com.certifyglobal.callback.Communicator;
import com.certifyglobal.callback.JSONObjectCallback;
import com.certifyglobal.utils.EndPoints;
import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.PreferencesKeys;
import com.certifyglobal.utils.Requestor;
import com.certifyglobal.utils.Utils;
import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;


public class QRUrlScanResults extends AppCompatActivity implements JSONObjectCallback, Communicator {
    private String TAG = "QRUrlScanResults - ";
    private String url;
    private Dialog dialog;
    private String sharedKey;
    private boolean third = false;
    private String userName = "";
    private String applicationName = "";
    private Token token;
    private String domainValue = "";
    private String domainTemp = "";
    private String id = "";
    private boolean hostValue=true;
    private  String header="";
    String companyId="";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_splash);
            header = "";
            hostValue = true;
            if (getIntent().getStringExtra("Url") != null) {
                url = getIntent().getStringExtra("Url");
                if (url.contains("otpauth://totp")) { // third party QR code Scaning
//                    third = true;
////                    if (Utils.readFromPreferences(this, PreferencesKeys.mobileNumber, "").isEmpty())
////                        Utils.PassphraseDialog(this, "", this);
////                    else
                    addTokenAndFinish(url);
                } else
                    SendReq();  //first api call
            }
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListener(JSONObject report, String status,JSONObject jsonObject) {
        try {
            if (report == null) {
                if (dialog != null)
                    dialog.dismiss();
                Logger.toast(QRUrlScanResults.this, getString(R.string.request_timed_out));
                finish();
            } else if (ApplicationWrapper.BaseUrl(domainValue, EndPoints.codePost).equals(status)) {
                if (dialog != null)
                    dialog.dismiss();
                // GetUserDetails post  response
                if (!report.isNull("response_code") && report.getInt("response_code") == 1) {
                    JSONObject objJson = report.getJSONObject("user_data");
                    sharedKey = Utils.getDecrypted(report.getString("shared_key"), QRUrlScanResults.this);
                    String name = String.format("%s %s", objJson.isNull("first_name") ? "" : objJson.getString("first_name"), objJson.getString("last_name"));
                    String companyName = objJson.isNull("company_name")  ? "" : objJson.getString("company_name");
                    String role = objJson.isNull("role")  ? "" : objJson.getString("role");
                    if (Utils.readFromPreferences(this, PreferencesKeys.mobileNumber, "").isEmpty()) {
                        Utils.saveToPreferences(this, PreferencesKeys.mobileNumber, objJson.isNull("Mobile") || objJson.getString("Mobile").isEmpty() ? "" : objJson.getString("Mobile"));
                    }
                    if (!objJson.isNull("company_icon") && !objJson.getString("company_icon").isEmpty()) {
                        byte[] imageBytes = Base64.decode(objJson.getString("company_icon"), Base64.DEFAULT);
                        ApplicationWrapper.getMdbCompanyAdapter().insertCompany(domainTemp,objJson.getString("user_id"), companyName, imageBytes);
                    }
                    String value = String.format("otpauth://totp/%s:%s|%s|%s|%s|%s|%s?secret=%s&digits=6&period=30", name.replace(" ", "%20"), companyName.replace(" ", "%20"), objJson.getString("user_name"), role.replace(" ", "%20"), companyId, objJson.getString("user_id"), domainValue, sharedKey);
                    third = false;
                    addTokenAndFinish(value);
                    Logger.toast(this, report.getString("response_text"));
                } else {


                    if (!report.isNull("response_text") && report.getString("response_text").equals(getString(R.string.device_security))) {
                        SettingDialog();
                    //    Logger.toast(this, report.getString("Message"));
                    }else{
                        Logger.toast(QRUrlScanResults.this, report.isNull("response_text")?getString(R.string.error):report.getString("response_text"));
                        finish();
                    }
//                    if (!Utils.readFromPreferences(QRUrlScanResults.this, PreferencesKeys.isLogin, false))
//                        startActivity(new Intent(QRUrlScanResults.this, MainActivity.class));
//                    finish();
                }
            }else if(report.get("Message").equals("A Server with the specified hostname could not be found.") && hostValue){
                hostValue=false;
                String data[] = domainTemp.split("\\-");
                domainValue = data[0];
                header=data[1];
                new AsyncGetJsonObject(this, ApplicationWrapper.BaseUrl(domainValue, EndPoints.activateMobile +id),header).execute();

            }
            else {
                // QR code response
                if (!report.isNull("Code") && report.getInt("Code") == 1) { //first api response
                    JSONObject userObj = report.getJSONObject("UserDetails");
                    companyId=userObj.getString("CompanyId");
                    Utils.QRCodeSender(report.getString("CodeId"), userObj.getString("UserId"), userObj.getInt("CompanyId"), QRUrlScanResults.this, QRUrlScanResults.this, domainValue,header);
                } else {
                    if (!report.isNull("Message"))
                        Logger.toast(QRUrlScanResults.this, report.getString("Message"));
                    else Logger.toast(QRUrlScanResults.this, getString(R.string.error));
                    if (dialog != null)
                        dialog.dismiss();
                    if (!Utils.readFromPreferences(QRUrlScanResults.this, PreferencesKeys.isLogin, false))
                        startActivity(new Intent(QRUrlScanResults.this, MainActivity.class));
                    finish();
                }
            }
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
            if (dialog != null)
                dialog.dismiss();
            Logger.toast(QRUrlScanResults.this, getString(R.string.error2));
            if (!Utils.readFromPreferences(QRUrlScanResults.this, PreferencesKeys.isLogin, false))
                startActivity(new Intent(QRUrlScanResults.this, MainActivity.class));
            finish();
        }
    }

    public void SendReq() {
        try {
            if (Utils.isConnectingToInternet(QRUrlScanResults.this)) {
                dialog = new Dialog(this);
                dialog = Utils.showDialog(dialog, this);
                if (dialog != null) dialog.show();
                String data[] = url.split("\\*");
                domainTemp= domainValue = data[1];
                id=data[0];

                new AsyncGetJsonObject(this, ApplicationWrapper.BaseUrl(domainValue, EndPoints.activateMobile + id),"").execute();
            } else {
                finish();
                Logger.toast(QRUrlScanResults.this, getResources().getString(R.string.network_error));
            }
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());

        }

    }

    private void addTokenAndFinish(String text) {
        try {
            token = new Token(text);
            if (new TokenPersistence(QRUrlScanResults.this).tokenExists(token)) {
                if (UserActivity.isUserIn) {
                    TokenPersistence mTokenPersistence = new TokenPersistence(this);
                    for (int i = 0; i < mTokenPersistence.length(); i++) {
                        Token tokenTemp = mTokenPersistence.get(i);
                        if (tokenTemp.getLabel().equals(token.getLabel())) {
                            new TokenPersistence(this).delete(i);
                            break;
                        }
                    }
                }
            }else{

                TokenPersistence mTokenPersistence = new TokenPersistence(this);
                for (int i = 0; i < mTokenPersistence.length(); i++) {
                    Token tokenTemp = mTokenPersistence.get(i);
                    String[] labelU = tokenTemp.getLabel().split("\\|");
                    String userId = labelU.length >= 4 ? labelU[4] : "";

                    String[] templabelU = token.getLabel().split("\\|");
                    String tempuserId = templabelU.length >= 4 ? templabelU[4] : "";

                    if (userId.equals(tempuserId)) {
                        new TokenPersistence(this).delete(i);
                        break;
                    }
                }

            }

            TokenPersistence.saveAsync(QRUrlScanResults.this, token);
            if (token.getImage() == null) {
                if (UserActivity.isUserIn) {
                    UserDashBored();
                }
                //   thirdParty();
                finish();
                return;
            }

            final ImageView image = findViewById(R.id.image);
            Picasso.get()
                    .load(token.getImage())
                    .placeholder(R.drawable.scan)
                    .into(image, new Callback() {
                        @Override
                        public void onSuccess() {
                            findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                            image.setAlpha(0.9f);
                            image.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (UserActivity.isUserIn) {
                                        UserDashBored();
                                    }
                                    finish();
                                }
                            }, 2000);
                        }

                        @Override
                        public void onError(Exception e) {
                            if (UserActivity.isUserIn) {
                                UserDashBored();
                            }
                            finish();
                        }
                    });
        } catch (Token.TokenUriInvalidException e) {
            if (UserActivity.isUserIn) {
                startActivity(new Intent(QRUrlScanResults.this, MainActivity.class));
            }
            // thirdParty();
            finish();
            e.printStackTrace();

        } catch (Exception e) {
            Logger.error(TAG + "addTokenAndFinish(String text)", e.getMessage());
        }
    }

    private void thirdParty() {
        try {

            if (third && token != null) {
                userName = token.getLabel();
                applicationName = Utils.getIssuer(token.toString());
                Utils.addMobileApp(userName, applicationName, token.toString(), "", null, null);
            }
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }

    private void UserDashBored() {
        try {
            Intent intent = new Intent(getApplicationContext(), UserActivity.class);
            startActivity(intent);
            UserActivity.isUserIn = false;
        } catch (Exception ignore) {

        }
    }

    @Override
    public void setAction(String userName, int position) {
        if (position == -10) {
            Utils.saveToPreferences(QRUrlScanResults.this, PreferencesKeys.mobileNumber, userName);
        } else third = false;

        addTokenAndFinish(url);
    }

    private void SettingDialog() {
        try {
            android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);
            // alertDialog.setTitle(getString(R.string.relink_message_title));
            //  alertDialog.setCancelable(false);
            alertDialog.setMessage(Html.fromHtml(getString(R.string.device_security)));
            alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startActivity(new Intent(QRUrlScanResults.this, SecurityCheckupActivity.class));
                    finish();
                }
            });
            android.app.AlertDialog builder = alertDialog.create();
            builder.show();
        } catch (Exception e) {

        }
    }
}
