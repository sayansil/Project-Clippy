package productivity.clippy;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.Bind;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoggedInActivity extends AppCompatActivity {
    private String _urllink;
    private Handler mHandler;
    ClipboardManager clipboard;
    String prev_pasteData;
    ContentResolver cr;
    SimpleDateFormat simpleDateFormat;

    public static final String PREFS_NAME = "loginID";
    SharedPreferences credentials;
    String userid;
    String password;
    SharedPreferences.Editor editor;

    @Bind(R.id.logged_user) TextView userView;
    @Bind(R.id.logout_btn) Button _outBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loggedin);
        ButterKnife.bind(this);

        credentials = getSharedPreferences(PREFS_NAME, 0);
        userid = credentials.getString("User ID", "");
        password = credentials.getString("User Password", "");

        userView.setText(getString(R.string.loggedin_text, userid));

        prev_pasteData = "[00000000000000]";
        simpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmss", Locale.US);
        mHandler = new Handler();

        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy );

        _outBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(LoggedInActivity.this,
                            R.style.AppTheme_Light_Dialog);
                    builder.setTitle("Re-enter password:");

                    final EditText confirm = new EditText(LoggedInActivity.this);
                    confirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    confirm.setHint("Password");
                    builder.setView(confirm);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(confirm.getText().toString().equals(password)){
                                editor = credentials.edit();
                                editor.putString("User ID", "");
                                editor.putString("User Password", "");
                                editor.apply();
                                finish();
                                stopRepeatingTask();
                            }
                            else{
                                Toast.makeText(getBaseContext(), "Invalid Password", Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                            }
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
        });

        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        startRepeatingTask();
        cr = getContentResolver();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(false);
        Toast.makeText(getBaseContext(), "Minimize this app", Toast.LENGTH_SHORT).show();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {

            String pasteData;
            String cloudData = "[00000000000000]";

            try {
                pasteData = getDeviceData();
                cloudData = getCloudData(); // "[00000000000000]";

                if(pasteData.equals(removeStamp(prev_pasteData))){
                    pasteData = prev_pasteData;
                }
                else {
                    pasteData = putStamp(pasteData);
                    prev_pasteData = pasteData;
                }

                if (!removeStamp(pasteData).equals(removeStamp(cloudData))) {
                    if(compareStamps(getStamp(pasteData), getStamp(cloudData)) > 0)
                        setCloudData(pasteData);
                    else if(compareStamps(getStamp(pasteData), getStamp(cloudData)) < 0) {
                        prev_pasteData = cloudData;
                        setDeviceData(removeStamp(cloudData));
                    }
                }
            } finally {
                mHandler.postDelayed(mStatusChecker, 2000);

            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    String getCloudData(){
        StringBuilder sb = new StringBuilder("[00000000000000]");

        _urllink="http://dunetest.000webhostapp.com/2346_newCall6764.php?var1=valid&txt="
                + userid + ".txt";
        try {
            sb = httpRequest(_urllink);
        } catch(Exception e) {
            Log.e("Get Cloud status: ", e.toString());
        }

        return sb.toString().replaceAll("~", " ").replaceAll("`", "\n");
    }

    void setCloudData(String data){
        data = data.replaceAll("\\n", "`").replaceAll("\\s", "~");
        Log.e("Set Cloud data: ", data);
        _urllink = "http://dunetest.000webhostapp.com/2346_newCall6764.php?var2=xy&txt="
                + userid + ".txt" + data.trim();

        try {
            httpRequest(_urllink);
        } catch (Exception e) {
            Log.e("Set Cloud status: ", e.toString());
        }
    }

    String getDeviceData(){
        String data = "[00000000000000]";
        if (clipboard.hasPrimaryClip()) {
            ClipData clip = clipboard.getPrimaryClip();
            data = clip.getItemAt(0).getText().toString();
        }
        return data;
    }

    void setDeviceData(String data)
    {
        ClipData clip = ClipData.newPlainText("Copied Text", data);
        clipboard.setPrimaryClip(clip);
    }

    String putStamp(String data){
        String stamp = simpleDateFormat.format(new Date());
        return "[" + stamp + "]" + data;
    }
    String removeStamp(String data){
        return data.substring(16);
    }
    String getStamp(String data){
        return data.substring(1, 15);
    }
    int compareStamps(String s1, String s2){
        return s1.compareTo(s2);
    }

    private StringBuilder httpRequest(String urllink) throws Exception{

        StringBuilder sb = new StringBuilder();
        String line;
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
