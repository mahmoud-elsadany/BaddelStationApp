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
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response.append(br.readLine());
                }else if (responseCode == HttpsURLConnection.HTTP_MOVED_TEMP) {
                    //302
                    response.append(conn.getHeaderField("location"));
                }else if (responseCode == HttpsURLConnection.HTTP_BAD_REQUEST){
                    //400
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response.append(br.readLine());
                    response.append("..phone_number_missed");
                }else if (responseCode == HttpsURLConnection.HTTP_FORBIDDEN){
                    //403
                    response.append("no_available_bikes, error_while_send_sms");
                } else {
                    response.append("Error Registering");
                }

            }else{
                String parameters = getPostDataParametersString(postDataParams);

                parameters = removeLastChar(parameters);

                URL url = new URL(requestURL+"?"+parameters);

                Log.d("payment",url.toString());

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
                }else if (responseCode == HttpsURLConnection.HTTP_MOVED_TEMP || responseCode == 400) {

                    response.append(conn.getHeaderField("location"));

                } else {
                    response.append("Error Registering");
                }

                Log.d("postResponse",url.toString());


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
