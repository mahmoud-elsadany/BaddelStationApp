package baddel.baddelstationapp.paymentClasses;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;
import android.webkit.SslErrorHandler;
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
import java.util.Timer;

import baddel.baddelstationapp.Controller.Controller;
import baddel.baddelstationapp.Models.trip_DS;
import baddel.baddelstationapp.connectToServer.myAsyncTask;
import baddel.baddelstationapp.connectToServer.responseDelegate;
import baddel.baddelstationapp.creditCardDataActivity;
import baddel.baddelstationapp.customViews.customDialogs;
import baddel.baddelstationapp.internalStorage.Session;
import baddel.baddelstationapp.startActivity;
import baddel.baddelstationapp.verifyMobileNumberActivity;

/**
 * Created by mahmo on 2017-07-05
 */

public class myWebClient extends WebViewClient{

    public Context sourceContext;
    public Class destContext;

    private Dialog loadingDialog;

    private String payFortTag = "payfort_";

    public myWebClient(Context sourceContext,Class destContext){
        this.sourceContext = sourceContext;
        this.destContext = destContext;
        loadingDialog = new customDialogs().loadingDialog(sourceContext);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        // TODO Auto-generated method stub
        super.onPageStarted(view, url, favicon);

    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        // Check to see if there is a progress dialog

        Log.d(payFortTag, "onLoadResource");
        //loadingDialog.show();

    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        Log.d(payFortTag, "onPageFinished");
        //String cookies = CookieManager.getInstance().getCookie(url);
        view.evaluateJavascript(javaScriptFunction(), new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String sessionResponse) {
                Log.d(payFortTag, "onPageFinishedInResponse");
                loadingDialog.cancel();
                if (sessionResponse != null){
                    if (sessionResponse.contains("success")){
                        Log.d(payFortTag, sessionResponse);

                        Intent successPayfortIntent = new Intent(sourceContext,destContext);
                        successPayfortIntent.putExtra("payfort",true);
                        sourceContext.startActivity(successPayfortIntent);

                        //Toast.makeText(sourceContext,"Error in Response",Toast.LENGTH_SHORT).show();

                    }else if (sessionResponse.contains("error")){
                        Log.d(payFortTag, sessionResponse);

                        Intent successPayfortIntent = new Intent(sourceContext,destContext);
                        successPayfortIntent.putExtra("payfort",false);
                        sourceContext.startActivity(successPayfortIntent);

                    } else{
                        Log.d(payFortTag, sessionResponse);
                        //sourceContext.startActivity(new Intent(sourceContext,destContext));
                        //Toast.makeText(sourceContext,"Error in Response",Toast.LENGTH_SHORT).show();
                    }
                } else{
                    Intent successPayfortIntent = new Intent(sourceContext,destContext);
                    successPayfortIntent.putExtra("payfort",false);
                    sourceContext.startActivity(successPayfortIntent);
                    //Toast.makeText(sourceContext,"No Response",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);

        Log.d(payFortTag, "payfortError"+ error.toString());
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);

        Log.d(payFortTag, "responseError"+ errorResponse);

    }

    @Override
    public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
        view.clearSslPreferences();
        view.clearFormData();
        view.clearCache(true);
        view.clearHistory();
        view.clearMatches();
        handler.proceed();
    }


    private String javaScriptFunction() {
        return "(function() { return sessionStorage.getItem('msg'); })();";
    }

}
