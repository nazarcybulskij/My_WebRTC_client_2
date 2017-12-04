package nazarko.inveritasoft.com.my_client;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;


/**
 * Created by nazarko on 24.11.17.
 */

public class MyClientApplication extends Application {

    private Socket mSocket;
    private String UUID = "";


    public Socket getSocket() {
        return mSocket;
    }

    public String  UUID() {
        return UUID;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            UUID = getUniqueID();
            mSocket = IO.socket(Constants.SERVER_URL+"/?"+"userid="+UUID);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUniqueID(){
        String myAndroidDeviceId = "";
        TelephonyManager mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (mTelephony.getDeviceId() != null){
            myAndroidDeviceId = mTelephony.getDeviceId();
        }else{
            myAndroidDeviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return myAndroidDeviceId;
    }

}
