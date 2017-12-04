package nazarko.inveritasoft.com.my_client;

import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

/**
 * Created by nazarko on 30.11.17.
 */

public class DefaultSdpObserver implements SdpObserver {
    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.e("TAG","failed to create offer:"+sessionDescription);
    }

    @Override
    public void onSetSuccess() {

    }

    @Override
    public void onCreateFailure(String s) {
        Log.e("TAG","create failure:"+s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.e("TAG","set failure:"+s);
    }
}
