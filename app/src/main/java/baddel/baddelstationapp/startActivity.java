package baddel.baddelstationapp;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Random;

import baddel.baddelstationapp.Controller.callController;
import baddel.baddelstationapp.Models.Station_DS;
import baddel.baddelstationapp.connectToServer.myAsyncTask;
import baddel.baddelstationapp.connectToServer.responseDelegate;
import baddel.baddelstationapp.customViews.customDialogs;
import baddel.baddelstationapp.internalStorage.SQliteDB;
import baddel.baddelstationapp.internalStorage.Session;
import pl.droidsonroids.gif.GifDrawable;

public class startActivity extends AppCompatActivity implements responseDelegate {

    //UI references
    /*@BindView(R.id.rentBicycleBT)
    Button rentBicycleBT;*/

    //UI references
    private Button rentBicycleBT,helpBT,downloadBT;
    private SliderLayout imageSlider;

    private HashMap<String,Integer> file_maps;


    //controller Class
    private callController callController;
//    private Controller Controller;
//    private boolean mBound = false;

    //HTTP asyncTask
    private myAsyncTask asyncTask;

    //sqlite database Object
    private SQliteDB sQliteDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sQliteDB = new SQliteDB(startActivity.this);

        setBaddelGifLogo();

        Session.getInstance().setChosenPeriodTime(0);

        rentBicycleBT = (Button)findViewById(R.id.rentBicycleBT);
        imageSlider = (SliderLayout)findViewById(R.id.imageSlider);

        setImageSlider();

        setRentBicycleBT();

        if (sQliteDB.numberOfStations()>0) {
            callController = new callController(startActivity.this);

//            if (!Session.getInstance().isTCPConnection())
//                customDialogs.ShowConnectionExceptionDialog(startActivity.this);

        }else{
            getStationID();
        }

    }

    private void setBaddelGifLogo(){
        try {
            GifDrawable gifFromResource = new GifDrawable( getResources(), R.drawable.baddelgif);
            gifFromResource.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String GenerateRandomNumber(int charLength) {
        return String.valueOf(charLength < 1 ? 0 : new Random()
                .nextInt((9 * (int) Math.pow(10, charLength - 1)) - 1)
                + (int) Math.pow(10, charLength - 1));
    }

    private void setImageSlider(){
        file_maps = new HashMap<String, Integer>();
        file_maps.put("cib",R.mipmap.cib);
        file_maps.put("vodafone",R.mipmap.vodafone);
        file_maps.put("pepsi",R.mipmap.pepsi);

        for(String name : file_maps.keySet()){
            TextSliderView textSliderView = new TextSliderView(this);
            textSliderView
                    .description(name)
                    .image(file_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.FitCenterCrop);

            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra",name);

            imageSlider.addSlider(textSliderView);
        }

        //tablet
        imageSlider.setPresetTransformer(SliderLayout.Transformer.Default);
        imageSlider.setSliderTransformDuration(4000,null);
        imageSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Top);
        imageSlider.setCustomAnimation(new DescriptionAnimation());
        imageSlider.setDuration(8000);
    }

    private void setRentBicycleBT(){
        rentBicycleBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getStationDetails();
            }
        });
    }

    @Override
    protected void onStop() {
        // Unbind from the service
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // Unbind from the service
        callController.unBindController();
        super.onDestroy();
    }

    private void getStationDetails(){
        //getRequest 2
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getAPIMETHODGetStationDetails();
        int myProcessNum = 3;

        HashMap<String, String> data = new HashMap<>();
        data.put("id",sQliteDB.getStationID());

        String URL = myURL+apiMethod;

        if (isNetworkConnected()){

            asyncTask = new myAsyncTask(startActivity.this,data,URL,myProcessNum, Session.getInstance().getTokenUserName(),Session.getInstance().getTokenPassword(),null,2);

            asyncTask.delegate = this;

            //asyncTask.execute();
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else{
            showToast("No Internet Connection");
        }


    }
    private void getStationID() {
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getAPIMETHODGetStationIDByIMEI();
        String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        int myProcessNum = 1;

        HashMap<String, String> data = new HashMap<>();
        data.put("imei",android_id);

        String URL = myURL+apiMethod;

        if (isNetworkConnected()){

            asyncTask = new myAsyncTask(startActivity.this,data,URL,myProcessNum, Session.getInstance().getTokenUserName(),Session.getInstance().getTokenPassword(),null,2);

            asyncTask.delegate = this;

            //asyncTask.execute();
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else{
            showToast("No Internet Connection");
        }
    }
    private void sendAppStationVersion(String stationID) {
        String myURL = Session.getInstance().getWebServicesBaseUrl();
        String apiMethod = Session.getInstance().getWebSocketUpdateAppMethod();
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo.versionName;

        int myProcessNum = 2;

        HashMap<String, String> data = new HashMap<>();
        data.put("id",stationID);
        data.put("appVersion",version);

        String URL = myURL+apiMethod;

        if (isNetworkConnected()){

            asyncTask = new myAsyncTask(startActivity.this,data,URL,myProcessNum, Session.getInstance().getTokenUserName(),Session.getInstance().getTokenPassword(),null,1);


            asyncTask.delegate = this;

            //asyncTask.execute();
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else{
            showToast("No Internet Connection");
        }
    }
    private void showToast(String msg) {
        Toast.makeText(startActivity.this,msg,Toast.LENGTH_LONG).show();
    }

    //check the connection
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
            return !ipAddr.equals("");
        } catch (Exception e) {
            return false;
        }

    }

    public boolean isTcpAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("192.168.8.100"); //You can replace it with your name
            return !ipAddr.equals("");
        } catch (Exception e) {
            return false;
        }

    }

    @Override
    public void getServerResponse(String response, int ProcessNum) {
        switch (ProcessNum){
            case 1:
                //getStationID by byimei
                Log.d("getStationID",response);
                Station_DS station_ds = new Station_DS(response);

                if (sQliteDB.numberOfStations()>0) {
                    sQliteDB.deleteAllStationRows();
                    sQliteDB.insertStationID(String.valueOf(station_ds.stationID));
                }else{
                    sQliteDB.insertStationID(String.valueOf(station_ds.stationID));
                }

                callController = new callController(startActivity.this);
                sendAppStationVersion(String.valueOf(station_ds.stationID));

//                if (!Session.getInstance().isTCPConnection())
//                    customDialogs.ShowConnectionExceptionDialog(startActivity.this);

                //startService();

                break;
            case 2:
                //sendAPKVersion
                Log.d("sendAPKversion",response);
                break;
            case 3:
                //get Station Details
                Log.d("getStationDetails",response);

                Station_DS stationDs = new Station_DS(response);

                Session.getInstance().setNumberOfAvailableBikes(stationDs.stationNumberOfAvailableBikes);

                startActivity(new Intent(startActivity.this,chooseRentTimeActivity.class));

                break;

            default:
                break;
        }
    }
}
