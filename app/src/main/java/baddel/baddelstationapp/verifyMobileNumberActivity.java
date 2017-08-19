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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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

import baddel.baddelstationapp.ClientTCPSocketing.TCPClient;
import baddel.baddelstationapp.Controller.Controller;
import baddel.baddelstationapp.Controller.callController;
import baddel.baddelstationapp.Models.trip_DS;
import baddel.baddelstationapp.connectToServer.myAsyncTask;
import baddel.baddelstationapp.connectToServer.responseDelegate;
import baddel.baddelstationapp.customViews.customDialogs;
import baddel.baddelstationapp.customViews.customViewGroup;
import baddel.baddelstationapp.internalStorage.Session;

public class verifyMobileNumberActivity extends AppCompatActivity implements responseDelegate {

    //UI references
    private TextView verMobileNumberTittleTV, verMobileNumberMessageTV;
    private EditText verMobileNumberET;
    private Button verMobileNumberNextBT, verMobileNumberCancelBT, resendSMSBT, modifyPhoneNumberBT;

    private String phoneNumberSTR;

    private Boolean modifyPhoneNumber = false;

    //TCP service
    private callController callController;

    //Async objects
    private myAsyncTask myAsyncTask;

    //countDown object
    private CountDownTimer myCounter;
    private Runnable myRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_mobile_number);
        hideSystemUI();
        disableStatusBar();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myCounter = null;

//        if (!Session.getInstance().isTCPConnection())
//            customDialogs.ShowConnectionExceptionDialog(verifyMobileNumberActivity.this);
        getBundle();
        setTextViews();
        setResendSMSBT();
        setVerMobileNumberTittleTV();
        setVerMobileNumberCancelBT();
        setVerMobileNumberNextBT();
        setModifyPhoneNumberBT();

        returnToStartActivity();

        startService();

    }

    private void getBundle() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            phoneNumberSTR = bundle.getString("phoneNumber");

            if (bundle.getBoolean("cancelTripIntent")) {
                if (myCounter != null) {
                    myCounter.cancel();
                    myCounter = null;
                }
                cancelReservationTrip(Session.getInstance().getCurrentTripArrayListObjects().get(0));
            }
        }
    }

    private void setVerMobileNumberTittleTV() {
        verMobileNumberTittleTV = (TextView) findViewById(R.id.verMobileNumberTittleTV);
        verMobileNumberTittleTV.setText(phoneNumberSTR);
    }

    private void startService() {
        //callController = new callController(verifyMobileNumberActivity.this);
    }

    private void returnToStartActivity() {
        myCounter = new CountDownTimer(Session.getInstance().getWaitingTime(), 1000) {

            public void onTick(long millisUntilFinished) {
                //TODO: Do something every second
            }

            public void onFinish() {
                Dialog timeoutWarningDialog = customDialogs.ShowTimeoutWarningDialog(myCounter, verifyMobileNumberActivity.this, verifyMobileNumberActivity.class);
                timeoutWarningDialog.show();

                final Handler mHandler = new Handler();
                myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        cancelReservationTrip(Session.getInstance().getCurrentTripArrayListObjects().get(0));
                    }
                };

                timeoutWarningDialog.setOnDismissListener(new Dialog.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        myCounter.cancel();
                        mHandler.removeCallbacks(myRunnable);
                        startActivity(new Intent(verifyMobileNumberActivity.this, verifyMobileNumberActivity.class));
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

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void setTextViews() {
        verMobileNumberMessageTV = (TextView) findViewById(R.id.verMobileNumberMessageTV);
        verMobileNumberET = (EditText) findViewById(R.id.verMobileNumberET);
    }

    private void setResendSMSBT() {
        resendSMSBT = (Button) findViewById(R.id.resendSMSBT);
        resendSMSBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshTokenRequest(Session.getInstance().getCurrentTripArrayListObjects().get(0), phoneNumberSTR);
            }
        });
    }

    private void setModifyPhoneNumberBT() {
        modifyPhoneNumberBT = (Button) findViewById(R.id.modifyPhoneNumberBT);
        modifyPhoneNumberBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyPhoneNumber = true;
                cancelReservationTrip(Session.getInstance().getCurrentTripArrayListObjects().get(0));
            }
        });
    }

    private void setVerMobileNumberNextBT() {
        verMobileNumberNextBT = (Button) findViewById(R.id.verMobileNumberNextBT);
        verMobileNumberNextBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String realToken = Session.getInstance().getMessageToken();
                if (verMobileNumberET.getText().toString().equals("")) {
                    showToast("please put your verify Code !!");
                } else {
                    //check verify code

                    confirmUserRequest(Session.getInstance().getCurrentTripArrayListObjects().get(0), verMobileNumberET.getText().toString());
//                    showToast("Verify code is Accepted");
//                    startActivity(new Intent(verifyMobileNumberActivity.this,creditCardDataActivity.class));

                }
            }
        });
    }

    private void setVerMobileNumberCancelBT() {
        verMobileNumberCancelBT = (Button) findViewById(R.id.verMobileNumberCancelBT);
        verMobileNumberCancelBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelReservationTrip(Session.getInstance().getCurrentTripArrayListObjects().get(0));
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(verifyMobileNumberActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void confirmUserRequest(trip_DS currentTrip, String userToken) {
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
        data.put("id", String.valueOf(currentTrip.tripId));
        data.put("confirmUser", confirmUserObject.toString());

        Log.d("confirmTag", "id: " + String.valueOf(currentTrip.tripId) + "\nconfirmUser: " + confirmUserObject.toString());

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

    private void refreshTokenRequest(trip_DS currentTrip, String phoneNumber) {
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getAPIMETHODPutRefreshToken();
        int myProcessNum = 2;


        JSONObject refreshTokenObject = new JSONObject();
        try {
            refreshTokenObject.put("PhoneNumber", phoneNumber);
            refreshTokenObject.put("SendSms", "true");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HashMap<String, String> data = new HashMap<>();
        data.put("id", String.valueOf(currentTrip.tripId));
        data.put("refreshToken", refreshTokenObject.toString());

        Log.d("refreshTokenTag", "id: " + String.valueOf(currentTrip.tripId)
                + "\nrefreshToken: " + refreshTokenObject.toString());

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

    private void cancelReservationTrip(trip_DS currentTrip) {
        Session.getInstance().setCancelTrip(true);
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getAPIMETHODPostCancelTrip();
        int myProcessNum = 3;

        HashMap<String, String> data = new HashMap<>();
        data.put("id", String.valueOf(currentTrip.tripId));

        Log.d("cancelTrip", "id: " + String.valueOf(currentTrip.tripId));

        String URL = myURL + apiMethod;

        if (isNetworkConnected()) {

            myAsyncTask = new myAsyncTask(verifyMobileNumberActivity.this, data, URL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 3);

            myAsyncTask.delegate = this;

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
        if (myCounter != null) {
            myCounter.cancel();
            myCounter = null;
//            callController.unBindController();
//            callController = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void getServerResponse(String response, int ProcessNum) {
        if (ProcessNum == 1) {
            //confirm sms api
            Log.d("confirmSMSResponse", response);

            String reservedSlots = "";

            trip_DS trip_ds = new trip_DS(response);
            ArrayList<trip_DS> trips = trip_ds.currentTripObjects;

            if (trips.size() > 0) {
                Session.getInstance().setCurrentTripArrayListObject(trips);
                showToast("Verify code is Accepted");

                for (trip_DS tripObj : trips) {
                    reservedSlots += tripObj.startSlotNumber + ", ";
                }

                Dialog slotsDialog = customDialogs.ShowReservedBikes(verifyMobileNumberActivity.this, creditCardDataActivity.class, reservedSlots);

                slotsDialog.show();
                //startActivity(new Intent(verifyMobileNumberActivity.this,creditCardDataActivity.class));
            } else {
                showToast("There Is Something Wrong");
            }

        } else if (ProcessNum == 2) {
            //refresh token api
            Log.d("refreshTokenResponse", response);

            if (response.equals("403"))
                showToast("Check your SMS");
            else if (response.equals("400"))
                showToast("max trials exceded");

        } else if (ProcessNum == 3) {
            //cancel trip
            Log.d("cancelTripResponse", response);

            showToast(response);

            if (modifyPhoneNumber) {
                Intent goToEnterPhoneNumber = new Intent(verifyMobileNumberActivity.this, enterPhoneNumberActivity.class);
                goToEnterPhoneNumber.putExtra("ModifyPhoneNumber", verMobileNumberTittleTV.getText().toString());
                startActivity(goToEnterPhoneNumber);
            }else{
                startActivity(new Intent(verifyMobileNumberActivity.this, startActivity.class));
            }
        }
    }

    @Override
    protected void onStop() {
        if (myCounter != null) {
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
