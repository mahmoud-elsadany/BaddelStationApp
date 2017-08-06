package baddel.baddelstationapp.customViews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import baddel.baddelstationapp.Controller.Controller;
import baddel.baddelstationapp.Controller.callController;
import baddel.baddelstationapp.R;
import baddel.baddelstationapp.chooseRentTimeActivity;
import baddel.baddelstationapp.internalStorage.Session;
import baddel.baddelstationapp.startActivity;
import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by mahmo on 2017-07-13.
 */

public class customDialogs {

    public static Dialog loadingDialog(Context myContext) {
        final Dialog dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setContentView(R.layout.loadingdialog);
        dialog.getWindow().setBackgroundDrawable(myContext.getResources().getDrawable(R.drawable.dialogshape));
        try {
            GifDrawable gifFromResource = new GifDrawable( myContext.getResources(), R.drawable.baddelgif);
            gifFromResource.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dialog;
    }

    public static Dialog ShowConnectionExceptionDialog(final Context myContext){
        final Dialog dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(false);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setContentView(R.layout.tcpconnectionexception);
        //dialog.getWindow().setBackgroundDrawable(myContext.getResources().getDrawable(R.drawable.dialogshape));

//        Controller controller = new Controller(myContext);
//        controller.requestMissedTrips();

        return dialog;
    }

    public static Dialog ShowTimeoutWarningDialog(final CountDownTimer myCounterDownTime, final Context myContext, final Class classContext){
        final Dialog dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setGravity(Gravity.CENTER);

        dialog.setCancelable(false);

        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setContentView(R.layout.time_out_exception_dialog);


        Button doneReservedBikesBT = (Button)dialog.findViewById(R.id.cancelTimeOutBT);
        doneReservedBikesBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                dialog.cancel();
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(myContext.getResources().getDrawable(R.drawable.dialogshape));


        return dialog;
    }

    public static Dialog ShowExitToSettingDialog(final Context myContext){
        final Dialog dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setGravity(Gravity.TOP);

        dialog.setCancelable(false);

        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setContentView(R.layout.exit_to_settings_dialog);

        final EditText kioskPassword = (EditText)dialog.findViewById(R.id.enterKioskPasswordET);

        Button doneReservedBikesBT = (Button)dialog.findViewById(R.id.exitToSettingsBT);
        doneReservedBikesBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (kioskPassword.getText().toString().equals("")){

                }else if (kioskPassword.getText().toString().equals(Session.getInstance().getKioskPassword())) {
                    myContext.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                }
                dialog.cancel();
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                kioskPassword.setText("");
            }
        });

        dialog.getWindow().setBackgroundDrawable(myContext.getResources().getDrawable(R.drawable.dialogshape));


        return dialog;
    }

    public static Dialog ShowReservedBikes(final Context myContext,final Class classContext,String slots){
        final Dialog dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setGravity(Gravity.CENTER);

        dialog.setCancelable(false);

        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setContentView(R.layout.reservedtripsdialog);

        TextView reservedTextView = (TextView)dialog.findViewById(R.id.slotNumbersTV);
        reservedTextView.setText(slots);

        Button doneReservedBikesBT = (Button)dialog.findViewById(R.id.slotNumbersSubmitBT);
        doneReservedBikesBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myContext.startActivity(new Intent(myContext,classContext));
                dialog.cancel();
            }
        });

        dialog.getWindow().setBackgroundDrawable(myContext.getResources().getDrawable(R.drawable.dialogshape));


        return dialog;
    }

    public static Dialog ShowDialogAfterStartTrip(final Context myContext,String slots){
        final Dialog dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setGravity(Gravity.CENTER);

        dialog.setCancelable(true);

        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setContentView(R.layout.after_start_trip_dialog);

        TextView reservedTextView = (TextView)dialog.findViewById(R.id.afterStartTripSlotsNumbersTV);
        reservedTextView.setText(slots);

        Button doneReservedBikesBT = (Button)dialog.findViewById(R.id.afterStartTripSlotNumbersSubmitBT);
        doneReservedBikesBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.getWindow().setBackgroundDrawable(myContext.getResources().getDrawable(R.drawable.dialogshape));


        return dialog;
    }

    public static Dialog ShowNoBikesAvailable(final Context myContext){
        final Dialog dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setGravity(Gravity.CENTER);

        dialog.setCancelable(false);

        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setContentView(R.layout.no_avaliable_bikes__dialog);

        Button noAvaliableBikesDialogSubmitBT = (Button)dialog.findViewById(R.id.noAvaliableBikesDialogSubmitBT);
        noAvaliableBikesDialogSubmitBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.getWindow().setBackgroundDrawable(myContext.getResources().getDrawable(R.drawable.dialogshape));


        //Sorry No available bikes at the time please come back later

        return dialog;
    }
//    public static Dialog ShowHelpDialog(final Context myContext){
//        final Dialog dialog = new Dialog(myContext);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.getWindow().setGravity(Gravity.TOP);
//
//        dialog.setCancelable(false);
//
//        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//        dialog.setContentView(R.layout.);
//
//        TextView reservedTextView = (TextView)dialog.findViewById(R.id.slotNumbersTV);
//        reservedTextView.setText();
//
//        dialog.getWindow().setBackgroundDrawable(myContext.getResources().getDrawable(R.drawable.dialogshape));
//
//
//        return dialog;
//    }
}
