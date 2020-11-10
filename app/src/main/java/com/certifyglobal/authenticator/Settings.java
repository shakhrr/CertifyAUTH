package com.certifyglobal.authenticator;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.PreferencesKeys;
import com.certifyglobal.utils.Utils;


public class Settings extends MainActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.setting);
            TextView tvTitle = findViewById(R.id.tv_title);
            TextView tvVersion = findViewById(R.id.tv_version);
            TextView tvBuild = findViewById(R.id.tv_build);
            LinearLayout llBioSign = findViewById(R.id.ll_bio_sign);
            LinearLayout llBle = findViewById(R.id.ll_ble);
            LinearLayout llSecurityCheckup = findViewById(R.id.ll_security_checkup);
            LinearLayout llRecover = findViewById(R.id.ll_recover);
            tvTitle.setText(getResources().getString(R.string.settings));
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText(String.format("%s %s", getResources().getString(R.string.version), packageInfo.versionName));
            tvBuild.setText(String.format("%s %s", getResources().getString(R.string.build), packageInfo.versionCode));

            llBioSign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent livePreIntent = new Intent(Settings.this, ScanActivity.class);
                    //  Intent livePreIntent = new Intent(Settings.this, WearUIActivity.class);
                    livePreIntent.putExtra("type", "BioSign");
                    startActivity(livePreIntent);
                }
            });
            llBle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Settings.this, DeviceSelect.class));
                }
            });
            llSecurityCheckup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Settings.this, SecurityCheckupActivity.class));
                }
            });
        } catch (Exception e) {
            Logger.error("Settings - ", "onCreate(Bundle savedInstanceState)");
        }

    }

    public static String getAPI() {
        return Utils.readFromPreferences(ApplicationWrapper.context, PreferencesKeys.apiLink, "https://apitest.certifyauth.com/");
    }


    @Override
    protected void onResume() {
        super.onResume();
    }
}