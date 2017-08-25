package baddel.baddelstationapp.saveLogs;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mahmo on 2017-08-23.
 */

public class myLogs {

    public static void logMyLog(String tag,String data){
        Log.d(tag,data);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String format = simpleDateFormat.format(new Date());

        String savingData = "**In: "+format+" , Tag: "+tag+" , "+"Data: "+data+"\n\n";
        try
        {
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File (sdCard.getAbsolutePath() +
                    "/stationLogs");
            if (!directory.exists())
            {
                directory.mkdirs();
            }

            File file = new File(directory, "mytext.txt");
            if (!file.exists())
            {
                file.createNewFile();
            }
            //FileOutputStream fOut = new FileOutputStream(file);

            FileOutputStream fOut = new FileOutputStream(file, true);

            OutputStreamWriter osw = new
                    OutputStreamWriter(fOut);

//---write the string to the file---
            osw.write(savingData);
            osw.flush();
            osw.close();

        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }


    }
}
