package com.certifyglobal.async_task;


import android.os.AsyncTask;

import com.certifyglobal.callback.JSONObjectCallback;
import com.certifyglobal.callback.JSONObjectCallbackSetting;
import com.certifyglobal.utils.Utils;

import org.json.JSONObject;

public class AsyncJSONObjectSenderSetting extends AsyncTask<Void, Void, JSONObject> {
    private JSONObjectCallbackSetting myComponent;
    private JSONObject req;
    private String url;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    public AsyncJSONObjectSenderSetting(JSONObject req, JSONObjectCallbackSetting myComponent, String url) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        return Utils.getJSONObject(req, url,"");
    }

    @Override
    protected void onPostExecute(JSONObject reportInfo) {
        if (myComponent == null) return;
        myComponent.onJSONObjectListenerSetting(reportInfo, url,req);
    }
}
