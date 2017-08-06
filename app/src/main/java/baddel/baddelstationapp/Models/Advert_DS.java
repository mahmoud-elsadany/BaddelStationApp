package baddel.baddelstationapp.Models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by mahmo on 2017-07-31.
 */

public class Advert_DS {

    public String AdID;
    public String AdName;
    public String AdContent;

    public static final String ADId_Object = "Id";;
    public static final String ADName_Object = "Name";
    public static final String AdContent_Object = "Content";


    public Advert_DS(String adID, String adName, String adContent) {
        AdID = adID;
        AdName = adName;
        AdContent = adContent;
    }

    public Advert_DS(String AdStr) {

        try {
            JSONObject advertObject = new JSONObject(AdStr);

            Log.d("advertLog",advertObject.toString());

            AdID = advertObject.getString(ADId_Object);
            AdName = advertObject.getString(ADName_Object);
            AdContent = advertObject.getString(AdContent_Object);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }



}
