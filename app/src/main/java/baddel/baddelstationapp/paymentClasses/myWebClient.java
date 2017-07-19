package baddel.baddelstationapp.paymentClasses;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import baddel.baddelstationapp.Controller.Controller;
import baddel.baddelstationapp.Models.trip_DS;
import baddel.baddelstationapp.connectToServer.myAsyncTask;
import baddel.baddelstationapp.connectToServer.responseDelegate;
import baddel.baddelstationapp.creditCardDataActivity;
import baddel.baddelstationapp.internalStorage.Session;
import baddel.baddelstationapp.startActivity;
import baddel.baddelstationapp.verifyMobileNumberActivity;

/**
 * Created by mahmo on 2017-07-05
 */

public class myWebClient extends WebViewClient implements responseDelegate {

    public Context sourceContext;
    public Class destContext;

    private myAsyncTask myAsyncTask;

    private String payFortTag = "payfort_";

    public myWebClient(Context sourceContext,Class destContext){
        this.sourceContext = sourceContext;
        this.destContext = destContext;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        // TODO Auto-generated method stub
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // TODO Auto-generated method stub
        view.loadUrl(url);
        return true;

    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        //String cookies = CookieManager.getInstance().getCookie(url);

        view.evaluateJavascript(javaScriptFunction(), new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String sessionResponse) {

                if (sessionResponse != null){
                    if (sessionResponse.contains("success")){
                        Log.d(payFortTag, sessionResponse);

                        ArrayList<trip_DS> trips_Array = Session.getInstance().getCurrentTripArrayListObjects();

                        for (trip_DS tripObject:trips_Array) {
                            putSetOrder(tripObject);
                        }


                    }else{
                        Log.d(payFortTag, sessionResponse);
                        //sourceContext.startActivity(new Intent(sourceContext,destContext));
                        Toast.makeText(sourceContext,"Error in Response",Toast.LENGTH_SHORT).show();
                    }
                } else{
                    //sourceContext.startActivity(new Intent(sourceContext,destContext));
                    Toast.makeText(sourceContext,"No Response",Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);

        Log.d(payFortTag, "payfortErrror"+ error.toString());

    }

    private void putSetOrder(trip_DS myTripDS) {
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getAPIMETHODPutSetOrder();
        int myProcessNum = 1;

        JSONObject setOrderObject = new JSONObject();
        try {
            setOrderObject.put("OrderId", Session.getInstance().getOrderId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HashMap<String, String> data = new HashMap<>();
        data.put("id",String.valueOf(myTripDS.tripId));
        data.put("tripPayment", setOrderObject.toString());

        String URL = myURL + apiMethod;

        if (isNetworkConnected()) {

            myAsyncTask = new myAsyncTask(sourceContext, data, URL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 3);

            myAsyncTask.delegate = this;

            myAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else {
            showToast("No Internet Connection");
        }
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);

        Log.d("payfort_HttpError", " error request:  "+ request.toString()+" error response:  "+errorResponse.toString());
    }
    private String javaScriptFunction() {
        return "(function() { return sessionStorage.getItem('msg'); })();";
    }

    @Override
    public void getServerResponse(String response, int ProcessNum) {
        if (ProcessNum == 1){
            Log.d("confirmSetOrder",response);
            trip_DS confirmTrip = new trip_DS(response);

            ArrayList<trip_DS> currentTrips = confirmTrip.currentTripObjects;

            Session.getInstance().setCurrentTripArrayListObject(currentTrips);

            if (currentTrips.size() > 0){
                for (trip_DS tripObject:currentTrips){
                    Controller controller = new Controller();
                    controller.sendToTCP(String.valueOf(tripObject.startSlotNumber),confirmTrip.currentTripObjects.get(0));
                    sourceContext.startActivity(new Intent(sourceContext,destContext));
                }
            }else{
                showToast("There Is Something Wrong");
            }
        }
    }

    //check the connection
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) sourceContext.getSystemService(sourceContext.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
    private void showToast(String message){
        Toast.makeText(sourceContext,message,Toast.LENGTH_LONG).show();
    }
}
