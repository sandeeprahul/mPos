package in.hng.mpos.activity;

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
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
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

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.hng.mpos.Database.CustomerDB;
import in.hng.mpos.Database.LoyaltyCredentialsDB;
import in.hng.mpos.Database.LoyaltyDetailsDB;
import in.hng.mpos.Database.OrderedProductDetailsDB;
import in.hng.mpos.Database.StoreDB;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.CustomerDetails;
import in.hng.mpos.gettersetter.LoyaltyDetails;
import in.hng.mpos.gettersetter.StoreDetails;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.helper.AlertManager;
import in.hng.mpos.helper.AppController;
import in.hng.mpos.helper.ConnectionDetector;
import in.hng.mpos.helper.Log;

public class ManualBillLoyaltyActivity extends AppCompatActivity {

    private AlertManager alert = new AlertManager();
    private SharedPreferences sp;
    SharedPreferences.Editor editor;
    private EditText mobile;

    private LinearLayout CusomerDetailsLL, PreviousBillLL;
    private Button search, proceed, Submit, Cancel, LoyaltyEdit, Update, UpdCancel;
    private Button btnSubmitOrder, btnCancelOrder;
    private TextView name, email, phone, points, tier;
    private TextView namepb, emailpb, phonepb, amountpb, statuspb;
    private OfferListAdapter adapter;
    private ProgressDialog progressDialog = null;
    private String StoreId, StoreOutletId, AccountId, AccountId_1, CustomerId, PATH, IPADDRESS;
    private String FirstName, LastName, Email, loyalMobile, loyalPoints, loyalTier, gender, dob, anniversary;
    private ListView listView;
    ArrayList<LoyaltyDetails> offerDetails;
    private String LoyaltyID, LoyaltyPWD, Location, UserID, TillNo, storeEmail, storePwd, toEmail;
    private PopupWindow insertpopupWindow, updatepopupwindow, OrderIDpopupwindow;
    private RelativeLayout linearLayout1;
    private RadioGroup radioGroup, radioGroupUpdate;
    private static final String TAG = "ManualBillLoyaltyActivity";
    private TextView txtOfferList;
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;
    private LinearLayout title, custSearch;
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
        setContentView(R.layout.activity_manual_bill_loyalty);

        mobile = findViewById(R.id.txtmobile);
        search = findViewById(R.id.loyalSearch);
        name = findViewById(R.id.txtname);
        email = findViewById(R.id.txtemail);
        phone = findViewById(R.id.txtloyalphone);
        points = findViewById(R.id.txtloyalppoints);
        tier = findViewById(R.id.txtloyaltier);

        namepb = findViewById(R.id.txtnamepb);
        emailpb = findViewById(R.id.txtemailpb);
        phonepb = findViewById(R.id.txtloyalphonepb);
        amountpb = findViewById(R.id.txtamountpb);
        statuspb = findViewById(R.id.txtstatuspb);

        PreviousBillLL = findViewById(R.id.lytpreviousdetails);
        CusomerDetailsLL = findViewById(R.id.lytCustDetails);
        CusomerDetailsLL.setVisibility(View.GONE);

        listView = findViewById(R.id.listloyaloffers);
        proceed = findViewById(R.id.btnproceed);
        LoyaltyEdit = findViewById(R.id.loyalEdit);
        linearLayout1 = findViewById(R.id.loyalty);
        Cancel = findViewById(R.id.btncancel);
        txtOfferList = findViewById(R.id.textView1);
        title = findViewById(R.id.lytTitle);
        custSearch = findViewById(R.id.lytCustSearch);

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
            adapter = new OfferListAdapter(ManualBillLoyaltyActivity.this, offerDetails);
            listView.setAdapter(adapter);
            Log.i(TAG, "Loyalty screen loaded");

        }

        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH = urlDB.getUrlDetails();
        IPADDRESS = urlDB.getIpUrlDetails();
        urlDB.close();

        Log.i(TAG, "Loading Manual Bill Loyalty Activity");

        StoreDB storedb = new StoreDB(getApplicationContext());
        storedb.open();
        ArrayList<StoreDetails> storeDetails = storedb.getStoreDetails();
        storedb.close();

        Log.w(TAG, "storeDetails Size##### " + storeDetails.size());

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
            adapter = new OfferListAdapter(ManualBillLoyaltyActivity.this, offerDetails);
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
                    .getColor(R.color.OrderTakingBackColor)));
            getSupportActionBar().setTitle("h&g - Assisted Order");
            isOrderTaking = true;
            Cancel.setVisibility(View.VISIBLE);
            title.setBackgroundDrawable(ContextCompat.getDrawable(ManualBillLoyaltyActivity.this, R.drawable.order_text_box_style));
            custSearch.setBackgroundDrawable(ContextCompat.getDrawable(ManualBillLoyaltyActivity.this, R.drawable.order_text_box_style));
            mobile.setBackgroundDrawable(ContextCompat.getDrawable(ManualBillLoyaltyActivity.this, R.drawable.order_text_box_style));
            Field f = null;
            try {
                f = TextView.class.getDeclaredField("mCursorDrawableRes");
                f.setAccessible(true);
                f.set(mobile, R.drawable.ordercursor);
            } catch (Exception ignored) {
            }

            CusomerDetailsLL.setBackgroundDrawable(ContextCompat.getDrawable(ManualBillLoyaltyActivity.this, R.drawable.order_text_box_style));
            listView.setBackgroundDrawable(ContextCompat.getDrawable(ManualBillLoyaltyActivity.this, R.drawable.order_text_box_style));
            proceed.setBackground(new ColorDrawable(getResources()
                    .getColor(R.color.OrderTakingBackColor)));
            proceed.setTextColor(Color.WHITE);
            Cancel.setBackground(new ColorDrawable(getResources()
                    .getColor(R.color.OrderTakingBackColor)));
            Cancel.setTextColor(Color.WHITE);
            search.setBackground(new ColorDrawable(getResources()
                    .getColor(R.color.OrderTakingBackColor)));
            search.setTextColor(Color.WHITE);
            txtOfferList.setBackground(new ColorDrawable(getResources()
                    .getColor(R.color.OrderTakingBackColor)));
            txtOfferList.setTextColor(Color.WHITE);


        }

        // if (!isOrderTaking)
        //  getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(ManualBillLoyaltyActivity.this)
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
                                editor.apply();

                                Intent intent = new Intent(ManualBillLoyaltyActivity.this, ManualBillLoyaltyActivity.class);
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

                View v = ManualBillLoyaltyActivity.this.getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                if (net.isConnectingToInternet()) {
                    progressDialog = ProgressDialog.show(ManualBillLoyaltyActivity.this, "",
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
                        adapter = new OfferListAdapter(ManualBillLoyaltyActivity.this, offerDetails);
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
                    alert.alert(ManualBillLoyaltyActivity.this, "No Internet Connection",
                            "Please check " +
                                    "your data connection or Wifi is ON !");
                }
            }

        });

        LoyaltyEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Loading Loyalty Details Updation Window");
                View v = ManualBillLoyaltyActivity.this.getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                LayoutInflater layoutInflater = (LayoutInflater) ManualBillLoyaltyActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View customView = layoutInflater.inflate(R.layout.layout_update_customer_popup, null);
                updatepopupwindow = new PopupWindow(customView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);


                Update = customView.findViewById(R.id.UpdateLyltyCustomer);
                UpdCancel = customView.findViewById(R.id.CancellyltyUpdate);

                //display the popup window
                updatepopupwindow.showAtLocation(linearLayout1, Gravity.CENTER, 0, -30);
                updatepopupwindow.setFocusable(true);
                updatepopupwindow.update();


                final TextView first_name_update = customView.findViewById(R.id.txtlyltyNameUpdate);
                final TextView last_name_update = customView.findViewById(R.id.txtlyltylastNameUpdate);
                radioGroupUpdate = customView.findViewById(R.id.radioGroupUpdate);
                radioGroupUpdate.clearCheck();

                RadioButton rbM = radioGroupUpdate.findViewById(R.id.radioButtonM);
                RadioButton rbF = radioGroupUpdate.findViewById(R.id.radioButtonF);


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
                        View view = ManualBillLoyaltyActivity.this.getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }

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
                                        Toast.makeText(ManualBillLoyaltyActivity.this, "Customer Details Updated Successfully", Toast.LENGTH_SHORT).show();
                                        updatepopupwindow.dismiss();
                                        mobile.setText(phone.getText().toString());
                                        search.performClick();


                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

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
                                            }
                                            showVolleyError(error);
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
                            }
                            AlertManager alert = new AlertManager();
                            alert.alert(ManualBillLoyaltyActivity.this, "No Internet Connection",
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
                        Intent intent = new Intent(ManualBillLoyaltyActivity.this, ManualBillBookActivity.class);
                        startActivity(intent);
                    } else {
                        Log.i(TAG, "Proceeding to order taking screen");
                        Intent intent = new Intent(ManualBillLoyaltyActivity.this, OrderTakingProcess.class);
                        startActivity(intent);
                        finish();
                    }

                } else {

                    if (!isOrderTaking) {
                        Log.i(TAG, "Proceeding to billing screen");
                        Intent intent = new Intent(ManualBillLoyaltyActivity.this, ManualBillBookActivity.class);
                        startActivity(intent);
                    } else {
                        Log.i(TAG, "Proceeding to order taking screen");
                        Intent intent = new Intent(ManualBillLoyaltyActivity.this, OrderTakingProcess.class);
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
            progressDialog = ProgressDialog.show(ManualBillLoyaltyActivity.this, "",
                    "Loading....", true);
            Log.i(TAG, "Loading Previous billDetails..");
            try {
                RequestQueue queue = AppController.getInstance().getRequestQueue();
                final String url = "http://" + IPADDRESS + "/mposbill/api/mposdata/LastbillDeatil?till_no=" + TillNo + "&location_code=" + Location;
                Log.d("Url Adress:", url);
                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // display response
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

                                    progressDialog.dismiss();

                                } catch (JSONException e) {
                                    progressDialog.dismiss();
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                progressDialog.dismiss();
                                showVolleyError(error);
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
            alert.alert(ManualBillLoyaltyActivity.this, "No Internet Connection",
                    "Please check " +
                            "your data connection or Wifi is ON !");

        }
    }

    public void Authenticate() {
        if (progressDialog != null) progressDialog.dismiss();
        try {
            handler.removeCallbacks(myRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressDialog = ProgressDialog.show(ManualBillLoyaltyActivity.this, "",
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
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
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
                            try {
                                handler.removeCallbacks(myRunnable);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
        View view = ManualBillLoyaltyActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        if (progressDialog != null) progressDialog.dismiss();
        try {
            handler.removeCallbacks(myRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressDialog = ProgressDialog.show(ManualBillLoyaltyActivity.this, "",
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
                                LayoutInflater layoutInflater = (LayoutInflater) ManualBillLoyaltyActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                View customView = layoutInflater.inflate(R.layout.loyalty_new_customer_popup, null);
                                insertpopupWindow = new PopupWindow(customView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);


                                Submit = customView.findViewById(R.id.InsertLyltyCustomer);
                                Cancel = customView.findViewById(R.id.CancellyltyInsert);

                                //instantiate popup window


                                //display the popup window

                                //insertpopupWindow.showAtLocation(linearLayout1, Gravity.CENTER, 0, -60);
                                insertpopupWindow.showAtLocation(linearLayout1, Gravity.CENTER, 0, 60);
                                //insertpopupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
                                insertpopupWindow.setFocusable(true);
                                insertpopupWindow.update();


                                final TextView first_name = customView.findViewById(R.id.txtlyltyName);
                                final TextView last_name = customView.findViewById(R.id.txtlyltylastName);
                                final TextView mobile_no = customView.findViewById(R.id.txtlyltyMobile);

                                radioGroup = customView.findViewById(R.id.radioGroup);
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
                                        View view = ManualBillLoyaltyActivity.this.getCurrentFocus();
                                        if (view != null) {
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                        }

                                        ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                                        if (net.isConnectingToInternet()) {
                                            if (gender == null) {
                                                showLoyaltyFailedAlert("Please Select Gender");
                                            } else if (mobile_no.getText().toString().isEmpty() | mobile_no.getText().toString().length() < 10) {
                                                showLoyaltyFailedAlert("Please enter valid mobile Number");
                                            } else {
                                                progressDialog = ProgressDialog.show(ManualBillLoyaltyActivity.this, "",
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
                                                            Toast.makeText(ManualBillLoyaltyActivity.this, "Customer Created Successfully", Toast.LENGTH_SHORT).show();

                                                            mobile.setText(mobile_no.getText().toString());
                                                            insertpopupWindow.dismiss();
                                                            mobile.setText(mobile_no.getText().toString());
                                                            search.performClick();
                                                            search.setEnabled(true);

                                                        } catch (Exception e) {
                                                            e.printStackTrace();
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
                                                                showVolleyError(error);
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
                                            alert.alert(ManualBillLoyaltyActivity.this, "No Internet Connection",
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
                        showVolleyError(error);
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
        progressDialog = ProgressDialog.show(ManualBillLoyaltyActivity.this, "",
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
                            }


                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    if (progressDialog != null) progressDialog.dismiss();
                    search.setEnabled(true);
                    try {
                        handler.removeCallbacks(myRunnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    showVolleyError(error);
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
            alert.alert(ManualBillLoyaltyActivity.this, "No Internet Connection",
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
            progressDialog = ProgressDialog.show(ManualBillLoyaltyActivity.this, "",
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
            CusomerDetailsLL.setVisibility(View.VISIBLE);
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
        }


    }

    public void Fetch_Loyal_Offers() {

        if (progressDialog != null) progressDialog.dismiss();
        try {
            handler.removeCallbacks(myRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressDialog = ProgressDialog.show(ManualBillLoyaltyActivity.this, "",
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
                        adapter = new OfferListAdapter(ManualBillLoyaltyActivity.this, offerDetails);
                        listView.setAdapter(adapter);
                        search.setEnabled(true);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    showVolleyError(error);
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
            alert.alert(ManualBillLoyaltyActivity.this, "No Internet Connection",
                    "Please check " +
                            "your data connection or Wifi is ON !");

        }
    }

    public ArrayList<LoyaltyDetails> readFinalData() {
        LoyaltyDetailsDB lyltyDB = new LoyaltyDetailsDB(getApplicationContext());
        lyltyDB.open();
        ArrayList<LoyaltyDetails> offers = lyltyDB.getOfferDetails();
        lyltyDB.close();
        return offers;
    }

    public void showLoyaltyFailedAlert(final String msg) {

        ManualBillLoyaltyActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, msg);
                AlertDialog.Builder builder = new AlertDialog.Builder(ManualBillLoyaltyActivity.this);
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
                                    Intent intent = new Intent(ManualBillLoyaltyActivity.this, DeclarationActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    Log.i(TAG, "No Declaration is pending");
                                    showLoyaltyFailedAlert("No Declaration is pending");                                //Intent intent = new Intent(Login.this, ManualBillLoyaltyActivity.class);
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

                            if (progressDialog != null && progressDialog.isShowing())
                                progressDialog.dismiss();

                            showVolleyError(error);
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
            alert.alert(ManualBillLoyaltyActivity.this, "No Internet Connection",
                    "Please check " +
                            "your data connection or Wifi is ON !");

        }

    }

    private void showVolleyError(VolleyError error) {
        try {

            if (error instanceof TimeoutError) {
                showLoyaltyFailedAlert("Time out error occurred.Please click on OK and try again");
                Log.e(TAG, "Time out error occurred.");

            } else if (error instanceof NoConnectionError) {
                showLoyaltyFailedAlert("Network error occurred.Please click on OK and try again");
                Log.e(TAG, "Network error occurred.");

            } else if (error instanceof AuthFailureError) {
                showLoyaltyFailedAlert("Authentication error occurred.Please click on OK and try again");
                Log.e(TAG, "Authentication error occurred.");

            } else if (error instanceof ServerError) {
                showLoyaltyFailedAlert("Server error occurred.Please click on OK and try again");
                Log.e(TAG, "Server error occurred.");

            } else if (error instanceof NetworkError) {
                showLoyaltyFailedAlert("Network error occurred.Please click on OK and try again");
                Log.e(TAG, "Network error occurred.");

            } else if (error instanceof ParseError) {
                showLoyaltyFailedAlert("An error occurred.Please click on OK and try again");
                Log.e(TAG, "An error occurred.");
            } else {
                showLoyaltyFailedAlert("An error occurred.Please click on OK and try again");
                Log.e(TAG, "An error occurred.");

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        super.onBackPressed();
        finish();
    }
}
