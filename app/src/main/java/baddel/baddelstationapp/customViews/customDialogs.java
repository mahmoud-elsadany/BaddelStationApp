package baddel.baddelstationapp.customViews;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;

import baddel.baddelstationapp.R;
import baddel.baddelstationapp.chooseRentTimeActivity;
import baddel.baddelstationapp.enterPhoneNumberActivity;
import baddel.baddelstationapp.internalStorage.Session;
import baddel.baddelstationapp.saveLogs.myLogs;
import baddel.baddelstationapp.startActivity;
import pl.droidsonroids.gif.GifDrawable;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

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
            GifDrawable gifFromResource = new GifDrawable(myContext.getResources(), R.drawable.baddelgif);
            gifFromResource.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dialog;
    }

    public static Dialog ShowConnectionExceptionDialog(final Context myContext) {
        final int[] counter = {0};
        final Dialog dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setGravity(Gravity.TOP);
        dialog.setCancelable(false);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setContentView(R.layout.tcpconnectionexception);

        final EditText enterPasswordTcpConnectionET = (EditText) dialog.findViewById(R.id.enterPasswordTcpConnectionET);
        Button exitToSettingsTcpConnectionBT = (Button) dialog.findViewById(R.id.exitToSettingsTcpConnectionBT);
        final LinearLayout TcpConnectionLinearLayoutToGoOut = (LinearLayout) dialog.findViewById(R.id.TcpConnectionLinearLayoutToGoOut);


        TextView exceptionTextView = (TextView) dialog.findViewById(R.id.textView5exeptionDialog);

        exceptionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (counter[0] >= 3) {
                    TcpConnectionLinearLayoutToGoOut.setVisibility(View.VISIBLE);
                    myLogs.logMyLog("exitTheOutOfService", "trueInif");
                    //Log.d("exitTheOutOfService", "trueInif");
                    Session.getInstance().setTcpInterval(20000);
                }
                myLogs.logMyLog("exitTheOutOfService", "true" + counter[0]);
                //Log.d("exitTheOutOfService", "true" + counter[0]);
                counter[0]++;
            }
        });

        exitToSettingsTcpConnectionBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myLogs.logMyLog("iam here","hola");
                //Log.d("iam here","hola");

                if (enterPasswordTcpConnectionET.getText().toString().equals("")) {
                    dialog.cancel();
                } else if (enterPasswordTcpConnectionET.getText().toString().equals(Session.getInstance().getKioskPassword())) {
                    Intent closeAppWithKioskIntent = new Intent(myContext,chooseRentTimeActivity.class);
                    closeAppWithKioskIntent.putExtra("EXITKIOSK",true);
                    closeAppWithKioskIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    myContext.startActivity(closeAppWithKioskIntent);
//                    myContext.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                }
                dialog.cancel();

            }

//            @Override
//            public void onClick(View v) {
//                if (enterPasswordTcpConnectionET.getText().toString().equals("")) {
//                    dialog.cancel();
//                } else if (enterPasswordTcpConnectionET.getText().toString().equals(Session.getInstance().getKioskPassword())) {
//                    Intent closeApp = new Intent(myContext,startActivity.class);
//                    closeApp.setFlags(FLAG_ACTIVITY_NEW_TASK);
//                    closeApp.putExtra("EXIT",1);
//                    myContext.startActivity(closeApp);
//                }
//                Session.getInstance().setTcpInterval(3000);
//                dialog.cancel();
//            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                counter[0] = 0;
                enterPasswordTcpConnectionET.setText("");
                TcpConnectionLinearLayoutToGoOut.setVisibility(View.GONE);
            }
        });

        //dialog.getWindow().setBackgroundDrawable(myContext.getResources().getDrawable(R.drawable.dialogshape));
//        Controller controller = new Controller(myContext);
//        controller.requestMissedTrips();


        return dialog;
    }

    public static Dialog ShowTimeoutWarningDialog(final CountDownTimer myCounterDownTime, final Context myContext, final Class classContext) {
        final Dialog dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setGravity(Gravity.CENTER);

        dialog.setCancelable(false);

        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setContentView(R.layout.time_out_exception_dialog);


        Button doneReservedBikesBT = (Button) dialog.findViewById(R.id.cancelTimeOutBT);
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

    public static Dialog ShowExitToSettingDialog(final Context myContext) {
        final Dialog dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setGravity(Gravity.TOP);

        dialog.setCancelable(false);

        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setContentView(R.layout.exit_to_settings_dialog);

        final EditText kioskPassword = (EditText) dialog.findViewById(R.id.enterKioskPasswordET);

        Button doneReservedBikesBT = (Button) dialog.findViewById(R.id.exitToSettingsBT);
        doneReservedBikesBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (kioskPassword.getText().toString().equals("")) {
                    dialog.cancel();
                } else if (kioskPassword.getText().toString().equals(Session.getInstance().getKioskPassword())) {
//                    Intent closeAppWithKioskIntent = new Intent(myContext,chooseRentTimeActivity.class);
//                    closeAppWithKioskIntent.putExtra("EXITKIOSK",true);
//                    closeAppWithKioskIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
//                    myContext.startActivity(closeAppWithKioskIntent);
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

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                kioskPassword.setText("");
            }
        });

        return dialog;
    }

    public static Dialog ShowReservedBikes(final Context myContext, final Class classContext, String slots) {
        final Dialog dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setGravity(Gravity.CENTER);

        dialog.setCancelable(false);

        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setContentView(R.layout.reservedtripsdialog);

        TextView reservedTextView = (TextView) dialog.findViewById(R.id.slotNumbersTV);
        reservedTextView.setText(slots.substring(0, slots.length() - 2));

        Button doneReservedBikesBT = (Button) dialog.findViewById(R.id.slotNumbersSubmitBT);
        doneReservedBikesBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myContext.startActivity(new Intent(myContext, classContext));
                dialog.cancel();
            }
        });

        final Button cancelReservedBT = (Button) dialog.findViewById(R.id.slotNumbersCancelBT);
        cancelReservedBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cancelReservedTripsIntent = new Intent(myContext, myContext.getClass());
                cancelReservedTripsIntent.putExtra("cancelTripIntent", true);
                myContext.startActivity(cancelReservedTripsIntent);
                dialog.cancel();
            }
        });

        dialog.getWindow().setBackgroundDrawable(myContext.getResources().getDrawable(R.drawable.dialogshape));


        return dialog;
    }

    public static Dialog ShowDialogAfterStartTrip(final Context myContext, String slots) {
        final Dialog dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setGravity(Gravity.CENTER);

        dialog.setCancelable(true);

        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setContentView(R.layout.after_start_trip_dialog);

        TextView reservedTextView = (TextView) dialog.findViewById(R.id.afterStartTripSlotsNumbersTV);
        reservedTextView.setText(slots.substring(0, slots.length() - 2));

        Button doneReservedBikesBT = (Button) dialog.findViewById(R.id.afterStartTripSlotNumbersSubmitBT);
        doneReservedBikesBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myContext.startActivity(new Intent(myContext, startActivity.class));
                dialog.cancel();
            }
        });

        dialog.getWindow().setBackgroundDrawable(myContext.getResources().getDrawable(R.drawable.dialogshape));


        return dialog;
    }

    public static Dialog ShowWarningMessage(final Context myContext, String warningMessage) {
        final Dialog dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setGravity(Gravity.CENTER);

        dialog.setCancelable(false);

        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setContentView(R.layout.no_avaliable_bikes__dialog);

        TextView noAvaliableBikesDialogTV = (TextView) dialog.findViewById(R.id.noAvaliableBikesDialogTV);
        noAvaliableBikesDialogTV.setText(warningMessage);

        Button noAvaliableBikesDialogSubmitBT = (Button) dialog.findViewById(R.id.noAvaliableBikesDialogSubmitBT);
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
