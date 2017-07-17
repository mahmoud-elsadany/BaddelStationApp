package baddel.baddelstationapp.Controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by mahmo on 2017-07-10.
 */

public class callController {

    private Controller Controller;
    private boolean mBound = false;

    public callController(Context myContext) {
        Intent intent = new Intent();
        intent.setClass(myContext, Controller.class);
        myContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Controller = new Controller(myContext);
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Controller.tripControllerBinder binder = (Controller.tripControllerBinder) service;
            Controller = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void unBindController(){
        if (mBound)
            Controller.stopSelf();
    }
}
