package baddel.baddelstationapp.ClientTCPSocketing;

import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import baddel.baddelstationapp.Controller.Controller;
import baddel.baddelstationapp.customViews.customDialogs;
import baddel.baddelstationapp.internalStorage.Session;
import baddel.baddelstationapp.startActivity;

/**
 * Created by mahmo on 2017-07-25.
 */

public class TCPcheck extends Service {

    private final String internetTag = "TCPtagCheck";

    private int mInterval = 6000; // 6 seconds by default, can be changed later
    private Handler mHandler;

    private Dialog TCPExceptionDialog;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        TCPExceptionDialog = customDialogs.ShowConnectionExceptionDialog(getApplicationContext());

        mHandler = new Handler();
        startRepeatingTask();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        int result = super.onStartCommand(intent, flags, startId);

        startRepeatingTask();
        return result;
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if(isOnline()){
                    Log.d(internetTag," connected");
//                    Session.getInstance().setTcpDown(false);
                    TCPExceptionDialog.cancel();
                }else{
                    Log.d(internetTag," disconnected");
//                    Session.getInstance().setTcpDown(true);
                    TCPExceptionDialog.show();
                }
            } finally {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    private void startRepeatingTask() {
        mStatusChecker.run();
    }

    private void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    private Boolean isOnline() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 "+ Session.getInstance().getTcpSocketIP());
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal==0);
            return reachable;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onDestroy() {
        TCPExceptionDialog.cancel();

        stopRepeatingTask();

        stopSelf();

        Log.d(internetTag, "internet destroyed");
        super.onDestroy();
    }

}

