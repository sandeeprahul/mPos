package in.hng.mpos.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import in.hng.mpos.Adapter.AddressAdapter;
import in.hng.mpos.Database.CustomerDB;
import in.hng.mpos.Database.LoyaltyDetailsDB;
import in.hng.mpos.Database.OrderedProductDetailsDB;
import in.hng.mpos.Database.StoreDB;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.AddressInfo;
import in.hng.mpos.gettersetter.CustomerDetails;
import in.hng.mpos.gettersetter.HomeDeliveryInfo;
import in.hng.mpos.gettersetter.SearchProduct;
import in.hng.mpos.gettersetter.StoreDetails;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.helper.AlertManager;
import in.hng.mpos.helper.ApiCall;
import in.hng.mpos.helper.ConnectionDetector;
import in.hng.mpos.helper.Log;

public class ActivityDeliveryType extends BaseActivity {

    private static final String TAG = "ActivityDeliveryProcess";

    String OrderID;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    private String PATH;
    private TextView addAddress;
    private ProgressDialog idialog = null;

    //private Button pickup, Homedelivery;
    private String cust_mobile;
    private LinkedHashMap<String, HomeDeliveryInfo> addresses = new LinkedHashMap<String, HomeDeliveryInfo>();
    private ArrayList<HomeDeliveryInfo> addrList = new ArrayList<HomeDeliveryInfo>();
    private AddressAdapter listAdapter;
    ListView listView;
    ArrayList<String> selectedIds = AddressAdapter.getArrayList();
    private String slot;
    Spinner deliverySpinner;
    ArrayList<String> delivery_slot;
    private CheckBox chkHomeDelivery, chkPickup;
    private LinearLayout HomeDelivery, SavedAddr, AddrList;
    private boolean isHomedelivery = false, isPickup = false;
    private String UserID, curLocationID, pickupLocationID;

    // private ExpandableListView simpleExpandableListView;
    //ArrayList<Integer> selectedIds = AddressAdapter.getArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_delivery_type);

        setActionBarTitle("h&g Assisted Order");

        //pickup = findViewById(R.id.btnPickup);
        //Homedelivery = findViewById(R.id.btnHmeDelivery);
        // simpleExpandableListView = (ExpandableListView) findViewById(R.id.simpleExpandableListView);
        //listAdapter = new AddressAdapter(ActivityDeliveryType.this, addrList);
        //simpleExpandableListView.setAdapter(listAdapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#3c3c3c"));
        }

        listView = (in.hng.mpos.helper.NestedListView) findViewById(R.id.listv);
        addAddress = findViewById(R.id.txtAddAddr);
        deliverySpinner = findViewById(R.id.SpnrDelvSlot);
        chkHomeDelivery = findViewById(R.id.chckHome);
        chkPickup = findViewById(R.id.chckStore);
        HomeDelivery = findViewById(R.id.LytHomeDelivery);
        SavedAddr = findViewById(R.id.LytSavedAddr);
        AddrList = findViewById(R.id.ListLayout);
        //selectedIds.clear();

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        editor.apply();
        OrderID = sp.getString("OrderID", "");
        //txtOrderID = findViewById(R.id.txtorderID);
        //Resources res = getResources();
        //PATH = res.getString(R.string.API_URL);
        //txtOrderID.setText("ORDER ID: " + OrderID);
        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH = urlDB.getUrlDetails();
        urlDB.close();

        CustomerDB cusdb = new CustomerDB(getApplicationContext());
        cusdb.open();
        ArrayList<CustomerDetails> customerDetails = cusdb.getCustomerDetails();
        cusdb.close();

        if (customerDetails.size() > 0) {
            cust_mobile = customerDetails.get(0).getMobileNO();
        }

        UserDB userDB = new UserDB(getApplicationContext());
        userDB.open();
        ArrayList<UserDetails> userDetails = userDB.getUserDetails();
        userDB.close();

        if (userDetails.size() > 0) {
            curLocationID = userDetails.get(0).getStoreID();
        }

        StoreDB strDB = new StoreDB(getApplicationContext());
        strDB.open();
        ArrayList<StoreDetails> storeDetails = strDB.getStoreDetails();
        strDB.close();
        if (storeDetails.size() > 0) {
            UserID = storeDetails.get(0).getUserID();
            pickupLocationID = storeDetails.get(0).getStoreID();
        }

        if (!sp.getString("isAddAddr", "").toString().equalsIgnoreCase("")) {
            chkHomeDelivery.setChecked(true);
            String Url = PATH + "displayhomedelivery";
            makeLoadDeliverySlotsApiCall(Url);

            HomeDelivery.setVisibility(View.VISIBLE);

            isHomedelivery = true;
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String addrId = ((TextView) view.findViewById(R.id.txtaddrID)).getText().toString();
                //Integer pos = new Integer(position);
                selectedIds.clear();
                selectedIds.add(addrId);

                listView.invalidateViews();
            }
        });

        deliverySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0,
                                       View arg1, int position, long arg3) {
                slot = deliverySpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                slot = delivery_slot.get(0).toString();
            }


        });
        chkHomeDelivery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    chkPickup.setChecked(false);
                    String Url = PATH + "displayhomedelivery";
                    makeLoadDeliverySlotsApiCall(Url);

                    HomeDelivery.setVisibility(View.VISIBLE);

                    isHomedelivery = true;

                } else {
                    HomeDelivery.setVisibility(View.GONE);
                    isHomedelivery = false;
                }
            }
        });

        chkPickup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    chkHomeDelivery.setChecked(false);
                    HomeDelivery.setVisibility(View.GONE);
                    isPickup = true;

                } else {
                    //doCloseEzetap();
                    isPickup = false;
                }
            }
        });

        Log.e(TAG, "Page Loaded..");
    }

    public void AddAddress(View v) {
        editor.putString("OrderID", OrderID);
        editor.putString("isAddAddr", "Y");
        editor.commit();
        Log.e(TAG, "Navigating to AddAdressActiviy");
        Intent i = new Intent(ActivityDeliveryType.this, AddAddressActivity.class);
        startActivity(i);
        finish();

    }

    public void PlaceOrder(View v) {

        new AlertDialog.Builder(ActivityDeliveryType.this)
                .setMessage("Are you sure to place the order?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if (isHomedelivery) {

                            idialog = ProgressDialog.show(ActivityDeliveryType.this, "",
                                    "Placing Order...", true);
                            new Thread(new Runnable() {
                                public void run() {
                                    placeHomeOrder();
                                }
                            }).start();

                        } else {

                            idialog = ProgressDialog.show(ActivityDeliveryType.this, "",
                                    "Placing Order...", true);
                            new Thread(new Runnable() {
                                public void run() {

                                    placeOrder();

                                }
                            }).start();
                        }
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }


    public void placeHomeOrder() {

        String AddrID;
        if (selectedIds.size() > 0)
            AddrID = selectedIds.get(0).toString();
        else
            AddrID = "";

        if (!AddrID.equalsIgnoreCase("")) {
            JSONObject finaljsonobj = new JSONObject();
            JSONArray productarray = new JSONArray();

            try {
                JSONObject Projsonobj = new JSONObject();
                Projsonobj.put("order_id", OrderID);
                Projsonobj.put("mobile_no", cust_mobile);
                Projsonobj.put("created_by", UserID);
                Projsonobj.put("created_loc_code", curLocationID);
                Projsonobj.put("order_type", "Home Delivery");
                Projsonobj.put("shipping_add_id", AddrID);
                Projsonobj.put("status", "placed");
                Projsonobj.put("delivery_slot", slot);
                Projsonobj.put("payment_type", "");
                Projsonobj.put("payment_status", "");
                Projsonobj.put("pickup_location", pickupLocationID);
                Projsonobj.put("store_code", "");
                productarray.put(0, Projsonobj);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                finaljsonobj.put("statusCode", "200");
                finaljsonobj.put("status", "Success");
                finaljsonobj.put("details", productarray);
            } catch (Exception e) {
                e.printStackTrace();
            }
            RequestQueue requestQueue = Volley.newRequestQueue(ActivityDeliveryType.this);
            String URL = PATH + "AssistedOrder";
            final String mRequestBody = finaljsonobj.toString();
            ConnectionDetector net = new ConnectionDetector(getApplicationContext());
            if (net.isConnectingToInternet()) {
                Log.i(TAG, "Api call:" + URL);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            Log.i(TAG, "Api call response:" + response);
                            JSONObject Jsonobj = new JSONObject(response);
                            String status = Jsonobj.getString("statusCode");

                            if (status.equalsIgnoreCase("200")) {

                                ActivityDeliveryType.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityDeliveryType.this);
                                        builder.setTitle("HnG Order Taking");
                                        builder.setMessage("Your order is placed successfully")
                                                .setCancelable(false)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        clearDatabase();
                                                    }
                                                });
                                        AlertDialog alert = builder.create();
                                        alert.show();
                                    }
                                });


                            } else {
                                idialog.dismiss();
                                String msg = Jsonobj.getString("Message");
                                Log.e(TAG, msg);
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
                            idialog.dismiss();
                            showVolleyError(TAG, error);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, "Exception <<::>> " + e);
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
                idialog.dismiss();
                Log.e(TAG, "No Internet");
                AlertManager alert = new AlertManager();
                alert.alert(ActivityDeliveryType.this, "No Internet Connection",
                        "Please check " +
                                "your data connection or Wifi is ON !");

            }
        } else {
            idialog.dismiss();
            showFailedAlert("Please provide address");
        }

    }

    public void placeOrder() {

        JSONObject finaljsonobj = new JSONObject();
        JSONArray productarray = new JSONArray();

        try {
            JSONObject Projsonobj = new JSONObject();
            Projsonobj.put("order_id", OrderID);
            Projsonobj.put("mobile_no", cust_mobile);
            Projsonobj.put("created_by", UserID);
            Projsonobj.put("created_loc_code", curLocationID);
            Projsonobj.put("order_type", "pickup");
            Projsonobj.put("shipping_add_id", "");
            Projsonobj.put("status", "placed");
            Projsonobj.put("delivery_slot", "");
            Projsonobj.put("payment_type", "");
            Projsonobj.put("payment_status", "");
            Projsonobj.put("pickup_location", pickupLocationID);
            Projsonobj.put("store_code", "");
            productarray.put(0, Projsonobj);

            finaljsonobj.put("statusCode", "200");
            finaljsonobj.put("status", "Success");
            finaljsonobj.put("details", productarray);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "placeOrder() Exception <<::>> " + e);
        }

        RequestQueue requestQueue = Volley.newRequestQueue(ActivityDeliveryType.this);
        String URL = PATH + "AssistedOrder";
        final String mRequestBody = finaljsonobj.toString();
        ConnectionDetector net = new ConnectionDetector(getApplicationContext());
        if (net.isConnectingToInternet()) {
            Log.i(TAG, "Api call:" + URL);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        Log.i(TAG, "Api call response:" + response);
                        JSONObject Jsonobj = new JSONObject(response);
                        String status = Jsonobj.getString("statusCode");

                        if (status.equalsIgnoreCase("200")) {

                            ActivityDeliveryType.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityDeliveryType.this);
                                    builder.setTitle("HnG Order Taking");
                                    builder.setMessage("Your order is placed successfully")
                                            .setCancelable(false)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    clearDatabase();
                                                }
                                            });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            });


                        } else {
                            idialog.dismiss();
                            String msg = Jsonobj.getString("Message");
                            Log.e(TAG, msg);
                            showFailedAlert(msg);
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
                        showVolleyError(TAG, error);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "VolleyError Exception <<::>> " + e);
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
            idialog.dismiss();
            Log.e(TAG, "No Internet");
            AlertManager alert = new AlertManager();
            alert.alert(ActivityDeliveryType.this, "No Internet Connection",
                    "Please check " +
                            "your data connection or Wifi is ON !");
        }
    }

    private void clearDatabase() {

        Log.w(TAG, "Clearing Database... ");

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

        Log.w(TAG, "Database cleared... ");

        Intent intent = new Intent(ActivityDeliveryType.this, LoyaltyActivity.class);
        startActivity(intent);
        finish();
    }

    private void makeFetchAddressApiCall(String urlPath) {
        Log.i(TAG, "Api call:" + urlPath);
        ApiCall.make(this, urlPath, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.i(TAG, "Api call response:" + response);
                            JSONObject responseObject = new JSONObject(response);
                            String status = responseObject.getString("statusCode");
                            if (status.equalsIgnoreCase("200")) {
                                ArrayList<AddressInfo> addrList = null;
                                JSONArray address = responseObject.getJSONArray("details");
                                addrList = new ArrayList<AddressInfo>();
                                for (int i = 0; i < address.length(); i++) {
                                    JSONObject json_address;
                                    AddressInfo addresses = new AddressInfo();
                                    json_address = address.getJSONObject(i);
                                    addresses.setAddrID(json_address.optString("ID"));
                                    addresses.setAddr(json_address.optString("ADDRESS"));
                                    addresses.setAddrType(json_address.optString("ADDRESS_TYPE"));
                                    addresses.setIsDefault(json_address.optString("DEFAULT_FLAG"));
                                    if (json_address.optString("DEFAULT_FLAG").equalsIgnoreCase("True")) {
                                        selectedIds.clear();
                                        selectedIds.add(json_address.optString("ID"));
                                    }
                                    addrList.add(addresses);

                                    if (addrList.size() <= 0) {
                                        SavedAddr.setVisibility(View.GONE);
                                        AddrList.setVisibility(View.GONE);
                                    }
                                }

                                listAdapter = new AddressAdapter(ActivityDeliveryType.this, addrList);
                                listView.setAdapter(listAdapter);

                            } else {
                                SavedAddr.setVisibility(View.GONE);
                                AddrList.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "JSON Exception <<::>> " + e);
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley Error <<::>> " + error);
                    }
                }
        );

    }

    private void makeLoadDeliverySlotsApiCall(String urlPath) {
        Log.i(TAG, "Api call:" + urlPath);
        ApiCall.make(this, urlPath, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.i(TAG, "Api call response:" + response);
                            JSONObject responseObject = new JSONObject(response);
                            String status = responseObject.getString("statusCode");
                            if (status.equalsIgnoreCase("200")) {
                                JSONArray products = responseObject.getJSONArray("details");
                                delivery_slot = new ArrayList<String>();
                                for (int i = 0; i < products.length(); i++) {
                                    SearchProduct product = new SearchProduct();
                                    JSONObject json_products;
                                    json_products = products.getJSONObject(i);
                                    delivery_slot.add(json_products.optString("deliveryslots"));
                                }

                                deliverySpinner.setAdapter(new ArrayAdapter<String>(ActivityDeliveryType.this, android.R.layout.simple_spinner_dropdown_item,
                                        delivery_slot));

                                String Url = PATH + "Assistedvalidatemobile?mobileno=" + cust_mobile; /*+ cust_mobile;*/
                                makeFetchAddressApiCall(Url);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley Error <<::>> " + error);
                    }
                }
        );

    }


    public void goBack() {
        Log.i(TAG, "Clicked Back button");
        editor.putString("OrderID", OrderID);
        editor.commit();
        Intent i = new Intent(ActivityDeliveryType.this, OrderTakingProcess.class);
        startActivity(i);
        finish();
    }
    //here we maintain our products in various departments

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(ActivityDeliveryType.this)
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

    public void showFailedAlert(final String msg) {

        ActivityDeliveryType.this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityDeliveryType.this);
                builder.setTitle("HnG Order Taking");
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
}