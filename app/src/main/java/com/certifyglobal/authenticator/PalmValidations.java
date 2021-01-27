package com.certifyglobal.authenticator;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.certifyglobal.callback.JSONObjectCallback;
import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.Utils;
import com.zwsb.palmsdk.activities.AuthActivity;
import com.zwsb.palmsdk.activities.PalmActivity;
import com.zwsb.palmsdk.helpers.SharedPreferenceHelper;
import org.json.JSONObject;
import butterknife.ButterKnife;
import static com.zwsb.palmsdk.activities.AuthActivity.NEW_USER_ACTION;

public class PalmValidations extends AppCompatActivity implements JSONObjectCallback {
    private static final String TAG = "PalmValidations - ";
    private String userName;
    private String requestId;
    private String correlationId;
    private String palmType = "left";
    private String pushType;
    private String userId;
    private String hostName;
    public static String defaultUserName = "AuthXUser";
    private Dialog dialog;
    private boolean statusBoolean;
    private LinearLayout llMessage;

    private TextView tvMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            setContentView(R.layout.activity_palm_validations);
            ButterKnife.bind(this);
            llMessage = findViewById(R.id.ll_message);
            tvMessage = findViewById(R.id.tv_message);
            palmType = "left";
            llMessage.setVisibility(View.GONE);
            Intent intentGet = getIntent();
            userName = intentGet.getStringExtra("user");
            pushType = intentGet.getStringExtra("pushType");
            requestId = intentGet.getStringExtra("requestId");
            correlationId = intentGet.getStringExtra("correlationId");
            userId = intentGet.getStringExtra("userId") == null ? "" : intentGet.getStringExtra("userId");
            hostName=intentGet.getStringExtra("hostName");
            AuthActivity.timeStamp = intentGet.getStringExtra("timeStamp");
            AuthActivity.timeOut = intentGet.getIntExtra("timeOut", 0);
            if (pushType.equals("3"))//verfying
                startActivityForResult(AuthActivity.getIntent(this, defaultUserName, true, false), 7);
            else try {
                SharedPreferenceHelper.setLeftPalmEnabled(PalmValidations.this, false, defaultUserName);
                SharedPreferenceHelper.setRightPalmEnabled(PalmValidations.this, false, defaultUserName);
            } catch (Exception ig) {
            }
        } catch (Exception e) {
            Logger.error(TAG + "-> onCreate(Bundle savedInstanceState)", e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Intent intent;
            if (pushType.equals("4")) {//enrolling palm
                switch (palmType) {
                    case "left":
                        palmType = "right";
                        intent = new Intent(this, AuthActivity.class);
                        intent.putExtra(AuthActivity.USER_ACTION_KEY, NEW_USER_ACTION);
                        intent.putExtra(AuthActivity.IS_RIGHT_PALM_NEEDED_KEY, false);
                        intent.putExtra(AuthActivity.USER_NAME_KEY, defaultUserName);
                        startActivityForResult(intent, NEW_USER_ACTION);
                        break;
                    case "right":
                        palmType = "done";
                        startActivityForResult(PalmActivity.getIntent(this, defaultUserName, false, true), 0);
                        break;
                    case "done":
                        palmType = "finish";
                        startActivityForResult(PalmActivity.getIntent(this, defaultUserName, true, true), 7);
                        break;
                }
            }
        } catch (Exception e) {
            Logger.error(TAG + "onResume()", e.getMessage());
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 7) {
                dialog = new Dialog(this);
                dialog = Utils.showDialog(dialog, this);
                if (dialog != null) dialog.show();
                if (resultCode == AuthActivity.ON_SCAN_RESULT_OK || resultCode == NEW_USER_ACTION)
                Utils.palmEnroll(userName,PalmValidations.this,requestId,userId,pushType,PalmValidations.this,correlationId,0,hostName);
            }
        } catch (Exception e) {
            Logger.error(TAG + "onActivityResult(int requestCode, int resultCode, Intent data)", e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListener(JSONObject report, String status,JSONObject req) {
        try {
            if (dialog != null) dialog.dismiss();
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            llMessage.setVisibility(View.VISIBLE);
            if (report == null) {
                SetFailed(getResources().getString(R.string.failed));
                return;
            }
            if (!report.isNull("ExceptionMessage")) {
                Logger.toast(PalmValidations.this, report.getString("ExceptionMessage"));
               // statusBoolean = false;
            } else if (!report.isNull("response_text")) {
                Logger.toast(PalmValidations.this, report.getString("response_text"));
              //  statusBoolean = false;
            }
            String requestIdServer = report.isNull("request_id") ? "" : report.getString("request_id");
            if (report.getString("response_code").equals("1")) {
                    llMessage.setBackgroundColor(getResources().getColor(R.color.green));
                    tvMessage.setText(pushType.equals("3") ? getResources().getString(R.string.authenticated) : getResources().getString(R.string.enrollment_successful));
                    tvMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_approve, 0, 0);
            } else {
                SetFailed(getResources().getString(R.string.failed));
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    startActivity(new Intent(PalmValidations.this, SplashActivity.class));
                }
            }, 2000);

        } catch (Exception e) {
            Logger.error(TAG + "onJSONObjectListener(JSONObject report, String status)", e.getMessage());
        }
    }

    public void SetFailed(String message) {
        llMessage.setBackgroundColor(getResources().getColor(R.color.orange));
        tvMessage.setText(message);
        tvMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_deny, 0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

}
