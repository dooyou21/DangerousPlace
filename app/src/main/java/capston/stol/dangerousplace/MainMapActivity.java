package capston.stol.dangerousplace;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapCompassManager;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapCalloutOverlay;
import com.nhn.android.mapviewer.overlay.NMapMyLocationOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import capston.stol.dangerousplace.bean.WarningInfo;
import capston.stol.dangerousplace.bean.WarningInfoList;
import capston.stol.dangerousplace.parser.WarningInfoListParser;
import capston.stol.dangerousplace.util.CloseLeftAnimation;
import capston.stol.dangerousplace.util.Constant;
import capston.stol.dangerousplace.util.GPSTrackingService;
import capston.stol.dangerousplace.util.NMapCalloutCustomOldOverlay;
import capston.stol.dangerousplace.util.NMapCalloutListOverlay;
import capston.stol.dangerousplace.util.NMapPOIflagType;
import capston.stol.dangerousplace.util.NMapViewerResourceProvider;
import capston.stol.dangerousplace.util.OpenLeftAnimation;
import capston.stol.dangerousplace.util.ScreenService;

import static capston.stol.dangerousplace.util.EncodeString.EncodeString;

public class MainMapActivity extends NMapActivity implements View.OnClickListener, NMapOverlayManager.OnCalloutOverlayListener, CompoundButton.OnCheckedChangeListener {

    //네이버 맵 객체
    public NMapView mMapView = null;
    //맵 컨트롤러
    private NMapController mMapController = null;

    //overlay
    NMapViewerResourceProvider mMapViewerResourceProvider = null;

    NMapOverlayManager mOverlayManager;

    NMapPOIdataOverlay poiDataOverlay;
    NMapMyLocationOverlay mMyLocationOverlay;
    NMapLocationManager mMapLocationManager;
    NMapCompassManager mMapCompassManager;

    //slide menu
    private DisplayMetrics metrics;
    private LinearLayout ll_mainLayout;
    private LinearLayout ll_menuLayout;
    private FrameLayout.LayoutParams leftMenuLayoutParams;
    private int leftMenuWidth;
    private static boolean isLeftExpanded = false;
    private RelativeLayout rlBlock;

    // down sliding drawer
    private TextView top3, content1, content2, content3;
    private SlidingUpPanelLayout downdrawer;

    private ImageView imgleft, imgadd, imgMyLoc;
    private TextView tvmypage, tvdmode, tvgraph, tvusremail;
    private Switch swDangermode;
    private RelativeLayout rlIntroImage;

    private SharedPreferences setting;
    private String usridx, usremail;

    //getWarningInfo를 위한 변수
    private double lat;
    private double lng;

    //WarningInfoList Object
    private WarningInfoList WInfoList = null;

    //For GPS Tracking Service
    private ComponentName GPSservice;

    private boolean needRefresh = false;

    int[] top3_arr = new int[3];

    private boolean gpsDialogflag = true;
    // 겹친 아이템들의 아이디들을 전송하기 위해 저장하는 array list
    private ArrayList<String> contents = new ArrayList<String>();

    //사용하지않음
//    private DefaultDialog gpsEnableDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);

        //Intro 이미지 보여짐

        //preference
        setting = getSharedPreferences("setting", 0);
        usridx = setting.getString("UserIdx", null);
        usremail = setting.getString("UserEmail", null);
//        Log.d("useremail", usridx+","+usremail);

        //leftsilde init 및 ui 초기화
        initSlideMenu();

        /**************지도 초기화 ****************/


        //네이버 지도 객체 연결
        mMapView = (NMapView) findViewById(R.id.mapView);

        // 네이버 지도 객체에 APIKEY 지정
        mMapView.setClientId(Constant.KEY.NMAPCLIENTID);
//        mMapView.setApiKey(Constant.KEY.NMAPAPIKEY);

        //지도 터치 활성화
        mMapView.setClickable(true);

        //확대/축소 활성화
//        mMapView.setBuiltInZoomControls(true, null);


        mMapController = mMapView.getMapController();

        mMapView.setOnMapStateChangeListener(onMapViewStateChangeListener);

        /***************오버레이 관리자 ****************/

        mMapViewerResourceProvider = new NMapViewerResourceProvider(this);

        mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);
        mOverlayManager.setOnCalloutOverlayListener(onCalloutOverlayListener);


        //----------위치 관리----------//
        mMapLocationManager = new NMapLocationManager(MainMapActivity.this);
        mMapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);

        mMapCompassManager = new NMapCompassManager(this);
        mMyLocationOverlay = mOverlayManager.createMyLocationOverlay(mMapLocationManager, mMapCompassManager);

        MyLocation();

    }

    private void MyLocation() {
        if (mMyLocationOverlay != null) {
            if (!mOverlayManager.hasOverlay(mMyLocationOverlay)) {
                mOverlayManager.addOverlay(mMyLocationOverlay);
            }
            if(!mMapLocationManager.enableMyLocation(true)){
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage("위치정보를 사용하려면 위치 서비스 권한을 허용해주세요.");
                dialog.setPositiveButton("설정하기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(goToSettings);
                        rlIntroImage.setVisibility(View.VISIBLE);
                        needRefresh = true;
                    }
                });
                dialog.setNegativeButton("허용안함", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                        lat = 37.8688359;
                        lng = 127.7377907;
                        gpsDialogflag = false;
                        new getWarningInfoListAsyncTask().execute(Double.toString(lat), Double.toString(lng));
                    }
                });
                if(gpsDialogflag){
                    dialog.show();
                } else {
                    lat = 37.8688359;
                    lng = 127.7377907;
                    new getWarningInfoListAsyncTask().execute(Double.toString(lat), Double.toString(lng));
                }
            }
            mMapView.postInvalidate();
        }
    }

    //사용하지않음. 기본 alert dialog를 사용함.
//    private View.OnClickListener goToSettingListener = new View.OnClickListener() {
//        public void onClick(View v) {
//            gpsEnableDialog.dismiss();
//            Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivity(goToSettings);
//            rlIntroImage.setVisibility(View.VISIBLE);
//            needRefresh = true;
//        }
//    };
//
//    private View.OnClickListener cancelListener = new View.OnClickListener() {//취소. gps 허용 안함
//        public void onClick(View v) {
//            gpsEnableDialog.dismiss();
//            lat = 37.8688359;
//            lng = 127.7377907;
//            new getWarningInfoListAsyncTask().execute(Double.toString(lat), Double.toString(lng));
//        }
//    };

    /*for left slide menu*/
    private void initSlideMenu() {

        //init left menu width
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        leftMenuWidth = (int) ((metrics.widthPixels) * 0.75);

        //init main view
        ll_mainLayout = (LinearLayout) findViewById(R.id.ll_mainLayout);

        //init left menu
        ll_menuLayout = (LinearLayout) findViewById(R.id.ll_menuLayout);
        leftMenuLayoutParams = (FrameLayout.LayoutParams) ll_menuLayout.getLayoutParams();
        leftMenuLayoutParams.width = leftMenuWidth;
        ll_menuLayout.setLayoutParams(leftMenuLayoutParams);
        rlBlock = (RelativeLayout) findViewById(R.id.rlBlock);
        rlBlock.setVisibility(View.GONE);

        //init ui

        rlIntroImage = (RelativeLayout) findViewById(R.id.rlIntroImage);

        tvusremail = (TextView) findViewById(R.id.tvemail);
        tvusremail.setText(usremail);

        imgleft = (ImageView) findViewById(R.id.imgleft);
        imgleft.setOnClickListener(this);
        imgadd = (ImageView) findViewById(R.id.imgadd);
        imgadd.setOnClickListener(this);
        imgMyLoc = (ImageView) findViewById(R.id.imgMyLoc);
        imgMyLoc.setOnClickListener(this);

        tvmypage = (TextView) findViewById(R.id.tvmypage);
        tvdmode = (TextView) findViewById(R.id.tvdmode);
        tvgraph = (TextView) findViewById(R.id.tvgraph);
        tvmypage.setOnClickListener(this);
        tvdmode.setOnClickListener(this);
        tvgraph.setOnClickListener(this);

        //down sliding drawer
        top3 = (TextView)findViewById(R.id.top3);
        downdrawer = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        content1 = (TextView)findViewById(R.id.content1);
        content2 = (TextView)findViewById(R.id.content2);
        content3 = (TextView)findViewById(R.id.content3);

        downdrawer.setDragView(top3);
        content1.setOnClickListener(this);
        content2.setOnClickListener(this);
        content3.setOnClickListener(this);


        swDangermode = (Switch) findViewById(R.id.swDangermode);
        swDangermode.setText("위험모드 Off");
        swDangermode.setOnCheckedChangeListener(this);
        SetSW();
    }

    //left menu toggle
    private void menuLeftSlideAnimationToggle() {
        if (!isLeftExpanded) {
            isLeftExpanded = true;

            imgadd.setVisibility(View.GONE);
            imgMyLoc.setVisibility(View.GONE);
            mMapView.setEnabled(false);
            rlBlock.setVisibility(View.VISIBLE);
            downdrawer.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

            //expand
            new OpenLeftAnimation(ll_mainLayout, leftMenuWidth,
                    Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.75f, 0, 0.0f, 0, 0.0f);

            //enable all menu view
            FrameLayout viewGroup = (FrameLayout) findViewById(R.id.ll_menuLayout).getParent();
            enableDisableViewGroup(viewGroup, true);

            //enable empty view
            findViewById(R.id.ll_empty).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_empty).setEnabled(true);
            findViewById(R.id.ll_empty).setOnTouchListener(
                    new View.OnTouchListener() {


                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            menuLeftSlideAnimationToggle();
                            return true;
                        }
                    });
        } else {
            isLeftExpanded = false;
            imgadd.setVisibility(View.VISIBLE);
            imgMyLoc.setVisibility(View.VISIBLE);
            mMapView.setEnabled(true);
            rlBlock.setVisibility(View.GONE);
            downdrawer.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

            //collapse
            new CloseLeftAnimation(ll_mainLayout, leftMenuWidth,
                    TranslateAnimation.RELATIVE_TO_SELF, 0.75f,
                    TranslateAnimation.RELATIVE_TO_SELF, 0.0f, 0, 0.0f, 0, 0.0f);

            //enable all of menu view
            FrameLayout viewGroup = (FrameLayout) findViewById(R.id.ll_menuLayout).getParent();
            enableDisableViewGroup(viewGroup, false);

            //disable empty view
            findViewById(R.id.ll_empty).setVisibility(View.GONE);
            findViewById(R.id.ll_empty).setEnabled(false);

        }
    }

    /*for left slide menu*/
    public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            if (view.getId() != R.id.ll_menuLayout) {
                view.setEnabled(enabled);
            } else if (view.getId() != R.id.ll_mainLayout) {
                view.setEnabled(!enabled);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        SetSW();
        super.onResume();
    }

    @Override
    protected void onStop() {
        mMapLocationManager.disableMyLocation();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        if(needRefresh) {
            mOverlayManager.clearOverlays();
            MyLocation();
        }
        super.onRestart();
    }

    public void onBackPressed() {

        if(isLeftExpanded){
            menuLeftSlideAnimationToggle();
        } else {

            if(downdrawer.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)){
                downdrawer.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            } else {

                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage("수상해를 종료하시겠습니까?");
                dialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                dialog.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        }

        //수정 필요
        //downdrawer열려있을때 back키 누르면 downdrawer들어가야함
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//메뉴키 구현
        if(keyCode == KeyEvent.KEYCODE_MENU){
            menuLeftSlideAnimationToggle();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgleft:
                menuLeftSlideAnimationToggle();
                break;
            case R.id.imgMyLoc:
                gpsDialogflag = true;
                MyLocation();
                break;
            case R.id.imgadd:
                Intent lIntent = new Intent(MainMapActivity.this, InsertWarningInfoActivity.class);
                lIntent.putExtra("USRIDX", usridx);
                lIntent.putExtra("INSERTORUPDATE", "insert");
                startActivityForResult(lIntent, 1);
                break;
            case R.id.tvmypage:
                Intent intent = new Intent(this, MyPageActivity.class);
                startActivity(intent);
                break;
            case R.id.tvdmode:
                if(swDangermode.isChecked()){
                    //Dialog로 수정해야함
                    Toast.makeText(MainMapActivity.this, "위험모드 실행중에는 설정을 변경할 수 없습니다!", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent2 = new Intent(this, SetDangerModeActivity.class);
                    startActivity(intent2);
                }
                break;
            case R.id.tvgraph:
                Intent graphIntent = new Intent(this, StatisticalChartActivity.class);
                startActivity(graphIntent);
                break;
            case R.id.content1:
                Intent top1Intent = new Intent(MainMapActivity.this, ShowWarningInfoActivity.class);
                top1Intent.putExtra("WINFOIDX", WInfoList.getWarningInfoArrayList().get(top3_arr[0]).getIdx() + "");
                startActivity(top1Intent);
                break;
            case R.id.content2:
                Intent top2Intent = new Intent(MainMapActivity.this, ShowWarningInfoActivity.class);
                top2Intent.putExtra("WINFOIDX", WInfoList.getWarningInfoArrayList().get(top3_arr[1]).getIdx()+"");
                startActivity(top2Intent);
                break;
            case R.id.content3:
                Intent top3Intent = new Intent(MainMapActivity.this, ShowWarningInfoActivity.class);
                top3Intent.putExtra("WINFOIDX", WInfoList.getWarningInfoArrayList().get(top3_arr[2]).getIdx()+"");
                startActivity(top3Intent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK) {//startActiviyForResult는 add, detail보기 버튼 누를때 실행된다.
//          data가 바뀌었을경우(추가하거나 데이터를 수정,삭제한 경우) 데이터 다시불러온다.
            MyLocation();
        } else if(resultCode==RESULT_CANCELED) {
            //갱신이 필요없을때.
        }
    }

    @Override
    public NMapCalloutOverlay onCreateCalloutOverlay(NMapOverlay nMapOverlay, NMapOverlayItem nMapOverlayItem, Rect rect) {
        // 오버레이 아이템의 저장된 title을 화면에 띄움
        Toast.makeText(this, nMapOverlayItem.getTitle(), Toast.LENGTH_SHORT).show();

        return null;
    }

    //===========위치추적모드 sw기능관련함수
    @Override
    public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
            buttonView.setText("위험모드 On");
            //GPSTracking ture and SendMessage true일때
            if(setting.getBoolean("GPSTracking", false) && setting.getBoolean("SendMessage", false)) {
                Toast.makeText(MainMapActivity.this, "두 개의 서비스를 같이 사용할 수 없음", Toast.LENGTH_SHORT).show();
//                if(mMapLocationManager.isMyLocationEnabled()){
////                    gpsDialogflag = true;
////                    MyLocation();
//                } else {
//                    GPSservice = startService(new Intent(MainMapActivity.this, GPSTrackingService.class));
//                    Log.i("gps and msg", "on");
//                    startService(new Intent(MainMapActivity.this, ScreenService.class));
//                    Log.i("msg", "on");
//                }
            }
            //GPSTracking ture and SendMessage false 일때
            else if(setting.getBoolean("GPSTracking", false)) {
                //gps켜져있지않을때 gps다시 설정하도록 수정해야함.
//                if(mMapLocationManager.isMyLocationEnabled()){
////                    gpsDialogflag = true;
////                    MyLocation();
//                } else {
                    GPSservice = startService(new Intent(MainMapActivity.this, GPSTrackingService.class));
                    Log.i("gps and msg", "on");
//                }
            }
            //GPSTracking false and SendMessage true일때
            else if(setting.getBoolean("SendMessage", false)) {
                startService(new Intent(MainMapActivity.this, ScreenService.class));
                Log.i("msg", "on");
            }
            else {
                Toast.makeText(MainMapActivity.this, "위험모드설정을 변경하여 기능을 사용해보세요!", Toast.LENGTH_SHORT).show();
                Log.i("notting", "notting happened");
            }

        } else {
            buttonView.setText("위험모드 Off");
            if(setting.getBoolean("GPSTracking", false)) {
                if (GPSservice == null) {
//                    Toast.makeText(MainMapActivity.this, "GPSservice is null", Toast.LENGTH_SHORT).show();
                }
                if (stopService(new Intent(MainMapActivity.this, GPSTrackingService.class))) {
//                    Toast.makeText(MainMapActivity.this, "GPSservice is stopped", Toast.LENGTH_SHORT).show();
                } else {
//                    Toast.makeText(MainMapActivity.this, "GPSservice is arleady stopped", Toast.LENGTH_SHORT).show();
                }

                Log.i("gps", "off");
            }

            //위험시 문자전송 기능 off
            if(setting.getBoolean("SendMessage", false)) {
                stopService(new Intent(MainMapActivity.this, ScreenService.class));
                Log.i("msg", "off");
            }
        }
    }
    private void SetSW() {//위험모드 실행되어있는지 확인
        if(setting.getBoolean("GPSTracking", false) || setting.getBoolean("SendMessage", false)) {
            ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if ("capston.stol.dangerousplace.util.GPSTrackingService".equals(service.service.getClassName()) ||
                        "capston.stol.dangerousplace.util.ScreenService".equals(service.service.getClassName())) {
                    Log.i("asdf", service.service.getClassName());
                    swDangermode.setChecked(true);
                    swDangermode.setText("위험모드 On");
                    break;
                } else {
                    swDangermode.setChecked(false);
                    swDangermode.setText("위험모드 Off");
                }
            }
        }
    }
    //==========================

    /*WarningInfoList Parsed JSON 가져오기*/
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

//            Log.e("latlng", lat + ", " + lng);

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
//                    Log.e("result json:", MarkerJSON);

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

            mOverlayManager.clearOverlays();

            /***************오버레이 관리자 ****************/

            //------- overlay item 직접 지정 할 때 사용--------//
            // overlay 객체 생성
            // 수상해 지수에 따른 marker의 색상을 다르게 해주기 위한 markerid 선언
            int markerID_black = NMapPOIflagType.MARKER_BLACK;
            int markerID_yellow = NMapPOIflagType.MARKER_YELLOW;
            int markerID_red = NMapPOIflagType.MARKER_RED;

            NMapPOIdata poiData = new NMapPOIdata(WInfoList.getTotal(), mMapViewerResourceProvider);

            if (WInfoList.getTotal() == 0) {

                Toast.makeText(MainMapActivity.this, "데이터가 없습니다", Toast.LENGTH_SHORT).show();

            } else {

                poiData.beginPOIdata(WInfoList.getTotal());

                // DB에서 수상해 지수를 받아와 값에 따른 다른 색깔의 마커를 지도에 띄움
                for (int i = 0; i < WInfoList.getTotal(); i++) {
                    WarningInfo winfo = WInfoList.getWarningInfoArrayList().get(i);
                    if (winfo.getWCount() < 10) {
                        poiData.addPOIitem(winfo.getYCoord(), winfo.getXCoord(), winfo.getTitle(), markerID_black, winfo.getIdx());
                    } else if (winfo.getWCount() < 20) {
                        poiData.addPOIitem(winfo.getYCoord(), winfo.getXCoord(), winfo.getTitle(), markerID_yellow, winfo.getIdx());
                    } else {
                        poiData.addPOIitem(winfo.getYCoord(), winfo.getXCoord(), winfo.getTitle(), markerID_red, winfo.getIdx());
                    }
//                Log.d("winfo" + i, winfo.getYCoord() + ", " + winfo.getXCoord() + ", " + markerID + ", " + winfo.getTitle()+"");

                }

                poiData.endPOIdata();

                // create POI data overlay
                poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
                // show all POI data
//                poiDataOverlay.showAllPOIdata(10);//poi아이템이 모두 화면에 표시되도록 화면을 움직이는 기능
                // set event listener to the overlay
                poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);
                //overlay event
                mOverlayManager.setOnCalloutOverlayListener(onCalloutOverlayListener);

                mMapLocationManager.disableMyLocation();

                Arrays.fill(top3_arr, 0);

                for(int i =0; i<WInfoList.getTotal(); i++) {
                    int tmp, w;
                    WarningInfo winfo = WInfoList.getWarningInfoArrayList().get(i);
                    int j = i;
                    w = winfo.getWCount();
                    if (WInfoList.getWarningInfoArrayList().get(top3_arr[0]).getWCount() < WInfoList.getWarningInfoArrayList().get(j).getWCount()) {
                        tmp = top3_arr[0];
                        top3_arr[0] = j;
                        j = tmp;
                    }
                    if (WInfoList.getWarningInfoArrayList().get(top3_arr[1]).getWCount() < WInfoList.getWarningInfoArrayList().get(j).getWCount()) {
                        tmp = top3_arr[1];
                        top3_arr[1] = j;
                        j = tmp;
                    }
                    if (WInfoList.getWarningInfoArrayList().get(top3_arr[2]).getWCount() < WInfoList.getWarningInfoArrayList().get(j).getWCount()) {
                        tmp = top3_arr[2];
                        top3_arr[2] = j;
                        j = tmp;
                    }
                }

//                Log.i("ArrayList number", WInfoList.getWarningInfoArrayList().isEmpty()+"??");

                // TOP 3 의 TEXT 설정 및 수상해 지수에 따른 TEXT COLOR 설정
                content1.setText(WInfoList.getWarningInfoArrayList().get(top3_arr[0]).getTitle() + " ");
                if (WInfoList.getWarningInfoArrayList().get(top3_arr[0]).getWCount() < 10) {
                    content1.setTextColor(Color.BLACK);
                }
                else if(WInfoList.getWarningInfoArrayList().get(top3_arr[0]).getWCount() < 20)
                {
                    content1.setTextColor(Color.YELLOW);
                }
                else
                    content1.setTextColor(Color.RED);

                content2.setText(WInfoList.getWarningInfoArrayList().get(top3_arr[1]).getTitle() + " ");
                if (WInfoList.getWarningInfoArrayList().get(top3_arr[1]).getWCount() < 10) {
                    content2.setTextColor(Color.BLACK);
                }
                else if(WInfoList.getWarningInfoArrayList().get(top3_arr[1]).getWCount() < 20)
                {
                    content2.setTextColor(Color.YELLOW);
                }
                else
                    content2.setTextColor(Color.RED);

                content3.setText(WInfoList.getWarningInfoArrayList().get(top3_arr[2]).getTitle() + " ");
                if (WInfoList.getWarningInfoArrayList().get(top3_arr[2]).getWCount() < 10) {
                    content3.setTextColor(Color.BLACK);
                }
                else if(WInfoList.getWarningInfoArrayList().get(top3_arr[2]).getWCount() < 20)
                {
                    content3.setTextColor(Color.YELLOW);
                }
                else
                    content3.setTextColor(Color.RED);

                needRefresh = false;

            }

            rlIntroImage.setVisibility(View.GONE);
        }

    }





    /* MyLocation Listener */
    private final NMapLocationManager.OnLocationChangeListener onMyLocationChangeListener = new NMapLocationManager.OnLocationChangeListener() {

        @Override
        public boolean onLocationChanged(NMapLocationManager locationManager, NGeoPoint myLocation) {

            if (mMapController != null) {
                mMapController.animateTo(myLocation);
            }

            if (myLocation!=null) {
                //현위치를 서버로 보내고 Warning Info list를 받아옴
                lat = myLocation.getLatitude();
                lng = myLocation.getLongitude();
            }

            new getWarningInfoListAsyncTask().execute(Double.toString(lat), Double.toString(lng));

            return true;
        }

        //정해진 시간 내에 위치 탐색 실패시
        @Override
        public void onLocationUpdateTimeout(NMapLocationManager locationManager) {

            Toast.makeText(MainMapActivity.this, "timeout", Toast.LENGTH_LONG).show();
//            mMapLocationManager.disableMyLocation(); 탐색실패에도 이놈이 필요한가? 실패했다는거자체가 이미 disabled인거면 안써도되는건데.
            //default좌표(강원대학교 한빛관)를 기반으로 WarningInfoList를 받아온다.
            new getWarningInfoListAsyncTask().execute("37.8688359", "127.7377907");
        }

        //현재 위치가 지도 상에 표시할 수 있는 범위 밖인 경우
        @Override
        public void onLocationUnavailableArea(NMapLocationManager locationManager, NGeoPoint myLocation) {

            Toast.makeText(MainMapActivity.this, "unavailable location", Toast.LENGTH_LONG).show();

            mMapLocationManager.disableMyLocation();
            //default좌표(강원대학교 한빛관)를 기반으로 WarningInfoList를 받아온다.
            new getWarningInfoListAsyncTask().execute("37.8688359", "127.7377907");
        }
    };

    /* on Callout Overlay Listener susang data*/
    private final NMapOverlayManager.OnCalloutOverlayListener onCalloutOverlayListener = new NMapOverlayManager.OnCalloutOverlayListener() {
        //POIitem 마커 선택시. 말풍선띄우는 기능!
        @Override
        public NMapCalloutOverlay onCreateCalloutOverlay(NMapOverlay itemOverlay, NMapOverlayItem overlayItem,
                                                         Rect itemBounds) {

            // arraylist data 초기화
            contents.clear();

            // 겹친 아이템 처리
            if (itemOverlay instanceof NMapPOIdataOverlay) {
                NMapPOIdataOverlay poiDataOverlay = (NMapPOIdataOverlay)itemOverlay;

                // check if it is selected by touch event
                // overlayItem 은 선택된 마커
                // poiItem 은 선택된 마커와 겹친 마커들
                if (!poiDataOverlay.isFocusedBySelectItem()) {
                    int countOfOverlappedItems = 1;

                    NMapPOIdata poiData = poiDataOverlay.getPOIdata();
                    for (int i = 0; i < poiData.count(); i++) {
                        NMapPOIitem poiItem = poiData.getPOIitem(i);

                        // skip selected item
                        if (poiItem == overlayItem) {
                            contents.add(Integer.toString(poiItem.getId()));
                            continue;
                        }

                        // check if overlapped or not
                        if (Rect.intersects(poiItem.getBoundsInScreen(), overlayItem.getBoundsInScreen())) {
                            countOfOverlappedItems++;
                            // 겹친 아이템들의 제목을 리스트에 저장
                            contents.add(Integer.toString(poiItem.getId()));
                        }
                    }

                    if (countOfOverlappedItems > 1) {
                        Intent Intent = new Intent(MainMapActivity.this, NMapCalloutListOverlay.class);
                        Intent.putExtra("CONTENT", contents);
                        startActivity(Intent);

                        return null;
                    }
                }
            }

            // use custom old callout overlay
            if (overlayItem instanceof NMapPOIitem) {
                NMapPOIitem poiItem = (NMapPOIitem)overlayItem;

                if (poiItem.showRightButton()) {
                    return new NMapCalloutCustomOldOverlay(itemOverlay, overlayItem, itemBounds,
                            mMapViewerResourceProvider);
                }
            }

            // use custom callout overlay
            //return new NMapCalloutCustomOverlay(itemOverlay, overlayItem, itemBounds, mMapViewerResourceProvider);

            // set basic callout overlay
            // return new NMapCalloutBasicOverlay(itemOverlay, overlayItem, itemBounds);

            // 겹치지 않았을때 리턴되는 말풍선
            return new NMapCalloutCustomOldOverlay(itemOverlay, overlayItem, itemBounds, mMapViewerResourceProvider);
        }
    };

    /* POI data State Change Listener*/
    private final NMapPOIdataOverlay.OnStateChangeListener onPOIdataStateChangeListener = new NMapPOIdataOverlay.OnStateChangeListener() {
        //말풍선 터치 시 ShowWarningInfo 페이지 보여주기
        @Override
        public void onCalloutClick(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
            Intent lIntent = new Intent(MainMapActivity.this, ShowWarningInfoActivity.class);
            lIntent.putExtra("WINFOIDX", item.getId() + "");
            startActivityForResult(lIntent, 1);
        }

        @Override
        public void onFocusChanged(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {

        }
    };

    /* MapView State Change Listener*/
    private final NMapView.OnMapStateChangeListener onMapViewStateChangeListener = new NMapView.OnMapStateChangeListener() {

        @Override
        public void onMapInitHandler(NMapView mapView, NMapError errorInfo) {

            if (errorInfo == null) { // success
                mMapController.setMapCenter(new NGeoPoint(127.7377907, 37.8688359), 11);//강원대학교 한빛관 좌표. 초기설정
            } else { // fail
                Toast.makeText(MainMapActivity.this, errorInfo.toString(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onAnimationStateChange(NMapView mapView, int animType, int animState) {

        }

        @Override
        public void onMapCenterChange(NMapView mapView, NGeoPoint center) {
            //지도의 중심이 변했을 경우
            new getWarningInfoListAsyncTask().execute(Double.toString(center.getLatitude()), Double.toString(center.getLongitude()));

        }

        @Override
        public void onZoomLevelChange(NMapView mapView, int level) {

        }

        @Override
        public void onMapCenterChangeFine(NMapView mapView) {

        }
    };

}
