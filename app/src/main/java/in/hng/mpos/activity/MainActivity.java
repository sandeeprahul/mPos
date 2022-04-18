package in.hng.mpos.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import in.hng.mpos.Utils.Constants;
import in.hng.mpos.gettersetter.CustomerDetails;
import in.hng.mpos.gettersetter.MrpList;
import in.hng.mpos.gettersetter.ProductList;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.helper.AlertManager;
import in.hng.mpos.helper.ApiCall;
import in.hng.mpos.helper.AppController;
import in.hng.mpos.helper.ConnectionDetector;
import in.hng.mpos.helper.JSONParser;
import in.hng.mpos.helper.Log;

public class MainActivity extends BaseActivity {

    private static final String TAG = "Scanning Product";

    Button process, cancelItem, addItem, updateQty, cancelUpdate, EanSearch;
    TextView updateItemname;
    ListView listView;
    EditText updadeItemQty, Ean;
    PopupWindow popupWindow, deletepopup, couponpopup;
    LinearLayout linearLayout1;
    View customView;
    Spinner mySpinner;
    ListAdapter adapter;

    JSONArray jsonarray, jsonarray_product;
    ArrayList<ProductList> skuList;
    ArrayList<MrpList> mrp_list;
    ArrayList<String> mrp_spnList;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    String Msg, Location, tillNo;
    String billno = "";
    String item_name, item_qty, item_storeSKU, item_Mrp;
    private String codeFormat, codeContent, path, details_url, sku_name, mrp, qty, statusCode, mrp_ind;
    private String LocationCode, storeSKU, skuCode, TAXcode, TAXrate, EANcode, CashierCode, PATH, eanCode;
    private Integer r_count;

    private ProgressDialog dialog = null;
    private Handler handler = new Handler();
    Runnable myRunnable;
    String URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        editor.apply();

        setActionBarTitle("h&g mPOS - Billing");

        process = findViewById(R.id.Process);
        Ean = findViewById(R.id.txtean);
        EanSearch = findViewById(R.id.EanSearch);
        listView = (in.hng.mpos.helper.NestedListView) findViewById(R.id.listv);
        linearLayout1 = findViewById(R.id.linearLayout1);
        Button fab = findViewById(R.id.fab);

        //instantiate the popup.xml layout file
        LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
        Ean.requestFocus();

        ProductDetailsDB productDB = new ProductDetailsDB(getApplicationContext());
        productDB.open();
        ArrayList<ProductList> products = productDB.getEANDetails();
        productDB.close();
        billno = sp.getString("Bill_no", "");
        if (products.size() > 0) {

            skuList = readFinalData();
            adapter = new ListAdapter(MainActivity.this, skuList);
            listView.setAdapter(adapter);
            Log.i(TAG, "Scanning screen loaded.");

        }

        //Resources res = getResources();
        //PATH = res.getString(R.string.API_URL);

        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH = urlDB.getUrlDetails();
        Log.d(TAG, "URL PATH ==========> " + PATH);
        urlDB.close();

        Ean.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                hideKeyboard();

                Log.i(TAG, "Searching Ean..");

                if (!popupWindow.isShowing()) {
                    if (Ean.getText().length() >= 6) {
                        ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                        if (net.isConnectingToInternet()) {

                            URL = PATH + "geteandetail?ean_code=" + Ean.getText().toString() + "&location=" + Location;
                            Log.i(TAG, "Api Call: " + URL);
                            makeLoadProductDetailAPI(URL);
                            //LoadProductDetails(Ean.getText().toString());
                        } else {
                            AlertManager alert = new AlertManager();
                            alert.alert(MainActivity.this, "No Internet Connection",
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

                        try {
                            Ean.setText("");
                            Log.i(TAG, "Scanning Product");
                            IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                            integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
                            //integrator.setResultDisplayDuration(0);
                            //integrator.setWide();  // Wide scanning rectangle, may work better for 1D barcodes
                            integrator.setCameraId(0);  // Use a specific camera of the device
                            integrator.initiateScan();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Log.e(TAG, "Exception <<::>> " + ex);
                        }
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "Please select MRP", Toast.LENGTH_SHORT);
                        toast.show();
                        Ean.setText("");
                        Ean.requestFocus();
                    }

                } else {
                    Log.e(TAG, "No Internet.");
                    AlertManager alert = new AlertManager();
                    alert.alert(MainActivity.this, "No Internet Connection",
                            "Please check " +
                                    "your data connection or Wifi is ON !");

                }

            }
        });

        process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog = ProgressDialog.show(MainActivity.this, "",
                        "Processing Bill...", true);

                Log.i(TAG, "Processing Bill...");

                handler = new Handler();

                handler.postDelayed(myRunnable = new Runnable() {
                    @Override
                    public void run() {

                        if (dialog != null) dialog.dismiss();
                    }
                }, 30000);


                if (!popupWindow.isShowing()) {
                    ProductDetailsDB prodb = new ProductDetailsDB(getApplicationContext());
                    prodb.open();
                    ArrayList<ProductList> items = prodb.getEANDetails();
                    prodb.close();
                    if (items.size() < 1) {
                        if (dialog != null) dialog.dismiss();
                        try {
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        showFailedAlert(TAG, "Please add products to process");
                    } else {

                        Log.i(TAG, "Checking for offers");
                        LoyaltyDetailsDB lyltydb = new LoyaltyDetailsDB(getApplicationContext());
                        lyltydb.open();
                        ArrayList<in.hng.mpos.gettersetter.LoyaltyDetails> offerList = lyltydb.checkOfferList();
                        lyltydb.close();
                        if (offerList.size() > 0) {
                            Log.i(TAG, "Redirecting to offer page");
                            editor.putString("Bill_no", billno);
                            editor.commit();
                            Intent intent = new Intent(MainActivity.this, LoyaltyOffers.class);
                            startActivity(intent);
                            if (dialog != null) dialog.dismiss();
                            try {
                                handler.removeCallbacks(myRunnable);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            finish();

                        } else {

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
                                if (customerdata.size() == 0) {
                                    JSONObject Headerobj = new JSONObject();
                                    Headerobj.put("billno", billno.isEmpty() ? "" : billno);
                                    Headerobj.put("LocationCode", userdata.get(0).getStoreID());
                                    Headerobj.put("UserCode", userdata.get(0).getUserID());
                               /*  Headerobj.put("CustomerName", customerdata.get(0).getCustomerName());
                                Headerobj.put("PhoneNo", customerdata.get(0).getMobileNO());
                                Headerobj.put("MailID", customerdata.get(0).getMailID());
                                Headerobj.put("Gender", customerdata.get(0).getGender());
                                Headerobj.put("DOB", customerdata.get(0).getDob().equalsIgnoreCase("null") ? "" : customerdata.get(0).getDob());
                                Headerobj.put("Anniversary", customerdata.get(0).getAnniversary().equalsIgnoreCase("null") ? "" : customerdata.get(0).getAnniversary());*/
                                    Headerobj.put("tillNo", tillNo);
                                    Headerjsonarray.put(0, Headerobj);
                                } else {
                                    JSONObject Headerobj = new JSONObject();
                                    Headerobj.put("billno", billno.isEmpty() ? "" : billno);
                                    Headerobj.put("LocationCode", userdata.get(0).getStoreID());
                                    Headerobj.put("UserCode", userdata.get(0).getUserID());
                                    Headerobj.put("CustomerName", customerdata.get(0).getCustomerName());
                                    Headerobj.put("PhoneNo", customerdata.get(0).getMobileNO());
                                    Headerobj.put("MailID", customerdata.get(0).getMailID());
                                    Headerobj.put("Gender", customerdata.get(0).getGender());
                                    Headerobj.put("DOB", customerdata.get(0).getDob().equalsIgnoreCase("null") ? "" : customerdata.get(0).getDob());
                                    Headerobj.put("Anniversary", customerdata.get(0).getAnniversary().equalsIgnoreCase("null") ? "" : customerdata.get(0).getAnniversary());
                                    Headerobj.put("tillNo", tillNo);
                                    Headerjsonarray.put(0, Headerobj);
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "process Exception 1 <<::>> " + e);
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
                                        Log.e(TAG, "process Exception 2 <<::>> " + e);
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
                                    Log.e(TAG, "process Exception 3 <<::>> " + e);
                                }

                                try {

                                    JSONObject OffrCodejsonobj = new JSONObject();
                                    int j = 0;
                                    OffrCodejsonobj.put("OfferID", "");
                                    offercodearray.put(0, OffrCodejsonobj);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "process Exception 4 <<::>> " + e);
                                }

                                try {
                                    finaljsonobj.put("detailLog", productarray);
                                    finaljsonobj.put("header", Headerjsonarray);
                                    finaljsonobj.put("loyaltydata", offerarray);
                                    finaljsonobj.put("loyaltypromo", offercodearray);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "process Exception 5 <<::>> " + e);
                                }

                                RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
                                String URL = PATH + "billdetaillog";
                                final String mRequestBody = finaljsonobj.toString();
                                ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                                if (net.isConnectingToInternet()) {
                                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {

                                            Log.d(TAG, "Bill Detail log Response <<::>> " + response);

                                            try {

                                                JSONObject Jsonobj = new JSONObject(response);
                                                String status = Jsonobj.getString("statusCode");

                                                if (status.equalsIgnoreCase("200")) {
                                                    Log.i(TAG, "Billlog API:Success");
                                                    billno = Jsonobj.getString("bill_no");
                                                    //BillNo.setText(billno);
                                                    JSONArray refershLog = Jsonobj.getJSONArray("refershLog");
                                                    ProductDetailsDB podb = new ProductDetailsDB(getApplicationContext());
                                                    podb.open();
                                                    podb.deleteUserTable();
                                                    podb.createProductDetailsTable();
                                                    podb.insertBulkPOSKUDetails(refershLog);
                                                    podb.close();
                                                    skuList = readFinalData();
                                                    adapter = new ListAdapter(MainActivity.this, skuList);
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
                                                    editor.putString("bbPromo", Jsonobj.getString("bbPromo"));
                                                    editor.putString("Promo_Txt", Jsonobj.getString("PromoTxt"));
                                                    editor.putString("Coupon_Disc", "0.00");
                                                    editor.commit();
                                                    if (dialog != null) dialog.dismiss();
                                                    try {
                                                        handler.removeCallbacks(myRunnable);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    Intent intent = new Intent(MainActivity.this, ProcessActivity.class);
                                                    startActivity(intent);
                                                    finish();

                                                    Log.d("Bill_log API response", Jsonobj.toString());
                                                } else {
                                                    if (dialog != null) dialog.dismiss();
                                                    try {
                                                        handler.removeCallbacks(myRunnable);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    String msg = Jsonobj.getString("Message");
                                                    Log.e(TAG, msg);
                                                    showFailedAlert(TAG, msg);
                                                }

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Log.e(TAG, "BillDetailLog Exception <<::>> " + e);
                                            }
                                            // Log.i("LOG_VOLLEY", response);
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {


                                            try {
                                                if (dialog != null) dialog.dismiss();
                                                try {
                                                    handler.removeCallbacks(myRunnable);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                showVolleyError(TAG, error);

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Log.e(TAG, "Volley Error Exception <<::>> " + e);
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
                                                return mRequestBody.getBytes("utf-8");
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
                                    if (dialog != null) dialog.dismiss();
                                    try {
                                        handler.removeCallbacks(myRunnable);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    Log.e(TAG, "No Internet");
                                    AlertManager alert = new AlertManager();
                                    alert.alert(MainActivity.this, "No Internet Connection",
                                            "Please check " +
                                                    "your data connection or Wifi is ON !");

                                }
                            }


                        }
                    }
                } else {
                    if (dialog != null) dialog.dismiss();
                    try {
                        handler.removeCallbacks(myRunnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "PopWindow Exception <<::>> " + e);
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

                hideKeyboard();

                Log.i(TAG, "Searching Ean..");
                if (!popupWindow.isShowing()) {

                    if (Ean.getText().length() >= 6) {
                        ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                        if (net.isConnectingToInternet()) {

                            URL = PATH + "geteandetail?ean_code=" + Ean.getText().toString() + "&location=" + Location;
                            Log.d(TAG, "Load Product Details Url <<::>> " + URL);
                            makeLoadProductDetailAPI(URL);
                            //LoadProductDetails(Ean.getText().toString());
                        } else {
                            AlertManager alert = new AlertManager();
                            alert.alert(MainActivity.this, "No Internet Connection",
                                    "Please check " +
                                            "your data connection or Wifi is ON !");

                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "Please enter valid EAN.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Please select MRP", Toast.LENGTH_SHORT).show();
                    Ean.setText("");
                    Ean.requestFocus();
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {

                LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                                adapter = new ListAdapter(MainActivity.this, skuList);
                                listView.setAdapter(adapter);
                                deletepopup.dismiss();
                            }
                        } else {
                            showFailedAlert(TAG, "Please enter valid quantity");
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

    String mskuCode = "";

    private void makeLoadProductDetailAPI(String urlPath) {

        dialog = ProgressDialog.show(MainActivity.this, "",
                "Loading product Details, Please Wait...", true);

        EanSearch.setEnabled(false);

        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                if (dialog != null) dialog.dismiss();
            }
        }, 30000);

        mskuCode = Ean.getText().toString().trim();

        ApiCall.make(this, urlPath, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "Api Call response: " + response);
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            statusCode = responseObject.getString("statusCode");
                            Log.e(TAG, "Status Code ====> " + statusCode);

                            if (statusCode.equalsIgnoreCase("200")) {
                                if (dialog != null) dialog.dismiss();
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
                                eanCode = json_product.getString("ean_code");

                                //if (sku_name != null && !sku_name.equalsIgnoreCase("null") && !TextUtils.isEmpty(sku_name)) {

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
                                    if (!((Activity) MainActivity.this).isFinishing()) {
                                        popupWindow.showAtLocation(linearLayout1, Gravity.CENTER, 0, 40);
                                        TextView item_name = customView.findViewById(R.id.txtitemname);
                                        item_name.setText(sku_name);
                                        mySpinner.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item,
                                                mrp_spnList));
                                    }


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

                                            writeData(sku_name, skuCode, storeSKU, mrp, TAXcode, TAXrate, eanCode);
                                            skuList = readFinalData();
                                            adapter = new ListAdapter(MainActivity.this, skuList);
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

                                    writeData(sku_name, skuCode, storeSKU, mrp, TAXcode, TAXcode, eanCode);
                                    skuList = readFinalData();
                                    adapter = new ListAdapter(MainActivity.this, skuList);
                                    listView.setAdapter(adapter);

                                    /*if (json_mrp.has("MRP")) {
                                        mrp = json_mrp.optString("MRP");
                                        if (!mrp.equalsIgnoreCase("null") && !mrp.isEmpty()) {
                                            writeData(sku_name, skuCode, storeSKU, mrp, TAXcode, TAXcode, eanCode);
                                            skuList = readFinalData();
                                            adapter = new ListAdapter(MainActivity.this, skuList);
                                            listView.setAdapter(adapter);
                                        } else
                                            showFailedAlert("Product MRP not found!");

                                        //openManualBilling("Product MRP not found! Do you want to add Manually", mskuCode);
                                        //dialog.dismiss();
                                        // handler.removeCallbacks(myRunnable);
                                    } else
                                        showFailedAlert("Product MRP not found!");*/

                                    //openManualBilling("Product MRP not found! Do you want to add Manually", mskuCode);

                                    EanSearch.setEnabled(true);
                                    Ean.setText("");
                                    Ean.requestFocus();

                                }

//                                } else {
//                                    openManualBilling("Sku Name not found! Do you want to add Manually", mskuCode);
//
//                                }
                                EanSearch.setEnabled(true);

                            } else {

                                Log.d(TAG, "Else Statement");
                                Log.d(TAG, "Status Code ==> " + statusCode);
                                Msg = responseObject.getString("Message");
                                Log.w(TAG, "Error Message " + Msg);

                                if (dialog != null) dialog.dismiss();
                                EanSearch.setEnabled(true);
                                showAlertMessage(TAG, Msg);
                                Ean.setText("");
                                Ean.requestFocus();

                                //openManualBilling(Msg + "! Do you want to add Manually", mskuCode);

                                try {
                                    handler.removeCallbacks(myRunnable);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "JSON Exception <<::>> " + e);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        try {
                            if (dialog != null) dialog.dismiss();
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
                            showVolleyError(TAG, error);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, "Volley Error Exception <<::>> " + e);
                        }
                    }
                }
        );
    }

    private void openManualBilling(String message, String skuCode) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(MainActivity.this, ManualBillBookActivity.class);
                        i.putExtra(Constants.SKU_CODE, mskuCode);
                        startActivity(i);
                        //startActivityForResult(i, 1001);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult.getContents() != null) {
            codeContent = scanningResult.getContents();
            ConnectionDetector net = new ConnectionDetector(getApplicationContext());
            if (net.isConnectingToInternet()) {

                URL = PATH + "geteandetail?ean_code=" + codeContent + "&location=" + Location;
                Log.d(TAG, "URL <<::>> " + URL);
                makeLoadProductDetailAPI(URL);
                //LoadProductDetails(codeContent);
            } else {
                AlertManager alert = new AlertManager();
                alert.alert(MainActivity.this, "No Internet Connection",
                        "Please check " +
                                "your data connection or Wifi is ON !");
            }

        } else {
            Toast.makeText(getApplicationContext(), "No items scanned!", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(MainActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();
            Ean.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Write to local Db Exception <<::>> " + e);
        }
    }

    public ArrayList<ProductList> readFinalData() {
        ProductDetailsDB podb = new ProductDetailsDB(getApplicationContext());
        podb.open();
        ArrayList<ProductList> skuList = podb.getEANDetails();

        podb.close();

        return skuList;

    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(MainActivity.this)
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

    private void goBack() {
        Intent i = new Intent(MainActivity.this, LoyaltyActivity.class);
        startActivity(i);
        finish();
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
    /*@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getCharacters() != null && !event.getCharacters().isEmpty()) {
            //Add more code..
            //Ean.setText("");

            if (this.getCurrentFocus() != null && this.getCurrentFocus().getId() != Ean.getId())
                Ean.requestFocus();
        }
        return super.dispatchKeyEvent(event);
    }*/
}
