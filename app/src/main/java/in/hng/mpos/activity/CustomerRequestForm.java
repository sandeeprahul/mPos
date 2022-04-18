package in.hng.mpos.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import in.hng.mpos.Database.CustomerDB;
import in.hng.mpos.Database.LoyaltyCredentialsDB;
import in.hng.mpos.Database.LoyaltyDetailsDB;
import in.hng.mpos.Database.ProductDetailsDB;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.R;
import in.hng.mpos.Utils.Constants;
import in.hng.mpos.gettersetter.LoyaltyDetails;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.helper.AlertManager;
import in.hng.mpos.helper.ApiCall;
import in.hng.mpos.helper.AppController;
import in.hng.mpos.helper.ConnectionDetector;
import in.hng.mpos.helper.Log;

public class CustomerRequestForm extends AppCompatActivity {

    private String TAG = CustomerRequestForm.class.getSimpleName();
    private static final String TAGG = "CustomerRequestFormActivity";


    Button btnSubmitRequestForm, btnNewRequestForm;
    String PATH;
    EditText phoneNumberRequestForm, firstNameRequestForm, emailRequestForm, dateRequestForm, addressRequestForm, requestTextForm, skuCodeRequestForm, skuNameRequestForm,skuQuantity;
    String url = "";
    private ProgressDialog dialog = null;
    private ProgressDialog progressDialog = null;
    private Handler handler = new Handler();
    Runnable myRunnable;
    String FirstName, LastName, Email, loyalMobile, loyalPoints, loyalTier, gender, dob, anniversary;
    ArrayList<LoyaltyDetails> offerDetails;
    String LoyaltyID, LoyaltyPWD, Location, UserID, TillNo, storeEmail, storePwd, toEmail;
    String StoreId, StoreOutletId, AccountId, AccountId_1, CustomerId, IPADDRESS;
    String statusCode,sku_name, skuCode,eanCode,Msg;
    JSONArray jsonArrayProduct, jsonArray;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_request_form);


        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH = urlDB.getUrlDetails();

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


        btnSubmitRequestForm = findViewById(R.id.btnSubmit_request_form);
        phoneNumberRequestForm = findViewById(R.id.phone_number_request_form);
        firstNameRequestForm = findViewById(R.id.first_name_request_form);
        emailRequestForm = findViewById(R.id.email_request_form);
        dateRequestForm = findViewById(R.id.date_request_form);
        addressRequestForm = findViewById(R.id.address_request_form);
        requestTextForm = findViewById(R.id.request_text_form);
        skuCodeRequestForm = findViewById(R.id.sku_code_request_form);
        skuNameRequestForm = findViewById(R.id.sku_name_request_form);

        btnNewRequestForm = findViewById(R.id.btn_new_request_form);
        skuQuantity = findViewById(R.id.sku_quantity);


        Date cDate = new Date();
        String todaysDateString = new SimpleDateFormat("yyyy-MM-dd").format(cDate);


        dateRequestForm.setText(todaysDateString);
        dateRequestForm.setEnabled(false);
        phoneNumberRequestForm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (editable.length() == 10) {
                    onClickSearch();
                    /*Toast toast = Toast.makeText(CustomerRequestForm.this, " successfully", Toast.LENGTH_SHORT);
                    toast.show();*/
                }
            }
        });


        skuCodeRequestForm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (editable.length() == 6 || editable.length() == 13) {
                    searchEan();
                }
            }
        });


        btnSubmitRequestForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog = ProgressDialog.show(CustomerRequestForm.this, "",
                        "Processing ...", true);


                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("requestType", "request");
                    jsonObject.put("mobileNo", phoneNumberRequestForm.getText().toString());
                    jsonObject.put("date", todaysDateString);
                    jsonObject.put("customerName", firstNameRequestForm.getText().toString());
                    jsonObject.put("locationCode", Location);
                    jsonObject.put("email", emailRequestForm.getText().toString());
                    jsonObject.put("address", addressRequestForm.getText().toString());
                    jsonObject.put("requestRemarks", requestTextForm.getText().toString());
                    jsonObject.put("createdBy", UserID);
                    jsonObject.put("skuCode", skuCodeRequestForm.getText().toString());
                    jsonObject.put("skuName", skuNameRequestForm.getText().toString());
                    jsonObject.put("skuQty", skuQuantity.getText().toString());


                    submitReuestFormData(jsonObject.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });


        btnNewRequestForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFOrmData();

            }
        });


    }

    private void clearFOrmData() {
        phoneNumberRequestForm.setText("");
        firstNameRequestForm.setText("");
        emailRequestForm.setText("");
        addressRequestForm.setText("");
        requestTextForm.setText("");
        skuCodeRequestForm.setText("");
        skuNameRequestForm.setText("");
        skuQuantity.setText("");
        firstNameRequestForm.setEnabled(true);
        emailRequestForm.setEnabled(true);
     //   btnSubmitRequestForm.setEnabled(true);

    }

    private void searchEan() {


        String urlPath = PATH + "manualgeteandetail?ean_code=" + skuCodeRequestForm.getText().toString() + "&location=" + Location;
        Log.w(TAG, "URL PAth " + urlPath);

        ApiCall.make(this, urlPath, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "Api Call response: " + response);
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            statusCode = responseObject.getString("statusCode");
                            Log.e(TAG, "Status Code ====> " + statusCode);

                            if (statusCode.equalsIgnoreCase("200")) {

                                jsonArrayProduct = responseObject.getJSONArray("product");
                                JSONObject json_product;
                                json_product = jsonArrayProduct.getJSONObject(0);

                                sku_name = json_product.getString("SKU_NAME");
                                skuCode = json_product.getString("SKU_CODE");
                                eanCode = json_product.getString("ean_code");
                                skuNameRequestForm.setText(sku_name);

                            } else {

                                Log.d(TAG, "Else Statement");
                                Log.d(TAG, "Status Code ==> " + statusCode);
                                Msg = responseObject.getString("Message");
                                skuNameRequestForm.setText("");
                                Toast toast = Toast.makeText(CustomerRequestForm.this, Msg, Toast.LENGTH_SHORT);
                                 toast.show();
                            }

                          /*  if (skuCode.isEmpty() && etSkuCode.getText().toString().trim().length() < 7)
                                showQtyMrpDialog(etSkuCode.getText().toString().trim());
                            else
                                showQtyMrpDialog(skuCode);*/

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        showVolleyError(error);
                      //  showQtyMrpDialog(etSkuCode.getText().toString().trim());
                    }
                }
        );
    }


    public void cleardetails() {
        try {

            firstNameRequestForm.setText("");
            emailRequestForm.setText("");
            firstNameRequestForm.setEnabled(true);
            emailRequestForm.setEnabled(true);
            addressRequestForm.setText("");
            requestTextForm.setText("");
            // phoneNumberRequestForm.setText("");


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
            Log.e(TAG, "Exception <<::>> " + e);
        }


    }

    private void showFailedAlert(final String msg) {

        this.runOnUiThread(new Runnable() {
            public void run() {

                if (!(CustomerRequestForm.this).isFinishing()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CustomerRequestForm.this);
                    builder.setTitle("HnG POS");
                    builder.setMessage(msg)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });
    }

    private void dismissDialog() {
        try {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showVolleyError(VolleyError error) {
        try {
            if (error instanceof TimeoutError) {
                showFailedAlert("Time out error occurred.Please click on OK and try again");
                Log.e(TAG, "Time out error occurred.");

            } else if (error instanceof NoConnectionError) {
                showFailedAlert("Network error occurred.Please click on OK and try again");
                Log.e(TAG, "Network error occurred.");

            } else if (error instanceof AuthFailureError) {
                showFailedAlert("Authentication error occurred.Please click on OK and try again");
                Log.e(TAG, "Authentication error occurred.");

            } else if (error instanceof ServerError) {
                showFailedAlert("Server error occurred.Please click on OK and try again");
                Log.e(TAG, "Server error occurred.");

            } else if (error instanceof NetworkError) {
                showFailedAlert("Network error occurred.Please click on OK and try again");
                Log.e(TAG, "Network error occurred.");

            } else if (error instanceof ParseError) {

                showFailedAlert("An error occurred.Please click on OK and try again");
                Log.e(TAG, "An error occurred.");
            } else {

                showFailedAlert("An error occurred.Please click on OK and try again");
                Log.e(TAG, "An error occurred.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void onClickSearch() {
        View v = CustomerRequestForm.this.getCurrentFocus();
      /* if (v != null) {
           InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
           imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
       }*/


        try {
            ConnectionDetector net = new ConnectionDetector(getApplicationContext());
            if (net.isConnectingToInternet()) {
                progressDialog = ProgressDialog.show(CustomerRequestForm.this, "",
                        "Fetching Details...", true);
                Log.i(TAG, "Searching...");
                handler = new Handler();
                handler.postDelayed(myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (progressDialog != null) progressDialog.dismiss();
                    }
                }, 30000);
                if (phoneNumberRequestForm.getText().toString().length() >= 10) {

                    //search.setEnabled(false);


                    CustomerDB custDB = new CustomerDB(getApplicationContext());
                    custDB.open();
                    custDB.deleteCustomerTable();
                    custDB.close();

                    LoyaltyDetailsDB offrDB = new LoyaltyDetailsDB(getApplicationContext());
                    offrDB.open();
                    offrDB.deleteLoyaltyTable();
                    offrDB.close();

              /* name.setText("");
               email.setText("");
               phone.setText("");
               points.setText("");
               tier.setText("");*/


                    Authenticate();


                } else {
                    if (progressDialog != null) progressDialog.dismiss();
                    //  search.setEnabled(true);
                    try {
                        handler.removeCallbacks(myRunnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    showFailedAlert("Please Provide Valid Mobile Number");
                }
            } else {

                AlertManager alert = new AlertManager();
                alert.alert(CustomerRequestForm.this, "No Internet Connection",
                        "Please check " +
                                "your data connection or Wifi is ON !");
            }
        } catch (Exception e) {
            e.getMessage();
        }

    }

    public void Authenticate() {
        if (progressDialog != null) progressDialog.dismiss();
        try {
            handler.removeCallbacks(myRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressDialog = ProgressDialog.show(CustomerRequestForm.this, "",
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
                                // search.setEnabled(true);
                                try {
                                    handler.removeCallbacks(myRunnable);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                showFailedAlert("Loyalty Authentication Failed");
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
                        //search.setEnabled(true);
                        try {
                            handler.removeCallbacks(myRunnable);
                            if (progressDialog != null) progressDialog.dismiss();
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

        progressDialog = ProgressDialog.show(CustomerRequestForm.this, "",
                "Searching Customer...", true);
        handler = new Handler();
        progressDialog.dismiss();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                if (progressDialog != null) progressDialog.dismiss();
            }
        }, 30000);
        Log.i(TAG, "Searching Customer- " + phoneNumberRequestForm.getText().toString());
        RequestQueue queue = AppController.getInstance().getRequestQueue();
        String url = "http://api.directdialogs.com/api/Customer/SearchCustomer?accountId=" + AccountId + "&mobile=" + phoneNumberRequestForm.getText().toString();
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
                                addressRequestForm.setText("");
                                requestTextForm.setText("");

                                if (FirstName.isEmpty()) {
                                    firstNameRequestForm.setEnabled(true);
                                } else if (!FirstName.isEmpty()) {
                                    firstNameRequestForm.setEnabled(false);
                                    firstNameRequestForm.setText(FirstName);
                                }


                                if (Email.isEmpty()) {
                                    firstNameRequestForm.setEnabled(true);
                                } else if (!Email.isEmpty()) {
                                    emailRequestForm.setText(Email);
                                    emailRequestForm.setEnabled(false);

                                }


                                //  Fetch_Glow_Rewards();


                            } else {
                                if (progressDialog != null) progressDialog.dismiss();
                                // search.setEnabled(true);
                                cleardetails();
                                try {
                                    handler.removeCallbacks(myRunnable);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Log.i(TAG, "Customer not found");
                                Toast toast = Toast.makeText(getApplicationContext(), "Customer not registered with loyalty", Toast.LENGTH_SHORT);
                                toast.show();

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
                        // search.setEnabled(true);
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

    private void goBack() {
        Log.e(TAG, "Clicked on Back");
        View view = CustomerRequestForm.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        Intent i = new Intent(CustomerRequestForm.this, LoyaltyActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(CustomerRequestForm.this)
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

    private void submitReuestFormData(String mRequestBody) {
        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();

        url = PATH + "registerApi";
        Log.w(TAG, "registerApi " + url);
        Log.e("RequestData",mRequestBody);
     //   btnSubmitRequestForm.setEnabled(true);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                  //  btnSubmitRequestForm.setEnabled(true);

                    dismissDialog();

                    JSONObject Jsonobj;
                    Log.w(TAG, "RegisterApi message " + response);
                    Jsonobj = new JSONObject(response);
                    String status = Jsonobj.getString("statusCode");

                    if (status.equalsIgnoreCase("200")) {
                        // btnSubmitRequestForm.setEnabled(false);
                      //  btnSubmitRequestForm.setClickable(false);
                        clearData();

                        Log.d("RegisterApi response", Jsonobj.toString());
                        String message = Jsonobj.getString("Message");
                        showFailedAlert(message);







                        /*ProductDetailsDB prdctDB = new ProductDetailsDB(getApplicationContext());
                        prdctDB.open();
                        prdctDB.deleteUserTable();
                        prdctDB.close();*/

                    } else {
                        showFailedAlert(Jsonobj.getString("Message"));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    dismissDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dismissDialog();
                showVolleyError(error);
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
    }

    private void clearData() {
        phoneNumberRequestForm.setText("");
        firstNameRequestForm.setText("");
        emailRequestForm.setText("");
        addressRequestForm.setText("");
        requestTextForm.setText("");
        skuCodeRequestForm.setText("");
        skuNameRequestForm.setText("");
        skuQuantity.setText("");

    }
}
