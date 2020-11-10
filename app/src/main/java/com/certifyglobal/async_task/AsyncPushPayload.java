package com.certifyglobal.async_task;


import android.os.AsyncTask;

import com.certifyglobal.callback.JSONObjectCallback;
import com.certifyglobal.callback.PayloadObjectCallback;
import com.certifyglobal.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class AsyncPushPayload extends AsyncTask<Void, Void, JSONArray> {
    private PayloadObjectCallback myComponent;
    private JSONObject req;
    private String url;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    public AsyncPushPayload(JSONObject req, PayloadObjectCallback myComponent, String url) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
    }

    @Override
    protected JSONArray doInBackground(Void... params) {
        return Utils.getJSONObjectInArray(req, url);
    }

    @Override
    protected void onPostExecute(JSONArray reportInfo) {
        if (myComponent == null) return;
        myComponent.onPayloadObjectListener(reportInfo, url);
    }
}
