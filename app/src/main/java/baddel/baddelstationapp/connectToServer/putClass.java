package baddel.baddelstationapp.connectToServer;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.HttpsURLConnection;

import baddel.baddelstationapp.internalStorage.Session;

/**
 * Created by mahmo on 2017-06-18.
 */

public class putClass {

    public String sendPutRequest(final String RequestUser, final String RequestPassword, String requestURL, HashMap<String, String> data) {

        HttpURLConnection urlConnection = null;
        StringBuilder response = new StringBuilder();

        try {
            URL urlToRequest;

            if (data.size() > 1 && !Session.getInstance().getCancelTrip()) {
                //Query String in index 0
                String parameters = putDataParametersQueryString(data);
                parameters = removeLastChar(parameters);
                urlToRequest = new URL(requestURL + "?" + parameters);
            }else if (data.size() == 1 && Session.getInstance().getCancelTrip()) {
                //Query String in index 0
                String parameters = putDataParametersQueryString(data);
                parameters = removeLastChar(parameters);
                urlToRequest = new URL(requestURL + "?" + parameters);
                Session.getInstance().setCancelTrip(false);
            } else {
                //request with no query String
                urlToRequest = new URL(requestURL);
            }


            Log.d("payment", urlToRequest.toString());

            urlConnection = (HttpURLConnection) urlToRequest.openConnection();
            /*basic Authentication*/
            String userpass = RequestUser + ":" + RequestPassword;
            String basicAuth = "Basic " + new String(Base64.encode(userpass.getBytes(), Base64.NO_WRAP));
            urlConnection.setRequestProperty("Authorization", basicAuth);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            /**/
            urlConnection.setReadTimeout(20000);
            urlConnection.setConnectTimeout(20000);
            urlConnection.setRequestMethod("PUT");

            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);

            OutputStream out = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

            if (data.size() > 1) {
                //Query String in index 0
                writer.write(getPutDataStringWithQueryString(data));

            } else {
                //request with no query String
                writer.write(getPutDataString(data));
            }

            writer.close();
            out.close();

//            BufferedReader br = new BufferedReader(new InputStreamReader(in));
//            response.append(br.readLine());

            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();

            Log.d("putResponseCodeTag", String.valueOf(responseCode));

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                response.append(br.readLine());
            }else if (responseCode == HttpsURLConnection.HTTP_MOVED_TEMP) {
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                response.append(br.readLine());
            }else if (responseCode == 400) {
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                response.append(br.readLine());
            }else if (responseCode == 500){
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                response.append(br.readLine());
            } else if (responseCode == 404) {
                response.append("404");
            }else if (responseCode == 403) {
                response.append("403");
            }


            return response.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "Error" + e.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error" + e.toString();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

    }

    private String getPutDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(entry.getValue());
        }

        return result.toString();
    }

    private String getPutDataStringWithQueryString(HashMap<String, String> params) throws UnsupportedEncodingException {

        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {

            if (!entry.getKey().equals("id")){
                result.append(entry.getValue());
                return result.toString();
            }

        }

        return result.toString();
    }


    private String putDataParametersQueryString(HashMap<String, String> params) throws UnsupportedEncodingException {
        TreeMap<String, String> treeMap = new TreeMap<>(params);

        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : treeMap.entrySet()) {
//            if (first) {
//                first = false;
//            }else if (!first){
//                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
//                result.append("=");
//                result.append(entry.getValue());
//                result.append("&");
//                return result.toString();
//            }else{
//                result.append("");
//            }
            if (entry.getKey().equals("id")){
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(entry.getValue());
                result.append("&");
                return result.toString();
            }else{
                result.append("");
            }

        }

        return result.toString();
    }

    private String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }


}
