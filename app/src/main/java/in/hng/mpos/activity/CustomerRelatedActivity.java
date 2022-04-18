package in.hng.mpos.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.R;
import in.hng.mpos.Utils.Constants;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.helper.AppController;
import in.hng.mpos.helper.Log;

public class CustomerRelatedActivity extends BaseActivity implements View.OnClickListener {

    private String TAG = CustomerRelatedActivity.class.getSimpleName();

    Button btnCustomerComplaint, btnCustomerFeedback, btnCustomerRequest,btnRequestForm;
    EditText etMobileNumber;
    ProgressDialog progressDialog;

    String PATH, IPADDRESS, LocationCode, userMobileNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_related);

        init();

        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH = urlDB.getUrlDetails();
        IPADDRESS = urlDB.getIpUrlDetails();
        urlDB.close();

        UserDB uDB = new UserDB(getApplicationContext());
        uDB.open();
        ArrayList<UserDetails> userDetails = uDB.getUserDetails();
        uDB.close();


        LocationCode = userDetails.get(0).getStoreID();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Intent intent = getIntent();
            if (intent.hasExtra(Constants.MOBILE_NUMBER)) {
                userMobileNumber = bundle.getString(Constants.MOBILE_NUMBER);
                Log.w(TAG, "userMobileNumber######### " + userMobileNumber);
                etMobileNumber.setText(userMobileNumber);
            }
        }
    }

    private void init() {
        etMobileNumber = findViewById(R.id.et_mobile_number);
        btnCustomerComplaint = findViewById(R.id.btn_customer_complaint);
        btnCustomerFeedback = findViewById(R.id.btn_customer_feedback);
        btnCustomerRequest = findViewById(R.id.btn_customer_request);
        btnRequestForm = findViewById(R.id.btn_request_form);


        btnCustomerComplaint.setOnClickListener(this);
        btnCustomerFeedback.setOnClickListener(this);
        btnCustomerRequest.setOnClickListener(this);
        btnRequestForm.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btn_customer_complaint:
                if (validate())
                    sendMessage(etMobileNumber.getText().toString().trim(), "compliant");
                break;

            case R.id.btn_customer_feedback:
                if (validate())
                    sendMessage(etMobileNumber.getText().toString().trim(), "feedback");
                break;

            case R.id.btn_customer_request:
                if (validate())
                    sendMessage(etMobileNumber.getText().toString().trim(), "request");
                break;

            case R.id.btn_request_form:
                Intent intent = new Intent(CustomerRelatedActivity.this, CustomerRequestForm.class);
                startActivity(intent);
                break;
        }
    }

    private void sendMessage(String mobileNo, String type) {

        disableButton();
        Log.w(TAG, "Customer Related " + type + " LocationCode " + LocationCode);
        hideKeyboard(etMobileNumber);

        try {

            RequestQueue queue = AppController.getInstance().getRequestQueue();
            final String url = PATH + "register?mobileNo=" + mobileNo + "&transType=" + type + "&LocationCode=" + LocationCode;

            Log.d("Url Adress:", url);
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.w(TAG, "Customer Related Response " + response);
                            try {

                                String statusCode = response.getString("statusCode");

                                Log.w(TAG, "STATUS CODE CUSTOMER RELATED " + statusCode);

                                String message = response.getString("Message");
                                showAlertMessage(TAG, message);

                                if (statusCode.equalsIgnoreCase("200"))
                                    etMobileNumber.setText(null);

                                enableButton();

                            } catch (JSONException e) {
                                e.printStackTrace();
                                enableButton();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            enableButton();
                            showVolleyError(TAG, error);
                        }
                    }
            ) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() {
                    return super.getBody();
                }
            };

            queue.add(getRequest);

        } catch (Exception e) {
            showAlertMessage(TAG, e.getMessage());
            Log.e(TAG, e.getMessage());
            System.out.println("Exception : " + e.getMessage());
            enableButton();
        }
    }

    private void enableButton() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            btnCustomerComplaint.setEnabled(true);
            btnCustomerFeedback.setEnabled(true);
            btnCustomerRequest.setEnabled(true);
        }
    }

    private void disableButton() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending Message.. Please wait..");
        progressDialog.setCancelable(false);
        progressDialog.show();

        btnCustomerComplaint.setEnabled(false);
        btnCustomerFeedback.setEnabled(false);
        btnCustomerRequest.setEnabled(false);
    }

    private boolean validate() {
        if (TextUtils.isEmpty(etMobileNumber.getText().toString().trim())) {
            showAlertMessage(TAG, "Mobile Number cannot be Empty!");
            return false;
        } else if (etMobileNumber.getText().toString().trim().length() > 10) {
            showAlertMessage(TAG, "Invalid Mobile Number!");
            return false;
        } else
            return true;
    }

    private void hideKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //if (userMobileNumber.isEmpty()) {
        Intent intent = new Intent(CustomerRelatedActivity.this, LoyaltyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        //}

    }
}
