package in.hng.mpos.MarketPlace;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import in.hng.mpos.Database.CustomerDB;
import in.hng.mpos.Database.LoyaltyDetailsDB;
import in.hng.mpos.Database.ProductDetailsDB;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.Adapter.ListAdapter;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.CustomerDetails;
import in.hng.mpos.gettersetter.MrpList;
import in.hng.mpos.gettersetter.ProductList;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.helper.AlertManager;
import in.hng.mpos.helper.AppController;
import in.hng.mpos.helper.ConnectionDetector;
import in.hng.mpos.helper.JSONParser;
import in.hng.mpos.helper.Log;

public class ScanProducts extends AppCompatActivity {
    private String codeFormat, codeContent, path, details_url, sku_name, mrp, qty, statusCode, mrp_ind;
    private String LocationCode, storeSKU, skuCode, TAXcode, TAXrate, EANcode, CashierCode, PATH, eanCode;
    private Integer r_count;
    ListAdapter adapter;
    Button add, process, cancelItem, addItem, updateQty, cancelUpdate, EanSearch;
    EditText couponCode;
    Button applyCoupon;
    JSONParser jParser = new JSONParser();
    JSONArray jsonarray, jsonarray_product;
    ListView listView;
    ArrayList<ProductList> skuList;
    ArrayList<MrpList> mrp_list;
    ArrayList<String> mrp_spnList;
    AlertManager alert = new AlertManager();
    String Msg, Location, tillNo;
    LinearLayout popup;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    TextView updateItemname;
    String billno = "";
    String vendorName="";
    EditText updadeItemQty, Ean;
    PopupWindow popupWindow, deletepopup, couponpopup;
    LinearLayout linearLayout1;
    String item_name, item_qty, item_storeSKU, item_Mrp;
    View customView;
    Spinner mySpinner;
    private static final String TAG = "ScanProduct";
    private ProgressDialog dialog = null;
    private Handler handler = new Handler();
    Runnable myRunnable;
    String URL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_products);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        editor.commit();
        vendorName=sp.getString("vendor_name", "");
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
        title.setText("h&g mPOS - Billing("+vendorName+")");
        title.setVisibility(View.VISIBLE);

        process = findViewById(R.id.btn_market_scan_process);
        Ean = findViewById(R.id.txtean);
        EanSearch = findViewById(R.id.EanSearch);
        listView = (in.hng.mpos.helper.NestedListView) findViewById(R.id.listv);
        linearLayout1 = findViewById(R.id.linearLayout1);
        Button fab = findViewById(R.id.fab);


        //instantiate the popup.xml layout file
        LayoutInflater layoutInflater = (LayoutInflater) ScanProducts.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        customView = layoutInflater.inflate(R.layout.popup, null);
        cancelItem = customView.findViewById(R.id.CancelItem);
        addItem = customView.findViewById(R.id.AddItem);
        mySpinner = customView.findViewById(R.id.SpnrMrp);

        //instantiate popup window
        popupWindow = new PopupWindow(customView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        couponpopup = new PopupWindow(customView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);


        UserDB userDB = new UserDB(getApplicationContext());
        userDB.open();
        ArrayList<UserDetails> userDetailses = userDB.getUserDetails();
        userDB.close();


        Location = userDetailses.get(0).getStoreID();
        tillNo = userDetailses.get(0).getTillNo();
        //Ean.requestFocus();


        ProductDetailsDB productDB = new ProductDetailsDB(getApplicationContext());
        productDB.open();
        ArrayList<ProductList> products = productDB.getEANDetails();
        productDB.close();
        billno = sp.getString("Bill_no", "");
        if (products.size() > 0) {

            skuList = readFinalData();
            adapter = new ListAdapter(ScanProducts.this, skuList);
            listView.setAdapter(adapter);
            Log.i(TAG, "Scanning screen loaded.");

        }

        //Resources res = getResources();
        //PATH = res.getString(R.string.API_URL);

        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH =urlDB.getUrlDetails();
        urlDB.close();

        Ean.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                View view = ScanProducts.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                Log.i(TAG, "Searching Ean..");

                if (!popupWindow.isShowing()) {
                    if (Ean.getText().length() >= 6) {
                        ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                        if (net.isConnectingToInternet()) {

                            URL = "http://10.46.16.18/selfcheck_mkt/api/mposmarketplace/geteandetail_mkt";
                            makeLoadProductDetailAPI(URL,Ean.getText().toString(),Location);
                            //LoadProductDetails(Ean.getText().toString());
                        } else {
                            AlertManager alert = new AlertManager();
                            alert.alert(ScanProducts.this, "No Internet Connection",
                                    "Please check " +
                                            "your data connection or Wifi is ON !");
                        }
                    } else {

                        Toast toast = Toast.makeText(getApplicationContext(), "Please enter valid EAN.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else {

                    Toast toast = Toast.makeText(getApplicationContext(), "Please select MRP", Toast.LENGTH_SHORT);
                    toast.show();
                    Ean.setText("");
                    Ean.requestFocus();

                }

                Ean.setText("");
                return false;
            }

        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                if (net.isConnectingToInternet()) {
                    if (!popupWindow.isShowing()) {
                        Ean.setText("");
                        Log.i(TAG, "Scanning Product");
                        IntentIntegrator integrator = new IntentIntegrator(ScanProducts.this);
                        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
                        //integrator.setResultDisplayDuration(0);
                        //integrator.setWide();  // Wide scanning rectangle, may work better for 1D barcodes
                        integrator.setCameraId(0);  // Use a specific camera of the device
                        integrator.initiateScan();
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "Please select MRP", Toast.LENGTH_SHORT);
                        toast.show();
                        Ean.setText("");
                        Ean.requestFocus();
                    }

                } else {
                    Log.e(TAG, "No Internet.");
                    AlertManager alert = new AlertManager();
                    alert.alert(ScanProducts.this, "No Internet Connection",
                            "Please check " +
                                    "your data connection or Wifi is ON !");

                }

            }
        });

        process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog = ProgressDialog.show(ScanProducts.this, "",
                        "Processing Bill...", true);

                Log.i(TAG,"Processing Bill...");

                handler = new Handler();

                handler.postDelayed(myRunnable=new Runnable() {
                    @Override
                    public void run() {

                        if(dialog!=null) dialog.dismiss();
                    }
                }, 30000);


                if (!popupWindow.isShowing()) {
                    ProductDetailsDB prodb = new ProductDetailsDB(getApplicationContext());
                    prodb.open();
                    ArrayList<ProductList> items = prodb.getEANDetails();
                    prodb.close();
                    if (items.size() < 1) {
                        if(dialog!=null) dialog.dismiss();
                        try {
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        showFailedAlert("Please add products to process");
                    } else {

                        Log.i(TAG, "Checking for offers");
                        LoyaltyDetailsDB lyltydb = new LoyaltyDetailsDB(getApplicationContext());
                        lyltydb.open();
                        ArrayList<in.hng.mpos.gettersetter.LoyaltyDetails> offerList = lyltydb.checkOfferList();
                        lyltydb.close();


                            Log.i(TAG, "Calling Bill_log API");
                            UserDB userdb = new UserDB(getApplicationContext());
                            userdb.open();
                            ArrayList<UserDetails> userdata = userdb.getUserDetails();

                            userdb.close();
                            CustomerDB custmrdb = new CustomerDB(getApplicationContext());
                            custmrdb.open();
                            ArrayList<CustomerDetails> customerdata = custmrdb.getCustomerDetails();
                            custmrdb.close();
                            JSONObject finaljsonobj = new JSONObject();
                            JSONArray Headerjsonarray = new JSONArray();
                            JSONArray productarray = new JSONArray();
                            JSONArray offerarray = new JSONArray();
                            JSONArray offercodearray = new JSONArray();
                            try {
                                JSONObject Headerobj = new JSONObject();
                                Headerobj.put("billno", "");
                                Headerobj.put("LocationCode", userdata.get(0).getStoreID());
                                Headerobj.put("order_no", sp.getString("order_id",""));
                                Headerobj.put("CasherCode", userdata.get(0).getUserID());
                                Headerobj.put("customer_bill_typ", sp.getString("vendor_code",""));
                                Headerobj.put("tillNo", tillNo);
                                Headerjsonarray.put(0, Headerobj);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            ProductDetailsDB podb = new ProductDetailsDB(getApplicationContext());
                            podb.open();
                            ArrayList<ProductList> products = podb.getEANDetails();
                            podb.close();
                            if (products.size() > 0) {
                                for (int i = 0; i < products.size(); i++) {

                                    try {
                                        JSONObject Projsonobj = new JSONObject();
                                        int j = 0;
                                        j = i + 1;
                                        Projsonobj.put("LocationCode", userdata.get(0).getStoreID());
                                        Projsonobj.put("StoreSkuLocNo", products.get(i).getStoreSKU());
                                        Projsonobj.put("SkuQty", products.get(i).getQty());
                                        Projsonobj.put("MRP", products.get(i).getMrp());
                                        Projsonobj.put("TaxCode", products.get(i).getTaxCode());
                                        Projsonobj.put("TaxRate", products.get(i).getTaxRate());
                                        Projsonobj.put("EanCode", products.get(i).getEanCode());
                                        Projsonobj.put("CasherCode", userdata.get(0).getUserID());
                                        Projsonobj.put("tillNo", tillNo);
                                        productarray.put(i, Projsonobj);


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }


                                try {
                                    JSONObject Offrjsonobj = new JSONObject();
                                    int j = 0;
                                    Offrjsonobj.put("MOBILE_NO", "");
                                    Offrjsonobj.put("CUST_ID", "");
                                    Offrjsonobj.put("CUST_OFFER_ID", "");
                                    Offrjsonobj.put("OFFER_DESC", "");
                                    Offrjsonobj.put("FROM_TIME", "");
                                    Offrjsonobj.put("TO_TIME", "");
                                    Offrjsonobj.put("VALID_FROM", "");
                                    Offrjsonobj.put("VALID_TO", "");
                                    Offrjsonobj.put("NAME", "");
                                    Offrjsonobj.put("OFFER_CODE", "");
                                    Offrjsonobj.put("OFFER_ID", "");
                                    Offrjsonobj.put("OFFER_TYPE", "");
                                    Offrjsonobj.put("OFFER_VALID_DAYS", "");
                                    Offrjsonobj.put("OFFER_VALUE", "");
                                    Offrjsonobj.put("OFFER_VALUE_TYPE", "");
                                    Offrjsonobj.put("OUTLET_NAME", "");
                                    Offrjsonobj.put("SKU_CODE", "");
                                    Offrjsonobj.put("PURCHASE_VAL1", "");
                                    Offrjsonobj.put("PURCHASE_VAL2", "");
                                    Offrjsonobj.put("STORE_NAME", "");
                                    Offrjsonobj.put("LOCATION_CODE", "");
                                    offerarray.put(0, Offrjsonobj);


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                try {

                                    JSONObject OffrCodejsonobj = new JSONObject();
                                    int j = 0;
                                    OffrCodejsonobj.put("OfferID", "");
                                    offercodearray.put(0, OffrCodejsonobj);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                                try {
                                    finaljsonobj.put("detailLog", productarray);
                                    finaljsonobj.put("header", Headerjsonarray);
                                 } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
                                String URL = "http://10.46.16.18/selfcheck_mkt/api/mposmarketplace/billdetaillog_mkt";
                                final String mRequestBody = finaljsonobj.toString();
                                ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                                if (net.isConnectingToInternet()) {
                                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {

                                            try {

                                                JSONObject Jsonobj = new JSONObject(response);
                                                String status = Jsonobj.getString("statusCode");

                                                if (status.equalsIgnoreCase("200")) {
                                                    Log.i(TAG, "Billlog API:Success");
                                                    billno = Jsonobj.getString("bill_no");

                                                    JSONArray refershLog = Jsonobj.getJSONArray("refershLog");
                                                    ProductDetailsDB podb = new ProductDetailsDB(getApplicationContext());
                                                    podb.open();
                                                    podb.deleteUserTable();
                                                    podb.createProductDetailsTable();
                                                    podb.insertBulkPOSKUDetails(refershLog);
                                                    podb.close();
                                                    skuList = readFinalData();
                                                    adapter = new ListAdapter(ScanProducts.this, skuList);
                                                    listView.setAdapter(adapter);

                                                    UserDB userdb = new UserDB(getApplicationContext());
                                                    userdb.open();
                                                    ArrayList<UserDetails> userdata = userdb.getUserDetails();
                                                    userdb.close();
                                                    editor.putString("Bill_no", Jsonobj.getString("bill_no"));
                                                    editor.putString("Location_code", userdata.get(0).getStoreID());
                                                    editor.putString("Cashier_code", userdata.get(0).getUserID());
                                                    editor.putString("Loyalty_Disc", Jsonobj.getString("LoyaltyDisc"));
                                                    editor.putString("Total_Amt", Jsonobj.getString("Bill_Value"));
                                                    editor.putString("Disc_Amt", Jsonobj.getString("Discount"));
//                                                    editor.putString("bbPromo", Jsonobj.getString("bbPromo"));
//                                                    editor.putString("Promo_Txt", Jsonobj.getString("PromoTxt"));
                                                    editor.putString("Coupon_Disc", "0.00");
                                                    editor.commit();
                                                    if(dialog!=null) dialog.dismiss();
                                                    try {
                                                        handler.removeCallbacks(myRunnable);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    Intent intent = new Intent(ScanProducts.this, MarketOrderProcess.class);
                                                    startActivity(intent);
                                                    finish();

                                                    Log.d("Bill_log API response", Jsonobj.toString());
                                                } else {
                                                    if(dialog!=null) dialog.dismiss();
                                                    try {
                                                        handler.removeCallbacks(myRunnable);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
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
                                                if(dialog!=null) dialog.dismiss();
                                                try {
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
                                    if(dialog!=null) dialog.dismiss();
                                    try {
                                        handler.removeCallbacks(myRunnable);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    Log.e(TAG, "No Internet");
                                    AlertManager alert = new AlertManager();
                                    alert.alert(ScanProducts.this, "No Internet Connection",
                                            "Please check " +
                                                    "your data connection or Wifi is ON !");

                                }
                            }



                    }
                } else {
                    if(dialog!=null) dialog.dismiss();
                    try {
                        handler.removeCallbacks(myRunnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast toast = Toast.makeText(getApplicationContext(), "Please select MRP", Toast.LENGTH_SHORT);
                    toast.show();
                    Ean.setText("");
                    Ean.requestFocus();
                }

            }

        });


        EanSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View v = ScanProducts.this.getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                Log.i(TAG, "Searching Ean..");
                if (!popupWindow.isShowing()) {

                    if (Ean.getText().length() >= 6) {
                        ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                        if (net.isConnectingToInternet()) {

                            URL = "http://10.46.16.18/selfcheck_mkt/api/mposmarketplace/geteandetail_mkt";
                            makeLoadProductDetailAPI(URL, Ean.getText().toString(), Location);
                            //LoadProductDetails(Ean.getText().toString());
                        } else {
                            AlertManager alert = new AlertManager();
                            alert.alert(ScanProducts.this, "No Internet Connection",
                                    "Please check " +
                                            "your data connection or Wifi is ON !");

                        }

                    } else {

                        Toast toast = Toast.makeText(getApplicationContext(), "Please enter valid EAN.", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please select MRP", Toast.LENGTH_SHORT);
                    toast.show();
                    Ean.setText("");
                    Ean.requestFocus();
                }
            }
        });


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {

                LayoutInflater layoutInflater = (LayoutInflater) ScanProducts.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View customView = layoutInflater.inflate(R.layout.delete_item_popup, null);
                cancelUpdate = customView.findViewById(R.id.CancelUpdate);
                updateQty = customView.findViewById(R.id.UpdateQty);
                updateItemname = customView.findViewById(R.id.txtupdateitemname);
                updadeItemQty = customView.findViewById(R.id.txtUpdateQty);

                //instantiate popup window
                deletepopup = new PopupWindow(customView, Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
                //display the popup window
                deletepopup.showAtLocation(listView, Gravity.CENTER_VERTICAL, 0, 30);
                deletepopup.setFocusable(true);
                deletepopup.update();
                skuList = readFinalData();
                // Integer position = Integer.valueOf(pos);


                item_name = skuList.get(pos).getSkuName();
                item_qty = skuList.get(pos).getQty();
                item_storeSKU = skuList.get(pos).getStoreSKU();
                item_Mrp = skuList.get(pos).getMrp();
                updateItemname.setText(item_name);
                updadeItemQty.setText(item_qty);


                updateQty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Integer up_qty = Integer.parseInt(updadeItemQty.getText().toString());
                        Integer qty = Integer.parseInt(item_qty);

                        if (!updadeItemQty.getText().toString().isEmpty()) {

                            if (up_qty > qty) {
                                Toast toast = Toast.makeText(getApplicationContext(), "Increment of Item Quantity is not allowed", Toast.LENGTH_SHORT);
                                toast.show();
                            } else {
                                ProductDetailsDB podb = new ProductDetailsDB(getApplicationContext());
                                podb.open();
                                if (updadeItemQty.getText().toString().equalsIgnoreCase("0")) {
                                    podb.deleteProduct(item_storeSKU, item_Mrp);
                                } else {
                                    podb.updateProductQty(item_storeSKU, item_Mrp, updadeItemQty.getText().toString());
                                }
                                podb.close();

                                skuList = readFinalData();
                                adapter = new ListAdapter(ScanProducts.this, skuList);
                                listView.setAdapter(adapter);
                                deletepopup.dismiss();
                            }
                        } else {
                            showFailedAlert("Please enter valid quantity");
                        }
                    }
                });

                //close the popup window on button click
                cancelUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deletepopup.dismiss();
                    }
                });


                return true;
            }
        });

    }

    private void makeLoadProductDetailAPI(String urlPath, String eanCode, String location) {
JSONObject finaljsonobj = new JSONObject();
        try {
            finaljsonobj.put("ean_code", eanCode);
            finaljsonobj.put("location_code", location);

        } catch (Exception e) {
            e.printStackTrace();
        }


        dialog = ProgressDialog.show(ScanProducts.this, "",
                "Loading product Details, Please Wait...", true);

        EanSearch.setEnabled(false);

        handler = new Handler();
        handler.postDelayed(myRunnable=new Runnable() {
            @Override
            public void run() {

                if(dialog!=null) dialog.dismiss();
            }
        }, 30000);
        final String mRequestBody = finaljsonobj.toString();
        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
        StringRequest stringRequest = new StringRequest(Request.Method.POST,urlPath, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            statusCode = responseObject.getString("statusCode");
                            if (statusCode.equalsIgnoreCase("200")) {
                                if(dialog!=null) dialog.dismiss();
                                try {
                                    handler.removeCallbacks(myRunnable);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                mrp_ind = responseObject.getString("multiplemrp");
                                jsonarray_product = responseObject.getJSONArray("product");
                                JSONObject json_product;
                                json_product = jsonarray_product.getJSONObject(0);
                                qty = "1";
                                sku_name = json_product.getString("SKU_NAME");
                                skuCode = json_product.getString("SKU_CODE");
                                TAXcode = json_product.getString("TAX_CODE");
                                TAXrate = json_product.getString("TAX_RATE");
                                EANcode = json_product.getString("ean_code");
                                if (mrp_ind.equalsIgnoreCase("Y")) {
                                    jsonarray = responseObject.getJSONArray("batch");
                                    mrp_list = new ArrayList<MrpList>();
                                    mrp_spnList = new ArrayList<String>();
                                    for (int i = 0; i < jsonarray.length(); i++) {
                                        JSONObject json_mrps;
                                        json_mrps = jsonarray.getJSONObject(i);
                                        MrpList mrpList = new MrpList();
                                        mrpList.setMrp(json_mrps.optString("MRP"));
                                        mrpList.setStoreSKU(json_mrps.optString("STORE_SKU_LOC_STOCK_NO"));
                                        mrp_list.add(mrpList);
                                        mrp_spnList.add(json_mrps.optString("MRP"));
                                    }
                                    Ean.setText("");
                                    Ean.requestFocus();
                                    //display the popup window
                                    popupWindow.showAtLocation(linearLayout1, Gravity.CENTER, 0, 40);
                                    TextView item_name = customView.findViewById(R.id.txtitemname);
                                    item_name.setText(sku_name);
                                    mySpinner.setAdapter(new ArrayAdapter<String>(ScanProducts.this, android.R.layout.simple_spinner_dropdown_item,
                                            mrp_spnList));
                                    mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                                        @Override
                                        public void onItemSelected(AdapterView<?> arg0,
                                                                   View arg1, int position, long arg3) {
                                            storeSKU = mrp_list.get(position).getStoreSKU();
                                            mrp = mrp_list.get(position).getMrp();
                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> parent) {
                                            mrp = mrp_list.get(0).getMrp();
                                            storeSKU = mrp_list.get(0).getStoreSKU();
                                        }


                                    });

                                    addItem.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            writeData(sku_name, skuCode, storeSKU, mrp, TAXcode, TAXrate, EANcode);
                                            skuList = readFinalData();
                                            adapter = new ListAdapter(ScanProducts.this, skuList);
                                            listView.setAdapter(adapter);
                                            popupWindow.dismiss();

                                        }
                                    });


                                    //close the popup window on button click
                                    cancelItem.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            popupWindow.dismiss();
                                        }
                                    });


                                } else {
                                    jsonarray = responseObject.getJSONArray("batch");
                                    JSONObject json_mrp;
                                    json_mrp = jsonarray.getJSONObject(0);
                                    storeSKU = json_mrp.optString("STORE_SKU_LOC_STOCK_NO");
                                    mrp = json_mrp.optString("MRP");
                                    writeData(sku_name, skuCode, storeSKU, mrp, TAXcode, TAXcode, EANcode);
                                    skuList = readFinalData();
                                    adapter = new ListAdapter(ScanProducts.this, skuList);
                                    listView.setAdapter(adapter);
                                    //dialog.dismiss();
                                    // handler.removeCallbacks(myRunnable);
                                    EanSearch.setEnabled(true);
                                    Ean.setText("");
                                    Ean.requestFocus();


                                }
                                EanSearch.setEnabled(true);

                            } else {

                                Msg = responseObject.getString("Message");
                                if(dialog!=null) dialog.dismiss();
                                try {
                                    handler.removeCallbacks(myRunnable);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                EanSearch.setEnabled(true);
                                showFailedAlert(Msg);

                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        try {
                            if(dialog!=null) dialog.dismiss();
                            EanSearch.setEnabled(true);
                            try {
                                try {
                                    handler.removeCallbacks(myRunnable);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
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
                    // VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
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






    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult.getContents() != null) {
            codeContent = scanningResult.getContents();
            ConnectionDetector net = new ConnectionDetector(getApplicationContext());
            if (net.isConnectingToInternet()) {

                URL = "http://10.46.16.18/selfcheck_mkt/api/mposmarketplace/geteandetail_mkt";
                makeLoadProductDetailAPI(URL, Ean.getText().toString(), Location);
                //LoadProductDetails(codeContent);
            } else {
                AlertManager alert = new AlertManager();
                alert.alert(ScanProducts.this, "No Internet Connection",
                        "Please check " +
                                "your data connection or Wifi is ON !");
            }

        } else {

            Toast toast = Toast.makeText(getApplicationContext(), "No items scanned!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    public void writeData(String skuName, String skuCode, String storeSKU, String mrp, String taxCode, String taxRate, String eanCode) {

        try {
            HashMap<String, String> ProductDetails = new HashMap<String, String>();
            ProductDetails.put("skuName", skuName);
            ProductDetails.put("skuCode", skuCode);
            ProductDetails.put("storeSKU", storeSKU);
            ProductDetails.put("Qty", "1");
            ProductDetails.put("Mrp", mrp);
            ProductDetails.put("taxCode", taxCode);
            ProductDetails.put("taxRate", taxRate);
            ProductDetails.put("eanCode", eanCode);

            ProductDetailsDB podb = new ProductDetailsDB(getApplicationContext());
            podb.open();
            ArrayList<ProductList> poList = podb.checkProductList(storeSKU, mrp);
            if (poList.size() > 0) {
                podb.updateProductList(ProductDetails);
            } else {
                podb.insertProductDetails(ProductDetails);
            }

            podb.close();
            // Toast.makeText(getApplicationContext(), "Data saved successfully", Toast.LENGTH_SHORT).show();
            Toast toast = Toast.makeText(ScanProducts.this, "Item added successfully", Toast.LENGTH_SHORT);
            toast.show();
            Ean.requestFocus();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ProductList> readFinalData() {
        ProductDetailsDB podb = new ProductDetailsDB(getApplicationContext());
        podb.open();
        ArrayList<ProductList> skuList = podb.getEANDetails();

        podb.close();

        return skuList;

    }

    public void goBack(){
        Intent i = new Intent(ScanProducts.this, OrderInfromation.class);
        startActivity(i);
        finish();

    }
    //here we maintain our products in various departments


    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(ScanProducts.this)
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



    @Override
    public void onDestroy () {

        try {
            handler.removeCallbacks(myRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onDestroy ();

    }

    public void showFailedAlert(final String msg) {

        ScanProducts.this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ScanProducts.this);
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
}
