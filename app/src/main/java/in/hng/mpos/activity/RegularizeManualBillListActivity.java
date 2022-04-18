package in.hng.mpos.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import in.hng.mpos.Adapter.RegularizeManualBillAdapter;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.RegularizeManualBill.Detail;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.helper.AppController;
import in.hng.mpos.helper.Log;

public class RegularizeManualBillListActivity extends BaseActivity {

    private static final String TAG = "RegularizeManualBillAct";

    RecyclerView rvManualBill;
    TextView tvNoData;
    RegularizeManualBillAdapter regularizeManualBillAdapter;

    HashMap<String, List<Detail>> dataMap = new HashMap<>();
    List<Detail> tempList = new ArrayList<>();
    String PATH, locationCode;

    ProgressDialog progressDialog;
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regularize_manual_bill_list);

        // Set up your ActionBar
        setActionBarTitle("h&g mPOS - Regularize Manual Bill");

        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH = urlDB.getUrlDetails();
        Log.w(TAG, "URL PATH<<::>> " + PATH);
        urlDB.close();

        UserDB userDB = new UserDB(getApplicationContext());
        userDB.open();
        ArrayList<UserDetails> userDetails = userDB.getUserDetails();
        userDB.close();
        if (userDetails.size() > 0) {
            locationCode = userDetails.get(0).getStoreID();
            Log.w(TAG, "Location Code <<<:::>>> " + locationCode);
        }

        regularizeManualBillAdapter = new RegularizeManualBillAdapter(this, tempList);
        tvNoData = findViewById(R.id.tvNoData);
        rvManualBill = findViewById(R.id.rvManualBill);
        rvManualBill.setLayoutManager(new LinearLayoutManager(this));
        rvManualBill.setHasFixedSize(true);
        rvManualBill.setAdapter(regularizeManualBillAdapter);

    }

    private void getManualBillList() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching data.. Please wait..");
        progressDialog.setCancelable(true);
        progressDialog.show();

        try {

            RequestQueue queue = AppController.getInstance().getRequestQueue();
            final String url = PATH + "mbbBilledDetails?locationCode=" + locationCode;
            Log.d(TAG, "Regularize Manual Bill URL <<::>> " + url);

            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d(TAG, "JSON Response <<::>> " + response);
                            tempList.clear();
                            regularizeManualBillAdapter.clearList();

                            if (progressDialog != null && progressDialog.isShowing())
                                progressDialog.dismiss();

                            try {

                                String statusCode = response.getString("statusCode");

                                if (statusCode.equalsIgnoreCase("200")) {

                                    JSONArray jsonArray = response.getJSONArray("detail");
                                    for (int i = 0; i < jsonArray.length(); i++) {

                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        Detail detail = gson.fromJson(jsonObject.toString(), Detail.class);
                                        tempList.add(detail);

                                        /*if (dataMap.containsKey(detail.getBillNo())) {
                                            List<Detail> dataList = dataMap.get(detail.getBillNo());
                                            dataList.add(detail);
                                        } else {
                                            List<Detail> dataList = new ArrayList<>();
                                            dataList.add(detail);
                                            //dataMap.put(detail.getBillNo(), dataList);
                                            tempList.add(detail);
                                        }*/
                                    }

                                    Collections.sort(tempList, new Comparator<Detail>() {
                                        public int compare(Detail o1, Detail o2) {
                                            if (o1.getActualBillNo().isEmpty()) {
                                                return (o2.getActualBillNo().isEmpty()) ? 0 : -1;
                                            }
                                            if (o2.getActualBillNo().isEmpty()) {
                                                return 1;
                                            }
                                            return o2.getActualBillNo().compareTo(o1.getActualBillNo());
                                        }
                                    });

                                    rvManualBill.setAdapter(regularizeManualBillAdapter);
                                    regularizeManualBillAdapter.notifyDataSetChanged();

                                } else {
                                    String message = response.getString("Message");
                                    showAlertMessage(TAG, message);

                                    tvNoData.setText(message);
                                }

                            } catch (JSONException e) {
                                if (progressDialog != null && progressDialog.isShowing())
                                    progressDialog.dismiss();

                                Log.e(TAG, "Response Exception <<::>> " + e);
                                e.printStackTrace();
                            }
                            tvNoData.setVisibility(tempList.size() > 0 ? View.GONE : View.VISIBLE);
                            rvManualBill.setVisibility(tempList.size() > 0 ? View.VISIBLE : View.GONE);
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

    @Override
    protected void onResume() {
        super.onResume();
        getManualBillList();
    }
}
