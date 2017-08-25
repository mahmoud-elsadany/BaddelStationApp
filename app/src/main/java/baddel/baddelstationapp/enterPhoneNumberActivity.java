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
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import baddel.baddelstationapp.ClientTCPSocketing.TCPClient;
import baddel.baddelstationapp.Models.trip_DS;
import baddel.baddelstationapp.connectToServer.myAsyncTask;
import baddel.baddelstationapp.connectToServer.responseDelegate;
import baddel.baddelstationapp.customViews.customDialogs;
import baddel.baddelstationapp.customViews.customViewGroup;
import baddel.baddelstationapp.internalStorage.Session;
import baddel.baddelstationapp.saveLogs.myLogs;

public class enterPhoneNumberActivity extends AppCompatActivity implements responseDelegate {

    //UI references
    private Button enterPhoneNumberNextBT,enterPhoneNumberCancelBT;
    private EditText enterPhoneNumberET;
    private CountryCodePicker enterPhoneNumberCCP;

    //asyncTask
    private myAsyncTask asyncTask;
    private String phoneNumberWithCode = "";

    //TcpSocket
//    private callController callController;

    //countDown object
    private CountDownTimer myCounter;
    private Runnable myRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();
        disableStatusBar();
        setContentView(R.layout.activity_enter_phone_number);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        myCounter = null;


//        if (!Session.getInstance().isTCPConnection())
//            customDialogs.ShowConnectionExceptionDialog(enterPhoneNumberActivity.this);

        setPhoneNumberValues();
        getBundle();
        setEnterPhoneNumberNextBT();
        setEnterPhoneNumberCancelBT();

        returnToStartActivity();

        startService();


    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void startService(){
        //callController = new callController(enterPhoneNumberActivity.this);
    }

    private void getBundle(){
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            enterPhoneNumberET.setText(bundle.getString("ModifyPhoneNumber"));
        }
    }

    private void returnToStartActivity() {
        myCounter = new CountDownTimer(Session.getInstance().getWaitingTime(), 1000) {

            public void onTick(long millisUntilFinished) {
                //TODO: Do something every second
            }

            public void onFinish() {
                Dialog timeoutWarningDialog = customDialogs.ShowTimeoutWarningDialog(myCounter,enterPhoneNumberActivity.this,enterPhoneNumberActivity.class);
                timeoutWarningDialog.show();

                final Handler mHandler = new Handler();
                myRunnable = new Runnable(){
                    @Override
                    public void run() {
                        startActivity(new Intent(enterPhoneNumberActivity.this,startActivity.class));
                    }
                };

                timeoutWarningDialog.setOnDismissListener(new Dialog.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        myCounter.cancel();
                        mHandler.removeCallbacks(myRunnable);
                        startActivity(new Intent(enterPhoneNumberActivity.this,enterPhoneNumberActivity.class));

                    }
                });


                mHandler.postDelayed(myRunnable, 7000);
            }

        }.start();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if(myCounter != null) {
                myCounter.cancel();
                myCounter = null;
                returnToStartActivity();
            }
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
        int numberOfBikes = Session.getInstance().getNumberOfChosenBikes();

        JSONObject JsonObject = new JSONObject();
        try {
            JsonObject.put("StationDeviceIMEI", android_id);
            JsonObject.put("CostCalculationMethod", "prepaid");
            JsonObject.put("PhoneNumber", PhoneNumber);
            JsonObject.put("BikeDeviceIMEI", "");
            JsonObject.put("NumberOfBikes", numberOfBikes);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        myLogs.logMyLog("requestTrip",JsonObject.toString());
        //Log.d("requestTrip",JsonObject.toString());


        int myProcessNum = 3;

        HashMap<String, String> data = new HashMap<>();
        data.put("requestTrip", JsonObject.toString());


        String URL = myURL + apiMethod;

        myLogs.logMyLog("urll",URL);
        //Log.d("urll",URL);
        myLogs.logMyLog("requestTripObject",JsonObject.toString());
        //Log.d("requestTripObject",JsonObject.toString());

        if (isNetworkConnected()) {
            asyncTask = new myAsyncTask(enterPhoneNumberActivity.this, data, URL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 1);

            asyncTask.delegate = this;

            //asyncTask.execute();
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            //showToast("done post PhoneNumber");
        } else {
            showToast("No Internet Connection");
        }
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
    public void getServerResponse(String response, int ProcessNum) {
        if (ProcessNum == 3){
            myLogs.logMyLog("requestTripResponse",response);
            //Log.d("requestTripResponse",response);

            if (response.contains("phone_number_missed")){
                showToast("phone Number is Missed");
            }else if (response.contains("no_available_bikes, error_while_send_sms")){
                showToast("Error while send SMS \n Because there is no available bikes");
            }else{
                trip_DS trip_ds = new trip_DS(response);

                ArrayList<trip_DS> trips = trip_ds.currentTripObjects;

                if (trips.size() > 0){
                    Session.getInstance().setCurrentTripArrayListObject(trips);

                    Intent goToConfirmSMS = new Intent(enterPhoneNumberActivity.this,verifyMobileNumberActivity.class);
                    goToConfirmSMS.putExtra("phoneNumber",phoneNumberWithCode);
                    startActivity(goToConfirmSMS);
                }else{
                    showToast("Error while send SMS Please try again");
                }


            }

        }

    }

    @Override
    public void onBackPressed() {

    }

    //check the connection
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
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

    @Override
    protected void onPause() {
        super.onPause();

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
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
