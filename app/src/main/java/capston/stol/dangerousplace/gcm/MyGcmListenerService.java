package capston.stol.dangerousplace.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import capston.stol.dangerousplace.R;
import capston.stol.dangerousplace.ShowWarningInfoActivity;

/**
 * Created by sbpark1 on 2016-05-24.
 */
public class MyGcmListenerService extends GcmListenerService {
    private static final String TAG = "MyGcmListenerService";

    private String SENDER_ID;
    /**
     *
     //* @param from SenderID 값을 받아온다.
     //* @param data Set형태로 GCM으로 받은 데이터 payload이다.
     */
   // @Override
    public void onMessageReceived(String from, Bundle data) {

        String title = null;
        String message = null;
        int warningIdx = 0;
        try {
            title = URLDecoder.decode(data.getString("title"),"euc-kr");
            message = URLDecoder.decode(data.getString("message"),"euc-kr");
            warningIdx = Integer.parseInt(data.getString("idx"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

       // Log.d(TAG, "From: " + from);
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "warningIdx: "+warningIdx);

        // GCM으로 받은 메세지를 디바이스에 알려주는 sendNotification()을 호출한다.
        sendNotification(title, message,warningIdx);
    }


    /**
     * 실제 디바에스에 GCM으로부터 받은 메세지를 알려주는 함수이다. 디바이스 Notification Center에 나타난다.
     * @param title
     * @param message
     */
    private void sendNotification(String title, String message, int warningIdx) {
        Intent intent = new Intent(this, ShowWarningInfoActivity.class);
        intent.putExtra("WINFOIDX",warningIdx+"");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);
                    PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.profile)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
