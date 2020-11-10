package com.certifyglobal.async_task;


import android.os.AsyncTask;

import com.certifyglobal.callback.JSONObjectCallback;
import com.certifyglobal.utils.Utils;

import org.json.JSONObject;

public class AsyncJSONObjectSender extends AsyncTask<Void, Void, JSONObject> {
    private JSONObjectCallback myComponent;
    private JSONObject req;
    private String url;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    public AsyncJSONObjectSender(JSONObject req, JSONObjectCallback myComponent, String url) {
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
        myComponent.onJSONObjectListener(reportInfo, url,req);
    }
}
