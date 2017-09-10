package baddel.baddelstationapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//import com.github.channguyen.rsv.RangeSliderView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.RunnableFuture;

import baddel.baddelstationapp.ClientTCPSocketing.TCPClient;
import baddel.baddelstationapp.ClientTCPSocketing.TCPcheck;
import baddel.baddelstationapp.Models.trip_DS;
import baddel.baddelstationapp.customViews.CircularSeekBar;
import baddel.baddelstationapp.customViews.customDialogs;
import baddel.baddelstationapp.customViews.customViewGroup;
import baddel.baddelstationapp.internalStorage.Session;
import baddel.baddelstationapp.saveLogs.myLogs;


public class chooseRentTimeActivity extends AppCompatActivity {

    private static final String chooseRentTimeActivityTag = "chooseRentTimeActivity";

    //UI references
    private TextView tripCostTV, sliderCounterTV, numberOfBikesTV, priceOfEachBikeTV;
    private CircularSeekBar circularSeekBar;
    private Button chooseRentTimeCancelBT, chooseRentTimeNextBT, oneBikeBT, twoBikesBT, threeBikesBT;

    private int bikeNumbers = 1;

    private int actualProgress = 30;

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
        setContentView(R.layout.activity_choose_rent_time);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myCounter = null;

        returnToStartActivity();

        setBikeNumbers();
        setChooseRentTimeNextBT();
        setChooseRentTimeCancelBT();
        setCircularSeekBar();

        startService();

        getBundle();
    }

    private void getBundle() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getBoolean("EXITKIOSK")){
                startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
//                finish();
//                finishAffinity();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
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

    private void returnToStartActivity() {
        myCounter = new CountDownTimer(Session.getInstance().getWaitingTime(), 1000) {

            public void onTick(long millisUntilFinished) {
                //TODO: Do something every second
            }

            public void onFinish() {
                Dialog timeoutWarningDialog = customDialogs.ShowTimeoutWarningDialog(myCounter,chooseRentTimeActivity.this,chooseRentTimeActivity.class);
                timeoutWarningDialog.show();

                final Handler mHandler = new Handler();
                myRunnable = new Runnable(){
                    @Override
                    public void run() {
                        startActivity(new Intent(chooseRentTimeActivity.this,startActivity.class));
                    }
                };

                timeoutWarningDialog.setOnDismissListener(new Dialog.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        startActivity(new Intent(chooseRentTimeActivity.this,chooseRentTimeActivity.class));
                        myCounter.cancel();
                        mHandler.removeCallbacks(myRunnable);
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

    private void startService() {
        //callController = new callController(chooseRentTimeActivity.this);
//        if (!Session.getInstance().isTCPConnection())
//            customDialogs.ShowConnectionExceptionDialog(chooseRentTimeActivity.this);
    }

    private void setCircularSeekBar() {
        tripCostTV = (TextView) findViewById(R.id.tripCostTV);
        sliderCounterTV = (TextView) findViewById(R.id.sliderCounterTV);
        numberOfBikesTV = (TextView)findViewById(R.id.numberOfBikesTV);
        priceOfEachBikeTV = (TextView)findViewById(R.id.priceOfEachBike);

        //rentingDetailsTV = (TextView) findViewById(R.id.rentingDetailsTV);
        circularSeekBar = (CircularSeekBar) findViewById(R.id.circularSeekBar);
        Session.getInstance().setChosenPeriodTime(30);

        circularSeekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {

                if (progress == 0)
                    actualProgress = 30;
                else
                    actualProgress = progress*30;

                tripCostTV.setText(costEquation(actualProgress,bikeNumbers) + " EGP");
                priceOfEachBikeTV.setText(costEquation(actualProgress,1) + " EGP Per bike");

                sliderCounterTV.setText(String.valueOf(actualProgress));
                //rentingDetailsTV.setText("reserve " + numberPicker.getValue() + " bikes for " + actualProgress + " min costs " + costEquation(actualProgress)*actualProgress*numberPicker.getValue()+" egp");
//                Session.getInstance().setChosenPeriodTime(progress*bikeNumbers);
                Session.getInstance().setChosenPeriodTime(actualProgress);
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {
            }
        });
    }

    private void setBikeNumbers(){
        oneBikeBT = (Button)findViewById(R.id.oneBikeIMBT);
        twoBikesBT = (Button)findViewById(R.id.twoBikesIMBT);
        threeBikesBT = (Button)findViewById(R.id.threeBikesIMBT);

        Session.getInstance().setNumberOfChosenBikes(1);

        if (Session.getInstance().getNumberOfAvailableBikes() == 2) {
            threeBikesBT.setVisibility(View.INVISIBLE);

        }else if (Session.getInstance().getNumberOfAvailableBikes() == 1){
            threeBikesBT.setVisibility(View.INVISIBLE);
            twoBikesBT.setVisibility(View.INVISIBLE);
        }else if (Session.getInstance().getNumberOfAvailableBikes() == 0){
            oneBikeBT.setVisibility(View.INVISIBLE);
            threeBikesBT.setVisibility(View.INVISIBLE);
            twoBikesBT.setVisibility(View.INVISIBLE);
        }

        oneBikeBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bikeNumbers = 1;
                oneBikeBT.setBackgroundResource(R.drawable.selectedbutton);
                twoBikesBT.setBackgroundResource(R.drawable.button);
                threeBikesBT.setBackgroundResource(R.drawable.button);
                tripCostTV.setText(costEquation(actualProgress,bikeNumbers) + " EGP");
                sliderCounterTV.setText(String.valueOf(actualProgress));
                numberOfBikesTV.setText("Bike x1");
                Session.getInstance().setNumberOfChosenBikes(1);
            }
        });
        twoBikesBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bikeNumbers = 2;
                oneBikeBT.setBackgroundResource(R.drawable.button);
                twoBikesBT.setBackgroundResource(R.drawable.selectedbutton);
                threeBikesBT.setBackgroundResource(R.drawable.button);
                tripCostTV.setText(costEquation(actualProgress,bikeNumbers) + " EGP");
                sliderCounterTV.setText(String.valueOf(actualProgress));
                numberOfBikesTV.setText("Bikes x2");
                Session.getInstance().setNumberOfChosenBikes(2);
            }
        });
        threeBikesBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bikeNumbers = 3;
                oneBikeBT.setBackgroundResource(R.drawable.button);
                twoBikesBT.setBackgroundResource(R.drawable.button);
                threeBikesBT.setBackgroundResource(R.drawable.selectedbutton);
                tripCostTV.setText(costEquation(actualProgress,bikeNumbers) + " EGP");
                sliderCounterTV.setText(String.valueOf(actualProgress));
                numberOfBikesTV.setText("Bikes x3");
                Session.getInstance().setNumberOfChosenBikes(3);
            }
        });

        myLogs.logMyLog("numberBikesTag",String.valueOf(Session.getInstance().getNumberOfChosenBikes()));
        //Log.d("numberBikesTag",String.valueOf(Session.getInstance().getNumberOfChosenBikes()));

    }

    private void setChooseRentTimeNextBT() {
        chooseRentTimeNextBT = (Button) findViewById(R.id.chooseRentTimeNextBT);
        chooseRentTimeNextBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Session.getInstance().setChosenPeriodTime(actualProgress);
                //Session.getInstance().setChosenPeriodTime(30*bikeNumbers);
                if (Session.getInstance().getChosenPeriodTime() > 0) {
                    //go to enter phone Number
                    startActivity(new Intent(chooseRentTimeActivity.this, enterPhoneNumberActivity.class));
                } else
                    showToast("please Choose rent Period Time !!");
            }
        });
    }

    private void setChooseRentTimeCancelBT() {
        chooseRentTimeCancelBT = (Button) findViewById(R.id.chooseRentTimeCancelBT);
        chooseRentTimeCancelBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(chooseRentTimeActivity.this, startActivity.class));
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(chooseRentTimeActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        if (myCounter != null){
            myCounter.cancel();
            myCounter = null;

        }
//        if (callController != null){
//            callController.unBindController();
//            callController = null;
//        }
        super.onDestroy();
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
    public void onBackPressed() {

    }

//    private float costEquation(int min,int bikeNumbers) {
//        float x = (float) (1.235204 - 0.3861872);
//        float y = (float) (min / 28.85022);
//        float myPower = (float) Math.pow(y, 1.429056);
//
//        float division = x /(1+myPower);
//
//        float total = (float) (0.3861872 + division);
//
//
//        //float realTotal = total*min*bikeNumbers;
//        float realTotal = total*min;
//
//        int returnTotal = (int)realTotal;
//
//        int fiveModulas = returnTotal % 5;
//
//        int returnRealTotal = (5-fiveModulas) + returnTotal;
//
//
//        //return returnRealTotal;
//        return returnRealTotal*bikeNumbers;
//    }

    private int costEquation(int min,int bikeNumbers){
        double minPrice = 0;

        if(min>=30 && min<60){
            minPrice = 25/30.0;
        }else if(min>=60 && min<120){
            minPrice = 40/60.0;
        }else if (min>=120 && min<300){
            minPrice = 70/120.0;

        }else{
            minPrice = 140/300.0;
        }

        double totalPrice =  Math.ceil(minPrice * min);


        double reminder = 0;
        double modulus =totalPrice % 5;
        if(modulus>0){
            reminder = 5-modulus;
        }

        //var roundedTotal = totalPrice + (5-(totalPrice % 5));
        double roundedTotal = (int)totalPrice + reminder;

        return (int)roundedTotal*bikeNumbers;
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
