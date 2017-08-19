package baddel.baddelstationapp.ClientWebSocketSignalR;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import baddel.baddelstationapp.internalStorage.SQliteDB;
import baddel.baddelstationapp.internalStorage.Session;
import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.Credentials;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.StateChangedCallback;
import microsoft.aspnet.signalr.client.http.Request;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler2;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;

/**
 * Created by mahmo on 2017-06-12.
 */

public class SignalRService extends Service {
    private HubConnection mHubConnection;
    private HubProxy mHubProxy;
    private Handler mHandler; // to display Toast message
    private final IBinder mBinder = new LocalBinder(); // Binder given to clients
    private String LOGTAG = "SignalRService";

    public static signalRDelegate responseDelegate;

    //private int mInterval = 3000; // 3 sec by default, can be changed later
    private int mInterval = 300000; // 5 min by interval
    //private int mInterval = 1800000; // 30 min by interval

    public void setOnResponseListener(signalRDelegate listener) {
        responseDelegate = listener;
    }

    public SignalRService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //mHandler = new Handler(Looper.getMainLooper());
        startSignalR();
        mHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);

        startRepeatingTask();
        //startSignalR();

        return result;
    }


    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                testConnection();
                Log.d(LOGTAG, "test Connection");
            } catch (Exception e) {
                Log.d(LOGTAG, "test fail");
            }


            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    private void startRepeatingTask() {
        mStatusChecker.run();
    }

    private void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    private void testConnection() {
        //State  Disconnected
        startSignalR();
        mHubConnection.stateChanged(new StateChangedCallback() {
            @Override
            public void stateChanged(ConnectionState connectionState, ConnectionState connectionState1) {
                if (connectionState.equals("Disconnected")) {
                    // disconnect was expected, for example, application is being shut down
                    Log.d(LOGTAG, "after Disconnected");
                    startSignalR();
                    return;
                }
            }
        });
//        if (mHubConnection.getState().equals("Disconnected")) {
//            // disconnect was expected, for example, application is being shut down
//            Log.d(LOGTAG, "after Disconnected");
//            startSignalR();
//            return;
//        }
        mHubConnection.error(new ErrorCallback() {
            @Override
            public void onError(Throwable throwable) {
                Log.d(LOGTAG, "SignalR Socket Error in test :" + throwable.toString());
                startSignalR();
            }
        });
        mHubConnection.closed(new Runnable() {
            @Override
            public void run() {

                Log.d(LOGTAG,"Socket has been closed");

                if (mHubConnection.getState().equals("Disconnected")) {
                    // disconnect was expected, for example, application is being shut down
                    Log.d(LOGTAG, "after Disconnected");
                    startSignalR();
//                    mHubConnection.reconnected(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.d(LOGTAG, "in Reconnected");
//                            startSignalR();
//                        }
//                    });
                    return;
                }

                new Timer(false).schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mHubConnection.start().done(new Action<Void>() {
                            @Override
                            public void run(Void v) throws Exception {
                                Log.d(LOGTAG, "test SignalRSocket connection started");
                            }
                        });
                    }
                }, 5000);

            }
        });


        //send heartBit to test connection
//        Log.d(LOGTAG, "receive message");
//
//        mHubProxy.invoke(String.class, "iAmAvailable", "Ack").done(new Action<String>() {
//            @Override
//            public void run(String returnValue) {
//                Log.d(LOGTAG, "receive message: " + returnValue);
//            }
//        }).onError(new ErrorCallback() {
//            @Override
//            public void onError(Throwable throwable) {
//                startSignalR();
//                Log.d(LOGTAG, "invoke error occurred");
//            }
//        });
//
//        Log.d(LOGTAG, "Socket in test connection");
    }

    @Override
    public void onDestroy() {
        stopRepeatingTask();
        mHubConnection.stop();
        stopSelf();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //startSignalR();
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public SignalRService getService() {
            return SignalRService.this;
        }
    }

    public void startSignalR() {
        SQliteDB sQliteDB = new SQliteDB(this);
        String stationID = sQliteDB.getStationID();
        Log.d(LOGTAG, "stationIDForSignalR:" + stationID);
        //String stationID = "2";

        if (stationID != null) {
            String serverUrl = Session.getInstance().getWebSocketBaseUrl();

            Platform.loadPlatformComponent(new AndroidPlatformComponent());

            Logger mLogger = new Logger() {
                @Override
                public void log(String s, LogLevel logLevel) {

                }
            };

            Credentials credentials = new Credentials() {
                @Override
                public void prepareRequest(Request request) {
                    request.addHeader("Username", Session.getInstance().getTokenUserName());
                    request.addHeader("Password", Session.getInstance().getTokenPassword());
                }
            };

            mHubConnection = new HubConnection(serverUrl, "stationId=" + stationID, true, mLogger);
            //mHubConnection = new HubConnection(serverUrl);
            String SERVER_HUB_CHAT = Session.getInstance().getWebSocketHub();
            mHubProxy = mHubConnection.createHubProxy(SERVER_HUB_CHAT);

            ClientTransport clientTransport = new ServerSentEventsTransport(mHubConnection.getLogger());
            //ClientTransport clientTransport = new WebsocketTransport(mHubConnection.getLogger());
            clientTransport.supportKeepAlive();

            SignalRFuture<Void> signalRFuture = mHubConnection.start().done(new Action<Void>() {
                @Override
                public void run(Void v) throws Exception {
                    Log.d(LOGTAG, "test SignalRSocket connection started");
                }
            });

            try {
                //signalRFuture.get(2000, TimeUnit.MILLISECONDS);
                signalRFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                Log.d(LOGTAG, e.toString());
                return;
            }

            String webSocketStartTripOnMethod = Session.getInstance().getWebSocketStartTripOnMethod();

            SubscriptionHandler2 StartTripHandler = new SubscriptionHandler2<Object, Object>() {
                @Override
                public void run(Object trip, Object tripId) {
                    //Here is where we get back the response from the server. Do stuffs
                    Gson gson = new Gson();
                    String staff = gson.toJson(trip);

                    String tripIdSTR = gson.toJson(tripId);

                    Log.d(LOGTAG, "staff: " + staff);
                    Log.d(LOGTAG, "tripIdSTR: " + tripIdSTR);

                    if (responseDelegate != null)
                        responseDelegate.getOnStartTripResponse(staff);
                }
            };

            mHubProxy.on(webSocketStartTripOnMethod, StartTripHandler, Object.class, Object.class);


        } else {
            Log.d(LOGTAG, "stationID NULL");
        }

    }


}
