package baddel.baddelstationapp.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by mahmo on 2017-06-16.
 */

public class trip_DS {

    public int tripId;
    public String slotStatus;

    public int slotProfileId;
    public String slotProfileUserName;
    public String slotProfilePhoneNumber;
    public String slotProfileFirstName;
    public String slotProfileLastName;
    public int slotProfileBalance;
    public boolean slotProfileIsBlocked;
    public boolean slotProfileIsRegistered;

    public int slotBikeId;
    public String slotBikeDeviceIMEI;
    public int slotBikeChargeLevel;
    public String slotBikeStatus;
    public String slotBikeReservationExpiration;

    public int slotDuration;
    public String slotCost;
    public String slotCostCalculationMethod;
    public String slotSecurityToken;

    public int startSlotId;
    public int startSlotNumber;
    public int startSlotStationId;
    public int startSlotBikeId;
    public String startSlotBikeDeviceIMEI;
    public int startSlotBikeChargeLevel;
    public String startSlotBikeStatus;
    public String startSlotBikeReservationExpiration;

    public String FinishSlot;
    public String TripCoordinates;
    public String StartTime;
    public String FinishTime;
    public int PlannedDuration;

    public ArrayList<trip_DS> currentTripObjects = new ArrayList<>();

    private static final String slotId_OBJECT = "Id";
    private static final String slotStatus_OBJECT = "Status";

    private static final String Profile_OBJECT = "Profile";
    private static final String ProfileId_OBJECT = "Id";
    private static final String ProfilePhoneNumber_OBJECT = "PhoneNumber";
    private static final String ProfileFirstName_OBJECT = "FirstName";
    private static final String ProfileLastName_OBJECT = "LastName";
    private static final String ProfileBalance_OBJECT = "Balance";
    private static final String ProfileIsBlocked_OBJECT = "IsBlocked";
    private static final String ProfileIsRegistered_OBJECT = "IsRegistered";

    private static final String Bike_OBJECT = "Bike";
    private static final String BikeId_OBJECT = "Id";
    private static final String BikeDeviceIMEI_OBJECT = "DeviceIMEI";
    private static final String BikeChargeLevel_OBJECT = "ChargeLevel";
    private static final String BikeBikeStatus_OBJECT = "BikeStatus";
    private static final String BikeReservationExpiration_OBJECT = "ReservationExpiration";

    private static final String Duration_OBJECT = "Duration";
    //private static final String Cost_OBJECT = "Cost";
    private static final String CostCalculationMethod_OBJECT = "CostCalculationMethod";
    private static final String SecurityToken_OBJECT = "SecurityToken";

    private static final String StartSlot_OBJECT = "StartSlot";
    private static final String StartSlotId_OBJECT = "Id";
    private static final String StartSlotSlotNumber_OBJECT = "SlotNumber";
    private static final String StartSlotStationId_OBJECT = "StationId";

    private static final String StartSlotBike_OBJECT = "Bike";
    private static final String StartSlotBikeId_OBJECT = "Id";
    private static final String StartSlotBikeDeviceIMEI_OBJECT = "DeviceIMEI";
    private static final String StartSlotBikeChargeLevel_OBJECT = "ChargeLevel";
    private static final String StartSlotBikeStatus_OBJECT = "BikeStatus";
    private static final String StartSlotBikeReservationExpiration_OBJECT = "ReservationExpiration";

    //    private static final String slotFinishSlot_OBJECT = "FinishSlot";
//    private static final String slotTripCoordinates_OBJECT = "FinishSlot";
//    private static final String slotStartTime_OBJECT = "StartTime";
//    private static final String slotFinishTime_OBJECT = "FinishTime";
    private static final String slotPlannedDuration_OBJECT = "PlannedDuration";


    public trip_DS(int tripId, String slotStatus, int slotProfileId,
                   String slotProfileUserName, String slotProfilePhoneNumber,
                   String slotProfileFirstName, String slotProfileLastName,
                   int slotProfileBalance, boolean slotProfileIsBlocked,
                   boolean slotProfileIsRegistered, int slotBikeId,
                   String slotBikeDeviceIMEI, int slotBikeChargeLevel,
                   String slotBikeStatus, String slotBikeReservationExpiration, int slotDuration,
                   String slotCost, String slotCostCalculationMethod, String slotSecurityToken,
                   int startSlotId, int startSlotNumber, int startSlotStationId,
                   int startSlotBikeId, String startSlotBikeDeviceIMEI,
                   int startSlotBikeChargeLevel, String startSlotBikeStatus,
                   String startSlotBikeReservationExpiration, String finishSlot,
                   String tripCoordinates, String startTime, String finishTime, int plannedDuration) {
        this.tripId = tripId;
        this.slotStatus = slotStatus;
        this.slotProfileId = slotProfileId;
        this.slotProfileUserName = slotProfileUserName;
        this.slotProfilePhoneNumber = slotProfilePhoneNumber;
        this.slotProfileFirstName = slotProfileFirstName;
        this.slotProfileLastName = slotProfileLastName;
        this.slotProfileBalance = slotProfileBalance;
        this.slotProfileIsBlocked = slotProfileIsBlocked;
        this.slotProfileIsRegistered = slotProfileIsRegistered;
        this.slotBikeId = slotBikeId;
        this.slotBikeDeviceIMEI = slotBikeDeviceIMEI;
        this.slotBikeChargeLevel = slotBikeChargeLevel;
        this.slotBikeStatus = slotBikeStatus;
        this.slotBikeReservationExpiration = slotBikeReservationExpiration;
        this.slotDuration = slotDuration;
        this.slotCost = slotCost;
        this.slotCostCalculationMethod = slotCostCalculationMethod;
        this.slotSecurityToken = slotSecurityToken;
        this.startSlotId = startSlotId;
        this.startSlotNumber = startSlotNumber;
        this.startSlotStationId = startSlotStationId;
        this.startSlotBikeId = startSlotBikeId;
        this.startSlotBikeDeviceIMEI = startSlotBikeDeviceIMEI;
        this.startSlotBikeChargeLevel = startSlotBikeChargeLevel;
        this.startSlotBikeStatus = startSlotBikeStatus;
        this.startSlotBikeReservationExpiration = startSlotBikeReservationExpiration;
        FinishSlot = finishSlot;
        TripCoordinates = tripCoordinates;
        StartTime = startTime;
        FinishTime = finishTime;
        PlannedDuration = plannedDuration;
    }

    public trip_DS(String response) {
        try {
            JSONObject JsonObject = new JSONObject(response);

            this.tripId = JsonObject.getInt(slotId_OBJECT);
            this.slotStatus = JsonObject.getString(slotStatus_OBJECT);

            JSONObject profileObject = JsonObject.getJSONObject(Profile_OBJECT);
            this.slotProfileId = profileObject.getInt(ProfileId_OBJECT);
            this.slotProfilePhoneNumber = profileObject.getString(ProfilePhoneNumber_OBJECT);
            this.slotProfileFirstName = profileObject.getString(ProfileFirstName_OBJECT);
            this.slotProfileLastName = profileObject.getString(ProfileLastName_OBJECT);
            this.slotProfileBalance = profileObject.getInt(ProfileBalance_OBJECT);
            this.slotProfileIsBlocked = profileObject.getBoolean(ProfileIsBlocked_OBJECT);
            this.slotProfileIsRegistered = profileObject.getBoolean(ProfileIsRegistered_OBJECT);

            JSONObject bikeObject = JsonObject.getJSONObject(Bike_OBJECT);
            this.slotBikeId = bikeObject.getInt(BikeId_OBJECT);
            this.slotBikeDeviceIMEI = bikeObject.getString(BikeDeviceIMEI_OBJECT);
            this.slotBikeChargeLevel = bikeObject.getInt(BikeChargeLevel_OBJECT);
            this.slotBikeStatus = bikeObject.getString(BikeBikeStatus_OBJECT);
            this.slotBikeReservationExpiration = bikeObject.getString(BikeReservationExpiration_OBJECT);


            this.slotDuration = JsonObject.getInt(Duration_OBJECT);
//            this.slotCost = JsonObject.getString(Cost_OBJECT);
            this.slotCostCalculationMethod = JsonObject.getString(CostCalculationMethod_OBJECT);
            try {
                this.slotSecurityToken = JsonObject.getString(SecurityToken_OBJECT);
            } catch (JSONException e) {

            }

            JSONObject startSlotObject = JsonObject.getJSONObject(StartSlot_OBJECT);
            this.startSlotId = startSlotObject.getInt(StartSlotId_OBJECT);
            this.startSlotNumber = startSlotObject.getInt(StartSlotSlotNumber_OBJECT);
            this.startSlotStationId = startSlotObject.getInt(StartSlotStationId_OBJECT);

            JSONObject startSlotBikeObject = JsonObject.getJSONObject(StartSlotBike_OBJECT);
            this.startSlotBikeId = startSlotBikeObject.getInt(StartSlotBikeId_OBJECT);
            this.startSlotBikeDeviceIMEI = startSlotBikeObject.getString(StartSlotBikeDeviceIMEI_OBJECT);
            this.startSlotBikeChargeLevel = startSlotBikeObject.getInt(StartSlotBikeChargeLevel_OBJECT);
            this.startSlotBikeStatus = startSlotBikeObject.getString(StartSlotBikeStatus_OBJECT);
            this.startSlotBikeReservationExpiration = startSlotBikeObject.getString(StartSlotBikeReservationExpiration_OBJECT);


//            FinishSlot = JsonObject.getString(slotFinishSlot_OBJECT);
//            TripCoordinates = JsonObject.getString(slotTripCoordinates_OBJECT);
//            StartTime = JsonObject.getString(slotStartTime_OBJECT);
//            FinishTime = JsonObject.getString(slotFinishTime_OBJECT);
            PlannedDuration = JsonObject.getInt(slotPlannedDuration_OBJECT);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
