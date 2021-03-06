package baddel.baddelstationapp.connectToServer;


import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import baddel.baddelstationapp.internalStorage.Session;
import baddel.baddelstationapp.saveLogs.myLogs;


public class getClass {

    public String getJSON(final String RequestUser, final String RequestPassword, String urlSTR, HashMap<String, String> dataParams) {
        //url/value
        StringBuilder current = new StringBuilder();
        String getUrl = urlSTR;

        if (dataParams != null) {
            for (Map.Entry<String, String> entry : dataParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                getUrl += "" + value;
            }
        }

        myLogs.logMyLog("getTestConnection: ", "get url:" + getUrl);
        //Log.d("getTestConnection: ", "get url:" + getUrl);

        URL url;
        HttpURLConnection httpConnection = null;
        try {
            url = new URL(getUrl);

            httpConnection = (HttpURLConnection) url.openConnection();
            if (RequestPassword != null && RequestUser != null) {
                /*basic Authentication*/
                String userpass = RequestUser + ":" + RequestPassword;
                String basicAuth = "Basic " + new String(Base64.encode(userpass.getBytes(), Base64.NO_WRAP));
                httpConnection.setRequestProperty("Authorization", basicAuth);
                /**/
            }

            httpConnection.setReadTimeout(20000);
            httpConnection.setConnectTimeout(20000);

            int responseCode = httpConnection.getResponseCode();

//            BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
//            current.append(br.readLine());

            if (responseCode == httpConnection.HTTP_OK){
                Session.getInstance().setResponseCode(responseCode);
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                current.append(br.readLine());
                myLogs.logMyLog("getResponse","responseCode: "+responseCode+"**"+current);
            }else if (responseCode == httpConnection.HTTP_FORBIDDEN){
                Session.getInstance().setResponseCode(responseCode);
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                current.append(br.readLine());
                myLogs.logMyLog("getResponse","responseCode: "+responseCode+"**"+current);
            }else if (responseCode == httpConnection.HTTP_NOT_FOUND){
                Session.getInstance().setResponseCode(responseCode);
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
                current.append(br.readLine());
                myLogs.logMyLog("getResponse","responseCode: "+responseCode+"**"+current);
            }else if (responseCode == httpConnection.HTTP_CLIENT_TIMEOUT){
                Session.getInstance().setResponseCode(responseCode);
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
                current.append(br.readLine());
                myLogs.logMyLog("getResponse","responseCode: "+responseCode+"**"+current);
            }else if (responseCode == httpConnection.HTTP_MOVED_TEMP){
                Session.getInstance().setResponseCode(responseCode);
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                current.append(br.readLine());
                myLogs.logMyLog("getResponse","responseCode: "+responseCode+"**"+current);
            }else if (responseCode == httpConnection.HTTP_BAD_REQUEST){
                Session.getInstance().setResponseCode(responseCode);
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
                current.append(br.readLine());
                myLogs.logMyLog("getResponse","responseCode: "+responseCode+"**"+current);
            }else if (responseCode == httpConnection.HTTP_ACCEPTED){
                Session.getInstance().setResponseCode(responseCode);
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                current.append(br.readLine());
                myLogs.logMyLog("getResponse","responseCode: "+responseCode+"**"+current);
            }else if (responseCode == httpConnection.HTTP_GONE){
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                current.append(br.readLine());
                myLogs.logMyLog("getResponse","responseCode: "+responseCode+"**"+current);
            } else if (responseCode == httpConnection.HTTP_CONFLICT){
                Session.getInstance().setResponseCode(responseCode);
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
                current.append(br.readLine());
                myLogs.logMyLog("getResponse","responseCode: "+responseCode+"**"+current);
            }else if (responseCode == httpConnection.HTTP_CREATED){
                Session.getInstance().setResponseCode(responseCode);
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                current.append(br.readLine());
                myLogs.logMyLog("getResponse","responseCode: "+responseCode+"**"+current);
            }else if (responseCode == httpConnection.HTTP_REQ_TOO_LONG){
                Session.getInstance().setResponseCode(responseCode);
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
                current.append(br.readLine());
                myLogs.logMyLog("getResponse","responseCode: "+responseCode+"**"+current);
            }else if (responseCode == httpConnection.HTTP_INTERNAL_ERROR){
                Session.getInstance().setResponseCode(responseCode);
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
                current.append(br.readLine());
                myLogs.logMyLog("getResponse","responseCode: "+responseCode+"**"+current);
            }else if (responseCode == httpConnection.HTTP_MULT_CHOICE){
                Session.getInstance().setResponseCode(responseCode);
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
                current.append(br.readLine());
                myLogs.logMyLog("getResponse","responseCode: "+responseCode+"**"+current);
            }else{
                Session.getInstance().setResponseCode(responseCode);
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
                current.append(br.readLine());
                myLogs.logMyLog("getResponse","responseCode: "+responseCode+"**"+current);
            }

            //InputStream in = httpConnection.getInputStream();
//            InputStreamReader isw = new InputStreamReader(in);
//            int data = isw.read();
//            String current = "";
//            while (data != -1) {
//                current += (char) data;
//                data = isw.read();
//            }

            return current.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "GetRequestError"+e.toString();
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

}

