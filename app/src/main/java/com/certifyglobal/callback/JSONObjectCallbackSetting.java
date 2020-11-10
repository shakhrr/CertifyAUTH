package com.certifyglobal.callback;

import org.json.JSONObject;

public interface JSONObjectCallbackSetting {
    void onJSONObjectListenerSetting(JSONObject report, String status, JSONObject req);
}
