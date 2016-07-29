package capston.stol.dangerousplace;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import capston.stol.dangerousplace.bean.WarningInfo;
import capston.stol.dangerousplace.util.Constant;

import static capston.stol.dangerousplace.util.EncodeString.EncodeString;

public class InsertWarningInfoActivity extends AppCompatActivity implements View.OnClickListener{

    EditText ettitle, etcontents;
    private Button btnCancel, btnConfirm, gpsbutton;
    private ImageView gpsimg;

    private WarningInfo winfo = new WarningInfo();

    int categoryIndex;
    TextView datetext;
    TextView timetext;
    Calendar calendar = Calendar.getInstance();
    Spinner spCategory;

    String infoDate_date, infoDate_time;

    String usridx, insertOrUpdate;

    boolean gpsinfo = false;
    boolean titleinfo = false;
    boolean dateinfo = false;
    boolean timeinfo = false;
    boolean contentinfo = false;
    boolean categoryinfo = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 등록 창의 타이틀 삭제
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_insert_warning_info);

        usridx = getIntent().getExtras().getString("USRIDX");
        insertOrUpdate = getIntent().getExtras().getString("INSERTORUPDATE");
        winfo.setUsrIdx(Integer.parseInt(usridx));

        ettitle = (EditText) findViewById(R.id.ettitle);
        etcontents = (EditText) findViewById(R.id.etcontents);

        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnConfirm = (Button)findViewById(R.id.btnConfirm);
        btnCancel.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

        //설정된 날짜/시간 표시
        datetext = (TextView)findViewById(R.id.datebutton);
        timetext = (TextView)findViewById(R.id.timebutton);
        datetext.setOnClickListener(this);
        timetext.setOnClickListener(this);

        //위치 선정으로 들어가는 버튼
        gpsbutton =  (Button)findViewById(R.id.gpsbutton);
        gpsbutton.setOnClickListener(this);
        // gps image
        gpsimg = (ImageView)findViewById(R.id.gpsimg);
        gpsimg.setImageResource(R.drawable.gps);

        // 분류 spinner 설정
        spCategory = (Spinner) findViewById(R.id.categorize_spinner);
        String[] cat = getResources().getStringArray(R.array.category1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_dropdown_item, cat);
        spCategory.setAdapter(adapter);
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedView, int position, long id) {
                printChecked(selectedView, position);
                categoryinfo = true;
                winfo.setCategory(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });

        if(insertOrUpdate.equals("update")){
            winfo.setTitle(getIntent().getExtras().getString("TITLE"));
            winfo.setDatetime(getIntent().getExtras().getString("DATE"));
            winfo.setContent(getIntent().getExtras().getString("CONTENT"));
            winfo.setXCoord(getIntent().getExtras().getDouble("XCOORD"));
            winfo.setYCoord(getIntent().getExtras().getDouble("YCOORD"));
            winfo.setIdx(getIntent().getExtras().getInt("WINDEX"));
            winfo.setCategory(getIntent().getExtras().getInt("CATEGORY"));

            ettitle.setText(winfo.getTitle());
            etcontents.setText(winfo.getContent());

            gpsimg.setImageResource(R.drawable.gps2);

            //textview에 표현하기 위해...
            String dt[] = winfo.getDatetime().split(" ");
            String date = dt[0].replace("-",".");
            String t[] = dt[1].split(":");
            if(Integer.parseInt(t[0])==12){
                //오후
                t[0] = "오후 "+t[0];
            } else if(Integer.parseInt(t[0])>12){
                //오후
                t[0] = (Integer.parseInt(t[0]) - 12) + "";
                t[0] = "오후 "+t[0];
            } else if(Integer.parseInt(t[0])==0) {
                //오전
                t[0] = "오전 12";
            } else {
                t[0] = "오전 "+t[0];
            }
            datetext.setText(date);
            timetext.setText("" + t[0] + ":" + t[1] + ":" + t[2]);

            Log.e("timeeee", date);

            //calendar 설정
            Date date1 = null;
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREAN);
            try {
                date1 = format.parse(winfo.getDatetime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            calendar.setTime(date1);
            updateDate();
            updateTime();

            timeinfo = true;
            dateinfo = true;
            gpsinfo = true;

            spCategory.setSelection(winfo.getCategory());


        }


    }

    // spinner 관련 함수 정의
    public void printChecked(View v, int position) {
        Spinner sp1 = (Spinner)findViewById(R.id.categorize_spinner);

        if(sp1.getSelectedItemPosition() >0 ){
            // sp1.getSelectedItemPosition() 이 선택된 리스트의 숫자를 가져온다
            categoryIndex = sp1.getSelectedItemPosition();
            Log.d("dasdol", "dadoldadol: " + sp1.getSelectedItemPosition());
        }
    }


    //날짜를 설정하는 창
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDate();

        }
    };

    //시간을 설정하는 창
    TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE,minute);
            updateTime();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.btnConfirm:

                if(!ettitle.getText().toString().equals("")) titleinfo = true;
                if(!etcontents.getText().toString().equals("")) contentinfo = true;

                if(gpsinfo && timeinfo && dateinfo && titleinfo && contentinfo && categoryinfo) {
                    winfo.setTitle(ettitle.getText().toString());
                    winfo.setContent(etcontents.getText().toString());
                    String infoDate = infoDate_date + " " + infoDate_time;
                    winfo.setDatetime(infoDate);
                    winfo.setCategory(categoryIndex);

                    if(insertOrUpdate.equals("update")){
                        Log.e("dateeeee", winfo.getDatetime());
                        new UpdateWarningInfo().execute(winfo);
                    } else {
                        new SendWarningInfo().execute(winfo);
                    }

                } else {
                    if(!titleinfo)
                        Toast.makeText(InsertWarningInfoActivity.this, "제목을 작성해주세요!.", Toast.LENGTH_LONG).show();
                    else if(!dateinfo)
                        Toast.makeText(InsertWarningInfoActivity.this, "날짜를 설정해주세요!", Toast.LENGTH_LONG).show();
                    else if(!timeinfo)
                        Toast.makeText(InsertWarningInfoActivity.this, "시간을 설정해주세요!", Toast.LENGTH_LONG).show();
                    else if(!contentinfo)
                        Toast.makeText(InsertWarningInfoActivity.this, "내용을 작성해주세요!", Toast.LENGTH_LONG).show();
                    else if(!categoryinfo)
                        Toast.makeText(InsertWarningInfoActivity.this, "카테고리를 선택해주세요!", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(InsertWarningInfoActivity.this, "장소를 지정해주세요!", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.datebutton:
                new DatePickerDialog(InsertWarningInfoActivity.this, date,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.timebutton:
                new TimePickerDialog(InsertWarningInfoActivity.this, time,
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false).show();
                break;
            // gps 버튼 클릭시에 RegiMapActivity로 액티비티 전환
            case R.id.gpsbutton:
                Intent lIntent = new Intent(InsertWarningInfoActivity.this , RegiMapActivity.class);
                lIntent.putExtra("INSERTORUPDATE", insertOrUpdate);
                if(insertOrUpdate.equals("update")) {
                    lIntent.putExtra("XCOORD", winfo.getXCoord());
                    lIntent.putExtra("YCOORD", winfo.getYCoord());
                }
                startActivityForResult(lIntent, 1);
                break;
        }
    }

    private void updateDate() {
        Date date = calendar.getTime();
        DateFormat mDateFormat = DateFormat.getDateInstance();
        String strDate = mDateFormat.format(date);
        infoDate_date = strDate;
        datetext.setText(strDate);
        dateinfo = true;
    }
    private void updateTime() {
        Date date = calendar.getTime();
        DateFormat mDateFormat = DateFormat.getTimeInstance();
        String strTime = mDateFormat.format(date);
        infoDate_time = strTime;
        timetext.setText(strTime);
        timeinfo = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        String latlng;
        if(resultCode==RESULT_OK) {
//           액티비티가 정상적으로 종료되었을 경우
//           InformationInput에서 호출한 경우에만 처리합니다.
            latlng = data.getStringExtra("GPS_DATA");

            Log.d("getlatlng", "gps_data: " + latlng);

            String gps_result[] = latlng.split(",");
            winfo.setYCoord(Double.parseDouble(gps_result[0]));
            winfo.setXCoord(Double.parseDouble(gps_result[1]));
            gpsinfo = true;

            //gps image가 빨간색으로 바뀌게 하는 코드
            gpsimg.setImageResource(R.drawable.gps2);
        } else if(resultCode==RESULT_CANCELED) {
            gpsinfo = false;
        }
    }

    private class SendWarningInfo extends AsyncTask<WarningInfo, Void, String> {

        StringBuilder result = new StringBuilder();
        String response;
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(WarningInfo... param) {
            Properties prop = new Properties();

            InputStream is;
            BufferedReader br;
            OutputStream os;

            HttpURLConnection conn;

            prop.setProperty(Constant.PARAMETER.UserIdx, Integer.toString(param[0].getUsrIdx()));
            prop.setProperty(Constant.PARAMETER.Title, param[0].getTitle());
            prop.setProperty(Constant.PARAMETER.Content, param[0].getContent());
            prop.setProperty(Constant.PARAMETER.Lng, Double.toString(param[0].getXCoord()));
            prop.setProperty(Constant.PARAMETER.Lat, Double.toString(param[0].getYCoord()));
            prop.setProperty(Constant.PARAMETER.Datetime, param[0].getDatetime());
            prop.setProperty(Constant.PARAMETER.Category, Integer.toString(param[0].getCategory()));

            String encodedString = EncodeString(prop);

            URL url = null;

            try {
                url = new URL(Constant.URL.Base + Constant.URL.WarningInfoRegist);
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

            if( !(param == null) && param.equals("regist!")){
                Toast.makeText(InsertWarningInfoActivity.this, "성공적으로 입력되었습니다!", Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(InsertWarningInfoActivity.this, "다시 시도해주세요!", Toast.LENGTH_LONG).show();
            }

        }
    }

    private class UpdateWarningInfo extends AsyncTask<WarningInfo, Void, String> {

        StringBuilder result = new StringBuilder();
        String response;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(WarningInfo... param) {
            Properties prop = new Properties();

            InputStream is;
            BufferedReader br;
            OutputStream os;

            HttpURLConnection conn;

            prop.setProperty(Constant.PARAMETER.UserIdx, Integer.toString(param[0].getUsrIdx()));
            prop.setProperty(Constant.PARAMETER.InfoIdx, Integer.toString(param[0].getIdx()));
            prop.setProperty(Constant.PARAMETER.Title, param[0].getTitle());
            prop.setProperty(Constant.PARAMETER.Content, param[0].getContent());
            prop.setProperty(Constant.PARAMETER.Lng, Double.toString(param[0].getXCoord()));
            prop.setProperty(Constant.PARAMETER.Lat, Double.toString(param[0].getYCoord()));
            prop.setProperty(Constant.PARAMETER.Datetime, param[0].getDatetime());
            prop.setProperty(Constant.PARAMETER.Category, Integer.toString(param[0].getCategory()));

            String encodedString = EncodeString(prop);

            URL url = null;

            try {
                url = new URL(Constant.URL.Base + Constant.URL.WarningInfoUpdate);
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

            if( !(param == null) && param.equals("success")){
                Toast.makeText(InsertWarningInfoActivity.this, "성공적으로 수정되었습니다!", Toast.LENGTH_LONG).show();

                Intent lIntent = new Intent();
                lIntent.putExtra("WINFOIDX", Integer.toString(winfo.getIdx()));
                setResult(RESULT_OK, lIntent);
                finish();

            } else {
                Toast.makeText(InsertWarningInfoActivity.this, "다시 시도해주세요!", Toast.LENGTH_LONG).show();
            }

        }
    }
}
