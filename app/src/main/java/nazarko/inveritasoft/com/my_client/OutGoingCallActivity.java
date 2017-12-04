package nazarko.inveritasoft.com.my_client;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Toast;

import org.webrtc.SessionDescription;

import java.util.logging.StreamHandler;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static nazarko.inveritasoft.com.my_client.Constants.*;

public class OutGoingCallActivity extends BaseActivity {

    private int CONNECTION_REQUEST =  110;

    View greenView;
    View redView;

    String mUserId;
    String mRoomId;

    private boolean onback=false;

    @Override
    public void onBackPressed() {
        if (onback==false)
            mSocket.emit(Constants.HANG_UP,mUserId,mRoomId);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_going_call);
        initData();
        initViews();
        start();
        call();
    }

    @Override
    protected void onDestroy() {
        mSocket.off(ANSWER_CALL_PHONE, onAnswerCall);
        mSocket.off(HANG_UP_CALL, onHangUpCall);
        mSocket.off(SDP_PHONE, onSDP);
        super.onDestroy();
    }

    private void initData() {
        mSocket = ((MyClientApplication)getApplication()).getSocket();
        mSocket.on(ANSWER_CALL_PHONE, onAnswerCall);
        mSocket.on(HANG_UP_CALL, onHangUpCall);
        mSocket.on(SDP_PHONE, onSDP);
        Intent intent = getIntent();
        if (intent !=null){
            Bundle bundle = intent.getExtras();
            if (bundle!=null){
                mUserId = bundle.getString(MainActivity.BUNDLE_USER_ID);
                mRoomId = bundle.getString(MainActivity.BUNDLE_ROOM_ID);
                Log.d(MainActivity.TAG+"room",mRoomId);
            }
        }
    }

    private void initViews() {
        greenView = findViewById(R.id.green);
        redView = findViewById(R.id.red);
        redView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.emit(Constants.HANG_UP,mUserId,mRoomId);
            }
        });
        greenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.emit(Constants.ANSWER_CALL,mUserId);
            }
        });
        greenView.setVisibility(View.GONE);
    }

    private Emitter.Listener onAnswerCall = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(OutGoingCallActivity.this, "answer call", Toast.LENGTH_SHORT).show();
                    greenView.setVisibility(View.GONE);
                    //connectToRoom(mRoomId ,0);

                }
            });
        }
    };

    private Emitter.Listener onHangUpCall = new Emitter.Listener() {
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onback = true;
                    onBackPressed();
                }
            });
        }
    };

    private Emitter.Listener onSDP = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.ANSWER,(String)args[0]);
                    localPeer.setRemoteDescription(new DefaultSdpObserver(),sessionDescription);
                    Toast.makeText(OutGoingCallActivity.this, (String)args[0], Toast.LENGTH_SHORT).show();
                }
            });
        }
    };


    private void connectToRoom(String roomId, int runTimeMs) {


        String roomUrl =  getString(R.string.pref_room_server_url_default);

        // Video call enabled flag.
        boolean videoCallEnabled =  Boolean.valueOf(getString(R.string.pref_videocall_default));

        // Use screencapture option.
        boolean useScreencapture = Boolean.valueOf(getString(R.string.pref_screencapture_default));

        // Use Camera2 option.
        boolean useCamera2 = Boolean.valueOf(getString(R.string.pref_camera2_default));

        // Get default codecs.
        String videoCodec = getString(R.string.pref_videocodec_default);

        String audioCodec = getString(R.string.pref_audiocodec_default);

        // Check HW codec flag.
        boolean hwCodec =  Boolean.valueOf(getString(R.string.pref_hwcodec_default));

        // Check Capture to texture.
        boolean captureToTexture =  Boolean.valueOf(getString(R.string.pref_capturetotexture_default));

        // Check FlexFEC.
        boolean flexfecEnabled = Boolean.valueOf(getString(R.string.pref_flexfec_default));

        // Check Disable Audio Processing flag.
        boolean noAudioProcessing = Boolean.valueOf(getString(R.string.pref_noaudioprocessing_default));

        // Check Disable Audio Processing flag.
        boolean aecDump = Boolean.valueOf(getString(R.string.pref_aecdump_default));

        // Check OpenSL ES enabled flag.
        boolean useOpenSLES = Boolean.valueOf(getString(R.string.pref_opensles_default));

        // Check Disable built-in AEC flag.
        boolean disableBuiltInAEC = Boolean.valueOf(getString(R.string.pref_disable_built_in_aec_default));

        // Check Disable built-in AGC flag.
        boolean disableBuiltInAGC = Boolean.valueOf(getString(R.string.pref_disable_built_in_agc_default));


        // Check Disable built-in NS flag.
        boolean disableBuiltInNS = Boolean.valueOf(getString(R.string.pref_disable_built_in_ns_default));

        // Check Enable level control.
        boolean enableLevelControl =  Boolean.valueOf(getString(R.string.pref_enable_level_control_default));

        // Check Disable gain control
        boolean disableWebRtcAGCAndHPF = Boolean.valueOf(getString(R.string.pref_disable_webrtc_agc_default));

        // Get video resolution from settings.
        int videoWidth = 0;
        int videoHeight = 0;
        if (videoWidth == 0 && videoHeight == 0) {
            String resolution = getString(R.string.pref_resolution_default);
            String[] dimensions = resolution.split("[ x]+");
            if (dimensions.length == 2) {
                try {
                    videoWidth = Integer.parseInt(dimensions[0]);
                    videoHeight = Integer.parseInt(dimensions[1]);
                } catch (NumberFormatException e) {
                    videoWidth = 0;
                    videoHeight = 0;
                }
            }
        }

        // Get camera fps from settings.
        int cameraFps = 0;
        if (cameraFps == 0) {
            String fps =  getString(R.string.pref_fps_default);
            String[] fpsValues = fps.split("[ x]+");
            if (fpsValues.length == 2) {
                try {
                    cameraFps = Integer.parseInt(fpsValues[0]);
                } catch (NumberFormatException e) {
                    cameraFps = 0;
                }
            }
        }

        // Check capture quality slider flag.
        boolean captureQualitySlider = Boolean.valueOf(getString(R.string.pref_capturequalityslider_default));

        // Get video and audio start bitrate.
        int videoStartBitrate = 0;
        if (videoStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_maxvideobitrate_default);
            String bitrateType =  "";
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue =  getString(R.string.pref_maxvideobitratevalue_default);
                videoStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        int audioStartBitrate = 0;
        if (audioStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
            String bitrateType = "";
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue =  getString(R.string.pref_startaudiobitratevalue_default);
                audioStartBitrate = Integer.parseInt(bitrateValue);
            }
        }



        // Check statistics display option.
        boolean displayHud = Boolean.valueOf(getString(R.string.pref_displayhud_default));

        boolean tracing = Boolean.valueOf(getString(R.string.pref_tracing_default));

        // Get datachannel options
        boolean dataChannelEnabled = Boolean.valueOf(getString(R.string.pref_enable_datachannel_default));

        boolean ordered = Boolean.valueOf(getString(R.string.pref_ordered_default));

        boolean negotiated = Boolean.valueOf(getString(R.string.pref_negotiated_default));

        int maxRetrMs =  Integer.valueOf(getString(R.string.pref_max_retransmit_time_ms_default));


        int maxRetr =  Integer.valueOf(getString(R.string.pref_max_retransmits_default));

        int id = Integer.valueOf(getString(R.string.pref_data_id_default));

        String protocol = getString(R.string.pref_data_protocol_default);

        if (validateUrl(roomUrl)) {
            Uri uri = Uri.parse(roomUrl);
            Intent intent = new Intent(this, CallActivity.class);
            intent.setData(uri);
            intent.putExtra(CallActivity.EXTRA_ROOMID, roomId);
            intent.putExtra(CallActivity.EXTRA_VIDEO_CALL, videoCallEnabled);
            intent.putExtra(CallActivity.EXTRA_SCREENCAPTURE, useScreencapture);
            intent.putExtra(CallActivity.EXTRA_CAMERA2, useCamera2);
            intent.putExtra(CallActivity.EXTRA_VIDEO_WIDTH, videoWidth);
            intent.putExtra(CallActivity.EXTRA_VIDEO_HEIGHT, videoHeight);
            intent.putExtra(CallActivity.EXTRA_VIDEO_FPS, cameraFps);
            intent.putExtra(CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
            intent.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, videoStartBitrate);
            intent.putExtra(CallActivity.EXTRA_VIDEOCODEC, videoCodec);
            intent.putExtra(CallActivity.EXTRA_HWCODEC_ENABLED, hwCodec);
            intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
            intent.putExtra(CallActivity.EXTRA_FLEXFEC_ENABLED, flexfecEnabled);
            intent.putExtra(CallActivity.EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
            intent.putExtra(CallActivity.EXTRA_AECDUMP_ENABLED, aecDump);
            intent.putExtra(CallActivity.EXTRA_OPENSLES_ENABLED, useOpenSLES);
            intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
            intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
            intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
            intent.putExtra(CallActivity.EXTRA_ENABLE_LEVEL_CONTROL, enableLevelControl);
            intent.putExtra(CallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, disableWebRtcAGCAndHPF);
            intent.putExtra(CallActivity.EXTRA_AUDIO_BITRATE, audioStartBitrate);
            intent.putExtra(CallActivity.EXTRA_AUDIOCODEC, audioCodec);
            intent.putExtra(CallActivity.EXTRA_DISPLAY_HUD, displayHud);
            intent.putExtra(CallActivity.EXTRA_TRACING, tracing);
            intent.putExtra(CallActivity.EXTRA_RUNTIME, runTimeMs);

            intent.putExtra(CallActivity.EXTRA_DATA_CHANNEL_ENABLED, dataChannelEnabled);

            if (dataChannelEnabled) {
                intent.putExtra(CallActivity.EXTRA_ORDERED, ordered);
                intent.putExtra(CallActivity.EXTRA_MAX_RETRANSMITS_MS, maxRetrMs);
                intent.putExtra(CallActivity.EXTRA_MAX_RETRANSMITS, maxRetr);
                intent.putExtra(CallActivity.EXTRA_PROTOCOL, protocol);
                intent.putExtra(CallActivity.EXTRA_NEGOTIATED, negotiated);
                intent.putExtra(CallActivity.EXTRA_ID, id);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private boolean validateUrl(String url) {
        if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
            return true;
        }
        Toast.makeText(getApplicationContext(),getString(R.string.error),Toast.LENGTH_SHORT).show();
        return false;
    }


}
