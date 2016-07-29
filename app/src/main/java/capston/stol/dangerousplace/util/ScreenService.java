package capston.stol.dangerousplace.util;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * Created by Dasol on 2016-05-25.
 */
public class ScreenService extends Service {

    private capston.stol.dangerousplace.util.ScreenReceiver mReceiver = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//service는 항상 맨처음 oncreate->onstartCommand순서로 실행되므로 안써도될꺼같은데?
//        mReceiver = new capston.stol.dangerousplace.util.ScreenReceiver();
//        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
//        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);

        if(intent != null){
            if(intent.getAction()==null){
                if(mReceiver==null){
                    mReceiver = new capston.stol.dangerousplace.util.ScreenReceiver();
                    IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
                    registerReceiver(mReceiver, filter);
                }
            }
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy(){//키락 서비스 종료. 종료할 때 왜 reenable을 호출하지?
        super.onDestroy();

        if(false){ // 한번도 Receiver가 동작하지 않았을 경우에는 reenable이 불가능하다.
            mReceiver.reenableKeyguard();
        }

        if(mReceiver != null){
            unregisterReceiver(mReceiver);
        }
    }
}