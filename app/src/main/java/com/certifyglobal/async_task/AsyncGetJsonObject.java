package com.certifyglobal.async_task;


import android.os.AsyncTask;


import com.certifyglobal.callback.JSONObjectCallback;
import com.certifyglobal.utils.Utils;

import org.json.JSONObject;

public class AsyncGetJsonObject extends AsyncTask<Void, Void, JSONObject> {
    private JSONObjectCallback myComponent;
    private String url;
    private String domain;
    private JSONObject jsonObject;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    public AsyncGetJsonObject(JSONObjectCallback myComponent, String url,String domain) {
        this.myComponent = myComponent;
        this.url = url;
        this.domain = domain;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        return Utils.getQRCodeUrl(url,domain);
    }

    @Override
    protected void onPostExecute(JSONObject reportInfo) {
        if (myComponent == null) return;
        myComponent.onJSONObjectListener(reportInfo, url,jsonObject);
    }
}
