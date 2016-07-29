package capston.stol.dangerousplace.util;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import capston.stol.dangerousplace.R;
import capston.stol.dangerousplace.SavePathActivity;

/**
 * Created by sjlee on 2016-05-15.
 */

public class GPSTrackingService extends Service {
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 10; //==>20초간격
    private static final float LOCATION_DISTANCE = 10f;
    private static String Path = "";//경로저장값
    private static int counter = 0;//무한루프 방지. 위치추적 종료를 위한 counter
    private static int destroyTimeLimit = 14;//5분이면 14. count limit
    private NotificationManager NotiManager;
    private Notification Noti;
    private NotificationCompat.Builder NotiBuilder;
    private Intent intent;//intent for notification
    private PendingIntent pendingIntent;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;
        String prov;


        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            prov = provider;
            mLastLocation = new Location(provider);
        }

        public Location getLocation(){
            return mLastLocation;
        }

        @Override
        public void onLocationChanged(Location location) {//위치가 변했을 경우

            Log.e(TAG, "onLocationChanged: " + location.getLatitude()*1000+","+location.getLongitude()*1000 +"/" + prov);

            int nlat = (int)(location.getLatitude()*1000);//new
            int nlng = (int)(location.getLongitude()*1000);
            int olat = (int)(mLastLocation.getLatitude()*1000);//old
            int olng = (int)(mLastLocation.getLongitude()*1000);

            if((nlat==olat)&&(nlng==olng)){//위치가 변했는지 검사. 소숫점 셋째자리까지 같으면 움직이지 않은 것으로 간주한다.
                counter++;
//                Path = Path + "|" + location.getLatitude() + "," + location.getLongitude();//test용. 지워야됨
                Log.d(TAG, "counter: " + counter);
                if(counter>destroyTimeLimit){
                    Log.d(TAG, "should be destoryed!" + counter);
                    stopSelf();
                }
            }else{
                counter = 0;
                Path = Path + " " + location.getLatitude() + "," + location.getLongitude();
//                intent.putExtra("PATH", Path);//path를 intent에 추가
            }
            Notification();
            mLastLocation.set(location);

        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        NotiManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Path = "";

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");

        initializeLocationManager();

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        Notification();

    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");

        Log.d(TAG, "Path when distroyed :" + Path);

        intent = new Intent(GPSTrackingService.this, SavePathActivity.class);
        intent.putExtra("PATH", Path);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.


                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }


    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void Notification(){

        intent = new Intent(GPSTrackingService.this, SavePathActivity.class);
        intent.putExtra("PATH", Path);

        pendingIntent = PendingIntent.getActivity(GPSTrackingService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotiBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("\'여수해\' 위치추적중")
                .setContentText("위치추적을 멈추고 싶다면 터치해주세요!")
                .setSmallIcon(R.drawable.gps)
                .setTicker("위치추적중")
                .setContentIntent(pendingIntent);
        Noti = NotiBuilder.build();
        Noti.defaults = Notification.DEFAULT_SOUND;
        Noti.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_NO_CLEAR;

        startForeground(777, Noti);
    }

}
