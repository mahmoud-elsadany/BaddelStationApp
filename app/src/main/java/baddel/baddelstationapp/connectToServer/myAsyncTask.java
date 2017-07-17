package baddel.baddelstationapp.connectToServer;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;

import baddel.baddelstationapp.customViews.customDialogs;

/**
 * Created by mahmoud on 2016-11-28.
 */
public class myAsyncTask extends AsyncTask<String, Void, String> {
    private HashMap<String, String> data;
    private JSONObject putJsonObject;
    private Context myContext;
    private String URL;
    private String userNameToken,passwordToken;
    private Boolean DialogExist = false;
    private Dialog loadingDialog;
    private int ProcessNum;
    private int postORget;

    private postClass rpc = new postClass();
    private getClass rgc = new getClass();
    private putClass rputc = new putClass();
    /**
     * The Delegate.
     */
    public responseDelegate delegate = null;

    /**
     * Instantiates a new My async task.
     *
     * @param context  the context
     * @param dataSend the data send
     * @param myURL    the my url
     */
    public myAsyncTask(Context context, HashMap<String, String> dataSend, String myURL,int ProcessNum, String userNameToken,String passwordToken,JSONObject putJsonObject,int postORget){
        this.myContext = context;

        Log.d("baddelContext",myContext.toString());
        if (myContext.toString().contains("verifyMobileNumberActivity")||myContext.toString().contains("startActivity")||myContext.toString().contains("creditCardDataActivity")||myContext.toString().contains("enterPhoneNumberActivity")){
            DialogExist = true;
        }
        this.data = dataSend;
        this.URL = myURL;
        this.userNameToken = userNameToken;
        this.passwordToken = passwordToken;
        this.ProcessNum = ProcessNum;
        this.postORget = postORget;
        this.putJsonObject = putJsonObject;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (DialogExist) {
            loadingDialog = new customDialogs().loadingDialog(myContext);
            loadingDialog.show();
        }
    }

    @Override
    protected String doInBackground(String... params) {
        String result;
        if (postORget == 1){//post request
            result = rpc.sendPostRequest(userNameToken,passwordToken,URL,data);
        }else if (postORget == 2){//get request
            result = rgc.getJSON(userNameToken,passwordToken,URL,data);
        }
        else if (postORget == 99){//download And Install apk
            //rgc.getAPK(URL,myContext);
            return "Done Download";
        }
        else{//put request
            result = rputc.sendPutRequest(userNameToken,passwordToken,URL,data);
        }

        return  result;
    }

    @Override
    protected void onPostExecute(String result){
        String s = result.trim();

        if (DialogExist)
            loadingDialog.dismiss();

        delegate.getServerResponse(s,ProcessNum);

    }

}