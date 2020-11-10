package com.certifyglobal.async_task;


import android.os.AsyncTask;

import com.certifyglobal.callback.JSONObjectCallback;
import com.certifyglobal.callback.JSONObjectCallbackImage;
import com.certifyglobal.utils.Utils;

import org.json.JSONObject;

public class AsyncJSONObjectImageUpdate extends AsyncTask<Void, Void, String> {
    private JSONObjectCallbackImage myComponent;
    private JSONObject req;
    private String url;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    public AsyncJSONObjectImageUpdate(JSONObject req, JSONObjectCallbackImage myComponent, String url) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
    }

    @Override
    protected String doInBackground(Void... params) {
        return Utils.getJSONObjectImage(req, url,"");
    }

    @Override
    protected void onPostExecute(String reportInfo) {
        if (myComponent == null) return;
        myComponent.onJSONObjectListenerImage(reportInfo, url,req);
    }
}
