package baddel.baddelstationapp.customViews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;

import baddel.baddelstationapp.Controller.callController;
import baddel.baddelstationapp.R;
import baddel.baddelstationapp.internalStorage.Session;
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
        dialog.getWindow().setGravity(Gravity.TOP);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.cancel();
            }
        });
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setContentView(R.layout.tcpconnectionexception);
        dialog.getWindow().setBackgroundDrawable(myContext.getResources().getDrawable(R.drawable.dialogshape));

        return dialog;
    }
}
