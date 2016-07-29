package capston.stol.dangerousplace.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import capston.stol.dangerousplace.MainMapActivity;
import capston.stol.dangerousplace.R;

/**
 * Created by sbpark1 on 2016-05-25.
 */
public abstract class NotificationBar extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d("service","onCreate 실행");
    }

    @Override
    public void onDestroy() {
        Log.d("service","onDestroy 실행");
        mRunning = false;
    }


    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
//               switch(msg.what) {}
//               Toast.makeText(getApplicationContext(), "알림!", 0).show();
            NotificationManager manager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(NotificationBar.this);
            builder.setSmallIcon(R.drawable.profile)
                    .setContentTitle("알립니다")
                    .setContentText("왔다네~왔다네~ 내~가 왔다네~")
                    .setAutoCancel(true); // 알림바에서 자동 삭제
                    //.setVibrate(new long[]{1000,2000,1000,3000,1000,4000});
            // autoCancel : 한번 누르면 알림바에서 사라진다.
            // vibrate : 쉬고, 울리고, 쉬고, 울리고... 밀리세컨
            // 진동이 되려면 AndroidManifest.xml에 진동 권한을 줘야 한다.

            // 알람 클릭시 MainActivity를 화면에 띄운다.
            Intent intent = new Intent(getApplicationContext(),MainMapActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext()
                    , 0
                    , intent
                    , Intent.FILL_IN_ACTION);
            builder.setContentIntent(pIntent);
            manager.notify(1, builder.build());
        };
    };
    protected boolean mRunning;


    // 제일 중요한 메서드! (서비스 작동내용을 넣어준다.)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("service", "onStartCommand 실행");
        final int time = intent.getIntExtra("time", 0);
//          Toast.makeText(this, "안녕~ 난 서비스 : "+time, 0).show();

        // handler 통한 Thread 이용
        new Thread(new Runnable() {

            @Override
            public void run() {
                mRunning = true;
                while(mRunning) {
                    SystemClock.sleep(time * 1000);
                    mHandler.sendEmptyMessage(0);
                }
            }

        }).start();


        return START_STICKY_COMPATIBILITY;
    }
}
