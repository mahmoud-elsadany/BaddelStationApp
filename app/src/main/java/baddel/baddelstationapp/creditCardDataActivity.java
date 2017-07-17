package baddel.baddelstationapp;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import baddel.baddelstationapp.Controller.callController;
import baddel.baddelstationapp.Models.orderDetailsObject;
import baddel.baddelstationapp.connectToServer.myAsyncTask;
import baddel.baddelstationapp.connectToServer.responseDelegate;
import baddel.baddelstationapp.internalStorage.Session;
import baddel.baddelstationapp.paymentClasses.SHA256Hashing;
import baddel.baddelstationapp.paymentClasses.myWebClient;

public class creditCardDataActivity extends AppCompatActivity implements responseDelegate {
    //UI references
    private Button creditCardDataCancelBT,creditCardDataNextBT;
    private EditText creditCardNumberET,creditCardValidYearET,creditCardValidMonthET,creditCardHolderNameET,creditCardCVVET;
    private TextView creditCardPeriodPriceTV;

    //HTTP asyncTask
    private myAsyncTask asyncTask;
    private HashMap<String, String> payFortData;
    private WebView webView;
    private int NoOfMin;

    //TcpSocket
    private callController callController;

    //countDown object
    private CountDownTimer myCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_card_data);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        if (!Session.getInstance().isTCPConnection())
//            customDialogs.ShowConnectionExceptionDialog(creditCardDataActivity.this);

        setEditTexts();
        setCreditCardDataNextBT();
        setCreditCardDataCancelBT();

        returnToStartActivity();


        NoOfMin = Session.getInstance().getChosenPeriodTime();

        startService();


    }

    private void returnToStartActivity(){
        myCounter = new CountDownTimer(90000, 1000) {

            public void onTick(long millisUntilFinished) {
                //TODO: Do something every second
            }
            public void onFinish() {
                startActivity(new Intent(creditCardDataActivity.this,startActivity.class));
            }
        }.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            myCounter.cancel();
            myCounter = null;
            returnToStartActivity();
        }
        return super.onTouchEvent(event);
    }


    private void startService(){
        callController = new callController(creditCardDataActivity.this);
    }

    private void setEditTexts(){
        creditCardPeriodPriceTV = (TextView)findViewById(R.id.creditCardPeriodPriceTV);
        String periodPriceSTR = Session.getInstance().getChosenPeriodTime()+" * minPrice";
        creditCardPeriodPriceTV.setText(periodPriceSTR);
        creditCardNumberET = (EditText)findViewById(R.id.creditCardNumberET1);
        creditCardValidYearET = (EditText)findViewById(R.id.creditCardValidYearET1);
        creditCardValidMonthET = (EditText)findViewById(R.id.creditCardValidMonthET1);
        creditCardHolderNameET = (EditText)findViewById(R.id.creditCardHolderNameET1);
        creditCardCVVET = (EditText)findViewById(R.id.creditCardCVVET1);
        webView = (WebView) findViewById(R.id.webView2);

    }

    private void setCreditCardDataCancelBT(){
        creditCardDataCancelBT = (Button)findViewById(R.id.creditCardDataCancelBT);
        creditCardDataCancelBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(creditCardDataActivity.this,startActivity.class));
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
                    setPayFortHashMap(cardHolderName, cardNumber, cardCVV,cardValidYear + cardValidMonth);

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
        payFortData.put("return_url", "http://104.197.104.190:8080/api/payment/request-token");
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

    @Override
    public void getServerResponse(String response, int ProcessNum) {
        switch (ProcessNum) {
            case 1:
                //order response
                Log.d("orderDetailsResponse", response);

                orderDetailsObject orderDetailsObject = new orderDetailsObject(response);

                Session.getInstance().setOrderId(orderDetailsObject.orderDetailsId);

                payFortData.put("merchant_reference", String.valueOf(orderDetailsObject.orderDetailsId));

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
                webView.loadUrl(payfortUrl);

                break;
            default:
                break;
        }
    }

    //check the connection
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {

            startActivity(new Intent(this,startActivity.class));

            webView.goBack();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        myCounter.cancel();
        myCounter = null;
        callController.unBindController();
        super.onDestroy();
    }
    @Override
    protected void onStop() {
        myCounter.cancel();
        myCounter = null;
        callController.unBindController();
        callController = null;
        super.onStop();
    }
}
