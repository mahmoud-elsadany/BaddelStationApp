package baddel.baddelstationapp;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import baddel.baddelstationapp.ClientTCPSocketing.TCPClient;
import baddel.baddelstationapp.Controller.Controller;
import baddel.baddelstationapp.Controller.callController;
import baddel.baddelstationapp.Models.orderDetailsObject;
import baddel.baddelstationapp.Models.trip_DS;
import baddel.baddelstationapp.connectToServer.myAsyncTask;
import baddel.baddelstationapp.connectToServer.responseDelegate;
import baddel.baddelstationapp.customViews.customDialogs;
import baddel.baddelstationapp.customViews.customViewGroup;
import baddel.baddelstationapp.internalStorage.Session;
import baddel.baddelstationapp.paymentClasses.SHA256Hashing;
import baddel.baddelstationapp.paymentClasses.myWebClient;

public class creditCardDataActivity extends AppCompatActivity implements responseDelegate {
    //UI references
    private Button creditCardDataCancelBT,creditCardDataNextBT;
    private EditText creditCardNumberET,creditCardValidYearET,creditCardValidMonthET,creditCardHolderNameET,creditCardCVVET;
    //private TextView creditCardPeriodPriceTV;

    //HTTP asyncTask
    private myAsyncTask asyncTask;
    private HashMap<String, String> payFortData;
    private WebView webView;
    private int NoOfMin;

    //TcpSocket
    private callController callController;

    //countDown object
    private CountDownTimer myCounter;
    private Runnable myRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();
        disableStatusBar();
        setContentView(R.layout.activity_credit_card_data);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myCounter = null;

//        if (!Session.getInstance().isTCPConnection())
//            customDialogs.ShowConnectionExceptionDialog(creditCardDataActivity.this);

        setEditTexts();
        setCreditCardDataNextBT();
        setCreditCardDataCancelBT();

        returnToStartActivity();


        NoOfMin = Session.getInstance().getChosenPeriodTime();


        getBundle();


    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void getBundle(){
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            if (bundle.getBoolean("payfort")){
                ArrayList<trip_DS> trips_Array = Session.getInstance().getCurrentTripArrayListObjects();
                putSetOrder(trips_Array.get(0));
            }else {
                Dialog errorDialog = customDialogs.ShowReservedBikes(creditCardDataActivity.this,creditCardDataActivity.class,"Sorry you have a problem with your Card !!");
                errorDialog.show();
            }
        }
    }

    private void returnToStartActivity() {
        myCounter = new CountDownTimer(200000, 1000) {

            public void onTick(long millisUntilFinished) {
                //TODO: Do something every second
            }

            public void onFinish() {
                final Dialog timeoutWarningDialog = customDialogs.ShowTimeoutWarningDialog(myCounter,creditCardDataActivity.this,creditCardDataActivity.class);
                timeoutWarningDialog.show();

                final Handler mHandler = new Handler();
                myRunnable = new Runnable(){
                    @Override
                    public void run() {
                        cancelReservationTrip(Session.getInstance().getCurrentTripArrayListObjects().get(0));
//                        Session.getInstance().setCancelTrip(true);
//                        startActivity(new Intent(creditCardDataActivity.this,startActivity.class));
                    }
                };

                timeoutWarningDialog.setOnDismissListener(new Dialog.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        myCounter.cancel();
                        mHandler.removeCallbacks(myRunnable);
                        startActivity(new Intent(creditCardDataActivity.this,creditCardDataActivity.class));
                    }
                });


                mHandler.postDelayed(myRunnable, 7000);
            }

        }.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (myCounter != null) {
                myCounter.cancel();
                myCounter = null;
                returnToStartActivity();
            }
        }
        return super.onTouchEvent(event);
    }

    private void setEditTexts(){
        //creditCardPeriodPriceTV = (TextView)findViewById(R.id.creditCardPeriodPriceTV);
        //String periodPriceSTR = Session.getInstance().getChosenPeriodTime()+" * minPrice";
        //creditCardPeriodPriceTV.setText(periodPriceSTR);
        creditCardNumberET = (EditText)findViewById(R.id.creditCardNumberET1);
        creditCardValidYearET = (EditText)findViewById(R.id.creditCardValidYearET1);
        creditCardValidMonthET = (EditText)findViewById(R.id.creditCardValidMonthET1);
        creditCardHolderNameET = (EditText)findViewById(R.id.creditCardHolderNameET1);
        creditCardCVVET = (EditText)findViewById(R.id.creditCardCVVET1);
        creditCardNumberET.requestFocus();
        webView = (WebView) findViewById(R.id.webView2);

    }

    private void setCreditCardDataCancelBT(){
        creditCardDataCancelBT = (Button)findViewById(R.id.creditCardDataCancelBT);
        creditCardDataCancelBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelReservationTrip(Session.getInstance().getCurrentTripArrayListObjects().get(0));
            }
        });
    }

    private void setCreditCardDataNextBT(){
        creditCardDataNextBT = (Button)findViewById(R.id.creditCardDataNextBT);
        creditCardDataNextBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cardNumber = creditCardNumberET.getText().toString();
                String cardValidYear = creditCardValidYearET.getText().toString();
                String cardValidMonth = creditCardValidMonthET.getText().toString();
                String cardHolderName = creditCardHolderNameET.getText().toString();
                String cardCVV = creditCardCVVET.getText().toString();


                if (cardCVV.equals("")||cardHolderName.equals("")||cardNumber.equals("")||cardValidYear.equals("")||cardValidMonth.equals(""))
                    showToast("please fill all values");
                else{
                    String editedCardHolderName = cardHolderName.replace(" ","+");

                    int cardValidMonthInt = Integer.parseInt(cardValidMonth);
                    if (cardValidMonthInt <= 9){
                        cardValidMonth = "0"+cardValidMonthInt;
                    }else{
                        cardValidMonth = String.valueOf(cardValidMonthInt);
                    }

                    setPayFortHashMap(editedCardHolderName, cardNumber, cardCVV,cardValidYear + cardValidMonth);

                    postNewOrder(NoOfMin);
                }

            }
        });
    }

    private void showToast(String message){
        Toast.makeText(creditCardDataActivity.this,message,Toast.LENGTH_LONG).show();
    }

    private void setPayFortHashMap(String card_holder_name_ET_STR, String card_number_ET_STR, String card_security_code_ET_STR, String expiry_date_ET_STR) {
        payFortData = new HashMap<>();
        payFortData.put("access_code", "DRKhxD1IUMT2uJiyWdnc");
        payFortData.put("card_holder_name", card_holder_name_ET_STR);
        payFortData.put("card_number", card_number_ET_STR);
        payFortData.put("card_security_code", card_security_code_ET_STR);
        payFortData.put("expiry_date", expiry_date_ET_STR);
        payFortData.put("language", "en");
        payFortData.put("merchant_identifier", "QXCCpHjg");
        payFortData.put("remember_me", "NO");
        payFortData.put("return_url", Session.getInstance().getWebServicesBaseUrl()+"payment/request-token");
        payFortData.put("service_command", "TOKENIZATION");
    }
    private void postNewOrder(int NoOfMin) {
        //POST Request for minutes
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getAPIMETHODPostPaymentOrder();

        JSONObject NoOfMinutesJsonObject = new JSONObject();
        try {
            NoOfMinutesJsonObject.put("NoOfMinutes", NoOfMin);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int myProcessNum = 1;

        HashMap<String, String> data = new HashMap<>();
        data.put("createOrder", NoOfMinutesJsonObject.toString());

        String URL = myURL + apiMethod;

        if (isNetworkConnected()) {

            asyncTask = new myAsyncTask(creditCardDataActivity.this, data, URL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 1);

            asyncTask.delegate = this;

            //asyncTask.execute();
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else {
            showToast("No Internet Connection");
        }
    }
    private void postDataToPayFort() {
        //POST Request parameters to payfort
        String payFortURL = Session.getInstance().getPayFortUrl();
        int myProcessNum = 2;

        HashMap<String, String> data = payFortData;
        data.put("signature", getSignature());


        if (isNetworkConnected()) {

            asyncTask = new myAsyncTask(creditCardDataActivity.this, data, payFortURL, myProcessNum, null, null, null, 1);

            asyncTask.delegate = this;

            //asyncTask.execute();
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else {
            showToast("No Internet Connection");
        }
    }
    private String getSignature() {
        String requestPhrase = "BADDELREQPHPAYMENT";
        ArrayList<String> keys = new ArrayList<>();

        for (Map.Entry<String, String> entry : payFortData.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (!key.equals("card_number") && !key.equals("expiry_date") && !key.equals("card_security_code") && !key.equals("card_holder_name") && !key.equals("remember_me"))
                keys.add(key + "=" + value);

        }

        Collections.sort(keys);

        String keysArrayListStringOneLine = "";
        for (String s : keys) {
            keysArrayListStringOneLine += s;
        }


        String signaturePhrase = requestPhrase + keysArrayListStringOneLine + requestPhrase;

        Log.d("paymentLog", "ArrayListWithoutInputs: " + signaturePhrase);

        try {
            signaturePhrase = SHA256Hashing.hash256(signaturePhrase);
        } catch (NoSuchAlgorithmException e) {
            Log.d("paymentLog", e.toString());
        }

        Log.d("paymentLog", "signatureAfterHashing: " + signaturePhrase);


        return signaturePhrase;
    }
    private void getPayfortUrl(String Url){
        //GET Request payfort url
        int myProcessNum = 3;

        if (isNetworkConnected()) {

            asyncTask = new myAsyncTask(creditCardDataActivity.this, null, Url, myProcessNum, null, null, null, 2);

            asyncTask.delegate = this;

            //asyncTask.execute();
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else {
            showToast("No Internet Connection");
        }

    }
    private void putSetOrder(trip_DS myTripDS) {
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getAPIMETHODPutSetOrder();
        int myProcessNum = 4;

        JSONObject setOrderObject = new JSONObject();
        try {
            setOrderObject.put("OrderId", Session.getInstance().getOrderId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("jsonOrder",setOrderObject.toString());

        HashMap<String, String> data = new HashMap<>();
        data.put("id",String.valueOf(myTripDS.tripId));
        data.put("tripPayment", setOrderObject.toString());

        Log.d("setOrderParams","id: "+String.valueOf(myTripDS.tripId)+"\ntripPayment: "+setOrderObject.toString());


        String URL = myURL + apiMethod;

        if (isNetworkConnected()) {

            asyncTask = new myAsyncTask(creditCardDataActivity.this, data, URL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 3);

            asyncTask.delegate = this;

            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else {
            showToast("No Internet Connection");
        }
    }
    private void cancelReservationTrip(trip_DS currentTrip){

        Session.getInstance().setCancelTrip(true);
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getAPIMETHODPostCancelTrip();
        int myProcessNum = 5;

        HashMap<String, String> data = new HashMap<>();
        data.put("id",String.valueOf(currentTrip.tripId));

        Log.d("cancelTrip","id: "+String.valueOf(currentTrip.tripId));

        String URL = myURL + apiMethod;

        if (isNetworkConnected()) {

            asyncTask = new myAsyncTask(creditCardDataActivity.this, data, URL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 3);

            asyncTask.delegate = this;

            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            showToast("No Internet Connection");
        }
    }


    @Override
    public void getServerResponse(String response, int ProcessNum) {
        switch (ProcessNum) {
            case 1:
                //order response
                Log.d("orderDetailsResponse", response);

                orderDetailsObject orderDetailsObject = new orderDetailsObject(response);

                Session.getInstance().setOrderId(orderDetailsObject.orderDetailsId);

                payFortData.put("merchant_reference", String.valueOf(orderDetailsObject.orderDetailsId));

                Log.d("creditCardResponse",String.valueOf(orderDetailsObject.orderDetailsId));

                postDataToPayFort();

                break;
            case 2:
                //payfort_response
                Log.d("payfort_response", response);

                getPayfortUrl(response);

                break;
            case 3:
                //payfort bank url
                Log.d("payfort_website_url", response);

                String payfortUrl = response.substring(1, response.length() - 1);


                webView.setWebViewClient(new myWebClient(creditCardDataActivity.this,creditCardDataActivity.class));
                webView.getSettings().setJavaScriptEnabled(true);
                webView.setVisibility(View.VISIBLE);
                webView.getSettings().setBuiltInZoomControls(true);
                webView.getSettings().setTextZoom(20);
                webView.getSettings().setSupportZoom(true);
                webView.getSettings().setLoadWithOverviewMode(true);
                webView.loadUrl(payfortUrl);

                break;
            case 4:
                //setOrder
                Log.d("confirmSetOrder",response);
                final trip_DS confirmTrip = new trip_DS(response);

                ArrayList<trip_DS> currentTrips = confirmTrip.currentTripObjects;

                Session.getInstance().setCurrentTripArrayListObject(currentTrips);


                if (currentTrips.size() > 0){
                    for (final trip_DS tripObject:currentTrips){
                        Controller controller = new Controller();
                        if(tripObject.startSlotNumber <= 9)
                            controller.sendToTCP("0"+String.valueOf(tripObject.startSlotNumber),confirmTrip.currentTripObjects.get(0));
                        else
                            controller.sendToTCP(String.valueOf(tripObject.startSlotNumber),confirmTrip.currentTripObjects.get(0));
                    }
//                    callController.unBindController();

                }else{
                    showToast("There Is Something Wrong");
                }

                showStartTripDialog();
                break;
            case 5:
                //cancel Trip
                Log.d("cancelTripResponse",response);

                showToast(response);
//                callController.unBindController();
                startActivity(new Intent(creditCardDataActivity.this,startActivity.class));

                break;
            default:
                break;
        }
    }

    private void showStartTripDialog(){
        if (myCounter != null){
            myCounter.cancel();
            myCounter = null;
//            callController.unBindController();
//            callController = null;
        }

        ArrayList<trip_DS> trips = Session.getInstance().getCurrentTripArrayListObjects();

        String reservedSlots = "";

        for (trip_DS tripObj : trips) {
            reservedSlots +=  tripObj.startSlotNumber + ", ";
        }


        final Dialog showReservedBikesDialog = customDialogs.ShowDialogAfterStartTrip(creditCardDataActivity.this, reservedSlots);
        showReservedBikesDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showReservedBikesDialog.cancel();
                startActivity(new Intent(creditCardDataActivity.this, startActivity.class));
            }
        }, 20000);
    }

    //check the connection
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {

            cancelReservationTrip(Session.getInstance().getCurrentTripArrayListObjects().get(0));

            //startActivity(new Intent(this,startActivity.class));

            webView.goBack();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (myCounter != null){
            myCounter.cancel();
            myCounter = null;
//            callController.unBindController();
//            callController = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    @Override
    protected void onStop() {
        if (myCounter != null){
            myCounter.cancel();
            myCounter = null;
//            callController.unBindController();
//            callController = null;
        }
        super.onStop();
    }

    private void disableStatusBar() {
        WindowManager manager = ((WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE));

        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        localLayoutParams.gravity = Gravity.TOP;
        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |

                // this is to enable the notification to recieve touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |

                // Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        localLayoutParams.height = (int) (50 * getResources()
                .getDisplayMetrics().scaledDensity);
        localLayoutParams.format = PixelFormat.TRANSPARENT;


        customViewGroup view = new customViewGroup(this);

        manager.addView(view, localLayoutParams);
    }
}
