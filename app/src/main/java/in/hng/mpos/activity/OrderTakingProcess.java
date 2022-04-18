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
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import in.hng.mpos.Database.CustomerDB;
import in.hng.mpos.Database.LoyaltyDetailsDB;
import in.hng.mpos.Database.OrderedProductDetailsDB;
import in.hng.mpos.Database.StoreDB;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.SearchProduct;
import in.hng.mpos.gettersetter.StoreDetails;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.helper.AlertManager;
import in.hng.mpos.helper.ApiCall;
import in.hng.mpos.helper.AutoSuggestAdapter;
import in.hng.mpos.helper.ConnectionDetector;
import in.hng.mpos.helper.Log;
import in.hng.mpos.helper.OrderProductsListAdapter;

public class OrderTakingProcess extends AppCompatActivity {

    private Button proceed;
    private String PATH;
    private AutoCompleteTextView autocompleteView;
    ArrayList<String> product_list = new ArrayList<>();
    private ProgressDialog dialog = null;
    ArrayList<SearchProduct> skuList;
    OrderProductsListAdapter adapter;
    ListView listView;
    private String sku_name, skuCode;
    ArrayList<SearchProduct> productList;
    private static final String TAG = "OrderTakingProcess";
    Button add, process, cancelItem, addItem, updateQty, cancelUpdate, EanSearch;
    TextView updateItemname;
    EditText updadeItemQty, Ean;
    PopupWindow deletepopup;
    String item_name, item_qty, item_SKU, item_Mrp;
    String OrderID = "";
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    View customView;
    Button Cancel;
    ArrayAdapter<String> AutoViewadapter;
    private String Location,USerID;

    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_taking);

        final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(
                R.layout.action_bar_simple, null);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarLayout);
        actionBar.setTitle("");
        final int actionBarColor = getResources().getColor(R.color.OrderTakingBackColor);
        actionBar.setBackgroundDrawable(new ColorDrawable(actionBarColor));
        TextView title = findViewById(R.id.ab_activity_title);
        title.setText("h&g Assisted Order");
        title.setVisibility(View.VISIBLE);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        editor.commit();
        OrderID = sp.getString("OrderID", "");

        //Resources res = getResources();
        //PATH = res.getString(R.string.API_URL);

        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH =urlDB.getUrlDetails();
        urlDB.close();


        Cancel = findViewById(R.id.btncancel);

        listView = (in.hng.mpos.helper.NestedListView) findViewById(R.id.listv);

        proceed = findViewById(R.id.btnproceed);


        LayoutInflater layoutInflater = (LayoutInflater) OrderTakingProcess.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        customView = layoutInflater.inflate(R.layout.delete_order_item_popup, null);
        deletepopup = new PopupWindow(customView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#3c3c3c"));
        }

        StoreDB storedb = new StoreDB(getApplicationContext());
        storedb.open();
        ArrayList<StoreDetails> storeDetails = storedb.getStoreDetails();
        storedb.close();

        if (storeDetails.size() > 0) {

            Location=storeDetails.get(0).getStoreID();
            USerID=storeDetails.get(0).getUserID();
        }

        OrderedProductDetailsDB productDB = new OrderedProductDetailsDB(getApplicationContext());
        productDB.open();
        ArrayList<SearchProduct> products = productDB.getEANDetails();
        productDB.close();
        if (products.size() > 0) {

            skuList = readFinalData();
            adapter = new OrderProductsListAdapter(OrderTakingProcess.this, skuList);
            listView.setAdapter(adapter);
            Log.i(TAG, "Scanning screen loaded.");

        }

        final AppCompatAutoCompleteTextView autoCompleteTextView =
                findViewById(R.id.auto_complete_edit_text);

        //Setting up the adapter for AutoSuggest
        autoSuggestAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextView.setThreshold(2);
        autoCompleteTextView.setAdapter(autoSuggestAdapter);
        autoCompleteTextView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        sku_name = productList.get(position).getSkuName();
                        skuCode = productList.get(position).getSkuCode();

                        new AlertDialog.Builder(OrderTakingProcess.this)
                                .setMessage("Do you want to add " + skuCode + "-" + sku_name + " ?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface idialog, int id) {

                                        writeData(sku_name, skuCode);
                                        skuList = readFinalData();
                                        adapter = new OrderProductsListAdapter(OrderTakingProcess.this, skuList);
                                        listView.setAdapter(adapter);
                                        autoCompleteTextView.setText("");

                                    }
                                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface idialog, int id) {

                                autoCompleteTextView.setText("");

                            }
                        })
                                .show();
                    }
                });

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                        AUTO_COMPLETE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(autoCompleteTextView.getText())) {
                        String Url=PATH + "AssistedSearchproducts?Location="+Location+"&Productname=" +autoCompleteTextView.getText().toString().replaceAll(" ", "%20");
                        makeAutofillApiCall(Url);
                    }
                }
                return false;
            }
        });

        Cancel.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(OrderTakingProcess.this)
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

                                Intent intent = new Intent(OrderTakingProcess.this, LoyaltyActivity.class);
                                startActivity(intent);
                                finish();


                            }
                        }).setNegativeButton("No", null)
                        .show();

            }
        });


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {

                LayoutInflater layoutInflater = (LayoutInflater) OrderTakingProcess.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View customView = layoutInflater.inflate(R.layout.delete_order_item_popup, null);
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

                item_SKU = skuList.get(pos).getSkuCode();
                item_name = skuList.get(pos).getSkuName();
                item_qty = skuList.get(pos).getQty();

                updateItemname.setText(item_name);
                updadeItemQty.setText(item_qty);


                updateQty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Integer up_qty = Integer.parseInt(updadeItemQty.getText().toString());
                        Integer qty = Integer.parseInt(item_qty);

                        if (!updadeItemQty.getText().toString().isEmpty()) {

                            OrderedProductDetailsDB podb = new OrderedProductDetailsDB(getApplicationContext());
                            podb.open();
                            if (updadeItemQty.getText().toString().equalsIgnoreCase("0")) {
                                podb.deleteProduct("",item_SKU,"","");
                            } else {
                                podb.updateProductQty(item_SKU, updadeItemQty.getText().toString());
                            }
                            podb.close();

                            skuList = readFinalData();
                            adapter = new OrderProductsListAdapter(OrderTakingProcess.this, skuList);
                            listView.setAdapter(adapter);
                            deletepopup.dismiss();

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

    private void makeAutofillApiCall(String urlPath) {
        Log.i(TAG, "Api Call: "+urlPath);
        ApiCall.make(this, urlPath, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "Api Call response: "+response);
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            String status = responseObject.getString("statusCode");
                            if (status.equalsIgnoreCase("200")) {

                                JSONArray products = responseObject.getJSONArray("details");
                                productList = null;
                                productList = new ArrayList<SearchProduct>();
                                product_list = new ArrayList<String>();
                                for (int i = 0; i < products.length(); i++) {
                                    SearchProduct product = new SearchProduct();
                                    JSONObject json_products;
                                    json_products = products.getJSONObject(i);
                                    product.setSkuCode(json_products.optString("sku_code"));
                                    product.setSkuName(json_products.optString("sku_name"));
                                    productList.add(product);
                                    product_list.add(json_products.optString("sku_name"));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //IMPORTANT: set data here and notify
                        autoSuggestAdapter.setData(product_list);
                        autoSuggestAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {


                    }
                }
        );
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(OrderTakingProcess.this)
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
        Intent i = new Intent(OrderTakingProcess.this, LoyaltyActivity.class);
        startActivity(i);
        finish();


    }


    public void ProcessBill(View v) {

        dialog = ProgressDialog.show(OrderTakingProcess.this, "",
                "Processing Order...", true);
        Log.i(TAG, "Processing Order.");
        if (!deletepopup.isShowing()) {
            OrderedProductDetailsDB prodb = new OrderedProductDetailsDB(getApplicationContext());
            prodb.open();
            ArrayList<SearchProduct> items = prodb.getEANDetails();
            prodb.close();
            if (items.size() < 1) {
                dialog.dismiss();
                showFailedAlert("Please add products to process");
            } else {

                Log.i(TAG, "Calling Order_Detail_log API");
                JSONObject finaljsonobj = new JSONObject();
                JSONArray productarray = new JSONArray();


                OrderedProductDetailsDB podb = new OrderedProductDetailsDB(getApplicationContext());
                podb.open();
                ArrayList<SearchProduct> products = podb.getEANDetails();
                podb.close();
                if (products.size() > 0) {
                    for (int i = 0; i < products.size(); i++) {

                        try {
                            JSONObject Projsonobj = new JSONObject();
                            int j = 0;
                            j = i + 1;
                            Projsonobj.put("order_id", OrderID);
                            Projsonobj.put("line_no", i + 1);
                            Projsonobj.put("sku_code", products.get(i).getSkuCode());
                            Projsonobj.put("order_qty", products.get(i).getQty());
                            Projsonobj.put("locationCode", "154");
                            Projsonobj.put("till_No", "13");
                            productarray.put(i, Projsonobj);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


                    try {
                        finaljsonobj.put("statusCode", "200");
                        finaljsonobj.put("status", "Success");
                        finaljsonobj.put("details", productarray);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    RequestQueue requestQueue = Volley.newRequestQueue(OrderTakingProcess.this);
                    String URL = PATH + "AssistedOrderDetailLog";
                    Log.i(TAG, "Api Call: "+URL);
                    final String mRequestBody = finaljsonobj.toString();
                    ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                    if (net.isConnectingToInternet()) {
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.i(TAG, "Api Call response: "+response);
                                try {

                                    JSONObject Jsonobj = new JSONObject(response);
                                    String status = Jsonobj.getString("statusCode");

                                    if (status.equalsIgnoreCase("200")) {
                                        Log.i(TAG, "Orderlog API:Success");
                                        OrderID = Jsonobj.getString("OrderId");

                                        UserDB userdb = new UserDB(getApplicationContext());
                                        userdb.open();
                                        ArrayList<UserDetails> userdata = userdb.getUserDetails();
                                        userdb.close();
                                        editor.putString("OrderID", OrderID);
                                        editor.putString("isAddAddr", "");
                                        editor.commit();
                                        dialog.dismiss();

                                        Intent intent = new Intent(OrderTakingProcess.this, ActivityDeliveryType.class);
                                        startActivity(intent);
                                        finish();

                                        Log.d("Order_Bill_log API response", Jsonobj.toString());
                                    } else {
                                        dialog.dismiss();
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
                                    dialog.dismiss();
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
                        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                6000,
                                3,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                        requestQueue.add(stringRequest);
                    } else {
                        dialog.dismiss();
                        Log.e(TAG, "No Internet");
                        AlertManager alert = new AlertManager();
                        alert.alert(OrderTakingProcess.this, "No Internet Connection",
                                "Please check " +
                                        "your data connection or Wifi is ON !");

                    }
                }


            }
        }
    }


    public void writeData(String skuName, String skuCode) {

        try {
            HashMap<String, String> ProductDetails = new HashMap<String, String>();
            ProductDetails.put("skuName", skuName);
            ProductDetails.put("skuCode", skuCode);
            ProductDetails.put("Qty", "1");

            OrderedProductDetailsDB podb = new OrderedProductDetailsDB(getApplicationContext());
            podb.open();
            ArrayList<SearchProduct> poList = podb.checkProductList(skuCode);
            if (poList.size() > 0) {
                podb.updateProductList("",ProductDetails);
            } else {
                podb.insertOrderedProductDetails(ProductDetails);
            }
            podb.close();
            autocompleteView.setText("");
            // Toast.makeText(getApplicationContext(), "Data saved successfully", Toast.LENGTH_SHORT).show();
            Toast toast = Toast.makeText(OrderTakingProcess.this, "Item added successfully", Toast.LENGTH_SHORT);
            toast.show();
            autocompleteView.requestFocus();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<SearchProduct> readFinalData() {
        OrderedProductDetailsDB podb = new OrderedProductDetailsDB(getApplicationContext());
        podb.open();
        ArrayList<SearchProduct> skuList = podb.getEANDetails();

        podb.close();

        return skuList;

    }

    public void showFailedAlert(final String msg) {

        OrderTakingProcess.this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(OrderTakingProcess.this);
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
