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


            int responseCode = httpConnection.getResponseCode();

            BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            current.append(br.readLine());

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

