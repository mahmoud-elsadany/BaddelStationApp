package baddel.baddelstationapp.internalStorage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import baddel.baddelstationapp.Models.Advert_DS;
import baddel.baddelstationapp.Models.trip_DS;

/**
 * Created by mahmo on 2017-06-08.
 */

public class Session {
    //random constant values
    private int waitingTime = 60000;
    private int chosenPeriodTime = 0;
    private String messageToken = "";
    private int numberOfAvailableBikes;
    private boolean isTCPConnection = true;
    private int numberOfChosenBikes;
    private Boolean cancelTrip = false;
    private ArrayList<trip_DS> currentTripArrayListObject;
    private ArrayList<Advert_DS> stationAdvertsList;
    private Boolean tcpDown = false;
    private Boolean sending = false;
    private int tcpInterval = 3000;
    private Boolean finished = false;
    private String KioskPassword = "300594";
    private String androidId;
    private int responseCode;

    private Boolean internetAvailability;

    public String getKioskPassword() {
        return KioskPassword;
    }

    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    //webSocket constant Values
    //private String webSocketBaseUrl = "http://104.197.104.190:8081/realtime";
    private String webSocketBaseUrl = "http://dev.api.baddelonline.com/realtime";
    //private String webSocketBaseUrl = "http://192.168.1.12:8888/realtime";
    //private String webSocketBaseUrl = "http://api.baddelonline.com/realtime";
    private String webSocketStartTripOnMethod = "startTrip";
    private String webSocketUpdateAppMethod = "updateApp";
    private String webSocketHub = "stationsHub";

    //HTTP API constant Values
//    private String webServicesBaseUrl = "http://104.197.104.190:8081/api/";
    private String webServicesBaseUrl = "http://dev.api.baddelonline.com/api/";
    //private String webServicesBaseUrl = "http://192.168.1.6:28824";
    //private String webServicesBaseUrl = "http://api.baddelonline.com/api/";
    //private String webServicesBaseUrl = "http://staging.api.baddelonline.com/api/";
    private String tokenUserName = "station";
    private String tokenPassword = "Baddel@123";

    private String APIMETHODGetStationSystemSettings = "system-settings";
    private String APIMETHODGetStationIDByIMEI = "stations/by-imei/";
    private String APIMETHODPostAppVersion = "stations/set-app-version";
    private String APIMETHODPostRequestTrip = "trips/request";
    private String APIMETHODPutStartTrip = "trips/start";
    private String APIMETHODPutRefreshToken = "trips/refresh-token";
    private String APIMETHODPutFinishTrip = "trips/finish";
    private String APIMETHODGetStationDetails = "stations/";
    private String APIMETHODPutConfirmUser = "trips/confirm-user";
    private String APIMETHODPutSetOrder = "trips/set-order";
    private String APIMETHODPostCancelTrip = "trips/cancel-trip";

    private Queue<String> startTripsOfflineObjects = new LinkedList<>();
    private Queue<String> finishTripsOfflineObjects = new LinkedList<>();

    //TCPSocket constant Values
    private String tcpSocketIP = "192.168.1.2";
    private int tcpSocketPORT = 5001;
//    private String tcpSocketIP = "192.168.1.100";
//    private int tcpSocketPORT = 5001;
    private String messageResponse;

    //payment values
    private String payFortUrl = "https://checkout.payfort.com/FortAPI/paymentPage";
    private String APIMETHODPostPaymentOrder = "payment/order";

    private String OrderId;

    private String savingLogs = "";

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

    public void setOrderId(String orderId) {
        OrderId = orderId;
    }

    public String getOrderId() {
        return OrderId;
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

    public ArrayList<trip_DS> getCurrentTripArrayListObjects() {
        return currentTripArrayListObject;
    }

    public void setCurrentTripArrayListObject(ArrayList<trip_DS> currentTripArrayListObject) {
        this.currentTripArrayListObject = currentTripArrayListObject;
    }

    public String getAPIMETHODPutConfirmUser() {
        return APIMETHODPutConfirmUser;
    }

    public String getAPIMETHODPutSetOrder() {
        return APIMETHODPutSetOrder;
    }

    public String getAPIMETHODPutRefreshToken() {
        return APIMETHODPutRefreshToken;
    }

    public ArrayList<Advert_DS> getStationAdvertsList() {
        return stationAdvertsList;
    }

    public void setStationAdvertsList(ArrayList<Advert_DS> stationAdvertsList) {
        this.stationAdvertsList = stationAdvertsList;
    }

    public String getAPIMETHODPostCancelTrip() {
        return APIMETHODPostCancelTrip;
    }

    public Boolean getCancelTrip() {
        return cancelTrip;
    }

    public void setCancelTrip(Boolean cancelTrip) {
        this.cancelTrip = cancelTrip;
    }

    public Boolean getTcpDown() {
        return tcpDown;
    }

    public void setTcpDown(Boolean tcpDown) {
        this.tcpDown = tcpDown;
    }

    public Boolean getSending() {
        return sending;
    }

    public void setSending(Boolean sending) {
        this.sending = sending;
    }

    public int getTcpInterval() {
        return tcpInterval;
    }

    public void setTcpInterval(int tcpInterval) {
        this.tcpInterval = tcpInterval;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public String getSavingLogs() {
        return savingLogs;
    }

    public void setSavingLogs(String savingLogs) {
        this.savingLogs = savingLogs;
    }

    public Boolean getInternetAvailability() {
        return internetAvailability;
    }

    public void setInternetAvailability(Boolean internetAvailability) {
        this.internetAvailability = internetAvailability;
    }

    public Queue<String> getStartTripsOfflineObject() {
        return startTripsOfflineObjects;
    }

    public void setStartTripsOfflineObject(String startTripsOfflineObject) {
        this.startTripsOfflineObjects.add(startTripsOfflineObject);
    }

    public Queue<String> getFinishTripsOfflineObject() {
        return finishTripsOfflineObjects;
    }

    public void setFinishTripsOfflineObject(String finishTripsOfflineObject) {
        this.finishTripsOfflineObjects.add(finishTripsOfflineObject);
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getAPIMETHODGetStationSystemSettings() {
        return APIMETHODGetStationSystemSettings;
    }

    public void setAPIMETHODGetStationSystemSettings(String APIMETHODGetStationSystemSettings) {
        this.APIMETHODGetStationSystemSettings = APIMETHODGetStationSystemSettings;
    }
}

