package baddel.baddelstationapp.connectToServer;

import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import baddel.baddelstationapp.customViews.customDialogs;
import baddel.baddelstationapp.internalStorage.Session;
import baddel.baddelstationapp.saveLogs.myLogs;

/**
 * Created by mahmo on 2017-07-18.
 */

public class internetCheck extends Service {

    private final String internetTag = "checkInternetTag";

    private int mInterval = 10000; // 10 seconds by default, can be changed later
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

        //startRepeatingTask();
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
                    myLogs.logMyLog(internetTag, "is online");
                    //Log.d(internetTag,"is online");
                    TCPExceptionDialog.cancel();
                }else{
                    myLogs.logMyLog(internetTag,"is offline");
                    //Log.d(internetTag,"is offline");
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
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal==0);
            return reachable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onDestroy() {

        stopRepeatingTask();

        stopSelf();

        myLogs.logMyLog(internetTag, "internet destroyed");
        //Log.d(internetTag, "internet destroyed");

        super.onDestroy();

    }

}
