package com.certifyglobal.callback;


import org.json.JSONArray;

public interface PayloadObjectCallback {
    void onPayloadObjectListener(JSONArray report, String status);
}
