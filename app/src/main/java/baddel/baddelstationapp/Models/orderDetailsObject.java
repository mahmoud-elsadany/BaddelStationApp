package baddel.baddelstationapp.Models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mahmo on 2017-07-04.
 */

public class orderDetailsObject {

    public int orderDetailsId;
    public int orderDetailsProfileId;
    public String orderDetailsProfileUserName;
    public String orderDetailsProfileEmail;
    public String orderDetailsProfilePhoneNumber;
    public String orderDetailsProfileFirstName;
    public String orderDetailsProfileLastName;
    public int orderDetailsProfileBalance;
    public Boolean orderDetailsProfileIsBlocked;
    public Boolean orderDetailsProfileIsRegistered;
    public int orderDetailsNoOfMinutes;
    public int orderDetailsTotal;
    public String orderDetailsProfileStatus;



    private static final String orderDetailsId_OBJECT = "Id";

    private static final String orderDetailsProfile_OBJECT = "Profile";
    private static final String orderDetailsProfileId_OBJECT = "Id";
    private static final String orderDetailsProfileUserName_OBJECT = "UserName";
    private static final String orderDetailsProfileEmail_OBJECT = "Email";
    private static final String orderDetailsProfilePhoneNumber_OBJECT = "PhoneNumber";
    private static final String orderDetailsProfileFirstName_OBJECT = "FirstName";
    private static final String orderDetailsProfileLastName_OBJECT = "LastName";
    private static final String orderDetailsProfileBalance_OBJECT = "Balance";
    private static final String orderDetailsProfileIsBlocked_OBJECT = "IsBlocked";
    private static final String orderDetailsProfileIsIsRegistered_OBJECT = "IsRegistered";

    private static final String orderDetailsNoOfMinutes_OBJECT = "NoOfMinutes";
    private static final String orderDetailsTotal_OBJECT = "Total";
    private static final String orderDetailsStatus_OBJECT = "Status";

    public orderDetailsObject(int orderDetailsId, int orderDetailsProfileId, String orderDetailsProfileUserName, String orderDetailsProfileEmail, String orderDetailsProfilePhoneNumber, String orderDetailsProfileFirstName, String orderDetailsProfileLastName, int orderDetailsProfileBalance, Boolean orderDetailsProfileIsBlocked, Boolean orderDetailsProfileIsRegistered, int orderDetailsNoOfMinutes, int orderDetailsTotal, String orderDetailsProfileStatus) {
        this.orderDetailsId = orderDetailsId;
        this.orderDetailsProfileId = orderDetailsProfileId;
        this.orderDetailsProfileUserName = orderDetailsProfileUserName;
        this.orderDetailsProfileEmail = orderDetailsProfileEmail;
        this.orderDetailsProfilePhoneNumber = orderDetailsProfilePhoneNumber;
        this.orderDetailsProfileFirstName = orderDetailsProfileFirstName;
        this.orderDetailsProfileLastName = orderDetailsProfileLastName;
        this.orderDetailsProfileBalance = orderDetailsProfileBalance;
        this.orderDetailsProfileIsBlocked = orderDetailsProfileIsBlocked;
        this.orderDetailsProfileIsRegistered = orderDetailsProfileIsRegistered;
        this.orderDetailsNoOfMinutes = orderDetailsNoOfMinutes;
        this.orderDetailsTotal = orderDetailsTotal;
        this.orderDetailsProfileStatus = orderDetailsProfileStatus;
    }

    public orderDetailsObject(String response) {
        try {
            JSONObject JsonObject = new JSONObject(response);

            this.orderDetailsId = JsonObject.getInt(orderDetailsId_OBJECT);
            this.orderDetailsNoOfMinutes = JsonObject.getInt(orderDetailsNoOfMinutes_OBJECT);
            this.orderDetailsTotal = JsonObject.getInt(orderDetailsTotal_OBJECT);
            this.orderDetailsProfileStatus = JsonObject.getString(orderDetailsStatus_OBJECT);

            JSONObject profileObject = JsonObject.getJSONObject(orderDetailsProfile_OBJECT);
            this.orderDetailsProfileId = profileObject.getInt(orderDetailsProfileId_OBJECT);
            this.orderDetailsProfileUserName = profileObject.getString(orderDetailsProfileUserName_OBJECT);
            this.orderDetailsProfileEmail = profileObject.getString(orderDetailsProfileEmail_OBJECT);
            this.orderDetailsProfilePhoneNumber = profileObject.getString(orderDetailsProfilePhoneNumber_OBJECT);
            this.orderDetailsProfileFirstName = profileObject.getString(orderDetailsProfileFirstName_OBJECT);
            this.orderDetailsProfileLastName = profileObject.getString(orderDetailsProfileLastName_OBJECT);
            this.orderDetailsProfileBalance = profileObject.getInt(orderDetailsProfileBalance_OBJECT);
            this.orderDetailsProfileIsBlocked = profileObject.getBoolean(orderDetailsProfileIsBlocked_OBJECT);
            this.orderDetailsProfileIsRegistered = profileObject.getBoolean(orderDetailsProfileIsIsRegistered_OBJECT);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
