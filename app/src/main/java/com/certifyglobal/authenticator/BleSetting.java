package com.certifyglobal.authenticator;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.certifyglobal.authenticator.ble.BleWrapper;
import com.certifyglobal.authenticator.ble.BleWrapperUiCallbacks;
import com.certifyglobal.utils.Logger;

import java.util.List;

public class BleSetting extends AppCompatActivity implements BleWrapperUiCallbacks {
    private BleWrapper mBleWrapperConect = null;
    private String LAG = "BleSetting - ";
    private boolean mScanning = false;
    TextView tv_message;
    private static final int ENABLE_BT_REQUEST_ID = 1;
    private String mDeviceName;
    private String mDeviceAddress;
    private String mDeviceRSSI;
    private Button bt_one;
    private Button bt_two;
    TextView tvTitle;
    private EditText etWrite;
    BluetoothGattCharacteristic characteristic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_ble_setting);
            tv_message = findViewById(R.id.tv_message);
            tvTitle = findViewById(R.id.tv_title);
            bt_one = findViewById(R.id.bt_one);
            bt_two = findViewById(R.id.bt_Two);
            etWrite = findViewById(R.id.et_write);
            tv_message.setEnabled(false);
            tvTitle.setText(getResources().getString(R.string.ble_setting));
            final Intent intent = getIntent();
            mDeviceName = intent.getStringExtra("DEVICE_NAME");
            mDeviceAddress = intent.getStringExtra("DEVICE_ADDRESS");
            mDeviceRSSI = intent.getIntExtra("DEVICE_RSSI", 0) + " db";
            bt_one.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        //  Logger.debug("characteristic bt_two",characteristic.getUuid().toString());
                        mBleWrapperConect.requestCharacteristicValue(characteristic);
                    } catch (Exception e) {
                        Logger.error(LAG + "bt_one --onClick", e.getMessage());
                    }
                }
            });
            bt_two.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (etWrite.getText().toString().isEmpty()) return;
//                        int format = mBleWrapperConect.getValueFormat(characteristic);
//                        Logger.debug("bt_two 22222222222222", BleNamesResolver.resolveValueTypeDescription(format));
//                        int props = characteristic.getProperties();
//                        String propertiesString = String.format("0x%04X [", props);
//                        Logger.debug("bt_two 3333333", propertiesString);
                        Logger.debug(LAG, "bt_two"+characteristic.getUuid());
                        byte[] dataToWrite = parseHexStringToBytes("0x0" + etWrite.getText().toString().trim());
                        mBleWrapperConect.writeDataToCharacteristic(characteristic, dataToWrite);
                    } catch (Exception e) {
                        Logger.error(LAG + "bt_two --onClick", e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Logger.error(LAG + "onCreate(Bundle savedInstanceState)", e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (mBleWrapperConect == null) mBleWrapperConect = new BleWrapper(this, this);

            if (mBleWrapperConect.initialize() == false) {
                finish();
            }

            // initialize BleWrapper object
            //  mBleWrapperConect.initialize();
            tv_message.setText("connecting ...");
            mBleWrapperConect.connect(mDeviceAddress);

        } catch (Exception e) {
            Logger.error(LAG + "onResume()", e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            mScanning = false;
            mBleWrapperConect.stopMonitoringRssiValue();
            mBleWrapperConect.diconnect();
            mBleWrapperConect.close();
        } catch (Exception e) {
            Logger.error(LAG + "onPause()", e.getMessage());
        }
    }


    private void bleMissing() {
        Toast.makeText(this, "BLE Hardware is required but not available!", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void uiDeviceFound(BluetoothDevice device, int rssi, byte[] record) {
        try {
            Toast.makeText(this, "DeviceFound!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Logger.error(LAG + "uiDeviceFound", e.getMessage());
        }
    }

    @Override
    public void uiDeviceConnected(BluetoothGatt gatt, BluetoothDevice device) {
        try {
            Logger.debug(LAG, "uiDeviceConnected");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_message.setText("connected");
                    bt_two.setBackgroundColor(getColor(R.color.green));

                }
            });
        } catch (Exception e) {
            Logger.error(LAG + "uiDeviceConnected", e.getMessage());
        }
    }

    @Override
    public void uiDeviceDisconnected(BluetoothGatt gatt, BluetoothDevice device) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_message.setText("disconnected");
                    bt_two.setBackgroundColor(getColor(R.color.gray));

                }
            });
        } catch (Exception e) {
            Logger.error(LAG + "uiDeviceDisconnected", e.getMessage());
        }
    }

    @Override
    public void uiAvailableServices(BluetoothGatt gatt, BluetoothDevice device, List<BluetoothGattService> services) {
        try {
            Logger.debug(LAG, "uiAvailableServices");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (mBleWrapperConect == null) return;
                    for (BluetoothGattService service : mBleWrapperConect.getCachedServices()) {
                        // Logger.debug(LAG, "" + service.toString() + "  " + service.getUuid());
                        mBleWrapperConect.getCharacteristicsForService(service);
                    }

                }
            });


        } catch (Exception e) {
            Logger.error(LAG + "uiAvailableServices", e.getMessage());
        }
    }

    @Override
    public void uiCharacteristicForService(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, final List<BluetoothGattCharacteristic> chars) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Logger.debug(LAG, "uiCharacteristicForService");
                    for (BluetoothGattCharacteristic ch : chars) {
                        //  Logger.debug(LAG, "" + ch.toString() + "  " + ch.getUuid());
                        //mCharacteristicsListAdapter.addCharacteristic(ch);
                        //    characteristic =ch;
                        uiCharacteristicsDetails(mBleWrapperConect.getGatt(), mBleWrapperConect.getDevice(), mBleWrapperConect.getCachedService(), chars.get(0));

                    }
                }
            });


        } catch (Exception e) {
            Logger.error(LAG + "uiCharacteristicForService", e.getMessage());
        }
    }

    @Override
    public void uiCharacteristicsDetails(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, final BluetoothGattCharacteristic cha) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Logger.debug(LAG, "uiCharacteristicsDetails");
                    characteristic = cha;
                    mBleWrapperConect.requestCharacteristicValue(cha);
                    Logger.debug(LAG, "uiCharacteristicsDetails  " + characteristic.getUuid());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Logger.debug("uiCharacteristicsDetails", "senddata");
                            byte[] dataToWrite = parseHexStringToBytes("0x012345");
                            mBleWrapperConect.writeDataToCharacteristic(characteristic, mDeviceName.getBytes());
                        }
                    }, 300000);


                }
            });

        } catch (Exception e) {
            Logger.error(LAG + "uiCharacteristicsDetails", e.getMessage());
        }
    }

    @Override
    public void uiNewValueForCharacteristic(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String strValue, int intValue, final byte[] mRawValue, String timestamp) {
        try {
            Logger.debug(LAG, "uiCharacteristicsDetails");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mRawValue != null && mRawValue.length > 0) {
                        final StringBuilder stringBuilder = new StringBuilder(mRawValue.length);
                        for (byte byteChar : mRawValue)
                            stringBuilder.append(String.format("%02X", byteChar));
                        //    tv_message.setText("0x" + stringBuilder.toString());
                    } else tv_message.setText("");

                }
            });


        } catch (Exception e) {
            Logger.error(LAG + "uiNewValueForCharacteristic", e.getMessage());
        }
    }

    @Override
    public void uiGotNotification(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic characteristic) {
        // tv_message.setText("uiGotNotification");
    }

    @Override
    public void uiSuccessfulWrite(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Successfully Write", Toast.LENGTH_LONG).show();
                // tv_message.setText("uiSuccessfulWrite");
            }
        });
    }

    @Override
    public void uiFailedWrite(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //  Toast.makeText(getApplicationContext(), "Writing to " + description + " FAILED!", Toast.LENGTH_LONG).show();
                // tv_message.setText("uiFailedWrite");
                Toast.makeText(getApplicationContext(), "FAILED Write", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void uiNewRssiAvailable(BluetoothGatt gatt, BluetoothDevice device, int rssi) {
        // tv_message.setText("uiNewRssiAvailable");
    }

    public byte[] parseHexStringToBytes(final String hex) {
        String tmp = hex.substring(2).replaceAll("[^[0-9][a-f]]", "");
        byte[] bytes = new byte[tmp.length() / 2]; // every two letters in the string are one byte finally

        String part = "";

        for (int i = 0; i < bytes.length; ++i) {
            part = "0x" + tmp.substring(i * 2, i * 2 + 2);
            bytes[i] = Long.decode(part).byteValue();
        }

        return bytes;
    }
}
