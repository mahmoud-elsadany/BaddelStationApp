package baddel.baddelstationapp.Controller;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import baddel.baddelstationapp.ClientTCPSocketing.OnMessageReceived;
import baddel.baddelstationapp.ClientTCPSocketing.TCPClient;
import baddel.baddelstationapp.ClientWebSocketSignalR.SignalRService;
import baddel.baddelstationapp.ClientWebSocketSignalR.signalRDelegate;
import baddel.baddelstationapp.Models.trip_DS;
import baddel.baddelstationapp.connectToServer.myAsyncTask;
import baddel.baddelstationapp.connectToServer.responseDelegate;
import baddel.baddelstationapp.internalStorage.SQliteDB;
import baddel.baddelstationapp.internalStorage.Session;
import baddel.baddelstationapp.saveLogs.myLogs;

/**
 * Created by mahmo on 2017-06-16.
 */

public class Controller extends Service implements responseDelegate {

    private static final String controllerTag = "controllerTAG";

    //tcp Socket variables
    private static TCPClient mTcpClient;
    private Handler handler;

    //general variables
    private String ControllerTag = "tcp";
    private Context myContext;

    //signalR service
    private SignalRService mService;
    private TCPClient tcpService;
    private boolean mBound = false;
    private boolean tcpBound = false;


    //HTTP asyncTask
    private myAsyncTask asyncTask;
    private SQliteDB sQliteDB;

    //tripObjects queue
    private Queue startedTripsQueue;
    private Queue finishedTripsQueue;

    //to send the trips postponed
    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private Handler mHandler;

    //request from mobile
    private Boolean isMobile = false;

    private final IBinder mBinder = new tripControllerBinder(); // Binder given to clients

    public Controller() {
    }

    public Controller(Context myContext) {
        this.myContext = myContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        startedTripsQueue = new LinkedList();
        finishedTripsQueue = new LinkedList();

        mHandler = new Handler();


        handler = new Handler(getApplicationContext().getMainLooper());

        sQliteDB = new SQliteDB(Controller.this);
        startWebSocketService();
        getStartTripDelegate();

        // connect to the server
        mTcpClient = new TCPClient();
        //startTCPClient();
        listenToSocket();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);

        handler = new Handler(getApplicationContext().getMainLooper());

        sQliteDB = new SQliteDB(Controller.this);
        //startWebSocketService();
        getStartTripDelegate();

        // connect to the server
        mTcpClient = new TCPClient();
        //mTcpClient.runTCP();
        //startTCPClient();
        listenToSocket();

        startRepeatingTask();


        return result;
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if(isOnline()){
                    myLogs.logMyLog(controllerTag, "is online So send all in queue");
                    while (startedTripsQueue.size() > 0){
                        //to startTrips
                        String myURL = Session.getInstance().getWebServicesBaseUrl();
                        String apiMethod = Session.getInstance().getAPIMETHODPutStartTrip();
                        int myProcessNum = 4;
                        commonInRequest(myURL,apiMethod,"startTrip",startedTripsQueue,myProcessNum);
                    }
                    while (finishedTripsQueue.size() > 0){
                        //to finishTrips
                        String myURL = Session.getInstance().getWebServicesBaseUrl();
                        String apiMethod = Session.getInstance().getAPIMETHODPutFinishTrip();
                        int myProcessNum = 5;
                        commonInRequest(myURL,apiMethod,"finishTrip",finishedTripsQueue,myProcessNum);
                    }
                }else{
                    myLogs.logMyLog(controllerTag,"is offline So see all in queue");

                    myLogs.logMyLog(controllerTag,"startQueue size: "+startedTripsQueue.size());

                    myLogs.logMyLog(controllerTag,"finishQueue size: "+finishedTripsQueue.size());
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

    @Override
    public void onDestroy() {
        stopRepeatingTask();
        stopService(new Intent(Controller.this, TCPClient.class));
        stopService(new Intent(Controller.this, SignalRService.class));
        //unbindService(mConnection);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class tripControllerBinder extends Binder {
        public Controller getService() {
            return Controller.this;
        }
    }

    private void startWebSocketService() {
//        Intent intent = new Intent();
//        intent.setClass(this, SignalRService.class);
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        startService(new Intent(this, SignalRService.class));
    }

//    private void startTCPClient() {
//        Intent intent = new Intent();
//        intent.setClass(this, TCPClient.class);
//        bindService(intent, TCPConnection, Context.BIND_AUTO_CREATE);
//        startService(new Intent(Controller.this, TCPClient.class));
//    }

    private void getStartTripDelegate() {
        new SignalRService().setOnResponseListener(new signalRDelegate() {
            @Override
            public void getOnStartTripResponse(String response) {
                myLogs.logMyLog(ControllerTag, "fromMobile" + response);
                //Log.d(ControllerTag, "fromMobile" + response);
                trip_DS trip_ds = new trip_DS(response, 1);

                //add 0 before numbers of one digit to send two digits for all values
                myLogs.logMyLog(ControllerTag, String.valueOf(trip_ds.startSlotNumber));
                //Log.d(ControllerTag, String.valueOf(trip_ds.startSlotNumber));
                String slotNumSTR = String.valueOf(trip_ds.startSlotNumber);
//                if (trip_ds.startSlotId > 0 && trip_ds.startSlotId < 10) {
//                    slotNumSTR = String.valueOf(trip_ds.startSlotId);
//                } else {
//                    slotNumSTR = String.valueOf(trip_ds.startSlotId);
//                }

                myLogs.logMyLog(ControllerTag, slotNumSTR);
                //Log.d(ControllerTag, slotNumSTR);
                //initiate arrayList before adding items
                Session.getInstance().getCurrentTripArrayListObjects().add(trip_ds);
                if(trip_ds.startSlotNumber <= 9)
                    sendToTCP("0"+slotNumSTR, trip_ds);
                else
                    sendToTCP(slotNumSTR, trip_ds);
            }

//            @Override
//            public void getAppUrlResponse(String response) {
//                Log.d(ControllerTag, "updateUrl: " + response);
//                //String ApkUrl = "http://prollygeek.com/baddel/BaddelStationApp.apk";
//                //String AnotherApkUrl = "https://download.apkpure.com/custom/com.apkpure.aegon-904.apk?_fn=QVBLUHVyZV92MS43LjNfYXBrcHVyZS5jb20uYXBr&k=1ad50b842d2f15d853a9a4770fe9f02f59499bf3&as=d74b54a8e559dd4758c66d138f2ce9d25946f96b&_p=Y29tLmFwa3B1cmUuYWVnb24%3D&c=1%7CTOOLS&arg=apkpure%3A%2F%2Fcampaign%2F%3Futm_source%3Dapkpure%26utm_medium%3Dhome%26utm_term%3Da4d";
//                //String imageUrl = "https://static.pexels.com/photos/60597/dahlia-red-blossom-bloom-60597.jpeg";
//                downloadAndUpdateAPK downloadAndUpdateAPK = new downloadAndUpdateAPK(myContext,response,"BaddelStationApp.apk");
//                downloadAndUpdateAPK.initiateDownloading();
//            }
        });
    }

    public void sendToTCP(String SlotNumber, final trip_DS tripObject) {
        Session.getInstance().setSending(true);

        final String Message = "UNLOCK_BIKE_" + SlotNumber;

        //String Message = "UNLOCK_BIKE_01";
        myLogs.logMyLog(controllerTag, "tcp: "+Message);
        //Log.d(controllerTag, "tcp: "+Message);

        if (mTcpClient != null) {

            mTcpClient.sendMessage(Message);
            myLogs.logMyLog(controllerTag, "tcpSent: "+Message);
            //Log.d(controllerTag, "tcpSent: "+Message);

            mTcpClient.setOnResponseListener(new OnMessageReceived() {
                @Override
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    Session.getInstance().setMessageResponse(message);

                    ArrayList<trip_DS> currentTrips = Session.getInstance().getCurrentTripArrayListObjects();

                    myLogs.logMyLog(controllerTag, "receiveFromTCP: "+message);
                    //Log.d(controllerTag, "receiveFromTCP: "+message);

                    if (message.contains("_TRUE_")) {
                        //split to get slotNumber { UNLOCK_SLOTNUMBER_TRUE_BIKEGUID }
                        String[] result = message.split("_");
                        String unlockSlotNumber = result[1];
                        String unlockBikeIMEI = result[3];

                        if (currentTrips == null) {
                            //when it started from mobile
                            isMobile = true;
                            StartTripRequest(tripObject);
                        } else {
                            //when it started from station
                            isMobile = false;
                            for (trip_DS tripDsObj : currentTrips) {
                                if (tripDsObj.startSlotNumber == Integer.parseInt(unlockSlotNumber)) {
                                    StartTripRequest(tripDsObj);
                                    currentTrips.remove(tripDsObj);
                                }

                            }
                        }

                        String Message = "ack";
                        if (mTcpClient != null) {
                            mTcpClient.sendMessage(Message);
                        }
                        Session.getInstance().setSending(false);
                    }
                    if (message.contains("LOCK_BIKE")) {
                        //split to get slotNumber { LOCK_BIKE_SLOTNUMBER_BIKEGUID }
                        String[] result = message.split("_");
                        String lockedSlotNumber = result[2];
                        String lockedBikeIMEI = result[3];

                        FinishTripRequest(lockedSlotNumber, lockedBikeIMEI);

                        //String Message = "ENDTRIP_" + lockedSlotNumber + "_OK";
                        String Message = "ack";
                        if (mTcpClient != null) {
                            mTcpClient.sendMessage(Message);
                        }
                        Session.getInstance().setSending(false);

                    } else if (message.equals("")) {
                        //when he send me nothing
                        if (mTcpClient != null) {
                            mTcpClient.sendMessage(Message);
                        }
                        Session.getInstance().setSending(false);

                    }

//                    else if (message.contains("TRUE")) {
//                        //split to get slotNumber { TRUE }
//                        if (currentTrips == null) {
//                            //when it started from mobile
//                            isMobile = true;
//                            StartTripRequest(tripObject);
//                        }else{
//                            //when it started from station
//                            isMobile = false;
//                            for (final trip_DS tripDsObj : currentTrips) {
//                                new Handler().postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        StartTripRequest(tripDsObj);
//                                    }
//                                }, 1000);
//                            }
//                        }
//
//                        String Message = "ack";
//                        if (mTcpClient != null) {
//                            mTcpClient.sendMessage(Message);
//                        }
//
//                    }


                }
            });
        }
    }

//    public void requestMissedTrips() {
//        if (mTcpClient != null) {
//            String Message = "REQUEST_MISSED_TRIPS";
//            mTcpClient.sendMessage(Message);
//
//            Log.d("tcpSendToTCP", Message);
//
//            mTcpClient.setOnResponseListener(new OnMessageReceived() {
//                @Override
//                public void messageReceived(String message) {
//                    //this method calls the onProgressUpdate
//                    Session.getInstance().setMessageResponse(message);
//
//                    Log.d("tcpSendToTCP", message);
//
//                    if (message.contains(",LOCK_BIKE_")) {
//                        //split Missed requests LOCK_BIKE_SlotNumber_BIKEGUID,LOCK_BIKE_SlotNumber_BIKEGUID
//                        String[] missedTripsObjects = message.split(",");
//                        for (int i = 0; i < missedTripsObjects.length; i++) {
//                            if (message.contains("LOCK_BIKE")) {
//                                //split to get slotNumber { LOCK_BIKE_SLOTNUMBER_BIKEGUID }
//                                String[] result = message.split("_");
//                                String lockedSlotNumber = result[2];
//                                String lockedBikeIMEI = result[3];
//
//                                FinishTripRequest(lockedSlotNumber, lockedBikeIMEI);
//                            }
//                        }
//                    }else if (message.contains("LOCK_BIKE")){
//                        //missed requests with no array
//                        String[] result = message.split("_");
//                        String lockedSlotNumber = result[2];
//                        String lockedBikeIMEI = result[3];
//                        FinishTripRequest(lockedSlotNumber, lockedBikeIMEI);
//
//                        //String Message = "ENDTRIP_" + lockedSlotNumber + "_OK";
//                        String Message = "ack";
//                        if (mTcpClient != null) {
//                            mTcpClient.sendMessage(Message);
//                        }
//                    }
//                }
//            });
//        }
//    }

    private void listenToSocket() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTcpClient.setOnResponseListener(new OnMessageReceived() {
                    @Override
                    public void messageReceived(String message) {
                        //this method calls the onProgressUpdate
                        Session.getInstance().setMessageResponse(message);

//                        ArrayList<trip_DS> currentTrips = Session.getInstance().getCurrentTripArrayListObjects();

                        myLogs.logMyLog(controllerTag, "tcpListenToSocket: "+message);
                        //Log.d(controllerTag, "tcpListenToSocket: "+message);

                        if (message.contains("LOCK_BIKE")) {
                            String[] result = message.split("_");
                            String lockedSlotNumber = result[2];
                            String lockedBikeIMEI = result[3];

                            FinishTripRequest(lockedSlotNumber, lockedBikeIMEI);

                            String Message = "ack";
                            if (mTcpClient != null) {
                                mTcpClient.sendMessage(Message);
                            }
                        }
//                        else if (message.contains("_TRUE_")) {
//                            //split to get slotNumber { UNLOCK_SLOTNUMBER_TRUE_BIKEGUID }
//                            String[] result = message.split("_");
//                            String unlockSlotNumber = result[1];
//                            String unlockBikeIMEI = result[3];
//
//                            if (currentTrips != null){
//                                //when it started from station
//                                isMobile = false;
//                                for (trip_DS tripDsObj : currentTrips) {
//                                    if (tripDsObj.startSlotNumber == Integer.parseInt(unlockSlotNumber)) {
//                                        StartTripRequest(tripDsObj);
//                                    }
//                                }
//                            }
//                            String Message = "ack";
//                            if (mTcpClient != null) {
//                                mTcpClient.sendMessage(Message);
//                            }
//                            Session.getInstance().setSending(false);
//                        }
//                        else if (message.contains("_TRUE_")) {
//                            //split to get slotNumber { UNLOCK_SLOTNUMBER_TRUE_BIKEGUID }
//                            String[] result = message.split("_");
//                            final String unlockSlotNumber = result[1];
//                            final String unlockBikeIMEI = result[3];
//
//
//                            if (currentTrips == null) {
//                                //when it started from mobile
//                                isMobile = true;
//                                StartTripRequest(tripObject);
//                            } else {
//                                //when it started from station
//                                isMobile = false;
//                                for (final trip_DS tripDsObj : currentTrips) {
//                                    new Handler().postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
////                                        if (tripDsObj.startSlotNumber == Integer.parseInt(unlockSlotNumber) && tripDsObj.slotBikeDeviceIMEI.equals(unlockBikeIMEI)) {
////                                            StartTripRequest(tripDsObj);
////                                        }
//                                            if (tripDsObj.startSlotNumber == Integer.parseInt(unlockSlotNumber)) {
//                                                StartTripRequest(tripDsObj);
//                                            }
//                                        }
//                                    }, 1000);
//                                }
//                            }
//
////                        for (trip_DS tripDsObj : currentTrips) {
//////                            if (tripDsObj.startSlotNumber == Integer.parseInt(unlockSlotNumber) && tripDsObj.slotBikeDeviceIMEI.equals(unlockBikeIMEI)) {
//////                                StartTripRequest(tripDsObj);
//////                            }
////                            if (tripDsObj.startSlotNumber == Integer.parseInt(unlockSlotNumber)) {
////                                StartTripRequest(tripDsObj);
////                            }
////                        }
//
//                            String Message = "ack";
//                            if (mTcpClient != null) {
//                                mTcpClient.sendMessage(Message);
//                            }
//
//                        }
                        String Message = "ack";
                        if (mTcpClient != null) {
                            mTcpClient.sendMessage(Message);
                        }

                    }
                });
            }
        });
    }

    private void runOnUiThread(Runnable r) {
        handler.post(r);
    }

//    private final ServiceConnection mConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName className,
//                                       IBinder service) {
//            SignalRService.LocalBinder binder = (SignalRService.LocalBinder) service;
//            mService = binder.getService();
//            mBound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            mBound = false;
//        }
//    };

//    private final ServiceConnection TCPConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName className,
//                                       IBinder service) {
//            TCPClient.LocalBinder binder = (TCPClient.LocalBinder) service;
//            tcpService = binder.getService();
//            tcpBound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            mBound = false;
//        }
//    };

    private void StartTripRequest(trip_DS tripObj) {
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getAPIMETHODPutStartTrip();
        int myProcessNum = 4;

        JSONObject startTripObject = new JSONObject();
        try {
            startTripObject.put("tripId", tripObj.tripId);
            startTripObject.put("SecurityToken", tripObj.slotSecurityToken);
            startTripObject.put("BikeDeviceIMEI", tripObj.slotBikeDeviceIMEI);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HashMap<String, String> data = new HashMap<>();
        data.put("startTrip", startTripObject.toString());

        startedTripsQueue.add(startTripObject.toString());
//        myLogs.logMyLog(controllerTag, "startTripParameter: "+startTripObject.toString());
//        //Log.d(controllerTag, "startTripParameter: "+startTripObject.toString());
//
//        String URL = myURL + apiMethod;
//
//        if (isNetworkConnected() && isOnline()) {
//
//            while (startedTripsQueue.size() > 0) {
//
//                HashMap<String,String> exceptionData = new HashMap<>();
//                exceptionData.put("startTrip", startedTripsQueue.remove().toString());
//
//                myLogs.logMyLog(controllerTag, "trips_queue_size"+String.valueOf(startedTripsQueue.size()));
//                //Log.d(controllerTag, "trips_queue_size"+String.valueOf(startedTripsQueue.size()));
//
//                asyncTask = new myAsyncTask(Controller.this, exceptionData, URL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 3);
//
//                asyncTask.delegate = this;
//
//                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            }
//
//        } else {
//            //showToast("No Internet Connection");
//            myLogs.logMyLog(controllerTag, "trips_queue_size_NoNet"+String.valueOf(startedTripsQueue.size()));
//            //Log.d(controllerTag, "trips_queue_size_NoNet"+String.valueOf(startedTripsQueue.size()));
//
//        }

        commonInRequest(myURL,apiMethod,"startTrip",startedTripsQueue,myProcessNum);

    }

    private void FinishTripRequest(String slotNumber, String BikeIMEI) {
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getAPIMETHODPutFinishTrip();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String date = df.format(new Date());
        myLogs.logMyLog(ControllerTag, "time: " + date);
        //Log.d(ControllerTag, "time: " + date);
        //String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        String android_id = Session.getInstance().getAndroidId();
        int myProcessNum = 5;
        JSONObject finishTripObject = new JSONObject();

        try {
            finishTripObject.put("SlotNumber", slotNumber);
            finishTripObject.put("BikeIMEI", BikeIMEI);
            finishTripObject.put("FinishTime", "");
            finishTripObject.put("StationIMEI", android_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HashMap<String, String> data = new HashMap<>();
        data.put("finishTrip", finishTripObject.toString());

        finishedTripsQueue.add(finishTripObject.toString());

        commonInRequest(myURL,apiMethod,"finishTrip",finishedTripsQueue,myProcessNum);
//        myLogs.logMyLog(controllerTag, "finishTripObjectSTR: "+finishTripObject.toString());
//        //Log.d(controllerTag, "finishTripObjectSTR: "+finishTripObject.toString());
//
//        String URL = myURL + apiMethod;
//        if (isNetworkConnected() && isOnline()) {
//            while (finishedTripsQueue.size() > 0) {
//
//                HashMap<String, String> exceptionData = new HashMap<>();
//                exceptionData.put("finishTrip", finishedTripsQueue.remove().toString());
//
//                myLogs.logMyLog(controllerTag, "trips_queue_size"+String.valueOf(finishedTripsQueue.size()));
//                //Log.d(controllerTag, "trips_queue_size"+String.valueOf(finishedTripsQueue.size()));
//
//                asyncTask = new myAsyncTask(Controller.this, exceptionData, URL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 3);
//
//                asyncTask.delegate = this;
//
//                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//
//            }
//
//
//
//        } else {
//            //showToast("No Internet Connection");
//            myLogs.logMyLog(controllerTag, "trips_queue_size_NoNet"+String.valueOf(finishedTripsQueue.size()));
//            //Log.d(controllerTag, "trips_queue_size_NoNet"+String.valueOf(finishedTripsQueue.size()));
//        }
    }

    private void commonInRequest(String myURL,String apiMethod,String hashMapKey,Queue myQueue,int myProcessNum){
        String URL = myURL + apiMethod;

        if (true) {

            while (myQueue.size() > 0) {

                HashMap<String,String> exceptionData = new HashMap<>();
                exceptionData.put(hashMapKey, myQueue.remove().toString());

                myLogs.logMyLog(controllerTag, "trips_queue_size"+String.valueOf(myQueue.size()));
                //Log.d(controllerTag, "trips_queue_size"+String.valueOf(startedTripsQueue.size()));

                asyncTask = new myAsyncTask(Controller.this, exceptionData, URL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 3);

                asyncTask.delegate = this;

                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

        } else {
            myLogs.logMyLog(controllerTag, "trips_queue_size_NoNet"+String.valueOf(startedTripsQueue.size()));
        }
    }

    private void showToast(String msg) {
        Toast.makeText(Controller.this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void getServerResponse(String response, int ProcessNum) {
        switch (ProcessNum) {
            case 4:
                //putRequest StartTrip
                myLogs.logMyLog("getStartTripResponse", response);
                //Log.d("getStartTripResponse", response);

                if (!isMobile) {



                }

                break;
            case 5:
                //putRequest finishTrip
                myLogs.logMyLog("getFinishTrip", response);
                //Log.d("getFinishTrip", response);
//                String Message = "finished";
//                if (mTcpClient != null) {
//                    mTcpClient.sendMessage(Message);
//                }
                break;
            default:
                break;
        }
    }

    //check the connection
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
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



}
