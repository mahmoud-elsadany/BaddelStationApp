package baddel.baddelstationapp;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import baddel.baddelstationapp.Controller.callController;
import baddel.baddelstationapp.customViews.customDialogs;
import baddel.baddelstationapp.internalStorage.Session;

public class confirmSMSActivity extends AppCompatActivity {

    //UI references
    private Button confirmSMSnoSMSBT,confirmSMSchangeMobileNumberBT,confirmSMSNextBT,confirmSMSCancelBT;
    private TextView confirmSMSMobileNumberTV;


    //variables
    private String phoneNumberSTR = "";


    //TcpSocket
    private callController callController;

    //countDown object
    private CountDownTimer myCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_sms);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


//        if (!Session.getInstance().isTCPConnection())
//            customDialogs.ShowConnectionExceptionDialog(confirmSMSActivity.this);


        getBundle();
        setConfirmSMSMobileNumberTV();
        setConfirmSMSchangeMobileNumberBT();
        setConfirmSMSnoSMSBT();
        setConfirmSMSCancelBT();
        setConfirmSMSNextBT();

        returnToStartActivity();

        startService();


    }

    private void startService(){
        callController = new callController(confirmSMSActivity.this);
    }

    private void getBundle(){
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            phoneNumberSTR = bundle.getString("phoneNumber");
        }
    }

    private void returnToStartActivity(){
        myCounter = new CountDownTimer(80000, 1000) {

            public void onTick(long millisUntilFinished) {
                //TODO: Do something every second
            }
            public void onFinish() {
                startActivity(new Intent(confirmSMSActivity.this,startActivity.class));
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

    private void setConfirmSMSMobileNumberTV(){
        confirmSMSMobileNumberTV = (TextView)findViewById(R.id.confirmSMSMobileNumberTV);
        confirmSMSMobileNumberTV.setText(phoneNumberSTR);
    }

    private void setConfirmSMSchangeMobileNumberBT(){
        confirmSMSchangeMobileNumberBT = (Button)findViewById(R.id.confirmSMSchangeMobileNumberBT);
        confirmSMSchangeMobileNumberBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToEnterPhoneNumber = new Intent(confirmSMSActivity.this,enterPhoneNumberActivity.class);
                goToEnterPhoneNumber.putExtra("ModifyPhoneNumber",confirmSMSMobileNumberTV.getText().toString());
                startActivity(goToEnterPhoneNumber);
            }
        });
    }

    private void setConfirmSMSnoSMSBT(){
        confirmSMSnoSMSBT = (Button)findViewById(R.id.confirmSMSnoSMSBT);
        confirmSMSnoSMSBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });
    }

    private void setConfirmSMSCancelBT(){
        confirmSMSCancelBT = (Button)findViewById(R.id.confirmSMSCancelBT);
        confirmSMSCancelBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToEnterPhoneNumber = new Intent(confirmSMSActivity.this,enterPhoneNumberActivity.class);
                goToEnterPhoneNumber.putExtra("ModifyPhoneNumber",confirmSMSMobileNumberTV.getText().toString());
                startActivity(goToEnterPhoneNumber);

            }
        });
    }

    private void setConfirmSMSNextBT(){
        confirmSMSNextBT = (Button)findViewById(R.id.confirmSMSNextBT);
        confirmSMSNextBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(confirmSMSActivity.this,verifyMobileNumberActivity.class));
            }
        });
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
    protected void onStop() {
        if (myCounter != null){
            myCounter.cancel();
            myCounter = null;
            callController.unBindController();
            callController = null;
        }
        super.onStop();
    }

    private void showToast(String message){
        Toast.makeText(confirmSMSActivity.this,message,Toast.LENGTH_LONG).show();
    }
}
