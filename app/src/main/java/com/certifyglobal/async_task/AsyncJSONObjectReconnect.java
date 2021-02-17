package com.certifyglobal.async_task;


import android.os.AsyncTask;

import com.certifyglobal.callback.JSONObjectCallback;
import com.certifyglobal.callback.JSONObjectCallbackReconnect;
import com.certifyglobal.utils.Utils;

import org.json.JSONObject;

public class AsyncJSONObjectReconnect extends AsyncTask<Void, Void, String> {
    private JSONObjectCallbackReconnect myComponent;
    private JSONObject req;
    private String url;
    private String header;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    public AsyncJSONObjectReconnect(JSONObject req, JSONObjectCallbackReconnect myComponent, String url, String header) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
        this.header = header;
    }

    @Override
    protected String doInBackground(Void... params) {
        return Utils.getJSONObjectReconnect(req, url,header);
    }

    @Override
    protected void onPostExecute(String reportInfo) {
        if (myComponent == null) return;
        myComponent.onJSONObjectListenerReconnect(reportInfo, url,req);
    }
}
