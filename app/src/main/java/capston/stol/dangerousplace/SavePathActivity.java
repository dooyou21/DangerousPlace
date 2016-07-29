package capston.stol.dangerousplace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;


import capston.stol.dangerousplace.util.Constant;
import static capston.stol.dangerousplace.util.EncodeString.EncodeString;
import capston.stol.dangerousplace.util.GPSTrackingService;

public class SavePathActivity extends AppCompatActivity {

    TextView tvNotification;
    Button btnOK;
    boolean isNeedSave = false;
    String Path = "";
    String usridx = "";

    private SharedPreferences setting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_path);

        tvNotification = (TextView)findViewById(R.id.tvNotification);
        btnOK = (Button)findViewById(R.id.btnOK);

        Intent intent = getIntent();
        Path = intent.getStringExtra("PATH");

        //preference
        setting = getSharedPreferences("setting", 0);
        usridx = setting.getString("UserIdx", null);


        stopService(new Intent(SavePathActivity.this, GPSTrackingService.class));

        if(Path.equals("")) {
            tvNotification.setText("움직임이 감지되지 않아 경로를 저장하지 않습니다.");
            Log.d("asdf", "path not exist");
            isNeedSave = false;
        } else {
            tvNotification.setText("지금까지의 이동경로가 저장됩니다.");
            Log.d("asdf", "path: "+Path);
            isNeedSave = true;
        }

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNeedSave){
                    new SavePathAsyncTask().execute(Path, usridx);
                    //asynctask 를 이용하여 서버에 'path'저장. 후에 postexecute에서 아래의 두줄 호출.

                }
                else {
                    finish();
                    startActivity(new Intent(SavePathActivity.this, MainMapActivity.class));
                }
            }
        });

    }

    private class SavePathAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String Path = params[0];
            String UserIdx = params[1];
            String resultstr = "";
            StringBuilder result = new StringBuilder();

            Properties prop = new Properties();
            BufferedReader br;
            OutputStream os;

            HttpURLConnection conn;

            prop.setProperty(Constant.PARAMETER.Path, Path);
            prop.setProperty(Constant.PARAMETER.UserIdx, UserIdx);

            String encodedString = EncodeString(prop);

            URL url = null;

            try{
                url = new URL(Constant.URL.Base + Constant.URL.SavePath);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Accept", "application/json");

                os = conn.getOutputStream();
                os.write(encodedString.getBytes());
                os.flush();
                os.close();

//                Log.d("URL", url + "?" + encodedString);
                int responseCode = conn.getResponseCode();
//                Log.d("respCode", responseCode + "");

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "euc-kr"));

                    String line = "";
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }

                    resultstr = result.toString();

                } else {
//                error;
                }
                conn.disconnect();
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return resultstr;
        }

        @Override
        protected void onPostExecute(String result) {

            if(result.equals("success")){
                Log.i("good", "good!!");
                Toast.makeText(SavePathActivity.this, "성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(new Intent(SavePathActivity.this, MainMapActivity.class));
            } else {
                Toast.makeText(SavePathActivity.this, "다시 시도해주세요.", Toast.LENGTH_LONG).show();
            }
        }
    }


}
