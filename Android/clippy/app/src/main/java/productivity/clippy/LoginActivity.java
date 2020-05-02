package productivity.clippy;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.ButterKnife;
import butterknife.Bind;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    public static final String PREFS_NAME = "loginID";
    SharedPreferences credentials;
    SharedPreferences.Editor editor;
    String old_userid;
    String old_userpd;
    boolean newlogin;

    @Bind(R.id.input_username) EditText _userText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.btn_login) Button _loginButton;
    @Bind(R.id.link_signup) TextView _signupLink;
    @Bind(R.id.invis) TextView inView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        newlogin = true;
        credentials = getSharedPreferences(PREFS_NAME, 0);
        old_userid = credentials.getString("User ID", "");
        old_userpd = credentials.getString("User Password", "");

        if(old_userid.length() != 0){
            newlogin = false;
            login(old_userid, old_userpd);
        }

        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(_userText.getText().toString(), _passwordText.getText().toString());
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.transition.push_left_in , R.transition.push_left_out);
            }
        });

    }


    public void login(final String username, final String password) {
        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy );

        Log.d(TAG, "Login");


        if (!validate(username, password)) {
            onLoginFailed("Login failed");
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");

        progressDialog.show();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        String user = newlogin?username:old_userid;
                        String pass = newlogin?password:old_userpd;

                        int v = verify_password(user, pass);
                        if(v == 0)
                            onLoginFailed("Account does not exist");
                        else if(v == 1){
                            _passwordText.setError("Invalid Password");
                            onLoginFailed("Login failed");
                        }
                        else {
                            Toast.makeText(getBaseContext(), "You have logged in", Toast.LENGTH_SHORT).show();

                            if(newlogin){
                                credentials = getSharedPreferences(PREFS_NAME, 0);
                                editor = credentials.edit();
                                editor.putString("User ID", user);
                                editor.putString("User Password", pass);
                                editor.apply();
                            }
                            onLoginSuccess();
                        }
                        progressDialog.dismiss();
                    }
                }, 2000);
    }

    public int verify_password(String username, String password)
    {
        boolean valid = !username.isEmpty();

        String _urllink = "http://dunetest.000webhostapp.com/2346_newCall6764.php?var4=xy";
        String line;

        try {
            URL _url = new URL(_urllink);
            HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
            conn.setRequestMethod("GET");


            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((line = in.readLine()) != null){
                if(line.substring(0,line.indexOf(';')).equals(username) && line.substring(line.indexOf(';')+1).equals(password)) {
                    in.close();
                    return valid?2:0;
                }
                if(line.substring(0,line.indexOf(';')).equals(username)) {
                    in.close();
                    return valid?1:0;
                }
            }

            in.close();
            return 0;

        } catch(Exception e) {
            System.out.println("Get Cloud Error: " + e.toString());
        }
        return 0;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();

        Intent intent = new Intent(this, LoggedInActivity.class);
        startActivity(intent);
    }

    public void onLoginFailed(String error) {
        Toast.makeText(getBaseContext(), error, Toast.LENGTH_LONG).show();

        credentials = getSharedPreferences(PREFS_NAME, 0);
        editor = credentials.edit();
        editor.putString("User ID", "");
        editor.putString("User Password", "");
        editor.apply();

        _loginButton.setEnabled(true);
    }

    public boolean validate(String username, String password) {
        boolean valid = true;
        if (password.isEmpty() || password.length() < 5 || password.length() > 15) {
            _passwordText.setError("Between 5 and 15 characters");
            valid = false;
        }
        else {
            _passwordText.setError(null);
        }
        if (username.isEmpty() || username.length() < 5 || username.length() > 15) {
            _userText.setError("Between 5 and 15 characters");
            valid = false;
        }
        else {
            _userText.setError(null);
        }

        return valid;
    }



}
