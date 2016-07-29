package capston.stol.dangerousplace;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
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

import capston.stol.dangerousplace.bean.WarningInfo;
import capston.stol.dangerousplace.parser.WarningInfoDetailParser;
import capston.stol.dangerousplace.util.Constant;
import capston.stol.dangerousplace.util.TimeForUser;

import static capston.stol.dangerousplace.util.EncodeString.EncodeString;

public class ShowWarningInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private String w_info_idx;
    private WarningInfo winfo = null;
    private Button btnMore, btnDanger, btnOK;

    private SharedPreferences setting;
    private String myidx;

    private boolean susang = false;
    private boolean changed = false;


    TextView tvTitle, tvCategory, tvContents, tvDate, tvTime;

    private String[] CATEGORY = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CATEGORY = getResources().getStringArray(R.array.category1);

        setting = getSharedPreferences("setting", 0);
        myidx = setting.getString("UserIdx", null);

        Intent sIntent = getIntent();
        //contents의 idx 받아옴 0은 받아오기 실패 했을 때의 기본 값
        w_info_idx = sIntent.getStringExtra("WINFOIDX");

//        Log.e("SBtest", "WInfoIdx: " + w_info_idx);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_warning_info);

        tvTitle = (TextView) this.findViewById(R.id.tvtitle);
        tvContents = (TextView) this.findViewById(R.id.tvcontents);
        tvDate = (TextView) this.findViewById(R.id.tvdate);
        tvTime = (TextView) this.findViewById(R.id.tvtime);
        tvCategory = (TextView) this.findViewById(R.id.tvCategory);

        btnMore = (Button) findViewById(R.id.more_btn);
        btnMore.setOnClickListener(this);
        btnDanger = (Button) findViewById(R.id.btnDanger);
        btnDanger.setOnClickListener(this);
        btnOK = (Button) findViewById(R.id.btnOK);
        btnOK.setOnClickListener(this);

        try {
            new SeeWarningInfo().execute(w_info_idx, myidx);
            //Log.e("SBtest", "dTitle1: " + wd.getWarningInfoArrayList().get(0).getTitle());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
        //좋아요버튼 onclickListener
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDanger:
                if(susang){//이미 선택되어있을 때
                    susang = false;
                    btnDanger.setTextColor(Color.WHITE);
                }else{//아직 선택되어있지 않을 때
                    susang = true;
                    btnDanger.setTextColor(Color.CYAN);
                }
                break;
            case R.id.more_btn:
                final CharSequence[] items = {"수정", "삭제"};

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ShowWarningInfoActivity.this);

                alertDialogBuilder.setTitle("수정 혹은 삭제");
                alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (id == 0){
//                            Toast.makeText(ShowWarningInfoActivity.this, items[id] + "selected", Toast.LENGTH_SHORT).show();
                            //수정
                            Intent lIntent = new Intent(ShowWarningInfoActivity.this, InsertWarningInfoActivity.class);

                            lIntent.putExtra("USRIDX", myidx);
                            lIntent.putExtra("INSERTORUPDATE","update");
                            lIntent.putExtra("TITLE", winfo.getTitle());
                            lIntent.putExtra("CONTENT", winfo.getContent());
                            lIntent.putExtra("DATE", winfo.getDatetime());
                            lIntent.putExtra("XCOORD", winfo.getXCoord());
                            lIntent.putExtra("YCOORD", winfo.getYCoord());
                            lIntent.putExtra("WINDEX", winfo.getIdx());
                            lIntent.putExtra("CATEGORY", winfo.getCategory());

//                            startActivity(lIntent);
                            startActivityForResult(lIntent, 1);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ShowWarningInfoActivity.this);
                            builder
                                .setTitle("삭제 확인")
                                .setMessage("정말로 삭제하시겠습니까?")
                                .setCancelable(true)
                                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //삭제로직구현
                                        new DeleteWarningInfo().execute(Integer.parseInt(w_info_idx));
                                    }
                                })
                                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.cancel();
                                    }
                                });
                            AlertDialog dl = builder.create();
                            dl.show();
                        }
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDIalog = alertDialogBuilder.create();
                alertDIalog.show();

                break;
            case R.id.btnOK:
                if(winfo.isCheckWCount() != susang){
                    try {
                        new UpdateSuSangHae().execute(w_info_idx, myidx);
//                        Log.e("SBtest", "susangupdate");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else {
                    if(changed){
                        // ListOverlay 에서 글을 선택할 경우
                        // 수정 / 삭제 사항을 list로 주기 위해 intent 생성 후 값 넘김
                        Intent intent = getIntent();
                        intent.putExtra("category", Integer.toString(winfo.getCategory()));
                        intent.putExtra("contentidx", Integer.toString(winfo.getIdx()));
                        intent.putExtra("title", winfo.getTitle());
                        intent.putExtra("flag", Integer.toString(0));
                        setResult(RESULT_OK);
                        finish();
                    }
                    else { setResult(RESULT_CANCELED); }
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK) {//수정되었을 경우 InsertWarningInfo에서 winfo의 idx를 intent로 넘겨준다

            w_info_idx = data.getStringExtra("WINFOIDX");

            try {
                new SeeWarningInfo().execute(w_info_idx, myidx);
                //Log.e("SBtest", "dTitle1: " + wd.getWarningInfoArrayList().get(0).getTitle());
            }catch (Exception e) {
                e.printStackTrace();
            }

        } else if(resultCode==RESULT_CANCELED) {

        }
    }

    //AsyncTask for getWInfo and UI Handling
    private class SeeWarningInfo extends AsyncTask<String, Void, WarningInfo> {

        StringBuilder result = new StringBuilder();
        String response;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected WarningInfo doInBackground(String... param) {
            Properties prop = new Properties();

            BufferedReader br;
            OutputStream os;

            HttpURLConnection conn;

            prop.setProperty(Constant.PARAMETER.InfoIdx, String.valueOf(Integer.parseInt(param[0])));
            prop.setProperty(Constant.PARAMETER.MyIdx, String.valueOf(Integer.parseInt(param[1])));

            String encodedString = EncodeString(prop);

            URL url = null;

            try {
                url = new URL(Constant.URL.Base + Constant.URL.WarningInfoDetail);
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
//                Log.d("respCode", responseCode + "");

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "euc-kr"));

                    String line = "";
                    while ((line = br.readLine()) != null) {
                        result.append(line);
//                        Log.e("inwhile", "asas");
                    }

                    String JSON = result.toString();

                    WarningInfoDetailParser parser = new WarningInfoDetailParser(JSON);
                    winfo = parser.parse();
//                    Log.d("result json:", JSON);

                } else {
                    return null;
                }
                conn.disconnect();


            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return winfo;
        }

        @Override
        protected void onPostExecute(WarningInfo wd) {

            String[] dateSplit = wd.getDatetime().split(" ");

            tvTitle.setText(wd.getTitle());
            tvCategory.setText(CATEGORY[wd.getCategory()]);
            tvContents.setText(wd.getContent());
            tvDate.setText(dateSplit[0]);
            TimeForUser time = new TimeForUser(dateSplit[1]);
            tvTime.setText(time.getTime());

            btnDanger.setText("수상해 `ㅁ\'" + "(" + wd.getWCount() + ")");
            susang = wd.isCheckWCount();

            if(susang){//이미 선택되어있을 때
                btnDanger.setTextColor(Color.CYAN);
            }else{//아직 선택되어있지 않을 때
                btnDanger.setTextColor(Color.WHITE);
            }

            if(Integer.parseInt(myidx) != winfo.getUsrIdx()) {
                btnMore.setVisibility(View.GONE);
            }


        }
    }


    //AsyngTask for insert SuSangHae
    private class UpdateSuSangHae extends AsyncTask<String, Void, String> {

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

            prop.setProperty(Constant.PARAMETER.WInfoIdx, String.valueOf(Integer.parseInt(param[0])));
            prop.setProperty(Constant.PARAMETER.WMyIdx, String.valueOf(Integer.parseInt(param[1])));

            String encodedString = EncodeString(prop);

            URL url = null;

            try {
                url = new URL(Constant.URL.Base + Constant.URL.SuSangHaeUpdate);
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
//                Log.d("respCode", responseCode + "");

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

//                    Log.d("result json:", response);

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
                int new_w_count = Integer.parseInt(param);
                Toast.makeText(ShowWarningInfoActivity.this, "수정되었습니다!" + new_w_count, Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(ShowWarningInfoActivity.this, "다시 시도해주세요!", Toast.LENGTH_LONG).show();
            }

        }
    }


    private class DeleteWarningInfo extends AsyncTask<Integer, Void, String> {

        StringBuilder result = new StringBuilder();
        String response;
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... param) {
            Properties prop = new Properties();

            InputStream is;
            BufferedReader br;
            OutputStream os;

            HttpURLConnection conn;

            prop.setProperty(Constant.PARAMETER.InfoIdx, Integer.toString(param[0]));

            String encodedString = EncodeString(prop);

            URL url = null;

            try {
                url = new URL(Constant.URL.Base + Constant.URL.WarningInfoDelete);
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
//                Log.d("respCode", responseCode + "");

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

//                    Log.d("success_or_fail", response);

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
                Toast.makeText(ShowWarningInfoActivity.this, "삭제완료되었습니다.", Toast.LENGTH_LONG).show();
                // ListOverlay 에서 글을 선택할 경우
                // 수정 / 삭제 사항을 list로 주기 위해 intent 생성 후 값 넘김
                Intent intent = getIntent();
                intent.putExtra("category", Integer.toString(winfo.getCategory()));
                intent.putExtra("contentidx",Integer.toString(winfo.getIdx()));
                intent.putExtra("flag", Integer.toString(1));
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(ShowWarningInfoActivity.this, "다시 시도해주세요!", Toast.LENGTH_LONG).show();
            }

        }
    }

}
