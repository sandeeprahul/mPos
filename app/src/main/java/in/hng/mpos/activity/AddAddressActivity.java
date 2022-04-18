
package in.hng.mpos.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import in.hng.mpos.Database.CustomerDB;
import in.hng.mpos.Database.LoyaltyDetailsDB;
import in.hng.mpos.Database.OrderedProductDetailsDB;
import in.hng.mpos.Database.StoreDB;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.CustomerDetails;
import in.hng.mpos.gettersetter.StoreDetails;
import in.hng.mpos.helper.AlertManager;
import in.hng.mpos.helper.ConnectionDetector;
import in.hng.mpos.helper.Log;

public class AddAddressActivity extends BaseActivity {

    private static final String TAG = "AddCustomerAddress";

    private TextInputEditText textInputName;
    private TextInputEditText textInputMobile;
    private TextInputEditText textInputAdd1;
    private TextInputEditText textInputAdd2;
    private TextInputEditText textInputPinCode;
    private Button button;

    private ProgressDialog dialog = null;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private String OrderID;
    private String Mobile = "", Name = "", add1, add2, city, state, pincode, add_type = "", location;
    private String PATH;
    private String title = "HnG Order Taking";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        setActionBarTitle("h&g Assisted Order");

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        editor.apply();
        OrderID = sp.getString("OrderID", "");

        textInputName = (TextInputEditText) findViewById(R.id.text_input_name);
        textInputMobile = findViewById(R.id.text_input_mobile);
        textInputAdd1 = findViewById(R.id.text_input_add1);
        textInputAdd2 = findViewById(R.id.text_input_add2);
        textInputPinCode = findViewById(R.id.text_input_pin_code);

        TextView text = (TextView) findViewById(R.id.txtNick); // retrieve your text view
        text.setText(Html.fromHtml(text.getText().toString() + "<sup>*</sup>"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#3c3c3c"));
        }

        //Resources res = getResources();
        //PATH = res.getString(R.string.API_URL);

        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH = urlDB.getUrlDetails();
        urlDB.close();

        StoreDB storedb = new StoreDB(getApplicationContext());
        storedb.open();
        ArrayList<StoreDetails> storeDetails = storedb.getStoreDetails();
        storedb.close();

        if (storeDetails.size() > 0) {
            location = storeDetails.get(0).getStoreID();
        }

        CustomerDB cusDB = new CustomerDB(getApplicationContext());
        cusDB.open();
        ArrayList<CustomerDetails> customerDetails = cusDB.getCustomerDetails();
        cusDB.close();

        if (customerDetails.size() > 0) {
            Name = customerDetails.get(0).getCustomerName();
            Mobile = customerDetails.get(0).getMobileNO();
        }
        textInputName.setText(Name);
        textInputMobile.setText(Mobile);

        findViewById(R.id.btncancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(AddAddressActivity.this)
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

                                Intent intent = new Intent(AddAddressActivity.this, LoyaltyActivity.class);
                                startActivity(intent);
                                finish();

                            }
                        }).setNegativeButton("No", null)
                        .show();
            }
        });
    }


    public void onClick(View v) {

        Drawable dr = getResources().getDrawable(R.drawable.button_pressed);
        dr.setColorFilter(getResources().getColor(R.color.OrderLightColor), PorterDuff.Mode.SRC_ATOP);

        switch (v.getId()) {
            case R.id.btn:

                if (button == null) {
                    button = (Button) findViewById(v.getId());
                    add_type = "Home";
                } else {
                    button.setBackgroundResource(R.drawable.button_pressed);
                    button = (Button) findViewById(v.getId());
                    add_type = "Home";
                }
                button.setBackgroundDrawable(dr);

                break;

            case R.id.btn2:
                if (button == null) {
                    button = (Button) findViewById(v.getId());
                    add_type = "Work";
                } else {
                    button.setBackgroundResource(R.drawable.button_pressed);
                    button = (Button) findViewById(v.getId());
                    add_type = "Work";
                }
                button.setBackgroundDrawable(dr);

                break;

            default:
                break;
        }
    }

    public void AddAddress(View v) {

        if (add_type.equalsIgnoreCase("")) {
            showFailedAlert(title, "Please select Address type(Work/Home)");
        } else if (validate()) {

            // if (add_type.equalsIgnoreCase(""))
            //    showFailedAlert("Please select Address type(Work/Home)");


            dialog = ProgressDialog.show(AddAddressActivity.this, "",
                    "Adding Address", true);
            add1 = textInputAdd1.getText().toString();
            add2 = textInputAdd2.getText().toString();
            pincode = textInputPinCode.getText().toString();

            JSONObject finaljsonobj = new JSONObject();
            JSONArray Headerjsonarray = new JSONArray();

            try {
                JSONObject Headerobj = new JSONObject();
                Headerobj.put("mobile_no", Mobile);
                Headerobj.put("customer_name", Name);
                Headerobj.put("address_line1", add1);
                Headerobj.put("address_line2", add2);
                Headerobj.put("city", "Hyderabad");
                Headerobj.put("state", "Andhra Pradesh");
                Headerobj.put("pincode", pincode);
                Headerobj.put("address_type", add_type);
                Headerobj.put("location_code", location);
                Headerjsonarray.put(0, Headerobj);

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "JSON Exception 1 <<::>> " + e);
            }

            try {
                finaljsonobj.put("statusCode", "200");
                finaljsonobj.put("status", "Success");
                finaljsonobj.put("details", Headerjsonarray);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Exception 2 <<::>> " + e);
            }

            RequestQueue requestQueue = Volley.newRequestQueue(AddAddressActivity.this);
            String URL = PATH + "AssistedOrderCustdetail";

            final String mRequestBody = finaljsonobj.toString();

            ConnectionDetector net = new ConnectionDetector(getApplicationContext());

            if (net.isConnectingToInternet()) {
                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {

                            Log.d(TAG, "AddAddress Response <<::>> " + response);

                            JSONObject Jsonobj = new JSONObject(response);
                            String status = Jsonobj.getString("statusCode");

                            if (status.equalsIgnoreCase("200")) {
                                dialog.dismiss();

                                Intent intent = new Intent(AddAddressActivity.this, ActivityDeliveryType.class);
                                startActivity(intent);
                                finish();
                                Toast toast = Toast.makeText(getApplicationContext(), "Address added successfully", Toast.LENGTH_SHORT);
                                toast.show();

                            } else {
                                dialog.dismiss();
                                String msg = Jsonobj.getString("Message");
                                Log.e(TAG, msg);
                                showFailedAlert(title, msg);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, "Exception <<::>> "+e);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        try {
                            dialog.dismiss();
                            showVolleyError(TAG, error);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, "Exception <<::>> " + e);
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
                alert.alert(AddAddressActivity.this, "No Internet Connection",
                        "Please check " +
                                "your data connection or Wifi is ON !");
            }
        } else {
            showFailedAlert(title, "All fields are mandatory");
        }

    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(AddAddressActivity.this)
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
        Intent i = new Intent(AddAddressActivity.this, ActivityDeliveryType.class);
        startActivity(i);
        finish();
    }

    public void ClearFields() {
        textInputName.setText("");
        textInputMobile.setText("");
        textInputAdd1.setText("");
        textInputAdd2.setText("");
        textInputPinCode.setText("");
    }

    public boolean validate() {

        if (textInputName.getText().toString().equalsIgnoreCase("")
                || textInputMobile.getText().toString().trim().equalsIgnoreCase("") ||
                textInputAdd1.getText().toString().trim().equalsIgnoreCase("") ||
                textInputAdd2.getText().toString().trim().equalsIgnoreCase("") || textInputPinCode.getText().toString().trim().equalsIgnoreCase("")) {
            return false;
        } else
            return true;
    }

}