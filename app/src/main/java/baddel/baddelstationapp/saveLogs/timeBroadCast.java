package baddel.baddelstationapp.saveLogs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import baddel.baddelstationapp.startActivity;

/**
 * Created by mahmo on 2017-06-17.
 */

public class timeBroadCast extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {

//        Intent serviceIntent = new Intent();
//        serviceIntent.setClass(context,startActivity.class);
//        serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(serviceIntent);

        if(intent.getAction().compareTo(Intent.ACTION_TIME_TICK)==0) {
            String hourOfDay = String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
            String minutes = String.valueOf(Calendar.getInstance().get(Calendar.MINUTE));

             int x = Calendar.getInstance().get(Calendar.MINUTE);

            if (x%2 == 0){
                myLogs.logMyLog("broadCastTickTimeTag","savingLogsTime: "+hourOfDay+":"+minutes);
                myLogs.saveLogsToFile();
            }

            Log.d("broadCastTickTag","Hello from timebroadCast "+hourOfDay+" "+minutes);
        }


    }
}
