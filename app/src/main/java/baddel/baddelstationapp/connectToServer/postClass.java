package baddel.baddelstationapp.connectToServer;

import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.HttpsURLConnection;

import baddel.baddelstationapp.internalStorage.Session;
import baddel.baddelstationapp.saveLogs.myLogs;
import info.guardianproject.netcipher.NetCipher;

/**
 * Created by mahmoud on 2016-10-19.
 */
public class postClass {
    /**
     * Send post request string.
     *
     * @param requestURL     the request url
     * @param postDataParams the post data params
     * @return the string
     */
    public String sendPostRequest(final String RequestUser, final String RequestPassword, String requestURL, HashMap<String, String> postDataParams) {
        //URL url;
        //String response = "";
        StringBuilder response = new StringBuilder();

        try {
            if (RequestUser != null && RequestPassword != null) {
                URL url = new URL(requestURL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                 /*basic Authentication*/
                String userpass = RequestUser + ":" + RequestPassword;
                String basicAuth = "Basic " + new String(Base64.encode(userpass.getBytes(), Base64.NO_WRAP));
                conn.setRequestProperty("Authorization", basicAuth);
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                /**/
                conn.setReadTimeout(20000);
                conn.setConnectTimeout(20000);
                conn.setRequestMethod("POST");

                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();

                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    Session.getInstance().setResponseCode(responseCode);
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response.append(br.readLine());
                }else if (responseCode == HttpsURLConnection.HTTP_CLIENT_TIMEOUT){
                    Session.getInstance().setResponseCode(responseCode);
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    response.append(br.readLine());
                    response.append("TimeOut");
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                } else if (responseCode == HttpsURLConnection.HTTP_MOVED_TEMP) {
                    //302
                    Session.getInstance().setResponseCode(responseCode);
                    response.append(conn.getHeaderField("location"));
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                }else if (responseCode == HttpsURLConnection.HTTP_BAD_REQUEST){
                    //400
                    Session.getInstance().setResponseCode(responseCode);
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    response.append(br.readLine());
                    response.append("..phone_number_missed");
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                }else if (responseCode == HttpsURLConnection.HTTP_FORBIDDEN){
                    //403
                    Session.getInstance().setResponseCode(responseCode);
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    response.append(br.readLine());
                    response.append("no_available_bikes, error_while_send_sms");
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                }else if (responseCode == HttpsURLConnection.HTTP_NOT_FOUND){
                    //404
                    Session.getInstance().setResponseCode(responseCode);
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    response.append(br.readLine());
                    response.append("Trip Not Found");
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                } else {
                    Session.getInstance().setResponseCode(responseCode);
                    response.append("Error Registering");
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                }

            }else{
                String parameters = getPostDataParametersString(postDataParams);

                parameters = removeLastChar(parameters);

                URL url = new URL(requestURL+"?"+parameters);

                myLogs.logMyLog("payment",url.toString());
                //Log.d("payment",url.toString());

                HttpsURLConnection conn = NetCipher.getHttpsURLConnection(url);

                conn.setRequestProperty("Content-Type", "text/plain");
                conn.setRequestProperty("Accept", "application/json, text/plain, */*");
                conn.setReadTimeout(20000);
                conn.setConnectTimeout(20000);
                conn.setRequestMethod("POST");

                conn.setDoInput(true);
                conn.setDoOutput(true);

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response.append(br.readLine());
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                }else if (responseCode == HttpsURLConnection.HTTP_MOVED_TEMP || responseCode == HttpsURLConnection.HTTP_BAD_REQUEST) {
                    response.append(conn.getHeaderField("location"));
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                }else if (responseCode == HttpsURLConnection.HTTP_FORBIDDEN){
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response.append(br.readLine());
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                }else if (responseCode == HttpsURLConnection.HTTP_NOT_FOUND){
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response.append(br.readLine());
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                }else if (responseCode == HttpsURLConnection.HTTP_CLIENT_TIMEOUT){
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response.append(br.readLine());
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                }else if (responseCode == HttpsURLConnection.HTTP_ACCEPTED){
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response.append(br.readLine());
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                }else if (responseCode == HttpsURLConnection.HTTP_GONE){
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response.append(br.readLine());
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                } else if (responseCode == HttpsURLConnection.HTTP_CONFLICT){
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response.append(br.readLine());
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                }else if (responseCode == HttpsURLConnection.HTTP_CREATED){
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response.append(br.readLine());
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                }else if (responseCode == HttpsURLConnection.HTTP_REQ_TOO_LONG){
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response.append(br.readLine());
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                }else if (responseCode == HttpsURLConnection.HTTP_INTERNAL_ERROR){
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response.append(br.readLine());
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                }else if (responseCode == HttpsURLConnection.HTTP_MULT_CHOICE){
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response.append(br.readLine());
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                }else{
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response.append(br.readLine());
                    myLogs.logMyLog("postResponse","responseCode: "+responseCode+"**"+response);
                }

                myLogs.logMyLog("postResponse",url.toString());
                //Log.d("postResponse",url.toString());


            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.toString();
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
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


    private String getPostDataParametersString(HashMap<String, String> params) throws UnsupportedEncodingException {
        TreeMap<String, String> treeMap = new TreeMap<>(params);

        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : treeMap.entrySet()) {
            if (first)
                first = false;
            else
                result.append("");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(entry.getValue());
            result.append("&");
        }

        return result.toString();
    }

    private String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }
}
