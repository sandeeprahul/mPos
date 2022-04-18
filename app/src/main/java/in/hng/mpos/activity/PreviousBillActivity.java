package in.hng.mpos.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import in.hng.mpos.Adapter.PreviousBillAdapter;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.Details;
import in.hng.mpos.gettersetter.PreviousBillApiPojo;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.helper.AlertManager;
import in.hng.mpos.helper.AppController;
import in.hng.mpos.helper.ConnectionDetector;
import in.hng.mpos.helper.Log;

public class PreviousBillActivity extends AppCompatActivity {

    private static final String TAG = "PREVIOUS BILL ACTIVITY";
    RecyclerView rvPreviousBill;
    private String PATH,IPADDRESS;
    private ProgressDialog progressDialog;
    ArrayList<Details> previousBillDetails;
    PreviousBillAdapter previousBillAdapter;
    String CurrentDate;
    private String Location;
    private String tillNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_bill);
        Init();
        LoadData();
    }

    private void LoadData() {
        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH =urlDB.getUrlDetails();
        IPADDRESS=urlDB.getIpUrlDetails();
        CurrentDate=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        urlDB.close();

        UserDB userDB = new UserDB(getApplicationContext());
        userDB.open();
        ArrayList<UserDetails> userDetailses = userDB.getUserDetails();
        userDB.close();


        Location = userDetailses.get(0).getStoreID();
        tillNo = userDetailses.get(0).getTillNo();
        LoadPreviousBillDetails();
    }

    private void Init() {
        rvPreviousBill= findViewById(R.id.rv_previousbill);

        previousBillDetails= new ArrayList<>();
        previousBillAdapter= new PreviousBillAdapter(previousBillDetails,this);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
        linearLayoutManager1.setOrientation(LinearLayoutManager.VERTICAL); // set Horizontal Orientation
        rvPreviousBill.setLayoutManager(linearLayoutManager1); // set LayoutManager to Recycler
        rvPreviousBill.setAdapter(previousBillAdapter);

    }
    private void LoadPreviousBillDetails() {
        ConnectionDetector net = new ConnectionDetector(getApplicationContext());
        if (net.isConnectingToInternet()) {
            progressDialog = ProgressDialog.show(PreviousBillActivity.this, "",
                    "Loading....", true);
            Log.i(TAG, "Loading Previous billDetails..");
            try {
                RequestQueue queue = AppController.getInstance().getRequestQueue();
                 final String url ="http://"+IPADDRESS+ "/mposbill/api/mposdata/billDeatil?bill_date="+CurrentDate+"&till_no="+tillNo+"&location_code="+Location;
               // final String url ="http://35.200.229.104/mposbill/api/mposdata/billDeatil?bill_date=2019-12-10&till_no=14&location_code=110";
                Log.d("Url Adress:", url);
                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // display response
                                try {
                                    String StatusCode = response.getString("statusCode");
                                    if (StatusCode.equalsIgnoreCase("200")) {
                                        PreviousBillApiPojo previousBillApiPojo= new PreviousBillApiPojo();
                                        Gson gson = new Gson();
                                        previousBillApiPojo= gson.fromJson(response.toString(), PreviousBillApiPojo.class);
                                        previousBillDetails= previousBillApiPojo.getDetail();
                                        LoadRecyclerview();
                                        progressDialog.dismiss();
                                    }else {
                                        progressDialog.dismiss();
                                        String Message = response.getString("Message");
                                        Log.i(TAG, Message);
                                        showLoyaltyFailedAlert(Message);
                                    }




                                    // finish();
                                    //Log.d("Response", response.toString());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                progressDialog.dismiss();

                                try {

                                    if (error instanceof TimeoutError) {
                                        showLoyaltyFailedAlert("Time out error occurred.Please click on OK and try again");
                                        Log.e(TAG, "Time out error occurred.");
                                        //Time out error

                                    } else if (error instanceof NoConnectionError) {
                                        showLoyaltyFailedAlert("Network error occurred.Please click on OK and try again");
                                        Log.e(TAG, "Network error occurred.");
                                        //net work error

                                    } else if (error instanceof AuthFailureError) {
                                        showLoyaltyFailedAlert("Authentication error occurred.Please click on OK and try again");
                                        Log.e(TAG, "Authentication error occurred.");
                                        //error

                                    } else if (error instanceof ServerError) {
                                        showLoyaltyFailedAlert("Server error occurred.Please click on OK and try again");
                                        Log.e(TAG, "Server error occurred.");
                                        //Error
                                    } else if (error instanceof NetworkError) {
                                        showLoyaltyFailedAlert("Network error occurred.Please click on OK and try again");
                                        Log.e(TAG, "Network error occurred.");
                                        //Error

                                    } else if (error instanceof ParseError) {
                                        //Error
                                        showLoyaltyFailedAlert("An error occurred.Please click on OK and try again");
                                        Log.e(TAG, "An error occurred.");
                                    } else {

                                        showLoyaltyFailedAlert("An error occurred.Please click on OK and try again");
                                        Log.e(TAG, "An error occurred.");
                                        //Error
                                    }
                                    //End


                                } catch (Exception e) {


                                }

                            }
                        }
                )


                {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                };


                queue.add(getRequest);

            } catch (Exception e) {
                // dialog.dismiss();
                showLoyaltyFailedAlert(e.getMessage());
                Log.e(TAG, e.getMessage());
                System.out.println("Exception : " + e.getMessage());
            }


        }

        else

        {

            AlertManager alert = new AlertManager();
            alert.alert(PreviousBillActivity.this, "No Internet Connection",
                    "Please check " +
                            "your data connection or Wifi is ON !");

        }
    }

    private void LoadRecyclerview() {
        previousBillAdapter.updateData(previousBillDetails);
        previousBillAdapter.notifyDataSetChanged();


    }

    private void showLoyaltyFailedAlert(final String msg) {
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(PreviousBillActivity.this, LoyaltyActivity.class);
        startActivity(intent);
    }
}
