package in.hng.mpos.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;

import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.service.PosprinterService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import in.hng.mpos.BuildConfig;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.MainActivityTemp;
import in.hng.mpos.R;
import in.hng.mpos.helper.ApiCall;
import in.hng.mpos.helper.Log;

public class SplashScreen extends BaseActivity {

    private String TAG = "Splash Screen";

    private Intent myintent;
    private TextView version;
    String versionName = BuildConfig.VERSION_NAME;

    private Crashlytics mCrashlytics;
    /**
     * The thread to process splash screen events
     */
    private Thread mSplashThread;
    private boolean logged;
    private String PATH, DEV_ID;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Fabric.with(this, new Crashlytics());
        //Crashlytics.getInstance().crash();

        setContentView(R.layout.activity_splash_screen);

        try {
            deleteCache(getApplicationContext());
            version = findViewById(R.id.txtversion);
            version.setText("Version " + versionName);
            myintent = new Intent(this, Login.class);
//            myintent = new Intent(SplashScreen.this, MainActivityTemp.class);

            DEV_ID = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
//            DEV_ID = "e3175dc0551e269f";
            Log.d(TAG, "Device ID ==> " + DEV_ID);
            Toast.makeText(SplashScreen.this,DEV_ID, Toast.LENGTH_SHORT).show();
            Log.e("DEV_ID",DEV_ID);

            //  DEV_ID = "f719f7b9d4b1d256";
            PATH = getString(R.string.API_URL) + "getURLdetail?deviceID=" + DEV_ID;
            makeLoginApiCall(PATH);
        } catch (Exception e) {
            Toast.makeText(this, "ONCreate exception " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean deleteDir(File dir) {

        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }


    private void makeLoginApiCall(String urlPath) {
        Log.e(TAG, "Api call " + urlPath);

        ApiCall.make(this, urlPath, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.i(TAG, "Api Call: " + response);
                            JSONObject responseObject = new JSONObject(response);
                            // JSONObject responseObject = new JSONObject("");
                            Log.i(TAG, response);
                            String StatusCode = responseObject.getString("statusCode");
                            if (StatusCode.equalsIgnoreCase("200")) {

                                Log.e(TAG, "url response :" + response);
                                String server_url = responseObject.getString("URL");
                                String[] arrSplit = server_url.split("/");
                                String ipAdrress = arrSplit[2];

                                //server_url = "http://43.254.160.190/selfcheck/api/checkout/";
                                server_url = "http://199.healthandglowonline.in/selfcheck_uat/api/checkout/";
                                UrlDB urlDB = new UrlDB(getApplicationContext());
                                urlDB.deleteUrlTable();
                                urlDB.insertServerUrl(server_url, ipAdrress);
                                urlDB.close();

                                Toast toast = Toast.makeText(getApplicationContext(), server_url, Toast.LENGTH_SHORT);
                                toast.show();

                                splashScreen(2000);

                            } else {
                                String Message = responseObject.getString("Message");
                                Log.i(TAG, Message);
                                showAlert(Message);
                                showAlertWithDeviceID(Message,DEV_ID);
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, e.toString());
                            showAlert(e.toString());
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        try {
                            showVolleyError(TAG, error);
                        } catch (Exception e) {
                            Toast.makeText(SplashScreen.this, "Login API Call crashed " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }
        );
    }

    public void splashScreen(final int x) {

        Handler handler = new Handler();
        try {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(myintent);
                    finish();
                }
            }, x);
        } catch (Exception ex) {
            Toast.makeText(this, "Crashed in Splashscreen " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAlert(final String msg) {
        Log.e(TAG, "alert Displayed" + msg);
        SplashScreen.this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreen.this);
                Log.i(TAG, msg);
                builder.setTitle("HnG mPOS");
                builder.setMessage(msg)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.d(TAG, "Application Closed");
                                int pid = android.os.Process.myPid();
                                android.os.Process.killProcess(pid);
                                //on click event
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }
    private void showAlertWithDeviceID(final String msg,final String deviceID) {
        Log.e(TAG, "alert Displayed" + msg);
        SplashScreen.this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreen.this);
                Log.i(TAG, msg);
                builder.setTitle("HnG mPOS");
                builder.setMessage(msg+"\nDeviceId: "+deviceID)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.d(TAG, "Application Closed");
                                int pid = android.os.Process.myPid();
                                android.os.Process.killProcess(pid);
                                //on click event
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

}
