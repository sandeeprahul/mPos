package in.hng.mpos.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.R;
import in.hng.mpos.helper.AlertManager;
import in.hng.mpos.helper.AppController;
import in.hng.mpos.helper.ConnectionDetector;
import in.hng.mpos.helper.Log;
import in.hng.mpos.helper.MailSender;

/**
 * Created by Cbly on 17-Mar-18.
 */

public class DeclarationActivity extends BaseActivity {

    private static final String TAG = "Declaration";

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String cash, card, coupon, loyalty, walllet, location, cashier, PATH, TillNo;
    private Float cashAmt, cardAmt, couponAmt, loyaltyAmt, walletAmt, Totalamt;
    private EditText edt_cash, edt_card, edt_coupon, edt_loyalty, edt_wallet;
    private TextView txtcash, txtcc, txtcoupon, txtloyalty, txtwallet;
    private Button Submit, Cancel, OK;
    private PopupWindow popupWindow;
    private LinearLayout linearLayoutCash;
    private ProgressDialog dialog = null;

    private Handler handler = new Handler();
    Runnable myRunnable;

    private String storeEmail, storePwd, toEmail;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_declaration);
        final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(
                R.layout.action_bar_simple, null);

        setActionBarTitle("h&g mPOS - Declaration");

        edt_cash = findViewById(R.id.dec_cash);
        edt_card = findViewById(R.id.dec_cc);
        edt_coupon = findViewById(R.id.dec_coupon);
        edt_loyalty = findViewById(R.id.dec_lylty);
        edt_wallet = findViewById(R.id.dec_wallet);
        Submit = findViewById(R.id.btn_dec_submit);
        Cancel = findViewById(R.id.btn_dec_cancel);
        linearLayoutCash = findViewById(R.id.linearLayoutCash);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        cash = sp.getString("Cash", "");
        card = sp.getString("Card", "");
        coupon = sp.getString("Coupon", "");
        loyalty = sp.getString("Loyalty", "");
        walllet = sp.getString("Wallet", "");
        location = sp.getString("Location", "");
        cashier = sp.getString("Cashier", "");
        TillNo = sp.getString("TillNo", "");
        storeEmail = sp.getString("fromMail", "");
        storePwd = sp.getString("mailPwd", "");
        toEmail = sp.getString("toMail", "");

        edt_cash.setText(cash);
        edt_card.setText(card);
        edt_coupon.setText(coupon);
        edt_loyalty.setText(loyalty);
        edt_wallet.setText(walllet);

        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH = urlDB.getUrlDetails();
        urlDB.close();

        Log.d(TAG, "Declaration screen loaded");

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Submit.setEnabled(false);

                dialog = ProgressDialog.show(DeclarationActivity.this, "",
                        "Submitting declaration details...", true);

                handler = new Handler();
                handler.postDelayed(myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 30000);

                Submit.setEnabled(false);

                cashAmt = Float.parseFloat(edt_cash.getText().toString().equalsIgnoreCase("") ? "0.00" : edt_cash.getText().toString());
                cardAmt = Float.parseFloat(edt_card.getText().toString().equalsIgnoreCase("") ? "0.00" : edt_card.getText().toString());
                couponAmt = Float.parseFloat(edt_coupon.getText().toString().equalsIgnoreCase("") ? "0.00" : edt_coupon.getText().toString());
                loyaltyAmt = Float.parseFloat(edt_loyalty.getText().toString().equalsIgnoreCase("") ? "0.00" : edt_loyalty.getText().toString());
                walletAmt = Float.parseFloat(edt_wallet.getText().toString().equalsIgnoreCase("") ? "0.00" : edt_wallet.getText().toString());

                Totalamt = cashAmt + cardAmt + couponAmt + loyaltyAmt + walletAmt;

                if (Totalamt > 0) {

                    JSONObject finaljsonobj = new JSONObject();

                    JSONArray Headerjsonarray = new JSONArray();
                    JSONArray Detailsjsonarray = new JSONArray();

                    try {
                        JSONObject headerjsonobj = new JSONObject();
                        headerjsonobj.put("Location", location);
                        headerjsonobj.put("Cashier", cashier);
                        headerjsonobj.put("TillNo", TillNo);
                        Headerjsonarray.put(0, headerjsonobj);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Exception 1 <<::>> " + e);
                    }

                    try {
                        JSONObject Detailjsonobj = new JSONObject();
                        Detailjsonobj.put("DeclareType", "Cash");
                        Detailjsonobj.put("DeclareValSystem", edt_cash.getText().toString());
                        Detailsjsonarray.put(0, Detailjsonobj);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Exception 2 <<::>> " + e);
                    }

                    try {
                        JSONObject Detailjsonobj = new JSONObject();
                        Detailjsonobj.put("DeclareType", "Card");
                        Detailjsonobj.put("DeclareValSystem", edt_card.getText().toString());
                        Detailsjsonarray.put(1, Detailjsonobj);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Exception 3 <<::>> " + e);
                    }


                    try {
                        JSONObject Detailjsonobj = new JSONObject();
                        Detailjsonobj.put("DeclareType", "Coupon");
                        Detailjsonobj.put("DeclareValSystem", edt_coupon.getText().toString());
                        Detailsjsonarray.put(2, Detailjsonobj);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Exception 4 <<::>> " + e);
                    }

                    try {
                        JSONObject Detailjsonobj = new JSONObject();
                        Detailjsonobj.put("DeclareType", "Loyalty");
                        Detailjsonobj.put("DeclareValSystem", edt_loyalty.getText().toString());
                        Detailsjsonarray.put(3, Detailjsonobj);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Exception 4 <<::>> " + e);
                    }

                    try {
                        JSONObject Detailjsonobj = new JSONObject();
                        Detailjsonobj.put("DeclareType", "Wallet");
                        Detailjsonobj.put("DeclareValSystem", edt_wallet.getText().toString());
                        Detailsjsonarray.put(4, Detailjsonobj);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Exception 5 <<::>> " + e);
                    }

                    try {
                        finaljsonobj.put("header", Headerjsonarray);
                        finaljsonobj.put("detail", Detailsjsonarray);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "Exception 5 <<::>> " + e);
                    }

                    Log.d(TAG, "Calling Declaration API");
                    RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
                    String URL = PATH + "completedeclaration/";
                    Log.w(TAG, "CashDeclarationUrl == " + URL);
                    final String mRequestBody = finaljsonobj.toString();

                    Log.w(TAG, "Declaration == " + mRequestBody);

                    ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                    if (net.isConnectingToInternet()) {
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                Log.d(TAG, "Declaration Response <<::>> " + response);

                                try {
                                    JSONObject Jsonobj = new JSONObject(response);
                                    String status = Jsonobj.getString("statusCode");

                                    if (status.equalsIgnoreCase("200")) {

                                        dialog.dismiss();
                                        handler.removeCallbacks(myRunnable);
                                        JSONObject jsonObject = new JSONObject(response.toString());
                                        String Status = jsonObject.getString("Status");

                                        if (Status.equalsIgnoreCase("Success")) {

                                            Log.d(TAG, "Data Retrieved from Declaration API");

                                            final String cash, card, coupon, loyalty, wallet;
                                            cash = Jsonobj.getString("Cash");
                                            card = Jsonobj.getString("Credit Card");
                                            coupon = Jsonobj.getString("Coupon");
                                            loyalty = Jsonobj.getString("Loyalty");
                                            wallet = Jsonobj.getString("Wallet");


                                            LayoutInflater layoutInflater = (LayoutInflater) DeclarationActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                            View customView = layoutInflater.inflate(R.layout.cash_declaration_popup, null);
                                            txtcash = customView.findViewById(R.id.txtcashvalue);
                                            txtcc = customView.findViewById(R.id.txtCCvalue);
                                            txtcoupon = customView.findViewById(R.id.txtcouponvalue);
                                            txtloyalty = customView.findViewById(R.id.txtlyltyvalue);
                                            txtwallet = customView.findViewById(R.id.txtwalletvalue);
                                            OK = customView.findViewById(R.id.dec_Ok);
                                            //instantiate popup window
                                            popupWindow = new PopupWindow(customView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);

                                            //display the popup window
                                            popupWindow.showAtLocation(linearLayoutCash, Gravity.CENTER, 0, 0);
                                            popupWindow.setFocusable(true);
                                            popupWindow.update();

                                            txtcash.setText(cash);
                                            txtcc.setText(card);
                                            txtcoupon.setText(coupon);
                                            txtloyalty.setText(loyalty);
                                            txtwallet.setText(wallet);

                                            OK.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Log.d(TAG, "Declaration submitted");
                                                    popupWindow.dismiss();
                                                    editor.clear();
                                                    editor.commit();
                                                    Date c = Calendar.getInstance().getTime();
                                                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                                                    final String formattedDate = df.format(c);
                                                    File logfile = Environment.getExternalStorageDirectory();
                                                    File file = new File(logfile.getAbsolutePath() + "/mPOSlog/");
                                                    File myFile = new File(logfile.getAbsolutePath() + "/mPOSlog/" + formattedDate + ".log");

                                                    if (myFile.exists()) {
                                                        new Thread(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    MailSender sender = new MailSender(storeEmail,
                                                                            storePwd);

                                                                    sender.sendMail("mPoslog - " + location, "PFA mPos Log for the store " + location + ", for the date " + formattedDate,
                                                                            storeEmail, toEmail);
                                                                    openLoginPage();
                                                                } catch (Exception e) {
                                                                    android.util.Log.e("SendMail", e.getMessage(), e);
                                                                    openLoginPage();
                                                                }
                                                            }

                                                        }).start();
                                                        Toast.makeText(DeclarationActivity.this, "User Logged Out Successfully",
                                                                Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        openLoginPage();
                                                        Toast.makeText(DeclarationActivity.this, "User Logged Out Successfully",
                                                                Toast.LENGTH_SHORT).show();
                                                    }

                                                }

                                            });

                                    /*
                                    Intent intent = new Intent(DeclarationActivity.this, Login.class);
                                    startActivity(intent);
                                    finish();
                                    */
                                        }
                                    } else {
                                        dialog.dismiss();

                                        handler.removeCallbacks(myRunnable);
                                        Submit.setEnabled(true);
                                        String Msg = Jsonobj.getString("Message");
                                        showAlertMessage(TAG, Msg);
                                        Log.e(TAG, Msg);
                                    }

                                } catch (Exception e) {
                                    dialog.dismiss();
                                    handler.removeCallbacks(myRunnable);
                                    Submit.setEnabled(true);
                                    e.printStackTrace();
                                    Log.e(TAG, "Exception <<::>> " + e);
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                dialog.dismiss();
                                handler.removeCallbacks(myRunnable);
                                Submit.setEnabled(true);
                                showVolleyError(TAG, error);
                            }
                        }) {
                            @Override
                            public String getBodyContentType() {
                                return "application/json; charset=utf-8";
                            }

                            @Override
                            public byte[] getBody() throws AuthFailureError {
                                try {
                                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                                } catch (UnsupportedEncodingException uee) {
                                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                                    return null;
                                }
                            }
                        };
                        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                6000,
                                3,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                        requestQueue.add(stringRequest);

                    } else {
                        Log.e(TAG, "No Internet");
                        AlertManager alert = new AlertManager();
                        alert.alert(DeclarationActivity.this, "No Internet Connection",
                                "Please check " +
                                        "your data connection or Wifi is ON !");

                    }
                } else {
                    dialog.dismiss();
                    handler.removeCallbacks(myRunnable);
                    Submit.setEnabled(true);
                    showAlertMessage(TAG, "Total declaration value should be greater than zero");
                }
            }

        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(DeclarationActivity.this)
                        .setMessage("Are you sure you want to Cancel Declaration?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.i(TAG, "Declaration Cancelled");
                                Intent i = new Intent(DeclarationActivity.this, LoyaltyActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                                finish();

                            }

                        }).setNegativeButton("No", null)
                        .show();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            new AlertDialog.Builder(DeclarationActivity.this)
                    .setMessage("Are you sure you want to Cancel Declaration?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            Intent i = new Intent(DeclarationActivity.this, LoyaltyActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            finish();

                        }

                    }).setNegativeButton("No", null)
                    .show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        try {
            handler.removeCallbacks(myRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void openLoginPage() {
        Intent i = new Intent(DeclarationActivity.this, Login.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}

