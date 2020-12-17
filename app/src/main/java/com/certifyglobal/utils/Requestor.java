package com.certifyglobal.utils;


import com.microsoft.appcenter.analytics.Analytics;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Requestor {

    static final int TIME_OUT = 600000;
    static final int TIME_OUT_IMAGE = 600000;

    public static String requestJson(String urlStr, JSONObject reqPing, String header) {
        String responseStr = null;
        String[] endPoint=urlStr.split(".com/");
//        if (EndPoints.deployment == EndPoints.Mode.Local) {
            Logger.debug("deepurlStr", urlStr);
            Logger.debug("deepreq", reqPing.toString());
     //   }
        try {
            HttpPost httpost = new HttpPost(urlStr);
            httpost.addHeader("Content-type", "application/json");
            if(!header.equals(""))
            httpost.addHeader("CompanyHostName", header);
           // httpost.addHeader("Content-type", "application/form-data");
            DefaultHttpClient httpclient1 = (DefaultHttpClient) WebClientDevWrapper.getNewHttpClient();
            httpost.setEntity(new StringEntity(reqPing.toString(), "UTF-8"));

            HttpResponse responseHttp = httpclient1.execute(httpost);
            StatusLine status = responseHttp.getStatusLine();
            if(status.getStatusCode()== HttpStatus.SC_OK) {
                responseStr = EntityUtils
                        .toString(responseHttp.getEntity());
                Logger.debug("deepresponse", responseStr);

            }else {
                JSONObject objMessage = new JSONObject();
                objMessage.put("Message", "Server is down Please try again!!!");
                 responseStr=objMessage.toString();

                Map<String, String> properties = new HashMap<>();
                for(Iterator<String> iter = reqPing.keys(); iter.hasNext();) {
                    String key = iter.next();
                    String value = reqPing.optString(key);
                    properties.put(key,value);
                }
                properties.put("URL:",urlStr);
                properties.put("Response:",responseStr);
                Analytics.trackEvent(endPoint[1], properties);
                Logger.debug("deep invalid response",responseStr);

            }
          // String.valueOf(responseStr);

            if (EndPoints.deployment == EndPoints.Mode.Local) {
                Logger.debug("responseStr", responseStr);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            Map<String, String> properties = new HashMap<>();
            for(Iterator<String> iter = reqPing.keys(); iter.hasNext();) {
                String key = iter.next();
                String value = reqPing.optString(key);
                properties.put(key,value);
            }
            properties.put("URL:",urlStr);
            properties.put("Response:",responseStr);
            Analytics.trackEvent(endPoint[1], properties);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> properties = new HashMap<>();
            for(Iterator<String> iter = reqPing.keys(); iter.hasNext();) {
                String key = iter.next();
                String value = reqPing.optString(key);
                properties.put(key,value);
            }
            properties.put("URL:",urlStr);
            properties.put("Response:",responseStr);
            Analytics.trackEvent(endPoint[1], properties);
        }
        return responseStr;
    }

    public static String getRequest(String urlStr,String header) {
        String data;
        String[] endPoint=urlStr.split(".com/");

        try {
            if (EndPoints.deployment == EndPoints.Mode.Local)
                Logger.debug("urlStr", urlStr);
            BufferedReader in;
            DefaultHttpClient httpclient1 = (DefaultHttpClient) WebClientDevWrapper.getNewHttpClient();
            URI website = new URI(urlStr);
            HttpGet request = new HttpGet();
            if(!header.equals(""))
            request.setHeader("CompanyHostName",header);
            request.setURI(website);
            HttpResponse response = httpclient1.execute(request);
//            StatusLine status = response.getStatusLine();
//            if(status.getStatusCode()== HttpStatus.SC_OK) {
                in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuffer sb = new StringBuffer("");
                String l = "";
                String nl = System.getProperty("line.separator");
                while ((l = in.readLine()) != null) {
                    sb.append(l + nl);
                }
                in.close();
                data = sb.toString();
            if (EndPoints.deployment == EndPoints.Mode.Local)
                Logger.debug("data", data);
            return data;
        } catch (java.net.UnknownHostException unhe) {
            Logger.error("getRequest(String urlStr)", unhe.getMessage());
            data = "A Server with the specified hostname could not be found.";
            Map<String, String> properties = new HashMap<>();
            properties.put("URL:",urlStr);
            properties.put("Response:",data);
            Analytics.trackEvent(endPoint[1], properties);
        } catch (Exception e){
            data ="The service is unavailable";

            Map<String, String> properties = new HashMap<>();
            properties.put("URL:",urlStr);
            properties.put("Response:",data);
            Analytics.trackEvent(endPoint[1], properties);
        }
        return data;
    }



    public static String getRequestJSON(String urlStr,String header) {
        String[] endPoint = urlStr.split(".com/");
        String data;

        try {

            if (EndPoints.deployment == EndPoints.Mode.Local)
                Logger.debug("urlStr", urlStr);

            BufferedReader in;
            HttpClient client = new DefaultHttpClient();
            URI website = new URI(urlStr);
            HttpGet request = new HttpGet();
            if(!header.equals(""))
                request.setHeader("CompanyHostName",header);
            request.setURI(website);
            // request.setHeader("Authorization","Bearer "+token);
            HttpResponse response = client.execute(request);
            response.getStatusLine().getStatusCode();
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String l = "";
            String nl = System.getProperty("line.separator");
            while ((l = in.readLine()) != null) {
                sb.append(l + nl);
            }
            in.close();
            data = sb.toString();
            if (EndPoints.deployment == EndPoints.Mode.Local)
                Logger.debug("data", data);
            return data;
           } catch (java.net.UnknownHostException unhe) {
        Logger.error("getRequest(String urlStr)", unhe.getMessage());
        data = "A Server with the specified hostname could not be found.";
        Map<String, String> properties = new HashMap<>();
        properties.put("URL:",urlStr);
        properties.put("Response:",data);
        Analytics.trackEvent(endPoint[1], properties);
    } catch (Exception e){
        data ="The service is unavailable";

        Map<String, String> properties = new HashMap<>();
        properties.put("URL:",urlStr);
        properties.put("Response:",data);
        Analytics.trackEvent(endPoint[1], properties);
    }
        return data;
    }

}
