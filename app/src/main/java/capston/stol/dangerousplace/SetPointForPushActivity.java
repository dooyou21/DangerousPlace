package capston.stol.dangerousplace;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.maps.overlay.NMapPathData;
import com.nhn.android.maps.overlay.NMapPathLineStyle;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;
import com.nhn.android.mapviewer.overlay.NMapPathDataOverlay;

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

import capston.stol.dangerousplace.bean.Path;
import capston.stol.dangerousplace.bean.Point;
import capston.stol.dangerousplace.parser.PathListParser;
import capston.stol.dangerousplace.parser.PointListParser;
import capston.stol.dangerousplace.util.Constant;
import capston.stol.dangerousplace.util.NMapPOIflagType;
import capston.stol.dangerousplace.util.NMapViewerResourceProvider;
import capston.stol.dangerousplace.util.PushPointInsertDialog;

import static capston.stol.dangerousplace.util.EncodeString.EncodeString;

public class SetPointForPushActivity extends NMapActivity {

    //네이버 맵 객체
    public NMapView mMapView = null;
    //맵 컨트롤러
    private NMapController mMapController = null;

    //overlay
    NMapViewerResourceProvider mMapViewerResourceProvider = null;
    NMapOverlayManager mOverlayManager;

    ImageView ivAddPoint, ivBack;
    TextView tvComment;

    private SharedPreferences setting;
    private String usridx = "";

    boolean InsertModeFlag = false;

    private PushPointInsertDialog mPushPointInsertDialog;

    private NGeoPoint PointGP;

    private String deletePointIdx, deletePathIdx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_point_for_push);

        setting = getSharedPreferences("setting", 0);
        usridx = setting.getString("UserIdx", null);

        ivAddPoint = (ImageView) findViewById(R.id.ivAddPoint);
        ivBack = (ImageView) findViewById(R.id.ivBack);
        tvComment = (TextView) findViewById(R.id.tvComment);

        ivAddPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //추가
                tvComment.setText("지도를 터치하면 해당 위치가 등록됩니다");
                ivAddPoint.setVisibility(View.GONE);
                InsertModeFlag = true;

            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tvComment.setText("추가해봐라");
                ivAddPoint.setVisibility(View.VISIBLE);
                InsertModeFlag = false;
            }
        });

        //preference
        setting = getSharedPreferences("setting", 0);
        usridx = setting.getString("UserIdx", null);

        //네이버 지도 객체 연결
        mMapView = (NMapView) findViewById(R.id.mapViewForPush);

        // 네이버 지도 객체에 APIKEY 지정
//        mMapView.setApiKey(Constant.KEY.NMAPAPIKEY);
        mMapView.setApiKey(Constant.KEY.NMAPCLIENTID);

        // initialize map view
        mMapView.setClickable(true);

        // register listener for map state changes
        mMapView.setOnMapStateChangeListener(onMapViewStateChangeListener);
        mMapView.setOnMapViewTouchEventListener(onMapViewTouchEventListener);

        // use map controller to zoom in/out, pan and set map center, zoom level etc.
        mMapController = mMapView.getMapController();

        mMapViewerResourceProvider = new NMapViewerResourceProvider(this);

        // create overlay manager
        mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);

        new getPathDataListAsyncTask().execute(usridx);
        new getPointDataListAsyncTask().execute(usridx);


    }

    /* MapView State Change Listener*/
    private final NMapView.OnMapStateChangeListener onMapViewStateChangeListener = new NMapView.OnMapStateChangeListener() {

        @Override
        public void onMapInitHandler(NMapView mapView, NMapError errorInfo) {

            if (errorInfo == null) { // success
                mMapController.setMapCenter(new NGeoPoint(127.7377907, 37.8688359), 11);//강원대학교 한빛관 좌표. 초기설정
            } else { // fail
                Toast.makeText(SetPointForPushActivity.this, errorInfo.toString(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onAnimationStateChange(NMapView mapView, int animType, int animState) {

        }

        @Override
        public void onMapCenterChange(NMapView mapView, NGeoPoint center) {

        }

        @Override
        public void onZoomLevelChange(NMapView mapView, int level) {

        }

        @Override
        public void onMapCenterChangeFine(NMapView mapView) {

        }
    };


    private View.OnClickListener leftListener = new View.OnClickListener() {
        public void onClick(View v) {//등록
            String pointTitle = mPushPointInsertDialog.getPointTitle();
            if(pointTitle.equals("")){//장소이름 없는 경우
                Toast.makeText(SetPointForPushActivity.this, "장소 이름을 입력해주세요", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(SetPointForPushActivity.this, pointTitle, Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "장소이름: "+pointTitle, Toast.LENGTH_SHORT).show();
                mPushPointInsertDialog.dismiss();
                //asynctask point insert
                //PointGP와 pointTitle, useridx 매개변수로 넘겨서execute
                new insertPointDataAsyncTask().execute(usridx, pointTitle, Double.toString(PointGP.getLatitude()), Double.toString(PointGP.getLongitude()));

            }

        }
    };

    private View.OnClickListener rightListener = new View.OnClickListener() {//취소
        public void onClick(View v) {
            mPushPointInsertDialog.dismiss();

        }
    };

    /* Map View Touch Event Listener*/
    private final NMapView.OnMapViewTouchEventListener onMapViewTouchEventListener = new NMapView.OnMapViewTouchEventListener() {

        @Override
        public void onLongPress(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLongPressCanceled(NMapView mapView) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSingleTapUp(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onTouchDown(NMapView mapView, MotionEvent ev) {

            if(InsertModeFlag){
//                //혹시 나중에 써먹을까 싶어서 남겨놓는다...
//                double lat = mapView.getMapController().getMapCenter().getLatitude();
//                double lng = mapView.getMapController().getMapCenter().getLongitude();
//                PointGP = new NGeoPoint(lng, lat);

                PointGP = mapView.getMapProjection().fromPixels((int)ev.getX(), (int)ev.getY());

                //제목 입력을 위한 dialog띄우기
                mPushPointInsertDialog = new PushPointInsertDialog(SetPointForPushActivity.this,
                        leftListener, // 왼쪽 버튼 이벤트. 확인 클릭시 insert asynctask 동작
                        rightListener); // 오른쪽 버튼 이벤트
                mPushPointInsertDialog.show();

                //+버튼 다시 보이도록
                ivAddPoint.setVisibility(View.VISIBLE);
                InsertModeFlag = false;
                tvComment.setText("추가해봐라");
            }

        }

        @Override
        public void onTouchUp(NMapView nMapView, MotionEvent motionEvent) {

        }

        @Override
        public void onScroll(NMapView nMapView, MotionEvent motionEvent, MotionEvent motionEvent1) {

        }
    };

    private void PathDataOverlay(ArrayList<Path> PathList) {

        for(Path path : PathList){

            NMapPathData pathData;
            ArrayList<NGeoPoint> points = path.getPoints();

            pathData = new NMapPathData(points.size()+1);
            pathData.initPathData();

            for(int j=0;j<points.size();++j){
                pathData.addPathPoint(points.get(j).getLongitude(), points.get(j).getLatitude(), NMapPathLineStyle.TYPE_SOLID);
            }

            pathData.endPathData();

//            Log.i("pathlist num", path.getPoints().toString());

            NMapPathDataOverlay overlay = new NMapPathDataOverlay(pathData, mMapController, mOverlayManager);
            overlay.setLineColor(getResources().getColor(R.color.themeRed), 100);
            overlay.setLineWidth((float)10.0);

            mOverlayManager.addOverlay(overlay);

        }

    }

    private void PathPOIDataOverlay(ArrayList<Path> PathList) {

        NMapPOIdata poiData = new NMapPOIdata(PathList.size(), mMapViewerResourceProvider, true);
        poiData.beginPOIdata(PathList.size());

        int i=1;
        for(Path path : PathList){
            poiData.addPOIitem(path.getPoints().get(0), null, NMapPOIflagType.NUMBER_BASE + i, null, path.getPathIdx());
            ++i;
        }

        poiData.endPOIdata();

        NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
        poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);
    }

    private void PointPOIDataOverlay(ArrayList<Point> PointList) {

        NMapPOIdata poiData = new NMapPOIdata(PointList.size(), mMapViewerResourceProvider);
        poiData.beginPOIdata(PointList.size());

        for(Point point : PointList){
//        Log.i("aaaa", point.getPointTitle()+","+point.getPoint().getLongitude()+","+point.getPoint().getLatitude());
            poiData.addPOIitem(point.getPoint(), point.getPointTitle(), NMapPOIflagType.MARKER_YELLOW, null, point.getIdx());

        }
        poiData.endPOIdata();

        NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
        poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);

    }


    /* POI data State Change Listener*/
    private final NMapPOIdataOverlay.OnStateChangeListener onPOIdataStateChangeListener = new NMapPOIdataOverlay.OnStateChangeListener() {

        @Override
        public void onCalloutClick(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {

            deletePointIdx = Integer.toString(item.getId());

            AlertDialog.Builder dialog = new AlertDialog.Builder(SetPointForPushActivity.this);
            dialog.setMessage("'" + item.getTitle() + "'" + " (을)를 '알림받을 장소'에서 제외할까요?");
            dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    new deletePointDataAsyncTask().execute(deletePointIdx);
                }
            });
            dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            dialog.show();

        }

        @Override
        public void onFocusChanged(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
            if (item != null) {//focus in
                if(item.getTitle() == null){//path
                    deletePathIdx = Integer.toString(item.getId());
                    Log.i("poiclicked", "this is path");
                    AlertDialog.Builder dialog = new AlertDialog.Builder(SetPointForPushActivity.this);
                    dialog.setMessage("이 경로를 삭제할까요?");
                    dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            new deletePathAsyncTask().execute(deletePathIdx);
                        }
                    });
                    dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();

                }
            }
        }
    };

//
//    public boolean onTouchEvent(MotionEvent ev) {
//        final int action  = ev.getAction();
//        final float x = ev.getX();
//        final float y = ev.getY();
//
//        switch(action){
//            case MotionEvent.ACTION_DOWN:
//                Log.i("!!!clicked!!!", "x: "+x+", y: "+y);
//                Log.i("!!!clicked!!!", ev.getDownTime()+"");
//
//                break;
//            case MotionEvent.ACTION_MOVE:
//                Log.i("!!!moved!!!", "x: "+x+", y: "+y);
//                break;
//            case MotionEvent.ACTION_UP:
//                Log.i("!!!up!!!", "x: "+x+", y: "+y);
//                break;
//        }
//        return true;
//    }

    //getPathDataListAsyncTask
    private class getPathDataListAsyncTask extends AsyncTask<String, Void, Void> {

        ArrayList<Path> PathList = null;

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected Void doInBackground(String... param){


            Properties prop = new Properties();
            BufferedReader br;
            OutputStream os;

            StringBuilder result = new StringBuilder();

            HttpURLConnection conn;

            prop.setProperty(Constant.PARAMETER.MyIdx, param[0]);
            String encodedString = EncodeString(prop);

            URL url = null;
            try{
                url = new URL(Constant.URL.Base + Constant.URL.GetPath);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Accept", "application/json");

                os = conn.getOutputStream();
                os.write(encodedString.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "euc-kr"));

                    String line = "";
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }

                    String pathJSON = result.toString();

//                    Log.e("path:", pathJSON);

                    PathListParser parser = new PathListParser(pathJSON);
                    PathList = parser.parse();

                } else {
                    Log.e("path:", "error");
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
        protected void onPostExecute(Void result){
            //PathList가 null이 아니면 path를 가져온다
            //path의 points를 바탕으로 overlay를 그린다.
            PathDataOverlay(PathList);

            PathPOIDataOverlay(PathList);
        }

    }


    //getPointDataListAsyncTask
    private class getPointDataListAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute(){

        }

        ArrayList<Point> pointList = null;

        @Override
        protected Void doInBackground(String... param){//param은 myidx

            Properties prop = new Properties();
            BufferedReader br;
            OutputStream os;

            StringBuilder result = new StringBuilder();

            HttpURLConnection conn;

            prop.setProperty(Constant.PARAMETER.MyIdx, param[0]);
            String encodedString = EncodeString(prop);

            URL url = null;

            try{
                url = new URL(Constant.URL.Base + Constant.URL.GetPointList);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Accept", "application/json");

                os = conn.getOutputStream();
                os.write(encodedString.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "euc-kr"));

                    String line = "";
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }

                    String pointJSON = result.toString();

//                    Log.e("point:", pointJSON);

                    PointListParser parser = new PointListParser(pointJSON);
                    pointList = parser.parse();

                } else {
                    Log.e("point:", "error");
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
        protected void onPostExecute(Void result){

            PointPOIDataOverlay(pointList);

        }

    }

    //insertPointDataAsyncTask
    private class insertPointDataAsyncTask extends AsyncTask<String, Void, String> {

        String pointTitle = "";

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected String doInBackground(String... param){

            StringBuilder result = new StringBuilder();
            String response = null;

            //my_idx, point_title, xcoord, ycoord
            Properties prop = new Properties();

            InputStream is;
            BufferedReader br;
            OutputStream os;

            HttpURLConnection conn;

            pointTitle = param[1];

            prop.setProperty(Constant.PARAMETER.MyIdx, param[0]);
            prop.setProperty(Constant.PARAMETER.PointTitle, param[1]);
            prop.setProperty(Constant.PARAMETER.Lng, param[2]);
            prop.setProperty(Constant.PARAMETER.Lat, param[3]);

            String encodedString = EncodeString(prop);

            URL url = null;

            try {
                url = new URL(Constant.URL.Base + Constant.URL.InsertPoint);

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

//                    Log.d("result json:", response);

                } else {
                    return null;
                }
                conn.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result){
            //PointGP위치와 title을 가지고 POIOverlay생성해야함.
            if(result.equals("p regist")){

                Toast.makeText(SetPointForPushActivity.this, "성공적으로 등록되었습니다!", Toast.LENGTH_SHORT);

                mOverlayManager.clearOverlays();
                new getPathDataListAsyncTask().execute(usridx);
                new getPointDataListAsyncTask().execute(usridx);

            } else {// p fail

                Toast.makeText(SetPointForPushActivity.this, "다시 시도해주세요!", Toast.LENGTH_SHORT);
            }

        }

    }

    //deletePointDataAsyncTask
    private class deletePointDataAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected String doInBackground(String... param){

            StringBuilder result = new StringBuilder();
            String response = null;

            Properties prop = new Properties();

            InputStream is;
            BufferedReader br;
            OutputStream os;

            HttpURLConnection conn;

            prop.setProperty(Constant.PARAMETER.PointIdx, param[0]);

            String encodedString = EncodeString(prop);

            URL url = null;

            try {
                url = new URL(Constant.URL.Base + Constant.URL.DeletePoint);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Accept", "application/json");

                os = conn.getOutputStream();
                os.write(encodedString.getBytes());
                os.flush();
                os.close();

//                Log.d("deletePoint", url + "?" + encodedString);

                int responseCode = conn.getResponseCode();
//                Log.d("respCode", responseCode + "");

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    is = new BufferedInputStream(conn.getInputStream());
                    br = new BufferedReader(new InputStreamReader(is));

                    String line = "";
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }

                    is.close();

                    response = result.toString();

//                    Log.d("result json:", response);

                } else {
                    return null;
                }
                conn.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result){
            if(result.equals("delete success")){
                mOverlayManager.clearOverlays();
                new getPathDataListAsyncTask().execute(usridx);
                new getPointDataListAsyncTask().execute(usridx);
            } else { // fail
                Toast.makeText(SetPointForPushActivity.this, "다시 시도해주세요!", Toast.LENGTH_SHORT);
            }

        }

    }
    //deletePointDataAsyncTask
    private class deletePathAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected String doInBackground(String... param){

            StringBuilder result = new StringBuilder();
            String response = null;

            Properties prop = new Properties();

            InputStream is;
            BufferedReader br;
            OutputStream os;

            HttpURLConnection conn;

            prop.setProperty(Constant.PARAMETER.PathIdx, param[0]);

            String encodedString = EncodeString(prop);

            URL url = null;

            try {
                url = new URL(Constant.URL.Base + Constant.URL.DetelePath);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Accept", "application/json");

                os = conn.getOutputStream();
                os.write(encodedString.getBytes());
                os.flush();
                os.close();

//                Log.d("deletePath", url + "?" + encodedString);

                int responseCode = conn.getResponseCode();
//                Log.d("respCode", responseCode + "");

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    is = new BufferedInputStream(conn.getInputStream());
                    br = new BufferedReader(new InputStreamReader(is));

                    String line = "";
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }

                    is.close();

                    response = result.toString();

//                    Log.d("result json:", response);

                } else {
                    return null;
                }
                conn.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result){
            if(result.equals("delete success")){
                mOverlayManager.clearOverlays();
                new getPathDataListAsyncTask().execute(usridx);
                new getPointDataListAsyncTask().execute(usridx);
            } else {
                Toast.makeText(SetPointForPushActivity.this, "다시 시도해주세요!", Toast.LENGTH_SHORT);
            }

        }

    }

}
