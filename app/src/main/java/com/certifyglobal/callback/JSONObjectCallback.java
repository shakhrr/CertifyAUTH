package com.certifyglobal.callback;

import org.json.JSONObject;

public interface JSONObjectCallback {
    void onJSONObjectListener(JSONObject report, String status,JSONObject req);
}
