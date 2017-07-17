package baddel.baddelstationapp;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import baddel.baddelstationapp.Controller.callController;
import baddel.baddelstationapp.Models.trip_DS;
import baddel.baddelstationapp.connectToServer.myAsyncTask;
import baddel.baddelstationapp.connectToServer.responseDelegate;
import baddel.baddelstationapp.customViews.customDialogs;
import baddel.baddelstationapp.internalStorage.Session;

public class enterPhoneNumberActivity extends AppCompatActivity implements responseDelegate {

    //UI references
    private Button enterPhoneNumberNextBT,enterPhoneNumberCancelBT;
    private EditText enterPhoneNumberET;
    private CountryCodePicker enterPhoneNumberCCP;

    //asyncTask
    private myAsyncTask asyncTask;
    private String phoneNumberWithCode = "";

    //TcpSocket
    private callController callController;

    //countDown object
    private CountDownTimer myCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_phone_number);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        if (!Session.getInstance().isTCPConnection())
//            customDialogs.ShowConnectionExceptionDialog(enterPhoneNumberActivity.this);

        setPhoneNumberValues();
        getBundle();
        setEnterPhoneNumberNextBT();
        setEnterPhoneNumberCancelBT();

        returnToStartActivity();

        startService();
    }

    private void startService(){
        callController = new callController(enterPhoneNumberActivity.this);
    }

    private void getBundle(){
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            enterPhoneNumberET.setText(bundle.getString("ModifyPhoneNumber"));
        }
    }

    private void returnToStartActivity(){
        myCounter = new CountDownTimer(Session.getInstance().getWaitingTime(), 1000) {

            public void onTick(long millisUntilFinished) {
                //TODO: Do something every second
            }
            public void onFinish() {
                startActivity(new Intent(enterPhoneNumberActivity.this,startActivity.class));
            }
        }.start();

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            myCounter.cancel();
            myCounter = null;
            returnToStartActivity();
            showToast("touch");
        }
        return super.onTouchEvent(event);
    }

    private void setEnterPhoneNumberNextBT(){
        enterPhoneNumberNextBT = (Button)findViewById(R.id.enterPhoneNumberNextBT);
        enterPhoneNumberNextBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(enterPhoneNumberET.getText().toString().equals("")){
                    showToast("please put your phone number !!");
                }else{
                    phoneNumberWithCode = enterPhoneNumberCCP.getSelectedCountryCode()+enterPhoneNumberET.getText().toString();
                    postRequestTrip(phoneNumberWithCode);
                }
            }
        });
    }

    private void setPhoneNumberValues(){
        enterPhoneNumberET = (EditText)findViewById(R.id.enterPhoneNumberET);
        enterPhoneNumberCCP = (CountryCodePicker)findViewById(R.id.enterPhoneNumberCCP);
    }

    private void setEnterPhoneNumberCancelBT(){
        enterPhoneNumberCancelBT = (Button)findViewById(R.id.enterPhoneNumberCancelBT);
        enterPhoneNumberCancelBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(enterPhoneNumberActivity.this,chooseRentTimeActivity.class));
            }
        });
    }

    private void showToast(String message){
        Toast.makeText(enterPhoneNumberActivity.this,message,Toast.LENGTH_LONG).show();
    }

    private void postRequestTrip(String PhoneNumber){
        //POST Request for PhoneNumber
        String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getAPIMETHODPostRequestTrip();

        JSONObject JsonObject = new JSONObject();
        try {
            JsonObject.put("StationDeviceIMEI", android_id);
            JsonObject.put("CostCalculationMethod", "prepaid");
            JsonObject.put("PhoneNumber", PhoneNumber);
            JsonObject.put("BikeDeviceIMEI", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        int myProcessNum = 3;

        HashMap<String, String> data = new HashMap<>();
        data.put("requestTrip", JsonObject.toString());


        String URL = myURL + apiMethod;

        Log.d("urll",URL);
        Log.d("requestTripObject",JsonObject.toString());

        if (isNetworkConnected()) {
            asyncTask = new myAsyncTask(enterPhoneNumberActivity.this, data, URL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 1);

            asyncTask.delegate = this;

            //asyncTask.execute();
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            showToast("done post PhoneNumber");
        } else {
            showToast("No Internet Connection");
        }
    }


    @Override
    protected void onDestroy() {
        myCounter.cancel();
        myCounter = null;
        callController.unBindController();
        super.onDestroy();
    }

    @Override
    public void getServerResponse(String response, int ProcessNum) {
        if (ProcessNum == 3){
            Log.d("requestTripResponse",response);

            if (response.contains("phone_number_missed")){
                showToast("phone Number is Missed");
            }else if (response.contains("no_available_bikes, error_while_send_sms")){
                showToast("Error while send SMS \n Because there is no available bikes");
            }else{
                trip_DS trip_ds = new trip_DS(response);

                Session.getInstance().setCurrentTripObject(trip_ds);

                String token = trip_ds.slotSecurityToken;
                if (token != null){
                    Session.getInstance().setMessageToken(token);

                    Intent goToConfirmSMS = new Intent(enterPhoneNumberActivity.this,confirmSMSActivity.class);
                    goToConfirmSMS.putExtra("phoneNumber",phoneNumberWithCode);
                    startActivity(goToConfirmSMS);
                }else{
                    showToast("Error while send SMS Please try again");
                }


            }

        }

    }
    //check the connection
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
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
