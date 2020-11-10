package com.certifyglobal.async_task;


import android.os.AsyncTask;

import com.certifyglobal.authenticator.ApplicationWrapper;
import com.certifyglobal.callback.JSONObjectCallback;
import com.certifyglobal.utils.PreferencesKeys;
import com.certifyglobal.utils.Utils;

public class AsyncGetCheckRoot extends AsyncTask<Void, Void, Boolean> {
    private JSONObjectCallback myComponent;
    private String url;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    public AsyncGetCheckRoot(JSONObjectCallback myComponent, String url) {
        this.myComponent = myComponent;
        this.url = url;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return CheckRoot.checkRoot();
    }

    @Override
    protected void onPostExecute(Boolean reportInfo) {
        Utils.saveToPreferences(ApplicationWrapper.context, PreferencesKeys.checkRoot, reportInfo);
//        if (myComponent == null) return;
//        myComponent.onJSONObjectListener(null, url);
    }
}
