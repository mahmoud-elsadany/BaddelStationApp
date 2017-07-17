package baddel.baddelstationapp.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by mahmo on 2017-06-16.
 */

public class Station_DS {

    public int stationID;
    public String stationName;
    public String stationDeviceIMEI;
    public double stationLatitude;
    public double stationLongitude;
    public String stationCreationDate;
    public int stationNumberOfSlots;
    public int stationNumberOfAvailableBikes;
    public ArrayList<String> stationSlots;
    public ArrayList<String> stationAds;


    private static final String Id_Object = "Id";
    private static final String Name_Object = "Name";
    private static final String stationDeviceIMEI_Object = "DeviceIMEI";
    private static final String Location_Object = "Location";
    private static final String Latitude_Object = "Latitude";
    private static final String Longitude_Object = "Longitude";
    private static final String CreationDate_Object = "CreationDate";
    private static final String NumberOfSlots_Object = "NumberOfSlots";
    private static final String NumberOfAvailableBikes_Object = "NumberOfAvailableBikes";
    private static final String Slots_Object = "Slots";
    private static final String Ads_Object = "Ads";


         //////   "CreationDate": "2017-06-16T15:48:37.443",

    public Station_DS(int stationID, String stationName, String stationDeviceIMEI, double stationLatitude, double stationLongitude, String stationCreationDate, int stationNumberOfSlots, int stationNumberOfAvailableBikes, ArrayList<String> stationSlots, ArrayList<String> stationAds) {
        this.stationID = stationID;
        this.stationName = stationName;
        this.stationDeviceIMEI = stationDeviceIMEI;
        this.stationLatitude = stationLatitude;
        this.stationLongitude = stationLongitude;
        this.stationCreationDate = stationCreationDate;
        this.stationNumberOfSlots = stationNumberOfSlots;
        this.stationNumberOfAvailableBikes = stationNumberOfAvailableBikes;
        this.stationSlots = stationSlots;
        this.stationAds = stationAds;
    }

    public Station_DS(String response) {
        try {
            JSONObject JsonObject = new JSONObject(response);
            this.stationID = JsonObject.getInt(Id_Object);
            this.stationName = JsonObject.getString(Name_Object);
            this.stationDeviceIMEI = JsonObject.getString(stationDeviceIMEI_Object);

            JSONObject locationJsonObject = JsonObject.getJSONObject(Location_Object);
            this.stationLatitude = locationJsonObject.getDouble(Latitude_Object);
            this.stationLongitude = locationJsonObject.getDouble(Longitude_Object);

            this.stationCreationDate = JsonObject.getString(CreationDate_Object);
            this.stationNumberOfSlots = JsonObject.getInt(NumberOfSlots_Object);
            this.stationNumberOfAvailableBikes = JsonObject.getInt(NumberOfAvailableBikes_Object);

            JSONArray stationSlotsArray = JsonObject.getJSONArray(Slots_Object);
            JSONObject stationSlotObject;

            for (int i = 0;i<stationSlotsArray.length();i++) {
                stationSlotObject = stationSlotsArray.getJSONObject(i);
                this.stationSlots.add(stationSlotObject.getString(Slots_Object));
            }

            JSONArray stationAdsArray = JsonObject.getJSONArray(Slots_Object);
            JSONObject stationAdsObject;

            for (int i = 0;i<stationAdsArray.length();i++) {
                stationAdsObject = stationAdsArray.getJSONObject(i);
                this.stationSlots.add(stationAdsObject.getString(Ads_Object));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
