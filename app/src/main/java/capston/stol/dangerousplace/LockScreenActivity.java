package capston.stol.dangerousplace;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import capston.stol.dangerousplace.util.Constant;

import static capston.stol.dangerousplace.util.EncodeString.EncodeString;

/**
 * Created by Dasol,sbpark on 2016-05-24.
 */
public class LockScreenActivity extends Activity implements View.OnClickListener{

    private SharedPreferences setting;
    private String myidx;
    private String[] emgData;


    Button message;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        setting = getSharedPreferences("setting", 0);
        myidx = setting.getString("UserIdx", null);

        message = (Button)findViewById(R.id.message);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        message.setOnClickListener(this);
    }

    // 클릭 할 경우 action
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.message:
                new RequestEmgData().execute(myidx);

                break;

        }
        //DB에서 값을 받아온 후 sendSMS함수 실행
    }

    // 문자 보내는 함수
    public void sendSMS(String smsNumber, String smsText){
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT_ACTION"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(getResultCode()){
                    case Activity.RESULT_OK:
                        // 전송 성공
                        Toast.makeText(LockScreenActivity.this, "전송 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        // 전송 실패
                        Toast.makeText(LockScreenActivity.this, "전송 실패", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        // 서비스 지역 아님
                        Toast.makeText(LockScreenActivity.this, "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        // 무선 꺼짐
                        Toast.makeText(LockScreenActivity.this, "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        // PDU 실패
                        Toast.makeText(LockScreenActivity.this, "PDU Null", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_SENT_ACTION"));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        // 도착 완료
                        Toast.makeText(LockScreenActivity.this, "SMS 도착 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        // 도착 안됨
                        Toast.makeText(LockScreenActivity.this, "SMS 도착 실패", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_DELIVERED_ACTION"));

        SmsManager mSmsManager = SmsManager.getDefault();
        mSmsManager.sendTextMessage(smsNumber, null, smsText, sentIntent, deliveredIntent);
    }

    private class RequestEmgData extends AsyncTask<String, Void, String> {

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

            prop.setProperty(Constant.PARAMETER.WMyIdx, String.valueOf(Integer.parseInt(param[0])));
//            Log.e("SBSBSBSB",param[0]);

            String encodedString = EncodeString(prop);

            URL url = null;

            try {
                url = new URL(Constant.URL.Base + Constant.URL.EmergencyRequestData);
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
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "euc-kr"));

                    String line = "";
                    while ((line = br.readLine()) != null) {
                        result.append(line);
//                        Log.e("inwhile", "asas");
                    }

                    response = result.toString();
                    //is.close();

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

        //@Override
        protected void onPostExecute(String param) {
            if(!(param == null)){
                //int new_w_count = Integer.parseInt(param);

                emgData = parseEmgData(param);
//                Log.d("asdf", emgData[0]+","+emgData[1]+" helle");
                if(emgData.length == 4){
                    sendSMS(emgData[0],emgData[1]);
                    sendSMS(emgData[2],emgData[3]);
                }
                else if(emgData.length == 2){
                    sendSMS(emgData[0], emgData[1]);
                }
                else if(emgData.length == 1){
                    Toast.makeText(LockScreenActivity.this, "비상연락망 등록해주세요.", Toast.LENGTH_SHORT).show();
                }

//                Toast.makeText(LockScreenActivity.this, "성공적으로 데이터받아왔다!" + param, Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(LockScreenActivity.this, "다시 시도해주세요!", Toast.LENGTH_LONG).show();
            }

        }
    }

    //data split
    public String[] parseEmgData(String param){
        String[] parseEmg;

        parseEmg = param.split(",");
        return parseEmg;
    }

}
