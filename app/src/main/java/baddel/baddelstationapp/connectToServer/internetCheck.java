package baddel.baddelstationapp.connectToServer;

import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.WeakHashMap;

import baddel.baddelstationapp.Controller.Controller;
import baddel.baddelstationapp.customViews.customDialogs;
import baddel.baddelstationapp.internalStorage.SQliteDB;
import baddel.baddelstationapp.internalStorage.Session;
import baddel.baddelstationapp.saveLogs.myLogs;

/**
 * Created by mahmo on 2017-07-18.
 */

public class internetCheck extends Service implements responseDelegate{

    private final String internetTag = "checkInternetTag";

    private myAsyncTask asyncTask;

    private int mInterval = 10000; // 10 seconds by default, can be changed later
    private Handler mHandler;

    private SQliteDB sQliteDB;
    private ArrayList<String> startTripsArrayList = new ArrayList<>();
    private ArrayList<String> finishTripsArrayList = new ArrayList<>();

    private Dialog TCPExceptionDialog;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sQliteDB = new SQliteDB(getApplicationContext());

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
                    Session.getInstance().setInternetAvailability(true);

//                    while (Session.getInstance().getFinishTripsOfflineObject().size() > 0) {
//                        uploadOldFinishTrip(Session.getInstance().getFinishTripsOfflineObject().remove());
//                    }
//                    while (Session.getInstance().getStartTripsOfflineObject().size() > 0) {
//                        uploadOldStartTrip(Session.getInstance().getStartTripsOfflineObject().remove());
//                    }
                    startTripsArrayList = sQliteDB.getAllStartedTrips();
                    finishTripsArrayList = sQliteDB.getAllFinishedTrips();

                    while (sQliteDB.numberOfStartedTrips() > 0) {
                        for (int i = 0;i < startTripsArrayList.size();i++){
                            uploadOldStartTrip(startTripsArrayList.get(i));
                        }
                        sQliteDB.deleteAllStartedTrips();
                    }
                    while (sQliteDB.numberOfFinishedTrips() > 0) {
                        for (int i = 0;i < finishTripsArrayList.size();i++){
                            uploadOldFinishTrip(finishTripsArrayList.get(i));
                        }
                        sQliteDB.deleteAllFinishedTrips();
                    }

                    //TCPExceptionDialog.cancel();
                }else{
                    myLogs.logMyLog(internetTag,"is offline");
                    Session.getInstance().setInternetAvailability(false);
                    //TCPExceptionDialog.show();
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
            return false;
        }
    }

    private void uploadOldStartTrip(String startTripObject){

        HashMap<String, String> data = new HashMap<>();
        data.put("startTrip", startTripObject);
        String TOTALURL = Session.getInstance().getWebServicesBaseUrl() + Session.getInstance().getAPIMETHODPutStartTrip();
        int myProcessNum = 1;

        myLogs.logMyLog(internetTag, "OldApiMethod: "+TOTALURL);

        asyncTask = new myAsyncTask(internetCheck.this, data, TOTALURL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 3);

        asyncTask.delegate = this;

        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void uploadOldFinishTrip(String finishTripObject){

        HashMap<String, String> data = new HashMap<>();
        data.put("finishTrip", finishTripObject);
        String TOTALURL = Session.getInstance().getWebServicesBaseUrl() + Session.getInstance().getAPIMETHODPutFinishTrip();
        int myProcessNum = 2;

        myLogs.logMyLog(internetTag, "OldApiMethod: "+TOTALURL);

        asyncTask = new myAsyncTask(internetCheck.this, data, TOTALURL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 3);

        asyncTask.delegate = this;

        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public void onDestroy() {

        stopRepeatingTask();

        stopSelf();

        myLogs.logMyLog(internetTag, "internet destroyed");
        //Log.d(internetTag, "internet destroyed");

        super.onDestroy();

    }

    @Override
    public void getServerResponse(String response, int ProcessNum) {
        switch (ProcessNum) {
            case 1:
                //putRequest StartTrip
                myLogs.logMyLog("getStartTripResponseOld", response);
                break;
            case 2:
                //putRequest finishTrip
                myLogs.logMyLog("getFinishTripOld", response);
                break;
            default:
                break;
        }
    }
}
