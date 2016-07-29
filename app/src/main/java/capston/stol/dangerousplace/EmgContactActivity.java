package capston.stol.dangerousplace;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

import capston.stol.dangerousplace.util.Constant;

import static capston.stol.dangerousplace.util.EncodeString.EncodeString;

/**
 * Created by dasol,sbpark on 2016-05-10.
 */
public class EmgContactActivity extends Activity implements View.OnClickListener{

    private EditText name1, phone1, content;
    private EditText name2, phone2;
    private Button savebtn, getphone1, getphone2;
    private boolean count = true;

    private SharedPreferences setting;
    private String myidx;

    private String sName1 = null;
    private String sName2 = null;
    private String sPhone1 = null;
    private String sPhone2 = null;
    private String sContent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super .onCreate(savedInstanceState);

        setting = getSharedPreferences("setting", 0);
        myidx = setting.getString("UserIdx", null);

        setContentView(R.layout.activity_emg_contact);

        savebtn = (Button)findViewById(R.id.savebtn);
        getphone1 = (Button)findViewById(R.id.getphone1);
        getphone2 = (Button)findViewById(R.id.getphone2);
        name1 = (EditText)findViewById(R.id.name1);
        phone1 = (EditText)findViewById(R.id.phone1);
        name2 = (EditText)findViewById(R.id.name2);
        phone2 = (EditText)findViewById(R.id.phone2);
        content = (EditText)findViewById(R.id.content);

        savebtn.setOnClickListener(this);
        getphone1.setOnClickListener(this);
        getphone2.setOnClickListener(this);

        new GetEmgData().execute(myidx);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.savebtn :
                String smsNum = phone1.getText().toString();
                String smsNum2 = phone2.getText().toString();
                String smsText = content.getText().toString();

                // 번호 두 개 중에 하나는 꼭 존재
                // text는 꼭 존재해야함
                // 번호, 이름, 메세지 저장하는 부분으로 변경
                if (smsNum.length()>0 || smsNum2.length() > 0 && smsText.length()>0 ){
                    if(smsNum.length()>0) { //sendSMS(smsNum, smsText);
                        sName1 = name1.getText().toString();
                        Log.e("name SB",""+smsText);
                        sPhone1 = phone1.getText().toString();
                        new UpdateEmgData().execute(sName1,sPhone1,smsText,"1",myidx);
                    }
                    if(smsNum2.length()>0) { //sendSMS(smsNum2,smsText);
                        sName2 = name2.getText().toString();
                        Log.e("name SB2",""+name2.getText());
                        sPhone2 = phone2.getText().toString();
                        new UpdateEmgData().execute(sName2,sPhone2,smsText,"2",myidx);
                    }
                }else{
                    Toast.makeText(this, "모두 입력해 주세요", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.getphone1:
                count = true;
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, 0);
                break;
            case R.id.getphone2:
                count = false;
                Intent intent2 = new Intent(Intent.ACTION_PICK);
                intent2.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent2, 0);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(count == true) {
            if (resultCode == RESULT_OK) {
                Cursor cursor = getContentResolver().query(data.getData(),
                        new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
                cursor.moveToFirst();
                name1.setText(cursor.getString(0));        //이름 얻어오기
                Log.e("name SB1",""+name1.getText());
                sName1 = name1.toString();
                phone1.setText(cursor.getString(1));     //번호 얻어오기
                sPhone1 = phone1.toString();
                cursor.close();
            }
            super.onActivityResult(requestCode, resultCode, data);
        } else
        {
            if (resultCode == RESULT_OK) {
                Cursor cursor = getContentResolver().query(data.getData(),
                        new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
                cursor.moveToFirst();
                name2.setText(cursor.getString(0));        //이름 얻어오기
                sName2 = name2.toString();
                phone2.setText(cursor.getString(1));     //번호 얻어오기
                sPhone2 = phone2.toString();
                cursor.close();
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class UpdateEmgData extends AsyncTask<String, Void, String> {

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

            prop.setProperty(Constant.PARAMETER.EmgName, param[0]);
            prop.setProperty(Constant.PARAMETER.EmgPhone, param[1]);
            prop.setProperty(Constant.PARAMETER.EmgContent,param[2]);
            prop.setProperty(Constant.PARAMETER.EmgNo, String.valueOf(Integer.parseInt(param[3])));
            prop.setProperty(Constant.PARAMETER.WMyIdx,String.valueOf(Integer.parseInt(param[4])));

            String encodedString = EncodeString(prop);

            URL url = null;

            try {
                url = new URL(Constant.URL.Base + Constant.URL.EmergencyUpdate);
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

                    response = result.toString();
                    is.close();

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

                Toast.makeText(EmgContactActivity.this, "성공적으로 입력되었습니다!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(EmgContactActivity.this, "다시 시도해주세요!", Toast.LENGTH_LONG).show();
            }

        }
    }

    private class GetEmgData extends AsyncTask<String, Void, String> {

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

            prop.setProperty(Constant.PARAMETER.WMyIdx,String.valueOf(Integer.parseInt(param[0])));

            String encodedString = EncodeString(prop);

            URL url = null;

            try {
                url = new URL(Constant.URL.Base + Constant.URL.EmergencyView);
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
//                    is.close();

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
                String[] emgData = parseEmgData(param);

                if(emgData[0].equals("pull")){
                    name1.setText(emgData[1]);
                    phone1.setText(emgData[2]);
                    name2.setText(emgData[3]);
                    phone2.setText(emgData[4]);
                    content.setText(emgData[5]);
                }
                else if(emgData[0].equals("1")){
                    name1.setText(emgData[1]);
                    phone1.setText(emgData[2]);
                    content.setText(emgData[3]);
                }
                else if(emgData[0].equals("2")){
                    name2.setText(emgData[1]);
                    phone2.setText(emgData[2]);
                    content.setText(emgData[3]);
                }
                else if(emgData.length == 1){
                    content.setText(emgData[0]);
                }

//                Toast.makeText(EmgContactActivity.this, "get!!!", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(EmgContactActivity.this, "다시 시도해주세요!", Toast.LENGTH_LONG).show();
            }

        }
    }

    //LockScreenActivity와 동일함 추후 이동해야해!
    //data split
    public String[] parseEmgData(String param){
        String[] parseEmg;

        parseEmg = param.split(",");
        return parseEmg;
    }

}




