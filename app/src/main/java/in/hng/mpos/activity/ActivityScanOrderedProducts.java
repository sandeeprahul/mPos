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
import android.preference.PreferenceManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import in.hng.mpos.Adapter.OrderItemListAdapter;
import in.hng.mpos.Database.OrderedProductDetailsDB;
import in.hng.mpos.Database.ProcessedOrderDB;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.MrpList;
import in.hng.mpos.gettersetter.ProductList;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.helper.AlertManager;
import in.hng.mpos.helper.ConnectionDetector;
import in.hng.mpos.helper.JSONParser;
import in.hng.mpos.helper.Log;

public class ActivityScanOrderedProducts extends BaseActivity {

    private static final String TAG = "Scanning Order Product";

    private String codeFormat, codeContent, path, details_url, sku_name, mrp, qty, statusCode, mrp_ind;
    private String LocationCode, storeSKU, skuCode, TAXcode, TAXrate, EANcode, CashierCode, PATH, eanCode;
    private Integer r_count;

    Button add, process, cancelItem, addItem, updateQty, cancelUpdate, EanSearch;
    EditText couponCode;
    Button applyCoupon;
    Spinner mySpinner;
    TextView updateItemname;
    EditText updadeItemQty, Ean;
    ListView listView;
    LinearLayout linearLayout1;
    View customView;

    JSONParser jParser = new JSONParser();
    JSONArray jsonarray, jsonarray_product;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    OrderItemListAdapter adapter;
    PopupWindow popupWindow, deletepopup, couponpopup;

    ArrayList<ProductList> skuList;
    ArrayList<MrpList> mrp_list;
    ArrayList<String> mrp_spnList;

    String Msg, Location, tillNo;
    String billno = "";
    String item_name, item_qty, item_storeSKU, item_Mrp;

    private ProgressDialog dialog = null;
    private boolean isFirstEntry, isProductExist, isProductPending;
    private String customerName, customerMobile, orderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_order_products);

        setActionBarTitle("h&g Scan Product");

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        editor.apply();
        customerName = sp.getString("customerName", "");
        customerMobile = sp.getString("customerMobile", "");
        orderID = sp.getString("orderID", "");

        listView = (in.hng.mpos.helper.NestedListView) findViewById(R.id.listv);
        EanSearch = findViewById(R.id.EanSearch);

        //instantiate the popup.xml layout file
        LayoutInflater layoutInflater = (LayoutInflater) ActivityScanOrderedProducts.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        customView = layoutInflater.inflate(R.layout.popup, null);
        cancelItem = customView.findViewById(R.id.CancelItem);
        addItem = customView.findViewById(R.id.AddItem);
        mySpinner = customView.findViewById(R.id.SpnrMrp);
        process = findViewById(R.id.Process);
        Ean = findViewById(R.id.txtean);
        EanSearch = findViewById(R.id.EanSearch);
        linearLayout1 = findViewById(R.id.linearLayout1);
        //instantiate popup window
        popupWindow = new PopupWindow(customView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);

        OrderedProductDetailsDB productDB = new OrderedProductDetailsDB(getApplicationContext());
        productDB.open();
        ArrayList<ProductList> products = productDB.getAllEANDetails(orderID);
        productDB.close();
        billno = sp.getString("Bill_no", "");

        if (products.size() > 0) {

            skuList = readFinalData();
            adapter = new OrderItemListAdapter(ActivityScanOrderedProducts.this, skuList);
            listView.setAdapter(adapter);
            Log.i(TAG, "Scanning screen loaded.");

        }

        UserDB uDB = new UserDB(getApplicationContext());
        uDB.open();
        ArrayList<UserDetails> userDetails = uDB.getUserDetails();
        uDB.close();
        Location = userDetails.get(0).getStoreID();

        //Resources res = getResources();
        //PATH = res.getString(R.string.API_URL);
        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH = urlDB.getUrlDetails();
        urlDB.close();

        EanSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View v = ActivityScanOrderedProducts.this.getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                Log.i(TAG, "Searching Ean..");
                if (!popupWindow.isShowing()) {

                    if (Ean.getText().length() >= 6) {
                        ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                        if (net.isConnectingToInternet()) {
                            LoadProductDetails(Ean.getText().toString());
                        } else {
                            AlertManager alert = new AlertManager();
                            alert.alert(ActivityScanOrderedProducts.this, "No Internet Connection",
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

                LayoutInflater layoutInflater = (LayoutInflater) ActivityScanOrderedProducts.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View customView = layoutInflater.inflate(R.layout.delete_item_popup, null);
                LinearLayout lyt = customView.findViewById(R.id.popup);
                LinearLayout lytHead = customView.findViewById(R.id.lytHeader);
                cancelUpdate = customView.findViewById(R.id.CancelUpdate);
                updateQty = customView.findViewById(R.id.UpdateQty);
                updateItemname = customView.findViewById(R.id.txtupdateitemname);
                updadeItemQty = customView.findViewById(R.id.txtUpdateQty);

                updateQty.setBackground(new ColorDrawable(getResources()
                        .getColor(R.color.OrderTakingBackColor)));
                updateQty.setTextColor(Color.WHITE);
                cancelUpdate.setBackground(new ColorDrawable(getResources()
                        .getColor(R.color.OrderTakingBackColor)));
                cancelUpdate.setTextColor(Color.WHITE);

                updadeItemQty.setBackgroundDrawable(ContextCompat.getDrawable(ActivityScanOrderedProducts.this, R.drawable.order_text_box_style));
                lyt.setBackgroundDrawable(ContextCompat.getDrawable(ActivityScanOrderedProducts.this, R.drawable.order_text_box_style));
                lytHead.setBackgroundDrawable(ContextCompat.getDrawable(ActivityScanOrderedProducts.this, R.drawable.order_text_box_style));
                //instantiate popup window

                skuList = readFinalData();
                item_name = skuList.get(pos).getSkuName();
                item_qty = skuList.get(pos).getQty();
                item_storeSKU = skuList.get(pos).getStoreSKU();
                item_Mrp = skuList.get(pos).getMrp();
                updateItemname.setText(item_name);
                updadeItemQty.setText(item_qty);

                if (!item_Mrp.equalsIgnoreCase("0.00")) {
                    deletepopup = new PopupWindow(customView, Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
                    //display the popup window
                    deletepopup.showAtLocation(listView, Gravity.CENTER_VERTICAL, 0, 30);
                    deletepopup.setFocusable(true);
                    deletepopup.update();
                }

                updateQty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Integer up_qty = Integer.parseInt(updadeItemQty.getText().toString());
                        //Integer qty = Integer.parseInt(item_qty);


                        if (!updadeItemQty.getText().toString().isEmpty()) {

                            OrderedProductDetailsDB podb = new OrderedProductDetailsDB(getApplicationContext());
                            podb.open();
                            if (updadeItemQty.getText().toString().equalsIgnoreCase("0")) {
                                podb.deleteProduct(orderID, "", item_storeSKU, item_Mrp);
                            } else {
                                podb.updateItemQty(item_storeSKU, item_Mrp, updadeItemQty.getText().toString());
                            }
                            podb.close();

                            skuList = readFinalData();
                            adapter = new OrderItemListAdapter(ActivityScanOrderedProducts.this, skuList);
                            listView.setAdapter(adapter);
                            deletepopup.dismiss();

                        } else {
                            showFailedAlert("HnG Order Processing", "Please enter valid quantity");
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

    public void ProcessOrder(View v) {

        skuList = readFinalData();
        OrderedProductDetailsDB podb = new OrderedProductDetailsDB(getApplicationContext());
        podb.open();
        for (int i = 0; i < skuList.size(); i++) {
            isProductPending = podb.isProductPending(skuList.get(i).getSkuCode().toString());
            if (isProductPending)
                break;
        }
        podb.close();
        if (!isProductPending) {
            dialog = ProgressDialog.show(ActivityScanOrderedProducts.this, "",
                    "Processing Bill...", true);
            Log.i(TAG, "Processing Bill.");
            Log.i(TAG, "Calling Bill_log API");
            UserDB userdb = new UserDB(getApplicationContext());
            userdb.open();
            ArrayList<UserDetails> userdata = userdb.getUserDetails();
            userdb.close();

            JSONObject finaljsonobj = new JSONObject();
            JSONArray Headerjsonarray = new JSONArray();
            JSONArray productarray = new JSONArray();

            try {
                JSONObject Headerobj = new JSONObject();
                Headerobj.put("billNo", orderID.isEmpty() ? "" : orderID);
                Headerobj.put("locationCode", userdata.get(0).getStoreID());
                Headerobj.put("customerName", customerName);
                Headerobj.put("phoneNo", customerMobile);
                Headerobj.put("userCode", userdata.get(0).getUserID());
                Headerobj.put("tillNo", userdata.get(0).getTillNo());
                Headerjsonarray.put(0, Headerobj);

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "JSON Exception 1 <<::>> "+e);
            }

            if (skuList.size() > 0) {
                for (int i = 0; i < skuList.size(); i++) {

                    try {
                        JSONObject Projsonobj = new JSONObject();
                        int j = 0;
                        j = i + 1;
                        Projsonobj.put("locationCode", userdata.get(0).getStoreID());
                        Projsonobj.put("billNo", orderID.isEmpty() ? "" : orderID);
                        Projsonobj.put("lineNo", j);
                        Projsonobj.put("storeSkuLocNo", skuList.get(i).getStoreSKU());
                        Projsonobj.put("skuQty", skuList.get(i).getQty());
                        Projsonobj.put("mrp", skuList.get(i).getMrp());
                        Projsonobj.put("casherCode", userdata.get(0).getUserID());
                        productarray.put(i, Projsonobj);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "JSON Exception 2 <<::>> "+e);
                    }
                }

                try {
                    finaljsonobj.put("detailLog", productarray);
                    finaljsonobj.put("header", Headerjsonarray);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "Exception 3 <<::>> " + e);
                }

                RequestQueue requestQueue = Volley.newRequestQueue(ActivityScanOrderedProducts.this);
                String URL = PATH + "Assistebilldetaillog";
                final String mRequestBody = finaljsonobj.toString();
                ConnectionDetector net = new ConnectionDetector(getApplicationContext());

                if (net.isConnectingToInternet()) {

                    Log.e(TAG, "Api Call: " + URL);

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.e(TAG, "Api Call Response : " + response);
                            try {

                                JSONObject Jsonobj = new JSONObject(response);
                                String status = Jsonobj.getString("statusCode");
                                if (status.equalsIgnoreCase("200")) {

                                    Log.i(TAG, "Billlog API:Success");
                                    JSONArray refershLog = Jsonobj.getJSONArray("refershLog");
                                    ProcessedOrderDB podb = new ProcessedOrderDB(getApplicationContext());
                                    podb.open();
                                    podb.deleteProductsTable();
                                    podb.createProcessedOrderTable();
                                    podb.insertProcessedOrderDetails(refershLog);
                                    podb.close();

                                    UserDB userdb = new UserDB(getApplicationContext());
                                    userdb.open();
                                    ArrayList<UserDetails> userdata = userdb.getUserDetails();
                                    userdb.close();
                                    editor.putString("Bill_no", orderID);
                                    editor.putString("Location_code", userdata.get(0).getStoreID());
                                    editor.putString("Cashier_code", userdata.get(0).getUserID());
                                    editor.putString("Total_Amt", Jsonobj.getString("bill_value"));
                                    editor.apply();
                                    dialog.dismiss();

                                    Intent intent = new Intent(ActivityScanOrderedProducts.this, ActivityProcessOrder.class);
                                    startActivity(intent);
                                    finish();

                                    Log.d("Bill_log API response", Jsonobj.toString());

                                } else {
                                    dialog.dismiss();
                                    String msg = Jsonobj.getString("Message");
                                    Log.e(TAG, msg);
                                    showFailedAlert("HnG Order Processing", msg);
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
                                dialog.dismiss();
                                showVolleyError(TAG, error);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, "VolleyError Exception <<::>> " + e);
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

                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                            5000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(stringRequest);

                } else {
                    dialog.dismiss();
                    Log.e(TAG, "No Internet");
                    AlertManager alert = new AlertManager();
                    alert.alert(ActivityScanOrderedProducts.this, "No Internet Connection",
                            "Please check " +
                                    "your data connection or Wifi is ON !");
                }
            }

        } else
            showFailedAlert("HnG Order Processing", "Products are pending");

    }

    public ArrayList<ProductList> readFinalData() {
        OrderedProductDetailsDB podb = new OrderedProductDetailsDB(getApplicationContext());
        podb.open();
        ArrayList<ProductList> skuList = podb.getAllEANDetails(orderID);
        podb.close();
        return skuList;
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(ActivityScanOrderedProducts.this)
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

    private void goBack() {
        Log.e(TAG, "Back Button Clicked.");
        View view = ActivityScanOrderedProducts.this.getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        Intent i = new Intent(ActivityScanOrderedProducts.this, ActivityLoadOrders.class);
        startActivity(i);
        finish();
    }

    public void LoadProductDetails(String productCode) {

        dialog = ProgressDialog.show(ActivityScanOrderedProducts.this, "",
                "Loading product Details, Please Wait...", true);

        RequestQueue queue = Volley.newRequestQueue(ActivityScanOrderedProducts.this);

        final String url = PATH + "geteandetail?ean_code=" + productCode + "&location=" + Location;
        Log.i(TAG, "Calling Product Details API:" + url);

        ConnectionDetector net = new ConnectionDetector(getApplicationContext());

        if (net.isConnectingToInternet()) {
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // display response
                            Log.e(TAG, "Response" + response);
                            try {
                                dialog.dismiss();
                                statusCode = response.getString("statusCode");
                                if (statusCode.equalsIgnoreCase("200")) {
                                    mrp_ind = response.getString("multiplemrp");

                                    jsonarray_product = response.getJSONArray("product");
                                    JSONObject json_product;
                                    json_product = jsonarray_product.getJSONObject(0);
                                    //LocationCode = "176";
                                    qty = "1";
                                    sku_name = json_product.getString("SKU_NAME");
                                    skuCode = json_product.getString("SKU_CODE");
                                    TAXcode = json_product.getString("TAX_CODE");
                                    TAXrate = json_product.getString("TAX_RATE");
                                    eanCode = json_product.getString("ean_code");
                                    //CashierCode = "101981";

                                    if (mrp_ind.equalsIgnoreCase("Y")) {
                                        jsonarray = response.getJSONArray("batch");
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
                                        LinearLayout lyt = customView.findViewById(R.id.linearHeader);
                                        LinearLayout popLyt = customView.findViewById(R.id.popup);
                                        item_name.setText(sku_name);

                                        addItem.setBackground(new ColorDrawable(getResources()
                                                .getColor(R.color.OrderTakingBackColor)));
                                        addItem.setTextColor(Color.WHITE);
                                        cancelItem.setBackground(new ColorDrawable(getResources()
                                                .getColor(R.color.OrderTakingBackColor)));
                                        cancelItem.setTextColor(Color.WHITE);
                                        mySpinner.setBackgroundDrawable(ContextCompat.getDrawable(ActivityScanOrderedProducts.this, R.drawable.order_text_box_style));
                                        popLyt.setBackgroundDrawable(ContextCompat.getDrawable(ActivityScanOrderedProducts.this, R.drawable.order_text_box_style));
                                        lyt.setBackgroundDrawable(ContextCompat.getDrawable(ActivityScanOrderedProducts.this, R.drawable.order_text_box_style));

                                        mySpinner.setAdapter(new ArrayAdapter<String>(ActivityScanOrderedProducts.this, android.R.layout.simple_spinner_dropdown_item,
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

                                                writeData(sku_name, skuCode, storeSKU, mrp, TAXcode, TAXrate, eanCode);
                                                skuList = readFinalData();
                                                adapter = new OrderItemListAdapter(ActivityScanOrderedProducts.this, skuList);
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
                                        jsonarray = response.getJSONArray("batch");
                                        JSONObject json_mrp = jsonarray.getJSONObject(0);
                                        storeSKU = json_mrp.optString("STORE_SKU_LOC_STOCK_NO");
                                        mrp = json_mrp.optString("MRP");
                                        writeData(sku_name, skuCode, storeSKU, mrp, TAXcode, TAXcode, eanCode);
                                        skuList = readFinalData();
                                        adapter = new OrderItemListAdapter(ActivityScanOrderedProducts.this, skuList);
                                        listView.setAdapter(adapter);
                                        dialog.dismiss();
                                        Ean.setText("");
                                        Ean.requestFocus();
                                    }


                                } else {

                                    Msg = response.getString("Message");
                                    showFailedAlert("HnG Order Processing", Msg);

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "JSON Exception <<::>> " + e);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            try {
                                dialog.dismiss();
                                showVolleyError(TAG, error);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, "VolleyError Exception <<::>> " + e);
                            }

                        }
                    }
            );
            getRequest.setRetryPolicy(new DefaultRetryPolicy(
                    5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


            queue.add(getRequest);
        } else {
            dialog.dismiss();
            AlertManager alert = new AlertManager();
            alert.alert(ActivityScanOrderedProducts.this, "No Internet Connection",
                    "Please check " +
                            "your data connection or Wifi is ON !");

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
            OrderedProductDetailsDB podb = new OrderedProductDetailsDB(getApplicationContext());
            podb.open();

            isFirstEntry = podb.isFirstEntry(orderID, skuCode);

            if (isFirstEntry)
                podb.insertItem(orderID, ProductDetails, isFirstEntry);
            else {
                isProductExist = podb.isProductExist(storeSKU);
                if (isProductExist)
                    podb.updateProductListQty(orderID, ProductDetails);
                else
                    podb.insertItem(orderID, ProductDetails, isProductExist);

            }
            podb.close();

            Toast toast = Toast.makeText(ActivityScanOrderedProducts.this, "Item added successfully", Toast.LENGTH_SHORT);
            toast.show();
            Ean.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}