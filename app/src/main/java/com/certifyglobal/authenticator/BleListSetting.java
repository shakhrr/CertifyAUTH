package com.certifyglobal.authenticator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.certifyglobal.authenticator.ble.BleWrapper;
import com.certifyglobal.authenticator.ble.BleWrapperUiCallbacks;
import com.certifyglobal.authenticator.facedetection.DeviceListAdapter;
import com.certifyglobal.utils.Logger;

import java.lang.reflect.Method;

public class BleListSetting extends AppCompatActivity {
    private BleWrapper mBleWrapper = null;
    private String LAG = "BleListSetting - ";
    private boolean mScanning = false;
    ListView mListView;
    private static final int ENABLE_BT_REQUEST_ID = 1;
    private DeviceListAdapter mDevicesListAdapter = null;
    private static int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.ble_list);
            mListView = findViewById(R.id.listView);


            mBleWrapper = new BleWrapper(this, new BleWrapperUiCallbacks.Null() {

                public void uiDeviceFound(final BluetoothDevice device, final int rssi, final byte[] record) {
                    handleFoundDevice(device, rssi, record);
                }
            });
            if (!mBleWrapper.isBtEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            // check if we have BT and BLE on board
            if (!mBleWrapper.checkBleHardwareAvailable()) {
                bleMissing();
            }
              mDevicesListAdapter = new DeviceListAdapter(this);
              mListView.setAdapter(mDevicesListAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        final BluetoothDevice device = mDevicesListAdapter.getDevice(position);
                        if (device == null) return;
                        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                           // unpairDevice(device);
                        } else {
                            Logger.debug("rrrrrrrrrrrrr","66666666666666");
                                                        pairDevice(device);
                        }
//                        final Intent intent = new Intent(BleListSetting.this, BleSetting.class);
//                        intent.putExtra("DEVICE_NAME", device.getName());
//                        intent.putExtra("DEVICE_ADDRESS", device.getAddress());
//                        intent.putExtra("DEVICE_RSSI", mDevicesListAdapter.getRssi(position));
//                        if (mScanning) {
//                            mScanning = false;
//                            mBleWrapper.stopScanning();
//                        }
//                        startActivity(intent);
//                        finish();
                    } catch (Exception e) {
                        Logger.error("mListView -", e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            Logger.error(LAG+"onCreate", e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (!mBleWrapper.isBtEnabled()) {
                // BT is not turned on - ask user to make it enabled
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
                // see onActivityResult to check what is the status of our request
            } else {
                mScanning = true;
                mBleWrapper.initialize();
                mBleWrapper.startScanning();
            }
            // initialize BleWrapper object
            //  mBleWrapperConect.initialize();


        } catch (Exception e) {
            Logger.error(LAG+"onResume", e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScanning = false;
        mBleWrapper.stopScanning();
    }

    private void handleFoundDevice(final BluetoothDevice device,
                                   final int rssi,
                                   final byte[] scanRecord) {
        // adding to the UI have to happen in UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() { //88:6B:0F:03:48:21
                mDevicesListAdapter.addDevice(device, rssi, scanRecord);
                mDevicesListAdapter.notifyDataSetChanged();
//                if (device.getName() != null && device.getName().equalsIgnoreCase("CERTIFYLT-49"))
//                // if (device.getAddress().equals("88:6B:0F:03:48:21"))
//                {
//                    final Intent intent = new Intent(BleListSetting.this, BleSetting.class);
//                    intent.putExtra("DEVICE_NAME", device.getName());
//                    intent.putExtra("DEVICE_ADDRESS", device.getAddress());
//                    intent.putExtra("DEVICE_RSSI", rssi);
//                    if (mScanning) {
//                        mScanning = false;
//                        mBleWrapper.stopScanning();
//                    }
//                    startActivity(intent);
//                    finish();
             //   }
                Logger.debug(LAG, "" + device.getName() + "  " + device.getAddress());

            }
        });
    }

    private void bleMissing() {
        Toast.makeText(this, "BLE Hardware is required but not available!", Toast.LENGTH_LONG).show();
        finish();
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
