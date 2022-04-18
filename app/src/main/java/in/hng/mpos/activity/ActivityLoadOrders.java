package in.hng.mpos.activity;

import android.app.AlertDialog;
import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import in.hng.mpos.Adapter.OrderRecyclerViewAdapter;
import in.hng.mpos.Database.OrderedProductDetailsDB;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.OrderInfo;
import in.hng.mpos.gettersetter.ProductList;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.helper.AlertManager;
import in.hng.mpos.helper.ApiCall;
import in.hng.mpos.helper.ConnectionDetector;
import in.hng.mpos.helper.Log;


public class ActivityLoadOrders extends BaseActivity {

    private static final String TAG = "ActivityLoadOrders";

    OrderRecyclerViewAdapter adapter;
    private String PATH;
    private String Location;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private String OrderID, customerName, customerMobile;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_orders);

        setActionBarTitle("h&g Pending Orders");

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        editor.apply();

        UserDB uDB = new UserDB(getApplicationContext());
        uDB.open();
        ArrayList<UserDetails> userDetails = uDB.getUserDetails();
        uDB.close();
        Location = userDetails.get(0).getStoreID();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_search);
        recyclerView.setNestedScrollingEnabled(false);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        ArrayList<OrderInfo> orderInfos = null;
        adapter = new OrderRecyclerViewAdapter(ActivityLoadOrders.this, orderInfos);

        //Resources res = getResources();
        //PATH = res.getString(R.string.API_URL);
        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH = urlDB.getUrlDetails();
        urlDB.close();
        ConnectionDetector net = new ConnectionDetector(getApplicationContext());
        if (net.isConnectingToInternet()) {
            String Url = PATH + "AssistedOrderdisplayheader?Locationcode=" + Location; /*+ cust_mobile;*/
            makeFetchOrdersApiCall(Url);

        } else {
            AlertManager alert = new AlertManager();
            alert.alert(ActivityLoadOrders.this, "No Internet Connection",
                    "Please check " +
                            "your data connection or Wifi is ON !");
        }

        ((OrderRecyclerViewAdapter) adapter).setOnItemClickListener(new OrderRecyclerViewAdapter
                .MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {

                OrderID = adapter.orders.get(position).getOrderID();
                customerName = adapter.orders.get(position).getCustName();
                customerMobile = adapter.orders.get(position).getCustMobile();

                String Url = PATH + "AssistedOrderdisplaydetail?Orderid=" + OrderID; /*+ cust_mobile;*/
                makeFetchOrdersDetailsApiCall(Url);
            }


        });

        Log.e(TAG, "Page Loaded");
    }

    private void makeFetchOrdersApiCall(String urlPath) {
        Log.e(TAG, "Api Call: " + urlPath);
        ApiCall.make(this, urlPath, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.e(TAG, "Api Call response: " + response);
                            JSONObject responseObject = new JSONObject(response);
                            String status = responseObject.getString("statusCode");
                            ArrayList<OrderInfo> orderList = null;
                            if (status.equalsIgnoreCase("200")) {

                                JSONArray orders = responseObject.getJSONArray("details");

                                orderList = new ArrayList<OrderInfo>();
                                for (int i = 0; i < orders.length(); i++) {
                                    OrderInfo order = new OrderInfo();
                                    JSONObject json_orders;
                                    json_orders = orders.getJSONObject(i);
                                    order.setOrderID(json_orders.optString("order_id"));
                                    order.setOrderDate(json_orders.optString("Order_date"));
                                    order.setCustName(json_orders.optString("customer_name"));
                                    order.setCustMobile(json_orders.optString("mobile_no"));
                                    order.setDelType(json_orders.optString("order_type"));
                                    orderList.add(order);
                                }

                                adapter = new OrderRecyclerViewAdapter(ActivityLoadOrders.this, orderList);
                                recyclerView.setAdapter(adapter);

                            } else {
                                String msg = responseObject.getString("Message");
                                showFailedAlert(msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "JSON Exception <<::>> " + e);
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley Error " + error);
                    }
                }
        );
    }

    private void makeFetchOrdersDetailsApiCall(String urlPath) {
        Log.e(TAG, "Api Call: " + urlPath);
        ApiCall.make(this, urlPath, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.e(TAG, "Api Call response: " + response);
                            JSONObject responseObject = new JSONObject(response);
                            String status = responseObject.getString("statusCode");
                            ArrayList<OrderInfo> orderList = null;
                            if (status.equalsIgnoreCase("200")) {

                                JSONArray orders = responseObject.getJSONArray("details");
                                OrderedProductDetailsDB podb = new OrderedProductDetailsDB(getApplicationContext());
                                podb.open();
                                podb.createOrderedProductDetailsTable();
                                ArrayList<ProductList> order = podb.getAllEANDetails(OrderID);
                                if (order.size() <= 0) {
                                    podb.insertBulkPOSKUDetails(OrderID, orders);
                                }
                                podb.close();


                                //skuList = readFinalData();
                                //adapter = new ListAdapter(ActivityLoadOrders.this, skuList);
                                //listView.setAdapter(adapter);


                                //adapter = new OrderRecyclerViewAdapter(ActivityLoadOrders.this, orderList);
                                //recyclerView.setAdapter(adapter);
                                editor.putString("orderID", OrderID);
                                editor.putString("customerName", customerName);
                                editor.putString("customerMobile", customerMobile);
                                editor.commit();
                                Intent intent = new Intent(ActivityLoadOrders.this, ActivityScanOrderedProducts.class);
                                startActivity(intent);
                                finish();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Exception JSON <<::>> " + e);
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

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(ActivityLoadOrders.this)
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
        Log.e(TAG, "Clicked on Back");
        View view = ActivityLoadOrders.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        Intent i = new Intent(ActivityLoadOrders.this, LoyaltyActivity.class);
        startActivity(i);
        finish();
    }

    public void showFailedAlert(final String msg) {

        ActivityLoadOrders.this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityLoadOrders.this);
                builder.setTitle("HnG Order Processing");
                builder.setMessage(msg)
                        .setCancelable(false)
                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
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
