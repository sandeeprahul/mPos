package in.hng.mpos.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.hng.mpos.Database.UserDB;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.helper.AppController;
import in.hng.mpos.helper.Log;

public class ActivityZaggleSettings extends AppCompatActivity {

    String TAG = "Zaggle settings Activity";

    ProgressDialog idialog = null;
    private Handler handler;
    Runnable myRunnable = null;
    String PATH, Location, zagPath, ZagKey = "";
    EditText txtCardNo, txtMobile, txtAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zaggle_settings);

        final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.action_bar_simple,null);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionBarLayout);
            actionBar.setTitle("");
            final int actionBarColor = getResources().getColor(R.color.ActionBarColor);
            actionBar.setBackgroundDrawable(new ColorDrawable(actionBarColor));
        }

        TextView title = findViewById(R.id.ab_activity_title);
        title.setText(R.string.Zaggle_heading);
        title.setVisibility(View.VISIBLE);

        txtCardNo = findViewById(R.id.txtZagCardNo);
        txtMobile = findViewById(R.id.txtZagMobNo);
        txtAmount = findViewById(R.id.txtZagAmt);

        Resources res = getResources();
        PATH = res.getString(R.string.API_URL);
        zagPath = res.getString(R.string.ZagURL);

        UserDB userDB = new UserDB(getApplicationContext());
        userDB.open();
        ArrayList<UserDetails> userDetails = userDB.getUserDetails();
        userDB.close();

        if (userDetails.size() > 0) {

            Location = userDetails.get(0).getStoreID();

        }


        makeGetWalletMasterAPIcall();


    }

    //wallet details
    public void makeGetWalletMasterAPIcall() {

        Log.i(TAG, "Fetching wallet details API");

        idialog = ProgressDialog.show(ActivityZaggleSettings.this, "", "loading Zaggle details...", true);
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                idialog.dismiss();
            }
        }, 30000);
        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
        String URL = PATH + "walletdetails?Location=" + Location;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {

            @Override
            public void onResponse(final String response) {

                try {
                    Log.i(TAG, "Wallet details response: " + response);
                    JSONObject Jsonobj = new JSONObject(response);
                    String status = Jsonobj.getString("statusCode");

                    if (status.equalsIgnoreCase("200")) {

                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        JSONArray ZagData = Jsonobj.getJSONArray("Zaggle");
                        JSONObject json_zag = ZagData.getJSONObject(0);

                        ZagKey = json_zag.optString("KEY");


                    } else {

                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ActivityZaggleSettings.this.runOnUiThread(new Runnable() {
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityZaggleSettings.this);
                                builder.setTitle("h&g mPOS");
                                builder.setMessage("Wallet details not available in database. Please contact IT for support")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {

                                                View view = ActivityZaggleSettings.this.getCurrentFocus();
                                                if (view != null) {
                                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                    if (imm != null) {
                                                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                                    }
                                                }

                                                Intent i = new Intent(ActivityZaggleSettings.this, LoyaltyActivity.class);
                                                startActivity(i);
                                                finish();

                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        });
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
                    try {
                        idialog.dismiss();
                        handler.removeCallbacks(myRunnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (error instanceof TimeoutError) {
                        showFailedAlert("Time out error occurred.");
                        Log.e(TAG, "Time out error occurred.");
                        //Time out error

                    } else if (error instanceof NoConnectionError) {
                        showFailedAlert("Network error occurred.");
                        Log.e(TAG, "Network error occurred.");
                        //net work error

                    } else if (error instanceof AuthFailureError) {
                        showFailedAlert("Authentication error occurred.");
                        Log.e(TAG, "Authentication error occurred.");
                        //error

                    } else if (error instanceof ServerError) {
                        showFailedAlert("Server error occurred.");
                        Log.e(TAG, "Server error occurred.");
                        //Error
                    } else if (error instanceof NetworkError) {
                        showFailedAlert("Network error occurred.");
                        Log.e(TAG, "Network error occurred.");
                        //Error

                    } else if (error instanceof ParseError) {
                        //Error
                        showFailedAlert("An error occurred.");
                        Log.e(TAG, "An error occurred.");
                    } else {

                        showFailedAlert("An error occurred.");
                        Log.e(TAG, "An error occurred.");
                        //Error
                    }

                } catch (Exception e) {

                    Log.e(TAG, "An error occurred.");
                }
            }
        }) {

        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(stringRequest);
    }


    public void CheckZaggleBalance(View v) {

        Log.i(TAG, "Getting Zag Brand OTP");

        if (txtCardNo.getText().length() >= 16) {
            txtMobile.setText("");
            txtAmount.setText("");
            idialog = ProgressDialog.show(ActivityZaggleSettings.this, "", "Authenticating...", true);
            handler = new Handler();
            handler.postDelayed(myRunnable = new Runnable() {
                @Override
                public void run() {

                    idialog.dismiss();
                }
            }, 30000);

            RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
            String URL = zagPath + "brandotppinsettings";
            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {

                @Override
                public void onResponse(final String response) {

                    try {

                        JSONObject Jsonobj = new JSONObject(response);
                        Log.i(TAG, "Zag brand OTP response: " + response);
                        String status = Jsonobj.getString("status");

                        if (status.equalsIgnoreCase("1")) {
                            try {
                                idialog.dismiss();
                                handler.removeCallbacks(myRunnable);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            makeZaggleCheckBalAPIcall();
                        } else {
                            try {
                                idialog.dismiss();
                                handler.removeCallbacks(myRunnable);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String Msg = Jsonobj.getString("message");
                            Log.e(TAG, Msg);
                            showFailedAlert(Msg);
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
                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (error instanceof TimeoutError) {
                            showFailedAlert("Time out error occurred.");
                            Log.e(TAG, "Time out error occurred.");
                            //Time out error

                        } else if (error instanceof NoConnectionError) {
                            showFailedAlert("Network error occurred.");
                            Log.e(TAG, "Network error occurred.");
                            //net work error

                        } else if (error instanceof AuthFailureError) {
                            showFailedAlert("Authentication error occurred.");
                            Log.e(TAG, "Authentication error occurred.");
                            //error

                        } else if (error instanceof ServerError) {
                            showFailedAlert("Server error occurred.");
                            Log.e(TAG, "Server error occurred.");
                            //Error
                        } else if (error instanceof NetworkError) {
                            showFailedAlert("Network error occurred.");
                            Log.e(TAG, "Network error occurred.");
                            //Error

                        } else if (error instanceof ParseError) {
                            //Error
                            showFailedAlert("An error occurred.");
                            Log.e(TAG, "An error occurred.");
                        } else {

                            showFailedAlert("An error occurred.");
                            Log.e(TAG, "An error occurred.");
                            //Error
                        }


                    } catch (Exception e) {

                        Log.e(TAG, "An error occurred.");
                    }
                }
            }) {

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    params.put("APPKEY", ZagKey);
                    return params;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    6000,
                    3,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(stringRequest);
        } else {
            if (txtCardNo.getText().length() < 16)
                showFailedAlert("Enter valid card number");
        }
    }

    public void makeZaggleCheckBalAPIcall() {

        Log.i(TAG, "Checking Zag Balance");
        idialog = ProgressDialog.show(ActivityZaggleSettings.this, "", "Checking Balance...", true);
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                idialog.dismiss();
            }
        }, 30000);

        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
        String URL = zagPath + "fetchcarddetailsbycardnumber?cardnumber=" + txtCardNo.getText().toString();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {

            @Override
            public void onResponse(final String response) {

                try {
                    Log.i(TAG, "Zag Bal API response: " + response);
                    JSONObject Jsonobj = new JSONObject(response);
                    String status = Jsonobj.getString("status");

                    if (status.equalsIgnoreCase("1")) {
                        String Bal = Jsonobj.getString("cardbalance");
                        String Text = "Zaggle balance for the card number " + txtCardNo.getText().toString() + "\n is Rs " + Bal + "/-";
                        showFailedAlert(Text);

                        //txtZagBal.setText(Bal);


                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        try {
                            idialog.dismiss();
                            handler.removeCallbacks(myRunnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String Msg = Jsonobj.getString("message");
                        showFailedAlert(Msg);
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
                    try {
                        idialog.dismiss();
                        handler.removeCallbacks(myRunnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (error instanceof TimeoutError) {
                        showFailedAlert("Time out error occurred.");
                        Log.e(TAG, "Time out error occurred.");
                        //Time out error

                    } else if (error instanceof NoConnectionError) {
                        showFailedAlert("Network error occurred.");
                        Log.e(TAG, "Network error occurred.");
                        //net work error

                    } else if (error instanceof AuthFailureError) {
                        showFailedAlert("Authentication error occurred.");
                        Log.e(TAG, "Authentication error occurred.");
                        //error

                    } else if (error instanceof ServerError) {
                        showFailedAlert("Server error occurred.");
                        Log.e(TAG, "Server error occurred.");
                        //Error
                    } else if (error instanceof NetworkError) {
                        showFailedAlert("Network error occurred.");
                        Log.e(TAG, "Network error occurred.");
                        //Error

                    } else if (error instanceof ParseError) {
                        //Error
                        showFailedAlert("An error occurred.");
                        Log.e(TAG, "An error occurred.");
                    } else {

                        showFailedAlert("An error occurred.");
                        Log.e(TAG, "An error occurred.");
                        //Error
                    }


                } catch (Exception e) {

                    Log.e(TAG, "An error occurred.");
                }
            }
        }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("APPKEY", ZagKey);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(stringRequest);


    }

    public void ActivateZaggleCard(View v) {
        Log.i(TAG, "Activating Zaggle Gift Card");


        if (!txtCardNo.getText().toString().isEmpty() && !txtMobile.getText().toString().isEmpty() && !txtAmount.getText().toString().isEmpty()) {
            if (txtCardNo.getText().toString().length() >= 16 && txtMobile.getText().toString().length() >= 10) {

                idialog = ProgressDialog.show(ActivityZaggleSettings.this, "", "Activating Zaggle Gift Card..", true);
                handler = new Handler();
                handler.postDelayed(myRunnable = new Runnable() {
                    @Override
                    public void run() {

                        idialog.dismiss();
                    }
                }, 30000);


                RequestQueue requestQueue = AppController.getInstance().getRequestQueue();
                String URL = zagPath + "activatecard?cardnumber=" + txtCardNo.getText().toString() + "&mobilenumber=" + txtMobile.getText().toString() + "&amount=" + txtAmount.getText().toString();
                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {

                    @Override
                    public void onResponse(final String response) {

                        try {
                            Log.i(TAG, "Zag redeem response: " + response);
                            JSONObject Jsonobj = new JSONObject(response);
                            String status = Jsonobj.getString("status");

                            if (status.equalsIgnoreCase("1")) {

                                idialog.dismiss();
                                handler.removeCallbacks(myRunnable);
                                String Msg = Jsonobj.getString("message");
                                String Bal = Jsonobj.getString("cardbalance");
                                showFailedAlert(Msg + "\n and the available balance is RS " + Bal + "\\-");

                            } else {
                                try {
                                    idialog.dismiss();
                                    handler.removeCallbacks(myRunnable);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                String Msg = Jsonobj.getString("message");
                                showFailedAlert(Msg);

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
                            try {
                                idialog.dismiss();
                                handler.removeCallbacks(myRunnable);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (error instanceof TimeoutError) {
                                showFailedAlert("Time out error occurred.");
                                Log.e(TAG, "Time out error occurred.");
                                //Time out error

                            } else if (error instanceof NoConnectionError) {
                                showFailedAlert("Network error occurred.");
                                Log.e(TAG, "Network error occurred.");
                                //net work error

                            } else if (error instanceof AuthFailureError) {
                                showFailedAlert("Authentication error occurred.");
                                Log.e(TAG, "Authentication error occurred.");
                                //error

                            } else if (error instanceof ServerError) {
                                showFailedAlert("Server error occurred.");
                                Log.e(TAG, "Server error occurred.");
                                //Error
                            } else if (error instanceof NetworkError) {
                                showFailedAlert("Network error occurred.");
                                Log.e(TAG, "Network error occurred.");
                                //Error

                            } else if (error instanceof ParseError) {
                                //Error
                                showFailedAlert("An error occurred.");
                                Log.e(TAG, "An error occurred.");
                            } else {

                                showFailedAlert("An error occurred.");
                                Log.e(TAG, "An error occurred.");
                                //Error
                            }
                        } catch (Exception e) {

                            Log.e(TAG, "An error occurred.");
                        }
                    }
                }) {

                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", "application/x-www-form-urlencoded");
                        params.put("APPKEY", ZagKey);
                        return params;
                    }
                };
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        6000,
                        3,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                requestQueue.add(stringRequest);
            } else {
                showFailedAlert("Please enter valid Card / Mobile Number");
            }
        } else {
            showFailedAlert("All fields are mandatory");
        }


    }

    public void ClearData(View v) {
        txtCardNo.setText("");
        txtMobile.setText("");
        txtAmount.setText("");
        txtCardNo.requestFocus();
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            new AlertDialog.Builder(ActivityZaggleSettings.this)
                    .setMessage("Are you sure you want to Exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            Intent i = new Intent(ActivityZaggleSettings.this, LoyaltyActivity.class);
                            startActivity(i);
                            finish();

                        }

                    }).setNegativeButton("No", null)
                    .show();
        }
        return super.onKeyDown(keyCode, event);
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


    public void showFailedAlert(final String msg) {

        ActivityZaggleSettings.this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityZaggleSettings.this);
                builder.setTitle("h&g mPOS");
                builder.setMessage(msg)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {


                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

    }
}
