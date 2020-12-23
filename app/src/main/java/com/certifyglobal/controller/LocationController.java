package com.certifyglobal.controller;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;

public class LocationController {
    private static LocationController instance = null;
    private Location currentLocation = null;
    public static LocationController getInstance() {
        if (instance == null) {
            instance = new LocationController();
        }
        return instance;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public boolean isCurrentLocationInRange(float mileRange) {
        Log.d("Location", "Check range");
        boolean result = false;
        return result;
    }

}
