package capston.stol.dangerousplace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
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

import capston.stol.dangerousplace.bean.MyList;
import capston.stol.dangerousplace.bean.WarningInfoList;
import capston.stol.dangerousplace.parser.WarningInfoListParser;
import capston.stol.dangerousplace.util.Constant;

import static capston.stol.dangerousplace.util.EncodeString.EncodeString;

/**
 * Created by sbpark1 on 2016-07-02.
 */
public class MyWarningInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private WarningInfoList winfoList = null;

    private SharedPreferences setting;
    private String myidx;

    private Button BtnSelect;
    private ListView myListView;
    private ListViewAdapter myAdapter;

    private SparseBooleanArray checkArr;

    private boolean cbFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_warning_info);

        setting = getSharedPreferences("setting", 0);
        myidx = setting.getString("UserIdx", null);


        //리스트 뷰 생성 및 커스텀한 어댑터 설정
        myListView = (ListView)findViewById(R.id.myWarningInfo);
        myAdapter = new ListViewAdapter(this);

        //리스트 선택불가능 하게 함
        myListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
        myListView.setAdapter(myAdapter);

        //터치리스너, 길게 터치했을때 리스너 설정
        myListView.setOnItemClickListener(onClickListItem);
        myListView.setOnItemLongClickListener(onItemLongClickListener);

        //버튼 생성후 롱 클릭 시 나타나기위해 우선 숨김
        BtnSelect = (Button) findViewById(R.id.select_btn);
        BtnSelect.setVisibility(View.GONE);
        BtnSelect.setOnClickListener(this);


        try {
            new MyWarningInfo().execute(myidx);
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.select_btn:
                checkArr = myListView.getCheckedItemPositions();

                String idxList = "";
                if(checkArr.size() !=0){
                    for(int i=myListView.getCount() -1; i>-1;i--){
                        if(checkArr.get(i)){
                            idxList +=winfoList.getWarningInfoArrayList().get(i).getIdx()+",";
                        }
                    }
                }
                //DB에 삭제
                new MultipleDelete().execute(idxList);

//                if(checkArr.size() !=0){
//                    for(int i=myListView.getCount() - 1; i >-1;i--){
//                        if(checkArr.get(i)) {
//                            myAdapter.remove(i);
//                        }
//                    }
//                }
//                //안보이도록 함
//                cbFlag = false;
//                myAdapter.setCheckBoxState(cbFlag);
//                myListView.setChoiceMode(ListView.CHOICE_MODE_NONE);

        }
    }


    private class ViewHolder{
        public CheckBox myCheckBox;
        public TextView myWinfo;
        public TextView myWinfoDate;
    }

    private class ListViewAdapter extends BaseAdapter{
        private Context mContext = null;
        //        private ArrayList<String> wTitleList = new ArrayList<String>();
        private ArrayList<MyList> myList = new ArrayList<MyList>();
        private boolean mCheckBoxState = false;

        //check box Visible Gone 정함
        public void setCheckBoxState(boolean pState){
            mCheckBoxState = pState;
            notifyDataSetChanged();
        }
        public ListViewAdapter(Context mContext){
            super();
            this.mContext = mContext;
        }

        //        public void addItem(String wTitle){
//            wTitleList.add(wTitle);
//        }
        public void addItem(MyList myData){
            myList.add(myData);
        }
        //        public void remove(int position){
//            wTitleList.remove(position);
//            dataChange();
//        }
        public void remove(int position){
            myList.remove(position);
            dataChange();
        }
        public void dataChange(){
            myAdapter.notifyDataSetChanged();
        }
        @Override
        public int getCount(){
            return myList.size();
        }
//        public int getCount(){
//            return wTitleList.size();
//        }

        @Override
        public Object getItem(int position){
            return myList.get(position);
        }
//        public Object getItem(int position){
//            return wTitleList.get(position);
//        }

        @Override
        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            //list를 띄움
            ViewHolder holder;

            if(convertView == null){
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.mylist_custom, null);

                holder.myCheckBox = (CheckBox) convertView.findViewById(R.id.my_winfo_checkbox);
                holder.myWinfo = (TextView) convertView.findViewById(R.id.my_winfo_list);
                holder.myWinfoDate = (TextView) convertView.findViewById(R.id.my_winfo_date);
                convertView.setTag(holder);

            } else{
                holder = (ViewHolder) convertView.getTag();
            }
//            String wTitle = wTitleList.get(position);
            MyList myData = myList.get(position);
            holder.myWinfo.setText(myData.wTitle);
            holder.myWinfoDate.setText(myData.wDate);

            if (!mCheckBoxState) {
                holder.myCheckBox.setVisibility(View.GONE);
            } else{
                holder.myCheckBox.setVisibility(View.VISIBLE);
            }
            holder.myCheckBox.setChecked(false);
            holder.myCheckBox.setFocusable(false);
            holder.myCheckBox.setClickable(false);
            holder.myCheckBox.setChecked(((ListView)parent).isItemChecked(position));
            return convertView;
        }
    }

    // 아이템 터치 이벤트
    private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if(!cbFlag) {
                int listId = (int) myAdapter.getItemId(arg2);
                Intent myWinfo = new Intent(MyWarningInfoActivity.this, ShowWarningInfoActivity.class);
                myWinfo.putExtra("WINFOIDX", winfoList.getWarningInfoArrayList().get(listId).getIdx() + "");
                startActivity(myWinfo);
            } else{
                myAdapter.setCheckBoxState(cbFlag);
            }
        }
    };

    //아이템 길게 터치했을 때 이벤트
    private AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            //
            cbFlag = true;
            myListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            myAdapter.setCheckBoxState(cbFlag);
            BtnSelect.setVisibility(View.VISIBLE);
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        if(cbFlag){
            cbFlag = false;
            myAdapter.setCheckBoxState(cbFlag);
            myListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
            BtnSelect.setVisibility(View.GONE);

            myListView.clearChoices();
        } else{
            super.onBackPressed();
        }
    }


    //warning data 불러오기
    private class MyWarningInfo extends AsyncTask<String, Void, WarningInfoList> {

        StringBuilder result = new StringBuilder();
        String response;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected WarningInfoList doInBackground(String... param) {
            Properties prop = new Properties();

            BufferedReader br;
            OutputStream os;

            HttpURLConnection conn;

            prop.setProperty(Constant.PARAMETER.MyIdx, String.valueOf(Integer.parseInt(param[0])));

            String encodedString = EncodeString(prop);

            URL url = null;

            try {
                url = new URL(Constant.URL.Base + Constant.URL.MyWarningInfo);
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

                    br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "euc-kr"));

                    String line = "";
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }

                    String JSON = result.toString();

                    WarningInfoListParser parser = new WarningInfoListParser(JSON);
                    winfoList = parser.parse();
                    Log.d("result json:", JSON);

                } else {
                    return null;
                }
                conn.disconnect();


            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return winfoList;
        }

        @Override
        protected void onPostExecute(WarningInfoList wd) {
            for(int i=0; i<wd.getTotal(); i++) {
                myAdapter.addItem(new MyList(wd.getWarningInfoArrayList().get(i).getTitle(),wd.getWarningInfoArrayList().get(i).getDatetime().split(" ")[0]));
                myAdapter.notifyDataSetChanged();
            }
        }
    }

    //다중 삭제
    private class MultipleDelete extends AsyncTask<String, Void, String> {

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

            prop.setProperty(Constant.PARAMETER.InfoIdx, param[0]);

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

            if( !(param == null) && param.equals("success")){
                Toast.makeText(MyWarningInfoActivity.this, "삭제완료되었습니다.", Toast.LENGTH_LONG).show();
                if(checkArr.size() !=0){
                    for(int i=myListView.getCount() - 1; i >-1;i--){
                        if(checkArr.get(i)) {
                            myAdapter.remove(i);
                            winfoList.getWarningInfoArrayList().remove(i);
                        }
                    }
                }
                //안보이도록 함
                cbFlag = false;
                myAdapter.setCheckBoxState(cbFlag);
                myListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
//                finish();
            } else if(!(param == null) && param.equals("null!!")){
                Toast.makeText(MyWarningInfoActivity.this, "하나 이상 선택해주세요.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MyWarningInfoActivity.this, "다시 시도해주세요!", Toast.LENGTH_LONG).show();
            }

        }
    }
}


