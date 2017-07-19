package baddel.baddelstationapp.connectToServer;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import baddel.baddelstationapp.ClientTCPSocketing.TCPClient;

/**
 * Created by mahmo on 2017-07-18.
 */

public class internetCheck extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public void onDestroy() {


        super.onDestroy();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                //runTCP(); //this function can change value of mInterval.
            } finally {
                //mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    private void startRepeatingTask() {
        mStatusChecker.run();
    }

//    private void stopRepeatingTask() {
//        mHandler.removeCallbacks(mStatusChecker);
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);

        startRepeatingTask();
        //runTCP();

        return result;
    }


//    public void runTCP() {
//        AsyncTask<Void, Void, String> tcpAsyncTask = new AsyncTask<Void, Void, String>() {
//
//            @Override
//            protected String doInBackground(Void... params) {
//
//                try {
//                    InetAddress ipAddr = InetAddress.getByName("google.com");
//
//                    return  !ipAddr.equals("");
//                } catch (Exception e) {
//
//                    Log.d(TCPTAG, "S: Error", e);
//
//                }
//
//
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(String message) {
//
//                //Session.getInstance().setTCPConnection(false);
//
//                if (message == null)
//                    TCPExceptionDialog.show();
//
//
//                Log.d(TCPTAG, "done connecting --> " + message);
//            }
//        };
//
//        tcpAsyncTask.execute();
//
//
//    }


}
