package com.certifyglobal.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.content.ContextCompat;

import com.certifyglobal.authenticator.PushNotificationActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GPSTracker extends Service implements LocationListener {

    @SuppressLint("StaticFieldLeak")
    private static GPSTracker thisObj = null;
    private final Context mContext;
    private Location location;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60;
    private String provider_info = "";
    private static final String LOG = "GPSTracker - ";
    private static LocationManager locationManager;
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    Geocoder geocoder;
    List<Address> addresses;

    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
        thisObj = this;
    }



    @SuppressWarnings("MissingPermission")
    public void getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            if (locationManager != null) {
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            }
            if (isGPSEnabled) {
                provider_info = LocationManager.GPS_PROVIDER;
            } else if (isNetworkEnabled) {
                provider_info = LocationManager.NETWORK_PROVIDER;
            }else{

            }
            if (!provider_info.isEmpty()) {
                locationManager.requestLocationUpdates(
                        provider_info,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        this
                );
                location = locationManager.getLastKnownLocation(provider_info);
                updateGPSCoordinates();
            }
        } catch (Exception e) {
            Logger.error(LOG + "getLocation()", e.getMessage());
        }
    }

    private void getCurrentAddress() {
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();
        Utils.saveToPreferences(mContext,PreferencesKeys.Deviceaddress,city+ ","+ state +", "+ "" +country);
    }

    private void updateGPSCoordinates() {
        try {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                getCurrentAddress();
            }
        } catch (Exception e) {
            Logger.error(LOG + "updateGPSCoordinates()", e.getMessage());
        }
    }

    public double getLatitude() {
        try {
            if (location != null) {
                latitude = location.getLatitude();
            }
        } catch (Exception e) {
            Logger.error(LOG + "getLatitude()", e.getMessage());
        }
        return latitude;
    }

    public double getLongitude() {
        try {
            if (null != location) {
                longitude = location.getLongitude();
            }
        } catch (Exception e) {
            Logger.error(LOG + "getLongitude()", e.getMessage());
        }
        return longitude;
    }

    public Location getLocationObj() {
        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        thisObj = null;
        super.onDestroy();
    }

    public static void stopGPS() {
        try {
            if (locationManager != null && thisObj != null) locationManager.removeUpdates(thisObj);
            locationManager = null;
        } catch (Exception e) {
            Logger.error("stopGPS()", e.getMessage());
        }
    }
}
