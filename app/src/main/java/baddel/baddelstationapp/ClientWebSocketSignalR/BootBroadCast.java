package baddel.baddelstationapp.ClientWebSocketSignalR;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import baddel.baddelstationapp.startActivity;

/**
 * Created by mahmo on 2017-06-17.
 */

public class BootBroadCast extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent serviceIntent = new Intent();
        serviceIntent.setClass(context,startActivity.class);
        serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(serviceIntent);

    }
}
