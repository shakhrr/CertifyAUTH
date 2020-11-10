package com.certifyglobal.async_task;


import android.os.AsyncTask;

import com.certifyglobal.callback.JSONObjectCallback;
import com.certifyglobal.utils.RequestorMulti;


import org.apache.http.HttpEntity;
import org.json.JSONObject;



public class AsyncJSONObjectMultipart extends AsyncTask<Void, Void, JSONObject> {
    private JSONObjectCallback myComponent;
    private HttpEntity req;
    private String url;
    JSONObject jsonObject;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    public AsyncJSONObjectMultipart(HttpEntity req, JSONObjectCallback myComponent, String url) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        return RequestorMulti.MultipartRequest(url, req);
    }

    @Override
    protected void onPostExecute(JSONObject reportInfo) {
        if (myComponent == null) return;
        myComponent.onJSONObjectListener(reportInfo, url,jsonObject);
    }
}
