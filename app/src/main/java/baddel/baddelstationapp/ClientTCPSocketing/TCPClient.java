package baddel.baddelstationapp.ClientTCPSocketing;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import baddel.baddelstationapp.Controller.Controller;
import baddel.baddelstationapp.chooseRentTimeActivity;
import baddel.baddelstationapp.customViews.customDialogs;
import baddel.baddelstationapp.internalStorage.Session;

/**
 * Created by mahmo on 2017-06-18.
 */

public class TCPClient extends Service {

    private static final String TCPTAG = "TCP";
    private final String internetTag = "checkTCPTag";

    private int firstTimeOpenSocket = 0;


    private String serverMessage;

    public static final String SERVERIP = Session.getInstance().getTcpSocketIP();
    public static final int SERVERPORT = Session.getInstance().getTcpSocketPORT();

    private Socket socket;

    private int mInterval = 2000; // 2 seconds by default, can be changed later
    private Handler mHandler;

    private Controller controller;

    static PrintWriter out;
    BufferedReader in;


    private final IBinder mBinder = new LocalBinder();

    public static OnMessageReceived responseDelegate;

    public void setOnResponseListener(OnMessageReceived listener) {
        responseDelegate = listener;
    }

    public TCPClient() {
        socket = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        runTCP();
//        controller = new Controller();
        mHandler = new Handler();
        startRepeatingTask();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);

        //mHandler = new Handler();
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


                sendMessage("@");
                Log.d("TCPtestsend", "@");

            } catch (Exception e) {
                Log.d("TCPtestsend", "test fail");
                runTCP();
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


    public void sendMessage(String message) {
        if (out != null && !out.checkError()) {
            Log.d("tcpReallySent", message);
            out.println(message);
            out.flush();
        }
    }

    private void runTCP() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (socket != null) {
                    try {
                        socket.close();
                        socket.shutdownInput();
                        socket.shutdownOutput();
                        socket.setKeepAlive(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    InetAddress serverAddr = InetAddress.getByName(SERVERIP);

                    socket = new Socket(serverAddr, SERVERPORT);

                    socket.setKeepAlive(true);

                    Log.d(TCPTAG, "C: Connecting...");

                    try {
                        String serverMessage = "";
                        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                        Log.d(TCPTAG, "C: Sent.");

                        Log.d(TCPTAG, "C: Done.");

                        sendMessage("REQUEST_MISSED_TRIPS");

                        //controller.requestMissedTrips();

                        //receive the message which the server sends back

                        byte[] buffer = new byte[1024];

                        int bytesRead;
                        InputStream inputStream = socket.getInputStream();


                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            ByteArrayOutputStream byteArrayOutputStream =
                                    new ByteArrayOutputStream(1024);
                            byteArrayOutputStream.write(buffer, 0, bytesRead);
                            serverMessage = byteArrayOutputStream.toString("UTF-8");


                            if (responseDelegate != null) {
                                //call the method messageReceived from MyActivity class
                                if (responseDelegate != null)
                                    responseDelegate.messageReceived(serverMessage);
                                Log.d(TCPTAG, "message:" + serverMessage);
                            }
                        }


                        Log.d(TCPTAG, "S: Received Message: '" + serverMessage + "'");


                    } catch (Exception e) {

                        Log.d(TCPTAG, "S: Error", e);

                    } finally {
                        //the socket must be closed. It is not possible to reconnect to this socket
                        // after it is closed, which means a new socket instance has to be created.
                        Log.d(TCPTAG, "here in finally");

                        if (socket != null) {
                            //mHandler = new Handler();
                            //startRepeatingTask();
                            socket.close();
                            runTCP();
                        }

                    }

                } catch (Exception e) {

                    Log.d(TCPTAG, "C: Error", e);

                    Log.d(TCPTAG, "Device is Out Of Service");

                    //startRepeatingTask();

                    runTCP();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String message) {

                //Session.getInstance().setTCPConnection(false);

                if (message == null) {
                    //TCPExceptionDialog.show();
                    //startRepeatingTask();
                    //runTCP();
                }


                Log.d(TCPTAG, "done connecting --> " + message);
            }
        }.execute();
    }


//    public void runTCP(){
//        try {
//            InetAddress serverAddr = InetAddress.getByName(SERVERIP);
//
//            socket = new Socket(serverAddr, SERVERPORT);
//
//            socket.setKeepAlive(true);
//
//            Log.d(TCPTAG, "C: Connecting...");
//
//            try {
//                String serverMessage = "";
//                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
//
//                Log.d(TCPTAG, "C: Sent.");
//
//                Log.d(TCPTAG, "C: Done.");
//
//                sendMessage("REQUEST_MISSED_TRIPS");
//
////                        controller.requestMissedTrips();
//
//                //TCPExceptionDialog.cancel();
//
//                //receive the message which the server sends back
//
//                byte[] buffer = new byte[1024];
//
//                int bytesRead;
//                InputStream inputStream = socket.getInputStream();
//
//
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    ByteArrayOutputStream byteArrayOutputStream =
//                            new ByteArrayOutputStream(1024);
//                    byteArrayOutputStream.write(buffer, 0, bytesRead);
//                    serverMessage = byteArrayOutputStream.toString("UTF-8");
//
//
//                    if (responseDelegate != null) {
//                        //call the method messageReceived from MyActivity class
//                        if (responseDelegate != null)
//                            responseDelegate.messageReceived(serverMessage);
//                        Log.d(TCPTAG, "message:" + serverMessage);
//                    }
//                }
//
//
//                Log.d(TCPTAG, "S: Received Message: '" + serverMessage + "'");
//
//
//            } catch (Exception e) {
//
//                Log.d(TCPTAG, "S: Error", e);
//
//            } finally {
//                //the socket must be closed. It is not possible to reconnect to this socket
//                // after it is closed, which means a new socket instance has to be created.
//                Log.d(TCPTAG, "here in finally");
//
//                if (socket != null) {
//                    //mHandler = new Handler();
//                    //startRepeatingTask();
//                    socket.close();
//                    runTCP();
//                }
//
//            }
//
//        } catch (Exception e) {
//
//            Log.d(TCPTAG, "C: Error", e);
//
//            Log.d(TCPTAG, "Device is Out Of Service");
//
//            //startRepeatingTask();
//
//            runTCP();
//        }
//
//        return null;
//    }

    public class LocalBinder extends Binder {
        public TCPClient getService() {
            return TCPClient.this;
        }
    }

    @Override
    public void onDestroy() {

        stopRepeatingTask();

        stopSelf();

        Log.d(internetTag, "internet destroyed");

        if (socket != null) {
            if (socket.isConnected()) {
                try {
                    socket.close();
                    socket.shutdownInput();
                    socket.shutdownOutput();
                    socket.setKeepAlive(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            socket = null;
        }

//        stopSelf();

        Log.d(TCPTAG, "Socket destroyed");
        super.onDestroy();
    }

}
