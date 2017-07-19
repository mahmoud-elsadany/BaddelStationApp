package baddel.baddelstationapp;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import baddel.baddelstationapp.Controller.callController;
import baddel.baddelstationapp.Models.trip_DS;
import baddel.baddelstationapp.connectToServer.myAsyncTask;
import baddel.baddelstationapp.connectToServer.responseDelegate;
import baddel.baddelstationapp.customViews.customDialogs;
import baddel.baddelstationapp.internalStorage.Session;

public class verifyMobileNumberActivity extends AppCompatActivity implements responseDelegate {

    //UI references
    private TextView verMobileNumberTittleTV,verMobileNumberMessageTV;
    private EditText verMobileNumberET;
    private Button verMobileNumberNextBT,verMobileNumberCancelBT,verMobileNumberResendBT;

    //TCP service
    private callController callController;

    //Async objects
   private myAsyncTask myAsyncTask;

    //countDown object
    private CountDownTimer myCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_mobile_number);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        if (!Session.getInstance().isTCPConnection())
//            customDialogs.ShowConnectionExceptionDialog(verifyMobileNumberActivity.this);

        setTextViews();
        setVerMobileNumberResendBT();
        setVerMobileNumberCancelBT();
        setVerMobileNumberNextBT();

        returnToStartActivity();

        startService();
    }

    private void startService(){
        callController = new callController(verifyMobileNumberActivity.this);
    }

    private void returnToStartActivity(){
        myCounter = new CountDownTimer(Session.getInstance().getWaitingTime(), 1000) {

            public void onTick(long millisUntilFinished) {
                //TODO: Do something every second
            }
            public void onFinish() {
                startActivity(new Intent(verifyMobileNumberActivity.this,startActivity.class));
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

    private void setTextViews(){
        verMobileNumberTittleTV = (TextView)findViewById(R.id.verMobileNumberTittleTV);
        verMobileNumberMessageTV = (TextView)findViewById(R.id.verMobileNumberMessageTV);
        verMobileNumberET = (EditText)findViewById(R.id.verMobileNumberET);

    }

    private void setVerMobileNumberResendBT(){
        verMobileNumberResendBT = (Button)findViewById(R.id.verMobileNumberResendBT);
        verMobileNumberResendBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });
    }

    private void setVerMobileNumberNextBT(){
        verMobileNumberNextBT = (Button)findViewById(R.id.verMobileNumberNextBT);
        verMobileNumberNextBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String realToken = Session.getInstance().getMessageToken();
                if (verMobileNumberET.getText().toString().equals("")){
                    showToast("please put your verify Code !!");
                }else{
                    //check verify code

                    confirmUserRequest(Session.getInstance().getCurrentTripArrayListObjects().get(0),verMobileNumberET.getText().toString());
//                    showToast("Verify code is Accepted");
//                    startActivity(new Intent(verifyMobileNumberActivity.this,creditCardDataActivity.class));

                }
            }
        });
    }

    private void setVerMobileNumberCancelBT(){
        verMobileNumberCancelBT = (Button)findViewById(R.id.verMobileNumberCancelBT);
        verMobileNumberCancelBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(verifyMobileNumberActivity.this,enterPhoneNumberActivity.class));
            }
        });
    }

    private void showToast(String message){
        Toast.makeText(verifyMobileNumberActivity.this,message,Toast.LENGTH_LONG).show();
    }

    private void confirmUserRequest(trip_DS currentTrip,String userToken) {
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getAPIMETHODPutConfirmUser();
        int myProcessNum = 1;


        JSONObject confirmUserObject = new JSONObject();
        try {
            confirmUserObject.put("Token", userToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HashMap<String, String> data = new HashMap<>();
        data.put("id",String.valueOf(currentTrip.tripId));
        data.put("confirmUser", confirmUserObject.toString());

        Log.d("confirmTag","id: "+String.valueOf(currentTrip.tripId)+"\nconfirmUser: "+confirmUserObject.toString());

        String URL = myURL + apiMethod;

        if (isNetworkConnected()) {

            myAsyncTask = new myAsyncTask(verifyMobileNumberActivity.this, data, URL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 3);

            myAsyncTask.delegate = this;

//asyncTask.execute();
            myAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            showToast("No Internet Connection");
        }
    }

    //check the connection
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    protected void onDestroy() {
        if (myCounter != null){
            myCounter.cancel();
            myCounter = null;
            callController.unBindController();
            callController = null;
        }
        super.onDestroy();
    }

    @Override
    public void getServerResponse(String response, int ProcessNum) {
        if (ProcessNum == 1){

            Log.d("confirmSMSResponse",response);

            trip_DS trip_ds = new trip_DS(response);
            ArrayList<trip_DS> trips = trip_ds.currentTripObjects;

            if (trips.size() > 0){
                Session.getInstance().setCurrentTripArrayListObject(trips);
                showToast("Verify code is Accepted");
                startActivity(new Intent(verifyMobileNumberActivity.this,creditCardDataActivity.class));
            }else{
                showToast("There Is Something Wrong");
            }

        }
    }

    @Override
    protected void onStop() {
        if (myCounter != null){
            myCounter.cancel();
            myCounter = null;
            callController.unBindController();
            callController = null;
        }
        super.onStop();
    }
}
