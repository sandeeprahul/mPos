package in.hng.mpos.activity;


import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
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
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.hng.mpos.BuildConfig;
import in.hng.mpos.Database.CardDB;
import in.hng.mpos.Database.CustomerDB;
import in.hng.mpos.Database.LoyaltyCredentialsDB;
import in.hng.mpos.Database.LoyaltyDetailsDB;
import in.hng.mpos.Database.OrderedProductDetailsDB;
import in.hng.mpos.Database.PrinterDB;
import in.hng.mpos.Database.ProductDetailsDB;
import in.hng.mpos.Database.StoreDB;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.Database.WalletDB;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.CardDetails;
import in.hng.mpos.gettersetter.PrinterDetails;
import in.hng.mpos.helper.AlertManager;
import in.hng.mpos.helper.ApiCall;
import in.hng.mpos.helper.ConnectionDetector;
import in.hng.mpos.helper.Log;
public class Login extends BaseActivity implements InputManager.InputDeviceListener {

    private EditText user, pass;
    private ProgressDialog dialog = null;
    private SharedPreferences sp;
    private AlertManager alert = new AlertManager();
    SharedPreferences.Editor editor;
    private String URL, PATH, DEV_ID;
    private static final String TAG = "Login";
    String versionName = BuildConfig.VERSION_NAME;
    String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
    private long enqueue;
    private DownloadManager dm;
    private TextView devID, version;
    private String userID, location, lyltyID, lyltyPWD, tillNo, isPBcoupon;
    private String storeEmail, storePwd, toEmail;

    private InputManager mInputManager;
    private SparseArray<InputDeviceState> mInputDeviceStates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_login);

        mInputManager = (InputManager) getSystemService(Context.INPUT_SERVICE);
        mInputDeviceStates = new SparseArray<InputDeviceState>();

        if (isPackageExisted("in.hng.billingapp")) {

            new AlertDialog.Builder(this)
                    .setMessage("Please click on OK button in the next message box to complete the installation of the app")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            Intent intent = new Intent(Intent.ACTION_DELETE);
                            //Enter app package name that app you wan to install
                            intent.setData(Uri.parse("package:in.hng.billingapp"));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            int pid = android.os.Process.myPid();
                            android.os.Process.killProcess(pid);
                        }
                    }).show();
        }

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        editor.clear();
        editor.apply();

        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH = urlDB.getUrlDetails();
        urlDB.close();

        DEV_ID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
//        DEV_ID = "e3175dc0551e269f";
        String versionName = BuildConfig.VERSION_NAME;
        Log.e(TAG, "Device ID ===> " + DEV_ID);
       // DEV_ID = "f719f7b9d4b1d256";
        Log.e(TAG, "Static Device ID ===> " + DEV_ID);

        setActionBarTitle("h&g mPOS - Login");

        user = findViewById(R.id.username);
        pass = findViewById(R.id.password);
        devID = findViewById(R.id.devid);
        version = findViewById(R.id.version);
        devID.setText("Device ID : " + DEV_ID);
        version.setText("V" + versionCode + " Version : " + versionName);

        File extStore = Environment.getExternalStorageDirectory();
        File myFile = new File(extStore.getAbsolutePath() + "/innoviti/");
        if (!myFile.exists()) {
            Log.i(TAG, "Creating Innovitti Folder");
            myFile.mkdir();
            copyAssets();
        }

        Log.i(TAG, "Application Loaded");
        this.deletetables();

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c
                                .getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c
                                .getInt(columnIndex)) {

                            String uriString = c
                                    .getString(c
                                            .getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));


                            String app = uriString.substring(uriString.indexOf("/download/") + 10);
                            Intent i_intent = new Intent(Intent.ACTION_VIEW);
                            i_intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                            i_intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/Download/" + app)), "application/vnd.android.package-archive");
                            i_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(i_intent);
                            System.exit(0);

                        }

                    }
                }
            }
        };

        registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void Login(View v) {

        View view = Login.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        ConnectionDetector net = new ConnectionDetector(getApplicationContext());
        Log.i(TAG, "checking for Internet connection");
        if (net.isConnectingToInternet()) {

            if (isEmpty(user)) {
                alert.alert(Login.this, "Required Field", "User ID is mandatory");
            } else if (isEmpty(pass)) {
                alert.alert(Login.this, "Required Field", "Password is mandatory");
            } else {

                try {
                    URL = PATH + "userlogin?userid=" + user.getText().toString().trim() + "&pwd=" + URLEncoder.encode(pass.getText().toString().trim(), "utf-8") + "&version=" + versionName + "&devId=" + DEV_ID;
                    Log.w(TAG, "Login Url " + URL);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                makeLoginApiCall(URL);


            }
        } else {
            AlertManager alert = new AlertManager();
            alert.alert(Login.this, "No Internet Connection",
                    "Please check " +
                            "your data connection or Wifi is ON !");

            Log.i(TAG, "No internet");
        }

    }

    private void makeLoginApiCall(String urlPath) {

        dialog = ProgressDialog.show(Login.this, "",
                "Validating user...", true);
        ApiCall.make(this, urlPath, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            Log.i(TAG, response.toString());
                            String StatusCode = responseObject.getString("statusCode");
                            String Message = responseObject.getString("Message");
                            Log.i(TAG, Message);
                            if (StatusCode.equalsIgnoreCase("200")) {

                                dialog.dismiss();

                                userID = responseObject.getString("Userid");
                                location = responseObject.getString("Location");
                                lyltyID = responseObject.getString("loyaltyuserid");
                                lyltyPWD = responseObject.getString("loyaltypassword");
                                tillNo = responseObject.getString("tillNo");
                                isPBcoupon = responseObject.getString("Couponpbflag");
                                storeEmail = responseObject.getString("email");
                                storePwd = responseObject.getString("emailpwd");
                                toEmail = responseObject.getString("toemail");

                                HashMap<String, String> UserDetails = new HashMap<String, String>();
                                UserDetails.put("userID", userID);
                                UserDetails.put("storeID", location);
                                UserDetails.put("loyaltyID", lyltyID);
                                UserDetails.put("loyaltyPWD", lyltyPWD);
                                UserDetails.put("tillNo", tillNo);
                                UserDetails.put("isPBcoupon", isPBcoupon);
                                UserDetails.put("storeEmail", storeEmail);
                                UserDetails.put("storePwd", storePwd);
                                UserDetails.put("toEmail", toEmail);

                                UserDB userDB = new UserDB(getApplicationContext());
                                userDB.open();
                                userDB.deleteUserTable();
                                userDB.createUserDetailsTable();
                                userDB.insertUserDetails(UserDetails);
                                userDB.close();

                                CustomerDB cusDB = new CustomerDB(getApplicationContext());
                                cusDB.open();
                                cusDB.deleteCustomerTable();
                                cusDB.close();

                                LoyaltyDetailsDB offrDB = new LoyaltyDetailsDB(getApplicationContext());
                                offrDB.open();
                                offrDB.deleteLoyaltyTable();
                                offrDB.close();

                                StoreDB storeDB = new StoreDB(getApplicationContext());
                                storeDB.open();
                                storeDB.deleteStoreTable();
                                storeDB.close();

                                OrderedProductDetailsDB orderDB = new OrderedProductDetailsDB(getApplicationContext());
                                orderDB.open();
                                orderDB.deleteProductsTable();
                                orderDB.close();

                                WalletDB walletDB = new WalletDB(getApplicationContext());
                                walletDB.open();
                                walletDB.deleteWalletTable();
                                walletDB.close();

                                new AlertDialog.Builder(Login.this)
                                        .setMessage("Your login store is " + location + "." + "\n" + "Are you sure to continue with this Store?")
                                        .setCancelable(false)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {

                                                ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                                                if (net.isConnectingToInternet()) {

                                                    URL = PATH + "declaration?Location=" + location + "&Cashier=" + userID + "&TillNo=" + tillNo;
                                                    makeDeclarationApiCall(URL);

                                                } else {
                                                    dialog.dismiss();
                                                    AlertManager alert = new AlertManager();
                                                    alert.alert(Login.this, "No Internet Connection",
                                                            "Please check " +
                                                                    "your data connection or Wifi is ON !");

                                                }

                                            }
                                        }).setNegativeButton("No", null)
                                        .show();


                            } else if (StatusCode.equalsIgnoreCase("202")) {

                                //http://healthandglowonline.ind.in/mposapk/hht.apk
                                //http://35.200.223.104/hng/applications/hht.apk

                                Log.i(TAG, "Update Required");
                                dialog.dismiss();
                                Toast.makeText(Login.this, "New update is available", Toast.LENGTH_SHORT).show();
                                dialog = ProgressDialog.show(Login.this, "",
                                        "Downloading Application..", true);
                                Log.i(TAG, "Updating Application");

                                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                                    try {
                                        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                        //DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://35.200.223.104/hng/applications/mpos.apk"));
                                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://199.healthandglowonline.in/hng/applications/mpos.apk"));

                                        request.setMimeType("application/vnd.android.package-archive");
                                        

                                        request.setDestinationInExternalPublicDir("/download/", "mpos.apk");
                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                        enqueue = dm.enqueue(request);
                                    } catch (IllegalArgumentException e) {

                                        dialog.dismiss();
                                        e.printStackTrace();

                                        Toast.makeText(Login.this, "Unable to download.. Please contact the IT support", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "Donwload Manager is Enabled " + downLoadMangerIsEnable(Login.this));

                                    }
                                } else {


                                    dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                    //DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://35.200.223.104/hng/applications/mpos.apk"));
                                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://199.healthandglowonline.in/hng/applications/mpos.apk"));

                                    request.setMimeType("application/vnd.android.package-archive");
                                    request.setDestinationInExternalPublicDir("/download/", "mpos.apk");
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    enqueue = dm.enqueue(request);
                                }

                            } else {
                                dialog.dismiss();
                                Log.i(TAG, Message);
                                showLoginFailedAlert(Message);
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, e.toString());
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        showVolleyError(TAG, error);
                    }
                }
        );
    }

    private void makeDeclarationApiCall(String urlPath) {

        dialog = ProgressDialog.show(Login.this, "",
                "Checking for pending declaration..", true);
        Log.i(TAG, "Checking for pending declaration");

        ApiCall.make(this, urlPath, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseObject = new JSONObject(response);


                            String Status = responseObject.getString("Status");
                            if (Status.equalsIgnoreCase("Success")) {
                                editor = sp.edit();
                                editor.putString("Location", location);
                                editor.putString("Cashier", userID);
                                editor.putString("Cash", responseObject.getString("Cash"));
                                editor.putString("Card", responseObject.getString("Credit Card"));
                                editor.putString("Coupon", responseObject.getString("Coupon"));
                                editor.putString("Loyalty", responseObject.getString("Loyalty"));
                                editor.putString("Wallet", responseObject.getString("Wallet"));
                                editor.putString("TillNo", tillNo);
                                editor.putString("fromMail", responseObject.getString("FromMailID"));
                                editor.putString("mailPwd", responseObject.getString("MailPWD"));
                                editor.putString("toMail", responseObject.getString("ToMailID"));
                                editor.apply();
                                Intent intent = new Intent(Login.this, DeclarationActivity.class);
                                startActivity(intent);
                                finish();
                                dialog.dismiss();
                                Toast.makeText(Login.this, "Declaration is pending", Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "Declaration Required");

                            } else {
                                dialog.dismiss();
                                PrinterDB printerDB = new PrinterDB(getApplicationContext());
                                printerDB.open();
                                ArrayList<PrinterDetails> printerDetails = printerDB.getPrinterDetails();
                                printerDB.close();
                                CardDB cardDB = new CardDB(getApplicationContext());
                                cardDB.open();
                                ArrayList<CardDetails> cardDetails = cardDB.getEDCdetails();
                                cardDB.close();
                                if (printerDetails.size() <= 0) {

                                    AlertDialog.Builder d = new AlertDialog.Builder(Login.this);
                                    d.setTitle("Printer Setting");
                                    // d.setIcon(R.drawable.ic_launcher);
                                    d.setMessage("Do you want to configure Bluetooth Printer?");
                                    d.setPositiveButton("YES",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {

                                                    Intent intent = new Intent(Login.this, PrinterActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });
                                    d.setNegativeButton("No",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    CardDB cardDB = new CardDB(getApplicationContext());
                                                    cardDB.open();
                                                    ArrayList<CardDetails> cardDetails = cardDB.getEDCdetails();
                                                    cardDB.close();
                                                    if (cardDetails.size() <= 0) {
                                                        AlertDialog.Builder d = new AlertDialog.Builder(Login.this);
                                                        d.setTitle("Card Setting");
                                                        // d.setIcon(R.drawable.ic_launcher);
                                                        d.setMessage("EDC details not configured, Please click OK to continue");
                                                        d.setPositiveButton("OK",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int which) {

                                                                        Intent intent = new Intent(Login.this, CardSettingsActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                });
                                                        d.show();
                                                    } else {
                                                        Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                                        Log.d(TAG, "Login Successful");
                                                        Intent intent = new Intent(Login.this, LoyaltyActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                    // do nothing
                                                }
                                            });
                                    d.show();

                                } else if (cardDetails.size() <= 0) {

                                    AlertDialog.Builder d = new AlertDialog.Builder(Login.this);
                                    d.setTitle("Card Setting");
                                    // d.setIcon(R.drawable.ic_launcher);
                                    d.setMessage("EDC details not configured, Please click OK to continue");
                                    d.setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {

                                                    Intent intent = new Intent(Login.this, CardSettingsActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });
                                    d.show();

                                } else {
                                    dialog.dismiss();
                                    Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Login Successful");
                                    Intent intent = new Intent(Login.this, LoyaltyActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, e.toString());
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        showVolleyError(TAG, error);
                    }
                }
        );
    }

    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() <= 0;
    }

    public void showLoginFailedAlert(final String msg) {

        Login.this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                Log.i(TAG, msg);
                builder.setTitle("Login Failed");
                builder.setMessage(msg)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //on click event
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

    }

    @Override
    public void onDestroy() {

        super.onDestroy();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.e(TAG, "ONKEY DOWN BUTTON PRESSED");
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            View view = Login.this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            new AlertDialog.Builder(Login.this)
                    .setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            System.exit(0);
                            Log.i(TAG, "Application Closed");
                        }
                    }).setNegativeButton("No", null)
                    .show();


        }
        return super.onKeyDown(keyCode, event);
    }

    public void deletetables() {

        Log.i(TAG, "Deleting Customer Details");
        CustomerDB cusDB = new CustomerDB(getApplicationContext());
        cusDB.open();
        cusDB.deleteCustomerTable();
        cusDB.close();

        Log.i(TAG, "Deleting Loyalty Details");
        LoyaltyDetailsDB offrDB = new LoyaltyDetailsDB(getApplicationContext());
        offrDB.open();
        offrDB.deleteLoyaltyTable();
        offrDB.close();

        Log.i(TAG, "Deleting Loyalty Credentials Details");
        LoyaltyCredentialsDB lyltyDB = new LoyaltyCredentialsDB(getApplicationContext());
        lyltyDB.open();
        lyltyDB.deleteLoyaltyCredentialsDetailsTable();
        lyltyDB.close();

        Log.i(TAG, "Deleting Product Details");
        ProductDetailsDB prdctDB = new ProductDetailsDB(getApplicationContext());
        prdctDB.open();
        prdctDB.deleteUserTable();
        prdctDB.close();

    }

    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            // Log.e("tag", "Failed to get asset file list.", e);
        }
        for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);

                String outDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/innoviti/";

                File outFile = new File(outDir, filename);

                out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public boolean isPackageExisted(String targetPackage) {

        List<ApplicationInfo> packages;
        PackageManager pm;

        pm = getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(targetPackage))
                return true;
        }
        return false;

    }

    public void ExitApplication(View v) {

        View view = Login.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        new AlertDialog.Builder(Login.this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(TAG, "Application Closed");
                        finish();
                        System.exit(0);
                    }
                }).setNegativeButton("No", null)
                .show();

    }

    @Override
    public void onInputDeviceAdded(int i) {
        Log.e(TAG, "Input device added " + i);
    }

    @Override
    public void onInputDeviceRemoved(int i) {
        Log.e(TAG, "Input device removed " + i);
    }

    @Override
    public void onInputDeviceChanged(int i) {
        Log.e(TAG, "Input device changed " + i);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Register an input device listener to watch when input devices are
        // added, removed or reconfigured.
        mInputManager.registerInputDeviceListener(this, null);

        // Query all input devices.
        // We do this so that we can see them in the log as they are enumerated.
        int[] ids = mInputManager.getInputDeviceIds();
        for (int i = 0; i < ids.length; i++) {
            Log.e(TAG, "Input ID's ==> " + ids[i]);
            getInputDeviceState(ids[i]);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Remove the input device listener when the activity is paused.
        mInputManager.unregisterInputDeviceListener(this);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Update device state for visualization and logging.

        InputDeviceState state = getInputDeviceState(event.getDeviceId());

        if (state != null) {
            switch (event.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    Log.e(TAG, "Action Down " + state);
                    if (state.onKeyDown(event)) {
                        Log.e(TAG, "Action Down " + state);
                    }
                    break;

                case KeyEvent.ACTION_UP:
                    Log.e(TAG, "Action Up " + state);
                    if (state.onKeyUp(event)) {
                        Log.e(TAG, "Action UP " + state);
                    }
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private InputDeviceState getInputDeviceState(int deviceId) {
        InputDeviceState state = mInputDeviceStates.get(deviceId);
        if (state == null) {
            final InputDevice device = mInputManager.getInputDevice(deviceId);
            if (device == null) {
                return null;
            }
            state = new InputDeviceState(device);
            mInputDeviceStates.put(deviceId, state);
        }
        return state;
    }

    private static class InputDeviceState {
        private static final String TAG = InputDeviceState.class.getSimpleName();
        private final InputDevice mDevice;
        private final int[] mAxes;
        private final float[] mAxisValues;
        private final SparseIntArray mKeys;

        public InputDeviceState(InputDevice device) {
            mDevice = device;

            int numAxes = 0;
            final List<InputDevice.MotionRange> ranges = device.getMotionRanges();
            for (InputDevice.MotionRange range : ranges) {
                if (range.isFromSource(InputDevice.SOURCE_CLASS_JOYSTICK)) {
                    numAxes += 1;
                }
            }

            mAxes = new int[numAxes];
            mAxisValues = new float[numAxes];
            int i = 0;
            for (InputDevice.MotionRange range : ranges) {
                if (range.isFromSource(InputDevice.SOURCE_CLASS_JOYSTICK)) {
                    mAxes[i++] = range.getAxis();
                }
            }

            mKeys = new SparseIntArray();
        }

        public InputDevice getDevice() {
            return mDevice;
        }

        public int getAxisCount() {
            return mAxes.length;
        }

        public int getAxis(int axisIndex) {
            return mAxes[axisIndex];
        }

        public float getAxisValue(int axisIndex) {
            return mAxisValues[axisIndex];
        }

        public int getKeyCount() {
            return mKeys.size();
        }

        public int getKeyCode(int keyIndex) {
            return mKeys.keyAt(keyIndex);
        }

        public boolean isKeyPressed(int keyIndex) {
            return mKeys.valueAt(keyIndex) != 0;
        }

        public boolean onKeyDown(KeyEvent event) {
            final int keyCode = event.getKeyCode();
            if (isGameKey(keyCode)) {
                if (event.getRepeatCount() == 0) {
                    final String symbolicName = KeyEvent.keyCodeToString(keyCode);
                    mKeys.put(keyCode, 1);
                    Log.i(TAG, mDevice.getName() + " - Key Down: " + symbolicName);
                }
                return true;
            }
            return false;
        }

        public boolean onKeyUp(KeyEvent event) {
            final int keyCode = event.getKeyCode();
            if (isGameKey(keyCode)) {
                int index = mKeys.indexOfKey(keyCode);
                if (index >= 0) {
                    final String symbolicName = KeyEvent.keyCodeToString(keyCode);
                    mKeys.put(keyCode, 0);
                    Log.i(TAG, mDevice.getName() + " - Key Up: " + symbolicName);
                }
                return true;
            }
            return false;
        }

        public boolean onJoystickMotion(MotionEvent event) {
            StringBuilder message = new StringBuilder();
            message.append(mDevice.getName()).append(" - Joystick Motion:\n");

            final int historySize = event.getHistorySize();
            for (int i = 0; i < mAxes.length; i++) {
                final int axis = mAxes[i];
                final float value = event.getAxisValue(axis);
                mAxisValues[i] = value;
                message.append("  ").append(MotionEvent.axisToString(axis)).append(": ");

                // Append all historical values in the batch.
                for (int historyPos = 0; historyPos < historySize; historyPos++) {
                    message.append(event.getHistoricalAxisValue(axis, historyPos));
                    message.append(", ");
                }

                // Append the current value.
                message.append(value);
                message.append("\n");
            }
            Log.i(TAG, message.toString());
            return true;
        }

        // Check whether this is a key we care about.
        // In a real game, we would probably let the user configure which keys to use
        // instead of hardcoding the keys like this.
        private static boolean isGameKey(int keyCode) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_SPACE:
                    return true;
                default:
                    return KeyEvent.isGamepadButton(keyCode);
            }
        }
    }

    static boolean downLoadMangerIsEnable(Context context) {
        int state = context.getApplicationContext().getPackageManager()
                .getApplicationEnabledSetting("com.android.providers.downloads");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return !(state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                    state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED);
        } else {
            return !(state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                    state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER);
        }
    }
}