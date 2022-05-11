package in.hng.mpos.MarketPlace;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.eze.api.EzeAPI;

import org.innoviti.portUtil.Inno_SerialComm_Process_EDC;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

import in.hng.mpos.Database.CustomerDB;
import in.hng.mpos.Database.LoyaltyCredentialsDB;
import in.hng.mpos.Database.LoyaltyDetailsDB;
import in.hng.mpos.Database.ProductDetailsDB;
import in.hng.mpos.Database.TransactionDB;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.Database.WalletDB;
import in.hng.mpos.R;
import in.hng.mpos.activity.PrinterActivity;
import in.hng.mpos.activity.WalletActivity;
import in.hng.mpos.activity.WebViewEbill;
import in.hng.mpos.activity.WebViewEbill_XPrinter;
import in.hng.mpos.gettersetter.CustomerDetails;
import in.hng.mpos.gettersetter.LoyaltyCredentials;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.gettersetter.WalletDetails;
import in.hng.mpos.helper.AlertManager;
import in.hng.mpos.helper.ApiCall;
import in.hng.mpos.helper.AppController;
import in.hng.mpos.helper.ConnectionDetector;
import in.hng.mpos.helper.Log;

public class MarketPayment extends AppCompatActivity implements Inno_SerialComm_Process_EDC.QRCodePaymentInterface, View.OnClickListener {


    public static final int DISMISS_WINDOW = 100;
    private Thread childThread;
    private CountDownTimer countDownTimer;
    private String inputXmlData, responseResult;
    private String txn_type, txn_mode, invoice_no, date_time, card_no, amount, loyalty_pnt, Location, cust_mobile, PATH;
    private Inno_SerialComm_Process_EDC inno_serialComm_process_edc;
    private EditText payment_card, coupon_code, coupon_amt, loyalty_amt, wallet_amt, redeem_code, loyalty_point;
    private TextView payable, Bal, BalPoint;
    private Button pay, apply, cancel_bill, new_bill;
    private String bal_amt, bill_no, otp_code;
    private Float billamt, cashamt, cashreturn, Walletamt, Lyltyamt, Creditamt, Couponamt, Totalamt, Balance, Otheramt;
    private Integer redemption_point;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Context context;
    private float total;
    private Boolean bill_staus = false;
    float bal_amount, max_amount, redeem_point;
    String couponSno = "", tID, cardNO, ApprovalCode;
    private static final String TAG = "MarketPayment";
    private String resCode, resMsg, transDate, transTime, invNO, RRN;
    ProgressDialog idialog = null;
    private String CashierID, tillNo;
    private LinearLayout couponPromo;
    private EditText bbQty;
    private String bbEligibility;
    private PopupWindow CardDetails_popup;
    private LinearLayout linearLayout_paymnt;
    private Spinner SpnrCard, SpnrEDC;
    private EditText authCode, CCamt;
    private Button cardDetailsOK, cardDetailsCancel;
    private String EDCtype, CardType, authID;
    private Float CardAmount = Float.parseFloat("0.00");
    private String PBamount, PBcode;
    private String isOrderID;
    private Handler handler;
    private boolean iswalletApplied = false;
    Runnable myRunnable = null;
    private String isWallet;


    private final int REQUEST_CODE_INITIALIZE = 10001;
    private final int REQUEST_CODE_PREPARE = 10002;
    private final int REQUEST_CODE_SALE_TXN = 10006;
    private final int REQUEST_CODE_CLOSE = 10014;


    private Handler messageHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISMISS_WINDOW:
                    if (countDownTimer != null)
                        countDownTimer.cancel();
                    if (!responseResult.equals("")) {
                        resCode = responseResult.substring(responseResult.indexOf("<ResponseCode>") + 14, responseResult.indexOf("</ResponseCode>"));
                        resMsg = responseResult.substring(responseResult.indexOf("<ResponseMessage>") + 17, responseResult.indexOf("</ResponseMessage>"));
                        Toast.makeText(MarketPayment.this, resCode + " : " + resMsg, Toast.LENGTH_LONG).show();
                        Log.i(TAG, resMsg);
                        if (resCode.equalsIgnoreCase("00")) {
                            String transDateTime = responseResult.substring(responseResult.indexOf("<TransactionTime>") + 17, responseResult.indexOf("</TransactionTime>"));
                            transDate = transDateTime.substring(0, 10);
                            transTime = transDateTime.substring(11, 19);
                            cardNO = responseResult.substring(responseResult.indexOf("<CardNumber>") + 12, responseResult.indexOf("</CardNumber>"));
                            tID = responseResult.substring(responseResult.indexOf("<TID>") + 5, responseResult.indexOf("</TID>"));
                            invNO = responseResult.substring(responseResult.indexOf("<InvoiceNumber>") + 15, responseResult.indexOf("</InvoiceNumber>"));
                            RRN = responseResult.substring(responseResult.indexOf("<RetrievalReferenceNumber>") + 26, responseResult.indexOf("</RetrievalReferenceNumber>"));
                            ApprovalCode = responseResult.substring(responseResult.indexOf("<ApprovalCode>") + 14, responseResult.indexOf("</ApprovalCode>"));
                            NumberFormat formatter = new DecimalFormat("#0.00");
                            String temp = String.valueOf(formatter.format(total));
                            payment_card.setText(temp);
                            Couponamt = Float.parseFloat(coupon_amt.getText().toString().equalsIgnoreCase("") ? "0.00" : coupon_amt.getText().toString());
                            Lyltyamt = Float.parseFloat(loyalty_amt.getText().toString().equalsIgnoreCase("") ? "0.00" : loyalty_amt.getText().toString());
                            Walletamt = Float.parseFloat(wallet_amt.getText().toString().equalsIgnoreCase("") ? "0.00" : wallet_amt.getText().toString());
                            Totalamt = Couponamt + cashamt  + Lyltyamt + Walletamt;
                            Balance = billamt - Totalamt;
                            Bal.setText(Balance.toString());
                            pay.performClick();
                        }
                    }
                    break;
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_payment);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        bal_amt = sp.getString("Total_Amt", "0.0");
        bill_no = sp.getString("Bill_no", "");
        PBcode = sp.getString("Coupon_Code", "");
        PBamount = sp.getString("Coupon_Disc", "");
        isOrderID = sp.getString("isOrderID", "");
        isWallet = sp.getString("isWallet", "");
        billamt = Float.parseFloat(bal_amt);
        //Resources res = getResources();
        //PATH = res.getString(R.string.API_URL);
        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH =urlDB.getUrlDetails();
        urlDB.close();
        inno_serialComm_process_edc = new Inno_SerialComm_Process_EDC();
        inno_serialComm_process_edc.registerCallback(this);
        context = this;


        // Set up your ActionBar
        final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(
                R.layout.action_bar_simple, null);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarLayout);
        actionBar.setTitle("");
        final int actionBarColor = getResources().getColor(R.color.ActionBarColor);
        actionBar.setBackgroundDrawable(new ColorDrawable(actionBarColor));
        TextView title = findViewById(R.id.ab_activity_title);
        title.setText("h&g mPOS - Payment");
        title.setVisibility(View.VISIBLE);


        loyalty_point = findViewById(R.id.txtloyaltypoints);
        coupon_code = findViewById(R.id.txtcoupncode);
        cancel_bill = findViewById(R.id.cancelbill);
        wallet_amt = findViewById(R.id.txtwallet);
        redeem_code = findViewById(R.id.txtredem);

        loyalty_amt = findViewById(R.id.txtlyty);

        payment_card = findViewById(R.id.txtcc);
        BalPoint = findViewById(R.id.txtbalpnt);
        payable = findViewById(R.id.txtbillamt);
        coupon_amt = findViewById(R.id.txtcu);
        apply = findViewById(R.id.Applycoupn);
        Bal = findViewById(R.id.txtbalamt);

        pay = findViewById(R.id.btn_market_payment_pay);
        new_bill = findViewById(R.id.BillNew);
        couponPromo = findViewById(R.id.PromoLayout);
        bbQty = findViewById(R.id.txtcuqty);

        linearLayout_paymnt = findViewById(R.id.linearLayoutPymnt);

        payable.setText(bal_amt);
        Bal.setText(bal_amt);


        CustomerDB cusdb = new CustomerDB(getApplicationContext());
        cusdb.open();
        ArrayList<CustomerDetails> customerDetails = cusdb.getCustomerDetails();
        cusdb.close();

        if (customerDetails.size() > 0) {
            loyalty_pnt = customerDetails.get(0).getPoints();
            cust_mobile = customerDetails.get(0).getMobileNO();
            BalPoint.setText(loyalty_pnt);
        }


        UserDB userDB = new UserDB(getApplicationContext());
        userDB.open();
        ArrayList<UserDetails> userDetailses = userDB.getUserDetails();
        userDB.close();

        CashierID = userDetailses.get(0).getUserID();
        Location = userDetailses.get(0).getStoreID();
        tillNo = userDetailses.get(0).getTillNo();


        if (isWallet.equalsIgnoreCase("Y")) {
            editor = sp.edit();
            loyalty_point.setText(sp.getString("Lytpoint", ""));
            redeem_code.setText(sp.getString("Lytotp", ""));
            loyalty_amt.setText(sp.getString("Lytamt", ""));
            coupon_code.setText(sp.getString("Cpncode", ""));
            coupon_amt.setText(sp.getString("Cpnamt", ""));
            update_amount();
        }
        WalletDB walletDB = new WalletDB(getApplicationContext());
        walletDB.open();
        ArrayList<WalletDetails> wallets = walletDB.getWalletDetails();
        if (wallets.size() > 0) {
            iswalletApplied = true;
            wallet_amt.setText(walletDB.getTotalAmt());
            update_amount();
        }
        walletDB.close();

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        bbEligibility = sp.getString("bbPromo", "");


        if (bbEligibility.equalsIgnoreCase("Y")) {
            bbQty.setVisibility(View.VISIBLE);
            couponPromo.setVisibility(View.VISIBLE);
        }

        /*
        TransactionDB trDB = new TransactionDB(getApplicationContext());
        trDB.open();
        ArrayList<TransactionDetails> transactionDetails = trDB.getTransactionDetails();
        trDB.close();

        */

        Log.i(TAG, "Loading Payment for BillNo:" + bill_no);

//        apply.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.i(TAG, "Applying Coupon");
//                View v = MarketPayment.this.getCurrentFocus();
//                if (v != null) {
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                }
//                ConnectionDetector net = new ConnectionDetector(getApplicationContext());
//                if (net.isConnectingToInternet()) {
//                    if (!coupon_code.getText().toString().isEmpty()) {
//                        idialog = ProgressDialog.show(MarketPayment.this, "", "Applying Coupon...", true);
//                        handler = new Handler();
//                        handler.postDelayed(myRunnable = new Runnable() {
//                            @Override
//                            public void run() {
//
//                                idialog.dismiss();
//                            }
//                        }, 30000);
//
//
//                        try {
//                            String BBQty;
//                            RequestQueue queue = AppController.getInstance().getRequestQueue();
//                            if (bbEligibility.equalsIgnoreCase("Y")) {
//                                BBQty = bbQty.getText().toString().equalsIgnoreCase("") ? "0" : bbQty.getText().toString();
//                            } else {
//                                BBQty = "";
//                            }
//                            final String url = PATH + "coupon?billNo=" + bill_no + "&couponslno=" + coupon_code.getText().toString().trim() + "&location=" + Location + "&FreeQuantity=" + BBQty + "&CashierID=" + CashierID + "&couponType=N";
//                            Log.i(TAG, url);
//                            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
//                                    new Response.Listener<JSONObject>() {
//                                        @Override
//                                        public void onResponse(JSONObject response) {
//                                            try {
//                                                if (response.getString("statusCode").equalsIgnoreCase("200")) {
//                                                    Log.d("Coupon Response", response.toString());
//                                                    String value = response.getString("couponvalue");
//                                                    couponSno = response.getString("couponcode");
//                                                    coupon_amt.setText(value);
//                                                    if (bbEligibility.equalsIgnoreCase("Y")) {
//                                                        bbQty.setText(response.getString("appliedQty"));
//                                                        bbQty.clearFocus();
//                                                    }
//                                                    Couponamt = Float.parseFloat(coupon_amt.getText().toString().equalsIgnoreCase("") ? "0.00" : coupon_amt.getText().toString());
//                                                    Lyltyamt = Float.parseFloat(loyalty_amt.getText().toString().equalsIgnoreCase("") ? "0.00" : loyalty_amt.getText().toString());
//                                                    Walletamt = Float.parseFloat(wallet_amt.getText().toString().equalsIgnoreCase("") ? "0.00" : wallet_amt.getText().toString());
//                                                    Totalamt = Couponamt + cashamt + Lyltyamt + Walletamt;
//                                                    Balance = billamt - Totalamt;
//                                                    Bal.setText(Balance.toString());
//                                                    idialog.dismiss();
//                                                    handler.removeCallbacks(myRunnable);
//                                                    Toast toast = Toast.makeText(getApplicationContext(), "Coupon Applied Successfully", Toast.LENGTH_SHORT);
//                                                    toast.show();
//                                                } else {
//                                                    idialog.dismiss();
//                                                    handler.removeCallbacks(myRunnable);
//                                                    coupon_amt.setText("0.00");
//                                                    String msg = response.getString("Message");
//                                                    showFailedAlert(msg);
//                                                }
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                    },
//                                    new Response.ErrorListener() {
//                                        @Override
//                                        public void onErrorResponse(VolleyError error) {
//
//
//                                            try {
//                                                idialog.dismiss();
//                                                handler.removeCallbacks(myRunnable);
//                                                if (error instanceof TimeoutError) {
//                                                    showFailedAlert("Time out error occurred.Please click on OK and try again");
//                                                    Log.e(TAG, "Time out error occurred.");
//                                                    //Time out error
//
//                                                } else if (error instanceof NoConnectionError) {
//                                                    showFailedAlert("Network error occurred.Please click on OK and try again");
//                                                    Log.e(TAG, "Network error occurred.");
//                                                    //net work error
//
//                                                } else if (error instanceof AuthFailureError) {
//                                                    showFailedAlert("Authentication error occurred.Please click on OK and try again");
//                                                    Log.e(TAG, "Authentication error occurred.");
//                                                    //error
//
//                                                } else if (error instanceof ServerError) {
//                                                    showFailedAlert("Server error occurred.Please click on OK and try again");
//                                                    Log.e(TAG, "Server error occurred.");
//                                                    //Error
//                                                } else if (error instanceof NetworkError) {
//                                                    showFailedAlert("Network error occurred.Please click on OK and try again");
//                                                    Log.e(TAG, "Network error occurred.");
//                                                    //Error
//
//                                                } else if (error instanceof ParseError) {
//                                                    //Error
//                                                    showFailedAlert("An error occurred.Please click on OK and try again");
//                                                    Log.e(TAG, "An error occurred.");
//                                                } else {
//
//                                                    showFailedAlert("An error occurred.Please click on OK and try again");
//                                                    Log.e(TAG, "An error occurred.");
//                                                    //Error
//                                                }
//                                                //End
//
//
//                                            } catch (Exception e) {
//
//
//                                            }
//
//
//
//
//
//
//                                            /*
//                                            String body;
//                                            try {
//                                                body = new String(error.networkResponse.data, "UTF-8");
//                                                showFailedAlert(body);
//                                                Log.d("Error.Response",body);
//                                            } catch (UnsupportedEncodingException e) {
//                                                e.printStackTrace();
//                                            }
//
//                                            */
//
//                                        }
//                                    }
//                            ) {
//                                @Override
//                                public String getBodyContentType() {
//                                    return "application/json; charset=utf-8";
//                                }
//                            };
//
//                            // add it to the RequestQueue
//                            queue.add(getRequest);
//
//
//                        } catch (Exception e) {
//                            // dialog.dismiss();
//                            showFailedAlert(e.getMessage());
//                            Log.e(TAG, e.getMessage());
//                            System.out.println("Exception : " + e.getMessage());
//                        }
//
//
//                    } else {
//                        showFailedAlert("Please enter coupon code");
//
//                    }
//
//                } else {
//                    AlertManager alert = new AlertManager();
//                    alert.alert(MarketPayment.this, "No Internet Connection",
//                            "Please check " +
//                                    "your data connection or Wifi is ON !");
//
//                }
//
//            }
//
//
//        });

        new_bill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!iswalletApplied) {

                    new AlertDialog.Builder(MarketPayment.this)
                            .setMessage("Are you sure to continue with new bill?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog = ProgressDialog.show(MarketPayment.this, "",
                                            "Saving Details...", false);
                                    final DialogInterface finalDialog = dialog;
                                    deletetables();
                                    finalDialog.dismiss();
                                    editor.clear();
                                    editor.commit();
                                    Intent intent = new Intent(MarketPayment.this, OrderInfromation.class);
                                    startActivity(intent);
                                    finish();

                                }
                            })
                            .setNegativeButton("No", null)
                            .show();


                }
                else
                {
                    showFailedAlert("can't do new bill as wallet is applied.");
                }
            }

        });

//        manual_pay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.i(TAG, "Loading Card Details Window");
//                View v = MarketPayment.this.getCurrentFocus();
//                LayoutInflater layoutInflater = (LayoutInflater) MarketPayment.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                if (v != null) {
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                }
//                View customView = layoutInflater.inflate(R.layout.card_details_popup, null);
//                CardDetails_popup = new PopupWindow(customView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
//
//                SpnrCard = customView.findViewById(R.id.spnrCard);
//                SpnrEDC = customView.findViewById(R.id.SpnrEDC);
//                authCode = customView.findViewById(R.id.txtAuthCode);
//                CCamt = customView.findViewById(R.id.txtamount);
//                cardDetailsOK = customView.findViewById(R.id.btnok);
//                cardDetailsCancel = customView.findViewById(R.id.btnCancel);
//
//
//                CCamt.setText(String.valueOf(CardAmount).equalsIgnoreCase("null") ? "" : String.valueOf(CardAmount));
//                authCode.setText(authID);
//
//
//                SpnrCard.setOnItemSelectedListener(new MarketPayment.CustomOnItemSelectedListener());
//
//                //display the popup window
//                CardDetails_popup.showAtLocation(linearLayout_paymnt, Gravity.CENTER, 0, 40);
//                CardDetails_popup.setFocusable(true);
//                CardDetails_popup.update();
//
//
//                SpnrCard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//
//                    @Override
//                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                        CardType = String.valueOf(parent.getSelectedItemPosition() + 1);
//                    }
//
//
//                    @Override
//                    public void onNothingSelected(AdapterView<?> parent) {
//                        CardType = "1";
//                    }
//                });
//
//                SpnrEDC.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//
//                    @Override
//                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//
//                        EDCtype = String.valueOf(parent.getSelectedItemPosition() + 1);
//                        // category=parent.getSelectedItem().toString();
//                    }
//
//
//                    @Override
//                    public void onNothingSelected(AdapterView<?> parent) {
//                        EDCtype = "1";
//                    }
//                });
//
//                cardDetailsOK.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                        CardAmount = CCamt.getText().toString().length() > 0 ? Float.parseFloat(CCamt.getText().toString()) : Float.parseFloat("0.00");
//                        payment_card.setText(String.valueOf(CardAmount).equalsIgnoreCase("null") ? "" : String.valueOf(CardAmount));
//                        authID = authCode.getText().toString();
//                        CardDetails_popup.dismiss();
//                        update_amount();
//
//                    }
//                });
//
//
//                //close the popup window on button click
//                cardDetailsCancel.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        CardDetails_popup.dismiss();
//                    }
//                });
//
//            }
//        });
//        initiate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.i(TAG, "Initiating Card Payment");
//
//                ConnectionDetector net = new ConnectionDetector(getApplicationContext());
//                if (net.isConnectingToInternet()) {
//
//
//                    CardAmount = Float.parseFloat("0.00");
//                    payment_card.setText(CardAmount.toString());
//                    update_amount();
//                    //final float total;
//                    txn_type = "00";
//                    txn_mode = "22";
//                    invoice_no = "123456";
//                    date_time = "1234567890";
//                    card_no = "1212121212";
//                    amount = "100";
//
//                    Couponamt = Float.parseFloat(coupon_amt.getText().toString().equalsIgnoreCase("") ? "0.00" : coupon_amt.getText().toString());
//                    cashamt = Float.parseFloat(paymnt_cash.getText().toString().equalsIgnoreCase("") ? "0.00" : paymnt_cash.getText().toString());
//                    Creditamt = Float.parseFloat(payment_card.getText().toString().equalsIgnoreCase("") ? "0.00" : payment_card.getText().toString());
//                    Lyltyamt = Float.parseFloat(loyalty_amt.getText().toString().equalsIgnoreCase("") ? "0.00" : loyalty_amt.getText().toString());
//                    Walletamt = Float.parseFloat(wallet_amt.getText().toString().equalsIgnoreCase("") ? "0.00" : wallet_amt.getText().toString());
//
//                    Totalamt = Couponamt + cashamt + Lyltyamt + Walletamt + Creditamt;
//                    Balance = billamt - Totalamt;
//                    if (Balance > 0) {
//
//                        CardDB edcDB = new CardDB(getApplicationContext());
//                        edcDB.open();
//                        ArrayList<CardDetails> edcDetails = edcDB.getEDCdetails();
//                        edcDB.close();
//                        if (edcDetails.size() > 0) {
//                            if (edcDetails.get(0).getEdcID().toString().equalsIgnoreCase("2")) {
//
//                                idialog = ProgressDialog.show(MarketPayment.this, "",
//                                        "Receiving Payment...", true);
//
//                                total = Balance;
//                                String mm = "12";
//                                String yy = "16";
//                                String mmm, dd, min, hh, ss, dateTimeFormat;
//                                String isMaunualEntry = "false";
//                                hh = date_time.substring(0, 2);
//                                min = date_time.substring(2, 4);
//                                ss = date_time.substring(4, 6);
//                                dd = date_time.substring(6, 8);
//                                mmm = date_time.substring(8, 10);
//                                Calendar c = Calendar.getInstance();
//                                int year = c.get(Calendar.YEAR);
//                                dateTimeFormat = String.valueOf(year) + "-" + mmm + "-" + dd + "T" + hh + ":" + min + ":"
//                                        + ss + "0.Z";
//
//                                NumberFormat formatter = new DecimalFormat("#0.00");
//                                String temp = String.valueOf(formatter.format(total));
//                                temp = temp.replace(".", "");
//
//                                inputXmlData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <purchase-request><Transaction ID=\"Aug1015\">"
//                                        + "<Card><IsManualEntry>" + isMaunualEntry + "</IsManualEntry><CardNumber>" + card_no
//                                        + "</CardNumber><ExpirationDate><MM>" + mm + "</MM><YY>" + yy
//                                        + "</YY></ExpirationDate></Card><Amount><BaseAmount>" + temp
//                                        + "</BaseAmount><discount>00</discount> " + "<Amount>" + temp
//                                        + "</Amount><CurrencyCode>INR</CurrencyCode></Amount><POS><ReferenceNumber>"
//                                        + invoice_no + "</ReferenceNumber>" + "<TransactionTime>" + dateTimeFormat
//                                        + "</TransactionTime><User><ID>NA</ID><Name>NA</Name></User>"
//                                        + "<TrackingNumber>0000</TrackingNumber></POS></Transaction> </purchase-request>";
//                                childThread = new Thread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        try {
//                                            Log.d(TAG, inputXmlData);
//                                            responseResult = Wrapper.innovEFT(context, txn_type, txn_mode, inputXmlData,
//                                                    inno_serialComm_process_edc);
//                                            idialog.dismiss();
//                                            if (responseResult != null) {
//                                                messageHandler.sendEmptyMessage(DISMISS_WINDOW);
//                                            }
//                                        } catch (ParserConfigurationException e1) {
//                                            idialog.dismiss();
//                                            e1.printStackTrace();
//                                        } catch (SAXException e1) {
//                                            idialog.dismiss();
//                                            e1.printStackTrace();
//                                        } catch (IOException e1) {
//                                            idialog.dismiss();
//                                            e1.printStackTrace();
//                                        } catch (InterruptedException e1) {
//                                            idialog.dismiss();
//                                            e1.printStackTrace();
//                                        } catch (Exception e1) {
//                                            idialog.dismiss();
//                                            e1.printStackTrace();
//                                        }
//                                        return;
//                                    }
//                                });
//                                childThread.start();
//
//                            } else if (edcDetails.get(0).getEdcID().toString().equalsIgnoreCase("1")) {
//
//                                doInitializeEzeTap();
//                                doPrepareDeviceEzeTap();
//
//                                try {
//                                    total = Balance;
//                                    JSONObject jsonRequest = new JSONObject();
//                                    JSONObject jsonOptionalParams = new JSONObject();
//                                    JSONObject jsonReferences = new JSONObject();
//                                    JSONObject jsonCustomer = new JSONObject();
//                                    jsonRequest.put("amount", Balance);
//                                    jsonCustomer.put("name", "");
//                                    jsonCustomer.put("mobileNo", "");
//                                    jsonCustomer.put("email", "");
//                                    jsonOptionalParams.put("customer", jsonCustomer);
//                                    jsonReferences.put("reference1", bill_no);
//                                    jsonReferences.put("reference2", tillNo);
//                                    jsonReferences.put("reference3", Location);
//                                    JSONArray array = new JSONArray();
//                                    array.put("addRef_xx1");
//                                    array.put("addRef_xx2");
//                                    jsonReferences.put("additionalReferences", array);
//                                    jsonOptionalParams.put("references", jsonReferences);
//                                    jsonRequest.put("options", jsonOptionalParams);
//                                    jsonRequest.put("mode", "SALE");//Card payment Mode(mandatory)
//                                    doSaleTxn(jsonRequest);
//                                    //doCloseEzetap();
//
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        } else {
//                            showFailedAlert("EDC not configured");
//                        }
//
//
//                    } else {
//                        Toast toast = Toast.makeText(MarketPayment.this, "Balance Amount is zero.", Toast.LENGTH_SHORT);
//                        toast.show();
//
//                    }
//                } else {
//                    AlertManager alert = new AlertManager();
//                    alert.alert(MarketPayment.this, "No Internet Connection",
//                            "Please check " +
//                                    "your data connection or Wifi is ON !");
//
//                }
//            }
//        });
//

//        otp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.i(TAG, "Applying Loyalty");
//                ConnectionDetector net = new ConnectionDetector(getApplicationContext());
//                if (net.isConnectingToInternet()) {
//                    View v = MarketPayment.this.getCurrentFocus();
//                    if (v != null) {
//                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                    }
//                    JSONArray redeemarray = new JSONArray();
//                    if (!loyalty_point.getText().toString().isEmpty()) {
//                        max_amount = 0;
//                        update_amount();
//                        idialog = ProgressDialog.show(MarketPayment.this, "", "Applying Loyalty...", true);
//                        handler = new Handler();
//                        handler.postDelayed(myRunnable = new Runnable() {
//                            @Override
//                            public void run() {
//
//                                idialog.dismiss();
//                            }
//                        }, 30000);
//
//
//                        bal_amount = Float.parseFloat(Bal.getText().toString());
//                        redeem_point = Float.parseFloat(loyalty_point.getText().toString());
//                        try {
//
//                            LoyaltyCredentialsDB loydb = new LoyaltyCredentialsDB(getApplicationContext());
//                            loydb.open();
//                            ArrayList<LoyaltyCredentials> loyaltyCredentials = loydb.getLoyaltyCredentialsDetails();
//                            loydb.close();
//
//                            JSONObject Headerobj = new JSONObject();
//                            Headerobj.put("AccountId", URLDecoder.decode(loyaltyCredentials.get(0).getAccountID()));
//                            Headerobj.put("StoreId", loyaltyCredentials.get(0).getStoreID());
//                            Headerobj.put("StoreOutletId", loyaltyCredentials.get(0).getStoreOutletID());
//                            Headerobj.put("CustomerId", loyaltyCredentials.get(0).getCustomerID());
//                            redeemarray.put(0, Headerobj);
//
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//
//                        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
//                        String URL = "http://api.directdialogs.com/api/LoyaltyRedemption/RedemptionRule";
//
//                        final String mRequestBody = redeemarray.toString().substring(1, redeemarray.toString().length() - 1);
//
//                        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
//                            @Override
//                            public void onResponse(final String response) {
//
//                                try {
//
//                                    JSONObject Jsonobj = new JSONObject(response);
//                                    Log.i(TAG, response.toString());
//                                    //NumberFormat formatter = new DecimalFormat("#0");
//                                    if (Jsonobj.length() > 0) {
//                                        redemption_point = Integer.parseInt(Jsonobj.getString("RedemptionPoint"));
//                                        max_amount = redeem_point / redemption_point;
//                                        if (max_amount <= bal_amount) {
//                                            idialog.dismiss();
//                                            handler.removeCallbacks(myRunnable);
//                                            ConnectionDetector net = new ConnectionDetector(getApplicationContext());
//                                            if (net.isConnectingToInternet()) {
//                                                new Thread(new Runnable() {
//                                                    public void run() {
//
//                                                        Fetch_Redeem_Points();
//
//                                                    }
//                                                }).start();
//
//                                            } else {
//                                                AlertManager alert = new AlertManager();
//                                                alert.alert(MarketPayment.this, "No Internet Connection",
//                                                        "Please check " +
//                                                                "your data connection or Wifi is ON !");
//                                            }
//
//                                        } else {
//                                            idialog.dismiss();
//                                            handler.removeCallbacks(myRunnable);
//                                            showFailedAlert("Loyalty amount should not be greater than balance bill amount");
//                                            max_amount = 0;
//                                            loyalty_point.setText("");
//                                            update_amount();
//                                        }
//                                    } else {
//                                        idialog.dismiss();
//                                        handler.removeCallbacks(myRunnable);
//                                        showFailedAlert("Loyalty server busy, please click on OK and retry.");
//                                        max_amount = 0;
//                                        loyalty_point.setText("");
//                                        update_amount();
//                                    }
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                                // Log.i("LOG_VOLLEY", response);
//                            }
//                        }, new Response.ErrorListener() {
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
//                                try {
//                                    idialog.dismiss();
//                                    handler.removeCallbacks(myRunnable);
//                                    if (error instanceof TimeoutError) {
//                                        showFailedAlert("Time out error occurred.Please click on OK and try again");
//                                        Log.e(TAG, "Time out error occurred.");
//                                        //Time out error
//
//                                    } else if (error instanceof NoConnectionError) {
//                                        showFailedAlert("Network error occurred.Please click on OK and try again");
//                                        Log.e(TAG, "Network error occurred.");
//                                        //net work error
//
//                                    } else if (error instanceof AuthFailureError) {
//                                        showFailedAlert("Authentication error occurred.Please click on OK and try again");
//                                        Log.e(TAG, "Authentication error occurred.");
//                                        //error
//
//                                    } else if (error instanceof ServerError) {
//                                        showFailedAlert("Server error occurred.Please click on OK and try again");
//                                        Log.e(TAG, "Server error occurred.");
//                                        //Error
//                                    } else if (error instanceof NetworkError) {
//                                        showFailedAlert("Network error occurred.Please click on OK and try again");
//                                        Log.e(TAG, "Network error occurred.");
//                                        //Error
//
//                                    } else if (error instanceof ParseError) {
//                                        //Error
//                                        showFailedAlert("An error occurred.Please click on OK and try again");
//                                        Log.e(TAG, "An error occurred.");
//                                    } else {
//
//                                        showFailedAlert("An error occurred.Please click on OK and try again");
//                                        Log.e(TAG, "An error occurred.");
//                                        //Error
//                                    }
//                                    //End
//
//
//                                } catch (Exception e) {
//
//
//                                }
//                            }
//                        }) {
//                            @Override
//                            public String getBodyContentType() {
//                                return "application/json; charset=utf-8";
//                            }
//
//                            @Override
//                            public byte[] getBody() throws AuthFailureError {
//                                try {
//                                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
//                                } catch (UnsupportedEncodingException uee) {
//                                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
//                                    return null;
//                                }
//                            }
//                        };
//
//                        requestQueue.add(stringRequest);
//
//
//                    } else {
//                        showFailedAlert("Please enter loyalty points");
//
//                    }
//                } else {
//                    AlertManager alert = new AlertManager();
//                    alert.alert(MarketPayment.this, "No Internet Connection",
//                            "Please check " +
//                                    "your data connection or Wifi is ON !");
//
//                }
//
//            }
//
//
//        });
//        paymnt_cash.addTextChangedListener(new TextWatcher() {
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            public void onTextChanged(CharSequence s, int start, int before,
//                                      int count) {
//                if (!s.equals("")) {
//                    update_amount();
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//                if (s.equals("")) {
//                    paymnt_cash.setText("0.00");
//                }
//
//
//            }
//
//        });
//
//        payment_card.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                update_amount();
//            }
//
//        });
//
//        loyalty_amt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//
//                update_amount();
//
//            }
//
//        });
//        wallet_amt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                update_amount();
//            }
//
//        });

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String couponcode= coupon_code.getText().toString();
                if(couponcode.length()>0){

                    View v = MarketPayment.this.getCurrentFocus();
                    if (v != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    idialog = ProgressDialog.show(MarketPayment.this, "",
                            "Processing Payment...", true);
                    handler = new Handler();
                    handler.postDelayed(myRunnable = new Runnable() {
                        @Override
                        public void run() {

                            idialog.dismiss();
                        }
                    }, 30000);
                    JSONObject finaljsonobj = new JSONObject();

                    JSONObject headerjsonobj = new JSONObject();
                    JSONArray headerjsonarray = new JSONArray();


                    JSONArray detailjsonarray = new JSONArray();

                    JSONObject couponjsonobj = new JSONObject();
                    JSONArray couponjsonarray = new JSONArray();

                    JSONObject PBcouponjsonobj = new JSONObject();
                    JSONArray PBcouponjsonarray = new JSONArray();


                    JSONObject walletjsonobj = new JSONObject();
                    JSONArray walletjsonarray = new JSONArray();

                    Integer count = 0;

                    try {

                        headerjsonobj.put("billno", bill_no);
                        headerjsonobj.put("cash_amount_taken", 0);
                        headerjsonobj.put("cash_amount_returned", 0);
                        headerjsonobj.put("other_amount", billamt);
                        headerjsonobj.put("total_payment_value", billamt);
                        headerjsonobj.put("location", Location);
                        headerjsonarray.put(0, headerjsonobj);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }





                    try {
                        JSONObject detailjsonobj = new JSONObject();
                        detailjsonobj.put("payment_mode_id", "3");
                        detailjsonobj.put("cc_auth_code", "");
                        detailjsonobj.put("card_code", "");
                        detailjsonobj.put("cc_edc_no", "");
                        detailjsonobj.put("payment_value", billamt);
                        detailjsonobj.put("cust_card_code", "");
                        detailjsonobj.put("customer_name", "");
                        detailjsonarray.put(count, detailjsonobj);
                        count = count + 1;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }








                    try {
                        String DenomValue, DenomCount;
                        Integer Count;
                        Float Value;
                        couponjsonobj.put("coupon_code", "118");

                        couponjsonobj.put("coupon_sl_no", couponcode);
                        if (bbEligibility.equalsIgnoreCase("Y")) {
                            DenomCount = bbQty.getText().toString().equalsIgnoreCase("0") ? "1" : bbQty.getText().toString();
                            Count = Integer.parseInt(DenomCount);
                            DenomValue = coupon_amt.getText().toString().equalsIgnoreCase("0.00") ? "0" : coupon_amt.getText().toString();
                            Value = Float.parseFloat(DenomValue);
                            couponjsonobj.put("denom_count", Count.toString());
                            couponjsonobj.put("denom_value", billamt);

                        } else {
                            couponjsonobj.put("denom_count", "");
                            couponjsonobj.put("denom_value",billamt);
                        }
                        couponjsonobj.put("lineNo", "0");
                        couponjsonobj.put("location_code", Location);
                        couponjsonobj.put("customer_phone", cust_mobile);
                        couponjsonarray.put(0, couponjsonobj);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



                    try {

                        finaljsonobj.put("header", headerjsonarray);
                        finaljsonobj.put("detail", detailjsonarray);
                        finaljsonobj.put("coupon", couponjsonarray);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
                    String URL = "http://10.46.16.18/selfcheck_mkt/api/mposmarketplace/billpayment_mkt";
                    final String mRequestBody = finaljsonobj.toString();
                    ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                    if (net.isConnectingToInternet()) {
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {

                                try {
                                    idialog.dismiss();
                                    handler.removeCallbacks(myRunnable);
                                    JSONObject Jsonobj = new JSONObject(response);
                                    String Status = Jsonobj.getString("Status");
                                    String Msg = Jsonobj.getString("Message");
                                    if (Status.equalsIgnoreCase("Success")) {

                                        pay.setEnabled(false);
                                        cancel_bill.setEnabled(false);
                                       // apply.setEnabled(false);

                                        //    otp.setEnabled(false);
                                        bill_staus = true;

                                        Toast toast = Toast.makeText(getApplicationContext(), Msg, Toast.LENGTH_SHORT);
                                        toast.show();

//                                        if (!isOrderID.equalsIgnoreCase("")) {
//                                            String Url = PATH + "AssistedOrderBillStatus?Orderid=" + isOrderID + "&billno=" + bill_no;
//                                            makeUpdateOrderIDApiCall(Url);
//                                        }
                                        Send_ebill();

//                                        Intent intent = new Intent(MarketPayment.this, WebViewEbill.class);
//                                        startActivity(intent);
//                                        finish();

                                    } else {
                                        idialog.dismiss();
                                        handler.removeCallbacks(myRunnable);
                                        Toast toast = Toast.makeText(getApplicationContext(), Msg, Toast.LENGTH_SHORT);
                                        toast.show();
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                try {
                                    idialog.dismiss();
                                    handler.removeCallbacks(myRunnable);
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

                        requestQueue.add(stringRequest);
                    } else {
                        idialog.dismiss();
                        handler.removeCallbacks(myRunnable);
                        AlertManager alert = new AlertManager();
                        alert.alert(MarketPayment.this, "No Internet Connection",
                                "Please check " +
                                        "your data connection or Wifi is ON !");

                    }

                }else {
                    Toast.makeText(MarketPayment.this,"Please enter Coupon Code",Toast.LENGTH_SHORT).show();
                }




            }

        });

        cancel_bill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!iswalletApplied) {


                    new AlertDialog.Builder(MarketPayment.this)
                            .setMessage("Are you sure cancel bill?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    new_bill.setEnabled(true);
                                    cancel_bill.setEnabled(false);
                                    pay.setEnabled(false);
                                    ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                                    if (net.isConnectingToInternet()) {
                                        idialog = ProgressDialog.show(MarketPayment.this, "",
                                                "Cancelling Bill...", false);
                                        RequestQueue queue = AppController.getInstance().getRequestQueue();
                                        final String url = "http://10.46.16.18/selfcheck_mkt/api/mposmarketplace/cancelbill_mkt?billNo=" + bill_no ;
                                        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                                                new Response.Listener<JSONObject>() {
                                                    @Override
                                                    public void onResponse(JSONObject response) {
                                                        // display response
                                                        try {
                                                            String Msg = response.getString("Message");
                                                            Toast.makeText(MarketPayment.this, Msg,
                                                                    Toast.LENGTH_SHORT).show();

                                                            deletetables();
                                                            idialog.dismiss();
                                                            editor.clear();
                                                            editor.commit();
                                                            Intent intent = new Intent(MarketPayment.this, OrderInfromation.class);
                                                            startActivity(intent);
                                                            finish();
                                                            Log.d("Response", response.toString());
                                                        } catch (JSONException e) {

                                                            e.printStackTrace();
                                                        }


                                                    }
                                                },
                                                new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError error) {

                                                        try {
                                                            idialog.dismiss();
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
                                                }
                                        )


                                        {
                                            @Override
                                            public String getBodyContentType() {
                                                return "application/json; charset=utf-8";
                                            }

                                        };


                                        queue.add(getRequest);


                                    } else {
                                        new_bill.setEnabled(false);
                                        cancel_bill.setEnabled(true);
                                        pay.setEnabled(true);
                                        AlertManager alert = new AlertManager();
                                        alert.alert(MarketPayment.this, "No Internet Connection",
                                                "Please check " +
                                                        "your data connection or Wifi is ON !");

                                    }
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();

                } else
                    showFailedAlert("Can't cancel bill as wallet is applied");
            }

        });

    }

    public void LoadWalletScreen(View v) {
        Float bal = Float.parseFloat(Bal.getText().toString());
        if (bal > 0) {
            editor.putString("Lytpoint", loyalty_point.getText().toString());
            editor.putString("Lytotp", redeem_code.getText().toString());
            editor.putString("Lytamt", loyalty_amt.getText().toString());

            editor.putString("Cpncode", coupon_code.getText().toString());
            editor.putString("Cpnamt", coupon_amt.getText().toString());
            editor.putString("Balamt", String.valueOf(bal));
            editor.commit();
            Intent intent = new Intent(MarketPayment.this, WalletActivity.class);
            startActivity(intent);
            finish();
        } else
            showFailedAlert("Balance amount is less than or equal to zero");
    }

    private void makeUpdateOrderIDApiCall(String urlPath) {
        ApiCall.make(this, urlPath, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            String status = responseObject.getString("statusCode");
                            if (status.equalsIgnoreCase("200")) {


                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {


                    }
                }
        );

    }

    public void deletetables() {
        CustomerDB cusDB = new CustomerDB(getApplicationContext());
        cusDB.open();
        cusDB.deleteCustomerTable();
        cusDB.close();

        LoyaltyDetailsDB offrDB = new LoyaltyDetailsDB(getApplicationContext());
        offrDB.open();
        offrDB.deleteLoyaltyTable();
        offrDB.close();

        LoyaltyCredentialsDB lyltyDB = new LoyaltyCredentialsDB(getApplicationContext());
        lyltyDB.open();
        lyltyDB.deleteLoyaltyCredentialsDetailsTable();
        lyltyDB.close();

        ProductDetailsDB prdctDB = new ProductDetailsDB(getApplicationContext());
        prdctDB.open();
        prdctDB.deleteUserTable();
        prdctDB.close();

        WalletDB walletDB = new WalletDB(getApplicationContext());
        walletDB.open();
        walletDB.deleteWalletTable();
        walletDB.close();

    }

    public void Fetch_Redeem_Points() {

        try {
            Log.i(TAG, "Getting Glow rewards points");
            JSONArray redeemarray = new JSONArray();
            LoyaltyCredentialsDB loydb = new LoyaltyCredentialsDB(getApplicationContext());
            loydb.open();
            ArrayList<LoyaltyCredentials> loyaltyCredentials = loydb.getLoyaltyCredentialsDetails();
            loydb.close();

            JSONObject Headerobj = new JSONObject();
            Headerobj.put("AccountId", URLDecoder.decode(loyaltyCredentials.get(0).getAccountID()));
            Headerobj.put("StoreId", loyaltyCredentials.get(0).getStoreID());
            Headerobj.put("StoreOutletId", loyaltyCredentials.get(0).getStoreOutletID());
            Headerobj.put("CustomerId", loyaltyCredentials.get(0).getCustomerID());
            Headerobj.put("PointsForRedemption", loyalty_point.getText().toString());
            Headerobj.put("ChannelType", "1");
            Headerobj.put("Token", bill_no);

            redeemarray.put(0, Headerobj);

            RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
            String URL = "http://api.directdialogs.com/api/point/Redeem";
            final String mRequestBody = redeemarray.toString().substring(1, redeemarray.toString().length() - 1);
            ConnectionDetector net = new ConnectionDetector(getApplicationContext());
            if (net.isConnectingToInternet()) {
                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {

                        try {

                            otp_code = response;
                            loyalty_amt.setText(String.valueOf(max_amount));
                            loyalty_point.setText("");
                            redeem_code.requestFocus();
                            update_amount();


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        try {
                            idialog.dismiss();
                            max_amount = 0;
                            loyalty_point.setText("");
                            update_amount();
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
                }
                ) {
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

                requestQueue.add(stringRequest);
            } else {
                idialog.dismiss();
                AlertManager alert = new AlertManager();
                alert.alert(MarketPayment.this, "No Internet Connection",
                        "Please check " +
                                "your data connection or Wifi is ON !");

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void update_amount() {

        loyalty_amt.setText(String.valueOf(max_amount));
        Couponamt = Float.parseFloat(coupon_amt.getText().toString().equalsIgnoreCase("") ? "0.00" : coupon_amt.getText().toString());
        Creditamt = Float.parseFloat(payment_card.getText().toString().equalsIgnoreCase("") ? "0.00" : payment_card.getText().toString());
        Lyltyamt = Float.parseFloat(loyalty_amt.getText().toString().equalsIgnoreCase("") ? "0.00" : loyalty_amt.getText().toString());
        Walletamt = Float.parseFloat(wallet_amt.getText().toString().equalsIgnoreCase("") ? "0.00" : wallet_amt.getText().toString());
        Totalamt = Couponamt + cashamt + Creditamt + Lyltyamt + Walletamt;
        Balance = billamt - Totalamt;
        Bal.setText(Balance.toString());
    }

    public void SaveTransactionDetails(String billNo, String resCode, String resMsg, String transDate, String transTime, String cardNO, String tID, String invNO, String RRN, String ApprovalCode) {

        try {
            Log.i(TAG, "Saving Card Transaction Details");
            HashMap<String, String> TransactionDetails = new HashMap<String, String>();
            TransactionDetails.put("billNo", billNo);
            TransactionDetails.put("responseCode", resCode);
            TransactionDetails.put("responseMessage", resMsg);
            TransactionDetails.put("tranDate", transDate);
            TransactionDetails.put("transTime", transTime);
            TransactionDetails.put("cardNO", cardNO);
            TransactionDetails.put("tID", tID);
            TransactionDetails.put("invNO", invNO);
            TransactionDetails.put("rrn", RRN);
            TransactionDetails.put("approvalCode", ApprovalCode);

            TransactionDB trdb = new TransactionDB(getApplicationContext());
            trdb.open();
            trdb.insertTransactionDetails(TransactionDetails);
            trdb.close();
            Log.i(TAG, "Transaction details saved successfully in mobile");

            ConnectionDetector net = new ConnectionDetector(getApplicationContext());
            if (net.isConnectingToInternet()) {

                JSONObject finaljsonobj = new JSONObject();
                JSONArray Headerjsonarray = new JSONArray();


                try {

                    JSONObject headerjsonobj = new JSONObject();
                    headerjsonobj.put("bill_no", billNo);
                    headerjsonobj.put("Location", Location);
                    headerjsonobj.put("response_code", resCode);
                    headerjsonobj.put("response_message", resMsg);
                    headerjsonobj.put("trans_date", transDate);
                    headerjsonobj.put("trans_time", transTime);
                    headerjsonobj.put("card_no", cardNO);
                    headerjsonobj.put("trans_id", tID);
                    headerjsonobj.put("invoice_no", invNO);
                    headerjsonobj.put("rrn", RRN);
                    headerjsonobj.put("approval_code", ApprovalCode);
                    Headerjsonarray.put(0, headerjsonobj);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    finaljsonobj.put("header", Headerjsonarray);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "Calling Save Transaction API");
                RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
                String URL = PATH + "customerCardTrans/";
                final String mRequestBody = finaljsonobj.toString();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        String body;
                        if (error.networkResponse.data != null) {
                            try {

                                body = new String(error.networkResponse.data, "UTF-8");
                                showFailedAlert(body);
                                Log.e(TAG, body);


                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
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
                requestQueue.add(stringRequest);
            }

            /*else {
                Log.e(TAG,"No Internet");
                AlertManager alert = new AlertManager();
                alert.alert(MarketPayment.this, "No Internet Connection",
                        "Please check " +
                                "your data connection or Wifi is ON !");
            }

            */
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Toast.makeText(getApplicationContext(), "Data saved successfully", Toast.LENGTH_SHORT).show();
        //Toast toast = Toast.makeText(MarketPayment.this, "Transaction details saved successfully", Toast.LENGTH_SHORT);
        // toast.show();
    }


    public void showFailedAlert(final String msg) {

        MarketPayment.this.runOnUiThread(new Runnable() {
            public void run() {
                Log.e(TAG, msg);
                AlertDialog.Builder builder = new AlertDialog.Builder(MarketPayment.this);
                builder.setTitle("HnG POS");
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


    public void goBack(){
        if (!bill_staus) {
            if (!iswalletApplied) {
                doCloseEzetap();
                Intent i = new Intent(MarketPayment.this, MarketOrderProcess.class);
                startActivity(i);
                finish();
            } else
                showFailedAlert("Can't go back as wallet is applied");
        } else {
            showFailedAlert("Not Allowed");
        }

    }
    //here we maintain our products in various departments


    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(MarketPayment.this)
                .setMessage("Do you want to go back?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        goBack();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        })
                .show();
    }



    public void Send_ebill() {

        idialog = ProgressDialog.show(MarketPayment.this, "",
                "Sending E-Bill...", true);
        Log.i(TAG, "Sending E-bill");
        RequestQueue queue = AppController.getInstance().getRequestQueue();
        final String url = "http://10.46.16.18/selfcheck_mkt/api/mposmarketplace/ebillcopy_mkt?billNo="+bill_no;

        Log.e("ebillCopy",url);

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        try {
                            String Status = response.getString("statusCode");
                            if (Status.equalsIgnoreCase("200")) {
                                idialog.dismiss();
                                Toast toast = Toast.makeText(getApplicationContext(), "E-Bill send Successfully", Toast.LENGTH_SHORT);
                                toast.show();
                                editor = sp.edit();
                                editor.putString("bill_no", bill_no);
                                editor.putString("Ebill_URL", response.getString("billurl"));
                                editor.apply();
                                if(PrinterActivity.ISCONNECT){
                                    Intent intent = new Intent(MarketPayment.this, WebViewEbill.class);
                                    startActivity(intent);
                                }else {
                                    Intent intent = new Intent(MarketPayment.this, WebViewEbill_XPrinter.class);
                                    startActivity(intent);
                                }

//                                Intent intent = new Intent(MarketPayment.this, ProductServiceDetailsActivity.class);
//                                startActivity(intent);
                                finish();
                                Log.i(TAG, "E-bill send");
                            } else {
                                idialog.dismiss();
                                Toast toast = Toast.makeText(getApplicationContext(), "E-Bill not sent.", Toast.LENGTH_SHORT);
                                toast.show();
                                editor = sp.edit();
                                editor.putString("bill_no", bill_no);
                                editor.putString("Ebill_URL", "");
                                editor.apply();
                                if(PrinterActivity.ISCONNECT){
                                    Intent intent = new Intent(MarketPayment.this, WebViewEbill_XPrinter.class);
                                    startActivity(intent);
                                }else {
                                    Intent intent = new Intent(MarketPayment.this, WebViewEbill.class);
                                    startActivity(intent);
                                }
                                finish();
                                Log.e(TAG, "E-bill not sent");
                                //showFailedAlert("E-Bill not send.");
                            }
                        } catch (JSONException e) {
                            idialog.dismiss();
                            showFailedAlert("E-Bill not sent.");

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        idialog.dismiss();
                        Toast toast = Toast.makeText(getApplicationContext(), "E-Bill not sent.", Toast.LENGTH_SHORT);
                        toast.show();
                        editor = sp.edit();
                        editor.putString("bill_no", bill_no);
                        editor.putString("Ebill_URL", "");
                        editor.apply();
                        if(PrinterActivity.ISCONNECT){
                            Intent intent = new Intent(MarketPayment.this, WebViewEbill_XPrinter.class);
                            startActivity(intent);
                        }else {
                            Intent intent = new Intent(MarketPayment.this, WebViewEbill.class);
                            startActivity(intent);
                        }
                        finish();
                        Log.e(TAG, "E-bill not sent");
                    }
                }
        );

        queue.add(getRequest);

    }

    @Override
    public void generateQRCode(Bitmap bitmap, String s) {

    }

    @Override
    public void onClick(View view) {

    }

    public class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            //SpnrCard = parent.getItemAtPosition(pos).toString();

        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {

        }

    }

    private void doInitializeEzeTap() {

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("demoAppKey", "626169f2-44fe-46a2-ab43-1a2a2b029db0");
            jsonRequest.put("prodAppKey", "52692f5a-8367-497b-9500-1a6a90911218");
            jsonRequest.put("merchantName", "HEALTH_AND_GLOW");
            jsonRequest.put("userName", "8105277766");
            jsonRequest.put("currencyCode", "INR");
            jsonRequest.put("appMode", "PROD");
            jsonRequest.put("captureSignature", "true");
            jsonRequest.put("prepareDevice", "true");
            EzeAPI.initialize(this, REQUEST_CODE_INITIALIZE, jsonRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void doPrepareDeviceEzeTap() {
        EzeAPI.prepareDevice(this, REQUEST_CODE_PREPARE);
    }

    private void doSaleTxn(JSONObject jsonRequest) {
        EzeAPI.cardTransaction(this, REQUEST_CODE_SALE_TXN, jsonRequest);
    }

    private void doCloseEzetap() {
        EzeAPI.close(this, REQUEST_CODE_CLOSE);
    }

    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            if (intent != null && intent.hasExtra("response")) {
                //Toast.makeText(this, intent.getStringExtra("response"), Toast.LENGTH_LONG).show();
                //android.util.Log.d("SampleAppLogs", intent.getStringExtra("response"));
            }
            if (resultCode == RESULT_OK) {
                JSONObject response = new JSONObject(intent.getStringExtra("response"));
                JSONObject Resresponse = new JSONObject(response.getJSONObject("result").toString());
                JSONObject txnResponse = new JSONObject(Resresponse.getJSONObject("txn").toString());
                tID = txnResponse.getString("tid");
                RRN = txnResponse.getString("rrNumber");
                invNO = txnResponse.getString("invoiceNumber");
                ApprovalCode = txnResponse.getString("authCode");
                JSONObject cardResponse = new JSONObject(Resresponse.getJSONObject("cardDetails").toString());
                cardNO = cardResponse.getString("maskedCardNo");
                JSONObject recResponse = new JSONObject(Resresponse.getJSONObject("receipt").toString());
                String transDateTime = recResponse.getString("receiptDate");
                transDate = transDateTime.substring(0, 10);
                transTime = transDateTime.substring(11, 19);
                NumberFormat formatter = new DecimalFormat("#0.00");
                String temp = String.valueOf(formatter.format(total));
                payment_card.setText(temp);
                Couponamt = Float.parseFloat(coupon_amt.getText().toString().equalsIgnoreCase("") ? "0.00" : coupon_amt.getText().toString());
                Creditamt = Float.parseFloat(payment_card.getText().toString().equalsIgnoreCase("") ? "0.00" : payment_card.getText().toString());
                Lyltyamt = Float.parseFloat(loyalty_amt.getText().toString().equalsIgnoreCase("") ? "0.00" : loyalty_amt.getText().toString());
                Walletamt = Float.parseFloat(wallet_amt.getText().toString().equalsIgnoreCase("") ? "0.00" : wallet_amt.getText().toString());
                Totalamt = Couponamt + cashamt + Creditamt + Lyltyamt + Walletamt;
                Balance = billamt - Totalamt;
                Bal.setText(Balance.toString());
                pay.performClick();


            } else if (resultCode == RESULT_CANCELED) {
                JSONObject response = new JSONObject(intent.getStringExtra("response"));
                response = response.getJSONObject("error");
                String errorCode = response.getString("code");
                String errorMessage = response.getString("message");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}