package in.hng.mpos.activity;

//529146

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import in.hng.mpos.Database.CustomerDB;
import in.hng.mpos.Database.LoyaltyCredentialsDB;
import in.hng.mpos.Database.LoyaltyDetailsDB;
import in.hng.mpos.Database.OrderedProductDetailsDB;
import in.hng.mpos.Database.ProductDetailsDB;
import in.hng.mpos.Database.StoreDB;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.Database.WalletDB;
import in.hng.mpos.MarketPlace.OrderInfromation;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.CustomerDetails;
import in.hng.mpos.gettersetter.LoyaltyDetails;
import in.hng.mpos.gettersetter.StoreDetails;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.helper.AlertManager;
import in.hng.mpos.helper.ApiCall;
import in.hng.mpos.helper.AppController;
import in.hng.mpos.helper.ConnectionDetector;
import in.hng.mpos.helper.Log;
import in.hng.mpos.helper.MailSender;

import static in.hng.mpos.R.*;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class LoyaltyActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "LoyaltyActivity";

    private AlertManager alert = new AlertManager();
    private SharedPreferences sp;
    SharedPreferences.Editor editor;

    private EditText mobile;
    private LinearLayout PreviousBillLL;
    private Button search, proceed, Submit, Cancel, LoyaltyEdit, Update, UpdCancel;
    private Button btnSubmitOrder, btnCancelOrder;
    private TextView name, email, phone, points, tier, txtOfferList;
    private TextView namepb, emailpb, phonepb, amountpb, statuspb;
    private LinearLayout title, custSearch, custDetails;
    private ListView listView;
    private RelativeLayout linearLayout1;
    private RadioGroup radioGroup, radioGroupUpdate;

    private OfferListAdapter adapter;
    private ProgressDialog progressDialog = null;

    ArrayList<LoyaltyDetails> offerDetails;
    private String StoreId, StoreOutletId, AccountId, AccountId_1, CustomerId, PATH, IPADDRESS;
    private String FirstName, LastName, Email, loyalMobile, loyalPoints, loyalTier, gender, dob, anniversary;
    private String LoyaltyID, LoyaltyPWD, Location, UserID, TillNo, storeEmail, storePwd, toEmail;

    private PopupWindow insertpopupWindow, updatepopupwindow, OrderIDpopupwindow;
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    private boolean isOrderTaking = false;
    private String isOrderID, isMobileNo;
    private String OrderID;
    private EditText edtOrderID;
    private Handler handler = new Handler();
    Runnable myRunnable;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loyalty);
        Toolbar toolbar = findViewById(R.id.toolbar);
     //  setSupportActionBar(toolbar);

        mobile = findViewById(id.txtmobile);
        search = findViewById(id.loyalSearch);
        name = findViewById(id.txtname);
        email = findViewById(id.txtemail);
        phone = findViewById(id.txtloyalphone);
        points = findViewById(id.txtloyalppoints);
        tier = findViewById(id.txtloyaltier);

        namepb = findViewById(id.txtnamepb);
        emailpb = findViewById(id.txtemailpb);
        phonepb = findViewById(id.txtloyalphonepb);
        amountpb = findViewById(id.txtamountpb);
        statuspb = findViewById(id.txtstatuspb);

        listView = findViewById(id.listloyaloffers);
        proceed = findViewById(id.btnproceed);
        LoyaltyEdit = findViewById(id.loyalEdit);
        linearLayout1 = findViewById(id.loyalty);
        Cancel = findViewById(id.btncancel);
        txtOfferList = findViewById(id.textView1);
        title = findViewById(id.lytTitle);
        custSearch = findViewById(id.lytCustSearch);
        custDetails = findViewById(id.lytCustDetails);
        PreviousBillLL = findViewById(R.id.lytpreviousdetails);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getActionBar().setHomeButtonEnabled(true);
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
//         Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();

        isOrderID = sp.getString("isOrderID", "");
        isMobileNo = sp.getString("isMobileNo", "");

        UserDB uDB = new UserDB(getApplicationContext());
        uDB.open();
        ArrayList<UserDetails> userDetails = uDB.getUserDetails();
        uDB.close();
        Location = userDetails.get(0).getStoreID();
        UserID = userDetails.get(0).getUserID();
        LoyaltyID = userDetails.get(0).getLoyaltyID();
        LoyaltyPWD = userDetails.get(0).getLoyaltyPWD();
        TillNo = userDetails.get(0).getTillNo();
        storeEmail = userDetails.get(0).getStoreEmail();
        storePwd = userDetails.get(0).getStorePwd();
        toEmail = userDetails.get(0).getSendEmailID();

        if (LoyaltyID.equalsIgnoreCase("") || LoyaltyPWD.equalsIgnoreCase("")) {
            showLoyaltyFailedAlert("Loyalty userID/Password is not available, Please contact IT.");
        }


        CustomerDB cusDB = new CustomerDB(getApplicationContext());
        cusDB.open();
        ArrayList<CustomerDetails> customerDetails = cusDB.getCustomerDetails();
        cusDB.close();

        if (!isOrderID.equalsIgnoreCase("")) {

            mobile.setText(isMobileNo);
            search.performClick();

        }


        if (customerDetails.size() > 0) {
            name.setText(customerDetails.get(0).getCustomerName());
            email.setText(customerDetails.get(0).getMailID());
            phone.setText(customerDetails.get(0).getMobileNO());
            points.setText(customerDetails.get(0).getPoints());
            tier.setText(customerDetails.get(0).getTier());

        }

        LoyaltyDetailsDB podb = new LoyaltyDetailsDB(getApplicationContext());
        podb.open();
        ArrayList<LoyaltyDetails> offerList = podb.checkOfferList();
        podb.close();
        if (offerList.size() > 0) {

            offerDetails = readFinalData();
            adapter = new OfferListAdapter(LoyaltyActivity.this, offerDetails);
            listView.setAdapter(adapter);
            Log.i(TAG, "Loyalty screen loaded");

        }


        //Resources res = getResources();
        //PATH = res.getString(string.API_URL);

        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH = urlDB.getUrlDetails();
        IPADDRESS = urlDB.getIpUrlDetails();
        urlDB.close();

        Log.i(TAG, "Loading Loyalty Activity");


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        StoreDB storedb = new StoreDB(getApplicationContext());
        storedb.open();
        ArrayList<StoreDetails> storeDetails = storedb.getStoreDetails();
        storedb.close();

        if (storeDetails.size() > 0) {

            CustomerDB custDB = new CustomerDB(getApplicationContext());
            custDB.open();
            custDB.deleteCustomerTable();
            custDB.close();

            LoyaltyDetailsDB offrDB = new LoyaltyDetailsDB(getApplicationContext());
            offrDB.open();
            offrDB.deleteLoyaltyTable();
            offrDB.close();

            name.setText("");
            email.setText("");
            phone.setText("");
            points.setText("");
            tier.setText("");

            offerDetails = readFinalData();
            adapter = new OfferListAdapter(LoyaltyActivity.this, offerDetails);
            listView.setAdapter(adapter);

            Location = storeDetails.get(0).getStoreID();
            UserID = storeDetails.get(0).getUserID();
            LoyaltyID = storeDetails.get(0).getLoyaltyID();
            LoyaltyPWD = storeDetails.get(0).getLoyaltyPWD();
            TillNo = userDetails.get(0).getTillNo();

            Log.w(TAG, "User ID == " + UserID);
            Log.w(TAG, "Location == " + Location);
            Log.w(TAG, "LoyaltyID == " + LoyaltyID);
            Log.w(TAG, "LoyaltyPWD == " + LoyaltyPWD);
            Log.w(TAG, "TillNo == " + TillNo);

            if (LoyaltyID.equalsIgnoreCase("") || LoyaltyPWD.equalsIgnoreCase("")) {
                showLoyaltyFailedAlert("Loyalty userID/Password is not available, Please contact IT.");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#3c3c3c"));
            }
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources()
                    .getColor(color.OrderTakingBackColor)));
            getSupportActionBar().setTitle("h&g - Assisted Order");
            isOrderTaking = true;
            Cancel.setVisibility(View.VISIBLE);
            title.setBackgroundDrawable(ContextCompat.getDrawable(LoyaltyActivity.this, drawable.order_text_box_style));
            custSearch.setBackgroundDrawable(ContextCompat.getDrawable(LoyaltyActivity.this, drawable.order_text_box_style));
            mobile.setBackgroundDrawable(ContextCompat.getDrawable(LoyaltyActivity.this, drawable.order_text_box_style));
            Field f = null;
            try {
                f = TextView.class.getDeclaredField("mCursorDrawableRes");
                f.setAccessible(true);
                f.set(mobile, drawable.ordercursor);
            } catch (Exception ignored) {
            }

            custDetails.setBackgroundDrawable(ContextCompat.getDrawable(LoyaltyActivity.this, drawable.order_text_box_style));
            listView.setBackgroundDrawable(ContextCompat.getDrawable(LoyaltyActivity.this, drawable.order_text_box_style));
            proceed.setBackground(new ColorDrawable(getResources()
                    .getColor(color.OrderTakingBackColor)));
            proceed.setTextColor(Color.WHITE);
            Cancel.setBackground(new ColorDrawable(getResources()
                    .getColor(color.OrderTakingBackColor)));
            Cancel.setTextColor(Color.WHITE);
            search.setBackground(new ColorDrawable(getResources()
                    .getColor(color.OrderTakingBackColor)));
            search.setTextColor(Color.WHITE);
            txtOfferList.setBackground(new ColorDrawable(getResources()
                    .getColor(color.OrderTakingBackColor)));
            txtOfferList.setTextColor(Color.WHITE);
        }

        if (!isOrderTaking)
            //  getSupportActionBar().setDisplayHomeAsUpEnabled(true);


            Cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    new AlertDialog.Builder(LoyaltyActivity.this)
                            .setMessage("Are you sure you want to Cancel Order Taking?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface idialog, int id) {
                                    StoreDB storeDB = new StoreDB(getApplicationContext());
                                    storeDB.open();
                                    storeDB.deleteStoreTable();
                                    storeDB.close();

                                    CustomerDB cusDB = new CustomerDB(getApplicationContext());
                                    cusDB.open();
                                    cusDB.deleteCustomerTable();
                                    cusDB.close();

                                    LoyaltyDetailsDB offrDB = new LoyaltyDetailsDB(getApplicationContext());
                                    offrDB.open();
                                    offrDB.deleteLoyaltyTable();
                                    offrDB.close();


                                    OrderedProductDetailsDB orderDB = new OrderedProductDetailsDB(getApplicationContext());
                                    orderDB.open();
                                    orderDB.deleteProductsTable();
                                    orderDB.close();

                                    editor.clear();
                                    editor.commit();

                                    Intent intent = new Intent(LoyaltyActivity.this, LoyaltyActivity.class);
                                    startActivity(intent);
                                    finish();


                                }
                            }).setNegativeButton("No", null)
                            .show();

                }
            });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View v = LoyaltyActivity.this.getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                if (net.isConnectingToInternet()) {
                    progressDialog = ProgressDialog.show(LoyaltyActivity.this, "",
                            "Fetching Details...", true);
                    Log.i(TAG, "Searching...");

                    handler = new Handler();
                    handler.postDelayed(myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (progressDialog != null) progressDialog.dismiss();
                        }
                    }, 30000);
                    if (mobile.getText().toString().length() >= 10) {

                        search.setEnabled(false);

                        LoyaltyEdit.setVisibility(View.INVISIBLE);
                        CustomerDB custDB = new CustomerDB(getApplicationContext());
                        custDB.open();
                        custDB.deleteCustomerTable();
                        custDB.close();

                        LoyaltyDetailsDB offrDB = new LoyaltyDetailsDB(getApplicationContext());
                        offrDB.open();
                        offrDB.deleteLoyaltyTable();
                        offrDB.close();

                        name.setText("");
                        email.setText("");
                        phone.setText("");
                        points.setText("");
                        tier.setText("");

                        offerDetails = readFinalData();
                        adapter = new OfferListAdapter(LoyaltyActivity.this, offerDetails);
                        listView.setAdapter(adapter);


                        Authenticate();


                    } else {
                        if (progressDialog != null) progressDialog.dismiss();
                        search.setEnabled(true);
                        try {
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        showLoyaltyFailedAlert("Please Provide Valid Mobile Number");
                    }
                } else {

                    AlertManager alert = new AlertManager();
                    alert.alert(LoyaltyActivity.this, "No Internet Connection",
                            "Please check " +
                                    "your data connection or Wifi is ON !");
                }
            }

        });

        LoyaltyEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Loading Loyalty Details Updation Window");
                hideKeyboard();

                LayoutInflater layoutInflater = (LayoutInflater) LoyaltyActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View customView = layoutInflater.inflate(layout.layout_update_customer_popup, null);
                updatepopupwindow = new PopupWindow(customView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);

                Update = customView.findViewById(id.UpdateLyltyCustomer);
                UpdCancel = customView.findViewById(id.CancellyltyUpdate);

                //instantiate popup window


                //display the popup window
                updatepopupwindow.showAtLocation(linearLayout1, Gravity.CENTER, 0, -30);
                updatepopupwindow.setFocusable(true);
                updatepopupwindow.update();


                final TextView first_name_update = customView.findViewById(id.txtlyltyNameUpdate);
                final TextView last_name_update = customView.findViewById(id.txtlyltylastNameUpdate);
                radioGroupUpdate = customView.findViewById(id.radioGroupUpdate);
                radioGroupUpdate.clearCheck();

                RadioButton rbM = radioGroupUpdate.findViewById(id.radioButtonM);
                RadioButton rbF = radioGroupUpdate.findViewById(id.radioButtonF);


                first_name_update.setText(FirstName);
                last_name_update.setText(LastName);
                if (gender.equals("1"))
                    rbM.setChecked(true);
                else if (gender.equals("2"))
                    rbF.setChecked(true);


                radioGroupUpdate.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton rb = group.findViewById(checkedId);
                        if (null != rb && checkedId > -1) {
                            if (rb.getText().toString().equalsIgnoreCase("Male"))
                                gender = "1";
                            else if (rb.getText().toString().equalsIgnoreCase("Female"))
                                gender = "2";
                        }
                    }
                });

                Update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "Updating Loyalty Customer Details");
                        hideKeyboard();

                        ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                        if (net.isConnectingToInternet()) {

                            RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
                            String URL = "http://api.directdialogs.com/api/Customer/UpdateCustomer";
                            Log.e(TAG, "Api Call : " + URL);

                            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.e(TAG, "Api Call response: " + response);

                                    // response
                                    try {
                                        Log.i(TAG, "Customer Details Updated Successfully");
                                        Toast.makeText(LoyaltyActivity.this, "Customer Details Updated Successfully", Toast.LENGTH_SHORT).show();
                                        updatepopupwindow.dismiss();
                                        mobile.setText(phone.getText().toString());
                                        search.performClick();

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Log.e(TAG, "Exception <<::>> " + e);
                                    }

                                    //Log.d("Response", response);
                                }
                            },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            if (progressDialog != null) progressDialog.dismiss();
                                            try {
                                                handler.removeCallbacks(myRunnable);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Log.e(TAG, "Exception <<::>> " + e);
                                            }
                                            showVolleyError(TAG, error);
                                        }
                                    }
                            ) {
                                @Override
                                protected Map<String, String> getParams() {
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put("CustomerId", CustomerId);
                                    params.put("FirstName", first_name_update.getText().toString());
                                    params.put("LastName", last_name_update.getText().toString());
                                    params.put("Gender", gender);
                                    params.put("AccountId", AccountId_1);
                                    params.put("StoreId", StoreId);
                                    params.put("StoreOutletId", StoreOutletId);
                                    return params;
                                }

                            };
                            requestQueue.add(stringRequest);


                        } else {
                            if (progressDialog != null) progressDialog.dismiss();
                            try {
                                handler.removeCallbacks(myRunnable);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, "Exception <<::>> " + e);
                            }
                            AlertManager alert = new AlertManager();
                            alert.alert(LoyaltyActivity.this, "No Internet Connection",
                                    "Please check " +
                                            "your data connection or Wifi is ON !");
                        }
                    }
                });

                UpdCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updatepopupwindow.dismiss();
                    }
                });

            }
        });

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CustomerDB custmrdb = new CustomerDB(getApplicationContext());
                custmrdb.open();
                ArrayList<CustomerDetails> customerdata = custmrdb.getCustomerDetails();
                custmrdb.close();



                if (customerdata.size() <= 0) {


                   /* Log.i(TAG, "Customer PhoneNo is Empty");
                    showLoyaltyFailedAlert("Please provide Customer Mobile Number");*/

                    if (!isOrderTaking) {
                        Log.i(TAG, "Proceeding to billing screen");
                        Intent intent = new Intent(LoyaltyActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.i(TAG, "Proceeding to order taking screen");
                        Intent intent = new Intent(LoyaltyActivity.this, OrderTakingProcess.class);
                        startActivity(intent);
                        finish();
                    }

                }
               else {

                    if (!isOrderTaking) {
                        Log.i(TAG, "Proceeding to billing screen");
                        Intent intent = new Intent(LoyaltyActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.i(TAG, "Proceeding to order taking screen");
                        Intent intent = new Intent(LoyaltyActivity.this, OrderTakingProcess.class);
                        startActivity(intent);
                        finish();
                    }
               }
            }
        });


        LoadPreviousBillDetails();

    }

    private void LoadPreviousBillDetails() {
        ConnectionDetector net = new ConnectionDetector(getApplicationContext());
        if (net.isConnectingToInternet()) {
            progressDialog = ProgressDialog.show(LoyaltyActivity.this, "",
                    "Loading....", true);
            Log.i(TAG, "Loading Previous billDetails..");
            try {
                RequestQueue queue = AppController.getInstance().getRequestQueue();
                final String url = "http://" + IPADDRESS + "/mposbill/api/mposdata/LastbillDeatil?till_no=" + TillNo + "&location_code=" + Location;
                // final String url ="http://35.200.223.104/mposbill/api/mposdata/LastbillDeatil?till_no=14&location_code=247";
                Log.d("Url Adress:", url);
                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override                            public void onResponse(JSONObject response) {
                                // display response
                                Log.d(TAG, "LoadPreviousBillDetails response <<::>> " + response);
                                try {

                                    String statusCode = response.getString("statusCode");

                                    if (statusCode.equalsIgnoreCase("200")) {

                                        String Msg = response.getString("detail");

                                        JSONArray jsonArray = response.getJSONArray("detail");
                                        JSONObject json = jsonArray.getJSONObject(0);
                                        namepb.setText(json.getString("customer_name"));
                                        emailpb.setText(json.getString("bill_date"));
                                        phonepb.setText(json.getString("customer_phone"));
                                        amountpb.setText(json.getString("bill_value"));
                                        if (json.get("bill_close_status").equals("1")) {
                                            statuspb.setText("Success");
                                        } else {
                                            statuspb.setText("Failed");
                                        }
                                    } else {
                                        String message = response.getString("Message");
                                        showLoyaltyFailedAlert(message);
                                    }

                                    //Toast.makeText(LoyaltyActivity.this, Msg,
                                    //      Toast.LENGTH_SHORT).show();

                                    progressDialog.dismiss();
                                    // finish();
                                    //Log.d("Response", response.toString());
                                } catch (JSONException e) {
                                    progressDialog.dismiss();
                                    e.printStackTrace();
                                    Log.e(TAG, "Exception <<::>> " + e);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                progressDialog.dismiss();
                                showVolleyError(TAG, error);
                            }
                        }
                ) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                };

                queue.add(getRequest);

            } catch (Exception e) {
                // dialog.dismiss();
                showLoyaltyFailedAlert(e.getMessage());
                Log.e(TAG, e.getMessage());
                System.out.println("Exception : " + e.getMessage());
            }

        } else {

            AlertManager alert = new AlertManager();
            alert.alert(LoyaltyActivity.this, "No Internet Connection",
                    "Please check " +
                            "your data connection or Wifi is ON !");

        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {

            case R.id.nav_regularize_manual_bill:
                intent = new Intent(LoyaltyActivity.this, RegularizeManualBillListActivity.class);
                startActivity(intent);
                return true;

            case R.id.nav_manual_billing:
                deletetables();
                intent = new Intent(LoyaltyActivity.this, ManualBillLoyaltyActivity.class);
                startActivity(intent);
                return true;

            case R.id.nav_customer_related:
                intent = new Intent(LoyaltyActivity.this, CustomerRelatedActivity.class);
                startActivity(intent);
                return true;

            case R.id.nav_MarketPlace:
                intent = new Intent(LoyaltyActivity.this, OrderInfromation.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.nav_placeOrder:
                String Url = PATH + "AssistedPickstore?userid=" + UserID;
                makeAutofillApiCall(Url);
                return true;

            case R.id.nav_processOrder:
                intent = new Intent(LoyaltyActivity.this, ActivityLoadOrders.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.nav_generateInvoice:
                LayoutInflater layoutInflater = (LayoutInflater) LoyaltyActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View customView = layoutInflater.inflate(layout.order_id_popup, null);
                OrderIDpopupwindow = new PopupWindow(customView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
                btnSubmitOrder = customView.findViewById(R.id.btnOrderID);
                btnCancelOrder = customView.findViewById(R.id.btnCancel);
                edtOrderID = customView.findViewById(R.id.txtorderID);
                OrderIDpopupwindow.showAtLocation(linearLayout1, Gravity.CENTER, 0, -60);
                OrderIDpopupwindow.setFocusable(true);
                OrderIDpopupwindow.update();

                btnSubmitOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OrderID = edtOrderID.getText().toString();
                        String Url = PATH + "AssistedOrderrefersh?Orderid=" + OrderID;
                        makeGenerateBillApiCall(Url);


                    }
                });

                btnCancelOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OrderIDpopupwindow.dismiss();
                    }
                });

                return true;
            default:
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
        }
    }


    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_loyalty, menu);

//        if (!isOrderTaking)
//            getMenuInflater().inflate(R.menu.menu_loyalty, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case id.action_declaration:
                CheckDeclaration(UserID, Location);
                return true;

            case id.action_logout:
                Logout(UserID, Location);
                return true;

            case id.action_printer:
                Intent intent = new Intent(LoyaltyActivity.this, PrinterActivity.class);
                startActivity(intent);
                finish();
                return true;

            case id.action_bill_log:
                Intent intent2 = new Intent(LoyaltyActivity.this, PreviousBillActivity.class);
                startActivity(intent2);
                finish();
                return true;

            case id.action_edc_settings:
                Intent CardIntent = new Intent(LoyaltyActivity.this, CardSettingsActivity.class);
                startActivity(CardIntent);
                finish();
                return true;

        }
        if (t.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);

    }

    public void Authenticate() {
        if (progressDialog != null) progressDialog.dismiss();
        try {
            handler.removeCallbacks(myRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressDialog = ProgressDialog.show(LoyaltyActivity.this, "",
                "Authenticating User...", true);
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                if (progressDialog != null) progressDialog.dismiss();
            }
        }, 30000);

        Log.i(TAG, "Authenticating User");
        RequestQueue queue = AppController.getInstance().getRequestQueue();
        final String url = "http://api.directdialogs.com/api/Account/Authenticate?userName=" + LoyaltyID + "&password=" + LoyaltyPWD + "&serviceType=2";
        Log.e(TAG, "Api Call : " + url);

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, "Api Call response: " + response);

                        // display response
                        try {
                            String Status = response.getString("IsAutheticated");

                            if (Status.equalsIgnoreCase("true")) {

                                AccountId_1 = response.getString("Token");
                                AccountId = URLEncoder.encode(AccountId_1, "utf-8");
                                StoreId = response.getString("StoreId");
                                StoreOutletId = response.getString("StoreOutletId");

                                Search_Customer();

                            } else {
                                if (progressDialog != null) progressDialog.dismiss();
                                search.setEnabled(true);
                                try {
                                    handler.removeCallbacks(myRunnable);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                showLoyaltyFailedAlert("Loyalty Authentication Failed");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "JSON Exception <<::>> " + e);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Exception <<::>> " + e);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        search.setEnabled(true);
                        try {
                            handler.removeCallbacks(myRunnable);
                            if (progressDialog != null) progressDialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        showVolleyError(TAG, error);
                    }
                }
        );
        getRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(getRequest);

    }

    public void Search_Customer() {
        hideKeyboard();

        if (progressDialog != null) progressDialog.dismiss();
        try {
            handler.removeCallbacks(myRunnable);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Handler exception " + e);
        }

        progressDialog = ProgressDialog.show(LoyaltyActivity.this, "",
                "Searching Customer...", true);
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                if (progressDialog != null) progressDialog.dismiss();
            }
        }, 30000);
        Log.i(TAG, "Searching Customer- " + mobile.getText().toString());
        RequestQueue queue = AppController.getInstance().getRequestQueue();
        String url = "http://api.directdialogs.com/api/Customer/SearchCustomer?accountId=" + AccountId + "&mobile=" + mobile.getText().toString();
        Log.e(TAG, "Api Call : " + url);

        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "Api Call : " + response);

                        try {
                            JSONArray json_loyal_cust_array = new JSONArray(response);

                            if (json_loyal_cust_array.length() > 0) {
                                Log.i(TAG, "Customer found");
                                JSONObject json_loyal_cust;
                                json_loyal_cust = json_loyal_cust_array.getJSONObject(0);
                                FirstName = json_loyal_cust.getString("FirstName");
                                LastName = json_loyal_cust.getString("LastName");
                                Email = json_loyal_cust.getString("Email");
                                loyalMobile = json_loyal_cust.getString("Mobile");
                                CustomerId = json_loyal_cust.getString("CustomerId");
                                dob = json_loyal_cust.getString("Birthday");
                                gender = json_loyal_cust.getString("Gender");
                                anniversary = json_loyal_cust.getString("Anniversaryday");

                                Fetch_Glow_Rewards();


                            } else {
                                if (progressDialog != null) progressDialog.dismiss();
                                search.setEnabled(true);
                                try {
                                    handler.removeCallbacks(myRunnable);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Log.i(TAG, "Customer not found");
                                Toast toast = Toast.makeText(getApplicationContext(), "Customer not registered with loyalty", Toast.LENGTH_SHORT);
                                toast.show();
                                //showLoyaltyFailedAlert("Customer does not exist");
                                LayoutInflater layoutInflater = (LayoutInflater) LoyaltyActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                View customView = layoutInflater.inflate(layout.loyalty_new_customer_popup, null);
                                insertpopupWindow = new PopupWindow(customView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);

                                Submit = customView.findViewById(id.InsertLyltyCustomer);
                                Cancel = customView.findViewById(id.CancellyltyInsert);

                                //instantiate popup window

                                //display the popup window

                                //insertpopupWindow.showAtLocation(linearLayout1, Gravity.CENTER, 0, -60);
                                insertpopupWindow.showAtLocation(linearLayout1, Gravity.CENTER, 0, 60);
                                //insertpopupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
                                insertpopupWindow.setFocusable(true);
                                insertpopupWindow.update();


                                final TextView first_name = customView.findViewById(id.txtlyltyName);
                                final TextView last_name = customView.findViewById(id.txtlyltylastName);
                                final TextView mobile_no = customView.findViewById(id.txtlyltyMobile);

                                radioGroup = customView.findViewById(id.radioGroup);
                                radioGroup.clearCheck();
                                mobile_no.setText(mobile.getText().toString());
                                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                                        RadioButton rb = group.findViewById(checkedId);
                                        if (null != rb && checkedId > -1) {
                                            if (rb.getText().toString().equalsIgnoreCase("Male"))
                                                gender = "1";
                                            else if (rb.getText().toString().equalsIgnoreCase("Female"))
                                                gender = "2";


                                        }

                                    }
                                });


                                Submit.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        hideKeyboard();

                                        ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                                        if (net.isConnectingToInternet()) {
                                            if (gender == null) {
                                                showLoyaltyFailedAlert("Please Select Gender");
                                            } else if (mobile_no.getText().toString().isEmpty() | mobile_no.getText().toString().length() < 10) {
                                                showLoyaltyFailedAlert("Please enter valid mobile Number");
                                            } else {
                                                progressDialog = ProgressDialog.show(LoyaltyActivity.this, "",
                                                        "Adding customer...", true);

                                                handler = new Handler();
                                                handler.postDelayed(myRunnable = new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        if (progressDialog != null)
                                                            progressDialog.dismiss();
                                                    }
                                                }, 30000);

                                                Log.i(TAG, "Adding customer");
                                                RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
                                                String URL = "http://api.directdialogs.com/api/Customer/CreateCustomer";
                                                Log.i(TAG, "Api Call: " + URL);
                                                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                                                    @Override
                                                    public void onResponse(String response) {
                                                        // response
                                                        Log.i(TAG, "Api Call response: " + response);
                                                        try {

                                                            JSONObject Jsonobj = new JSONObject(response);
                                                            String Cust_ID = Jsonobj.getString("CustomerId");
                                                            HashMap<String, String> LoyaltyCredentialDetails = new HashMap<String, String>();
                                                            LoyaltyCredentialDetails.put("accountID", AccountId);
                                                            LoyaltyCredentialDetails.put("storeID", StoreId);
                                                            LoyaltyCredentialDetails.put("storeOutletID", StoreOutletId);
                                                            LoyaltyCredentialDetails.put("customerID", Cust_ID);
                                                            LoyaltyCredentialsDB loyDB = new LoyaltyCredentialsDB(getApplicationContext());
                                                            loyDB.open();
                                                            loyDB.deleteLoyaltyCredentialsDetailsTable();
                                                            loyDB.createLoyaltyCredentialsDetailsTable();
                                                            loyDB.insertLoyaltyCredentialsDetails(LoyaltyCredentialDetails);
                                                            loyDB.close();

                                                            HashMap<String, String> CustomerDetails = new HashMap<String, String>();
                                                            CustomerDetails.put("customerID", Cust_ID);
                                                            CustomerDetails.put("mobileNO", mobile_no.getText().toString());
                                                            CustomerDetails.put("name", first_name.getText().toString() + " " + last_name.getText().toString());
                                                            CustomerDetails.put("mailID", "");
                                                            CustomerDetails.put("gender", gender);
                                                            CustomerDetails.put("dob", "");
                                                            CustomerDetails.put("anniversary", "");
                                                            CustomerDetails.put("points", "0");
                                                            CustomerDetails.put("tier", "");
                                                            CustomerDB cusDB = new CustomerDB(getApplicationContext());
                                                            cusDB.open();
                                                            cusDB.deleteCustomerTable();
                                                            cusDB.createCustomerDetailsTable();
                                                            cusDB.insertCustomerDetails(CustomerDetails);
                                                            cusDB.close();

                                                            LoyaltyEdit.setVisibility(View.VISIBLE);
                                                            if (progressDialog != null)
                                                                progressDialog.dismiss();
                                                            try {
                                                                handler.removeCallbacks(myRunnable);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                            Toast.makeText(LoyaltyActivity.this, "Customer Created Successfully", Toast.LENGTH_SHORT).show();

                                                            mobile.setText(mobile_no.getText().toString());
                                                            insertpopupWindow.dismiss();
                                                            mobile.setText(mobile_no.getText().toString());
                                                            search.performClick();
                                                            search.setEnabled(true);

                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                            Log.e(TAG, "Exception <<::>> " + e);
                                                        }

                                                        // Log.d("Response", response);
                                                    }
                                                },
                                                        new Response.ErrorListener() {
                                                            @Override
                                                            public void onErrorResponse(VolleyError error) {


                                                                if (progressDialog != null)
                                                                    progressDialog.dismiss();
                                                                search.setEnabled(true);
                                                                try {
                                                                    handler.removeCallbacks(myRunnable);
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                                showVolleyError(TAG, error);

                                                            }
                                                        }) {
                                                    @Override
                                                    protected Map<String, String> getParams() {
                                                        Map<String, String> params = new HashMap<String, String>();
                                                        params.put("FirstName", first_name.getText().toString());
                                                        params.put("LastName", last_name.getText().toString());
                                                        params.put("Mobile", mobile_no.getText().toString());
                                                        params.put("Gender", gender);
                                                        params.put("AccountId", AccountId_1);
                                                        params.put("StoreId", StoreId);
                                                        params.put("StoreOutletId", StoreOutletId);
                                                        return params;
                                                    }

                                                };
                                                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                                        6000,
                                                        3,
                                                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                                                requestQueue.add(stringRequest);

                                            }

                                        } else {
                                            if (progressDialog != null) progressDialog.dismiss();
                                            search.setEnabled(true);
                                            try {
                                                handler.removeCallbacks(myRunnable);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            AlertManager alert = new AlertManager();
                                            alert.alert(LoyaltyActivity.this, "No Internet Connection",
                                                    "Please check " +
                                                            "your data connection or Wifi is ON !");
                                        }


                                    }
                                });

                                //close the popup window on button click
                                Cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        insertpopupWindow.dismiss();
                                    }
                                });
                            }

                        } catch (Exception e) {
                            // In your production code handle any errors and catch the individual exceptions
                            e.printStackTrace();
                            Log.e(TAG, "Exception <<::>> " + e);
                        }

                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (progressDialog != null) progressDialog.dismiss();
                        search.setEnabled(true);
                        try {
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        cleardetails();
                        showVolleyError(TAG, error);
                    }
                }
        );

        queue.add(getRequest);

    }

    public void Fetch_Glow_Rewards() {

        if (progressDialog != null) progressDialog.dismiss();
        try {
            handler.removeCallbacks(myRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressDialog = ProgressDialog.show(LoyaltyActivity.this, "",
                "Fetching glow rewards...", true);
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                if (progressDialog != null) progressDialog.dismiss();
            }
        }, 30000);

        Log.i(TAG, "Fetching glow rewards");
        RequestQueue queue = AppController.getInstance().getRequestQueue();
        final String url = "http://api.directdialogs.com/api/Customer/overview?accountId=" + AccountId + "&customerid=" + CustomerId;
        Log.i(TAG, "Url - " + url);
        ConnectionDetector net = new ConnectionDetector(getApplicationContext());

        if (net.isConnectingToInternet()) {
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i(TAG, "Api Call response: " + response);
                            // display response
                            try {
                                if (response.length() > 0) {

                                    loyalPoints = response.getString("PointBalance");
                                    loyalTier = response.getString("TierName");
                                    filldetails();

                                    //dialog.dismiss();
                                    //handler.removeCallbacks(myRunnable);
                                } else {
                                    if (progressDialog != null) progressDialog.dismiss();
                                    search.setEnabled(true);
                                    try {
                                        handler.removeCallbacks(myRunnable);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    showLoyaltyFailedAlert("Error Fetching points");
                                    Log.i(TAG, "Error Fetching points");
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "Exception <<::>> " + e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    try {
                        if (progressDialog != null) progressDialog.dismiss();
                        search.setEnabled(true);
                        handler.removeCallbacks(myRunnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    showVolleyError(TAG, error);
                }
            }
            );
            getRequest.setRetryPolicy(new DefaultRetryPolicy(
                    6000,
                    3,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            queue.add(getRequest);
        } else {
            if (progressDialog != null) progressDialog.dismiss();
            search.setEnabled(true);
            try {
                handler.removeCallbacks(myRunnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
            AlertManager alert = new AlertManager();
            alert.alert(LoyaltyActivity.this, "No Internet Connection",
                    "Please check " +
                            "your data connection or Wifi is ON !");

        }
    }

    public void filldetails() {
        try {
            if (progressDialog != null) progressDialog.dismiss();
            try {
                handler.removeCallbacks(myRunnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
            progressDialog = ProgressDialog.show(LoyaltyActivity.this, "",
                    "Saving customer details...", true);
            handler = new Handler();
            handler.postDelayed(myRunnable = new Runnable() {
                @Override
                public void run() {

                    if (progressDialog != null) progressDialog.dismiss();
                }
            }, 30000);

            Log.i(TAG, "Loyal Details: " + AccountId + "-" + StoreId + "-" + StoreOutletId + "-" + CustomerId);
            PreviousBillLL.setVisibility(View.GONE);
            custDetails.setVisibility(View.VISIBLE);
            name.setText(FirstName + " " + LastName);
            email.setText(Email);
            phone.setText(loyalMobile);
            points.setText(String.valueOf(Math.round(Float.parseFloat(loyalPoints))));
            tier.setText(loyalTier);
            mobile.setText("");

            HashMap<String, String> LoyaltyCredentialDetails = new HashMap<String, String>();
            LoyaltyCredentialDetails.put("accountID", AccountId);
            LoyaltyCredentialDetails.put("storeID", StoreId);
            LoyaltyCredentialDetails.put("storeOutletID", StoreOutletId);
            LoyaltyCredentialDetails.put("customerID", CustomerId);
            LoyaltyCredentialsDB loyDB = new LoyaltyCredentialsDB(getApplicationContext());
            loyDB.open();
            loyDB.deleteLoyaltyCredentialsDetailsTable();
            loyDB.createLoyaltyCredentialsDetailsTable();
            loyDB.insertLoyaltyCredentialsDetails(LoyaltyCredentialDetails);
            loyDB.close();

            Log.i(TAG, "Customer Details: " + CustomerId + "-" + loyalMobile + "-" + FirstName + " " + LastName + "-" + Email + "-" + gender + "-" + dob + "-" + anniversary + "-" + loyalPoints + "-" + loyalPoints);
            HashMap<String, String> CustomerDetails = new HashMap<String, String>();
            CustomerDetails.put("customerID", CustomerId);
            CustomerDetails.put("mobileNO", loyalMobile);
            CustomerDetails.put("name", FirstName + " " + LastName);
            CustomerDetails.put("mailID", Email);
            CustomerDetails.put("gender", gender);
            CustomerDetails.put("dob", dob);
            CustomerDetails.put("anniversary", anniversary);
            CustomerDetails.put("points", loyalPoints);
            CustomerDetails.put("tier", loyalTier);
            CustomerDB cusDB = new CustomerDB(getApplicationContext());
            cusDB.open();
            cusDB.deleteCustomerTable();
            cusDB.createCustomerDetailsTable();
            cusDB.insertCustomerDetails(CustomerDetails);
            cusDB.close();
            LoyaltyEdit.setVisibility(View.VISIBLE);

            Fetch_Loyal_Offers();

        } catch (Exception e) {
            if (progressDialog != null) progressDialog.dismiss();
            try {
                handler.removeCallbacks(myRunnable);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.out.println("Exception : " + e.getMessage());
        }

    }

    public void cleardetails() {
        try {

            name.setText("");
            email.setText("");
            phone.setText("");
            points.setText("");
            tier.setText("");
            mobile.setText("");

            HashMap<String, String> LoyaltyCredentialDetails = new HashMap<String, String>();
            LoyaltyCredentialDetails.put("accountID", "");
            LoyaltyCredentialDetails.put("storeID", "");
            LoyaltyCredentialDetails.put("storeOutletID", "");
            LoyaltyCredentialDetails.put("customerID", "");
            LoyaltyCredentialsDB loyDB = new LoyaltyCredentialsDB(getApplicationContext());
            loyDB.open();
            loyDB.deleteLoyaltyCredentialsDetailsTable();
            loyDB.createLoyaltyCredentialsDetailsTable();
            loyDB.insertLoyaltyCredentialsDetails(LoyaltyCredentialDetails);
            loyDB.close();

            HashMap<String, String> CustomerDetails = new HashMap<String, String>();
            CustomerDetails.put("customerID", "");
            CustomerDetails.put("mobileNO", "");
            CustomerDetails.put("name", "");
            CustomerDetails.put("mailID", "");
            CustomerDetails.put("gender", "");
            CustomerDetails.put("dob", "");
            CustomerDetails.put("anniversary", "");
            CustomerDetails.put("points", "");
            CustomerDetails.put("tier", "");
            CustomerDB cusDB = new CustomerDB(getApplicationContext());
            cusDB.open();
            cusDB.deleteCustomerTable();
            cusDB.createCustomerDetailsTable();
            cusDB.insertCustomerDetails(CustomerDetails);
            cusDB.close();

        } catch (Exception e) {

            System.out.println("Exception : " + e.getMessage());
            Log.e(TAG, "Exception <<::>> "+e);
        }


    }

    public void Fetch_Loyal_Offers() {

        if (progressDialog != null) progressDialog.dismiss();
        try {
            handler.removeCallbacks(myRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressDialog = ProgressDialog.show(LoyaltyActivity.this, "",
                "Fetching loyalty Offers...", true);
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                if (progressDialog != null) progressDialog.dismiss();
            }
        }, 30000);
        Log.i(TAG, "Fetching loyalty Offers");
        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
        String URL = "http://api.directdialogs.com/api/offer/GetOffersByCustomer";
        Log.i(TAG, "Api Call: " + URL);
        ConnectionDetector net = new ConnectionDetector(getApplicationContext());
        if (net.isConnectingToInternet()) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i(TAG, "Api Call response: " + response);
                    // response
                    try {
                        if (progressDialog != null) progressDialog.dismiss();
                        try {
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        JSONArray offers = new JSONArray(response);
                        LoyaltyDetailsDB lyltyDB = new LoyaltyDetailsDB(getApplicationContext());
                        lyltyDB.open();
                        lyltyDB.deleteLoyaltyTable();
                        lyltyDB.createLoyaltyDetailsTable();
                        lyltyDB.insertBulkOfferDetails(offers);
                        lyltyDB.close();
                        offerDetails = readFinalData();
                        adapter = new OfferListAdapter(LoyaltyActivity.this, offerDetails);
                        listView.setAdapter(adapter);
                        search.setEnabled(true);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    // Log.d("Response", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {


                    try {
                        if (progressDialog != null) progressDialog.dismiss();
                        search.setEnabled(true);
                        try {
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (error instanceof TimeoutError) {
                            showLoyaltyFailedAlert("Time out error occurred.");
                            Log.e(TAG, "Time out error occurred.");
                            //Time out error

                        } else if (error instanceof NoConnectionError) {
                            showLoyaltyFailedAlert("Network error occurred.");
                            Log.e(TAG, "Network error occurred.");
                            //net work error

                        } else if (error instanceof AuthFailureError) {
                            showLoyaltyFailedAlert("Authentication error occurred.Please click on OK and try again");
                            Log.e(TAG, "Authentication error occurred.Please click on OK and try again");
                            //error

                        } else if (error instanceof ServerError) {
                            showLoyaltyFailedAlert("Server error occurred.");
                            Log.e(TAG, "Server error occurred.");
                            //Error
                        } else if (error instanceof NetworkError) {
                            showLoyaltyFailedAlert("Network error occurred.Please click on OK and try again");
                            Log.e(TAG, "Network error occurred.");
                            //Error

                        } else if (error instanceof ParseError) {
                            //Error
                            showLoyaltyFailedAlert("An error occurred.Please click on OK and try again");
                            Log.e(TAG, "An error occurred.");
                        } else {

                            showLoyaltyFailedAlert("An error occurred.Please click on OK and try again");
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
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("StoreId", StoreId);
                    params.put("StoreOutletId", StoreOutletId);
                    params.put("CustomerId", CustomerId);
                    params.put("AccountId", URLDecoder.decode(AccountId));
                    params.put("Token", URLDecoder.decode(AccountId));

                    return params;
                }

            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    6000,
                    3,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(stringRequest);
        } else {
            if (progressDialog != null) progressDialog.dismiss();
            search.setEnabled(true);
            try {
                handler.removeCallbacks(myRunnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
            AlertManager alert = new AlertManager();
            alert.alert(LoyaltyActivity.this, "No Internet Connection",
                    "Please check " +
                            "your data connection or Wifi is ON !");

        }
    }


    private void makeAutofillApiCall(String urlPath) {
        progressDialog = ProgressDialog.show(LoyaltyActivity.this, "",
                "Please Wait...", true);
        Log.i(TAG, "Api Call: " + urlPath);
        ApiCall.make(this, urlPath, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "Api Call response: " + response);
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            Log.i(TAG, response.toString());
                            String StatusCode = responseObject.getString("statusCode");
                            //String Message = responseObject.getString("Message");
                            //Log.i(TAG, Message);
                            if (StatusCode.equalsIgnoreCase("200")) {

                                progressDialog.dismiss();
                                JSONArray Details = responseObject.getJSONArray("details");
                                final String location, lyltyID, lyltyPWD;

                                location = Details.getJSONObject(0).getString("Order_Location_code");
                                lyltyID = Details.getJSONObject(0).getString("loyalty_username");
                                lyltyPWD = Details.getJSONObject(0).getString("loyalty_Password");
                                //tillNo = responseObject.getString("tillNo");

                                HashMap<String, String> StoreDetails = new HashMap<String, String>();
                                StoreDetails.put("userID", UserID);
                                StoreDetails.put("storeID", location);
                                StoreDetails.put("loyaltyID", lyltyID);
                                StoreDetails.put("loyaltyPWD", lyltyPWD);
                                StoreDetails.put("tillNo", TillNo);

                                StoreDB storeDB = new StoreDB(getApplicationContext());
                                storeDB.open();
                                storeDB.deleteStoreTable();
                                storeDB.createStoreDetailsTable();
                                storeDB.insertStoreDetails(StoreDetails);
                                storeDB.close();

                                Intent intent = new Intent(LoyaltyActivity.this, LoyaltyActivity.class);
                                startActivity(intent);
                                finish();


                                // Toast.makeText(Login.this, Message,Toast.LENGTH_SHORT).show();


                            } else {
                                progressDialog.dismiss();
                                String Message = responseObject.getString("Message");
                                Log.i(TAG, Message);
                                showLoyaltyFailedAlert(Message);
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, e.toString());
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        progressDialog.dismiss();

                        try {

                            if (error instanceof TimeoutError) {
                                showLoyaltyFailedAlert("Time out error occurred.Please click on OK and try again");
                                Log.e(TAG, "Time out error occurred.");
                                //Time out error

                            } else if (error instanceof NoConnectionError) {
                                showLoyaltyFailedAlert("Network error occurred.Please click on OK and try again");
                                Log.e(TAG, "Network error occurred.");
                                //net work error

                            } else if (error instanceof AuthFailureError) {
                                showLoyaltyFailedAlert("Authentication error occurred.Please click on OK and try again");
                                Log.e(TAG, "Authentication error occurred.");
                                //error

                            } else if (error instanceof ServerError) {
                                showLoyaltyFailedAlert("Server error occurred.Please click on OK and try again");
                                Log.e(TAG, "Server error occurred.");
                                //Error
                            } else if (error instanceof NetworkError) {
                                showLoyaltyFailedAlert("Network error occurred.Please click on OK and try again");
                                Log.e(TAG, "Network error occurred.");
                                //Error

                            } else if (error instanceof ParseError) {
                                //Error
                                showLoyaltyFailedAlert("An error occurred.Please click on OK and try again");
                                Log.e(TAG, "An error occurred.");
                            } else {

                                showLoyaltyFailedAlert("An error occurred.Please click on OK and try again");
                                Log.e(TAG, "An error occurred.");
                                //Error
                            }
                            //End


                        } catch (Exception e) {


                        }


                    }
                }
        );
    }

    private void makeGenerateBillApiCall(String urlPath) {
        progressDialog = ProgressDialog.show(LoyaltyActivity.this, "",
                "Please Wait...", true);
        Log.i(TAG, "Api Call: " + urlPath);
        ApiCall.make(this, urlPath, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "Api Call: " + response);
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            Log.i(TAG, response.toString());
                            String StatusCode = responseObject.getString("statusCode");
                            //String Message = responseObject.getString("Message");
                            //Log.i(TAG, Message);
                            if (StatusCode.equalsIgnoreCase("200")) {

                                //dialog.dismiss();

                                Log.i(TAG, "Billlog API:Success");
                                // billno = Jsonobj.getString("bill_no");
                                //BillNo.setText(billno);
                                JSONArray refershLog = responseObject.getJSONArray("refershLog");
                                ProductDetailsDB podb = new ProductDetailsDB(getApplicationContext());
                                podb.open();
                                podb.deleteUserTable();
                                podb.createProductDetailsTable();
                                podb.insertBulkPOSKUDetails(refershLog);
                                podb.close();


                                UserDB userdb = new UserDB(getApplicationContext());
                                userdb.open();
                                ArrayList<UserDetails> userdata = userdb.getUserDetails();
                                userdb.close();
                                editor.putString("Bill_no", "");
                                editor.putString("isOrderID", OrderID);
                                editor.putString("isMobileNo", responseObject.getString("mobile_no"));
                                editor.commit();
                                progressDialog.dismiss();
                                Intent intent = new Intent(LoyaltyActivity.this, LoyaltyActivity.class);
                                startActivity(intent);
                                finish();

                                Log.d("Bill_log API response", responseObject.toString());


                            } else {
                                progressDialog.dismiss();
                                String Message = responseObject.getString("Message");
                                Log.i(TAG, Message);
                                showLoyaltyFailedAlert(Message);
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, e.toString());
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        progressDialog.dismiss();

                        try {

                            if (error instanceof TimeoutError) {
                                showLoyaltyFailedAlert("Time out error occurred.Please click on OK and try again");
                                Log.e(TAG, "Time out error occurred.");
                                //Time out error

                            } else if (error instanceof NoConnectionError) {
                                showLoyaltyFailedAlert("Network error occurred.Please click on OK and try again");
                                Log.e(TAG, "Network error occurred.");
                                //net work error

                            } else if (error instanceof AuthFailureError) {
                                showLoyaltyFailedAlert("Authentication error occurred.Please click on OK and try again");
                                Log.e(TAG, "Authentication error occurred.");
                                //error

                            } else if (error instanceof ServerError) {
                                showLoyaltyFailedAlert("Server error occurred.Please click on OK and try again");
                                Log.e(TAG, "Server error occurred.");
                                //Error
                            } else if (error instanceof NetworkError) {
                                showLoyaltyFailedAlert("Network error occurred.Please click on OK and try again");
                                Log.e(TAG, "Network error occurred.");
                                //Error

                            } else if (error instanceof ParseError) {
                                //Error
                                showLoyaltyFailedAlert("An error occurred.Please click on OK and try again");
                                Log.e(TAG, "An error occurred.");
                            } else {

                                showLoyaltyFailedAlert("An error occurred.Please click on OK and try again");
                                Log.e(TAG, "An error occurred.");
                                //Error
                            }
                            //End


                        } catch (Exception e) {


                        }


                    }
                }
        );
    }


    public ArrayList<LoyaltyDetails> readFinalData() {
        LoyaltyDetailsDB lyltyDB = new LoyaltyDetailsDB(getApplicationContext());
        lyltyDB.open();
        ArrayList<LoyaltyDetails> offers = lyltyDB.getOfferDetails();
        lyltyDB.close();
        return offers;

    }


    public void showLoyaltyFailedAlert(final String msg) {

        LoyaltyActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, msg);
                AlertDialog.Builder builder = new AlertDialog.Builder(LoyaltyActivity.this);
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

    public void CheckDeclaration(final String user, final String location) {
        Log.i(TAG, "Checking for declaration");
        RequestQueue queue = AppController.getInstance().getRequestQueue();
        final String url = PATH + "declaration?Location=" + location + "&Cashier=" + user + "&TillNo=" + TillNo;
        Log.i(TAG, "Api Call: " + url);
        ConnectionDetector net = new ConnectionDetector(getApplicationContext());
        if (net.isConnectingToInternet()) {
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i(TAG, "Api Call response: " + response);
                            // display response
                            try {
                                String Status = response.getString("Status");
                                Log.i(TAG, "Declaration required");
                                if (Status.equalsIgnoreCase("Success")) {
                                    editor.putString("Location", location);
                                    editor.putString("Cashier", user);
                                    editor.putString("Cash", response.getString("Cash"));
                                    editor.putString("Card", response.getString("Credit Card"));
                                    editor.putString("Coupon", response.getString("Coupon"));
                                    editor.putString("Loyalty", response.getString("Loyalty"));
                                    editor.putString("Wallet", response.getString("Wallet"));
                                    editor.putString("TillNo", TillNo);
                                    editor.putString("fromMail", response.getString("FromMailID"));
                                    editor.putString("mailPwd", response.getString("MailPWD"));
                                    editor.putString("toMail", response.getString("ToMailID"));
                                    editor.commit();
                                    Intent intent = new Intent(LoyaltyActivity.this, DeclarationActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    Log.i(TAG, "No Declaration is pending");
                                    showLoyaltyFailedAlert("No Declaration is pending");                                //Intent intent = new Intent(Login.this, LoyaltyActivity.class);
                                    //startActivity(intent);
                                    // finish();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            try {
                                progressDialog.dismiss();
                                if (error instanceof TimeoutError) {
                                    showLoyaltyFailedAlert("Time out error occurred.Please click on OK and try again");
                                    Log.e(TAG, "Time out error occurred.");
                                    //Time out error

                                } else if (error instanceof NoConnectionError) {
                                    showLoyaltyFailedAlert("Network error occurred.Please click on OK and try again");
                                    Log.e(TAG, "Network error occurred.");
                                    //net work error

                                } else if (error instanceof AuthFailureError) {
                                    showLoyaltyFailedAlert("Authentication error occurred.Please click on OK and try again");
                                    Log.e(TAG, "Authentication error occurred.");
                                    //error

                                } else if (error instanceof ServerError) {
                                    showLoyaltyFailedAlert("Server error occurred.Please click on OK and try again");
                                    Log.e(TAG, "Server error occurred.");
                                    //Error
                                } else if (error instanceof NetworkError) {
                                    showLoyaltyFailedAlert("Network error occurred.Please click on OK and try again");
                                    Log.e(TAG, "Network error occurred.");
                                    //Error

                                } else if (error instanceof ParseError) {
                                    //Error
                                    showLoyaltyFailedAlert("An error occurred.Please click on OK and try again");
                                    Log.e(TAG, "An error occurred.");
                                } else {

                                    showLoyaltyFailedAlert("An error occurred.Please click on OK and try again");
                                    Log.e(TAG, "An error occurred.");
                                    //Error
                                }
                                //End


                            } catch (Exception e) {


                            }

                        }
                    }
            );
            getRequest.setRetryPolicy(new DefaultRetryPolicy(
                    6000,
                    3,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            queue.add(getRequest);
        } else {
            progressDialog.dismiss();
            AlertManager alert = new AlertManager();
            alert.alert(LoyaltyActivity.this, "No Internet Connection",
                    "Please check " +
                            "your data connection or Wifi is ON !");

        }

    }


    public void Logout(final String user, final String location) {
        Log.i(TAG, "Calling LogOut API");

        if (progressDialog != null) progressDialog.dismiss();
        try {
            handler.removeCallbacks(myRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressDialog = ProgressDialog.show(LoyaltyActivity.this, "",
                "Logging out, Please wait", true);
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                if (progressDialog != null) progressDialog.dismiss();
            }
        }, 30000);
        new AlertDialog.Builder(LoyaltyActivity.this)
                .setMessage("Are you sure you want to Logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface idialog, int id) {


                        RequestQueue queue = AppController.getInstance().getRequestQueue();
                            final String url = PATH + "logout?Location=" + location + "&Cashier=" + user;
                        Log.i(TAG, "Calling LogOut API:" + url);
                        ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                        if (net.isConnectingToInternet()) {
                            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            // display response
                                            try {
                                                String Status = response.getString("Status");
                                                String Msg = response.getString("Message");
                                                if (Status.equalsIgnoreCase("Success")) {

                                                    editor.clear();
                                                    editor.commit();
                                                    Log.i(TAG, "User Logged out");
                                                    Toast.makeText(LoyaltyActivity.this, "User Logged Out Successfully",
                                                            Toast.LENGTH_SHORT).show();

                                                    Date c = Calendar.getInstance().getTime();
                                                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                                                    final String formattedDate = df.format(c);
                                                    File logfile = Environment.getExternalStorageDirectory();
                                                    File file = new File(logfile.getAbsolutePath() + "/mPOSlog/");
                                                    File myFile = new File(logfile.getAbsolutePath() + "/mPOSlog/" + formattedDate + ".log");



                                                    /*
                                                    if(file.exists()){

                                                        File[] listFiles = file.listFiles();
                                                        long purgeTime = System.currentTimeMillis() - (21 * 24 * 60 * 60 * 1000);
                                                        for(File listFile : listFiles) {
                                                            if(listFile.lastModified() < purgeTime) {
                                                                if(!listFile.delete()) {
                                                                    System.err.println("Unable to delete file: " + listFile);
                                                                }
                                                            }
                                                        }
                                                    }
                                                    */

                                                    if (myFile.exists()) {
                                                        new Thread(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    MailSender sender = new MailSender(storeEmail,
                                                                            storePwd);

                                                                    sender.sendMail("mPoslog - " + location, "PFA mPos Log for the store " + location + ", for the date " + formattedDate,
                                                                            storeEmail, toEmail);

                                                                    if (progressDialog != null)
                                                                        progressDialog.dismiss();
                                                                    try {
                                                                        handler.removeCallbacks(myRunnable);
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    Intent i = new Intent(LoyaltyActivity.this, Login.class);
                                                                    startActivity(i);
                                                                    finish();

                                                                    //Toast.makeText(LoyaltyActivity.this, "User Logged Out Successfully",
                                                                    //      Toast.LENGTH_SHORT).show();


                                                                } catch (Exception e) {
                                                                    android.util.Log.e("SendMail", e.getMessage(), e);
                                                                    Intent i = new Intent(LoyaltyActivity.this, Login.class);
                                                                    startActivity(i);
                                                                    finish();
                                                                }
                                                            }

                                                        }).start();


                                                    } else {

                                                        if (progressDialog != null)
                                                            progressDialog.dismiss();
                                                        try {
                                                            handler.removeCallbacks(myRunnable);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        Intent i = new Intent(LoyaltyActivity.this, Login.class);
                                                        startActivity(i);
                                                        finish();
                                                        Toast.makeText(LoyaltyActivity.this, "User Logged Out Successfully",
                                                                Toast.LENGTH_SHORT).show();
                                                    }

                                                } else {
                                                    if (progressDialog != null)
                                                        progressDialog.dismiss();
                                                    try {
                                                        handler.removeCallbacks(myRunnable);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    showLoyaltyFailedAlert(Msg);
                                                    CheckDeclaration(UserID, Location);
                                                    //Intent intent = new Intent(Login.this, LoyaltyActivity.class);
                                                }

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }


                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            try {
                                                if (progressDialog != null)
                                                    progressDialog.dismiss();
                                                try {
                                                    handler.removeCallbacks(myRunnable);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                if (error instanceof TimeoutError) {
                                                    showLoyaltyFailedAlert("Time out error occurred.Please click on OK and try again");
                                                    Log.e(TAG, "Time out error occurred.");
                                                    //Time out error

                                                } else if (error instanceof NoConnectionError) {
                                                    showLoyaltyFailedAlert("Network error occurred.Please click on OK and try again");
                                                    Log.e(TAG, "Network error occurred.");
                                                    //net work error

                                                } else if (error instanceof AuthFailureError) {
                                                    showLoyaltyFailedAlert("Authentication error occurred.Please click on OK and try again");
                                                    Log.e(TAG, "Authentication error occurred.");
                                                    //error

                                                } else if (error instanceof ServerError) {
                                                    showLoyaltyFailedAlert("Server error occurred.Please click on OK and try again");
                                                    Log.e(TAG, "Server error occurred.");
                                                    //Error
                                                } else if (error instanceof NetworkError) {
                                                    showLoyaltyFailedAlert("Network error occurred.Please click on OK and try again");
                                                    Log.e(TAG, "Network error occurred.");
                                                    //Error

                                                } else if (error instanceof ParseError) {
                                                    //Error
                                                    showLoyaltyFailedAlert("An error occurred.Please click on OK and try again");
                                                    Log.e(TAG, "An error occurred.");
                                                } else {

                                                    showLoyaltyFailedAlert("An error occurred.Please click on OK and try again");
                                                    Log.e(TAG, "An error occurred.");
                                                    //Error
                                                }
                                                //End


                                            } catch (Exception e) {


                                            }

                                        }
                                    }
                            );
                            getRequest.setRetryPolicy(new DefaultRetryPolicy(
                                    6000,
                                    3,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                            queue.add(getRequest);
                        } else {
                            if (progressDialog != null) progressDialog.dismiss();
                            try {
                                handler.removeCallbacks(myRunnable);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            AlertManager alert = new AlertManager();
                            alert.alert(LoyaltyActivity.this, "No Internet Connection",
                                    "Please check " +
                                            "your data connection or Wifi is ON !");

                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                if (progressDialog != null) progressDialog.dismiss();

            }
        }).show();

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

        goBack();
    }

    private void goBack() {
        View view = LoyaltyActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

        Logout(UserID, Location);
    }

    private void deletetables() {
        Log.i(TAG, "Deleting customer details table.");
        CustomerDB cusDB = new CustomerDB(getApplicationContext());
        cusDB.open();
        cusDB.deleteCustomerTable();
        cusDB.close();

        Log.i(TAG, "Deleting Loyalty details table.");
        LoyaltyDetailsDB offrDB = new LoyaltyDetailsDB(getApplicationContext());
        offrDB.open();
        offrDB.deleteLoyaltyTable();
        offrDB.close();

        Log.i(TAG, "Deleting Loyalty Credentials details table.");
        LoyaltyCredentialsDB lyltyDB = new LoyaltyCredentialsDB(getApplicationContext());
        lyltyDB.open();
        lyltyDB.deleteLoyaltyCredentialsDetailsTable();
        lyltyDB.close();

        Log.i(TAG, "Deleting Product details table.");
        ProductDetailsDB prdctDB = new ProductDetailsDB(getApplicationContext());
        prdctDB.open();
        prdctDB.deleteUserTable();
        prdctDB.close();

        WalletDB walletDB = new WalletDB(getApplicationContext());
        walletDB.open();
        walletDB.deleteWalletTable();
        walletDB.close();
    }

}


