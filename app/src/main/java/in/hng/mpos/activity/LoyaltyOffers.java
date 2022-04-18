package in.hng.mpos.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
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
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import in.hng.mpos.Database.CustomerDB;
import in.hng.mpos.Database.LoyaltyDetailsDB;
import in.hng.mpos.Database.ProductDetailsDB;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.R;
import in.hng.mpos.Adapter.SelectOfferListAdapter;
import in.hng.mpos.gettersetter.CustomerDetails;
import in.hng.mpos.gettersetter.LoyaltyDetails;
import in.hng.mpos.gettersetter.OfferList;
import in.hng.mpos.gettersetter.ProductList;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.helper.AlertManager;
import in.hng.mpos.helper.AppController;
import in.hng.mpos.helper.ConnectionDetector;
import in.hng.mpos.helper.Log;

/**
 * Created by Cbly on 10-Mar-18.
 */

public class LoyaltyOffers extends AppCompatActivity {

    TextView name, email, mobile, points, tier;
    ArrayList<LoyaltyDetails> offerDetails;
    OfferListAdapter adapter;
    ListView listView;
    Button ok_process;
    String billno, PATH;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    ArrayList<String> selectedOffers = SelectOfferListAdapter.getArrayList();
    ArrayList<OfferList> offer_list;
    OfferList offerLists = new OfferList();
    private static final String TAG = "Loyalty Offer";
    private ProgressDialog dialog = null;
    private Handler handler;
    Runnable myRunnable;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loyalty_details);
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
        title.setText("h&g mPOS - Billing");
        title.setVisibility(View.VISIBLE);

        name = findViewById(R.id.txtloyalname);
        email = findViewById(R.id.txtloyalemail);
        mobile = findViewById(R.id.txtloyalphoneNo);
        points = findViewById(R.id.txtlylpoints);
        tier = findViewById(R.id.txtlyltier);
        listView = findViewById(R.id.listoffers);
        ok_process = findViewById(R.id.btnok);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        billno = sp.getString("Bill_no", "");


        selectedOffers.clear();

        //Resources res = getResources();
        //PATH = res.getString(R.string.API_URL);

        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH =urlDB.getUrlDetails();
        urlDB.close();


        CustomerDB cusDB = new CustomerDB(getApplicationContext());
        cusDB.open();
        ArrayList<CustomerDetails> customerDetails = cusDB.getCustomerDetails();
        cusDB.close();
        Log.i(TAG, "Loading Loyalty Offers");
        if (customerDetails.size() > 0) {
            name.setText(customerDetails.get(0).getCustomerName());
            email.setText(customerDetails.get(0).getMailID());
            mobile.setText(customerDetails.get(0).getMobileNO());
            points.setText(customerDetails.get(0).getPoints());
            tier.setText(customerDetails.get(0).getTier());

        }

        LoyaltyDetailsDB podb = new LoyaltyDetailsDB(getApplicationContext());
        podb.open();
        final ArrayList<LoyaltyDetails> offerList = podb.checkOfferList();
        podb.close();
        if (offerList.size() > 0) {

            offerDetails = readFinalData();
            offer_list = new ArrayList<OfferList>();
            SelectOfferListAdapter adapter = new SelectOfferListAdapter(LoyaltyOffers.this, offerDetails);
            listView.setAdapter(adapter);


        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {


                String offer = offerDetails.get(position).getCustomerOfferID();
                if (selectedOffers.contains(offer)) {
                    selectedOffers.remove(offer);
                } else {
                    selectedOffers.add(offer);
                }

                listView.invalidateViews();

            }

        });

        ok_process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectionDetector net = new ConnectionDetector(getApplicationContext());
                if (net.isConnectingToInternet()) {

                    dialog = ProgressDialog.show(LoyaltyOffers.this, "",
                            "Processing Bill...", true);

                    handler = new Handler();
                    handler.postDelayed(myRunnable = new Runnable() {
                        @Override
                        public void run() {

                            dialog.dismiss();
                        }
                    }, 30000);

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
                        Headerobj.put("billno", billno.isEmpty() ? "" : billno);
                        Headerobj.put("LocationCode", userdata.get(0).getStoreID());
                        Headerobj.put("CustomerName", customerdata.get(0).getCustomerName());
                        Headerobj.put("PhoneNo", customerdata.get(0).getMobileNO());
                        Headerobj.put("UserCode", userdata.get(0).getUserID());
                        Headerobj.put("MailID", customerdata.get(0).getMailID());
                        Headerobj.put("Gender", customerdata.get(0).getGender());
                        Headerobj.put("DOB", customerdata.get(0).getDob().equalsIgnoreCase("null") ? "" : customerdata.get(0).getDob());
                        Headerobj.put("Anniversary", customerdata.get(0).getAnniversary().equalsIgnoreCase("null") ? "" : customerdata.get(0).getAnniversary());
                        Headerobj.put("tillNo", userdata.get(0).getTillNo());
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
                                Projsonobj.put("tillNo", userdata.get(0).getTillNo());
                                productarray.put(i, Projsonobj);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        LoyaltyDetailsDB lyltydb = new LoyaltyDetailsDB(getApplicationContext());
                        lyltydb.open();
                        ArrayList<in.hng.mpos.gettersetter.LoyaltyDetails> offerList = lyltydb.checkOfferList();
                        lyltydb.close();

                        for (int i = 0; i < offerList.size(); i++) {

                            try {
                                JSONObject Offrjsonobj = new JSONObject();
                                int j = 0;
                                j = i + 1;
                                Offrjsonobj.put("MOBILE_NO", customerdata.get(0).getMobileNO().equalsIgnoreCase("null") ? "" : customerdata.get(0).getMobileNO());
                                Offrjsonobj.put("CUST_ID", customerdata.get(0).getCustomerID().equalsIgnoreCase("null") ? "" : customerdata.get(0).getCustomerID());
                                Offrjsonobj.put("CUST_OFFER_ID", offerList.get(i).getCustomerOfferID().equalsIgnoreCase("null") ? "" : offerList.get(i).getCustomerOfferID());
                                Offrjsonobj.put("OFFER_DESC", offerList.get(i).getOfferDESC().equalsIgnoreCase("null") ? "" : offerList.get(i).getOfferDESC());
                                Offrjsonobj.put("TO_TIME", offerList.get(i).getToTime().equalsIgnoreCase("null") ? "" : offerList.get(i).getToTime());
                                Offrjsonobj.put("VALID_FROM", offerList.get(i).getValidFrom().equalsIgnoreCase("null") ? "" : offerList.get(i).getValidFrom());
                                Offrjsonobj.put("VALID_TO", offerList.get(i).getValidTo().equalsIgnoreCase("null") ? "" : offerList.get(i).getValidTo());
                                Offrjsonobj.put("NAME", offerList.get(i).getName().equalsIgnoreCase("null") ? "" : offerList.get(i).getName());
                                Offrjsonobj.put("OFFER_CODE", offerList.get(i).getOfferCode().equalsIgnoreCase("null") ? "" : offerList.get(i).getOfferCode());
                                Offrjsonobj.put("OFFER_ID", offerList.get(i).getOfferID().equalsIgnoreCase("null") ? "" : offerList.get(i).getOfferID());
                                Offrjsonobj.put("OFFER_TYPE", offerList.get(i).getOfferType().equalsIgnoreCase("null") ? "" : offerList.get(i).getOfferType());
                                Offrjsonobj.put("OFFER_VALID_DAYS", offerList.get(i).getOfferValidDays().equalsIgnoreCase("null") ? "" : offerList.get(i).getOfferValidDays());
                                Offrjsonobj.put("OFFER_VALUE", offerList.get(i).getOfferValue().equalsIgnoreCase("null") ? "" : offerList.get(i).getOfferValue());
                                Offrjsonobj.put("OFFER_VALUE_TYPE", offerList.get(i).getOfferValueType().equalsIgnoreCase("null") ? "" : offerList.get(i).getOfferValueType());
                                Offrjsonobj.put("OUTLET_NAME", offerList.get(i).getOutletName().equalsIgnoreCase("null") ? "" : offerList.get(i).getOutletName());
                                Offrjsonobj.put("SKU_CODE", offerList.get(i).getSkuCode().equalsIgnoreCase("null") ? "" : offerList.get(i).getSkuCode());
                                Offrjsonobj.put("PURCHASE_VAL1", offerList.get(i).getPurchaseVal1().equalsIgnoreCase("null") ? "" : offerList.get(i).getPurchaseVal1());
                                Offrjsonobj.put("PURCHASE_VAL2", offerList.get(i).getPurchaseVal2().equalsIgnoreCase("null") ? "" : offerList.get(i).getPurchaseVal2());
                                Offrjsonobj.put("STORE_NAME", offerList.get(i).getStoreName().equalsIgnoreCase("null") ? "" : offerList.get(i).getStoreName());
                                Offrjsonobj.put("LOCATION_CODE", userdata.get(0).getStoreID().equalsIgnoreCase("null") ? "" : userdata.get(0).getStoreID());
                                offerarray.put(i, Offrjsonobj);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        try {

                            for (int i = 0; i < selectedOffers.size(); i++) {
                                JSONObject OffrCodejsonobj = new JSONObject();
                                OffrCodejsonobj.put("OfferID", selectedOffers.get(i));
                                offercodearray.put(i, OffrCodejsonobj);
                            }


                            //int j = 0;
                            //OffrCodejsonobj.put("OfferID", "8229");


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        try {
                            finaljsonobj.put("detailLog", productarray);
                            finaljsonobj.put("header", Headerjsonarray);
                            finaljsonobj.put("loyaltydata", offerarray);
                            finaljsonobj.put("loyaltypromo", offercodearray);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
                        String URL = PATH + "billdetaillog";
                        Log.i(TAG, "Api Call: "+URL);
                        final String mRequestBody = finaljsonobj.toString();


                        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.i(TAG, "Api Call response: "+response);
                                try {

                                    JSONObject Jsonobj = new JSONObject(response);
                                    String status = Jsonobj.getString("statusCode");

                                    if (status.equalsIgnoreCase("200")) {
                                        Log.i(TAG, "Bill processing completed");
                                        billno = Jsonobj.getString("bill_no");
                                        JSONArray refershLog = Jsonobj.getJSONArray("refershLog");
                                        ProductDetailsDB podb = new ProductDetailsDB(getApplicationContext());
                                        podb.open();
                                        podb.deleteUserTable();
                                        podb.createProductDetailsTable();
                                        podb.insertBulkPOSKUDetails(refershLog);
                                        podb.close();

                                        UserDB userDB = new UserDB(getApplicationContext());
                                        userDB.open();
                                        ArrayList<UserDetails> userDetailses = userDB.getUserDetails();
                                        userDB.close();

                                        dialog.dismiss();
                                        handler.removeCallbacks(myRunnable);
                                        editor.putString("Bill_no", Jsonobj.getString("bill_no"));
                                        editor.putString("Location_code", userDetailses.get(0).getStoreID());
                                        editor.putString("Cashier_code", userDetailses.get(0).getUserID());
                                        editor.putString("Loyalty_Disc", Jsonobj.getString("LoyaltyDisc"));
                                        editor.putString("Total_Amt", Jsonobj.getString("Bill_Value"));
                                        editor.putString("Disc_Amt", Jsonobj.getString("Discount"));
                                        editor.putString("bbPromo", Jsonobj.getString("bbPromo"));
                                        editor.putString("Promo_Txt", Jsonobj.getString("PromoTxt"));
                                        editor.commit();
                                        Intent intent = new Intent(LoyaltyOffers.this, ProcessActivity.class);
                                        startActivity(intent);
                                        finish();
                                        Log.d("My App", Jsonobj.toString());
                                    } else {
                                        dialog.dismiss();
                                        handler.removeCallbacks(myRunnable);
                                        String msg = Jsonobj.getString("Message");
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
                } else {
                    AlertManager alert = new AlertManager();
                    alert.alert(LoyaltyOffers.this, "No Internet Connection",
                            "Please check " +
                                    "your data connection or Wifi is ON !");

                }
            }


        });
    }

    public ArrayList<in.hng.mpos.gettersetter.LoyaltyDetails> readFinalData() {
        LoyaltyDetailsDB lyltyDB = new LoyaltyDetailsDB(getApplicationContext());
        lyltyDB.open();
        ArrayList<in.hng.mpos.gettersetter.LoyaltyDetails> offers = lyltyDB.getOfferDetails();

        lyltyDB.close();

        return offers;

    }

    public void showFailedAlert(final String msg) {

        LoyaltyOffers.this.runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, msg);
                AlertDialog.Builder builder = new AlertDialog.Builder(LoyaltyOffers.this);
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

        new AlertDialog.Builder(LoyaltyOffers.this)
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
        Log.i(TAG, "Back Button Clicked ");
        Intent i = new Intent(LoyaltyOffers.this, MainActivity.class);
        startActivity(i);
        finish();

    }


}
