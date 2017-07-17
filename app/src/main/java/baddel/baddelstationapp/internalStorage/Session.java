package baddel.baddelstationapp.internalStorage;

import baddel.baddelstationapp.Models.trip_DS;

/**
 * Created by mahmo on 2017-06-08.
 */

public class Session {

    //random constant values
    private int waitingTime = 30000;
    private int chosenPeriodTime = 0;
    private String messageToken = "";
    private int numberOfAvailableBikes;
    private boolean isTCPConnection = true;
    private int numberOfChosenBikes;
    private trip_DS currentTripObject;

    //webSocket constant Values
    private String webSocketBaseUrl = "http://104.197.104.190:8081/realtime";
    private String webSocketStartTripOnMethod = "startTrip";
    private String webSocketUpdateAppMethod = "updateApp";
    private String webSocketHub = "stationsHub";

    //HTTP API constant Values
    private String webServicesBaseUrl = "http://104.197.104.190:8081/api/";
    //private String webServicesBaseUrl = "http://dev.api.baddelonline.com/api/";
    //private String webServicesBaseUrl = "http://staging.api.baddelonline.com/api/";
    private String tokenUserName = "station";
    private String tokenPassword = "Baddel@123";
    private String APIMETHODGetStationIDByIMEI = "stations/by-imei/";
    private String APIMETHODPostAppVersion = "stations/set-app-version";
    private String APIMETHODPostRequestTrip = "trips/request";
    private String APIMETHODPutStartTrip = "trips/start";
    private String APIMETHODPutFinishTrip = "trips/finish";
    private String APIMETHODGetStationDetails = "stations/";
    private String APIMETHODPutConfirmUser = "trips/confirm-user";
    private String APIMETHODPutSetOrder = "trips/set-order";


    //TCPSocket constant Values
    private String tcpSocketIP = "192.168.1.101";
    private int tcpSocketPORT = 5001;
//    private String tcpSocketIP = "192.168.1.100";
//    private int tcpSocketPORT = 5001;
    private String messageResponse;


    //payment values
    private String payFortUrl = "https://checkout.payfort.com/FortAPI/paymentPage";
    private String APIMETHODPostPaymentOrder = "payment/order";

    public int getOrderId() {
        return OrderId;
    }

    private int OrderId;


    private static Session instance;

    private Session() {
    }

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public int getChosenPeriodTime() {
        return chosenPeriodTime;
    }

    public void setChosenPeriodTime(int chosenPeriodTime) {
        this.chosenPeriodTime = chosenPeriodTime;
    }

    public String getWebSocketBaseUrl() {
        return webSocketBaseUrl;
    }

    public String getWebSocketStartTripOnMethod() {
        return webSocketStartTripOnMethod;
    }

    public String getWebSocketHub() {
        return webSocketHub;
    }

    public String getWebServicesBaseUrl() {
        return webServicesBaseUrl;
    }

    public String getTcpSocketIP() {
        return tcpSocketIP;
    }

    public int getTcpSocketPORT() {
        return tcpSocketPORT;
    }

    public String getTokenUserName() {
        return tokenUserName;
    }

    public String getTokenPassword() {
        return tokenPassword;
    }

    public String getAPIMETHODGetStationIDByIMEI() {
        return APIMETHODGetStationIDByIMEI;
    }

    public String getWebSocketUpdateAppMethod() {
        return webSocketUpdateAppMethod;
    }

    public String getAPIMETHODPostAppVersion() {
        return APIMETHODPostAppVersion;
    }

    public String getAPIMETHODPutStartTrip() {
        return APIMETHODPutStartTrip;
    }

    public String getAPIMETHODPutFinishTrip() {
        return APIMETHODPutFinishTrip;
    }

    public String getMessageResponse() {
        return messageResponse;
    }

    public void setMessageResponse(String messageResponse) {
        this.messageResponse = messageResponse;
    }

    public String getAPIMETHODPostPaymentOrder() {
        return APIMETHODPostPaymentOrder;
    }

    public void setOrderId(int orderId) {
        OrderId = orderId;
    }

    public String getPayFortUrl() {
        return payFortUrl;
    }

    public String getAPIMETHODPostRequestTrip() {
        return APIMETHODPostRequestTrip;
    }

    public String getMessageToken() {
        return messageToken;
    }

    public void setMessageToken(String messageToken) {
        this.messageToken = messageToken;
    }

    public String getAPIMETHODGetStationDetails() {
        return APIMETHODGetStationDetails;
    }

    public int getNumberOfAvailableBikes() {
        return numberOfAvailableBikes;
    }

    public void setNumberOfAvailableBikes(int numberOfAvailableBikes) {
        this.numberOfAvailableBikes = numberOfAvailableBikes;
    }

    public int getNumberOfChosenBikes() {
        return numberOfChosenBikes;
    }

    public void setNumberOfChosenBikes(int numberOfChosenBikes) {
        this.numberOfChosenBikes = numberOfChosenBikes;
    }

    public boolean isTCPConnection() {
        return isTCPConnection;
    }

    public void setTCPConnection(boolean TCPConnection) {
        isTCPConnection = TCPConnection;
    }

    public trip_DS getCurrentTripObject() {
        return currentTripObject;
    }

    public void setCurrentTripObject(trip_DS currentTripObject) {
        this.currentTripObject = currentTripObject;
    }

    public String getAPIMETHODPutConfirmUser() {
        return APIMETHODPutConfirmUser;
    }

    public String getAPIMETHODPutSetOrder() {
        return APIMETHODPutSetOrder;
    }
}

