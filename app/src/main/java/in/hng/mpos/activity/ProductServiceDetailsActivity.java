package in.hng.mpos.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.hng.mpos.Adapter.ProductListAdapter;
import in.hng.mpos.Adapter.ServiceListAdapter;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.R;
import in.hng.mpos.ResponseModel.Details;
import in.hng.mpos.ResponseModel.ProductServiceInsert;
import in.hng.mpos.ResponseModel.ProductServiceMaster;
import in.hng.mpos.gettersetter.ProductInfo;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.helper.AppController;
import in.hng.mpos.helper.ConnectionDetector;
import in.hng.mpos.helper.Log;


public class ProductServiceDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    TextView title, RvName;
    Button Btn, btnFinish;
    RecyclerView RvProduct, RvService;
    Boolean StepOne;
    ProductListAdapter productListAdapter;
    ServiceListAdapter serviceListAdapter;
    Dialog dialog;
    Runnable myRunnable;
    Handler handler;
    String Location, PATH;
    int ExpertLimit;
    ProductServiceMaster p;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    ArrayList<ProductInfo> ProductList, ServiceList;
    private String Bill_no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_service_details);
        Init();


    }

    private void LoadRecyclerViewData() {

        UserDB uDB = new UserDB(getApplicationContext());
        uDB.open();
        ArrayList<UserDetails> userDetails = uDB.getUserDetails();
        uDB.close();

        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH = urlDB.getUrlDetails();
        urlDB.close();

        Location = userDetails.get(0).getStoreID();
        FetchDetails();
        //  LoadDetails();


//        ServiceList.add(new ProductInfo("1","Skin Testing",false));
//        ServiceList.add(new ProductInfo("2","Mini Facial",false));
//        ServiceList.add(new ProductInfo("3","Make Over",false));
//        ServiceList.add(new ProductInfo("4","Nail Art",false));

    }

    private void Init() {
        ExpertLimit = 0;
        title = findViewById(R.id.tv_product_title);
        Btn = findViewById(R.id.btn_product);
        btnFinish = findViewById(R.id.btn_prodcut_finish);
        RvName = findViewById(R.id.tv_rv_name);
        RvProduct = findViewById(R.id.rv_product);
        RvService = findViewById(R.id.rv_service);
        RvService.setVisibility(View.GONE);
        btnFinish.setVisibility(View.GONE);


        ProductList = new ArrayList<>();
        ServiceList = new ArrayList<>();
        productListAdapter = new ProductListAdapter(ProductList, ExpertLimit, this);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL); // set Horizontal Orientation
        RvProduct.setLayoutManager(linearLayoutManager); // set LayoutManager to Recycler
        RvProduct.setAdapter(productListAdapter);

        serviceListAdapter = new ServiceListAdapter(ServiceList, this);


        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
        linearLayoutManager1.setOrientation(LinearLayoutManager.VERTICAL); // set Horizontal Orientation
        RvService.setLayoutManager(linearLayoutManager1); // set LayoutManager to Recycler
        RvService.setAdapter(serviceListAdapter);


        StepOne = false;
        Btn.setOnClickListener(this);
        btnFinish.setOnClickListener(this);
        LoadRecyclerViewData();

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_prodcut_finish) {

            ProductList = productListAdapter.getData();
            ServiceList = serviceListAdapter.getData();
            Boolean isSelected = false;
            for (int i = 0; i < ServiceList.size(); i++) {
                if (ServiceList.get(i).IsChecked) {
                    isSelected = true;
                    break;
                }
            }
            if (isSelected) {
                SendDatatoServer();
            } else {
                showPopupforService();
            }

        }

        if (view.getId() == R.id.btn_product) {
            ProductList = productListAdapter.getData();
            Boolean isSelected = false;
            for (int i = 0; i < ProductList.size(); i++) {
                if (ProductList.get(i).IsChecked) {
                    isSelected = true;
                    break;
                }
            }
            if (isSelected) {
                StepOne = true;
                title.setText("Select Service");
                Btn.setVisibility(View.GONE);
                btnFinish.setVisibility(View.VISIBLE);
                RvName.setText("Service Name");
                RvProduct.setVisibility(View.GONE);
                RvService.setVisibility(View.VISIBLE);
            } else {
                showPopupforProduct();
            }

        }


    }

    private void showPopupforProduct() {
        new AlertDialog.Builder(this)
                .setMessage("No Items Selected. Do you want to Continue?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        StepOne = true;
                        title.setText("Select Service");
                        Btn.setVisibility(View.GONE);
                        btnFinish.setVisibility(View.VISIBLE);
                        RvName.setText("Service Name");
                        RvProduct.setVisibility(View.GONE);
                        RvService.setVisibility(View.VISIBLE);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showPopupforService() {
        new AlertDialog.Builder(this)
                .setMessage("No Items Selected. Do you want to Continue?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SendDatatoServer();
                    }
                })
                .setNegativeButton("No", null)
                .show();

    }

    private void SendDatatoServer() {
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        Bill_no = sp.getString("Bill_no", "");


        ArrayList<Details> details1 = new ArrayList<>();

        for (int i = 0; i < ProductList.size(); i++) {
            if (ProductList.get(i).IsChecked) {
                Details details2 = new Details();
                details2.setBill_no(Bill_no);
                details2.setSpecialist_id(ProductList.get(i).code);
                details2.setType_code("1");
                details1.add(details2);
            }
        }
        for (int i = 0; i < ServiceList.size(); i++) {
            if (ServiceList.get(i).IsChecked) {

                Details details2 = new Details();
                details2.setBill_no(Bill_no);
                details2.setSpecialist_id(ServiceList.get(i).code);
                details2.setType_code("2");
                details1.add(details2);

            }
        }

        Details[] details = new Details[details1.size()];
        for (int i = 0; i < details1.size(); i++) {
            details[i] = details1.get(i);
        }

        ProductServiceInsert productServiceInsert = new ProductServiceInsert();
        productServiceInsert.setLocation_code(Location);
        productServiceInsert.setStstusCode("200");
        productServiceInsert.setStatus("Success");
        productServiceInsert.setDetails(details);

        SendSpecialistAndServiceData(productServiceInsert);

        Log.d("", "");
    }

    private void SendSpecialistAndServiceData(ProductServiceInsert productServiceInsert) {

        JSONObject payload = new JSONObject();

        try {
            payload = new JSONObject(new Gson().toJson(productServiceInsert));

        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (dialog != null) dialog.dismiss();
        try {
            handler.removeCallbacks(myRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog = ProgressDialog.show(this, "",
                "Please Wait...", true);
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                if (dialog != null) dialog.dismiss();
            }
        }, 30000);

        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
        String URL = PATH + "productserviceinsert/";
        ConnectionDetector net = new ConnectionDetector(getApplicationContext());
        if (net.isConnectingToInternet()) {
            JsonObjectRequest stringRequest = new JsonObjectRequest(URL, payload, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (dialog != null) dialog.dismiss();
                        try {
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        JSONObject val = response;

                        showFailedAlert("Data Saved Successfully");
                        NavigatetoNextPage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {


                    try {
                        if (dialog != null) dialog.dismiss();

                        try {
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (error instanceof TimeoutError) {
                            showFailedAlert("Time out error occurred.");
                            //Log.e(TAG, "Time out error occurred.");
                            //Time out error

                        } else if (error instanceof NoConnectionError) {
                            showFailedAlert("Network error occurred.");
                            //Log.e(TAG, "Network error occurred.");
                            //net work error

                        } else if (error instanceof AuthFailureError) {
                            showFailedAlert("Authentication error occurred.Please click on OK and try again");
                            //Log.e(TAG, "Authentication error occurred.Please click on OK and try again");
                            //error

                        } else if (error instanceof ServerError) {
                            showFailedAlert("Server error occurred.");
                            //Log.e(TAG, "Server error occurred.");
                            //Error
                        } else if (error instanceof NetworkError) {
                            showFailedAlert("Network error occurred.Please click on OK and try again");
                            //Log.e(TAG, "Network error occurred.");
                            //Error

                        } else if (error instanceof ParseError) {
                            //Error
                            showFailedAlert("An error occurred.Please click on OK and try again");
                            //Log.e(TAG, "An error occurred.");
                        } else {

                            showFailedAlert("An error occurred.Please click on OK and try again");
                            //Log.e(TAG, "An error occurred.");
                            //Error
                        }
                        //End


                    } catch (Exception e) {


                    }

                }
            }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("LocationCode", Location);


                    return params;
                }

            };
            requestQueue.add(stringRequest);
        } else {
            if (dialog != null) dialog.dismiss();

            try {
                handler.removeCallbacks(myRunnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
            showFailedAlert("No Internet Connection, \n " +
                    "Please check " +
                    "your data connection or Wifi is ON !");

        }

    }

    private void NavigatetoNextPage() {
        Intent intent = new Intent(this, WebViewEbill.class);
        startActivity(intent);
        finish();
    }


    public void FetchDetails() {

        if (dialog != null) dialog.dismiss();
        try {
            handler.removeCallbacks(myRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog = ProgressDialog.show(this, "",
                "Please Wait...", true);
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                if (dialog != null) dialog.dismiss();
            }
        }, 30000);

        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
        String URL = PATH + "productservicemaster/";
        ConnectionDetector net = new ConnectionDetector(getApplicationContext());
        if (net.isConnectingToInternet()) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // response
                    try {
                        if (dialog != null) dialog.dismiss();
                        try {
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            JSONObject Jsonobj = new JSONObject(response);
                            String status = Jsonobj.getString("statusCode");

                            if (status.equalsIgnoreCase("201")) {
                                NavigatetoNextPage();
                            }

                        } catch (Exception e) {

                        }


                        Gson g = new Gson();
                        p = g.fromJson(response, ProductServiceMaster.class);
                        JSONObject val = new JSONObject(response);
                        LoadDetails();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    // Log.d("Response", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {


                    try {
                        if (dialog != null) dialog.dismiss();

                        try {
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (error instanceof TimeoutError) {
                            showFailedAlert("Time out error occurred.");
                            //Log.e(TAG, "Time out error occurred.");
                            //Time out error

                        } else if (error instanceof NoConnectionError) {
                            showFailedAlert("Network error occurred.");
                            //Log.e(TAG, "Network error occurred.");
                            //net work error

                        } else if (error instanceof AuthFailureError) {
                            showFailedAlert("Authentication error occurred.Please click on OK and try again");
                            //Log.e(TAG, "Authentication error occurred.Please click on OK and try again");
                            //error

                        } else if (error instanceof ServerError) {
                            showFailedAlert("Server error occurred.");
                            //Log.e(TAG, "Server error occurred.");
                            //Error
                        } else if (error instanceof NetworkError) {
                            showFailedAlert("Network error occurred.Please click on OK and try again");
                            //Log.e(TAG, "Network error occurred.");
                            //Error

                        } else if (error instanceof ParseError) {
                            //Error
                            showFailedAlert("An error occurred.Please click on OK and try again");
                            //Log.e(TAG, "An error occurred.");
                        } else {

                            showFailedAlert("An error occurred.Please click on OK and try again");
                            //Log.e(TAG, "An error occurred.");
                            //Error
                        }
                        //End


                    } catch (Exception e) {


                    }

                }
            }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("LocationCode", Location);


                    return params;
                }

            };
            requestQueue.add(stringRequest);
        } else {
            if (dialog != null) dialog.dismiss();

            try {
                handler.removeCallbacks(myRunnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
            showFailedAlert("No Internet Connection, \n " +
                    "Please check " +
                    "your data connection or Wifi is ON !");

        }
    }

    private void LoadDetails() {

//        Gson g = new Gson();
//        p = g.fromJson("{\n" +
//                "\n" +
//                "    \"ststusCode\": \"200\",\n" +
//                "\n" +
//                "    \"status\": \"Success\",\n" +
//                "\n" +
//                "    \"experts_limit\": \"2\",\n" +
//                "\n" +
//                "    \"specialist\": [\n" +
//                "\n" +
//                "        {\n" +
//                "\n" +
//                "            \"sel\": \"0\",\n" +
//                "\n" +
//                "            \"code\": \"2005887\",\n" +
//                "\n" +
//                "            \"name\": \"Masethung R\"\n" +
//                "\n" +
//                "        },\n" +
//                "\n" +
//                "        {\n" +
//                "\n" +
//                "            \"sel\": \"0\",\n" +
//                "\n" +
//                "            \"code\": \"2005888\",\n" +
//                "\n" +
//                "            \"name\": \"test2\"\n" +
//                "\n" +
//                "        },\n" +
//                "\n" +
//                "        {\n" +
//                "\n" +
//                "            \"sel\": \"0\",\n" +
//                "\n" +
//                "            \"code\": \"2005889\",\n" +
//                "\n" +
//                "            \"name\": \"test3\"\n" +
//                "\n" +
//                "        }\n" +
//                "\n" +
//                "    ],\n" +
//                "\n" +
//                "    \"service\": [\n" +
//                "\n" +
//                "        {\n" +
//                "\n" +
//                "            \"sel\": \"0\",\n" +
//                "\n" +
//                "            \"code\": \"1\",\n" +
//                "\n" +
//                "            \"name\": \"Skin Testing\"\n" +
//                "\n" +
//                "        },\n" +
//                "\n" +
//                "        {\n" +
//                "\n" +
//                "            \"sel\": \"0\",\n" +
//                "\n" +
//                "            \"code\": \"2\",\n" +
//                "\n" +
//                "            \"name\": \"Mini Facial\"\n" +
//                "\n" +
//                "        },\n" +
//                "\n" +
//                "        {\n" +
//                "\n" +
//                "            \"sel\": \"0\",\n" +
//                "\n" +
//                "            \"code\": \"3\",\n" +
//                "\n" +
//                "            \"name\": \"Make-over\"\n" +
//                "\n" +
//                "        },\n" +
//                "\n" +
//                "        {\n" +
//                "\n" +
//                "            \"sel\": \"0\",\n" +
//                "\n" +
//                "            \"code\": \"4\",\n" +
//                "\n" +
//                "            \"name\": \"Nail art\"\n" +
//                "\n" +
//                "        }\n" +
//                "\n" +
//                "    ]\n" +
//                "\n" +
//                "}", ProductServiceMaster.class);
//    //    JSONObject val = new JSONObject(response);

        ExpertLimit = Integer.parseInt(p.getExperts_limit());
        productListAdapter.setLimit(ExpertLimit);
        productListAdapter.notifyDataSetChanged();
        for (int i = 0; i < p.getSpecialist().length; i++) {
            ProductList.add(new ProductInfo(p.getSpecialist()[i].getCode(), p.getSpecialist()[i].getName(), false));
            productListAdapter.notifyDataSetChanged();
        }

//        ProductList.add(new ProductInfo("2","Rahul",false));
//        ProductList.add(new ProductInfo("3","Shekhar",false));
//        ProductList.add(new ProductInfo("4","adi",false));


        for (int j = 0; j < p.getSpecialist().length; j++) {
            ServiceList.add(new ProductInfo(p.getService()[j].getCode(), p.getService()[j].getName(), false));
            serviceListAdapter.notifyDataSetChanged();
        }


    }


    @Override
    public void onBackPressed() {
        ProductServiceDetailsActivity.this.runOnUiThread(new Runnable() {
            private static final String TAG = "ProductServiceDetailsActivity";

            public void run() {
                Log.e(TAG, "You cannot go back at this stage");
                AlertDialog.Builder builder = new AlertDialog.Builder(ProductServiceDetailsActivity.this);
                builder.setTitle("HnG POS");
                builder.setMessage("You cannot go back at this stage")
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

    private void showFailedAlert(final String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();


    }
}
