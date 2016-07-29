package capston.stol.dangerousplace.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import capston.stol.dangerousplace.R;
import capston.stol.dangerousplace.ShowWarningInfoActivity;
import capston.stol.dangerousplace.bean.WarningInfo;
import capston.stol.dangerousplace.bean.WarningInfoList;
import capston.stol.dangerousplace.parser.WarningInfoListParser;

import static capston.stol.dangerousplace.util.EncodeString.EncodeString;

/**
 * Created by Dasol on 2016-07-03.
 */
public class NMapCalloutListOverlay extends Activity {

    private WarningInfoList winfolist = null;

    private ScrollView mScroll;
    private ListView list1;
    private ListView list2;
    private ListView list3;
    private ListView list4;
    private CustomAdapter adapter1;
    private CustomAdapter adapter2;
    private CustomAdapter adapter3;
    private CustomAdapter adapter4;
    ArrayList<String> ctg1;
    ArrayList<String> ctg2;
    ArrayList<String> ctg3;
    ArrayList<String> ctg4;

    private int contextid;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        mScroll = (ScrollView)findViewById(R.id.scroll_view);

        // list 1 = 성범죄
        // list 2 = 폭력
        // list 3 = 도난
        // list 4 = 기타
        list1 = (ListView)this.findViewById(R.id.list1);
        list2 = (ListView)this.findViewById(R.id.list2);
        list3 = (ListView)this.findViewById(R.id.list3);
        list4 = (ListView)this.findViewById(R.id.list4);

        Intent intent = getIntent();
        ArrayList<String> temp = intent.getStringArrayListExtra("CONTENT");

        String contents = "";

        for(int i =0;i<temp.size();i++) {
            contents += temp.get(i) + ",";
        }

        try {
            new SeeWarningInfo().execute(contents);
        }catch (Exception e) {
            e.printStackTrace();
        }


        // 각각 list들의 스크롤바를 이용할 수 있도록
        list1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScroll.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        list2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScroll.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        list3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScroll.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        list4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScroll.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        list1.setOnItemClickListener(onClickListItem);
        list2.setOnItemClickListener(onClickListItem2);
        list3.setOnItemClickListener(onClickListItem3);
        list4.setOnItemClickListener(onClickListItem4);
    }

    // customadapter 관련 클래스
    private class CustomAdapter extends ArrayAdapter<String> {
        private ArrayList<String> items;

        public CustomAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
            super(context, textViewResourceId, objects);
            this.items = objects;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if( v == null ) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.activity_contents, null);
            }

            ImageView imageView = (ImageView)v.findViewById(R.id.imageView);
            TextView textView = (TextView)v.findViewById(R.id.textView);

            textView.setText(items.get(position));

            return v;
        }
    }

    // 아이템 터치 이벤트
    private OnItemClickListener onClickListItem = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            // 이벤트 발생 시 해당 아이템 위치의 텍스트를 출력
            String temp = adapter1.getItem(position);
            contextid = position;
            for(int i = 0; i < winfolist.getTotal(); i ++ ) {
                WarningInfo winfo = winfolist.getWarningInfoArrayList().get(i);

                if(temp.equals(winfo.getTitle())) {
                    Intent intent = new Intent(NMapCalloutListOverlay.this, ShowWarningInfoActivity.class);
                    intent.putExtra("WINFOIDX", winfo.getIdx() + "");
                    startActivityForResult(intent, 2);
                }
            }
        }
    };

    private OnItemClickListener onClickListItem2 = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            // 이벤트 발생 시 해당 아이템 위치의 텍스트를 출력
            String temp = adapter2.getItem(position);
            contextid = position;
            for(int i = 0; i < winfolist.getTotal(); i ++ ) {
                WarningInfo winfo = winfolist.getWarningInfoArrayList().get(i);

                if(temp.equals(winfo.getTitle())) {
                    Intent intent = new Intent(NMapCalloutListOverlay.this, ShowWarningInfoActivity.class);
                    intent.putExtra("WINFOIDX", winfo.getIdx() + "");
                    startActivityForResult(intent, 2);
                }
            }
        }
    };

    private OnItemClickListener onClickListItem3 = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            // 이벤트 발생 시 해당 아이템 위치의 텍스트를 출력
            String temp = adapter3.getItem(position);
            contextid = position;
            for(int i = 0; i < winfolist.getTotal(); i ++ ) {
                WarningInfo winfo = winfolist.getWarningInfoArrayList().get(i);

                if(temp.equals(winfo.getTitle())) {
                    Intent intent = new Intent(NMapCalloutListOverlay.this, ShowWarningInfoActivity.class);
                    intent.putExtra("WINFOIDX", winfo.getIdx() + "");
                    startActivityForResult(intent, 2);
                }
            }
        }
    };

    private OnItemClickListener onClickListItem4 = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            // 이벤트 발생 시 해당 아이템 위치의 텍스트를 출력
            String temp = adapter4.getItem(position);
            contextid = position;
            for(int i = 0; i < winfolist.getTotal(); i ++ ) {
                WarningInfo winfo = winfolist.getWarningInfoArrayList().get(i);

                if(temp.equals(winfo.getTitle())) {
                    Intent intent = new Intent(NMapCalloutListOverlay.this, ShowWarningInfoActivity.class);
                    intent.putExtra("WINFOIDX", winfo.getIdx() + "");
                    startActivityForResult(intent, 2);
                }
            }
        }
    };

    //AsyncTask for getWInfo and UI Handling
    private class SeeWarningInfo extends AsyncTask<String, Void, WarningInfoList> {

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

            prop.setProperty(Constant.PARAMETER.InfoIdx, param[0]);

            String encodedString = EncodeString(prop);

            URL url = null;

            try {
                url = new URL(Constant.URL.Base + Constant.URL.WarningInfoList);
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

                    //받은 데이터 jason 파싱
                    WarningInfoListParser parser = new WarningInfoListParser(JSON);
                    winfolist = parser.parse();

                } else {
                    Log.e("dasol parser", "fail");
                    return null;
                }
                conn.disconnect();

            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return winfolist;
        }

        @Override
        protected void onPostExecute(WarningInfoList wd) {
            int category;

            // ctg 1 = 성범죄
            // ctg 2 = 폭력
            // ctg 3 = 도난
            // ctg 4 = 기타
            ctg1 = new ArrayList<>();
            ctg2 = new ArrayList<>();
            ctg3 = new ArrayList<>();
            ctg4 = new ArrayList<>();

            for(int i=0;i<wd.getTotal();i++){
                WarningInfo winfo = wd.getWarningInfoArrayList().get(i);
                category = winfo.getCategory();

                // 카테고리에 따른 list 추가
                switch(category) {
                    case 1:
                        ctg4.add(winfo.getTitle());
                        break;
                    case 2:
                        ctg1.add(winfo.getTitle());
                        break;
                    case 3:
                        ctg2.add(winfo.getTitle());
                        break;
                    case 4:
                        ctg3.add(winfo.getTitle());
                        break;
                }
            }

            adapter1 = new CustomAdapter(NMapCalloutListOverlay.this, 0, ctg1);
            list1.setAdapter(adapter1);

            adapter2 = new CustomAdapter(NMapCalloutListOverlay.this, 0, ctg2);
            list2.setAdapter(adapter2);

            adapter3 = new CustomAdapter(NMapCalloutListOverlay.this, 0, ctg3);
            list3.setAdapter(adapter3);

            adapter4 = new CustomAdapter(NMapCalloutListOverlay.this, 0, ctg4);
            list4.setAdapter(adapter4);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            int flag = Integer.parseInt(data.getStringExtra("flag"));
            if(requestCode==2) {
                if(flag==1){ // 데이터를 삭제했을 경우 flag = 1을 받아와서 실행
                    int categorytemp = Integer.parseInt(data.getStringExtra("category"));
                    int idxtemp = Integer.parseInt(data.getStringExtra("contentidx"));

                    switch(categorytemp) {
                        case 1:
                            for (int i = 0; i < winfolist.getTotal(); i++) {
                                if (idxtemp == winfolist.getWarningInfoArrayList().get(i).getIdx()) {
                                    if (contextid > -1) {
                                        ctg4.remove(contextid);
                                    }
                                    adapter4.notifyDataSetChanged();
                                }
                            }
                            break;
                        case 2:
                            for (int i = 0; i < winfolist.getTotal(); i++) {
                                if (idxtemp == winfolist.getWarningInfoArrayList().get(i).getIdx()) {
                                    if (contextid > -1) {
                                        ctg1.remove(contextid);
                                    }
                                    adapter1.notifyDataSetChanged();
                                }
                            }
                            break;
                        case 3:
                            for (int i = 0; i < winfolist.getTotal(); i++) {
                                if (idxtemp == winfolist.getWarningInfoArrayList().get(i).getIdx()) {
                                    if (contextid > -1) {
                                        ctg2.remove(contextid);
                                    }
                                    adapter2.notifyDataSetChanged();
                                }
                            }
                            break;
                        case 4:
                            for (int i = 0; i < winfolist.getTotal(); i++) {
                                if (idxtemp == winfolist.getWarningInfoArrayList().get(i).getIdx()) {
                                    if (contextid > -1) {
                                        ctg3.remove(contextid);
                                    }
                                    adapter3.notifyDataSetChanged();
                                }
                            }
                            break;
                    }
                }
                else { // 데이터를 수정했을 경우 flag = 0을 받아와서 실행
                    int categorytemp = Integer.parseInt(data.getStringExtra("category"));
                    int idxtemp = Integer.parseInt(data.getStringExtra("contentidx"));
                    String modititle = data.getStringExtra("title");
                    switch(categorytemp) {
                        case 1:
                            for (int i = 0; i < winfolist.getTotal(); i++) {
                                if (idxtemp == winfolist.getWarningInfoArrayList().get(i).getIdx()) {
                                    ctg4.set(contextid, modititle);
                                    adapter4.notifyDataSetChanged();
                                }
                            }
                            break;
                        case 2:
                            for (int i = 0; i < winfolist.getTotal(); i++) {
                                if (idxtemp == winfolist.getWarningInfoArrayList().get(i).getIdx()) {
                                    ctg1.set(contextid, modititle);
                                    adapter1.notifyDataSetChanged();
                                }
                            }
                            break;
                        case 3:
                            for (int i = 0; i < winfolist.getTotal(); i++) {
                                if (idxtemp == winfolist.getWarningInfoArrayList().get(i).getIdx()) {
                                    ctg2.set(contextid, modititle);
                                    adapter2.notifyDataSetChanged();
                                }
                            }
                            break;
                        case 4:
                            for (int i = 0; i < winfolist.getTotal(); i++) {
                                if (idxtemp == winfolist.getWarningInfoArrayList().get(i).getIdx()) {
                                    ctg3.set(contextid, modititle);
                                    adapter3.notifyDataSetChanged();
                                }
                            }
                            break;
                    }
                }
            }
        }
        else if(resultCode==RESULT_CANCELED) {
            //갱신이 필요없을때.
        }
    }
}
