package capston.stol.dangerousplace;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.MultipleCategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import capston.stol.dangerousplace.bean.WarningInfoList;
import capston.stol.dangerousplace.parser.WarningInfoListParser;
import capston.stol.dangerousplace.util.Constant;

import static capston.stol.dangerousplace.util.EncodeString.EncodeString;

/**
 * Created by sbpark1 on 2016-07-08.
 */
public class StatisticalChartActivity extends AppCompatActivity {

    XYMultipleSeriesRenderer categoryRenderer,renderer,barCharRenderer;
    List<double[]> values = new ArrayList<double[]>();
    WarningInfoList WInfoList;
    Button tabset1,tabset2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_charts);
        setContentView(R.layout.activity_image_tab);
        final LinearLayout tab2 = (LinearLayout)findViewById(R.id.tab_time);
        tab2.setVisibility(LinearLayout.GONE);
        tabset1 = (Button)findViewById(R.id.btn1);
        tabset2 = (Button)findViewById(R.id.btn2);

        tabset1.setBackgroundColor(Color.WHITE);
        setTabAction();
        new getWarningInfoListAsyncTask().execute("0", "0");


    }
    private void setTabAction(){
        final LinearLayout tab1 = (LinearLayout)findViewById(R.id.tab_cate);
        final LinearLayout tab2 = (LinearLayout)findViewById(R.id.tab_time);

        Button btn1 = (Button)findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabset1.setBackgroundColor(Color.WHITE);
                tabset2.setBackgroundColor(Color.LTGRAY);
                tab1.setVisibility(LinearLayout.VISIBLE);
                tab2.setVisibility(LinearLayout.GONE);
            }
        });

        Button btn2 = (Button)findViewById(R.id.btn2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barChartForTime();
                tabset1.setBackgroundColor(Color.LTGRAY);
                tabset2.setBackgroundColor(Color.WHITE);
                tab1.setVisibility(LinearLayout.GONE);
                tab2.setVisibility(LinearLayout.VISIBLE);
            }
        });
    }



    public void CategoryChart(){
        categoryRenderer= new XYMultipleSeriesRenderer();

        categoryRenderer.setChartTitle("카테고리 별 글 빈도");
        categoryRenderer.setChartTitleTextSize(35);
//        Log.e("ssiba", "위"+values.get(0)[0] + "");

        MultipleCategorySeries series = new MultipleCategorySeries("카테고리");
        series.add("카테고리", new String[]{"성범죄", "폭력", "도난", "기타"}, values.get(0));


        categoryRenderer.setLabelsColor(Color.parseColor("#252533"));
        categoryRenderer.setLabelsTextSize(75);
        categoryRenderer.setLegendTextSize(75);
        categoryRenderer.setShowLegend(false);

        int[] colors = new int[]{Color.parseColor("#FFA9B0"), Color.parseColor("#FFE2A5"), Color.parseColor("#94E0E0"), Color.parseColor("#A295F2")};

        for(int color : colors){
            SimpleSeriesRenderer r =  new SimpleSeriesRenderer();
            r.setColor(color);
            categoryRenderer.addSeriesRenderer(r);
        }

        categoryRenderer.setZoomEnabled(true,true);

        GraphicalView categoryGv;
        categoryGv = ChartFactory.getDoughnutChartView(this, series, categoryRenderer);

//        LinearLayout llBody = (LinearLayout) findViewById(R.id.charte);
        RelativeLayout rlBody = (RelativeLayout)findViewById(R.id.charte);
        rlBody.addView(categoryGv);
    }

    public void CategoryChart1(){
        renderer= new XYMultipleSeriesRenderer();

        renderer.setChartTitle("카테고리 별 글 빈도");
        renderer.setChartTitleTextSize(35);
//        Log.e("ssiba", "아래" + values.get(1)[0] + "");

        MultipleCategorySeries series = new MultipleCategorySeries("카테고리");
        series.add("카테고리", new String[]{"성범죄", "폭력", "도난", "기타"}, values.get(1));

        renderer.setLabelsColor(Color.parseColor("#252533"));
        renderer.setLabelsTextSize(75);
        renderer.setLegendTextSize(75);
        renderer.setShowLegend(false);

        int[] colors = new int[]{Color.parseColor("#FFA9B0"), Color.parseColor("#FFE2A5"), Color.parseColor("#94E0E0"), Color.parseColor("#A295F2")};

        for(int color : colors){
            SimpleSeriesRenderer r =  new SimpleSeriesRenderer();
            r.setColor(color);
            renderer.addSeriesRenderer(r);
        }

        renderer.setZoomEnabled(true,true);

        GraphicalView categoryGv;
        categoryGv = ChartFactory.getDoughnutChartView(this, series, renderer);

        RelativeLayout rlBody = (RelativeLayout)findViewById(R.id.charta);
        rlBody.addView(categoryGv);
    }

    public void barChartForTime(){
        barCharRenderer = new XYMultipleSeriesRenderer();
        barCharRenderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));
        barCharRenderer.setMargins(new int[]{0, 100,40, 0});
        barCharRenderer.setLegendTextSize(35);

//        Log.e("ssiba", "background "+barCharRenderer.isApplyBackgroundColor());

        String[] titles = new String[] {"시간대 별 데이터 수"};

        int[] colors = new int[]{Color.parseColor("#dd4b39")};
        int length = colors.length;
        for(int i=0; i<length; i++){
            SimpleSeriesRenderer r= new SimpleSeriesRenderer();
            r.setColor(colors[i]);
            barCharRenderer.addSeriesRenderer(r);
        }

        barCharRenderer.setXTitle("시간대");
        barCharRenderer.setYTitle("빈도");
        barCharRenderer.setAxisTitleTextSize(80);

        barCharRenderer.setLabelsTextSize(40);
        barCharRenderer.setXAxisMin(0);
        barCharRenderer.setXAxisMax(9);
        barCharRenderer.setYAxisMin(0);
        barCharRenderer.setYAxisMax(40);

        barCharRenderer.setAxesColor(Color.GRAY);
        barCharRenderer.setLabelsColor(Color.GRAY);

        barCharRenderer.setPanEnabled(false, false);
        barCharRenderer.setZoomEnabled(false, false);

        barCharRenderer.setXLabels(0);
        barCharRenderer.addTextLabel(1, "0~3");
        barCharRenderer.addTextLabel(2,"3~6");
        barCharRenderer.addTextLabel(3,"6~9");
        barCharRenderer.addTextLabel(4,"9~12");
        barCharRenderer.addTextLabel(5,"12~15");
        barCharRenderer.addTextLabel(6,"15~18");
        barCharRenderer.addTextLabel(7,"18~21");
        barCharRenderer.addTextLabel(8,"21~24");

        barCharRenderer.setDisplayChartValues(true);
        barCharRenderer.setChartValuesTextSize(50);

        barCharRenderer.setXLabelsAlign(Paint.Align.CENTER);
        barCharRenderer.setYLabelsAlign(Paint.Align.LEFT);

        barCharRenderer.setBarSpacing(0.3f);

        barCharRenderer.setApplyBackgroundColor(false);

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        for (int i = 0; i < titles.length; i++) {
//            Log.e("ssiba","title length "+ titles.length +", i:"+i);
            CategorySeries series = new CategorySeries(titles[i]);
            double[] v = values.get(2);
            int seriesLength = v.length;
            for (int k = 0; k < seriesLength; k++) {
                series.add(v[k]);
            }
            dataset.addSeries(series.toXYSeries());
        }

        GraphicalView gv = ChartFactory.getBarChartView(this, dataset, barCharRenderer, BarChart.Type.DEFAULT);
        RelativeLayout rlBody = (RelativeLayout)findViewById(R.id.barchart);
        rlBody.addView(gv);

    }

    private class getWarningInfoListAsyncTask extends AsyncTask<String, Void, Void> {

        StringBuilder result = new StringBuilder();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(String... param) {

            String lng = param[0];
            String lat = param[1];

            Properties prop = new Properties();

            BufferedReader br;
            OutputStream os;

            HttpURLConnection conn;

            prop.setProperty(Constant.PARAMETER.Lng, lng);
            prop.setProperty(Constant.PARAMETER.Lat, lat);
            String encodedString = EncodeString(prop);

            URL url = null;
            try {
                url = new URL(Constant.URL.Base + Constant.URL.WarningInfoView);
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

                    String MarkerJSON = result.toString();
                    Log.e("result json:", MarkerJSON);

                    //받은 데이터 json파싱
                    WarningInfoListParser parser = new WarningInfoListParser(MarkerJSON);
                    WInfoList = parser.parse();

                } else {
//                error;
                }
                conn.disconnect();
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }

        @Override

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            double[] cate_susangArr = new double[4];
            double[] cate_dataArr = new double[4];
            double[] time_3Arr = new double[8];
            Arrays.fill(cate_susangArr, 0);

            //DB 1;기타, 2:성범죄, 3:폭력, 4:도난
            for(int i=0; i<WInfoList.getTotal();i++){
                if(WInfoList.getWarningInfoArrayList().get(i).getCategory() == 2) cate_susangArr[0] += WInfoList.getWarningInfoArrayList().get(i).getWCount();
                else if(WInfoList.getWarningInfoArrayList().get(i).getCategory() == 3) cate_susangArr[1] += WInfoList.getWarningInfoArrayList().get(i).getWCount();
                else if(WInfoList.getWarningInfoArrayList().get(i).getCategory() == 4) cate_susangArr[2] += WInfoList.getWarningInfoArrayList().get(i).getWCount();
                else cate_susangArr[3] += WInfoList.getWarningInfoArrayList().get(i).getWCount();

            }
            values.add(cate_susangArr);

//            Log.e("ssiba", "total " + cate_susangArr[0] + ", " + cate_susangArr[1] + ", " + cate_susangArr[2] + ", " + cate_susangArr[3]);
            Arrays.fill(cate_dataArr, 0);

            //카테고리 - 데이터 수
           for(int i=0; i<WInfoList.getTotal();i++){
                if(WInfoList.getWarningInfoArrayList().get(i).getCategory() == 2) cate_dataArr[0]++;
                else if(WInfoList.getWarningInfoArrayList().get(i).getCategory() == 3) cate_dataArr[1]++;
                else if(WInfoList.getWarningInfoArrayList().get(i).getCategory() == 4) cate_dataArr[2]++;
                else cate_dataArr[3]++;

            }
//            Log.e("ssiba", "total " + cate_dataArr[0] + ", " + cate_dataArr[1] + ", " + cate_dataArr[2] + ", "+cate_dataArr[3]);
            values.add(cate_dataArr);


            for(int i=0; i<WInfoList.getTotal(); i++){
//                Log.e("ssiba -t",WInfoList.getWarningInfoArrayList().get(i).getDatetime()+"");
                int hour = Integer.parseInt(WInfoList.getWarningInfoArrayList().get(i).getDatetime().split(" ")[1].split(":")[0]);
                if(hour <3) time_3Arr[0]++;
                else if(hour<6) time_3Arr[1]++;
                else if(hour<9) time_3Arr[2]++;
                else if(hour<12) time_3Arr[3]++;
                else if(hour<15) time_3Arr[4]++;
                else if(hour<18) time_3Arr[5]++;
                else if(hour<21) time_3Arr[6]++;
                else time_3Arr[7]++;

            }
//            Log.e("ssiba", "result " + time_3Arr[0] + ", " + time_3Arr[1] + ", " + time_3Arr[2] + ", "+time_3Arr[3]+ ", "+time_3Arr[4]+ ", "+time_3Arr[5]+ ", "+time_3Arr[6]+ ", "+time_3Arr[7]);
            values.add(time_3Arr);

            CategoryChart();
            CategoryChart1();
//            barChartForTime();

        }

    }


}
