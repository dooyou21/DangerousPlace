package capston.stol.dangerousplace.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by sbpark1 on 2016-05-24.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "MyInstanceIDLS";

    @Override
    public void onTokenRefresh(){
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
