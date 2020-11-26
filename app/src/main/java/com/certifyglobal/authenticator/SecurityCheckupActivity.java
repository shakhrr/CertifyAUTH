package com.certifyglobal.authenticator;

import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.certifyglobal.async_task.GetVersionCode;
import com.certifyglobal.callback.Communicator;
import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.PreferencesKeys;
import com.certifyglobal.utils.Utils;

public class SecurityCheckupActivity extends AppCompatActivity implements Communicator {

    private final static String TAG = "SecurityCheckupActivity";
    private TextView tvTitle;
    private TextView tvOs;
    private TextView tvCertifyAuth;
    private TextView tvScreen;
    private TextView tvJail;
    private ImageView imageOs;
    private ImageView imageCertifyAuth;
    private ImageView imageScreen;
    private ImageView imageJail;
    private ImageView imageOsMenu;
    private ImageView imageCertifyAuthMenu;
    private ImageView imageScreenMenu;
    private ImageView imageRootedMenu;
    private String versionCode;
    private LinearLayout llOs;
    private LinearLayout llApp;
    private LinearLayout llPrevious;
    private TextView tvPrevious;
    private ImageView imagePrevious;
    private View viewPrevious;

    private TextView tvRootedMessage;
    private TextView tvScreenLockMessage;
    private TextView tvOneOs;
    private TextView tvTwoOs;
    private TextView tvOneApp;
    private TextView tvTwoApp;
    private TextView tvThreeApp;
    private TextView tvLink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_security_checkup);
            viewPrevious = tvTitle = findViewById(R.id.tv_title);
            tvOs = findViewById(R.id.tv_current_version);
            tvCertifyAuth = findViewById(R.id.tv_auth);
            tvScreen = findViewById(R.id.tv_lock);
            tvJail = findViewById(R.id.tv_device_rooted);

            imageOs = findViewById(R.id.image_current_version);
            imageCertifyAuth = findViewById(R.id.image_auth);
            imageScreen = findViewById(R.id.image_screen_lock);
            imageJail = findViewById(R.id.image_device_rooted);
            TextView tvVersion = findViewById(R.id.tv_version);
            TextView tvBuild = findViewById(R.id.tv_build);
            llOs = findViewById(R.id.ll_os);
            llApp = findViewById(R.id.ll_app);
            //   tvLockMessage = findViewById(R.id.tv_lock_message);
            tvScreenLockMessage = findViewById(R.id.tv_lock_message);
            tvRootedMessage = findViewById(R.id.tv_rooted_message);
            tvOneOs = findViewById(R.id.tv_one);
            tvTwoOs = findViewById(R.id.tv_two);
            tvOneApp = findViewById(R.id.tv_one_app);
            tvTwoApp = findViewById(R.id.tv_two_app);
            tvThreeApp = findViewById(R.id.tv_three_app);
            tvLink = findViewById(R.id.tv_link);
            imageOsMenu = findViewById(R.id.menu_os);
            imageCertifyAuthMenu = findViewById(R.id.menu_app);
            imageScreenMenu = findViewById(R.id.menu_screen);
            imageRootedMenu = findViewById(R.id.menu_rooted);
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText(String.format("%s %s", getResources().getString(R.string.version), packageInfo.versionName));
            tvBuild.setText(String.format("%s %s", getResources().getString(R.string.build), packageInfo.versionCode));
            tvTitle.setText(getResources().getString(R.string.security_checkup));
            ImageView img_ic_back=findViewById(R.id.img_ic_back);
            img_ic_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            tvLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String packageName = getPackageName();
                        String url;
                        try {
                            getPackageManager().getPackageInfo(packageName, 0);
                            url = "market://details?id=" + packageName;
                        } catch (Exception ignored) {
                            url = "http://play.google.com/store/apps/details?id=" + packageName;
                        }
                        try {
                            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            //noinspection deprecation
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            startActivity(intent);
                        } catch (Exception ignored) {
                        }
                    } catch (Exception e) {
                        Logger.error(TAG + "onClick(View v)", e.getMessage());
                    }
                }
            });
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;

            if (!Utils.compareTowString(Build.VERSION.RELEASE, Utils.readFromPreferences(this, PreferencesKeys.osVersion, "8.1"))) {
                tvOs.setText(getResources().getString(R.string.android_no_issue));
                imageOs.setImageResource(R.drawable.os_suss);
                tvOneOs.setText(getResources().getString(R.string.android_no_issue_message));
                tvTwoOs.setText(String.format("%s Android %s.", getResources().getString(R.string.os_version), Build.VERSION.RELEASE));
            } else {
                tvOs.setText(getResources().getString(R.string.android_issue));
                imageOs.setImageResource(R.drawable.os_failed);
                tvOneOs.setText(getResources().getString(R.string.android_issue_message));
                tvTwoOs.setText(String.format("%s Android %s. \n %s Android %s.", getResources().getString(R.string.os_version), Build.VERSION.RELEASE, getResources().getString(R.string.recommend_os_version), Utils.readFromPreferences(this, PreferencesKeys.osVersion, "8.1")));
            }
            if (!Utils.compareTowString(versionCode, Utils.readFromPreferences(this, PreferencesKeys.appVersion, "2.4"))) {
                tvCertifyAuth.setText(getResources().getString(R.string.certify_updated));
                imageCertifyAuth.setImageResource(R.drawable.auth_suss);
                tvOneApp.setText(getResources().getString(R.string.authx_no_message));
                tvTwoApp.setText(String.format("%s AuthX %s.", getResources().getString(R.string.os_version), versionCode));
                tvThreeApp.setVisibility(View.GONE);
                tvLink.setVisibility(View.GONE);
            } else {
                tvCertifyAuth.setText(getResources().getString(R.string.certify_updated_not));
                imageCertifyAuth.setImageResource(R.drawable.auth_failed);
                tvOneApp.setText(getResources().getString(R.string.authx_no_issue_message));
                tvTwoApp.setText(String.format("%s AuthX %s \n %s AuthX %s", getResources().getString(R.string.os_version), versionCode, getResources().getString(R.string.recommend_os_version), Utils.readFromPreferences(this, PreferencesKeys.appVersion, "2.4")));
                tvThreeApp.setText("Install the latest app here ");
            }
            KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (myKM != null && myKM.isKeyguardSecure()) {
                tvScreen.setText(getResources().getString(R.string.screen_lock));
                imageScreen.setImageResource(R.drawable.screen_lock);
                tvScreenLockMessage.setText(getResources().getString(R.string.screen_lock_message));
            } else {
                tvScreen.setText(getResources().getString(R.string.screen_lock_not));
                imageScreen.setImageResource(R.drawable.screen_lock_failed);
                tvScreenLockMessage.setText(getResources().getString(R.string.screen_lock_not_message));
            }

            if (!Utils.readFromPreferences(this, PreferencesKeys.checkRoot, false)) {
                tvJail.setText(getResources().getString(R.string.device_not_rooted));
                imageJail.setImageResource(R.drawable.jail_break_suss);
                tvRootedMessage.setText(getResources().getString(R.string.device_not_rooted_message));
            } else {
                tvJail.setText(getResources().getString(R.string.device_rooted));
                imageJail.setImageResource(R.drawable.jail_break_failed);
                tvRootedMessage.setText(getResources().getString(R.string.device_not_rooted_message));
            }
            //   new GetVersionCode(this, this).execute();

        } catch (Exception e) {
            Logger.error(TAG + "onCreate(Bundle savedInstanceState)", e.getMessage());
        }

    }

    // items click to set action
    public void myOnClick(View v) {
        try {
            if (llPrevious != null)
                llPrevious.setVisibility(View.GONE);
            if (tvPrevious != null)
                tvPrevious.setVisibility(View.GONE);
            if (imagePrevious != null)
                imagePrevious.setImageResource(R.drawable.ic_action_down);
            switch (v.getId()) {
                case R.id.tr_os:
                    if (viewPrevious != v) {
                        llPrevious = llOs;
                        imagePrevious = imageOsMenu;
                        imageOsMenu.setImageResource(R.drawable.ic_action_up);
                        llOs.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.tr_auth:
                    if (viewPrevious != v) {
                        llPrevious = llApp;
                        imagePrevious = imageCertifyAuthMenu;
                        imageCertifyAuthMenu.setImageResource(R.drawable.ic_action_up);
                        llApp.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.tr_lock:
                    if (viewPrevious != v) {
                        tvPrevious = tvScreenLockMessage;
                        imagePrevious = imageScreenMenu;
                        imageScreenMenu.setImageResource(R.drawable.ic_action_up);
                        tvScreenLockMessage.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.tr_rooted:
                    if (viewPrevious != v) {
                        tvPrevious = tvRootedMessage;
                        imagePrevious = imageRootedMenu;
                        imageRootedMenu.setImageResource(R.drawable.ic_action_up);
                        tvRootedMessage.setVisibility(View.VISIBLE);
                    }
                    break;
            }
            if (viewPrevious == v) viewPrevious = tvTitle;
            else
                viewPrevious = v;
        } catch (Exception e) {
            Logger.error(TAG + "myOnClick(View v)", e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {

        } catch (Exception e) {
            Logger.error(TAG + "onReceive(Context context, Intent intentOf)", e.getMessage());
        }
    }

    @Override
    public void setAction(String newVersion, int position) {
        try {
            if (newVersion != null && !newVersion.isEmpty()) {
                if (!versionCode.equalsIgnoreCase(newVersion)) {
                    tvCertifyAuth.setText(getResources().getString(R.string.certify_updated_not));
                    imageCertifyAuth.setImageResource(R.drawable.auth_failed);
                } else {
                    tvCertifyAuth.setText(getResources().getString(R.string.certify_updated));
                    imageCertifyAuth.setImageResource(R.drawable.auth_suss);
                }
            }
        } catch (Exception e) {
            Logger.error(TAG + "setAction(String newVersion, int position)", e.getMessage());
        }
    }
}
