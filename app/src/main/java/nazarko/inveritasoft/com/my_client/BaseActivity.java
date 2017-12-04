package nazarko.inveritasoft.com.my_client;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;

import static nazarko.inveritasoft.com.my_client.Constants.SDP;

/**
 * Created by nazarko on 30.11.17.
 */

public class BaseActivity extends AppCompatActivity {

    private PeerConnectionFactory peerConnectionFactory;
    private MediaConstraints audioConstraints;
    private AudioSource audioSource;
    private AudioTrack localAudioTrack;
    private MediaConstraints sdpConstraints;


    public PeerConnection localPeer;



    private DataChannel channel;
    private boolean pcInitialized = false;

    public Socket mSocket;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        start();
//        call();
    }

    @Override
    protected void onDestroy() {
        channel.close();
        if(pcInitialized){
            localPeer.close();
        }
        super.onDestroy();
    }


    public   void start(){
        //Initialize PeerConnectionFactory globals.
        //Params are context, initAudio,initVideo and videoCodecHwAcceleration
        PeerConnectionFactory.initializeAndroidGlobals(this, true, false, false);

        //Create a new PeerConnectionFactory instance.
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        peerConnectionFactory = new PeerConnectionFactory();

        //Create MediaConstraints - Will be useful for specifying video and audio constraints.
        audioConstraints = new MediaConstraints();

        //create an AudioSource instance
        audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
        localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);
    }


    public  void call(){
        //we already have video and audio tracks. Now create peerconnections
        final List<PeerConnection.IceServer> iceServers = new ArrayList<>();

        iceServers.add( new PeerConnection.IceServer("stun:stun.l.google.com:19302"));

        iceServers.add( new PeerConnection.IceServer("stun:stun.ekiga.net"));

        iceServers.add( new PeerConnection.IceServer("stun:stun.fwdnet.net"));

        iceServers.add( new PeerConnection.IceServer("stun:stun1.l.google.com:19302"));

        iceServers.add( new PeerConnection.IceServer("stun:stun3.l.google.com:19302"));

        iceServers.add( new PeerConnection.IceServer("stun:stun2.l.google.com:19302"));

        iceServers.add( new PeerConnection.IceServer("stun:stun4.l.google.com:19302"));

        iceServers.add( new PeerConnection.IceServer("stun:stun.ideasip.com"));


        //create sdpConstraints
        sdpConstraints = new MediaConstraints();
        sdpConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveAudio", "true"));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveVideo", "false"));


        pcInitialized = true;
        //creating localPeer
        localPeer = peerConnectionFactory.createPeerConnection(iceServers, sdpConstraints, new CustomPeerConnectionObserver("local"){
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {

            }


            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                super.onIceGatheringChange(iceGatheringState);
                if (iceGatheringState == PeerConnection.IceGatheringState.COMPLETE){
                    try {
                        Log.d("TAG",sessionDescriptionToJSON(localPeer.getLocalDescription()).toString());
                        mSocket.emit(SDP,sessionDescriptionToJSON(localPeer.getLocalDescription()).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
            }


        });
        makeDataChanal();

        localPeer.createOffer(new DefaultSdpObserver(){
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                if (sessionDescription != null){
                    localPeer.setLocalDescription(new DefaultSdpObserver(){
                        @Override
                        public void onCreateSuccess(SessionDescription sessionDescription) {

                        }

                    },sessionDescription);

                }
            }
        },sdpConstraints);


        //creating local mediastream
        MediaStream stream = peerConnectionFactory.createLocalMediaStream("102");
        stream.addTrack(localAudioTrack);
        localPeer.addStream(stream);
    }


    private void makeDataChanal() {
        DataChannel.Init init = new  DataChannel.Init();
        channel = localPeer.createDataChannel("test",init);
        channel.registerObserver(new DefaultDataChannelObserver());
    }

    private JSONObject sessionDescriptionToJSON(SessionDescription sessionDescription) throws JSONException {
        JSONObject  result = new JSONObject();
        result.put(SDP,sessionDescription.description);
        return  result;
    }








}
