package baddel.baddelstationapp.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by mahmo on 2017-08-31.
 */

public class systemSettingDS {

    public String Key;
    public String Value;

    private HashMap<String,String> systemSettings = new HashMap<>();

    public systemSettingDS(String jsonResponse){
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);

            JSONObject ObjectOfArray;

            for (int i = 0; i < jsonArray.length();i++){

                ObjectOfArray = jsonArray.getJSONObject(i);

                Key = ObjectOfArray.getString("Key");

                Value = ObjectOfArray.getString("Value");

                systemSettings.put(Key,Value);


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String,String> currentSystemSettings(){
        return systemSettings;
    }

    public String currentSupportNumberSystemSettings(){
        String supportNumber = systemSettings.get("supportNumber");
        return supportNumber;
    }

}
