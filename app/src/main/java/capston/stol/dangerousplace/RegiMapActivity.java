package capston.stol.dangerousplace;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
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
import com.nhn.android.mapviewer.overlay.NMapOverlayManager.OnCalloutOverlayListener;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;

import capston.stol.dangerousplace.util.Constant;
import capston.stol.dangerousplace.util.NMapPOIflagType;
import capston.stol.dangerousplace.util.NMapViewerResourceProvider;

//오버레이 터치시 이벤트 연결

/**
 * Created by Dasol on 2016-01-04.
 */
public class RegiMapActivity extends NMapActivity implements NMapView.OnMapStateChangeListener, OnCalloutOverlayListener{

    //네이버 맵 객체
    private NMapView mMapView = null;
    //맵 컨트롤러
    private NMapController mMapController = null;

    // 오버레이의 리소스를 제공하기 위한 객체
    NMapViewerResourceProvider mMapViewerResourceProvider = null;
    // 오버레이 관리자
    NMapOverlayManager mOverlayManager;

    NMapMyLocationOverlay mMyLocationOverlay;
    NMapLocationManager mMapLocationManager;
    NMapCompassManager mMapCompassManager;

    private String gpsInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regimap);

        Double xcoord=null, ycoord=null;


        String insertOrUpdate = getIntent().getExtras().getString("INSERTORUPDATE");

        if(insertOrUpdate.equals("update")) {
            xcoord = getIntent().getExtras().getDouble("XCOORD");
            ycoord = getIntent().getExtras().getDouble("YCOORD");
        }

        /**************지도 초기화 ****************/

        //네이버 지도 객체 생성
        mMapView = new NMapView(this);

        //지도 객체로부터 컨트롤러 추출
        mMapController = mMapView.getMapController();

        // 네이버 지도 객체에 APIKEY 지정
//        mMapView.setApiKey(Constant.KEY.NMAPAPIKEY);
        mMapView.setClientId(Constant.KEY.NMAPCLIENTID);

        //지도 터치 활성화
        mMapView.setClickable(true);

        //확대/축소 활성화
        mMapView.setBuiltInZoomControls(true, null);

        // register listener for map state changes
        mMapView.setOnMapStateChangeListener(this);

        // set the activity content to the map view
        setContentView(mMapView);

        /****** 오버레이 관련 선언 *********/
        // 오버레이 리소스 관리객체 할당
        mMapViewerResourceProvider = new NMapViewerResourceProvider(this);

        // 오버레이 관리자 추가
        mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);

        // 사용자가 찍을 마커 선언
        int marker1 = NMapPOIflagType.MARKER_BLACK;

        // POI 데이터 선언
        NMapPOIdata poiData = new NMapPOIdata(1, mMapViewerResourceProvider);

        // 마커 시작
        poiData.beginPOIdata(1);

        //marker1을 가져와서 poidata에 넣는다
        NMapPOIitem item = poiData.addPOIitem(null, "Touch & Drag to Move", marker1, 0);

        if(insertOrUpdate.equals("update")){
            //update일 경우 받아온 위치에 마커 표현
            item.setPoint(new NGeoPoint(ycoord, xcoord));
        } else {
            //insert일 경우 맵뷰의 중앙으로 위치를 초기화
            item.setPoint(mMapController.getMapCenter());
        }
        // set floating mode
        item.setFloatingMode(NMapPOIitem.FLOATING_TOUCH | NMapPOIitem.FLOATING_DRAG);

        // show right button on callout
        item.setRightButton(true);

        poiData.endPOIdata();

        // POI 데이터 오버레이 생성
        NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);

        if (poiDataOverlay != null) {
            // 오버레이 아이템의 위치 변경을 처리하기 위한 이벤트 리스너
            poiDataOverlay.setOnFloatingItemChangeListener(onPOIdataFloatingItemChangeListener);

        }

        // 오버레이 아이템 선택시 발생하는 이벤트
        mOverlayManager.setOnCalloutOverlayListener(this);

        /****** 오버레이 끝 *********/

//        /******** 현재 위치 시작 *********/
//        //위치 관리 매니저 객체 생성
//        mMapLocationManager = new NMapLocationManager(this);
//        //현재 위치 변경시 호출되는 콜백 인터페이스를 설정
//        mMapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);
//        //NMapMyLocationOverlay 객체 생성
//        mMyLocationOverlay = mOverlayManager.createMyLocationOverlay(mMapLocationManager, mMapCompassManager);
//
//        //내위치잡기 시작
//        MyLocation();
    }

    /**
     * 지도 레벨 변경 시 호출되며 변경된 지도 레벨이 파라미터로 전달된다.
     */
    @Override
    public void onZoomLevelChange(NMapView mapview, int level) {}

    @Override
    public void onAnimationStateChange(NMapView nMapView, int i, int i1) {

    }

    /**
     * 지도가 초기화된 후 호출된다.
     * 정상적으로 초기화되면 errorInfo 객체는 null이 전달되며,
     * 초기화 실패 시 errorInfo객체에 에러 원인이 전달된다
     */
    public void onMapInitHandler(NMapView mapView, NMapError errorInfo) {
        if (errorInfo == null) { // success
            //mMapController.setMapCenter(new NGeoPoint(126.978371, 37.5666091), 11);
        } else { // fail
            Log.e("NMAP", "onMapInitHandler: error=" + errorInfo.toString());
        }
    }

    @Override
    public void onMapCenterChange(NMapView nMapView, NGeoPoint nGeoPoint) {

    }

    @Override
    public void onMapCenterChangeFine(NMapView nMapView) {

    }

    /******** 오버레이 관련 함수들 *********/
    private final NMapPOIdataOverlay.OnFloatingItemChangeListener onPOIdataFloatingItemChangeListener = new NMapPOIdataOverlay.OnFloatingItemChangeListener() {

        // 오버레이 아이템 위치가 변경될때 실행됨
        // point에 좌표값 저장
        @Override
        public void onPointChanged(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
            NGeoPoint point = item.getPoint();
            gpsInfo = point.toString();
//            Log.d("NMAP", "onPointChanged: point=" + gpsInfo);

            AlertDialog.Builder dialog = new AlertDialog.Builder(RegiMapActivity.this);

            dialog.setMessage("해당 위치로 등록하시겠습니까?");
            dialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // 수상한일 발생 위치 선택 완료
                    // 이전 액티비티로 돌아간다

                    Intent lIntent = new Intent().putExtra("GPS_DATA", gpsInfo);
//                    mMapLocationManager.disableMyLocation();
                    setResult(RESULT_OK, lIntent);
                    finish();
                }
            });
            dialog.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // 수상한 일 발생 위치 다시 선택
                    dialog.cancel();
                }
            });
            dialog.show();
        }
    };

    //오버레이 아이템을 선택 시 호출
    @Override
    public NMapCalloutOverlay onCreateCalloutOverlay(NMapOverlay arg0,
                                                     NMapOverlayItem arg1, Rect arg2) {
        // 오버레이 아이템의 저장된 title을 화면에 띄움
        Toast.makeText(this, arg1.getTitle(), Toast.LENGTH_SHORT).show();

        return null;
    }

//    /* MyLocation Listener */
//    //위치 변경 콜백 인터페이스 정의
//    private final NMapLocationManager.OnLocationChangeListener onMyLocationChangeListener = new NMapLocationManager.OnLocationChangeListener() {
//        //위치가 변경되면 호출
//        @Override
//        public boolean onLocationChanged(NMapLocationManager locationManager, NGeoPoint myLocation) {
//
//            mMapLocationManager.disableMyLocation();
//
//            if (mMapController != null) {
//                mMapController.animateTo(myLocation);
//            }
//
//            return true;
//        }
//
//        //정해진 시간 내에 위치 탐색 실패시
//        @Override
//        public void onLocationUpdateTimeout(NMapLocationManager locationManager) {
//
//            Toast.makeText(RegiMapActivity.this, "timeout", Toast.LENGTH_LONG).show();
////            mMapLocationManager.disableMyLocation();
//        }
//
//        //현재 위치가 지도 상에 표시할 수 있는 범위 밖인 경우
//        @Override
//        public void onLocationUnavailableArea(NMapLocationManager locationManager, NGeoPoint myLocation) {
//
//            Toast.makeText(RegiMapActivity.this, "unavailable location", Toast.LENGTH_LONG).show();
//
//            mMapLocationManager.disableMyLocation();
//        }
//
//    };
//
//    private void MyLocation() {
//        if (mMyLocationOverlay != null) {
//            if (!mOverlayManager.hasOverlay(mMyLocationOverlay)) {
//                mOverlayManager.addOverlay(mMyLocationOverlay);
//            }
//            if(!mMapLocationManager.enableMyLocation(true)){//GPS 꺼져있는 경우
////                Toast.makeText(RegiMapActivity.this, "Please enable a My Location source in system settings",
////                        Toast.LENGTH_LONG).show();
////                Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
////                startActivity(goToSettings);
//            }
//            mMapView.postInvalidate();
//        }
//    }
//    /********* MyLocation end **********/

    // 이전 activity로 돌아가기
    public void onBackPressed() {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setMessage("좌표 지정을 취소하시겠습니까?");
        d.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // 현재 activity 종료
                // 이전 activity로 돌아간다
//                if(mMapLocationManager.isMyLocationEnabled()) { mMapLocationManager.disableMyLocation(); }
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        d.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        d.show();
    }
}
