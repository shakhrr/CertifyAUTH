package com.certifyglobal.async_task;

import android.content.Context;
import android.os.AsyncTask;

import com.certifyglobal.callback.Communicator;
import com.certifyglobal.utils.Logger;

import org.jsoup.Jsoup;

public class GetVersionCode extends AsyncTask<Void, String, String> {
    Context context;
    Communicator componentCallbacks;

    public GetVersionCode(Context context, Communicator componentCallbacks) {
        this.context = context;
        this.componentCallbacks = componentCallbacks;
    }

    @Override
    protected String doInBackground(Void... voids) {

        String newVersion = null;
        if (context == null) return "";
        try {

            newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + context.getPackageName() + "&hl=it")
                    // .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get()
                    .select(".hAyfc .htlgb")
                    .get(7) // .select("div[itemprop=softwareVersion]")
                    .ownText();
            return newVersion;
        } catch (Exception e) {
            return newVersion;
        }
    }

    @Override
    protected void onPostExecute(String onlineVersion) {
        super.onPostExecute(onlineVersion);
        try {
            if (componentCallbacks != null)
                componentCallbacks.setAction(onlineVersion, 0);
        } catch (Exception ignor) {

        }
    }
}