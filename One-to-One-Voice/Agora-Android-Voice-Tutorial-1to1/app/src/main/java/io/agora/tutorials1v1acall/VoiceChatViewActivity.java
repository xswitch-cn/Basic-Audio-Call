package io.agora.tutorials1v1acall;

import static android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;

public class VoiceChatViewActivity extends AppCompatActivity implements HttpConnectionTask.TaskCallback {
    private EditText mAppIdEditText;

    private EditText mTokenEditText;

    private EditText mChannelEditText;

    private EditText mUserIdEditText;

    private View mLoginView;

    private View mCallingView;

    private static final String DEFAULT_CHANNEL = "voiceDemoChannel1";

    private String appId;

    private String token = null;

    private String channel;

    private int userId;

    private TextView mCurrentUserTextView;

    private TextView mRemoteUserTextView;

    private View mXSwitchLoginView;

    private EditText mXSwitchLoginNameEditText;

    private EditText mXSwitchPasswordEditText;

    private EditText mXSwitchDomainEditText;

    private EditText mSipNumberEditText;

    private TextView mXSwitchTokenView;

    private Button mXSwitchCallButton;

    private Button mXSwitchHangUpButton;

    private String mCallData;

    private static final String LOG_TAG = VoiceChatViewActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;

    private RtcEngine mRtcEngine; // Tutorial Step 1
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1

        /**
         * Occurs when a remote user (Communication)/host (Live Broadcast) leaves the channel.
         *
         * There are two reasons for users to become offline:
         *
         *     Leave the channel: When the user/host leaves the channel, the user/host sends a goodbye message. When this message is received, the SDK determines that the user/host leaves the channel.
         *     Drop offline: When no data packet of the user or host is received for a certain period of time (20 seconds for the communication profile, and more for the live broadcast profile), the SDK assumes that the user/host drops offline. A poor network connection may lead to false detections, so we recommend using the Agora RTM SDK for reliable offline detection.
         *
         * @param uid ID of the user or host who
         * leaves
         * the channel or goes offline.
         * @param reason Reason why the user goes offline:
         *
         *     USER_OFFLINE_QUIT(0): The user left the current channel.
         *     USER_OFFLINE_DROPPED(1): The SDK timed out and the user dropped offline because no data packet was received within a certain period of time. If a user quits the call and the message is not passed to the SDK (due to an unreliable channel), the SDK assumes the user dropped offline.
         *     USER_OFFLINE_BECOME_AUDIENCE(2): (Live broadcast only.) The client role switched from the host to the audience.
         */
        @Override
        public void onUserOffline(final int uid, final int reason) { // Tutorial Step 4
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft(uid, reason);
                }
            });
        }

        /**
         * Occurs when a remote user stops/resumes sending the audio stream.
         * The SDK triggers this callback when the remote user stops or resumes sending the audio stream by calling the muteLocalAudioStream method.
         *
         * @param uid ID of the remote user.
         * @param muted Whether the remote user's audio stream is muted/unmuted:
         *
         *     true: Muted.
         *     false: Unmuted.
         */
        @Override
        public void onUserMuteAudio(final int uid, final boolean muted) { // Tutorial Step 6
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserVoiceMuted(uid, muted);
                }
            });
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLoginView.setVisibility(View.GONE);
                    mCallingView.setVisibility(View.VISIBLE);
                    mXSwitchLoginView.setVisibility(View.VISIBLE);
                    mCurrentUserTextView.setText(getString(R.string.current_user, String.valueOf(uid)));
                    showLongToast("onJoinChannelSuccess: Join success");
                }
            });
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRemoteUserTextView.setText(getString(R.string.remote_user, String.valueOf(uid)));
                }
            });
        }

        @Override
        public void onLicenseValidationFailure(int error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLongToast("onLicenseValidationFailure");
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_chat_view);

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)) {
            //initAgoraEngineAndJoinChannel();
        }
        initView();
        Window window = getWindow();
        WindowInsetsController controller = window.getInsetsController();
        if (controller != null) {
            controller.setSystemBarsAppearance(APPEARANCE_LIGHT_STATUS_BARS, APPEARANCE_LIGHT_STATUS_BARS); // 设置为深色图标
        }
    }

    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();     // Tutorial Step 1
        joinChannel();               // Tutorial Step 2
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // initAgoraEngineAndJoinChannel();
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                    finish();
                }
                break;
            }
        }
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mRtcEngine != null) {
            leaveChannel();
            RtcEngine.destroy();
        }
        mRtcEngine = null;
    }

    // Tutorial Step 7
    public void onLocalAudioMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        // Stops/Resumes sending the local audio stream.
        mRtcEngine.muteLocalAudioStream(iv.isSelected());
    }

    // Tutorial Step 5
    public void onSwitchSpeakerphoneClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        // Enables/Disables the audio playback route to the speakerphone.
        //
        // This method sets whether the audio is routed to the speakerphone or earpiece. After calling this method, the SDK returns the onAudioRouteChanged callback to indicate the changes.
        mRtcEngine.setEnableSpeakerphone(view.isSelected());
    }

    // Tutorial Step 3
    public void onEncCallClicked(View view) {
        if (mRtcEngine != null) {
            leaveChannel();
            RtcEngine.destroy();
            mRtcEngine = null;
        }
        mLoginView.setVisibility(View.VISIBLE);
        mXSwitchLoginView.setVisibility(View.GONE);
        mCallingView.setVisibility(View.GONE);
        // finish();
    }

    // Tutorial Step 1
    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), appId, mRtcEventHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));

            //throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
            showLongToast("Join fail! Reason: " + e.getMessage());
        }
    }

    // Tutorial Step 2
    private void joinChannel() {
        if (mRtcEngine == null) {
            return;
        }
//        String accessToken = getString(R.string.agora_access_token);
//        if (TextUtils.equals(accessToken, "") || TextUtils.equals(accessToken, "#YOUR ACCESS TOKEN#")) {
//            accessToken = null; // default, no token
//        }
        
        // Sets the channel profile of the Agora RtcEngine.
        // CHANNEL_PROFILE_COMMUNICATION(0): (Default) The Communication profile. Use this profile in one-on-one calls or group calls, where all users can talk freely.
        // CHANNEL_PROFILE_LIVE_BROADCASTING(1): The Live-Broadcast profile. Users in a live-broadcast channel have a role as either broadcaster or audience. A broadcaster can both send and receive streams; an audience can only receive streams.
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);

        // Allows a user to join a channel.
        int result = mRtcEngine.joinChannel(token, channel, "Extra Optional Data", userId); // if you do not specify the uid, we will generate the uid for you
        if (result != 0) {
            showLongToast("Join error: token or channel is invalid");
            mRtcEngine.leaveChannel();
        }
    }

    // Tutorial Step 3
    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    // Tutorial Step 4
    private void onRemoteUserLeft(int uid, int reason) {
        showLongToast(String.format(Locale.US, "user %d left %d", (uid & 0xFFFFFFFFL), reason));
        View tipMsg = findViewById(R.id.quick_tips_when_use_agora_sdk); // optional UI
        tipMsg.setVisibility(View.VISIBLE);
        mXSwitchHangUpButton.setEnabled(false);
        mRemoteUserTextView.setText(getString(R.string.remote_user, "no remote user joined"));
        if (!TextUtils.isEmpty(mXSwitchTokenView.getText().toString())) {
            mXSwitchCallButton.setEnabled(true);
        }
    }

    // Tutorial Step 6
    private void onRemoteUserVoiceMuted(int uid, boolean muted) {
        showLongToast(String.format(Locale.US, "user %d muted or unmuted %b", (uid & 0xFFFFFFFFL), muted));
    }

    // Extended
    @Override
    protected void onResume() {
        super.onResume();
        restoreUserData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveUserData();
    }

    private void saveUserData() {
        // 获取SharedPreferences对象
        SharedPreferences sharedPreferences = getSharedPreferences(CommonConstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
        // 获取Editor对象
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 保存数据
        editor.putString(CommonConstant.APP_ID, mAppIdEditText.getText().toString()); // 保存字符串
        editor.putString(CommonConstant.TOKEN, mTokenEditText.getText().toString());
        editor.putString(CommonConstant.CHANNEL, mChannelEditText.getText().toString());
        editor.putString(CommonConstant.USER_ID, mUserIdEditText.getText().toString());

        editor.putString(CommonConstant.X_SWITCH_LOGIN_NAME, mXSwitchLoginNameEditText.getText().toString()); // 保存字符串
        editor.putString(CommonConstant.X_SWITCH_PASSWORD, mXSwitchPasswordEditText.getText().toString());
        editor.putString(CommonConstant.X_SWITCH_DOMAIN, mXSwitchDomainEditText.getText().toString());
        // editor.putString(CommonConstant.X_SWITCH_TOKEN, mXSwitchTokenView.getText().toString());
        editor.putString(CommonConstant.X_SWITCH_SIP_NUMBER, mSipNumberEditText.getText().toString());

        // 提交更改
        editor.apply(); // 或者 editor.commit(); 但apply是异步的，通常更快
    }

    private void restoreUserData() {
        // 获取SharedPreferences对象
        SharedPreferences sharedPreferences = getSharedPreferences(CommonConstant.PREFERENCE_NAME, Context.MODE_PRIVATE);

        // 读取数据
        String appId = sharedPreferences.getString(CommonConstant.APP_ID, "");
        String token = sharedPreferences.getString(CommonConstant.TOKEN, "");
        String channel = sharedPreferences.getString(CommonConstant.CHANNEL, "");
        String user_id = sharedPreferences.getString(CommonConstant.USER_ID, "");

        String xSwitchLoginName = sharedPreferences.getString(CommonConstant.X_SWITCH_LOGIN_NAME, "");
        String xSwitchPassword = sharedPreferences.getString(CommonConstant.X_SWITCH_PASSWORD, "");
        String xSwitchDomain = sharedPreferences.getString(CommonConstant.X_SWITCH_DOMAIN, "");
        // String xSwitchToken = sharedPreferences.getString(CommonConstant.X_SWITCH_TOKEN, "");
        String xSwitchSipNumber = sharedPreferences.getString(CommonConstant.X_SWITCH_SIP_NUMBER, "");
        mAppIdEditText.setText(appId);
        mTokenEditText.setText(token);
        mChannelEditText.setText(channel);
        mUserIdEditText.setText(user_id);
        mXSwitchLoginNameEditText.setText(xSwitchLoginName);
        mXSwitchPasswordEditText.setText(xSwitchPassword);
        mXSwitchDomainEditText.setText(xSwitchDomain);
        // mXSwitchTokenView.setText(xSwitchToken);
        mSipNumberEditText.setText(xSwitchSipNumber);
    }

    private void initView() {
        mAppIdEditText = findViewById(R.id.app_id_edit);
        mTokenEditText = findViewById(R.id.token_edit);
        mChannelEditText = findViewById(R.id.channel_edit);
        mUserIdEditText = findViewById(R.id.user_id_edit);
        mLoginView = findViewById(R.id.loginView);
        mCallingView = findViewById(R.id.callingView);
        mCurrentUserTextView = findViewById(R.id.current_user);
        mRemoteUserTextView = findViewById(R.id.remote_user);
        mRemoteUserTextView.setText(getString(R.string.remote_user, "no remote user joined"));
        mXSwitchLoginView = findViewById(R.id.xSwitchLoginView);
        mXSwitchLoginNameEditText = findViewById(R.id.login);
        mXSwitchPasswordEditText = findViewById(R.id.password);
        mXSwitchDomainEditText = findViewById(R.id.domain);
        mSipNumberEditText = findViewById(R.id.sipNumber);
        mXSwitchTokenView = findViewById(R.id.token_text);
        mXSwitchCallButton = findViewById(R.id.xSwitchCallButton);
        mXSwitchHangUpButton = findViewById(R.id.xSwitchHangUpButton);
    }

    public void onJoinButtonClicked(View view) {
        appId = (mAppIdEditText.getText() != null) ? mAppIdEditText.getText().toString() : "";
        token = (mTokenEditText.getText() != null) ? mTokenEditText.getText().toString() : null;
        channel = (mChannelEditText.getText() != null) ? mChannelEditText.getText().toString() : DEFAULT_CHANNEL;
        if (mUserIdEditText.getText() != null) {
            try {
                userId = Integer.parseInt(mUserIdEditText.getText().toString());
            } catch (NumberFormatException e) {
                userId = 0;
            }
        }
        if (TextUtils.isEmpty(appId)) {
            showLongToast("APP ID must not be null!");
            return;
        }
        if (!checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)) {
            return;
        }
        initAgoraEngineAndJoinChannel();
    }

    public void onLeaveButtonClicked(View view) {
        if (mRtcEngine != null) {
            leaveChannel();
            RtcEngine.destroy();
            mRtcEngine = null;
        }
    }

    public void onLoginButtonClicked(View view) {
        String login = mXSwitchLoginNameEditText.getText().toString();
        String password = mXSwitchPasswordEditText.getText().toString();
        String domain = mXSwitchDomainEditText.getText().toString();
        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(password) || TextUtils.isEmpty(domain)) {
            Log.i(LOG_TAG, "invalid parameter: login or password or domain is empty!");
            showLongToast("invalid parameter: login or password or domain is empty!");
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("login", login);
            jsonObject.put("password", password);
            jsonObject.put("domain", domain);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "onLoginButtonClicked" + e.getMessage());
        }
        HttpConnectionTask httpConnectionTask = new HttpConnectionTask(jsonObject.toString(), this);
        httpConnectionTask.execute(CommonConstant.GET_TOKEN_URL);
    }

    public void onCallButtonClicked(View view) {
        String token = mTokenEditText.getText().toString();
        String channel = mChannelEditText.getText().toString();
        String sipNumber = mSipNumberEditText.getText().toString();
        String domain = mXSwitchDomainEditText.getText().toString();
        if (TextUtils.isEmpty(sipNumber) || TextUtils.isEmpty(domain)) {
            Log.i(LOG_TAG, "invalid parameter: sipNumber or domain is empty!");
            showLongToast("invalid parameter: sipNumber or domain is empty!");
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("destNumber", sipNumber);
            jsonObject.put("token", token);
            jsonObject.put("channel", channel);
            jsonObject.put("domain", domain);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "onCallButtonClicked" + e.getMessage());
        }
        HttpConnectionTask httpConnectionTask = new HttpConnectionTask(jsonObject.toString(), this);
        httpConnectionTask.setAuthorization(mXSwitchTokenView.getText().toString());
        httpConnectionTask.execute(CommonConstant.CALL_PSTN_URL);
    }

    public void onHangUpButtonClicked(View view) {
        if (TextUtils.isEmpty(mCallData)) {
            showLongToast("No sip call is running!");
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("hangup_cause", "NORMAL_CLEARING");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "onHangUpButtonClicked" + e.getMessage());
        }
        HttpConnectionTask httpConnectionTask = new HttpConnectionTask(jsonObject.toString(), this);
        httpConnectionTask.setAuthorization(mXSwitchTokenView.getText().toString());
        httpConnectionTask.setCallData(mCallData);
        httpConnectionTask.execute(CommonConstant.CALL_HANG_UP_URL);
    }

    @Override
    public void onRequestReturn(String url, String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch(url) {
                    case CommonConstant.GET_TOKEN_URL: {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            mXSwitchTokenView.setText(jsonObject.getString("token"));
                            mXSwitchCallButton.setEnabled(true);
                            showLongToast("XSwitch login successful!");
                        } catch (JSONException e) {
                            //throw new RuntimeException(e);
                            e.printStackTrace();
                            showLongToast(result);
                        }
                        break;
                    }
                    case CommonConstant.CALL_PSTN_URL: {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            mCallData = jsonObject.getString("data");
                            mXSwitchHangUpButton.setEnabled(true);
                            mXSwitchCallButton.setEnabled(false);
                            showLongToast("Call successful!");
                        } catch (JSONException e) {
                            //throw new RuntimeException(e);
                            e.printStackTrace();
                            showLongToast(result);
                        }
                        break;
                    }
                    case CommonConstant.CALL_HANG_UP_URL:
                        break;
                    default: {
                        showLongToast(result);
                        break;
                    }
                }
            }
        });
    }
}
