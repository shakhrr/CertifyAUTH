
package com.certifyglobal.authenticator;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.certifyglobal.authenticator.barcodescanning.ScanFrameProcessor;
import com.certifyglobal.utils.Logger;
import com.certifyglobal.utils.PreferencesKeys;
import com.certifyglobal.utils.Utils;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.FotoapparatBuilder;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.selector.FocusModeSelectorsKt;
import io.fotoapparat.selector.SelectorsKt;
import io.fotoapparat.view.CameraView;

import static io.fotoapparat.selector.LensPositionSelectorsKt.back;


public class ScanActivity extends Activity {
    private static final String TAG = "ScanActivity - ";
    private Fotoapparat fotoapparat;
    private String type;
    private static ScanBroadcastReceiver receiver;
    public boolean QRCodeScanBoolean;

    public class ScanBroadcastReceiver extends BroadcastReceiver {
        public static final String ACTION = "com.certifyglobal.authenticator.ACTION_CODE_SCANNED";

        @Override
        public void onReceive(Context context, Intent intentOf) {
            try {

                String text = intentOf.getStringExtra("scanResult");
                if (QRCodeScanBoolean && (type.equals("BioSign") || text != null && (text.contains("*") || text.contains("otpauth://totp")))) {
                    QRCodeScanBoolean = false;
                    switch (type) {
                        case "Barcode":
                            Intent intent = new Intent(ScanActivity.this, QRUrlScanResults.class);
                            intent.putExtra("Url", text);
                            startActivity(intent);
                            break;
                        case "BioSign":
                            Intent intent2 = new Intent(ScanActivity.this, BioSignActivity.class);
                            intent2.putExtra("Url", text);
                            startActivity(intent2);
                            break;
                    }
                    finish();
                }
            } catch (Exception e) {
                Logger.error(TAG + "onReceive(Context context, Intent intentOf)", e.getMessage());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            this.unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            // catch exception, when trying to unregister receiver again
            // there seems to be no way to check, if receiver if registered
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            receiver = new ScanBroadcastReceiver();
            this.registerReceiver(receiver, new IntentFilter(ScanBroadcastReceiver.ACTION));
            setContentView(R.layout.scan);
            if (!Utils.readFromPreferences(ScanActivity.this, PreferencesKeys.isLogin, false)) {
                Utils.getDeviceUUid(this);
                Utils.getNumberVersion(this);
                Utils.getHMacSecretKey(this);
            }
            CameraView cameraView = findViewById(R.id.camera_view);
            Intent intentGet = getIntent();
            type = intentGet.getStringExtra("type");
            FotoapparatBuilder builder = Fotoapparat.with(this);
            //builder.lensPosition(LensPositionSelectors.back());
            builder.focusMode(SelectorsKt.firstAvailable(FocusModeSelectorsKt.continuousFocusPicture(),
                    FocusModeSelectorsKt.autoFocus(),
                    FocusModeSelectorsKt.fixed()));

            fotoapparat = builder.into(cameraView).previewScaleType(ScaleType.CenterCrop).lensPosition(back()).frameProcessor(new ScanFrameProcessor(this)).build();
            QRCodeScanBoolean = true;
        } catch (Exception e) {
            Logger.error(TAG + "onCreate(Bundle savedInstanceState)", e.getMessage());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            fotoapparat.start();
        } catch (Exception e) {
            Logger.error(TAG + "onStart()", e.getMessage());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            fotoapparat.stop();
        } catch (Exception e) {
            Logger.error(TAG + "onStop()", e.getMessage());
        }
    }
}
