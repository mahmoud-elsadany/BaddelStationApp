package baddel.baddelstationapp;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import baddel.baddelstationapp.ClientTCPSocketing.TCPClient;
import baddel.baddelstationapp.ClientTCPSocketing.TCPcheck;
import baddel.baddelstationapp.Controller.Controller;
import baddel.baddelstationapp.Models.Advert_DS;
import baddel.baddelstationapp.Models.Station_DS;
import baddel.baddelstationapp.Models.systemSettingDS;
import baddel.baddelstationapp.Models.trip_DS;
import baddel.baddelstationapp.connectToServer.internetCheck;
import baddel.baddelstationapp.connectToServer.myAsyncTask;
import baddel.baddelstationapp.connectToServer.responseDelegate;
import baddel.baddelstationapp.customViews.customDialogs;
import baddel.baddelstationapp.customViews.customViewGroup;
import baddel.baddelstationapp.internalStorage.SQliteDB;
import baddel.baddelstationapp.internalStorage.Session;
import baddel.baddelstationapp.saveLogs.myLogs;
import baddel.baddelstationapp.saveLogs.timeBroadCast;

import static android.R.attr.data;

@SuppressWarnings("WrongConstant")
public class startActivity extends AppCompatActivity implements responseDelegate {

    private static final String startActivityTag = "startActivtyTag";

    //UI references
    private Button rentBicycleBT, helpBT, downloadBT;
    private TextView supportNumberTV;
    private ImageView logoImageView;
    private SliderLayout imageSlider;
    private Typeface font;

    //controller Class
//    private callController callController;

    //HTTP asyncTask
    private myAsyncTask asyncTask;

    //sqlite database Object
    private SQliteDB sQliteDB;

    private int count = 0;

    private Boolean isRentBicycle;

    private Handler mHandler = new Handler();

    private ArrayList<trip_DS> initiateArrayList = new ArrayList<>();

    private Controller Controller;
    private boolean mBound = false;

    private Station_DS station_ds;

    private static BroadcastReceiver tickReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();
        disableStatusBar();
        setContentView(R.layout.activity_start);

        startService(new Intent(startActivity.this, internetCheck.class));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkConnectivity();
            }
        },4000);

        timeTicking();

    }

    private void startingActivity(){
        count = 0;
        isRentBicycle = false;
        Session.getInstance().setCurrentTripArrayListObject(initiateArrayList);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //disableStatusBar();

        String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        Session.getInstance().setAndroidId(android_id);

        sQliteDB = new SQliteDB(startActivity.this);

        Session.getInstance().setChosenPeriodTime(0);

        supportNumberTV = (TextView)findViewById(R.id.textView6SupportNumber);
        rentBicycleBT = (Button) findViewById(R.id.rentBicycleBT);
        imageSlider = (SliderLayout) findViewById(R.id.imageSlider);

        setFontType();

        setRentBicycleBT();


        if (sQliteDB.numberOfStations() > 0) {
            callController(startActivity.this);
            //callController = new callController(startActivity.this);
            getStationDetails();
            sendAppStationVersion(sQliteDB.getStationID());
        } else {
            getStationID();
        }

        //startService(new Intent(startActivity.this, TCPcheck.class));
        //startService(new Intent(startActivity.this, TCPClient.class));

        setLogoImageView();

        getBundle();
    }

    private void timeTicking() {
        tickReceiver = new timeBroadCast();
        registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    private void getBundle() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getBoolean("showStartTripDialog")) {
                ArrayList<trip_DS> trips = Session.getInstance().getCurrentTripArrayListObjects();

                String reservedSlots = "";

                for (trip_DS tripObj : trips) {
                    reservedSlots += tripObj.startSlotNumber + ", ";
                }
                final Dialog showReservedBikesDialog = customDialogs.ShowDialogAfterStartTrip(startActivity.this, reservedSlots);
                showReservedBikesDialog.show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showReservedBikesDialog.cancel();
                    }
                }, 5000);
            } else if (bundle.getBoolean("EXITKIOSK")) {
                startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                finish();
                finishAffinity();
            }
        }
    }

    private void setLogoImageView() {
        final Dialog slotsDialog = customDialogs.ShowExitToSettingDialog(startActivity.this);
        logoImageView = (ImageView) findViewById(R.id.imageView);
        logoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                if (count == 6) {
                    slotsDialog.show();
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            slotsDialog.cancel();
                        }
                    }, 20000);
                    count = 0;
                }
            }
        });
    }

    private void disableStatusBar() {
        WindowManager manager = ((WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE));

        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        localLayoutParams.gravity = Gravity.TOP;
        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |

                // this is to enable the notification to recieve touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |

                // Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        localLayoutParams.height = (int) (50 * getResources()
                .getDisplayMetrics().scaledDensity);
        localLayoutParams.format = PixelFormat.TRANSPARENT;


        customViewGroup view = new customViewGroup(this);

        manager.addView(view, localLayoutParams);
    }

    private void hideSystemUI() {

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void setFontType() {
        font = Typeface.createFromAsset(getAssets(), "robotoregular.ttf");
        rentBicycleBT.setTypeface(font);
    }

    private void setImageSlider() {

        imageSlider.removeAllSliders();
        HashMap<String, String> file_maps = new HashMap<>();

        ArrayList<Advert_DS> stationAdverts = Session.getInstance().getStationAdvertsList();
        for (Advert_DS advertObj : stationAdverts) {
            file_maps.put(advertObj.AdName, advertObj.AdContent);
        }

        for (String name : file_maps.keySet()) {
            TextSliderView textSliderView = new TextSliderView(this);
            textSliderView
                    .description(name)
                    .image(file_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit);

            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra", name);

            imageSlider.addSlider(textSliderView);
        }

        //tablet
        imageSlider.setPresetTransformer(SliderLayout.Transformer.Default);
        imageSlider.setSliderTransformDuration(4000, null);
        imageSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Top);
        imageSlider.setCustomAnimation(new DescriptionAnimation());
        imageSlider.setDuration(8000);
    }

    private void setRentBicycleBT() {
        rentBicycleBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Session.getInstance().setCurrentTripArrayListObject(null);
                getSystemSettings();
                isRentBicycle = true;
                getStationDetails();
            }
        });
    }

    @Override
    protected void onStop() {
        // Unbind from the service
        count = 0;
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    @Override
    protected void onDestroy() {
        // Unbind from the service
        myLogs.logMyLog("appDestroyed", "Stop services");
        //Log.d("appDestroyed","Stop services");
        stopService(new Intent(startActivity.this, TCPClient.class));
        stopService(new Intent(startActivity.this, TCPcheck.class));
        //callController.unBindController();
        unbindService(mConnection);
        super.onDestroy();
    }

    private void checkConnectivity(){
        if (!isNetworkConnected()){
            Dialog TCPExceptionDialog = customDialogs.ShowConnectionExceptionDialog(getApplicationContext());
            TCPExceptionDialog.show();
        }
//        else if (isNetworkConnected()&&Session.getInstance().getInternetAvailability()){
//            startingActivity();
//        }
        else{
            startingActivity();
        }
    }

    private void getSystemSettings(){
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getAPIMETHODGetStationSystemSettings();
        int myProcessNum = 4;

        HashMap<String, String> data = null;

        String URL = myURL + apiMethod;

        if (isNetworkConnected()) {

            asyncTask = new myAsyncTask(startActivity.this, data, URL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 2);

            asyncTask.delegate = this;

            //asyncTask.execute();
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            showToast("No Internet Connection");
        }

    }

    private void getStationDetails() {
        //getRequest 2
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getAPIMETHODGetStationDetails();
        int myProcessNum = 3;

        HashMap<String, String> data = new HashMap<>();
        data.put("id", sQliteDB.getStationID());

        String URL = myURL + apiMethod;

        if (isNetworkConnected()) {

            asyncTask = new myAsyncTask(startActivity.this, data, URL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 2);

            asyncTask.delegate = this;

            //asyncTask.execute();
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            showToast("No Internet Connection");
        }


    }

    private void getStationID() {
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getAPIMETHODGetStationIDByIMEI();
        String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        int myProcessNum = 1;

        HashMap<String, String> data = new HashMap<>();
        data.put("imei", android_id);

        myLogs.logMyLog("config", android_id);
        //Log.d("config", android_id);

        String URL = myURL + apiMethod;

        if (isNetworkConnected()) {

            asyncTask = new myAsyncTask(startActivity.this, data, URL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 2);

            asyncTask.delegate = this;

            //asyncTask.execute();
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            showToast("No Internet Connection");
        }
    }

    private void sendAppStationVersion(String stationID) {
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getAPIMETHODPostAppVersion();
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo.versionName;

        int myProcessNum = 2;

        JSONObject appVersionJson = new JSONObject();
        try {
            appVersionJson.put("id", stationID);
            appVersionJson.put("appVersion", version);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HashMap<String, String> data = new HashMap<>();
        data.put("stationdata", appVersionJson.toString());

        myLogs.logMyLog("sendApkVerParameters", appVersionJson.toString());
        //Log.d("sendApkVerParameters",appVersionJson.toString());

        String URL = myURL + apiMethod;

        if (isNetworkConnected()) {

            asyncTask = new myAsyncTask(startActivity.this, data, URL, myProcessNum, Session.getInstance().getTokenUserName(), Session.getInstance().getTokenPassword(), null, 1);

            asyncTask.delegate = this;

            //asyncTask.execute();
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            showToast("No Internet Connection");
        }
    }

    private void showToast(String msg) {
        Toast.makeText(startActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    //check the connection
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void getServerResponse(String response, int ProcessNum) {
        switch (ProcessNum) {
            case 1:
                //getStationID by byimei
                if (Session.getInstance().getResponseCode() == 200) {
                    myLogs.logMyLog("getStationID", response);
                    station_ds = new Station_DS(response);

                    if (sQliteDB.numberOfStations() > 0) {
                        sQliteDB.deleteAllStationRows();
                        sQliteDB.insertStationID(String.valueOf(station_ds.stationID));
                    } else {
                        sQliteDB.insertStationID(String.valueOf(station_ds.stationID));
                    }

                    callController(startActivity.this);
                    //callController = new callController(startActivity.this);
                    sendAppStationVersion(String.valueOf(station_ds.stationID));

                    getStationDetails();

                    //startService();
                }else{
                    getStationID();
                }

                break;
            case 2:
                //sendAPKVersion
                if (Session.getInstance().getResponseCode() == 200) {
                    myLogs.logMyLog("sendAPKversion", response);
                } else {
                    if (sQliteDB.numberOfStations() > 0) {
                        sQliteDB.deleteAllStationRows();
                        sQliteDB.insertStationID(String.valueOf(station_ds.stationID));
                    } else {
                        sQliteDB.insertStationID(String.valueOf(station_ds.stationID));
                    }
                    sendAppStationVersion(String.valueOf(station_ds.stationID));
                }
                //Log.d("sendAPKversion", response);
                break;
            case 3:
                //get Station Details
                myLogs.logMyLog("getStationDetails", response);
                //Log.d("getStationDetails", response);
                if (Session.getInstance().getResponseCode() == 200) {
                    Station_DS stationDs = new Station_DS(response);

                    Session.getInstance().setNumberOfAvailableBikes(stationDs.stationNumberOfAvailableBikes);

                    //Session.getInstance().setNumberOfAvailableBikes(0);

                    Session.getInstance().setStationAdvertsList(stationDs.stationAdvertDS_ArrayList());

                    if (isRentBicycle && Session.getInstance().getNumberOfAvailableBikes() != 0)
                        startActivity(new Intent(startActivity.this, chooseRentTimeActivity.class));
                    else if (isRentBicycle && Session.getInstance().getNumberOfAvailableBikes() == 0) {
                        final Dialog noAvailableBikesDialog = customDialogs.ShowWarningMessage(getApplicationContext(), "Sorry No available bikes at the time \n please come back later");
                        noAvailableBikesDialog.show();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                noAvailableBikesDialog.cancel();
                            }
                        }, 5000);

                    } else {
                        setImageSlider();

                    }
                } else {
                    getStationDetails();
                }


                break;

            case 4:
                //getSystemSettings
                myLogs.logMyLog("systemSettings",response);
                systemSettingDS systemSettingDS = new systemSettingDS(response);

                supportNumberTV.setText(systemSettingDS.currentSupportNumberSystemSettings());

                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {

    }

    private void callController(Context myContext) {
        Intent intent = new Intent();
        intent.setClass(myContext, Controller.class);
        myContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Controller = new Controller(myContext);
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Controller.tripControllerBinder binder = (Controller.tripControllerBinder) service;
            Controller = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


}
