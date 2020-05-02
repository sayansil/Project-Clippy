package productivity.clippy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.Bind;

public class SignupActivity extends AppCompatActivity{

    private static final String TAG = "SignupActivity";
    String _urllink;

    public static final String PREFS_NAME = "loginID";
    SharedPreferences credentials;
    SharedPreferences.Editor editor;

    @Bind(R.id.input_username) EditText _userText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.input_reEnterPassword) EditText _reEnterPasswordText;
    @Bind(R.id.btn_signup) Button _signupButton;
    @Bind(R.id.link_login) TextView _loginLink;
    @Bind(R.id.checkBox) CheckBox _agreeTC;
    @Bind(R.id.TandC) TextView _tnc;
    @Bind(R.id.serial_key) EditText _serialKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.transition.push_left_in, R.transition.push_left_out);
            }
        });

        _tnc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),TNCActivity.class);
                startActivity(intent);
                overridePendingTransition(R.transition.push_left_in, R.transition.push_left_out);
            }
        });
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(false);
    }

    public void signup(){
        Log.d(TAG, "Signup");

        if (!validate(_userText.getText().toString(),
                _passwordText.getText().toString(),
                _reEnterPasswordText.getText().toString(),
                _serialKey.getText().toString())) {
            onSignupFailed("Sign Up Failed");
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy );

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {

                        String password = _passwordText.getText().toString();
                        String username = _userText.getText().toString();
                        String serialkey = _serialKey.getText().toString();

                        if(accountExists(username))
                            onSignupFailed("Account already exists!");
                        else if(!verify_account(username, serialkey))
                            onSignupFailed("Account not verified!");
                        else {
                            boolean success = uploadAccount(username, password);

                            if(success) {
                                removeKeyfromCloud(serialkey);
                                credentials = getSharedPreferences(PREFS_NAME, 0);
                                editor = credentials.edit();
                                editor.putString("User ID", "");
                                editor.putString("User Password", "");
                                editor.apply();
                                onSignupSuccess();
                            }
                            else
                                onSignupFailed("Sign Up Failed");
                        }
                        progressDialog.dismiss();
                    }
                }, 2000);
    }

    public boolean uploadAccount(String username, String password)
    {
        String data = username + ";" + password;
        _urllink = "http://dunetest.000webhostapp.com/2346_newCall6764.php?var3=xy&txt="
                + data.trim();

        try {
            httpRequest(_urllink);
        } catch (Exception e) {
            Log.e("Set Cloud Error: " , e.toString());
        }

        _urllink = "http://dunetest.000webhostapp.com/2346_newCall6764.php?var0=xy&txt="
                + data.substring(0, data.indexOf(';')) + ".txt";

        try {
            httpRequest(_urllink);
        } catch (Exception e) {
            Log.e("Set Cloud Error: " , e.toString());
        }
        return true;
    }

    public boolean accountExists(String username)
    {
        _urllink = "http://dunetest.000webhostapp.com/2346_newCall6764.php?var4=xy";
        String line;

        try {
            URL _url = new URL(_urllink);
            HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((line = in.readLine()) != null){

                if(line.substring(0,line.indexOf(';')).equals(username)) {
                    in.close();
                    return true;
                }
            }

            in.close();
            return false;

        } catch(Exception e) {
            Log.e("Get Cloud Error: " , e.toString());
        }

        return true;
    }

    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        finish();

        Toast.makeText(getBaseContext(), "New Account created", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void onSignupFailed(String error) {
        Toast.makeText(getBaseContext(), error, Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate(String username, String password, String reEnterPassword, String serial) {
        boolean valid = true;
        if(username.length() < 5 || username.length() > 15){
            _userText.setError("Between 5 and 15 characters");
            valid = false;
        }
        else {
            _userText.setError(null);
        }

        if (password.isEmpty() || password.length() < 5 || password.length() > 15) {
            _passwordText.setError("Between 5 and 15 characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (!reEnterPassword.equals(password)) {
            _reEnterPasswordText.setError("Password Do not match");
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }

        if(!_agreeTC.isChecked()){
            _tnc.setError("You didn't agree to our T&C :(");
            valid = false;
        }
        else {
            _tnc.setError(null);
        }

        if(serial.length() != 10){
            _serialKey.setError("Invalid Serial key");
            valid = false;
        }
        else {
            _serialKey.setError(null);
        }

        return valid;
    }

    public boolean verify_account(String username, String serialkey)
    {
        boolean valid = username.length() > 0;
        valid = valid &&(serialkey.length() == 10) && !Pattern.compile("[^0-9]").matcher(serialkey).find();


        _urllink = "http://dunetest.000webhostapp.com/2346_newCall6764.php?var5=xy";
        String line;

        try {
            URL _url = new URL(_urllink);
            HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((line = in.readLine()) != null){
                if(line.equals(serialkey)) {
                    in.close();
                    return valid;
                }
            }

            in.close();
            return false;

        } catch(Exception e) {
            Log.e("Get Cloud Error: " , e.toString());
        }

        return false;
    }

    public void removeKeyfromCloud(String serialkey){
        String line, serials = "";

        try {
            _urllink = "http://dunetest.000webhostapp.com/2346_newCall6764.php?var5=xy";
            URL _url = new URL(_urllink);
            HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = in.readLine()) != null){
                if(!line.equals(serialkey))
                serials = serials + ";" +line.trim();
            }
            in.close();

            _urllink = "http://dunetest.000webhostapp.com/2346_newCall6764.php?var7=xy";
            _url = new URL(_urllink);
            conn = (HttpURLConnection) _url.openConnection();
            conn.setRequestMethod("GET");
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            in.readLine();
            in.close();

            serials = serials.substring(1) + ";";
            while(!serials.isEmpty()){
                line = serials.substring(0,serials.indexOf(";"));
                serials = serials.substring(serials.indexOf(";") + 1);

                if(!line.trim().isEmpty()) {
                    _urllink = "http://dunetest.000webhostapp.com/2346_newCall6764.php?var6=valid&txt=" + line;
                    _url = new URL(_urllink);
                    conn = (HttpURLConnection) _url.openConnection();
                    conn.setRequestMethod("GET");
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    in.readLine();
                    in.close();
                }
            }
        } catch(Exception e) {
            Log.e("Get Cloud Error: " , e.toString());
        }

    }

    private StringBuilder httpRequest(String urllink) throws Exception{
        String line;
        StringBuilder sb = new StringBuilder();
        URL _url = new URL(urllink);
        HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        while ((line = in.readLine()) != null)
            sb.append(line);

        in.close();

        return sb;
    }
}
