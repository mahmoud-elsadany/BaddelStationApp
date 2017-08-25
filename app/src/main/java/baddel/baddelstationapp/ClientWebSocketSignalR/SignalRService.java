package baddel.baddelstationapp.ClientWebSocketSignalR;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import baddel.baddelstationapp.internalStorage.SQliteDB;
import baddel.baddelstationapp.internalStorage.Session;
import baddel.baddelstationapp.saveLogs.myLogs;
import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler2;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;
import microsoft.aspnet.signalr.client.transport.WebsocketTransport;

/**
 * Created by mahmo on 2017-06-12.
 */

public class SignalRService extends Service {
    private HubConnection mHubConnection;
    private HubProxy mHubProxy;
    private SignalRFuture<Void> signalRFuture;
    private final IBinder mBinder = new LocalBinder(); // Binder given to clients
    private String LOGTAG = "SignalRService";
    private SignalRFuture<Void> mSignalRFuture;
    private ClientTransport mClientTransport;
    private final String SIGNALR_HUB = Session.getInstance().getWebSocketHub();


    private int mInterval = 300000; // 5 minutes by default, can be changed later
    //private int mInterval = 3000; // 3 sec default, can be changed later
    private Handler mHandler;

    public static signalRDelegate responseDelegate;

    public void setOnResponseListener(signalRDelegate listener) {
        responseDelegate = listener;
    }

    public SignalRService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initSignalr();

        mHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);

        startRepeatingTask();

        return result;
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
//                if (Session.getInstance().getTcpDown()) {
//                    //tcp has been down
//                    if (socket != null) {
//                        if (socket.isConnected()) {
//                            try {
//                                socket.close();
//                                socket.shutdownInput();
//                                socket.shutdownOutput();
//                                socket.setKeepAlive(false);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        socket = null;
//                    }
//                    //runTCP();
//                    firstTimeOpenSocket = 0;
//                    Log.d("TCPreconnect", "tcp down");
//                } else {
//                    //tcp is connected
//                    // runTCP();
//                    if (firstTimeOpenSocket == 0)
//                        runTCP();
//
//                    firstTimeOpenSocket = 1;
//
//                    Log.d("TCPreconnect", "tcp connected");
//                }
                testConnection();
                myLogs.logMyLog(LOGTAG, "test connection");

            } catch (Exception e) {
                initSignalr();
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
        Log.d(LOGTAG, "signalR destroyed");

        stopRepeatingTask();
        mHubConnection.stop();
        stopSelf();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public SignalRService getService() {
            return SignalRService.this;
        }
    }


    private void testConnection() {
        Log.d(LOGTAG, "signalR test Connection");

        initSignalr();

        mHubConnection.reconnecting(new Runnable() {
            @Override
            public void run() {
                //mHubConnection.disconnect();
                mHubConnection.stop();

                myLogs.logMyLog(LOGTAG, "Reconnecting");
            }
        });

        mHubConnection.reconnected(new Runnable() {
            @Override
            public void run() {
                myLogs.logMyLog(LOGTAG, "Reconnected");
            }
        });
        mHubConnection.connectionSlow(new Runnable() {
            @Override
            public void run() {
                //mHubConnection.disconnect();
                mHubConnection.stop();
                myLogs.logMyLog(LOGTAG, "Connection Slow");
            }
        });
        mHubConnection.closed(new Runnable() {
            @Override
            public void run() {
                myLogs.logMyLog(LOGTAG, "Connection CLosed");

                initSignalr();

            }
        });
    }


    private void initSignalr() {
        String serverUrl = Session.getInstance().getWebSocketBaseUrl();
        SQliteDB sQliteDB = new SQliteDB(this);
        String stationID = sQliteDB.getStationID();
        myLogs.logMyLog(LOGTAG, "stationIDForSignalR:" + stationID);

        if (stationID != null) {
            Logger mLogger = new Logger() {
                @Override
                public void log(String s, LogLevel logLevel) {

                }
            };
            Platform.loadPlatformComponent(new AndroidPlatformComponent());

            mHubConnection = new HubConnection(serverUrl, "stationId=" + stationID, false, mLogger);
            mHubProxy = mHubConnection.createHubProxy(SIGNALR_HUB);
            mClientTransport = new ServerSentEventsTransport(mHubConnection.getLogger());
            myLogs.logMyLog(LOGTAG, "initiated signalR" + stationID);

            signalRFuture = mHubConnection.start(mClientTransport);

            try {
                //signalRFuture.get(3000, TimeUnit.MILLISECONDS);
                signalRFuture.get();
                myLogs.logMyLog(LOGTAG, "started signalR socket");
            } catch (InterruptedException | ExecutionException e) {
                myLogs.logMyLog(LOGTAG, "exception_signalFuture: " + e.toString());
                initSignalr();
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

                    myLogs.logMyLog(LOGTAG, "staff: " + staff);
                    myLogs.logMyLog(LOGTAG, "tripIdSTR: " + tripIdSTR);

                    if (responseDelegate != null)
                        responseDelegate.getOnStartTripResponse(staff);
                }
            };

            mHubProxy.on(webSocketStartTripOnMethod, StartTripHandler, Object.class, Object.class);

            //subscribeToEvents();

//            catch (TimeoutException e) {
//                e.printStackTrace();
//                myLogs.logMyLog(LOGTAG, "exception_signalFuture: " + e.toString());
//                startSignal();
//            }
        }
    }


}
