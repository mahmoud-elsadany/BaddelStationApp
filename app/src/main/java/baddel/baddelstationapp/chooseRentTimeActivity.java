package baddel.baddelstationapp;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

//import com.github.channguyen.rsv.RangeSliderView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import baddel.baddelstationapp.Controller.callController;
import baddel.baddelstationapp.customViews.CircularSeekBar;
import baddel.baddelstationapp.customViews.customDialogs;
import baddel.baddelstationapp.internalStorage.Session;


public class chooseRentTimeActivity extends AppCompatActivity {
    //UI references
    private TextView countingTimeTV, numberOfBikesTV, sliderCounterTV, rentingDetailsTV;
    //private RangeSliderView largeSlider;
    private CircularSeekBar circularSeekBar;
    private Button chooseRentTimeCancelBT, chooseRentTimeHelpBT, chooseRentTimeNextBT;
    private NumberPicker numberPicker;

    //recyclerView variables
    private ArrayList<String> rentCardsPeriodTimes = new ArrayList<>();

    //TcpSocket
    private callController callController;

    //countDown object
    private CountDownTimer myCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_rent_time);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        returnToStartActivity();

        //setChooseRentTimeSeekBar();
        setNumberPicker();
        setChooseRentTimeNextBT();
        setChooseRentTimeCancelBT();
        setChooseRentTimeHelpBT();

        setCircularSeekBar();
        //setPeriodSeekBar();

        startService();


        Log.d("costing","cost of 30 min: " + costEquation(30));

    }

    private void returnToStartActivity() {
        myCounter = new CountDownTimer(Session.getInstance().getWaitingTime(), 1000) {

            public void onTick(long millisUntilFinished) {
                //TODO: Do something every second
            }

            public void onFinish() {
                startActivity(new Intent(chooseRentTimeActivity.this, startActivity.class));
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


    private void startService() {
        callController = new callController(chooseRentTimeActivity.this);
//        if (!Session.getInstance().isTCPConnection())
//            customDialogs.ShowConnectionExceptionDialog(chooseRentTimeActivity.this);
    }

    private void setCircularSeekBar() {
        countingTimeTV = (TextView) findViewById(R.id.countingTimeTV);
        sliderCounterTV = (TextView) findViewById(R.id.sliderCounterTV);
        rentingDetailsTV = (TextView) findViewById(R.id.rentingDetailsTV);

        circularSeekBar = (CircularSeekBar) findViewById(R.id.circularSeekBar);
        Session.getInstance().setChosenPeriodTime(30);

        circularSeekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                countingTimeTV.setText("Reserved Minutes : " + String.valueOf(progress) + " Min for each bike");
                sliderCounterTV.setText(progress + " min");
                rentingDetailsTV.setText("reserve " + numberPicker.getValue() + " bikes for " + progress + " min costs " + 23+" egp");
                Session.getInstance().setChosenPeriodTime(progress);
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {

            }
        });
    }

    private void setNumberPicker() {
        numberOfBikesTV = (TextView) findViewById(R.id.numberOfBikesTV);
        numberPicker = (NumberPicker) findViewById(R.id.numberOfBikesNP);

        numberPicker.setMinValue(1);
        if (Session.getInstance().getNumberOfAvailableBikes() >= 3)
            numberPicker.setMaxValue(3);
        else
            numberPicker.setMaxValue(Session.getInstance().getNumberOfAvailableBikes());


        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                //Display the newly selected number from picker
                numberOfBikesTV.setText("Number Of Bikes : " + newVal);
                Session.getInstance().setNumberOfChosenBikes(newVal);
            }
        });

    }

    private void setChooseRentTimeNextBT() {
        chooseRentTimeNextBT = (Button) findViewById(R.id.chooseRentTimeNextBT);
        chooseRentTimeNextBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    private void setChooseRentTimeHelpBT() {
        chooseRentTimeHelpBT = (Button) findViewById(R.id.chooseRentTimeHelpBT);
        chooseRentTimeHelpBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(chooseRentTimeActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        myCounter.cancel();
        myCounter = null;
        callController.unBindController();
        callController = null;
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

    private double costEquation(int min) {
        double x = 0.4001504 + (0.8129855 - 0.4001504);
        double power = min / 65.64831;
        double myPower = Math.pow(power, 2.605385);

        double total = x / (1 + myPower);

        return total;
    }
}



   /*
    private void setChooseRentTimeRecyclerView(){
        chooseRentTimeRecyclerView = (RecyclerView)findViewById(R.id.chooseRentTimeRecyclerView);
        for (int i = 30;i<=600;i+=30){
            rentCardsPeriodTimes.add(i+" min");
        }
        rentCardsRCAdapter = new rentCardsRCAdapter(rentCardsPeriodTimes,chooseRentTimeActivity.this);


        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        chooseRentTimeRecyclerView.setLayoutManager(horizontalLayoutManagaer);

        chooseRentTimeRecyclerView.setAdapter(rentCardsRCAdapter);
    }*/
