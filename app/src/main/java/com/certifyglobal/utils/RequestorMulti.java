package com.certifyglobal.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;


public class RequestorMulti {

    static final int TIME_OUT = 600000;
    static final int TIME_OUT_IMAGE = 600000;


    public static JSONObject MultipartRequest(String urlStr, HttpEntity entity) {
        try {
            if (EndPoints.deployment == EndPoints.Mode.Local) {
                Logger.debug("urlStr", urlStr);
                Logger.debug("req", "" + entity.toString());
            }
            HttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(urlStr);
            httpPost.addHeader("Content-Type", "multipart/form-data");
          //  httpPost.addHeader("Origin", "https://admintest-awrbrlef.certifyauth.com");
          //  httpPost.addHeader("Referer", "https://admintest-awrbrlef.certifyauth.com/editadministrator/10460");
          //  httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");
            // CloseableHttpResponse response2 = httpclient.execute(httpPost);
            //  Logger.debug("req", "" + response2.getStatusLine());
            //Path of the file to be uploaded
            String responseStr = "";

            //Add the data to the multipart entity
            httpPost.setEntity(entity);
            //Execute the post request
            HttpResponse response1 = client.execute(httpPost);
            responseStr = EntityUtils
                    .toString(response1.getEntity());
            Logger.debug("req", "" + responseStr);
            if (responseStr != null && !responseStr.equals(""))
                return new JSONObject(responseStr);
        } catch (Exception e) {
            Logger.error("MultipartRequest(String urlStr, MultipartEntity entity)", e.getMessage());
//        }catch (JSONException e){
//
        }
        return null;
    }
//    public static JSONObject httpEntity(String urlStr,MultipartEntity reqEntity){
////        try{
////            HttpClient httpclient = new DefaultHttpClient();
////            HttpPost httppost = new HttpPost(urlStr);
////
////            httppost.setEntity(reqEntity);
////
////            HttpResponse response = httpclient.execute(httppost);
////            HttpEntity resEntity = response.getEntity();
////        }catch (Exception e){
////
////        }
////        return null;
////    }
}
