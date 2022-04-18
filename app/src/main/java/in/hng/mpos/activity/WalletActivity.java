package in.hng.mpos.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.common.hash.Hashing;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import in.hng.mpos.Adapter.WalletListAdapter;
import in.hng.mpos.Database.CustomerDB;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.Database.WalletDB;
import in.hng.mpos.R;
import in.hng.mpos.Utils.Constants;
import in.hng.mpos.gettersetter.CustomerDetails;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.gettersetter.WalletDetails;
import in.hng.mpos.helper.AppController;
import in.hng.mpos.helper.Log;
import in.hng.mpos.helper.QRCodeHelper;


public class WalletActivity extends AppCompatActivity {

    private TextView txtCardNo, txtZagBal, txtZagAmt, txtBalBillAmt, txtZagOTP;
    private Button btnZagRedeem;
    private String ZagBal;
    private Float BalBillAmount, BalZagAmount, ZagAmount;
    ListView listView;
    ArrayList<WalletDetails> walletList;
    LinearLayout ZagRedeem, llPhonePeProcess;
    EditText etPhonepeWalletAmt;
    private CheckBox ZagOTPcheck;
    private Boolean isZagOTP = false;
    ProgressDialog idialog = null;
    private Handler handler;
    Runnable myRunnable = null;
    WalletListAdapter adapter;
    EditText PPamt, MKamt, MKotp, PTamt, PTotp;
    String Sha256data, Sha256data_check;
    String encodebase64;
    String transID;
    LinearLayout container, ZagLyt, ParentLyt, PPlyt, MKlyt, PtmLyt;
    String ZagKey, ZagRefNo = "";
    String PPsaltKey, PPsaltIndex, PPmerchantID, PPinsType, PPexpiresIn, PPstoreID, PPtermID;
    String PhonePeUPIsaltKey, PhonePeUPIsaltIndex, PhonePeUPImerchantID, PhonePeUPIexpiresIn, PhonePeUPIstoreID,
            PhonePeUPIterminalID;
    String MKkey, MKsmid, MKgenOTP;
    Boolean isPPinitiated = false, isUsedWallet = false;
    Button PTredeem, PPredeem, PPstatus, MKredeem;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    String Billno, CashierID, Location, tillNo, mobileNo, PATH, zagPath, ppPath, mkPath;
    String TAG = "Wallet Activity";
    String BalanceAmount;
    String fromActivity = "";

    String IPAddress = "http://200.healthandglow.in";
    private Handler PPhandler;
    Runnable PPrunnable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(
                R.layout.action_bar_simple, null);
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarLayout);
        actionBar.setTitle("");
        final int actionBarColor = getResources().getColor(R.color.ActionBarColor);
        actionBar.setBackgroundDrawable(new ColorDrawable(actionBarColor));
        TextView title = findViewById(R.id.ab_activity_title);
        title.setText("h&g mPOS - Wallet");
        title.setVisibility(View.VISIBLE);

        txtCardNo = findViewById(R.id.txtcardno);
        txtZagBal = findViewById(R.id.txtZaggleBal);
        txtZagAmt = findViewById(R.id.txtZaggleAmt);
        txtBalBillAmt = findViewById(R.id.txtBal);
        ZagRedeem = findViewById(R.id.LytZagRedeem);
        txtZagOTP = findViewById(R.id.txtZaggleOTP);
        btnZagRedeem = findViewById(R.id.RedeemZaggle);
        ZagOTPcheck = findViewById(R.id.chckZagOTP);
        listView = findViewById(R.id.listv);
        PPamt = findViewById(R.id.txtPPAmt);
        ZagLyt = findViewById(R.id.ZaggleLyt);
        ParentLyt = findViewById(R.id.linearLayoutPymnt);
        MKamt = findViewById(R.id.txtMKAmt);
        MKotp = findViewById(R.id.txtMKOtp);
        PPlyt = findViewById(R.id.LytPPredeem);
        MKlyt = findViewById(R.id.LytMKredeem);
        PtmLyt = findViewById(R.id.LytPayTm);
        PTamt = findViewById(R.id.txtPayTmAmt);
        PTotp = findViewById(R.id.txtPayTmOTP);
        PTredeem = findViewById(R.id.RedeemPayTm);
        PPredeem = findViewById(R.id.RedeemPP);
        PPstatus = findViewById(R.id.btnPPstatus);
        MKredeem = findViewById(R.id.RedeemMK);
        llPhonePeProcess = findViewById(R.id.ll_phonepe_upi);
        etPhonepeWalletAmt = findViewById(R.id.et_phonepe_wallet_amount);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        Billno = sp.getString("Bill_no", "");
        BalanceAmount = sp.getString("Balamt", "");
        txtBalBillAmt.setText(BalanceAmount);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Intent intent = getIntent();
            if (intent.hasExtra(Constants.FROM))
                fromActivity = bundle.getString(Constants.FROM);

            Log.w(TAG, "From Activity ######### "+fromActivity);
        }

        Log.i(TAG, "wallet screen loaded");
        WalletDB walletDB = new WalletDB(getApplicationContext());
        walletDB.open();
        ArrayList<WalletDetails> wallets = walletDB.getWalletDetails();
        if (wallets.size() > 0) {
            adapter = new WalletListAdapter(WalletActivity.this, wallets);
            listView.setAdapter(adapter);
        }
        walletDB.close();

        Resources res = getResources();
        //PATH = res.getString(R.string.API_URL);

        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH = urlDB.getUrlDetails();
        urlDB.close();
        zagPath = res.getString(R.string.ZagURL);
        ppPath = res.getString(R.string.ppURL);
        mkPath = res.getString(R.string.mkURL);

        UserDB userDB = new UserDB(getApplicationContext());
        userDB.open();
        ArrayList<UserDetails> userDetailses = userDB.getUserDetails();
        userDB.close();

        if (userDetailses.size() > 0) {
            CashierID = userDetailses.get(0).getUserID();
            Location = userDetailses.get(0).getStoreID();
            tillNo = userDetailses.get(0).getTillNo();
        }

        CustomerDB cusdb = new CustomerDB(getApplicationContext());
        cusdb.open();
        ArrayList<CustomerDetails> customerDetails = cusdb.getCustomerDetails();
        cusdb.close();

        if (customerDetails.size() > 0) {
            mobileNo = customerDetails.get(0).getMobileNO();
        }

        BalBillAmount = Float.parseFloat(txtBalBillAmt.getText().toString());

        makeGetWalletMasterAPIcall();

        txtZagAmt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (!hasFocus) {
                    if (!txtZagAmt.getText().toString().trim().equalsIgnoreCase("")) {
                        ZagAmount = Float.parseFloat(txtZagAmt.getText().toString());

                    }

                }
            }


        });


        ZagOTPcheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    if (!txtZagAmt.getText().toString().equalsIgnoreCase(""))
                        makeGenerateOTPforZagAPIcall();
                    else {
                        showFailedAlert("please enter amount for redemption");
                        ZagOTPcheck.setChecked(false);
                    }
                } else {
                    isZagOTP = false;
                }

            }
        });
    }


    //wallet details
    public void makeGetWalletMasterAPIcall() {

        Log.i(TAG, "Fetching wallet details API");

        idialog = ProgressDialog.show(WalletActivity.this, "", "loading Wallet details...", true);
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                idialog.dismiss();
            }
        }, 30000);
        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
        String URL = PATH + "walletdetails?Location=" + Location;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {

            @Override
            public void onResponse(final String response) {

                try {
                    Log.i(TAG, "Wallet details response: " + response);
                    JSONObject Jsonobj = new JSONObject(response);
                    String status = Jsonobj.getString("statusCode");

                    if (status.equalsIgnoreCase("200")) {

                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        JSONArray ZagData = Jsonobj.getJSONArray("Zaggle");
                        if (ZagData.length()>=1){
                        JSONObject json_zag = ZagData.getJSONObject(0);


                                ZagKey = json_zag.optString("KEY");
                        }



                        //PhonePe


                        JSONArray PPdata = Jsonobj.getJSONArray("Phonepe");
                        if (PPdata.length()>=1){
                        JSONObject json_PP = PPdata.getJSONObject(0);



                                PPsaltKey = json_PP.optString("saltkey");
                                PPsaltIndex = json_PP.optString("saltindex");
                                PPmerchantID = json_PP.optString("merchantId");
                                PPinsType = json_PP.optString("instrumentType");
                                PPexpiresIn = json_PP.optString("expiresIn");
                                PPstoreID = json_PP.optString("storeId");
                                PPtermID = json_PP.optString("terminalId");



                        }




                        //PhonePe UPI
                        JSONArray PhonePeUPIArray = Jsonobj.getJSONArray("Phonepeupi");
                        if (PhonePeUPIArray.length()>=1){
                            JSONObject PhonePeUPIJsonObj = PhonePeUPIArray.getJSONObject(0);
                                PhonePeUPIsaltKey = PhonePeUPIJsonObj.optString("saltkey");
                                PhonePeUPIsaltIndex = PhonePeUPIJsonObj.optString("saltindex");
                                PhonePeUPImerchantID = PhonePeUPIJsonObj.optString("merchantId");
                                PhonePeUPIexpiresIn = PhonePeUPIJsonObj.optString("expiresIn");
                                PhonePeUPIstoreID = PhonePeUPIJsonObj.optString("storeId");
                                PhonePeUPIterminalID = PhonePeUPIJsonObj.optString("terminalId");


                        }




                        //MobiKwik
                        JSONArray MKdata = Jsonobj.getJSONArray("Mobikwik");
                        JSONObject json_MK = MKdata.getJSONObject(0);
                        if (MKdata.length()>=0){

                                MKkey = json_MK.optString("key");
                                MKsmid = json_MK.optString("smid");
                                MKgenOTP = json_MK.optString("genrateotp");


                        }




                    } else {

                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        WalletActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this);
                                builder.setTitle("h&g mPOS");
                                builder.setMessage("Wallet details not available in database. Please contact IT for support")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {

                                                View view = WalletActivity.this.getCurrentFocus();
                                                if (view != null) {
                                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                    if (imm != null) {
                                                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                                    }
                                                }

                                                editor.putString("isWallet", "Y");
                                                editor.commit();
                                                Intent i = new Intent(WalletActivity.this, PaymentActivity.class);
                                                i.putExtra(Constants.FROM, fromActivity);
                                                startActivity(i);
                                                finish();

                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        });
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Log.i("LOG_VOLLEY", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    try {
                        idialog.dismiss();
                        handler.removeCallbacks(myRunnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (error instanceof TimeoutError) {
                        showFailedAlert("Time out error occurred.");
                        Log.e(TAG, "Time out error occurred.");
                        //Time out error

                    } else if (error instanceof NoConnectionError) {
                        showFailedAlert("Network error occurred.");
                        Log.e(TAG, "Network error occurred.");
                        //net work error

                    } else if (error instanceof AuthFailureError) {
                        showFailedAlert("Authentication error occurred.");
                        Log.e(TAG, "Authentication error occurred.");
                        //error

                    } else if (error instanceof ServerError) {
                        showFailedAlert("Server error occurred.");
                        Log.e(TAG, "Server error occurred.");
                        //Error
                    } else if (error instanceof NetworkError) {
                        showFailedAlert("Network error occurred.");
                        Log.e(TAG, "Network error occurred.");
                        //Error

                    } else if (error instanceof ParseError) {
                        //Error
                        showFailedAlert("An error occurred.");
                        Log.e(TAG, "An error occurred.");
                    } else {

                        showFailedAlert("An error occurred.");
                        Log.e(TAG, "An error occurred.");
                        //Error
                    }

                } catch (Exception e) {


                }
            }
        }) {

        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(stringRequest);
    }

    //Paytm wallet
    public void RedeemPTmamount(View v) {

        Log.i(TAG, "Redeeming Paytm amount");

        final JSONArray redeemarray = new JSONArray();
        if (!PTamt.getText().toString().isEmpty() & !PTotp.getText().toString().isEmpty()) {
            if (Float.valueOf(PTamt.getText().toString()) <= Float.valueOf(BalanceAmount)) {
                PTredeem.setEnabled(false);
                idialog = ProgressDialog.show(WalletActivity.this, "", "Processing Payment...", true);
                handler = new Handler();
                handler.postDelayed(myRunnable = new Runnable() {
                    @Override
                    public void run() {

                        idialog.dismiss();
                        PTredeem.setEnabled(true);
                    }
                }, 30000);


                try {


                    JSONObject Headerobj = new JSONObject();
                    Headerobj.put("mobileno", mobileNo);
                    Headerobj.put("otp", PTotp.getText().toString());
                    Headerobj.put("billamount", PTamt.getText().toString());
                    Headerobj.put("merchantOrderId", Billno);
                    Headerobj.put("posid", Location + tillNo);
                    Headerobj.put("comment", "test");

                    redeemarray.put(0, Headerobj);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
                String URL = PATH + "Paytmpayment";
                final String mRequestBody = redeemarray.toString().substring(1, redeemarray.toString().length() - 1);
                Log.e("paytmCode",mRequestBody);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {

                        try {
                            Log.i(TAG, "Paytm response: " + response);
                            JSONObject Jsonobj = new JSONObject(response);
                            String status = Jsonobj.getString("statusCode");
                            if (status.equalsIgnoreCase("SUCCESS")) {

                                String responseArray = Jsonobj.getString("response");
                                JSONObject responseJsonobj = new JSONObject(responseArray);

                                HashMap<String, String> walletDetails = new HashMap<String, String>();
                                walletDetails.put("walletID", "1");
                                walletDetails.put("walletName", "Paytm");
                                walletDetails.put("walletAmount", responseJsonobj.getString("txnAmount"));
                                walletDetails.put("walletOTP", PTotp.getText().toString());
                                walletDetails.put("walletTransID", responseJsonobj.getString("walletSystemTxnId"));


                                WalletDB walletDB = new WalletDB(getApplicationContext());
                                walletDB.open();
                                walletDB.insertWalletDetails(walletDetails);

                                ArrayList<WalletDetails> wallets = walletDB.getWalletDetails();

                                if (wallets.size() > 0) {

                                    adapter = new WalletListAdapter(WalletActivity.this, wallets);
                                    listView.setAdapter(adapter);

                                }


                                walletDB.close();

                                setBalanceAmount();
                                LoadPaytmLayout(PtmLyt);
                                try {
                                    idialog.dismiss();
                                    handler.removeCallbacks(myRunnable);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                showFailedAlert("Amount redeemed successfully");
                            }

                            else if (status.equalsIgnoreCase("403")){
                                showFailedAlert(Jsonobj.getString("statusMessage"));
                                idialog.dismiss();
                            }


                            else {
                                String msg = Jsonobj.getString("Message");
                                showFailedAlert(msg);
                                Log.i(TAG, msg);
                                PTredeem.setEnabled(true);
                                try {
                                    idialog.dismiss();
                                    handler.removeCallbacks(myRunnable);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }


                        } catch (Exception e) {

                            PTredeem.setEnabled(true);
                            try {
                                idialog.dismiss();
                                handler.removeCallbacks(myRunnable);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            Log.e(TAG, "Json response error occured");
                            showFailedAlert("Json response error occured");
                            e.printStackTrace();
                        }
                        // Log.i("LOG_VOLLEY", response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {

                            PTredeem.setEnabled(true);
                            try {
                                idialog.dismiss();
                                handler.removeCallbacks(myRunnable);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (error instanceof TimeoutError) {
                                showFailedAlert("Time out error occurred.Please click on OK and try again");
                                Log.e(TAG, "Time out error occurred.");
                                //Time out error

                            } else if (error instanceof NoConnectionError) {
                                showFailedAlert("Network error occurred.Please click on OK and try again");
                                Log.e(TAG, "Network error occurred.");
                                //net work error

                            } else if (error instanceof AuthFailureError) {
                                showFailedAlert("Authentication error occurred.Please click on OK and try again");
                                Log.e(TAG, "Authentication error occurred.");
                                //error

                            } else if (error instanceof ServerError) {
                                showFailedAlert("Server error occurred.Please click on OK and try again");
                                Log.e(TAG, "Server error occurred.");
                                //Error
                            } else if (error instanceof NetworkError) {
                                showFailedAlert("Network error occurred.Please click on OK and try again");
                                Log.e(TAG, "Network error occurred.");
                                //Error

                            } else if (error instanceof ParseError) {
                                //Error
                                showFailedAlert("An error occurred.Please click on OK and try again");
                                Log.e(TAG, "An error occurred.");
                            } else {

                                showFailedAlert("An error occurred.Please click on OK and try again");
                                Log.e(TAG, "An error occurred.");
                                //Error
                            }
                            //End


                        } catch (Exception e) {


                        }
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
                showFailedAlert("wallet redemption amount should not be greater than balance bill amount");
            }
        } else
            showFailedAlert("Please enter amount or OTP");
    }


    //Zaggle wallet

    public void makeGenerateOTPforZagAPIcall() {
        Log.i(TAG, "calling GenerateOTPforZag API");
        idialog = ProgressDialog.show(WalletActivity.this, "", "sending OTP...", true);
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                idialog.dismiss();
            }
        }, 30000);
        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
        String URL = zagPath + "sendotpforredemption?cardnumber=" + txtCardNo.getText().toString() + "&amount=" + txtZagAmt.getText().toString();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {

            @Override
            public void onResponse(final String response) {

                try {
                    Log.i(TAG, "Response: " + response);
                    JSONObject Jsonobj = new JSONObject(response);
                    String status = Jsonobj.getString("status");

                    if (status.equalsIgnoreCase("1")) {
                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ZagRefNo = Jsonobj.getString("referencenumber");
                        isZagOTP = true;
                    } else {

                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        isZagOTP = false;
                        ZagOTPcheck.setChecked(false);
                        String Msg = Jsonobj.getString("message");
                        showFailedAlert(Msg);
                    }


                } catch (Exception e) {
                    try {
                        idialog.dismiss();
                        handler.removeCallbacks(myRunnable);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    ZagOTPcheck.setChecked(false);
                    showFailedAlert("OTP error occured");

                    e.printStackTrace();
                }
                // Log.i("LOG_VOLLEY", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    try {
                        idialog.dismiss();
                        handler.removeCallbacks(myRunnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ZagOTPcheck.setChecked(false);
                    if (error instanceof TimeoutError) {
                        showFailedAlert("Time out error occurred.");
                        //Log.e(TAG, "Time out error occurred.");
                        //Time out error

                    } else if (error instanceof NoConnectionError) {
                        showFailedAlert("Network error occurred.");
                        //Log.e(TAG, "Network error occurred.");
                        //net work error

                    } else if (error instanceof AuthFailureError) {
                        showFailedAlert("Authentication error occurred.");
                        //Log.e(TAG, "Authentication error occurred.");
                        //error

                    } else if (error instanceof ServerError) {
                        showFailedAlert("Server error occurred.");
                        //Log.e(TAG, "Server error occurred.");
                        //Error
                    } else if (error instanceof NetworkError) {
                        showFailedAlert("Network error occurred.");
                        //Log.e(TAG, "Network error occurred.");
                        //Error

                    } else if (error instanceof ParseError) {
                        //Error
                        showFailedAlert("An error occurred.");
                        //Log.e(TAG, "An error occurred.");
                    } else {

                        showFailedAlert("An error occurred.");
                        //Log.e(TAG, "An error occurred.");
                        //Error
                    }

                } catch (Exception e) {


                }
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("APPKEY", ZagKey);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(stringRequest);
    }

    public void CheckZaggleBalance(View v) {

        Log.i(TAG, "Getting Zag Brand OTP");

        if (txtCardNo.getText().length() >= 16 && txtZagBal.getText().toString().equalsIgnoreCase("")) {

            idialog = ProgressDialog.show(WalletActivity.this, "", "Authenticating...", true);
            handler = new Handler();
            handler.postDelayed(myRunnable = new Runnable() {
                @Override
                public void run() {

                    idialog.dismiss();
                }
            }, 30000);

            RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
            String URL = zagPath + "brandotppinsettings";
            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {

                @Override
                public void onResponse(final String response) {

                    try {

                        JSONObject Jsonobj = new JSONObject(response);
                        Log.i(TAG, "Zag brand OTP response: " + response);
                        String status = Jsonobj.getString("status");

                        if (status.equalsIgnoreCase("1")) {
                            try {
                                idialog.dismiss();
                                handler.removeCallbacks(myRunnable);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            makeZaggleCheckBalAPIcall();
                        } else {
                            try {
                                idialog.dismiss();
                                handler.removeCallbacks(myRunnable);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String Msg = Jsonobj.getString("message");
                            Log.e(TAG, Msg);
                            showFailedAlert(Msg);
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // Log.i("LOG_VOLLEY", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (error instanceof TimeoutError) {
                            showFailedAlert("Time out error occurred.");
                            Log.e(TAG, "Time out error occurred.");
                            //Time out error

                        } else if (error instanceof NoConnectionError) {
                            showFailedAlert("Network error occurred.");
                            Log.e(TAG, "Network error occurred.");
                            //net work error

                        } else if (error instanceof AuthFailureError) {
                            showFailedAlert("Authentication error occurred.");
                            Log.e(TAG, "Authentication error occurred.");
                            //error

                        } else if (error instanceof ServerError) {
                            showFailedAlert("Server error occurred.");
                            Log.e(TAG, "Server error occurred.");
                            //Error
                        } else if (error instanceof NetworkError) {
                            showFailedAlert("Network error occurred.");
                            Log.e(TAG, "Network error occurred.");
                            //Error

                        } else if (error instanceof ParseError) {
                            //Error
                            showFailedAlert("An error occurred.");
                            Log.e(TAG, "An error occurred.");
                        } else {

                            showFailedAlert("An error occurred.");
                            Log.e(TAG, "An error occurred.");
                            //Error
                        }


                    } catch (Exception e) {


                    }
                }
            }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    params.put("APPKEY", ZagKey);
                    return params;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    6000,
                    3,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(stringRequest);
        } else {
            if (txtCardNo.getText().length() < 16)
                showFailedAlert("Enter valid card number");
        }
    }

    public void makeZaggleCheckBalAPIcall() {

        Log.i(TAG, "Checking Zag Balance");
        idialog = ProgressDialog.show(WalletActivity.this, "", "Checking Balance...", true);
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                idialog.dismiss();
            }
        }, 30000);

        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
        String URL = zagPath + "fetchcarddetailsbycardnumber?cardnumber=" + txtCardNo.getText().toString();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {

            @Override
            public void onResponse(final String response) {

                try {
                    Log.i(TAG, "Zag Bal API response: " + response);
                    JSONObject Jsonobj = new JSONObject(response);
                    String status = Jsonobj.getString("status");

                    if (status.equalsIgnoreCase("1")) {
                        String Bal = Jsonobj.getString("cardbalance");
                        txtZagBal.setText(Bal);
                        txtZagAmt.setEnabled(true);
                        txtZagOTP.setEnabled(true);
                        ZagOTPcheck.setEnabled(true);
                        btnZagRedeem.setEnabled(true);

                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String Msg = Jsonobj.getString("message");
                        showFailedAlert(Msg);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Log.i("LOG_VOLLEY", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    try {
                        idialog.dismiss();
                        handler.removeCallbacks(myRunnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (error instanceof TimeoutError) {
                        showFailedAlert("Time out error occurred.");
                        Log.e(TAG, "Time out error occurred.");
                        //Time out error

                    } else if (error instanceof NoConnectionError) {
                        showFailedAlert("Network error occurred.");
                        Log.e(TAG, "Network error occurred.");
                        //net work error

                    } else if (error instanceof AuthFailureError) {
                        showFailedAlert("Authentication error occurred.");
                        Log.e(TAG, "Authentication error occurred.");
                        //error

                    } else if (error instanceof ServerError) {
                        showFailedAlert("Server error occurred.");
                        Log.e(TAG, "Server error occurred.");
                        //Error
                    } else if (error instanceof NetworkError) {
                        showFailedAlert("Network error occurred.");
                        Log.e(TAG, "Network error occurred.");
                        //Error

                    } else if (error instanceof ParseError) {
                        //Error
                        showFailedAlert("An error occurred.");
                        Log.e(TAG, "An error occurred.");
                    } else {

                        showFailedAlert("An error occurred.");
                        Log.e(TAG, "An error occurred.");
                        //Error
                    }


                } catch (Exception e) {


                }
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("APPKEY", ZagKey);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(stringRequest);


    }

    public void RedeemZagAmount(View v) {
        Log.i(TAG, "Redeeming Zag Amount");
        String AuthType;

        if (!txtZagOTP.getText().toString().isEmpty() && !txtZagAmt.getText().toString().isEmpty()) {
            if (Float.valueOf(txtZagAmt.getText().toString()) <= Float.valueOf(BalanceAmount)) {
                if (!isZagOTP) {
                    AuthType = "1";
                    GetreferenceForPIN();
                } else
                    RedeemZagWithRefNo("2");
            } else {
                showFailedAlert("wallet redemption amount should not be greater than balance bill amount");
            }
        } else {
            showFailedAlert("Either Amount or OTP/pin is empty");
        }


    }

    public void GetreferenceForPIN() {

        Log.i(TAG, "calling GetreferenceForPIN API");
        idialog = ProgressDialog.show(WalletActivity.this, "", "generating referance number", true);
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                idialog.dismiss();
            }
        }, 30000);


        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
        String URL = zagPath + "gettransactionreference?cardnumber=" + txtCardNo.getText().toString() + "&amount=" + txtZagAmt.getText().toString();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {

            @Override
            public void onResponse(final String response) {

                try {
                    Log.i(TAG, "Response: " + response);
                    JSONObject Jsonobj = new JSONObject(response);
                    String status = Jsonobj.getString("status");

                    if (status.equalsIgnoreCase("1")) {
                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ZagRefNo = Jsonobj.getString("referencenumber");
                        RedeemZagWithRefNo("1");

                    } else {

                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        String Msg = Jsonobj.getString("message");
                        showFailedAlert(Msg);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Log.i("LOG_VOLLEY", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    try {
                        idialog.dismiss();
                        handler.removeCallbacks(myRunnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (error instanceof TimeoutError) {
                        showFailedAlert("Time out error occurred.");
                        //Log.e(TAG, "Time out error occurred.");
                        //Time out error

                    } else if (error instanceof NoConnectionError) {
                        showFailedAlert("Network error occurred.");
                        //Log.e(TAG, "Network error occurred.");
                        //net work error

                    } else if (error instanceof AuthFailureError) {
                        showFailedAlert("Authentication error occurred.");
                        //Log.e(TAG, "Authentication error occurred.");
                        //error

                    } else if (error instanceof ServerError) {
                        showFailedAlert("Server error occurred.");
                        //Log.e(TAG, "Server error occurred.");
                        //Error
                    } else if (error instanceof NetworkError) {
                        showFailedAlert("Network error occurred.");
                        //Log.e(TAG, "Network error occurred.");
                        //Error

                    } else if (error instanceof ParseError) {
                        //Error
                        showFailedAlert("An error occurred.");
                        //Log.e(TAG, "An error occurred.");
                    } else {

                        showFailedAlert("An error occurred.");
                        //Log.e(TAG, "An error occurred.");
                        //Error
                    }

                } catch (Exception e) {


                }
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("APPKEY", ZagKey);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);

    }

    public void RedeemZagWithRefNo(String authType) {

        idialog = ProgressDialog.show(WalletActivity.this, "", "Processing amount..", true);
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                idialog.dismiss();
            }
        }, 30000);

        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
        String URL = zagPath + "redeemcard_v4?authentication_type=" + authType + "&otporpin=" + txtZagOTP.getText().toString() + "&referencenumber=" + ZagRefNo + "&billno=" + Billno;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {

            @Override
            public void onResponse(final String response) {

                try {
                    Log.i(TAG, "Zag redeem response: " + response);
                    JSONObject Jsonobj = new JSONObject(response);
                    String status = Jsonobj.getString("status");

                    if (status.equalsIgnoreCase("1")) {

                        HashMap<String, String> walletDetails = new HashMap<String, String>();
                        walletDetails.put("walletID", "3");
                        walletDetails.put("walletName", "Zaggle");
                        walletDetails.put("walletAmount", txtZagAmt.getText().toString());
                        walletDetails.put("walletOTP", txtCardNo.getText().toString());
                        walletDetails.put("walletTransID", Jsonobj.getString("transactionid"));


                        WalletDB walletDB = new WalletDB(getApplicationContext());
                        walletDB.open();
                        walletDB.insertWalletDetails(walletDetails);

                        ArrayList<WalletDetails> wallets = walletDB.getWalletDetails();

                        if (wallets.size() > 0) {


                            adapter = new WalletListAdapter(WalletActivity.this, wallets);
                            listView.setAdapter(adapter);

                        }


                        walletDB.close();
                        setBalanceAmount();
                        LoadZagLayout(ZagLyt);
                        idialog.dismiss();
                        handler.removeCallbacks(myRunnable);

                        String Msg = Jsonobj.getString("message");
                        showFailedAlert(Msg);

                    } else {
                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String Msg = Jsonobj.getString("message");
                        showFailedAlert(Msg);

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Log.i("LOG_VOLLEY", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    resetZagMenu();
                    try {
                        idialog.dismiss();
                        handler.removeCallbacks(myRunnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (error instanceof TimeoutError) {
                        showFailedAlert("Time out error occurred.");
                        Log.e(TAG, "Time out error occurred.");
                        //Time out error

                    } else if (error instanceof NoConnectionError) {
                        showFailedAlert("Network error occurred.");
                        Log.e(TAG, "Network error occurred.");
                        //net work error

                    } else if (error instanceof AuthFailureError) {
                        showFailedAlert("Authentication error occurred.");
                        Log.e(TAG, "Authentication error occurred.");
                        //error

                    } else if (error instanceof ServerError) {
                        showFailedAlert("Server error occurred.");
                        Log.e(TAG, "Server error occurred.");
                        //Error
                    } else if (error instanceof NetworkError) {
                        showFailedAlert("Network error occurred.");
                        Log.e(TAG, "Network error occurred.");
                        //Error

                    } else if (error instanceof ParseError) {
                        //Error
                        showFailedAlert("An error occurred.");
                        Log.e(TAG, "An error occurred.");
                    } else {

                        showFailedAlert("An error occurred.");
                        Log.e(TAG, "An error occurred.");
                        //Error
                    }
                } catch (Exception e) {


                }
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("APPKEY", ZagKey);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(stringRequest);
    }

    public void ResetZagMenu(View v) {
        resetZagMenu();
    }

    public void resetZagMenu() {


        txtCardNo.setText("");
        txtZagBal.setText("");
        txtZagAmt.setText("");
        txtZagOTP.setText("");
        txtCardNo.setEnabled(true);
        txtZagAmt.setEnabled(false);
        txtZagOTP.setEnabled(false);
        btnZagRedeem.setEnabled(false);
        ZagOTPcheck.setChecked(false);
        ZagOTPcheck.setEnabled(false);


    }

    //PhonePe wallet

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void RedeemPPamount(View v) {

        Log.i(TAG, "Redeeming Phonepe amount");
        View view = WalletActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        final JSONArray redeemarray = new JSONArray();
        if (!PPamt.getText().toString().isEmpty()) {
            if (PPmerchantID != null && PPinsType != null && PPexpiresIn != null && PPtermID != null) {
                if (Float.valueOf(PPamt.getText().toString()) <= Float.valueOf(BalanceAmount)) {
                    PPredeem.setEnabled(false);
                    idialog = ProgressDialog.show(WalletActivity.this, "", "Requesting Amount...", true);
                    handler = new Handler();
                    handler.postDelayed(myRunnable = new Runnable() {
                        @Override
                        public void run() {

                            idialog.dismiss();
                            PPredeem.setEnabled(true);
                        }
                    }, 30000);


                    try {

                        Calendar c = Calendar.getInstance();
                        System.out.println("Current time => " + c.getTime());

                        SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmmss");
                        String formattedDate = df.format(c.getTime());

                        JSONObject Headerobj = new JSONObject();

                        Headerobj.put("merchantId", PPmerchantID);
                        Headerobj.put("transactionId", Billno + formattedDate);
                        Headerobj.put("merchantOrderId", Billno);
                        Headerobj.put("amount", Integer.valueOf(PPamt.getText().toString()) * 100);
                        Headerobj.put("instrumentType", PPinsType);
                        Headerobj.put("instrumentReference", mobileNo);
                        Headerobj.put("message", "collect for " + Billno + " order");
                        Headerobj.put("email", "");
                        Headerobj.put("expiresIn", Integer.valueOf(PPexpiresIn));
                        Headerobj.put("shortName", "Masethung");
                        Headerobj.put("storeId", PPstoreID);
                        Headerobj.put("terminalId", PPtermID);
                        redeemarray.put(0, Headerobj);
                        String data = redeemarray.toString().substring(1, redeemarray.toString().length() - 1);
                        encodebase64 = Base64.encodeToString(data.getBytes(), Base64.NO_WRAP);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
                    String URL = ppPath + "charge";
                    final String requestpe = "{\"request\":\"" + encodebase64 + "\"}";
                    String new_encodebase64 = encodebase64 + "/v3/charge" + PPsaltKey;
                    Sha256data = Hashing.sha256()
                            .hashString(new_encodebase64, StandardCharsets.UTF_8)
                            .toString();
                    Sha256data = Sha256data + "###" + PPsaltIndex;


                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String response) {

                            try {
                                Log.i(TAG, "Redeem Phonpe response: " + response);
                                JSONObject Jsonobj = new JSONObject(response);
                                String status = Jsonobj.getString("success");
                                if (status.equalsIgnoreCase("true")) {
                                    try {
                                        idialog.dismiss();
                                        handler.removeCallbacks(myRunnable);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    //PPredeem.setEnabled(true);
                                    PPstatus.setEnabled(true);
                                    isPPinitiated = true;
                                    showFailedAlert("Transaction initiated," +
                                            "Please ask the customer to approve the payment from their registered mobile number.");
                                    String data = Jsonobj.getString("data");
                                    JSONObject JsonData = new JSONObject(data);
                                    transID = JsonData.getString("transactionId");
                                    PPhandler = new Handler();
                                    PPhandler.postDelayed(PPrunnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            PPredeem.setEnabled(true);
                                        }
                                    }, 60000);
                                } else {
                                    PPredeem.setEnabled(true);
                                    String msg = Jsonobj.getString("message");
                                    showFailedAlert(msg);
                                    try {
                                        idialog.dismiss();
                                        handler.removeCallbacks(myRunnable);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            // Log.i("LOG_VOLLEY", response);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            try {
                                PPredeem.setEnabled(true);
                                try {
                                    idialog.dismiss();
                                    handler.removeCallbacks(myRunnable);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (error instanceof TimeoutError) {
                                    showFailedAlert("Time out error occurred.Please click on OK and try again");
                                    Log.e(TAG, "Time out error occurred.");
                                    //Time out error

                                } else if (error instanceof NoConnectionError) {
                                    showFailedAlert("Network error occurred.Please click on OK and try again");
                                    Log.e(TAG, "Network error occurred.");
                                    //net work error

                                } else if (error instanceof AuthFailureError) {
                                    showFailedAlert("Authentication error occurred.Please click on OK and try again");
                                    Log.e(TAG, "Authentication error occurred.");
                                    //error

                                } else if (error instanceof ServerError) {
                                    showFailedAlert("Server error occurred.Please click on OK and try again");
                                    Log.e(TAG, "Server error occurred.");
                                    //Error
                                } else if (error instanceof NetworkError) {
                                    showFailedAlert("Network error occurred.Please click on OK and try again");
                                    Log.e(TAG, "Network error occurred.");
                                    //Error

                                } else if (error instanceof ParseError) {
                                    //Error
                                    showFailedAlert("An error occurred.Please click on OK and try again");
                                    Log.e(TAG, "An error occurred.");
                                } else {

                                    showFailedAlert("An error occurred.Please click on OK and try again");
                                    Log.e(TAG, "An error occurred.");
                                    //Error
                                }
                                //End


                            } catch (Exception e) {


                            }
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("x-verify", Sha256data);
                            params.put("Content-Type", "application/json");
                            return params;
                        }


                        @Override
                        public byte[] getBody() throws AuthFailureError {

                            try {

                                return requestpe == null ? null : requestpe.getBytes("utf-8");

                            } catch (UnsupportedEncodingException uee) {
                                VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestpe, "utf-8");
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
                    showFailedAlert("wallet redemption amount should not be greater than balance bill amount");
                }
            } else {

                WalletActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this);
                        builder.setTitle("h&g mPOS");
                        builder.setMessage("PhonePe data not available in database. Please contact IT for support")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        View view = WalletActivity.this.getCurrentFocus();
                                        if (view != null) {
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            if (imm != null) {
                                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                            }
                                        }

                                        editor.putString("isWallet", "Y");
                                        editor.commit();
                                        Intent i = new Intent(WalletActivity.this, PaymentActivity.class);
                                        i.putExtra(Constants.FROM, fromActivity);
                                        startActivity(i);
                                        finish();

                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
            }
        } else
            showFailedAlert("Please enter amount");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void CheckPPstatus(View v) {
        Log.i(TAG, "Checking Phonpe status");
        idialog = ProgressDialog.show(WalletActivity.this, "", "Requesting Amount...", true);
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                idialog.dismiss();
                PPredeem.setEnabled(true);
            }
        }, 30000);

        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
        String URL = ppPath + "transaction/" + PPmerchantID + "/" + transID + "/status";
        Sha256data_check = Hashing.sha256()
                .hashString("/v3/transaction/" + PPmerchantID + "/" + transID + "/status" + PPsaltKey, StandardCharsets.UTF_8)
                .toString();
        Sha256data_check = Sha256data_check + "###" + PPsaltIndex;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {

            @Override
            public void onResponse(final String response) {

                try {
                    Log.i(TAG, "PhonePe check status response: " + response);
                    PPhandler.removeCallbacks(PPrunnable);
                    JSONObject Jsonobj = new JSONObject(response);
                    String status = Jsonobj.getString("success");
                    if (status.equalsIgnoreCase("true")) {
                        String statusMsg = Jsonobj.getString("code");
                        if (statusMsg.equalsIgnoreCase("PAYMENT_SUCCESS")) {
                            isPPinitiated = false;
                            String json_data = Jsonobj.getString("data");
                            JSONObject Json_data_obj = new JSONObject(json_data);

                            HashMap<String, String> walletDetails = new HashMap<String, String>();
                            walletDetails.put("walletID", "4");
                            walletDetails.put("walletName", "PhonePe");
                            walletDetails.put("walletAmount", String.valueOf(Integer.valueOf(Json_data_obj.getString("amount")) / 100));
                            walletDetails.put("walletOTP", Json_data_obj.getString("providerReferenceId"));
                            walletDetails.put("walletTransID", Json_data_obj.getString("transactionId"));


                            WalletDB walletDB = new WalletDB(getApplicationContext());
                            walletDB.open();
                            walletDB.insertWalletDetails(walletDetails);

                            ArrayList<WalletDetails> wallets = walletDB.getWalletDetails();
                            if (wallets.size() > 0) {
                                adapter = new WalletListAdapter(WalletActivity.this, wallets);
                                listView.setAdapter(adapter);
                            }

                            setBalanceAmount();
                            LoadPPlayout(PPlyt);
                            walletDB.close();
                            try {
                                idialog.dismiss();
                                handler.removeCallbacks(myRunnable);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String msg = Jsonobj.getString("message");
                            showFailedAlert(msg);
                        } else {
                            try {
                                idialog.dismiss();
                                handler.removeCallbacks(myRunnable);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String msg = Jsonobj.getString("message");
                            showFailedAlert(msg);
                        }
                    } else {
                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String msg = Jsonobj.getString("message");
                        showFailedAlert(msg);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Log.i("LOG_VOLLEY", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    try {
                        idialog.dismiss();
                        handler.removeCallbacks(myRunnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (error instanceof TimeoutError) {
                        showFailedAlert("Time out error occurred.");
                        Log.e(TAG, "Time out error occurred.");
                        //Time out error

                    } else if (error instanceof NoConnectionError) {
                        showFailedAlert("Network error occurred.");
                        Log.e(TAG, "Network error occurred.");
                        //net work error

                    } else if (error instanceof AuthFailureError) {
                        showFailedAlert("Authentication error occurred.");
                        Log.e(TAG, "Authentication error occurred.");
                        //error

                    } else if (error instanceof ServerError) {
                        showFailedAlert("Server error occurred.");
                        Log.e(TAG, "Server error occurred.");
                        //Error
                    } else if (error instanceof NetworkError) {
                        showFailedAlert("Network error occurred.");
                        Log.e(TAG, "Network error occurred.");
                        //Error

                    } else if (error instanceof ParseError) {
                        //Error
                        showFailedAlert("An error occurred.");
                        Log.e(TAG, "An error occurred.");
                    } else {

                        showFailedAlert("An error occurred.");
                        Log.e(TAG, "An error occurred.");
                        //Error
                    }

                } catch (Exception e) {


                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-verify", Sha256data_check);
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(stringRequest);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void CancelPPrequest() {
        Log.i(TAG, "Cancelling Phonepe status");
        idialog = ProgressDialog.show(WalletActivity.this, "", "Cancelling Phonepe request...", true);
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                idialog.dismiss();
                PPredeem.setEnabled(true);
            }
        }, 30000);

        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
        String URL = ppPath + "charge/" + PPmerchantID + "/" + transID + "/cancel ";
        Sha256data_check = Hashing.sha256()
                .hashString("/v3/charge/" + PPmerchantID + "/" + transID + "/cancel" + PPsaltKey, StandardCharsets.UTF_8)
                .toString();
        Sha256data_check = Sha256data_check + "###" + PPsaltIndex;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {

            @Override
            public void onResponse(final String response) {

                try {
                    Log.i(TAG, "PhonePe cancel transaction response: " + response);
                    PPhandler.removeCallbacks(PPrunnable);
                    JSONObject Jsonobj = new JSONObject(response);
                    String status = Jsonobj.getString("success");
                    if (status.equalsIgnoreCase("true")) {

                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Log.i(TAG, "Submitting with pending phonpe transaction");
                        editor.putString("isWallet", "Y");
                        editor.commit();
                        Intent i = new Intent(WalletActivity.this, PaymentActivity.class);
                        i.putExtra(Constants.FROM, fromActivity);
                        startActivity(i);
                        finish();


                    } else {
                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //String msg = Jsonobj.getString("message");
                        //showFailedAlert(msg);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Log.i("LOG_VOLLEY", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    try {
                        idialog.dismiss();
                        handler.removeCallbacks(myRunnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (error instanceof TimeoutError) {
                        showFailedAlert("Time out error occurred.");
                        Log.e(TAG, "Time out error occurred.");
                        //Time out error

                    } else if (error instanceof NoConnectionError) {
                        showFailedAlert("Network error occurred.");
                        Log.e(TAG, "Network error occurred.");
                        //net work error

                    } else if (error instanceof AuthFailureError) {
                        showFailedAlert("Authentication error occurred.");
                        Log.e(TAG, "Authentication error occurred.");
                        //error

                    } else if (error instanceof ServerError) {
                        showFailedAlert("Server error occurred.");
                        Log.e(TAG, "Server error occurred.");
                        //Error
                    } else if (error instanceof NetworkError) {
                        showFailedAlert("Network error occurred.");
                        Log.e(TAG, "Network error occurred.");
                        //Error

                    } else if (error instanceof ParseError) {
                        //Error
                        showFailedAlert("An error occurred.");
                        Log.e(TAG, "An error occurred.");
                    } else {

                        showFailedAlert("An error occurred.");
                        Log.e(TAG, "An error occurred.");
                        //Error
                    }

                } catch (Exception e) {


                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-verify", Sha256data_check);
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(stringRequest);
    }


    //Mobikwik wallet

    public void RedeemMKamount(View v) throws Exception {

        Log.i(TAG, "Redeeming Mobikwik amount");

        final JSONArray redeemarray = new JSONArray();
        String data = null;
        if (!MKamt.getText().toString().isEmpty() & !MKotp.getText().toString().isEmpty()) {

            if (MKotp.getText().toString().length() != 6) {
                showFailedAlert("Enter Valid OTP");

            } else {

                if (MKsmid != null && MKgenOTP != null && MKkey != null) {
                    if (Float.valueOf(MKamt.getText().toString()) <= Float.valueOf(BalanceAmount)) {
                        MKredeem.setEnabled(false);
                        idialog = ProgressDialog.show(WalletActivity.this, "", "Requesting Amount...", true);
                        handler = new Handler();
                        handler.postDelayed(myRunnable = new Runnable() {
                            @Override
                            public void run() {

                                idialog.dismiss();
                            }
                        }, 30000);


                        try {

                            JSONObject Headerobj = new JSONObject();
                            Headerobj.put("amount", MKamt.getText().toString());
                            Headerobj.put("cellno", mobileNo + MKotp.getText().toString());
                            Headerobj.put("merchantname", "test");
                            Headerobj.put("orderid", Billno);
                            Headerobj.put("smid", MKsmid);
                            Headerobj.put("genrateotp", MKgenOTP);

                            redeemarray.put(0, Headerobj);
                            data = redeemarray.toString().substring(1, redeemarray.toString().length() - 1);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        String key = MKkey;
                        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
                        String hashmsg = ComputeHash(data, key);
                        String URL = mkPath;


                        final String requestpe = "{\"request\":" + redeemarray.toString().substring(1, redeemarray.toString().length() - 1) + ",\"checksum\":\"" + hashmsg + "\"}";
                        Log.i(TAG, "Mobikwik request: " + requestpe);

                        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(final String response) {

                                try {

                                    Log.i(TAG, "mobikwik redeem response: " + response);
                                    JSONObject responseObject = new JSONObject(response);
                                    String resp = responseObject.getString("response");
                                    JSONObject json_response = new JSONObject(resp);
                                    String status = json_response.getString("status");
                                    String statusCode = json_response.getString("statusCode");
                                    if (status.equalsIgnoreCase("true") & statusCode.equalsIgnoreCase("0")) {

                                        String json_data = json_response.getString("data");
                                        JSONObject json_dataObj = new JSONObject(json_data);


                                        HashMap<String, String> walletDetails = new HashMap<String, String>();
                                        walletDetails.put("walletID", "5");
                                        walletDetails.put("walletName", "Mobikwik");
                                        walletDetails.put("walletAmount", json_dataObj.getString("debitedamount"));
                                        walletDetails.put("walletOTP", PTotp.getText().toString());
                                        walletDetails.put("walletTransID", json_dataObj.getString("refId"));


                                        WalletDB walletDB = new WalletDB(getApplicationContext());
                                        walletDB.open();
                                        walletDB.insertWalletDetails(walletDetails);

                                        ArrayList<WalletDetails> wallets = walletDB.getWalletDetails();

                                        if (wallets.size() > 0) {
                                            adapter = new WalletListAdapter(WalletActivity.this, wallets);
                                            listView.setAdapter(adapter);
                                        }
                                        setBalanceAmount();
                                        walletDB.close();
                                        showFailedAlert("Wallet amount received successfully");
                                        LoadMKlayout(MKlyt);
                                        MKredeem.setEnabled(true);
                                        try {
                                            idialog.dismiss();
                                            handler.removeCallbacks(myRunnable);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    } else {
                                        MKredeem.setEnabled(true);
                                        try {
                                            idialog.dismiss();
                                            handler.removeCallbacks(myRunnable);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        String msg = json_response.getString("statusDescription");
                                        showFailedAlert(msg);
                                    }


                                } catch (Exception e) {
                                    MKredeem.setEnabled(true);
                                    try {
                                        idialog.dismiss();
                                        handler.removeCallbacks(myRunnable);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    e.printStackTrace();
                                }
                                // Log.i("LOG_VOLLEY", response);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                try {
                                    MKredeem.setEnabled(true);
                                    try {
                                        idialog.dismiss();
                                        handler.removeCallbacks(myRunnable);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (error instanceof TimeoutError) {
                                        showFailedAlert("Time out error occurred.Please click on OK and try again");
                                        Log.e(TAG, "Time out error occurred.");
                                        //Time out error

                                    } else if (error instanceof NoConnectionError) {
                                        showFailedAlert("Network error occurred.Please click on OK and try again");
                                        Log.e(TAG, "Network error occurred.");
                                        //net work error

                                    } else if (error instanceof AuthFailureError) {
                                        showFailedAlert("Authentication error occurred.Please click on OK and try again");
                                        Log.e(TAG, "Authentication error occurred.");
                                        //error

                                    } else if (error instanceof ServerError) {
                                        showFailedAlert("Server error occurred.Please click on OK and try again");
                                        Log.e(TAG, "Server error occurred.");
                                        //Error
                                    } else if (error instanceof NetworkError) {
                                        showFailedAlert("Network error occurred.Please click on OK and try again");
                                        Log.e(TAG, "Network error occurred.");
                                        //Error

                                    } else if (error instanceof ParseError) {
                                        //Error
                                        showFailedAlert("An error occurred.Please click on OK and try again");
                                        Log.e(TAG, "An error occurred.");
                                    } else {

                                        showFailedAlert("An error occurred.Please click on OK and try again");
                                        Log.e(TAG, "An error occurred.");
                                        //Error
                                    }
                                    //End


                                } catch (Exception e) {


                                }
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("Content-Type", "application/json");
                                return params;
                            }


                            @Override
                            public byte[] getBody() throws AuthFailureError {

                                try {

                                    return requestpe == null ? null : requestpe.getBytes("utf-8");

                                } catch (UnsupportedEncodingException uee) {
                                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestpe, "utf-8");
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
                        showFailedAlert("wallet redemption amount should not be greater than balance bill amount");
                    }
                } else {
                    WalletActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this);
                            builder.setTitle("h&g mPOS");
                            builder.setMessage("Mobikwik data not available in database. Please contact IT for support")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            View view = WalletActivity.this.getCurrentFocus();
                                            if (view != null) {
                                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                if (imm != null) {
                                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                                }
                                            }

                                            editor.putString("isWallet", "Y");
                                            editor.commit();
                                            Intent i = new Intent(WalletActivity.this, PaymentActivity.class);
                                            i.putExtra(Constants.FROM, fromActivity);
                                            startActivity(i);
                                            finish();

                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    });
                }
            }
        } else {
            showFailedAlert("Either amount or OTP should not be empty");
        }

    }

    String requestType = "";
    String data = "", mTransactionID = "";
    Dialog QRCodeDialog;

    public void generateQRCode(View v) {

        //String URL = "http://35.200.175.164/hgwallet_test/api/PhonepeQR/GenerateQRCode/";
        //String URL = "http://35.200.175.164/hgwallet/api/PhonepeQR/GenerateQRCode/";
        String URL = IPAddress + "/hgwallet/api/PhonepeQR/GenerateQRCode/";

        CustomerDB custmrdb = new CustomerDB(getApplicationContext());
        custmrdb.open();
        ArrayList<CustomerDetails> customerdata = custmrdb.getCustomerDetails();
        custmrdb.close();

        if (etPhonepeWalletAmt.getText().toString().length() == 0)
            showFailedAlert("Enter Wallet Amount");
        else if (customerdata.size()==0){
            showFailedAlert("Wallet feature cannot be used without Mobile no");
        }
        else if (customerdata.size()>0){
            if (!customerdata.get(0).getMobileNO().isEmpty()){
                 idialog = ProgressDialog.show(WalletActivity.this, "", "Requesting Amount...", true);
            handler = new Handler();
            handler.postDelayed(myRunnable = new Runnable() {
                @Override
                public void run() {

                    idialog.dismiss();
                }
            }, 30000);

            JSONArray redeemarray = new JSONArray();
            try {

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
                String transIDData = sdf.format(c.getTime());
                String formattedDate = df.format(c.getTime());

                mTransactionID = Billno + transIDData;
                double walletAmount = Double.parseDouble(etPhonepeWalletAmt.getText().toString()) * 100;

                JSONObject dataHeaderObj = new JSONObject();
                //dataHeaderObj.put("merchantId", "M2306160483220675579140"); //Test
                dataHeaderObj.put("merchantId", PhonePeUPImerchantID); //Production
                dataHeaderObj.put("storeId", PhonePeUPIstoreID);
                dataHeaderObj.put("terminalId", PhonePeUPIterminalID);
                dataHeaderObj.put("transactionId", mTransactionID);
                dataHeaderObj.put("amount", walletAmount);
                dataHeaderObj.put("expiresIn", PhonePeUPIexpiresIn); //3 minutes
                dataHeaderObj.put("merchantOrderId", Billno + formattedDate);
                dataHeaderObj.put("message", "");

                JSONObject HeaderObj = new JSONObject();
                HeaderObj.put("statusCode", "200");
                HeaderObj.put("saltkey", PhonePeUPIsaltKey); //Production
                //HeaderObj.put("saltkey", "8289e078-be0b-484d-ae60-052f117f8deb"); //Test
                HeaderObj.put("saltindex", PhonePeUPIsaltIndex);
                HeaderObj.put("data", dataHeaderObj);

                redeemarray.put(0, HeaderObj);
                //data = redeemarray.toString().substring(1, redeemarray.toString().length() - 1);
                data = HeaderObj.toString();


            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestQueue requestQueue = AppController.getInstance().getRequestQueue();

            requestType = data;
            final String requestBody = data;
            //Log.i(TAG, "PhonePe request: " + requestType);

            /*showFailedAlert("Transaction initiated," +
                    "Please ask the customer to scan the QR Code using the PhonePe App.");*/

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        idialog.dismiss();
                        handler.removeCallbacks(myRunnable);

                        Log.i(TAG, "PhonePe Initilization: " + response);
                        JSONObject responseObject = new JSONObject(response);
                        boolean success = responseObject.getBoolean("success");
                        String status = responseObject.getString("code");
                        if (success && status.equalsIgnoreCase("SUCCESS")) {

                            JSONObject dataJsonObj = responseObject.getJSONObject("data");
                            isPPinitiated = true;

                            ShowQRCodeDailog(dataJsonObj.getString("qrString"));
                            Toast.makeText(WalletActivity.this, responseObject.getString("message"), Toast.LENGTH_SHORT).show();

                        } else {
                            try {
                                idialog.dismiss();
                                handler.removeCallbacks(myRunnable);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String msg = responseObject.getString("message");
                            showFailedAlert(msg);
                        }
                    } catch (Exception e) {
                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (error instanceof TimeoutError) {
                            showFailedAlert("Time out error occurred.Please click on OK and try again");
                            Log.e(TAG, "Time out error occurred.");

                        } else if (error instanceof NoConnectionError) {
                            showFailedAlert("Network error occurred.Please click on OK and try again");
                            Log.e(TAG, "Network error occurred.");

                        } else if (error instanceof AuthFailureError) {
                            showFailedAlert("Authentication error occurred.Please click on OK and try again");
                            Log.e(TAG, "Authentication error occurred.");

                        } else if (error instanceof ServerError) {
                            showFailedAlert("Server error occurred.Please click on OK and try again");
                            Log.e(TAG, "Server error occurred.");

                        } else if (error instanceof NetworkError) {
                            showFailedAlert("Network error occurred.Please click on OK and try again");
                            Log.e(TAG, "Network error occurred.");

                        } else if (error instanceof ParseError) {
                            showFailedAlert("An error occurred.Please click on OK and try again");
                            Log.e(TAG, "An error occurred.");
                        } else {

                            showFailedAlert("An error occurred.Please click on OK and try again");
                            Log.e(TAG, "An error occurred.");
                        }
                        //End
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");
                    return params;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }


                @Override
                public byte[] getBody() throws AuthFailureError {

                    try {

                        Log.d(TAG, "Request Type ==> " + requestBody);
                        return requestBody == null ? null : requestBody.getBytes("utf-8");

                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestType, "utf-8");
                        return null;
                    }

                }

               /* @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        Log.e(TAG, "Response String =====> " + responseString);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }*/

            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    6000,
                    3,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(stringRequest);
        }
            }
        }



    private void ShowQRCodeDailog(String response) {

        QRCodeDialog = new Dialog(this);
        QRCodeDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button DlgBtnPay, DlgBtnExit;
        ImageView DlgIvQRCode;
        QRCodeDialog.setContentView(R.layout.qrcode_dialog);

        DlgBtnPay = QRCodeDialog.findViewById(R.id.btn_pay);
        DlgBtnExit = QRCodeDialog.findViewById(R.id.btn_exit);
        DlgIvQRCode = QRCodeDialog.findViewById(R.id.iv_qrcode);

        Bitmap bitmap = QRCodeHelper
                .newInstance(this)
                .setContent(response)
                .setErrorCorrectionLevel(ErrorCorrectionLevel.Q)
                .setMargin(2)
                .getQRCOde();

        DlgIvQRCode.setImageBitmap(bitmap);

        DlgBtnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPaymentStatus();
            }
        });

        DlgBtnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this);
                builder.setTitle("h&g mPOS");
                builder.setMessage("UPI payment is initiated please check with the customer on payment status," +
                        " before exiting this screen.")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                QRCodeDialog.dismiss();
                                dialog.dismiss();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });

        QRCodeDialog.setCancelable(false);
        QRCodeDialog.show();
        Window window = QRCodeDialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private void checkPaymentStatus() {

        //String URL = "http://35.200.175.164/hgwallet_test/api/PhonepeQR/CheckStatus/";
        //String URL = "http://35.200.175.164/hgwallet/api/PhonepeQR/CheckStatus/";
        String URL = IPAddress + "/hgwallet/api/PhonepeQR/CheckStatus/";

        idialog = ProgressDialog.show(WalletActivity.this, "", "Checking Payment Status...", true);
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                idialog.dismiss();
            }
        }, 30000);

        String data = "";
        JSONArray redeemarray = new JSONArray();
        try {

            Calendar c = Calendar.getInstance();

            Log.e(TAG, "Transaction ID - Check Payment Status == > " + mTransactionID);

            JSONObject HeaderObj = new JSONObject();
            //HeaderObj.put("saltkey", "8289e078-be0b-484d-ae60-052f117f8deb"); //test
            //HeaderObj.put("merchantId", "M2306160483220675579140");
            HeaderObj.put("saltkey", PhonePeUPIsaltKey); //production
            HeaderObj.put("saltindex", PhonePeUPIsaltIndex);
            HeaderObj.put("merchantId", PhonePeUPImerchantID);
            HeaderObj.put("transactionId", mTransactionID);

            redeemarray.put(0, HeaderObj);
            //data = redeemarray.toString().substring(1, redeemarray.toString().length() - 1);
            data = HeaderObj.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();

        Log.i(TAG, "PhonePe Check Status request: " + data);

        requestType = data;
        final String requestBody = data;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {

                try {
                    idialog.dismiss();
                    handler.removeCallbacks(myRunnable);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    isPPinitiated = false;
                    Log.i(TAG, "PhonePe Check Payment Status : " + response);
                    JSONObject responseObject = new JSONObject(response);
                    boolean success = responseObject.getBoolean("success");
                    String code = responseObject.getString("code");
                    if (success && code.equalsIgnoreCase("PAYMENT_SUCCESS")) {

                        QRCodeDialog.dismiss();
                        JSONObject dataJsonObj = responseObject.getJSONObject("data");
                        loadPhonePeDataIntoLocalDb(dataJsonObj.getDouble("amount"), dataJsonObj.getString("transactionId"),
                                dataJsonObj.getString("providerReferenceId"));
                        Toast.makeText(WalletActivity.this, responseObject.getString("message"), Toast.LENGTH_SHORT).show();

                    } else {
                        String msg = responseObject.getString("message");
                        showFailedAlert(msg);
                    }
                } catch (Exception e) {
                    try {
                        idialog.dismiss();
                        handler.removeCallbacks(myRunnable);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    try {
                        idialog.dismiss();
                        handler.removeCallbacks(myRunnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (error instanceof TimeoutError) {
                        showFailedAlert("Time out error occurred.Please click on OK and try again");
                        Log.e(TAG, "Time out error occurred.");

                    } else if (error instanceof NoConnectionError) {
                        showFailedAlert("Network error occurred.Please click on OK and try again");
                        Log.e(TAG, "Network error occurred.");

                    } else if (error instanceof AuthFailureError) {
                        showFailedAlert("Authentication error occurred.Please click on OK and try again");
                        Log.e(TAG, "Authentication error occurred.");

                    } else if (error instanceof ServerError) {
                        showFailedAlert("Server error occurred.Please click on OK and try again");
                        Log.e(TAG, "Server error occurred.");

                    } else if (error instanceof NetworkError) {
                        showFailedAlert("Network error occurred.Please click on OK and try again");
                        Log.e(TAG, "Network error occurred.");

                    } else if (error instanceof ParseError) {
                        showFailedAlert("An error occurred.Please click on OK and try again");
                        Log.e(TAG, "An error occurred.");
                    } else {

                        showFailedAlert("An error occurred.Please click on OK and try again");
                        Log.e(TAG, "An error occurred.");
                    }
                    //End


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {

                try {

                    return requestBody == null ? null : requestBody.getBytes("utf-8");

                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestType, "utf-8");
                    return null;
                }

            }

        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(stringRequest);
    }

    private void loadPhonePeDataIntoLocalDb(double walletAmt, String transID, String refId) {
        HashMap<String, String> walletDetails = new HashMap<String, String>();
        walletDetails.put("walletID", "8");
        walletDetails.put("walletName", "PhonePeUPI");
        walletDetails.put("walletAmount", String.valueOf(walletAmt / 100));
        walletDetails.put("walletOTP", refId);
        walletDetails.put("walletTransID", transID);

        WalletDB walletDB = new WalletDB(getApplicationContext());
        walletDB.open();
        walletDB.insertWalletDetails(walletDetails);

        ArrayList<WalletDetails> wallets = walletDB.getWalletDetails();

        if (wallets.size() > 0) {
            adapter = new WalletListAdapter(WalletActivity.this, wallets);
            listView.setAdapter(adapter);
        }
        setBalanceAmount();
        walletDB.close();
        showFailedAlert("Wallet amount received successfully");
        LoadPhonePelayout(llPhonePeProcess);
    }

    public static String ComputeHash(String jsonData, String trasactionKey) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {


        try {
            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(trasactionKey.getBytes(), "HmacSHA256"));
            return toHexString(mac.doFinal(jsonData.getBytes()));
        } catch (final Exception e) {
            return "";
        }

    }

    private static String toHexString(final byte[] bytes) {
        final Formatter formatter = new Formatter();
        for (final byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public void SubmitWallet(View v) {
        if (isPPinitiated) {
            WalletActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this);
                    builder.setTitle("h&g mPOS");
                    builder.setMessage("PhonePe transaction is initiated, Do you want to go back by without completing transaction?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                public void onClick(DialogInterface dialog, int id) {
                                    CancelPPrequest();

                                    //on click event
                                }
                            }).setNegativeButton("No", null)
                            .show();

                }
            });

        } else {

            WalletActivity.this.runOnUiThread(new Runnable() {


                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this);
                    builder.setTitle("h&g mPOS");
                    builder.setMessage("Are you sure to submit wallet data?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    editor.putString("isWallet", "Y");
                                    editor.commit();
                                    Intent i = new Intent(WalletActivity.this, PaymentActivity.class);
                                    i.putExtra(Constants.FROM, fromActivity);
                                    startActivity(i);
                                    finish();

                                    //on click event
                                }
                            }).setNegativeButton("No", null)
                            .show();

                }
            });

        }
    }


    //Load Wallet Layouts

    public void LoadZagLayout(View v) {
        try {

            if (isPPinitiated) {
                WalletActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this);
                        builder.setTitle("h&g mPOS");
                        builder.setMessage("PhonePe transaction is initiated, Do you want to change wallet type without completing transaction?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        isPPinitiated = false;
                                        WalletDB walletDB = new WalletDB(getApplicationContext());
                                        walletDB.open();
                                        isUsedWallet = walletDB.isWalletused("3");
                                        walletDB.close();
                                        resetControls();
                                        if (ZagLyt.getVisibility() == View.VISIBLE)
                                            ZagLyt.setVisibility(View.GONE);
                                        else {

                                            if (!isUsedWallet) {
                                                ZagLyt.setVisibility(View.VISIBLE);
                                                PPlyt.setVisibility(View.GONE);
                                                MKlyt.setVisibility(View.GONE);
                                                PtmLyt.setVisibility(View.GONE);
                                            } else {
                                                showFailedAlert("Zaggle wallet is already used for this bill.");
                                            }
                                        }
                                        //on click event
                                    }
                                }).setNegativeButton("No", null)
                                .show();

                    }
                });

            } else {

                WalletDB walletDB = new WalletDB(getApplicationContext());
                walletDB.open();
                isUsedWallet = walletDB.isWalletused("3");
                walletDB.close();
                resetControls();
                if (ZagLyt.getVisibility() == View.VISIBLE)
                    ZagLyt.setVisibility(View.GONE);
                else {

                    if (!isUsedWallet) {
                        ZagLyt.setVisibility(View.VISIBLE);
                        PPlyt.setVisibility(View.GONE);
                        MKlyt.setVisibility(View.GONE);
                        PtmLyt.setVisibility(View.GONE);
                    } else {
                        showFailedAlert("Zaggle wallet is already used for this bill.");
                    }
                }

            }
            //ParentLyt.removeAllViewsInLayout();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void LoadPaytmLayout(View v) {
        try {

            if (isPPinitiated) {
                WalletActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this);
                        builder.setTitle("h&g mPOS");
                        builder.setMessage("PhonePe transaction is initiated, Do you want to change wallet type without completing transaction?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        isPPinitiated = false;
                                        WalletDB walletDB = new WalletDB(getApplicationContext());
                                        walletDB.open();
                                        isUsedWallet = walletDB.isWalletused("1");
                                        walletDB.close();
                                        resetControls();
                                        if (PtmLyt.getVisibility() == View.VISIBLE)
                                            PtmLyt.setVisibility(View.GONE);
                                        else {
                                            if (!isUsedWallet) {
                                                PtmLyt.setVisibility(View.VISIBLE);
                                                ZagLyt.setVisibility(View.GONE);
                                                PPlyt.setVisibility(View.GONE);
                                                MKlyt.setVisibility(View.GONE);
                                            } else {
                                                showFailedAlert("Paytm wallet is already used for this bill.");
                                            }
                                        }
                                    }
                                }).setNegativeButton("No", null)
                                .show();

                    }
                });

            } else {

                WalletDB walletDB = new WalletDB(getApplicationContext());
                walletDB.open();
                isUsedWallet = walletDB.isWalletused("1");
                walletDB.close();
                resetControls();
                if (PtmLyt.getVisibility() == View.VISIBLE)
                    PtmLyt.setVisibility(View.GONE);
                else {
                    if (!isUsedWallet) {
                        PtmLyt.setVisibility(View.VISIBLE);
                        ZagLyt.setVisibility(View.GONE);
                        PPlyt.setVisibility(View.GONE);
                        MKlyt.setVisibility(View.GONE);
                    } else {
                        showFailedAlert("Paytm wallet is already used for this bill.");
                    }
                }
            }
            //ParentLyt.removeAllViewsInLayout();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void LoadPPlayout(View v) {
        try {


            if (isPPinitiated) {
                WalletActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this);
                        builder.setTitle("h&g mPOS");
                        builder.setMessage("PhonePe transaction is initiated, Do you want to change wallet type without completing transaction?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        isPPinitiated = false;
                                        resetControls();
                                        WalletDB walletDB = new WalletDB(getApplicationContext());
                                        walletDB.open();
                                        isUsedWallet = walletDB.isWalletused("4");
                                        walletDB.close();
                                        if (PPlyt.getVisibility() == View.VISIBLE)
                                            PPlyt.setVisibility(View.GONE);
                                        else {
                                            if (!isUsedWallet) {
                                                PPlyt.setVisibility(View.VISIBLE);
                                                ZagLyt.setVisibility(View.GONE);
                                                MKlyt.setVisibility(View.GONE);
                                                PtmLyt.setVisibility(View.GONE);
                                            } else {
                                                showFailedAlert("PhonePe wallet is already used for this bill.");
                                            }
                                        }
                                    }
                                }).setNegativeButton("No", null)
                                .show();

                    }
                });

            } else {


                resetControls();

                WalletDB walletDB = new WalletDB(getApplicationContext());
                walletDB.open();
                isUsedWallet = walletDB.isWalletused("4");
                walletDB.close();
                if (PPlyt.getVisibility() == View.VISIBLE)
                    PPlyt.setVisibility(View.GONE);
                else {
                    if (!isUsedWallet) {
                        PPlyt.setVisibility(View.VISIBLE);
                        ZagLyt.setVisibility(View.GONE);
                        MKlyt.setVisibility(View.GONE);
                        PtmLyt.setVisibility(View.GONE);
                    } else {
                        showFailedAlert("PhonePe wallet is already used for this bill.");
                    }
                }
            }
            //ParentLyt.removeAllViewsInLayout();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void LoadMKlayout(View v) {
        try {


            if (isPPinitiated) {
                WalletActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this);
                        builder.setTitle("h&g mPOS");
                        builder.setMessage("PhonePe transaction is initiated, Do you want to change wallet type without completing transaction?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        isPPinitiated = false;
                                        resetControls();

                                        WalletDB walletDB = new WalletDB(getApplicationContext());
                                        walletDB.open();
                                        isUsedWallet = walletDB.isWalletused("5");
                                        walletDB.close();

                                        if (MKlyt.getVisibility() == View.VISIBLE)
                                            MKlyt.setVisibility(View.GONE);
                                        else {
                                            if (!isUsedWallet) {
                                                MKlyt.setVisibility(View.VISIBLE);
                                                PPlyt.setVisibility(View.GONE);
                                                ZagLyt.setVisibility(View.GONE);
                                                PtmLyt.setVisibility(View.GONE);
                                            } else {
                                                showFailedAlert("Mobikwik wallet is already used for this bill.");
                                            }
                                        }
                                    }
                                }).setNegativeButton("No", null)
                                .show();

                    }
                });

            } else {
                resetControls();

                WalletDB walletDB = new WalletDB(getApplicationContext());
                walletDB.open();
                isUsedWallet = walletDB.isWalletused("5");
                walletDB.close();

                if (MKlyt.getVisibility() == View.VISIBLE)
                    MKlyt.setVisibility(View.GONE);
                else {
                    if (!isUsedWallet) {
                        MKlyt.setVisibility(View.VISIBLE);
                        PPlyt.setVisibility(View.GONE);
                        ZagLyt.setVisibility(View.GONE);
                        PtmLyt.setVisibility(View.GONE);
                    } else {
                        showFailedAlert("Mobikwik wallet is already used for this bill.");
                    }
                }
            }
            //ParentLyt.removeAllViewsInLayout();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void LoadPhonePelayout(View v) {
        try {


            if (isPPinitiated) {
                WalletActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this);
                        builder.setTitle("h&g mPOS");
                        builder.setMessage("PhonePe transaction is initiated, Do you want to change wallet type without completing transaction?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        isPPinitiated = false;
                                        resetControls();

                                        WalletDB walletDB = new WalletDB(getApplicationContext());
                                        walletDB.open();
                                        isUsedWallet = walletDB.isWalletused("4");
                                        walletDB.close();

                                        if (llPhonePeProcess.getVisibility() == View.VISIBLE)
                                            llPhonePeProcess.setVisibility(View.GONE);
                                        else {
                                            if (!isUsedWallet) {
                                                llPhonePeProcess.setVisibility(View.VISIBLE);
                                                MKlyt.setVisibility(View.GONE);
                                                PPlyt.setVisibility(View.GONE);
                                                ZagLyt.setVisibility(View.GONE);
                                                PtmLyt.setVisibility(View.GONE);
                                            } else {
                                                showFailedAlert("Mobikwik wallet is already used for this bill.");
                                            }
                                        }
                                    }
                                }).setNegativeButton("No", null)
                                .show();

                    }
                });

            } else {
                resetControls();

                WalletDB walletDB = new WalletDB(getApplicationContext());
                walletDB.open();
                isUsedWallet = walletDB.isWalletused("4");
                walletDB.close();

                if (llPhonePeProcess.getVisibility() == View.VISIBLE)
                    llPhonePeProcess.setVisibility(View.GONE);
                else {
                    if (!isUsedWallet) {
                        llPhonePeProcess.setVisibility(View.VISIBLE);
                        MKlyt.setVisibility(View.GONE);
                        PPlyt.setVisibility(View.GONE);
                        ZagLyt.setVisibility(View.GONE);
                        PtmLyt.setVisibility(View.GONE);
                    } else {
                        showFailedAlert("PhonePe wallet is already used for this bill.");
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setBalanceAmount() {
        try {
            WalletDB walletDB = new WalletDB(getApplicationContext());
            walletDB.open();
            String totalAmt = walletDB.getTotalAmt();
            walletDB.close();
            Float walletAmt = Float.valueOf(totalAmt);
            Float balanceAmt = Float.valueOf(BalanceAmount);
            Float Balance = balanceAmt - walletAmt;
            txtBalBillAmt.setText(Balance.toString());
        } catch (Exception e) {

        }

    }

    public void resetControls() {
        try {
            resetZagMenu();
            PTamt.setText("");
            PTotp.setText("");
            PPamt.setText("");
            MKamt.setText("");
            MKotp.setText("");
        } catch (Exception e) {

        }
    }

    @Override
    public void onDestroy() {

        try {
            handler.removeCallbacks(myRunnable);
            PPhandler.removeCallbacks(PPrunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }


        super.onDestroy();

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            View view = WalletActivity.this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }

            if (isPPinitiated) {
                WalletActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this);
                        builder.setTitle("h&g mPOS");
                        builder.setMessage("PhonePe transaction is initiated, Do you want to go back by without completing transaction?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        editor.putString("isWallet", "Y");
                                        editor.commit();
                                        Intent i = new Intent(WalletActivity.this, PaymentActivity.class);
                                        i.putExtra(Constants.FROM, fromActivity);
                                        startActivity(i);
                                        finish();
                                        //on click event
                                    }
                                }).setNegativeButton("No", null)
                                .show();

                    }
                });
                ;
            } else {

                WalletActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this);
                        builder.setTitle("h&g mPOS");
                        builder.setMessage("Do you want to go back?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        editor.putString("isWallet", "Y");
                                        editor.commit();
                                        Intent i = new Intent(WalletActivity.this, PaymentActivity.class);
                                        i.putExtra(Constants.FROM, fromActivity);
                                        startActivity(i);
                                        finish();
                                        //on click event
                                    }
                                }).setNegativeButton("No", null)
                                .show();

                    }
                });
                ;


            }


        }
        return super.onKeyDown(keyCode, event);
    }


    public void showFailedAlert(final String msg) {

        WalletActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this);
                builder.setTitle("h&g mPOS");
                builder.setMessage(msg)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {


                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

    }
}
