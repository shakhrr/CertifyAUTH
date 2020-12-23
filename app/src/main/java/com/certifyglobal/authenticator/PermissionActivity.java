package com.certifyglobal.authenticator;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.PreferencesKeys;
import com.certifyglobal.utils.Utils;

public class PermissionActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
   private TextView tvTitle;
    private SwitchCompat swithc_menu_phone, swithc_menu_camera, swithc_menu_storage, swithc_menu_location;
    private boolean denydialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(getResources().getString(R.string.permission));
        ImageView img_ic_back = findViewById(R.id.img_ic_back);
        swithc_menu_phone = findViewById(R.id.swithc_menu_phone);
        swithc_menu_camera = findViewById(R.id.swithc_menu_camera);
        swithc_menu_storage = findViewById(R.id.swithc_menu_storage);
        swithc_menu_location = findViewById(R.id.swithc_menu_location);

        readPreferencesValue();

        swithc_menu_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    swithc_menu_phone.setChecked(true);
                } else {
                    Utils.PermissionRequest(PermissionActivity.this, Utils.permission.phone);
                }
            }
        });
        swithc_menu_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    swithc_menu_camera.setChecked(true);
                } else {
                    Utils.PermissionRequest(PermissionActivity.this, Utils.permission.camera);
                }
            }
        });
        swithc_menu_storage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    swithc_menu_storage.setChecked(true);
                } else {
                    Utils.PermissionRequest(PermissionActivity.this, Utils.permission.storage);
                }
            }
        });
        swithc_menu_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    swithc_menu_location.setChecked(true);
                } else {
                    Utils.PermissionRequest(PermissionActivity.this, Utils.permission.location);
                }
            }
        });
        img_ic_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void readPreferencesValue() {
        if (ContextCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            swithc_menu_phone.setChecked(true);
        } else {
            swithc_menu_phone.setChecked(false);
        }
        if (ContextCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            swithc_menu_camera.setChecked(true);
        } else {
            swithc_menu_camera.setChecked(false);
        }
        if (ContextCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            swithc_menu_storage.setChecked(true);
        } else {
            swithc_menu_storage.setChecked(false);
        }
        if (ContextCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            swithc_menu_location.setChecked(true);
        } else {
            swithc_menu_location.setChecked(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];
                if (permission.equals(Manifest.permission.READ_PHONE_STATE)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        Utils.saveToPreferences(this, PreferencesKeys.phone, true);
                        swithc_menu_phone.setChecked(true);
                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        Utils.saveToPreferences(this, PreferencesKeys.phone, false);
                        swithc_menu_phone.setChecked(false);
                    } else {
                        Utils.saveToPreferences(this, PreferencesKeys.phone, false);
                        swithc_menu_phone.setChecked(false);
                        Utils.gotoSetting(this);
                    }
                } else if (permission.equals(Manifest.permission.CAMERA)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        Utils.saveToPreferences(this, PreferencesKeys.camera, true);
                        swithc_menu_camera.setChecked(true);
                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        Utils.saveToPreferences(this, PreferencesKeys.camera, false);
                        swithc_menu_camera.setChecked(false);
                    } else {
                        Utils.saveToPreferences(this, PreferencesKeys.camera, false);
                        swithc_menu_camera.setChecked(false);
                        Utils.gotoSetting(this);
                    }
                } else if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        Utils.saveToPreferences(this, PreferencesKeys.location, true);
                        swithc_menu_location.setChecked(true);
                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        Utils.saveToPreferences(this, PreferencesKeys.location, false);
                        swithc_menu_location.setChecked(false);
                    } else {
                        Utils.saveToPreferences(this, PreferencesKeys.location, false);
                        swithc_menu_location.setChecked(false);
                         Utils.gotoSetting(this);
                    }

                } else if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        Utils.saveToPreferences(this, PreferencesKeys.storage, true);
                        swithc_menu_storage.setChecked(true);
                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        Utils.saveToPreferences(this, PreferencesKeys.storage, false);
                        swithc_menu_storage.setChecked(false);
                    } else {
                        swithc_menu_storage.setChecked(false);
                        Utils.saveToPreferences(this, PreferencesKeys.storage, false);
                        Utils.gotoSetting(this);

                    }
                }
            }
        }
    }


}
