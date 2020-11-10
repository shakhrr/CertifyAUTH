package com.certifyglobal.async_task;


import android.os.AsyncTask;

import com.certifyglobal.callback.JSONObjectCallback;
import com.certifyglobal.utils.Utils;

import org.json.JSONObject;

public class AsyncJSONObjectHeader extends AsyncTask<Void, Void, JSONObject> {
    private JSONObjectCallback myComponent;
    private JSONObject req;
    private String url;
    private String header;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    public AsyncJSONObjectHeader(JSONObject req, JSONObjectCallback myComponent, String url,String header) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
        this.header = header;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        return Utils.getJSONObject(req, url,header);
    }

    @Override
    protected void onPostExecute(JSONObject reportInfo) {
        if (myComponent == null) return;
        myComponent.onJSONObjectListener(reportInfo, url,req);
    }
}
