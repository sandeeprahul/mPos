package in.hng.mpos.activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import in.hng.mpos.Adapter.RegManualBillDataAdapter;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.RegularizeManualBill.BillDetail;
import in.hng.mpos.gettersetter.RegularizeManualBill.Detail;
import in.hng.mpos.helper.AppConstants;
import in.hng.mpos.helper.AppController;
import in.hng.mpos.helper.Log;

public class RegularizeManualBillActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = RegularizeManualBillActivity.class.getSimpleName();

    List<BillDetail> detailList = new ArrayList<>();
    String billNo, billDate, locationCode, cashierCode, totalBillValue, actualBillNo,customerName,customerPhone;

    TextView tvBillNo, tvBillDate, tvLocationCode, tvCashierID, tvBillAmount, tvPaymentType, tvPaymentAmt, tvOtherAmount,manualBillCustomerName,manualBillCustomerPhone;
    EditText etActualBillNo;
    Button btnUpdate;
    RecyclerView rvBillItem;
    RegManualBillDataAdapter regManualBillDataAdapter;

    Gson gson = new Gson();
    ProgressDialog progressDialog;
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    String PATH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regularize_manual_bill);

        setActionBarTitle("h&g mPOS - Regularize Manual Bill");

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            billNo = bundle.getString(AppConstants.BILL_NO);
            billDate = bundle.getString(AppConstants.BILL_DATE);
            locationCode = bundle.getString(AppConstants.LOCATION_CODE);
            cashierCode = bundle.getString(AppConstants.CASHIER_ID);
            totalBillValue = bundle.getString(AppConstants.TOTAL_BILL);
            actualBillNo = bundle.getString(AppConstants.ACTUAL_BILL_NO);
            customerName = bundle.getString(AppConstants.CUSTOMER_NAME);
            customerPhone = bundle.getString(AppConstants.CUSTOMER_PHONE);

            Log.w(TAG, "BILL NO <<<::>>> " + billNo);
            Log.w(TAG, "BILL Date <<<::>>> " + billDate);
            Log.w(TAG, "Location Code <<<::>>> " + locationCode);
            Log.w(TAG, "Cashier Code <<<::>>> " + cashierCode);
            Log.w(TAG, "Total Bill Value <<<::>>> " + totalBillValue);
            Log.w(TAG, "Actual Bill No <<<::>>> " + actualBillNo);
            Log.w(TAG, "Customer Name <<<::>>> " + customerName);
            Log.w(TAG, "Customer Phone <<<::>>> " + customerPhone);
        }

        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH = urlDB.getUrlDetails();
        Log.w(TAG, "URL PATH<<::>> " + PATH);
        urlDB.close();

        regManualBillDataAdapter = new RegManualBillDataAdapter(detailList);
        rvBillItem = findViewById(R.id.rvBillItem);
        rvBillItem.setLayoutManager(new LinearLayoutManager(this));
        rvBillItem.setNestedScrollingEnabled(false);
        rvBillItem.setHasFixedSize(true);
        rvBillItem.setAdapter(regManualBillDataAdapter);

        tvBillNo = findViewById(R.id.tvBillNo);
        tvBillDate = findViewById(R.id.tvBillDate);
        tvLocationCode = findViewById(R.id.tvLocationCode);
        manualBillCustomerName = findViewById(R.id.manual_bill_customer_name);
        manualBillCustomerPhone = findViewById(R.id.manual_bill_customer_phone);
        tvCashierID = findViewById(R.id.tvCashierID);
        tvBillAmount = findViewById(R.id.tvBillAmount);
        tvPaymentType = findViewById(R.id.tvPaymentType);
        tvPaymentAmt = findViewById(R.id.tvPaymentAmt);
        tvOtherAmount = findViewById(R.id.tvOtherAmount);
        etActualBillNo = findViewById(R.id.etActualBillNo);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(this);

        getManualBillList();

        Date date = null;
        try {
            date = sdf.parse(billDate);
            billDate = dateFormat.format(date);
            tvBillDate.setText(billDate);
        } catch (ParseException e) {
            e.printStackTrace();
            tvBillDate.setText(billDate);
        }

        tvBillNo.setText(billNo);
        tvLocationCode.setText(locationCode);
        tvCashierID.setText(cashierCode);
        manualBillCustomerName.setText(customerName);
        manualBillCustomerPhone.setText(customerPhone);
        tvBillAmount.setText(totalBillValue);

        if (!actualBillNo.isEmpty()) {
            etActualBillNo.setText(actualBillNo);
            etActualBillNo.setEnabled(false);
            btnUpdate.setEnabled(false);
            btnUpdate.setBackgroundColor(Color.parseColor("#CCCCCC"));
        } else {
            etActualBillNo.setEnabled(true);
            btnUpdate.setEnabled(true);
            btnUpdate.setBackgroundColor(Color.parseColor("#FA8C01"));
        }
    }

    private void getManualBillList() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching data.. Please wait..");
        progressDialog.setCancelable(true);
        progressDialog.show();

        try {

            Log.w(TAG, "Bill No for fetching data <<::>> " + billNo);
            RequestQueue queue = AppController.getInstance().getRequestQueue();
            final String url = PATH + "mbbBilledData?bill_no=" + billNo;
            Log.d(TAG, "Regularize Manual Bill URL 2 <<::>> " + url);

            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d(TAG, "JSON Response <<::>> " + response);

                            detailList.clear();

                            if (progressDialog != null && progressDialog.isShowing())
                                progressDialog.dismiss();

                            try {

                                JSONArray billDetail = response.getJSONArray("detail");
                                for (int i = 0; i < billDetail.length(); i++) {
                                    JSONObject jsonObject = billDetail.getJSONObject(i);
                                    detailList.add(gson.fromJson(jsonObject.toString(), BillDetail.class));
                                }
                                regManualBillDataAdapter.notifyDataSetChanged();

                                JSONArray paymentHeader = response.getJSONArray("paymentHeader");
                                JSONObject paymentJson = paymentHeader.getJSONObject(0);
                                tvPaymentAmt.setText(paymentJson.getString("cash_amount_taken"));
                                tvOtherAmount.setText(paymentJson.getString("other_amount"));

                                JSONArray paymentDetail = response.getJSONArray("paymentDetail");
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int k = 0; k < paymentDetail.length(); k++) {

                                    JSONObject jsonObject = paymentDetail.getJSONObject(k);
                                    if (k == 0) {
                                        stringBuilder.append(getPaymentValue(jsonObject.getString("payment_mode_id")));
                                    } else
                                        stringBuilder.append(",").append(getPaymentValue(jsonObject.getString("payment_mode_id")));
                                }
                                tvPaymentType.setText(stringBuilder);

                            } catch (Exception e) {
                                if (progressDialog != null && progressDialog.isShowing())
                                    progressDialog.dismiss();

                                Log.e(TAG, "Response Exception <<::>> " + e);
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            Log.e(TAG, "VolleyEror <<::>> " + error);
                            if (progressDialog != null && progressDialog.isShowing())
                                progressDialog.dismiss();

                            showVolleyError(TAG, error);
                        }
                    }
            ) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };
            queue.add(getRequest);

        } catch (Exception e) {
            Log.e(TAG, "Exception <<::>> " + e);
            showAlertMessage(TAG, e.toString());
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    private String getPaymentValue(String paymentModeId) {
        switch (paymentModeId) {
            case "1":
                return "Cash";

            case "2":
                return "Card";

            case "3":
                return "Coupon";

            case "4":
                return "Loyalty Amt";

            case "5":
                return "Wallet";

            default:
                return "---";
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnUpdate) {

            if (valid()) {

                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Updating Actual bill no.. Please wait");
                progressDialog.setCancelable(false);
                progressDialog.show();

                try {

                    RequestQueue queue = AppController.getInstance().getRequestQueue();
                    String url = PATH + "mbbbillupdate?bill_no=" + tvBillNo.getText().toString().trim() +
                            "&actual_bill_no=" + etActualBillNo.getText().toString() + "&locationCode=" + tvLocationCode.getText().toString()
                            + "&cashierCode=" + tvCashierID.getText().toString();
                    Log.d(TAG, "Regularize Manual Bill URL <<::>> " + url);

                    JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                    Log.e(TAG, "Update Actual Bill Api Call response: " + response);

                                    if (progressDialog != null && progressDialog.isShowing())
                                        progressDialog.dismiss();
                                    try {
                                        Log.i(TAG, "Actual Bill Number Updated Successfully");
                                        showAlertMessage("Actual Bill Number Updated Successfully");

                                        etActualBillNo.setEnabled(false);
                                        btnUpdate.setEnabled(false);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Log.e(TAG, "Exception <<::>> " + e.toString());
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                    Log.e(TAG, "VolleyError <<::>> " + error);
                                    if (progressDialog != null && progressDialog.isShowing())
                                        progressDialog.dismiss();
                                    showVolleyError(TAG, error);
                                }
                            }
                    ) {
                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }
                    };
                    queue.add(getRequest);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e(TAG, "Full api call Exception <<::>> " + ex.toString());
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                }

            }
        }
    }

    private boolean valid() {
        if (etActualBillNo.getText().toString().isEmpty()) {
            showAlertMessage(TAG, "Actual Bill Number cannot be Empty");
            return false;
        } else
            return true;
    }

    private void showAlertMessage(String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, message);
                AlertDialog.Builder builder = new AlertDialog.Builder(RegularizeManualBillActivity.this);
                builder.setTitle("HnG POS");
                builder.setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

}
