package capston.stol.dangerousplace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import capston.stol.dangerousplace.util.Constant;

import static capston.stol.dangerousplace.util.EncodeString.EncodeString;

public class GoogleSignInActivity extends AppCompatActivity {

    static final int RC_SING_IN = 0;

    SharedPreferences setting;
    SharedPreferences.Editor editor;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sign_in);

// Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

// Build a GoogleApiClient with access to the Google Sign-In API and the
// options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this /* FragmentActivity */, this/*OnConnectionFailedListener*/)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SING_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("resultCode", resultCode + "");

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SING_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {

        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("handle", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.//이미로그인되어있는 상태->true. 이미 데이터 있으면 회원가입 안하고 걍 로그인만되어야하는데...
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.i("handle", "success:" + acct.getEmail());
            //url 보냄
            new SignInAsyncTask().execute(acct.getEmail());
        } else {
            // Signed out, show unauthenticated UI.
            Log.d("handle", "nooooo");
        }
    }

    public class SignInAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... param){

            String useremail = param[0];
            try {
                Properties prop = new Properties();
                prop.setProperty(Constant.PARAMETER.Email, useremail);
                String encodedString = EncodeString(prop);

                Log.e("email", useremail);

                URL url = new URL(Constant.URL.Base + Constant.URL.GoogleSignIn + encodedString);
                HttpURLConnection conn= (HttpURLConnection)url.openConnection();
                conn.setUseCaches(false);
                conn.setRequestMethod("GET");



                try {
                    Log.d("asynctask", "responseCode " + conn.getResponseCode());
                    Log.e("Signin ", "success");

                    String usrIdx = conn.getHeaderField("usrIdx");
                    Log.e("usrIdx", "usrIdx: " + usrIdx);

                    setting = getSharedPreferences("setting", 0);
                    editor = setting.edit();
                    editor.putString("UserEmail", useremail);
                    editor.putString("UserIdx", usrIdx);
                    editor.commit();

                    finish();
                    startActivity(new Intent(GoogleSignInActivity.this, MainMapActivity.class));

                } catch (Exception e){
                    Log.e("Signin ", "error");
                    e.printStackTrace();
                }

                conn.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
