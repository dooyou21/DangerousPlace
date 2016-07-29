package capston.stol.dangerousplace;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
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
import java.util.ArrayList;
import java.util.Properties;

import capston.stol.dangerousplace.util.Constant;

import static capston.stol.dangerousplace.util.EncodeString.EncodeString;

/**
 * Created by dasol on 2016-05-09.
 * 마이 페이지 화면 구현
 */
public class MyPageActivity extends Activity implements View.OnClickListener{

    private TextView mylist, emgcontact, most_used_way, deleteme;

    private SharedPreferences setting;
    private SharedPreferences.Editor editor;

    private String myidx;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super .onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        setting = getSharedPreferences("setting", 0);
        myidx = setting.getString("UserIdx", null);

        mylist = (TextView)findViewById(R.id.mylist);
        emgcontact = (TextView)findViewById(R.id.emgcontact);
        most_used_way = (TextView)findViewById(R.id.most_used_way);
        deleteme = (TextView)findViewById(R.id.deleteme);

        mylist.setOnClickListener(this);
        emgcontact.setOnClickListener(this);
        most_used_way.setOnClickListener(this);
        deleteme.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch(v.getId()){
            case R.id.mylist:
//                Toast.makeText(MyPageActivity.this, "mylist", Toast.LENGTH_LONG).show();
                Intent myListIntent = new Intent(this, MyWarningInfoActivity.class);
                startActivity(myListIntent);
                break;
            case R.id.emgcontact:
                Intent intent = new Intent(this, EmgContactActivity.class);
                startActivity(intent);
                break;
            case R.id.most_used_way:
                Intent lIntent = new Intent(this, SetPointForPushActivity.class);
                startActivity(lIntent);
                break;
            case R.id.deleteme:
                final CharSequence[] items = {"탈퇴","탈퇴 및 모든데이터 삭제"};

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MyPageActivity.this);

                alertDialogBuilder.setTitle("회원 탈퇴");
                alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (id == 0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MyPageActivity.this);
                            builder
                                    .setTitle("탈퇴 확인")
                                    .setMessage("정말로 탈퇴하시겠습니까?")
                                    .setCancelable(true)
                                    .setPositiveButton("탈퇴", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            new DeleteUser().execute(myidx, String.valueOf(false));
                                        }
                                    })
                                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog dl = builder.create();
                            dl.show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MyPageActivity.this);
                            builder
                                    .setTitle("탈퇴 및 모든데이터 삭제 확인")
                                    .setMessage("정말로 삭제하시겠습니까?")
                                    .setCancelable(true)
                                    .setPositiveButton("탈퇴 및 데이터 제거", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            new DeleteUser().execute(myidx, String.valueOf(true));
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
                //push test
//                Intent pushintent = new Intent(this, GcmActivity.class);
//                startActivity(pushintent);
                break;
        }
    }

    private class DeleteUser extends AsyncTask<String, Void, String> {

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

            prop.setProperty(Constant.PARAMETER.MyIdx, param[0]);

            String encodedString = EncodeString(prop);

            URL url = null;

            try {
                url = new URL(Constant.URL.Base + Constant.URL.UserDropOut);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Accept", "application/json");

                os = conn.getOutputStream();
                os.write(encodedString.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("respCode", responseCode + "");

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    is = new BufferedInputStream(conn.getInputStream());
                    br = new BufferedReader(new InputStreamReader(is));

                    String line = "";
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }

                    is.close();

                    response = result.toString();

                    Log.d("success_or_fail", response);

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

//            if (pDialog!=null&&pDialog.isShowing()){
//                pDialog.dismiss();
//            }

            if( !(param == null) && param.equals("success")){
                Toast.makeText(MyPageActivity.this, "탈퇴완료되었습니다.", Toast.LENGTH_LONG).show();

                editor = setting.edit();
                editor.clear();
                editor.commit();

                //어플종료
                moveTaskToBack(true);
                ArrayList<Activity> actList = new ArrayList<Activity>();
                for(int i = 0;i<actList.size();i++)
                    actList.get(i).finish();
                android.os.Process.killProcess(android.os.Process.myPid());

            } else {
                Toast.makeText(MyPageActivity.this, "다시 시도해주세요!", Toast.LENGTH_LONG).show();
            }

        }
    }
}