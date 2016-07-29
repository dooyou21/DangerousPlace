package capston.stol.dangerousplace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {
    SharedPreferences setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        setting = getSharedPreferences("setting", 0);
        String useremail = setting.getString("UserEmail", null);

        if(useremail == null) {
            finish();
            startActivity(new Intent(IntroActivity.this, GoogleSignInActivity.class));
        } else {
            finish();
            //useremail 로 로그인
            startActivity(new Intent(IntroActivity.this, MainMapActivity.class));
        }
    }

}
