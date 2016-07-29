package capston.stol.dangerousplace;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import capston.stol.dangerousplace.gcm.NotificationBar;
import capston.stol.dangerousplace.gcm.QuickstartPreferences;
import capston.stol.dangerousplace.gcm.RegistrationIntentService;
import capston.stol.dangerousplace.util.Constant;

import static capston.stol.dangerousplace.util.EncodeString.EncodeString;

/**
 * Created by dasol on 2016-05-14.
 */
public class SetDangerModeActivity extends Activity {

    private Switch swTracking, swSendMessage, swPushAlert;
    private Button btnSaveSetting;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    SharedPreferences setting;
    SharedPreferences.Editor edit;
    String useridx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registBroadcastReceiver();

        setting = getSharedPreferences("setting", 0);
        useridx = setting.getString("UserIdx", null);
        edit = setting.edit();

        setContentView(R.layout.activity_set_danger_mode);

        swPushAlert = (Switch)findViewById(R.id.swPushAlert);
        swSendMessage = (Switch)findViewById(R.id.swSendMessage);
        swTracking = (Switch)findViewById(R.id.swTracking);
        btnSaveSetting = (Button)findViewById(R.id.btnSaveSetting);
        btnSaveSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.i("gps", setting.getBoolean("GPSTracking", false)+","+setting.getBoolean("PushAlert", false));
                finish();
            }
        });

        //switch init
        if(setting.getBoolean("SendMessage", false)){
            swSendMessage.setChecked(true);
        } else {
            swSendMessage.setChecked(false);
        }
        if(setting.getBoolean("PushAlert", false)){
            swPushAlert.setChecked(true);
        } else {
            swPushAlert.setChecked(false);
        }
        if(setting.getBoolean("GPSTracking", false)){
            swTracking.setChecked(true);
        } else {
            swTracking.setChecked(false);
        }

        swSendMessage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    edit.putBoolean("SendMessage", true);
                    edit.commit();
//                    Intent intent = new Intent(SetDangerModeActivity.this, ScreenService.class);
//                    startService(intent);
                }else {
                    edit.putBoolean("SendMessage", false);
                    edit.commit();
//                    Intent intent = new Intent(SetDangerModeActivity.this, ScreenService.class);
//                    stopService(intent);
                }
            }
        });

        swTracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 버튼이 on 되었을 때 서비스 시작
                if(isChecked) {
                    edit.putBoolean("GPSTracking", true);
                    edit.commit();
                } else {
                    // 버튼이 off 되었을 때
                    edit.putBoolean("GPSTracking", false);
                    edit.commit();
                }
            }
        });

        swPushAlert.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    getInstanceIdToken();
                } else {
                    //gcm문자받지않음 서버로 전송
                    new SendPushOff().execute(useridx);
                }
            }
        });

    }

    //앱이 실행되어 화면에 나타날때 LocalBoardcastManager에 액션을 정의하여 등록한다.
    @Override
    protected void onResume() {
        super.onResume();

//        ActivityManager manager = (ActivityManager)this.getSystemService(Activity.ACTIVITY_SERVICE);
//        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if("capston.stol.dangerousplace.util.ScreenService".equals(service.service.getClassName())) {
//                //위험시 문자전송 모드 실행중
//                swScreen.setChecked(true);
//            } else {
//                swScreen.setChecked(false);
//            }
//        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_READY));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_GENERATING));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));

    }

    //앱이 화면에서 사라지면 등록된 LocalBoardcast를 모두 삭제한다.
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    //Google Play Service를 사용할 수 있는 환경인지 체크한다.
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("asd", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void getInstanceIdToken() {
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    //LocalBroadcast 리시버를 정의한다. 토큰을 획득하기 위한 READY, GENERATING, COMPLETE 액션에 따라 UI에 변화를 준다.
    public void registBroadcastReceiver(){
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.i("asdfASDf", "asdfasdf");
                if(action.equals(QuickstartPreferences.REGISTRATION_READY)){
                    // 액션이 READY일 경우
                    Log.i("asdfASDf", "ready");
                } else if(action.equals(QuickstartPreferences.REGISTRATION_GENERATING)){
                    // 액션이 GENERATING일 경우
                    Log.i("asdfASDf", "ing");
                } else if(action.equals(QuickstartPreferences.REGISTRATION_COMPLETE)){
                    // 액션이 COMPLETE일 경우
                    Log.i("asdfASDf", "complete");
                    String token = intent.getStringExtra("token");
                    new SendPushRequest().execute(token, useridx);
                }
            }
        };
    }

    private class SendPushOff extends AsyncTask<String, Void, String> {

        StringBuilder result = new StringBuilder();
        String response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            Properties prop = new Properties();

            InputStream is;
            BufferedReader br;
            OutputStream os;

            HttpURLConnection conn;

            prop.setProperty(Constant.PARAMETER.UserIdx, params[0].toString());

            String encodedString = EncodeString(prop);

            URL url = null;

            try {
                url = new URL(Constant.URL.Base + Constant.URL.SetPushOff);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Accept", "application/json");

                os = conn.getOutputStream();
                os.write(encodedString.getBytes());
                os.flush();
                os.close();

//                Log.d("sendWarningInfo", url + "?" + encodedString);

                int responseCode = conn.getResponseCode();
                Log.d("respCode", responseCode + "");

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    is = new BufferedInputStream(conn.getInputStream());
                    br = new BufferedReader(new InputStreamReader(is));

                    String line = "";
                    while ((line = br.readLine()) != null) {
                        result.append(line);
//                        Log.e("inwhile", "asas");
                    }
                    is.close();

                    response = result.toString();

                    Log.d("result json:", response);

                } else {
                    return null;
                }
                conn.disconnect();
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("push off")){
                edit.putBoolean("PushAlert", false);
                edit.commit();
            }


        }

    }

    private class SendPushRequest extends AsyncTask<String, Void, String> {

        StringBuilder result = new StringBuilder();
        String response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... param) {
            Properties prop = new Properties();

            InputStream is;
            BufferedReader br;
            OutputStream os;

            HttpURLConnection conn;

            prop.setProperty(Constant.PARAMETER.InstanceId, param[0].toString());
            prop.setProperty(Constant.PARAMETER.UserIdx, param[1].toString());

            String encodedString = EncodeString(prop);

            URL url = null;

            try {
                url = new URL(Constant.URL.Base + Constant.URL.SetPushOn);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Accept", "application/json");

                os = conn.getOutputStream();
                os.write(encodedString.getBytes());
                os.flush();
                os.close();

//                Log.d("sendWarningInfo", url + "?" + encodedString);

                int responseCode = conn.getResponseCode();
                Log.d("respCode", responseCode + "");

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    is = new BufferedInputStream(conn.getInputStream());
                    br = new BufferedReader(new InputStreamReader(is));

                    String line = "";
                    while ((line = br.readLine()) != null) {
                        result.append(line);
//                        Log.e("inwhile", "asas");
                    }
                    is.close();

                    response = result.toString();

                    Log.d("result json:", response);

                } else {
                    return null;
                }
                conn.disconnect();
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String param) {

            if(param!=null){
                Log.e("PUSH SB", param);

                Intent intent = new Intent(SetDangerModeActivity.this, NotificationBar.class);
                startService(intent);
                //new MyGcmListenerService().onMessageReceived(gcmData);

                edit.putBoolean("PushAlert", true);
                edit.commit();

            }else
                Log.e("PUSH SB error","error....");

        }
    }

}
