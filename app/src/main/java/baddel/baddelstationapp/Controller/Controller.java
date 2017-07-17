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
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import baddel.baddelstationapp.ClientTCPSocketing.OnMessageReceived;
import baddel.baddelstationapp.ClientTCPSocketing.TCPClient;
import baddel.baddelstationapp.ClientWebSocketSignalR.SignalRService;
import baddel.baddelstationapp.ClientWebSocketSignalR.signalRDelegate;
import baddel.baddelstationapp.Models.trip_DS;
import baddel.baddelstationapp.connectToServer.myAsyncTask;
import baddel.baddelstationapp.connectToServer.responseDelegate;
import baddel.baddelstationapp.internalStorage.SQliteDB;
import baddel.baddelstationapp.internalStorage.Session;

/**
 * Created by mahmo on 2017-06-16.
 */

public class Controller extends Service implements responseDelegate {

    //tcp Socket variables
    private TCPClient mTcpClient;
    private Handler handler;


    //general variables
    private String ControllerTag = "tcp";
    private Context myContext;

    //signalR service
    private SignalRService mService;
    private boolean mBound = false;


    //HTTP asyncTask
    private myAsyncTask asyncTask;
    private SQliteDB sQliteDB;


    private final IBinder mBinder = new tripControllerBinder(); // Binder given to clients

    public Controller() {
    }

    public Controller(Context myContext) {
        this.myContext = myContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler(getApplicationContext().getMainLooper());

        sQliteDB = new SQliteDB(Controller.this);
        startWebSocketService();
        getStartTripDelegate();

        // connect to the server
        mTcpClient = new TCPClient();
        //mTcpClient.runTCP();
        startTCPClient();
        listenToSocket();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);

        handler = new Handler(getApplicationContext().getMainLooper());

        sQliteDB = new SQliteDB(Controller.this);
        startWebSocketService();
        getStartTripDelegate();

        // connect to the server
        mTcpClient = new TCPClient();
        //mTcpClient.runTCP();
        startTCPClient();
        listenToSocket();

        return result;
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(Controller.this, TCPClient.class));
        unbindService(mConnection);
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
        Intent intent = new Intent();
        intent.setClass(this, SignalRService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void getStartTripDelegate() {
        new SignalRService().setOnResponseListener(new signalRDelegate() {
            @Override
            public void getOnStartTripResponse(String response) {
                Log.d(ControllerTag, response);
                trip_DS trip_ds = new trip_DS(response);
                //add 0 before numbers of one digit to send two digits for all values
                Log.d(ControllerTag, String.valueOf(trip_ds.startSlotNumber));
                String slotNumSTR = String.valueOf(trip_ds.startSlotNumber);
//                if (trip_ds.startSlotId > 0 && trip_ds.startSlotId < 10) {
//                    slotNumSTR = String.valueOf(trip_ds.startSlotId);
//                } else {
//                    slotNumSTR = String.valueOf(trip_ds.startSlotId);
//                }
                Log.d(ControllerTag, slotNumSTR);
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

    public void sendToTCP(String SlotNumber, final trip_DS slotObject) {
        String Message = "UNLOCK_BIKE_" + SlotNumber;
        if (mTcpClient != null) {
            mTcpClient.sendMessage(Message);
        }

        mTcpClient.setOnResponseListener(new OnMessageReceived() {
            @Override
            public void messageReceived(String message) {
                //this method calls the onProgressUpdate
                Session.getInstance().setMessageResponse(message);

                Log.d("tcpSendToTCP", message);

                if (message.equals("TRUE")) {
                    StartTripRequest(slotObject);
                }else if (message.contains("LOCK_BIKE")) {
                    String[] result = message.split("_");
                    String lockedSlotNumber = result[2];
                    String lockedBikeIMEI = result[3];

                    FinishTripRequest(lockedSlotNumber, lockedBikeIMEI);

                    String Message = "finished";
                    if (mTcpClient != null) {
                        mTcpClient.sendMessage(Message);
                    }
                }

            }
        });
    }

    private void listenToSocket() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTcpClient.setOnResponseListener(new OnMessageReceived() {
                    @Override
                    public void messageReceived(String message) {
                        //this method calls the onProgressUpdate
                        Session.getInstance().setMessageResponse(message);

                        Log.d("tcpListenToSocket", message);

                        if (message.contains("LOCK_BIKE")) {
                            String[] result = message.split("_");
                            String lockedSlotNumber = result[2];
                            String lockedBikeIMEI = result[3];

                            FinishTripRequest(lockedSlotNumber, lockedBikeIMEI);

                            String Message = "finished";
                            if (mTcpClient != null) {
                                mTcpClient.sendMessage(Message);
                            }
                        }

                    }
                });
            }
        });
    }

    private void runOnUiThread(Runnable r) {
        handler.post(r);
    }

    private void startTCPClient() {
        startService(new Intent(Controller.this, TCPClient.class));
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            SignalRService.LocalBinder binder = (SignalRService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private void StartTripRequest(trip_DS slotObject) {
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getAPIMETHODPutStartTrip();
        int myProcessNum = 4;

//        JSONObject startTripObject = new JSONObject();
//        try {
//            startTripObject.put("tripId", slotObject.tripId);
//            startTripObject.put("SecurityToken", slotObject.slotSecurityToken);
//            startTripObject.put("BikeDeviceIMEI", slotObject.slotBikeDeviceIMEI);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }


        JSONObject startTripObject = new JSONObject();
        try {
            startTripObject.put("tripId", slotObject.tripId);
            startTripObject.put("SecurityToken", slotObject.slotSecurityToken);
            startTripObject.put("BikeDeviceIMEI", slotObject.slotBikeDeviceIMEI);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HashMap<String, String> data = new HashMap<>();
        data.put("startTrip", startTripObject.toString());

        String URL = myURL + apiMethod;

        if (isNetworkConnected()) {

            asyncTask = new myAsyncTask(Controller.this, data, URL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 3);

            asyncTask.delegate = this;

//            asyncTask.execute();
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        } else {
            showToast("No Internet Connection");
        }
    }

    private void FinishTripRequest(String slotNumber, String BikeIMEI) {
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getAPIMETHODPutFinishTrip();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String date = df.format(new Date());
        Log.d(ControllerTag, "time: "+date);
        String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
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

        String URL = myURL + apiMethod;
        if (isNetworkConnected()) {
            asyncTask = new myAsyncTask(Controller.this, data, URL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 3);

            asyncTask.delegate = this;

            //asyncTask.execute();
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else {
            showToast("No Internet Connection");
        }
    }

    private void showToast(String msg) {
        Toast.makeText(Controller.this, msg, Toast.LENGTH_LONG).show();
    }

    //check the connection
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void getServerResponse(String response, int ProcessNum) {
        switch (ProcessNum) {
            case 4:
                //putRequest StartTrip
                Log.d("getStartTrip", response);
                break;
            case 5:
                //putRequest finishTrip
                Log.d("getFinishTrip", response);

//                String Message = "finished";
//                if (mTcpClient != null) {
//                    mTcpClient.sendMessage(Message);
//                }

                break;
            default:
                break;
        }
    }

}
