package in.hng.mpos.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import in.hng.mpos.MainActivityTemp;
import in.hng.mpos.helper.BluetoothUtil;
import in.hng.mpos.helper.SunmiPrintHelper;
import sunmi.sunmiui.dialog.DialogCreater;
import sunmi.sunmiui.dialog.ListDialog;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.authentication.utils.ToastUtil;
import com.cie.btp.CieBluetoothPrinter;
import com.google.gson.Gson;
import com.ngx.BluetoothPrinter;
import com.ngx.DebugLog;
import com.ngx.PrinterWidth;
import com.pranavpandey.android.dynamic.util.DynamicUnitUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import android_serialport_api.PrinterAPI;
import android_serialport_api.SerialPortManager;
import in.hng.mpos.Adapter.PrintAdapter;
import in.hng.mpos.Database.CustomerDB;
import in.hng.mpos.Database.LoyaltyCredentialsDB;
import in.hng.mpos.Database.LoyaltyDetailsDB;
import in.hng.mpos.Database.PrinterDB;
import in.hng.mpos.Database.ProductDetailsDB;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.Database.WalletDB;
import in.hng.mpos.R;
import in.hng.mpos.Utils.Constants;
import in.hng.mpos.gettersetter.BillPrintDetailsPojo.BillPrintPojo;
import in.hng.mpos.gettersetter.CustomerDetails;
import in.hng.mpos.gettersetter.PrinterDetails;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.helper.AlertManager;
import in.hng.mpos.helper.ApiCall;
import in.hng.mpos.helper.ConnectionDetector;
import in.hng.mpos.helper.Log;

import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_DEVICE_NAME;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_CONNECTED;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_CONNECTING;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_LISTEN;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_NONE;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_MESSAGES;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_MSG;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOTIFICATION_ERROR_MSG;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOTIFICATION_MSG;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOT_CONNECTED;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOT_FOUND;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_SAVED;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_STATUS;


import static in.hng.mpos.activity.PrinterActivity.ISCONNECT;

import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.TaskCallback;
import net.posprinter.service.PosprinterService;
import net.posprinter.utils.BitmapProcess;
import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterPos80;
import net.posprinter.utils.DataForSendToPrinterTSC;

/**
 * Created by Cbly on 22-Mar-18.
 */

public class WebViewEbill extends AppCompatActivity implements PrinterAPI.printerStatusListener {

    public String Ebill_URL;
    public static String bill_no;
    private WebView webView;
    private SwipeRefreshLayout swipe;
    LinearLayout btns_ll;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Button e_bill, print_bill, new_bill, btn_more;
    private ProgressDialog dialog = null;
    ConnectionDetector net;
    private static final String TAG = "E-bill Window";
    private String PATH, Location;
    private BluetoothPrinter mBtp = PrinterActivity.mBtp;
    public CieBluetoothPrinter mPrinter = PrinterActivity.mPrinter;
    private TextView tvStatus;
    private String mConnectedDeviceName = "";
    public static final String title_connecting = "connecting...";
    public static final String title_connected_to = "connected: ";
    public static final String title_not_connected = "not connected";
    private Handler handler = new Handler();
    Runnable myRunnable;
    private boolean isNGX = false, isExel = false, isRugtek = false;
    private PrinterAPI api;

    private LinearLayout printlayout, PrintViewRugtek;
    String fromActivity = "", userMobileNumber = "";

  /*  @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothPrinter.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothPrinter.STATE_CONNECTED:
                            tvStatus.setText(title_connected_to);
                            tvStatus.append(mConnectedDeviceName);
                            tvStatus.setTextColor(Color.GREEN);
                            break;
                        case BluetoothPrinter.STATE_CONNECTING:
                            tvStatus.setText(title_connecting);
                            break;
                        case BluetoothPrinter.STATE_LISTEN:
                        case BluetoothPrinter.STATE_NONE:
                            tvStatus.setText(title_not_connected);
                            tvStatus.setTextColor(Color.RED);
                            break;
                    }
                    break;
                case BluetoothPrinter.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(
                            BluetoothPrinter.DEVICE_NAME);
                    break;
                case BluetoothPrinter.MESSAGE_STATUS:
                    tvStatus.setText(msg.getData().getString(
                            BluetoothPrinter.STATUS_TEXT));
                    break;
                default:
                    break;
            }
        }
    };*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.action_webviewtemp);

        //Resources res = getResources();
        //PATH = res.getString(R.string.API_URL);
        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH = urlDB.getUrlDetails();
        urlDB.close();
        net = new ConnectionDetector(getApplicationContext());
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        api = new PrinterAPI();
        // Set up your ActionBar
        final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.action_bar_simple, null);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarLayout);
        actionBar.setTitle("");
        final int actionBarColor = getResources().getColor(R.color.ActionBarColor);
        actionBar.setBackgroundDrawable(new ColorDrawable(actionBarColor));
        TextView title = findViewById(R.id.ab_activity_title);
        title.setText("h&g mPOS - E-Bill");
        title.setVisibility(View.VISIBLE);

        Log.i(TAG, "showing ebill..");

        btn_more = findViewById(R.id.btn_more);
        e_bill = findViewById(R.id.sndbill);
        print_bill = findViewById(R.id.printbill);
        new_bill = findViewById(R.id.newbill);
        swipe = findViewById(R.id.swipe);
        swipe.setVisibility(View.GONE);
        swipe.setOnRefreshListener(() -> WebAction());
//        webView.setVisibility(View.GONE);
        tvStatus = findViewById(R.id.tvStatus);

        printlayout = findViewById(R.id.ll_print_layout);
        PrintViewRugtek = findViewById(R.id.ll_printview_rugtek);
//        printlayout.setVisibility(View.GONE);
        swipe.setVisibility(View.VISIBLE);
        Ebill_URL = sp.getString("Ebill_URL", "");
        bill_no = sp.getString("Bill_no", "");

        Log.w(TAG, "Ebill_URL " + Ebill_URL);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Intent intent = getIntent();
            if (intent.hasExtra(Constants.FROM))
                fromActivity = bundle.getString(Constants.FROM);

            Log.w(TAG, "From Activity######## " + fromActivity);
            btn_more.setVisibility(View.VISIBLE);
           /* if (fromActivity != null && fromActivity.equalsIgnoreCase(Constants.MANUAL_BILL))
                btn_more.setVisibility(View.VISIBLE);
            else
                btn_more.setVisibility(View.GONE);*/
        }

        UserDB userDB = new UserDB(getApplicationContext());
        userDB.open();
        ArrayList<UserDetails> userDetailses = userDB.getUserDetails();
        userDB.close();

        Location = userDetailses.get(0).getStoreID();
        CustomerDB custmrdb = new CustomerDB(getApplicationContext());
        custmrdb.open();
        ArrayList<CustomerDetails> customerdata = custmrdb.getCustomerDetails();
        custmrdb.close();

        Log.w(TAG, "Customer Data Size " + customerdata.size());

        if (customerdata.size() > 0) {
            userMobileNumber = customerdata.get(0).getMobileNO();
            Log.w(TAG, "Mobile Number########## " + userMobileNumber);
        }


        if (net.isConnectingToInternet()) {
            WebAction();
        } else {
            AlertManager alert = new AlertManager();
            alert.alert(WebViewEbill.this, "No Internet Connection",
                    "Please check " +
                            "your data connection or Wifi is ON !");
        }


        PrinterDB printerDB = new PrinterDB(getApplicationContext());
        printerDB.open();
        ArrayList<PrinterDetails> printerDetails = printerDB.getPrinterDetails();
        printerDB.close();
        if (printerDetails.size() > 0) {

            if (printerDetails.get(0).getPrinterName().equalsIgnoreCase("NGX")) {
                try {

//                    mBtp.initService(this, mHandler);"
                   PrinterActivity. myBinder.ConnectBtPort(printerDetails.get(0).getPrinterID(), new TaskCallback() {
                        @Override
                        public void OnSucceed() {
                            ISCONNECT=true;
                            Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void OnFailed() {
                            ISCONNECT=false;
                            Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
                        }
                    } );
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                mBtp.setPreferredPrinter(printerDetails.get(0).getPrinterID());
//                mBtp.setPrinterWidth(PrinterWidth.PRINT_WIDTH_72MM);
                isNGX = true;
            } else {

                if (printerDetails.get(0).getPrinterName().equalsIgnoreCase("Rugtek")) {
                    isRugtek = true;
                    tvStatus.setText("Connected");
                    printlayout.setVisibility(View.VISIBLE);
                    swipe.setVisibility(View.GONE);
                    LoadPrintData();

                } else {
                    try {
                        mPrinter.initService(this);
                        //mPrinter.connectToPrinter();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    isExel = true;
                }
            }
        } else {
            print_bill.setVisibility(View.GONE);
        }

        print_bill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRugtek) {
                    Log.e("isRugtek", "isRugtek:true");
//                    PrintBillRugtek();//original
                    if (!BluetoothUtil.isBlueToothPrinter) {
                        printTemp(WebViewEbill.this);//testing
                    }
                } else {
                    Log.e("isRugtek", "isRugtek:False");
                   /* Intent intent = new Intent(WebViewEbill.this, TscActivity.class);
                    startActivity(intent);*/

                    printBitmap();
                   /* if (net.isConnectingToInternet()) {

                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                        //if (tvStatus.getText().toString().equalsIgnoreCase("Not connected to any bluetooth printer")) {
                        if (mBluetoothAdapter.isEnabled()) {
                            String Url = PATH + "ebillprinter?bill_no=" + bill_no;
                            Log.i(TAG, "Api call:" + Url);
                            makePrintBillAPIcall(Url);
                        } else {
                            showFailedAlert("Please enable blutooth");
                        }

                        //new DownloadFileFromURL(getApplicationContext()).execute();


                    } else {
                        AlertManager alert = new AlertManager();
                        alert.alert(WebViewEbill.this, "No Internet Connection",
                                "Please check " +
                                        "your data connection or Wifi is ON !");
                    }
*/
                }

            }
        });


        new_bill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(WebViewEbill.this)
                        .setMessage("Are you sure to continue with new bill?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog = ProgressDialog.show(WebViewEbill.this, "",
                                        "Saving Details...", false);

                                Log.i(TAG, "new bill clicked");
                                final DialogInterface finalDialog = dialog;
                                deletetables();
                                finalDialog.dismiss();
                                editor.clear();
                                editor.apply();
                                Intent intent = new Intent(WebViewEbill.this, LoyaltyActivity.class);
                                startActivity(intent);
                                finish();

                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });


        btn_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                deletetables();
                editor.clear();
                editor.apply();

                Intent i = new Intent(WebViewEbill.this, CustomerRelatedActivity.class);
                i.putExtra(Constants.MOBILE_NUMBER, userMobileNumber);
                startActivity(i);
                finish();
            }
        });

    }


    public void printTemp(Context context) {


//        SunmiPrintHelper.getInstance().printBitmap(getBitmapFromView(PrintViewRugtek), 0);
        SunmiPrintHelper.getInstance().printBitmap(createBitmapFromView(PrintViewRugtek, 0, 0), 0);
        SunmiPrintHelper.getInstance().feedPaper();


    }

    private Bitmap createBitmapFromView(Context context, View view, int width, int height) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        view.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
//        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));


        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        Drawable background = view.getBackground();

        if (background != null)
            //has background drawable, then draw it on the canvas
            background.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);

        view.draw(canvas);

        return bitmap;
    }

    public @NonNull
    static Bitmap createBitmapFromView(@NonNull View view, int width, int height) {
        if (width > 0 && height > 0) {
            view.measure(View.MeasureSpec.makeMeasureSpec(DynamicUnitUtils
                            .convertDpToPixels(width), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(DynamicUnitUtils
                            .convertDpToPixels(height), View.MeasureSpec.EXACTLY));
        }
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable background = view.getBackground();

        if (background != null)
            //has background drawable, then draw it on the canvas
            background.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);

        view.draw(canvas);


        return bitmap;
    }

    public static Bitmap getBitmapFromView(View view, int width, int height) {
        if (width > 0 && height > 0) {
            view.measure(View.MeasureSpec.makeMeasureSpec(DynamicUnitUtils
                            .convertDpToPixels(width), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(DynamicUnitUtils
                            .convertDpToPixels(height), View.MeasureSpec.EXACTLY));
        }
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    private void printBitmap() {

        final Bitmap bitmap1 =  createBitmapFromView(PrintViewRugtek,200,200);
//        final Bitmap bitmap1 =  createBitmapFromView(btns_ll,0,0);
//        final Bitmap bitmap1 =  BitmapProcess.compressBmpByYourWidth
//                (BitmapFactory.decodeResource(getResources(), R.drawable.eye_yes),150);
        PrinterActivity.myBinder.WriteSendData(new TaskCallback() {
            @Override
            public void OnSucceed() {
                Toast.makeText(getApplicationContext(),getString(R.string.send_success),Toast.LENGTH_SHORT).show();

            }

            @Override
            public void OnFailed() {
                Toast.makeText(getApplicationContext(),getString(R.string.send_failed),Toast.LENGTH_SHORT).show();

            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {

                List<byte[]> list = new ArrayList<>();
                // Label size
                list.add(DataForSendToPrinterTSC.sizeBymm(50,30));
                // Gap
                list.add(DataForSendToPrinterTSC.gapBymm(2,0));
                // clear buffer
                list.add(DataForSendToPrinterTSC.cls());
                list.add(DataForSendToPrinterTSC.bitmap(10,10,0,bitmap1, BitmapToByteData.BmpType.Threshold));
                list.add(DataForSendToPrinterTSC.print(1));

                return list;
            }
        });
    }


    private void LoadPrintData() {
        dialog = ProgressDialog.show(WebViewEbill.this, "",
                "Loading bill Details...", true);

        Log.i(TAG, "Loading bill Details..");
        if (net.isConnectingToInternet()) {

            handler = new Handler();

            handler.postDelayed(myRunnable = new Runnable() {
                @Override
                public void run() {

                    dialog.dismiss();
                }
            }, 30000);

            final String urlPath = PATH + "/ebillprinter_detail?bill_no=" + bill_no;
            ApiCall.make(this, urlPath, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // display response
                            try {
                                JSONObject responseObject = new JSONObject(response);
                                String Status = responseObject.getString("statusCode");
                                String data = responseObject.getString("data");
                                if (Status.equalsIgnoreCase("200")) {
                                    dialog.dismiss();
                                    Gson g = new Gson();
                                    BillPrintPojo p = g.fromJson(data, BillPrintPojo.class);
                                    LoadPrintRecyclerView(p);
                                    handler.removeCallbacks(myRunnable);


                                } else {
                                    dialog.dismiss();

                                }
                            } catch (JSONException e) {
                                dialog.dismiss();


                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            try {
                                dialog.dismiss();

                                handler.removeCallbacks(myRunnable);
                                if (error instanceof TimeoutError) {
                                    showFailedAlert("Time out error occurred.Please click on OK and try again");
                                    Log.i(TAG, "Time out error occurred.");

                                    //Time out error

                                } else if (error instanceof NoConnectionError) {
                                    showFailedAlert("Network error occurred.Please click on OK and try again");
                                    Log.i(TAG, "Network error occurred.");
                                    //net work error

                                } else if (error instanceof AuthFailureError) {
                                    showFailedAlert("Authentication error occurred.Please click on OK and try again");
                                    Log.i(TAG, "Authentication error occurred.");
                                    //error

                                } else if (error instanceof ServerError) {
                                    showFailedAlert("Server error occurred.Please click on OK and try again");
                                    Log.i(TAG, "Server error occurred.");
                                    //Error
                                } else if (error instanceof NetworkError) {
                                    showFailedAlert("Network error occurred.Please click on OK and try again");
                                    Log.i(TAG, "Network error occurred.");
                                    //Error

                                } else if (error instanceof ParseError) {
                                    //Error
                                    showFailedAlert("An error occured.Please click on OK and try again");
                                    Log.i(TAG, "An error occurred.");
                                } else {

                                    showFailedAlert("An error occured.Please click on OK and try again");
                                    Log.i(TAG, "An error occurred.");
                                    //Error
                                }
                                //End


                            } catch (Exception e) {

                                dialog.dismiss();
                                e.printStackTrace();
                            }

                        }
                    }
            );

        } else {
            dialog.dismiss();
            AlertManager alert = new AlertManager();
            alert.alert(WebViewEbill.this, "No Internet Connection",
                    "Please check " +
                            "your data connection or Wifi is ON !");
        }


    }

    private void LoadPrintRecyclerView(BillPrintPojo bill_detail) {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_print_value);

        PrintAdapter mAdapter = new PrintAdapter(bill_detail.getBill_detail(), this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        LoadPrintInfor(bill_detail);

    }

    private void LoadPrintInfor(BillPrintPojo bill_detail) {
        TextView Adress1, Adress2, Adress3, Adress4, Adress5;
        TextView CashTendered, CardTendered, CouponTendered, LoyaltyTendered, WalletTendered, BalanceAmount;
        TextView GST18, GST18Amount, CGST9, SGST9, CGSTAmount, SGSTAmount;
        TextView BillNo, TillNo, BillDate, BillTime;
        TextView CustomerName, MobileNo;
        TextView CasherID, GSTIN;
        TextView FooterText;
        TextView Qty, TotalAmount;
        LinearLayout LLCash, LLCard, LLCoupon, LLLoyalty, LLWallet;
        Adress1 = findViewById(R.id.tv_bill_address1);
        Adress1.setText(bill_detail.getBillheaders().getAddress1());
        Adress2 = findViewById(R.id.tv_bill_address2);
        Adress2.setText(bill_detail.getBillheaders().getAddress2());
        Adress3 = findViewById(R.id.tv_bill_address3);
        Adress3.setText(bill_detail.getBillheaders().getAddress3());

        Adress4 = findViewById(R.id.tv_bill_address4);
        Adress4.setText(bill_detail.getBillheaders().getAddress4());

        Adress5 = findViewById(R.id.tv_bill_address5);
        Adress5.setText(bill_detail.getBillheaders().getCity() + "," + bill_detail.getBillheaders().getPincode());

        LLCash = findViewById(R.id.ll_cash);
        LLCash.setVisibility(View.GONE);
        LLCard = findViewById(R.id.ll_card);
        LLCard.setVisibility(View.GONE);
        LLCoupon = findViewById(R.id.ll_coupon);
        LLCoupon.setVisibility(View.GONE);
        LLLoyalty = findViewById(R.id.ll_loyalty);
        LLLoyalty.setVisibility(View.GONE);
        LLWallet = findViewById(R.id.ll_wallet);
        LLWallet.setVisibility(View.GONE);

        CashTendered = findViewById(R.id.tv_cash_tendered);
        CardTendered = findViewById(R.id.tv_card_tendered);
        WalletTendered = findViewById(R.id.tv_wallet_tendered);
        CouponTendered = findViewById(R.id.tv_coupon_tendered);
        LoyaltyTendered = findViewById(R.id.tv_loyalty_tendered);


        if (Float.parseFloat(bill_detail.getBill_payment().getCash()) != 0) {
            LLCash.setVisibility(View.VISIBLE);
            CashTendered.setText(bill_detail.getBill_payment().getCash());
        }
        if (Float.parseFloat(bill_detail.getBill_payment().getCard()) != 0) {
            LLCard.setVisibility(View.VISIBLE);
            CardTendered.setText(bill_detail.getBill_payment().getCard());
        }
        if (Float.parseFloat(bill_detail.getBill_payment().getCoupon()) != 0) {
            LLCoupon.setVisibility(View.VISIBLE);
            CouponTendered.setText(bill_detail.getBill_payment().getCoupon());
        }
        if (Float.parseFloat(bill_detail.getBill_payment().getWallet()) != 0) {
            LLWallet.setVisibility(View.VISIBLE);
            WalletTendered.setText(bill_detail.getBill_payment().getWallet());
        }
        if (Float.parseFloat(bill_detail.getBill_payment().getLoyalty()) != 0) {
            LLLoyalty.setVisibility(View.VISIBLE);
            LoyaltyTendered.setText(bill_detail.getBill_payment().getLoyalty());
        }

        BalanceAmount = findViewById(R.id.tv_balance_amount);
        BalanceAmount.setText(bill_detail.getBill_payment().getCashamount_return());

        GST18 = findViewById(R.id.tv_gst18);
        GST18.setText(bill_detail.getTaxdetails().getHsnname());
        GST18Amount = findViewById(R.id.tv_gst_amount);
        GST18Amount.setText(bill_detail.getTaxdetails().getTaxvalue());
        CGST9 = findViewById(R.id.tv_cgst9);
        CGST9.setText(bill_detail.getTaxdetails().getCGST());
        SGST9 = findViewById(R.id.tv_sgst9);
        SGST9.setText(bill_detail.getTaxdetails().getSGST());

        CGSTAmount = findViewById(R.id.tv_cgst_amount);
        CGSTAmount.setText(bill_detail.getTaxdetails().getTaxamount());

        SGSTAmount = findViewById(R.id.tv_sgst_amount);
        SGSTAmount.setText(bill_detail.getTaxdetails().getTaxamount());
        BillNo = findViewById(R.id.tv_print_bill_no);
        BillNo.setText(bill_detail.getBill_payment().getBillno());

        TillNo = findViewById(R.id.tv_print_till_no);
        TillNo.setText(bill_detail.getBill_payment().getTillno());
        BillDate = findViewById(R.id.tv_bill_date);
        BillDate.setText(bill_detail.getBill_payment().getBilldate());
        BillTime = findViewById(R.id.tv_bill_time);
        BillTime.setText(bill_detail.getBill_payment().getBilltime());

        CustomerName = findViewById(R.id.tv_customer_name);
        CustomerName.setText(bill_detail.getCustomer_Casher().getCustomername());
        MobileNo = findViewById(R.id.tv_mobile_no);
        MobileNo.setText(bill_detail.getCustomer_Casher().getMobilenumber());

        CasherID = findViewById(R.id.tv_casher_id);
        CasherID.setText(bill_detail.getCustomer_Casher().getCasher_UserCode());

        GSTIN = findViewById(R.id.tv_gst_in);
        GSTIN.setText(bill_detail.getCustomer_Casher().getGST_TIN());

        FooterText = findViewById(R.id.tv_footer_text);
        String Text = "";
        for (int i = 0; i < bill_detail.getTerms_Condition().size(); i++) {
            Text = Text + bill_detail.getTerms_Condition().get(i).getValue() + " ";
        }
        FooterText.setText(Text);

        Qty = findViewById(R.id.tv_qty);
        Qty.setText(bill_detail.getBill_payment().getBillQty());
        TotalAmount = findViewById(R.id.tv_total_amount);
        TotalAmount.setText(bill_detail.getBill_payment().getBillvalue());


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPrinter.onActivityRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private final BroadcastReceiver ReceiptPrinterMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            com.cie.btp.DebugLog.logTrace("Printer Message Received");
            Bundle b = intent.getExtras();

            switch (b.getInt(RECEIPT_PRINTER_STATUS)) {
                case RECEIPT_PRINTER_CONN_STATE_NONE:
                    tvStatus.setText(R.string.printer_not_conn);
                    break;
                case RECEIPT_PRINTER_CONN_STATE_LISTEN:
                    tvStatus.setText(R.string.ready_for_conn);
                    break;
                case RECEIPT_PRINTER_CONN_STATE_CONNECTING:
                    tvStatus.setText(R.string.printer_connecting);
                    break;
                case RECEIPT_PRINTER_CONN_STATE_CONNECTED:
                    tvStatus.setText(R.string.printer_connected);
                    //new AsyncPrint().execute();
                    tvStatus.setText(title_connected_to);
                    tvStatus.append(mConnectedDeviceName);
                    tvStatus.setTextColor(Color.GREEN);
                    break;
                case RECEIPT_PRINTER_CONN_DEVICE_NAME:
                    //savePrinterMac(b.getString(RECEIPT_PRINTER_NAME));
                    break;
                case RECEIPT_PRINTER_NOTIFICATION_ERROR_MSG:
                    String n = b.getString(RECEIPT_PRINTER_MSG);
                    tvStatus.setText(n);
                    break;
                case RECEIPT_PRINTER_NOTIFICATION_MSG:
                    String m = b.getString(RECEIPT_PRINTER_MSG);
                    tvStatus.setText(m);
                    break;
                case RECEIPT_PRINTER_NOT_CONNECTED:

                    tvStatus.setText("Printer Not Connected");
                    tvStatus.setTextColor(Color.RED);
                    break;
                case RECEIPT_PRINTER_NOT_FOUND:
                    tvStatus.setText("SPrinter Not Found");
                    tvStatus.setTextColor(Color.RED);
                    break;
                case RECEIPT_PRINTER_SAVED:
                    tvStatus.setText(R.string.printer_saved);
                    break;
            }
        }
    };

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onPause() {

        try {
            mBtp.onActivityPause();
            mPrinter.onActivityPause();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onResume() {

        if (api == null) {
            Log.d("jokey", "new API");
            api = new PrinterAPI();
        }
        if (!SerialPortManager.getInstance().isPrintOpen()
                && !SerialPortManager.getInstance().openSerialPortPrinter()) {
            //  ToastUtil.showToast(this, "Toast Util Fail");
        }
        try {
            mBtp.onActivityResume();
            DebugLog.logTrace("onResume");
            mPrinter.onActivityPause();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mBtp.onActivityResult(requestCode, resultCode, this);
    }

    public void SendEbill(View v) {
        dialog = ProgressDialog.show(WebViewEbill.this, "",
                "Sending E-Bill...", true);

        Log.i(TAG, "sending ebill..");


        CustomerDB custmrdb = new CustomerDB(getApplicationContext());
        custmrdb.open();
        ArrayList<CustomerDetails> customerdata = custmrdb.getCustomerDetails();
        custmrdb.close();

        if (customerdata.size() == 0) {

            showFailedAlert("Mobile no is mandatory for Sending Bill");
            dialog.dismiss();
        } else {
            if (customerdata.size() > 0)
                if (!customerdata.get(0).getMobileNO().isEmpty()) {
                    if (net.isConnectingToInternet()) {

                        e_bill.setEnabled(false);
                        handler = new Handler();

                        handler.postDelayed(myRunnable = new Runnable() {
                            @Override
                            public void run() {

                                dialog.dismiss();
                            }
                        }, 30000);


                        String urlPath = "";
                        if (fromActivity != null && fromActivity.equalsIgnoreCase(Constants.MANUAL_BILL))
                            urlPath = PATH + "sentmbbebill?billNo=" + bill_no;
                        else
                            urlPath = PATH + "sentebill?billno=" + bill_no + "&location=" + Location;

                        Log.w(TAG, "Send E-Bill URL <<<::::>>>> " + urlPath);

                        ApiCall.make(this, urlPath, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        // display response
                                        try {
                                            JSONObject responseObject = new JSONObject(response);
                                            String Status = responseObject.getString("statusCode");
                                            String Msg = responseObject.getString("Message");
                                            if (Status.equalsIgnoreCase("200")) {
                                                dialog.dismiss();
                                                e_bill.setEnabled(true);
                                                handler.removeCallbacks(myRunnable);
                                                showFailedAlert("E-Bill send Successfully");
                                                Log.i(TAG, "E-Bill send Successfully");

                                            } else {
                                                dialog.dismiss();
                                                handler.removeCallbacks(myRunnable);
                                                e_bill.setEnabled(true);
                                                showFailedAlert("E-Bill not sent.");
                                                Log.i(TAG, "E-Bill not sent.");
                                            }
                                        } catch (JSONException e) {
                                            dialog.dismiss();
                                            e_bill.setEnabled(true);
                                            handler.removeCallbacks(myRunnable);
                                            showFailedAlert("E-Bill not sent.");
                                            Log.i(TAG, "E-Bill not sent.");

                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                        try {
                                            dialog.dismiss();
                                            e_bill.setEnabled(true);
                                            handler.removeCallbacks(myRunnable);
                                            if (error instanceof TimeoutError) {
                                                showFailedAlert("Time out error occurred.Please click on OK and try again");
                                                Log.i(TAG, "Time out error occurred.");

                                                //Time out error

                                            } else if (error instanceof NoConnectionError) {
                                                showFailedAlert("Network error occurred.Please click on OK and try again");
                                                Log.i(TAG, "Network error occurred.");
                                                //net work error

                                            } else if (error instanceof AuthFailureError) {
                                                showFailedAlert("Authentication error occurred.Please click on OK and try again");
                                                Log.i(TAG, "Authentication error occurred.");
                                                //error

                                            } else if (error instanceof ServerError) {
                                                showFailedAlert("Server error occurred.Please click on OK and try again");
                                                Log.i(TAG, "Server error occurred.");
                                                //Error
                                            } else if (error instanceof NetworkError) {
                                                showFailedAlert("Network error occurred.Please click on OK and try again");
                                                Log.i(TAG, "Network error occurred.");
                                                //Error

                                            } else if (error instanceof ParseError) {
                                                //Error
                                                showFailedAlert("An error occured.Please click on OK and try again");
                                                Log.i(TAG, "An error occurred.");
                                            } else {

                                                showFailedAlert("An error occured.Please click on OK and try again");
                                                Log.i(TAG, "An error occurred.");
                                                //Error
                                            }
                                            //End


                                        } catch (Exception e) {

                                            dialog.dismiss();
                                            e.printStackTrace();
                                        }

                                    }
                                }
                        );

                    } else {
                        dialog.dismiss();
                        AlertManager alert = new AlertManager();
                        alert.alert(WebViewEbill.this, "No Internet Connection",
                                "Please check " +
                                        "your data connection or Wifi is ON !");
                    }
                }
        }


    }


    public void WebAction() {

        webView = (WebView) findViewById(R.id.webView);
        webView.setVisibility(View.GONE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw();
        }
//        webView.loadUrl(Ebill_URL);
        swipe.setRefreshing(true);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }

            @Override
            public void onPageFinished(WebView view, final String url) {
                swipe.setRefreshing(false);

            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

//                webView.loadUrl("file://android_assets/error.html");
                webView.loadUrl(Ebill_URL);

            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // DO NOT CALL SUPER METHOD
//                super.onReceivedSslError(view, handler, error);
            }
        });

        webView.loadUrl(Ebill_URL);
        /* webView.setWebViewClient(new WebViewClient() {

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

                webView.loadUrl("file:///android_assets/error.html");

            }

            public void onPageFinished(WebView view, String url) {
                // do your stuff here
                swipe.setRefreshing(false);
            }

        });*/

    }

    @Override
    public void onBackPressed() {

        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }

    @Override
    public void onDestroy() {
        api.closePrint();
        try {
            mBtp.onActivityDestroy();
            mPrinter.onActivityDestroy();
            handler.removeCallbacks(myRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onDestroy();

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIPT_PRINTER_MESSAGES);
        LocalBroadcastManager.getInstance(this).registerReceiver(ReceiptPrinterMessageReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        api.close();
        super.onStop();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(ReceiptPrinterMessageReceiver);
        } catch (Exception e) {
            com.cie.btp.DebugLog.logException(e);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {


            showFailedAlert("Not Allowed");

        }
        return super.onKeyDown(keyCode, event);
    }


    public void deletetables() {
        Log.i(TAG, "Deleting customer details table.");
        CustomerDB cusDB = new CustomerDB(getApplicationContext());
        cusDB.open();
        cusDB.deleteCustomerTable();
        cusDB.close();

        Log.i(TAG, "Deleting Loyalty details table.");
        LoyaltyDetailsDB offrDB = new LoyaltyDetailsDB(getApplicationContext());
        offrDB.open();
        offrDB.deleteLoyaltyTable();
        offrDB.close();

        Log.i(TAG, "Deleting Loyalty Credentials details table.");
        LoyaltyCredentialsDB lyltyDB = new LoyaltyCredentialsDB(getApplicationContext());
        lyltyDB.open();
        lyltyDB.deleteLoyaltyCredentialsDetailsTable();
        lyltyDB.close();

        Log.i(TAG, "Deleting Product details table.");
        ProductDetailsDB prdctDB = new ProductDetailsDB(getApplicationContext());
        prdctDB.open();
        prdctDB.deleteUserTable();
        prdctDB.close();

        WalletDB walletDB = new WalletDB(getApplicationContext());
        walletDB.open();
        walletDB.deleteWalletTable();
        walletDB.close();


    }


    public void showFailedAlert(final String msg) {

        WebViewEbill.this.runOnUiThread(new Runnable() {
            public void run() {
                Log.e(TAG, msg);
                AlertDialog.Builder builder = new AlertDialog.Builder(WebViewEbill.this);
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

    private void makePrintBillAPIcall(String urlPath) {

        dialog = ProgressDialog.show(WebViewEbill.this, "",
                "Printing Bill...", true);
        ApiCall.make(this, urlPath, new Response.Listener<String>() {

                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.i(TAG, "Api call resonse:" + response);
                            JSONObject Jsonobj = new JSONObject(response);
                            String status = Jsonobj.getString("statusCode");

                            if (status.equalsIgnoreCase("200")) {
                                String PrintText = Jsonobj.getString("data");
                                TextPaint tp = new TextPaint();
                                Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/DroidSansMono.ttf");
                                BluetoothPrinter mBtp = PrinterActivity.mBtp;
                                PrinterDB printerDB = new PrinterDB(WebViewEbill.this);
                                printerDB.open();
                                ArrayList<PrinterDetails> printerDetails = printerDB.getPrinterDetails();
                                printerDB.close();
                                if (printerDetails.size() > 0) {


                                    InputStream inputStream = new ByteArrayInputStream(PrintText.substring(0, PrintText.length() - 2).getBytes(StandardCharsets.UTF_8));
                                    if (inputStream != null) {
                                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                                        String receiveString = "";
                                        StringBuilder stringBuilder = new StringBuilder();

                                        while ((receiveString = bufferedReader.readLine()) != null) {
                                            stringBuilder.append(receiveString);
                                            stringBuilder.append('\n');
                                        }

                                        inputStream.close();


                                        Bitmap Icon = BitmapFactory.decodeResource(getResources(), R.drawable.healthandglow);
                                        if (printerDetails.get(0).getPrinterName().equalsIgnoreCase("NGX")) {
                                            mBtp.setPreferredPrinter(printerDetails.get(0).getPrinterID());
                                            mBtp.setPrinterWidth(PrinterWidth.PRINT_WIDTH_72MM);
                                            tp.setTextSize(20);
                                            tp.setTypeface(tf);
                                            try {
                                                mBtp.printPicLogoImage(Icon);
                                                mBtp.addText(stringBuilder.toString(), Layout.Alignment.ALIGN_NORMAL, tp);
                                                mBtp.print();
                                                dialog.dismiss();
                                            } catch (Exception ex) {
                                                dialog.dismiss();
                                                showFailedAlert("Printer error occurred. Please try to reconnect the printer");

                                            }

                                        } else {
                                            Icon = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
                                            mPrinter.connectToPrinter();
                                            mPrinter.printGrayScaleImage(Icon, 10000);
                                            mPrinter.printTextLine(stringBuilder.toString());
                                            dialog.dismiss();
                                        }
                                    }

                                } else {
                                    dialog.dismiss();
                                    showFailedAlert("printer not configured");
                                }


                            }
                        } catch (Exception e) {
                            dialog.dismiss();
                            e.printStackTrace();
                        }
                        // Log.i("LOG_VOLLEY", response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();


                    }
                }

        );
    }

    @Override
    public void hot() {
        Toast.makeText(getApplicationContext(), "hot", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void noPaper() {
        Toast.makeText(getApplicationContext(), "noPaper", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void end() {
        Toast.makeText(getApplicationContext(), "end", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void work() {
        Toast.makeText(getApplicationContext(), "The printer is working", Toast.LENGTH_SHORT).show();
    }

    public void PrintBillRugtek() {
        api.printView(PrintViewRugtek, WebViewEbill.this);
    }


}
