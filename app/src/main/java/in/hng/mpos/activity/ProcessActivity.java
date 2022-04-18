package in.hng.mpos.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import in.hng.mpos.Database.CustomerDB;
import in.hng.mpos.Database.LoyaltyCredentialsDB;
import in.hng.mpos.Database.LoyaltyDetailsDB;
import in.hng.mpos.Database.ProductDetailsDB;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.Database.WalletDB;
import in.hng.mpos.Adapter.ListAdapter;
import in.hng.mpos.R;
import in.hng.mpos.Utils.Constants;
import in.hng.mpos.gettersetter.CustomerDetails;
import in.hng.mpos.gettersetter.ProductList;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.helper.AlertManager;
import in.hng.mpos.helper.AppController;
import in.hng.mpos.helper.ConnectionDetector;
import in.hng.mpos.helper.Log;

/**
 * Created by Cbly on 06-Mar-18.
 */

public class ProcessActivity extends AppCompatActivity {
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    ArrayList<ProductList> skuList;
    String Bill_no, Location_code, Cashier_code, Total_Amt, Disc_Amt, Loyalty_Disc, redeem_code, PATH;
    ListAdapter adapter;
    ListView listView;
    TextView bill_no, date, location_code, cashier_id, total_amt, disc_amt, loyal_disc, total_qty, sku_count;
    Button pay_bill, cancel_bill, new_bill;
    int totalQty = 0;
    private static final String TAG = "Process";
    ProgressDialog idialog = null;
    String isPBcoupon;
    LinearLayout pbLayout;
    Button cpnApply;
    EditText cpnCode;
    String pbCouponDiscount;
    TextView txtcpnDisc;
    String CouponCode;
    String PromoText;
    private Handler handler;
    Runnable myRunnable = null;

    String fromActivity = "";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.process_bill);

        Log.i(TAG, "Process Screen Loaded");

        // Set up your ActionBar
        final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.action_bar_simple, null);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarLayout);
        actionBar.setTitle("");
        final int actionBarColor = getResources().getColor(R.color.ActionBarColor);
        actionBar.setBackgroundDrawable(new ColorDrawable(actionBarColor));
        TextView title = findViewById(R.id.ab_activity_title);
        title.setText("h&g mPOS - Billing");
        title.setVisibility(View.VISIBLE);
        //Resources res = getResources();
        //PATH = res.getString(R.string.API_URL);
        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH = urlDB.getUrlDetails();
        urlDB.close();
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        Bill_no = sp.getString("Bill_no", "");
        Location_code = sp.getString("Location_code", "");
        Cashier_code = sp.getString("Cashier_code", "");
        Total_Amt = sp.getString("Total_Amt", "");
        Disc_Amt = sp.getString("Disc_Amt", "");
        Loyalty_Disc = sp.getString("Loyalty_Disc", "");
        pbCouponDiscount = sp.getString("Coupon_Disc", "").equalsIgnoreCase("") ? "0.00" : sp.getString("Coupon_Disc", "");
        CouponCode = sp.getString("Coupon_Code", "");
        PromoText = sp.getString("Promo_Txt", "");

        String date_now = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        skuList = readFinalData();
        listView = findViewById(R.id.listv);
        bill_no = findViewById(R.id.txtbillNo);
        date = findViewById(R.id.txtdate);
        location_code = findViewById(R.id.txtlocation);
        cashier_id = findViewById(R.id.txtcashierID);
        total_amt = findViewById(R.id.txttotalAmt);
        disc_amt = findViewById(R.id.txtdiscamt);
        loyal_disc = findViewById(R.id.txtloyaldisc);
        total_qty = findViewById(R.id.txttotalQty);
        sku_count = findViewById(R.id.txtSkuCount);
        pay_bill = findViewById(R.id.paybill);
        cancel_bill = findViewById(R.id.BillCancel);
        new_bill = findViewById(R.id.BillNew);
        pbLayout = findViewById(R.id.pbLayout);
        cpnApply = findViewById(R.id.Applycoupn);
        cpnCode = findViewById(R.id.txtPBcoupncode);
        txtcpnDisc = findViewById(R.id.txtCoupondisc);

        Bundle bundle = getIntent().getExtras();
        Intent intent = getIntent();
        if (bundle != null) {
            if (intent.hasExtra(Constants.FROM))
                fromActivity = bundle.getString(Constants.FROM);
        }
        Log.w(TAG, "From Activity " + fromActivity);

          /*
        Validation to disable coupon code during manual billing
         */
        Log.w(TAG, "Process Activity Di sable coupon#### " + fromActivity);
        if (!fromActivity.isEmpty()) {
            cpnCode.setEnabled(false);
            cpnApply.setEnabled(false);
            cpnApply.setBackgroundColor(Color.parseColor("#B6B6B6"));
        } else {
            cpnCode.setEnabled(true);
            cpnApply.setEnabled(true);
            cpnApply.setBackgroundColor(Color.parseColor("#FA8C01"));
        }

        adapter = new ListAdapter(ProcessActivity.this, skuList);
        listView.setAdapter(adapter);
        bill_no.setText(Bill_no);
        date.setText(date_now);
        location_code.setText(Location_code);
        cashier_id.setText(Cashier_code);
        disc_amt.setText(Disc_Amt);
        loyal_disc.setText(Loyalty_Disc);
        total_amt.setText(Total_Amt);
        txtcpnDisc.setText(pbCouponDiscount);

        if (Float.valueOf(pbCouponDiscount) > 0) {
            cpnApply.setEnabled(false);
            cpnCode.setEnabled(false);
            cpnCode.setText(CouponCode);
        }


        for (int i = 0; i < skuList.size(); i++) {
            totalQty = totalQty + Integer.parseInt(skuList.get(i).getQty());
        }

        total_qty.setText(String.valueOf(totalQty));
        sku_count.setText(String.valueOf(skuList.size()));

        UserDB userDB = new UserDB(getApplicationContext());
        userDB.open();
        ArrayList<UserDetails> userDetails = userDB.getUserDetails();
        userDB.close();
        if (userDetails.size() > 0) {
            isPBcoupon = userDetails.get(0).getIsPB();
            if (isPBcoupon.equalsIgnoreCase("Y"))
                pbLayout.setVisibility(View.VISIBLE);
            else
                txtcpnDisc.setText("0.00");
        }

        if (!PromoText.equalsIgnoreCase(""))
            showFailedAlert(PromoText);

        cpnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Applying Coupon");
                View v = ProcessActivity.this.getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                if (net.isConnectingToInternet()) {


                    CustomerDB custmrdb = new CustomerDB(getApplicationContext());
                    custmrdb.open();
                    ArrayList<CustomerDetails> customerdata = custmrdb.getCustomerDetails();
                    custmrdb.close();

                    if (customerdata.size()==0){
                        showFailedAlert("Mobile no is Mandatory for Coupon Redemtion");
                    }
                    else if (cpnCode.getText().toString().isEmpty()){
                        showFailedAlert("Please enter coupon code");
                    }
                    else if (customerdata.size()>0){
                        if (!customerdata.get(0).getMobileNO().isEmpty()){
                            if (!cpnCode.getText().toString().isEmpty()) {
                                idialog = ProgressDialog.show(ProcessActivity.this, "", "Applying Coupon...", true);
                                handler = new Handler();
                                handler.postDelayed(myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        idialog.dismiss();
                                    }
                                }, 30000);

                                try {
                                    String BBQty;
                                    RequestQueue queue = AppController.getInstance().getRequestQueue();
                                    final String url = PATH + "coupon?billNo=" + Bill_no + "&couponslno=" + cpnCode.getText().toString().trim() + "&location=" + Location_code + "&FreeQuantity=" + "" + "&CashierID=" + Cashier_code + "&couponType=" + isPBcoupon;
                                    Log.i(TAG, "Api Call: " + url);
                                    JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                                            new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    try {
                                                        Log.i(TAG, "Api Call response: " + response.toString());
                                                        if (response.getString("statusCode").equalsIgnoreCase("200")) {
                                                            Log.d("Coupon Response", response.toString());
                                                            txtcpnDisc.setText(response.getString("CouponDiscount"));
                                                            disc_amt.setText(response.getString("Discount"));
                                                            total_amt.setText(response.getString("BillValue"));
                                                            cpnCode.setText(response.getString("couponcode"));
                                                            cpnCode.setEnabled(false);
                                                            cpnApply.setEnabled(false);


                                                            idialog.dismiss();
                                                            handler.removeCallbacks(myRunnable);
                                                            Toast toast = Toast.makeText(getApplicationContext(), "Coupon Applied Successfully", Toast.LENGTH_SHORT);
                                                            toast.show();

                                                        } else {
                                                            idialog.dismiss();
                                                            handler.removeCallbacks(myRunnable);
                                                            cpnCode.setText("");
                                                            txtcpnDisc.setText("0.00");
                                                            String msg = response.getString("Message");
                                                            showFailedAlert(msg);
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {


                                                    try {
                                                        idialog.dismiss();
                                                        handler.removeCallbacks(myRunnable);
                                                        cpnCode.setText("");
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
                                    };

                                    // add it to the RequestQueue
                                    getRequest.setRetryPolicy(new DefaultRetryPolicy(
                                            6000,
                                            3,
                                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                                    queue.add(getRequest);


                                } catch (Exception e) {
                                    // dialog.dismiss();
                                    showFailedAlert(e.getMessage());
                                    Log.e(TAG, e.getMessage());
                                    System.out.println("Exception : " + e.getMessage());
                                }


                            }
                        }
                    }


                    else {
                        showFailedAlert("Please enter coupon code");

                    }

                } else {
                    AlertManager alert = new AlertManager();
                    alert.alert(ProcessActivity.this, "No Internet Connection",
                            "Please check " +
                                    "your data connection or Wifi is ON !");

                }

            }


        });

        pay_bill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editor.putString("Total_Amt", total_amt.getText().toString());
                editor.putString("Coupon_Disc", txtcpnDisc.getText().toString());
                editor.putString("Disc_Amt", disc_amt.getText().toString());
                editor.putString("Coupon_Code", cpnCode.getText().toString());
                editor.putString("Bill_no", Bill_no);
                editor.putString("isWallet", "N");
                editor.apply();

                Log.w(TAG, "Moving to Payment Activity From " + fromActivity);

                Intent i = new Intent(ProcessActivity.this, PaymentActivity.class);
                if (fromActivity.isEmpty())
                    i.putExtra(Constants.FROM, "");
                else
                    i.putExtra(Constants.FROM, fromActivity);
                startActivity(i);
                finish();

            }
        });

        cancel_bill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                new AlertDialog.Builder(ProcessActivity.this)
                        .setMessage("Are you sure cancel bill?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new_bill.setEnabled(true);
                                cancel_bill.setEnabled(false);
                                pay_bill.setEnabled(false);
                                ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                                if (net.isConnectingToInternet()) {
                                    idialog = ProgressDialog.show(ProcessActivity.this, "",
                                            "Cancelling Bill...", true);
                                    Log.i(TAG, "Cancelling Bill");
                                    try {
                                        RequestQueue queue = AppController.getInstance().getRequestQueue();

                                        final String url = PATH + "cancelbill?billNo=" + Bill_no + "&location=" + Location_code;
                                        Log.i(TAG, "Api Call: " + url);
                                        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                                                new Response.Listener<JSONObject>() {
                                                    @Override
                                                    public void onResponse(JSONObject response) {
                                                        // display response
                                                        Log.i(TAG, "Api Call response: " + response.toString());
                                                        try {
                                                            String Msg = response.getString("Message");
                                                            Toast.makeText(ProcessActivity.this, Msg,
                                                                    Toast.LENGTH_SHORT).show();
                                                            deletetables();
                                                            idialog.dismiss();
                                                            editor.clear();
                                                            editor.commit();
                                                            Intent intent = new Intent(ProcessActivity.this, LoyaltyActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                            //Log.d("Response", response.toString());
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }


                                                    }
                                                },
                                                new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError error) {

                                                        idialog.dismiss();

                                                        try {

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

                                        };
                                        getRequest.setRetryPolicy(new DefaultRetryPolicy(
                                                6000,
                                                3,
                                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


                                        queue.add(getRequest);

                                    } catch (Exception e) {
                                        // dialog.dismiss();
                                        showFailedAlert(e.getMessage());
                                        Log.e(TAG, e.getMessage());
                                        System.out.println("Exception : " + e.getMessage());
                                    }


                                } else {
                                    new_bill.setEnabled(false);
                                    cancel_bill.setEnabled(true);
                                    pay_bill.setEnabled(true);
                                    AlertManager alert = new AlertManager();
                                    alert.alert(ProcessActivity.this, "No Internet Connection",
                                            "Please check " +
                                                    "your data connection or Wifi is ON !");

                                }
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

            }

        });

        new_bill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(ProcessActivity.this)
                        .setMessage("Are you sure to continue with new bill?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog = ProgressDialog.show(ProcessActivity.this, "",
                                        "Saving Details...", false);
                                final DialogInterface finalDialog = dialog;
                                deletetables();
                                finalDialog.dismiss();
                                editor.clear();
                                editor.commit();
                                Intent intent = new Intent(ProcessActivity.this, LoyaltyActivity.class);
                                startActivity(intent);
                                finish();

                            }
                        })
                        .setNegativeButton("No", null)
                        .show();


            }
        });

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

    public void showFailedAlert(final String msg) {

        ProcessActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProcessActivity.this);
                builder.setTitle("HnG mPOS");
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

    public ArrayList<ProductList> readFinalData() {
        ProductDetailsDB podb = new ProductDetailsDB(getApplicationContext());
        podb.open();
        ArrayList<ProductList> skuList = podb.getEANDetails();

        podb.close();

        return skuList;

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
    public void onBackPressed() {

        new AlertDialog.Builder(ProcessActivity.this)
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
        }).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void goBack() {
        editor.putString("Bill_no", Bill_no);
        editor.putString("Coupon_Disc", "0.00");
        editor.putString("Disc_Amt", "0.00");
        editor.putString("Coupon_Code", "");
        editor.commit();

        if (fromActivity.isEmpty()) {
            Intent i = new Intent(ProcessActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        } else {
            Intent i = new Intent(ProcessActivity.this, ManualBillBookActivity.class);
            startActivity(i);
            finish();
        }
    }

}