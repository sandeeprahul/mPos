package in.hng.mpos.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.cie.btp.CieBluetoothPrinter;
import com.ngx.BluetoothPrinter;
import com.ngx.DebugLog;

import java.util.ArrayList;
import java.util.HashMap;

import in.hng.mpos.Database.CardDB;
import in.hng.mpos.Database.PrinterDB;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.CardDetails;
import in.hng.mpos.gettersetter.PrinterDetails;

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

public class PrinterActivity extends AppCompatActivity {

    public static BluetoothPrinter mBtp = BluetoothPrinter.INSTANCE;
    public static CieBluetoothPrinter mPrinter = CieBluetoothPrinter.INSTANCE;
    private static FragmentManager fragMgr;
    private Fragment nm;
    private static final String cHomeStack = "home";
    private TextView tvName, tvID, tvStatus;
    Button btnConnect, btnClear, btnUnpair, btnCancel;
    public static SharedPreferences mSp;
    private static LinearLayout ngxLyt, exelLyt;
    SwitchCompat ngx_check, exel_check, rugtek_check;
    private String mConnectedDeviceName = "";
    private String mConnectedDeviceID = "";
    public static final String title_connecting = "connecting...";
    public static final String title_connected_to = "connected: ";
    public static final String title_not_connected = "not connected";
    private boolean isNGX = false, isExel = false;

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothPrinter.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothPrinter.STATE_CONNECTED:
                            tvStatus.setText("Connected");
                            HashMap<String, String> PrinterDetails = new HashMap<String, String>();
                            PrinterDetails.put("printerID", mConnectedDeviceID);
                            PrinterDetails.put("printerName", "NGX");
                            PrinterDB printerDB = new PrinterDB(getApplicationContext());
                            printerDB.open();
                            printerDB.deletePrinterTable();
                            printerDB.createPrinterDetailsTable();
                            printerDB.insertPrinterDetails(PrinterDetails);
                            ArrayList<in.hng.mpos.gettersetter.PrinterDetails> printerDetails = printerDB.getPrinterDetails();
                            printerDB.close();
                            if (printerDetails.size() > 0) {
                                tvName.setText(printerDetails.get(0).getPrinterName());
                                tvID.setText(printerDetails.get(0).getPrinterID());
                                //
                            }
                            isNGX = true;
                            //tvStatus.append(mConnectedDeviceName);
                            break;
                        case BluetoothPrinter.STATE_CONNECTING:
                            tvStatus.setText(title_connecting);
                            break;
                        case BluetoothPrinter.STATE_LISTEN:
                        case BluetoothPrinter.STATE_NONE:
                            tvStatus.setText(title_not_connected);
                            break;
                    }
                    break;
                case BluetoothPrinter.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(
                            BluetoothPrinter.DEVICE_NAME);
                    mConnectedDeviceID = msg.getData().getString(
                            BluetoothPrinter.DEVICE_MACID);
                    break;

                case BluetoothPrinter.MESSAGE_STATUS:
                    tvStatus.setText("not connected");
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_settings);


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
        title.setText("h&g mPOS - Printer Settings");
        title.setVisibility(View.VISIBLE);

        tvName = findViewById(R.id.txtprinterName);
        tvID = findViewById(R.id.txtprinterID);
        tvStatus = findViewById(R.id.txtprinterStatus);
        btnConnect = findViewById(R.id.connectNGX);
        btnClear = findViewById(R.id.clearNGX);
        btnUnpair = findViewById(R.id.unpairNGX);
        btnCancel = findViewById(R.id.cancelPrinter);
        ngxLyt = findViewById(R.id.ngx_lyt);
        exelLyt = findViewById(R.id.exel_lyt);
        ngx_check = findViewById(R.id
                .NGX_check);
        ngx_check.setSwitchPadding(40);

        rugtek_check = (SwitchCompat) findViewById(R.id
                .rugtek_check);
        rugtek_check.setSwitchPadding(40);

        exel_check = (SwitchCompat) findViewById(R.id
                .Exel_check);
        exel_check.setSwitchPadding(40);

        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mAdapter == null) {
            Toast.makeText(this, R.string.bt_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        try {
            mPrinter.initService(PrinterActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mBtp.initService(this, mHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvStatus.setText("");
        PrinterDB printerDB = new PrinterDB(getApplicationContext());
        printerDB.open();
        ArrayList<PrinterDetails> printerDetails = printerDB.getPrinterDetails();
        printerDB.close();
        if (printerDetails.size() > 0) {

            tvName.setText(printerDetails.get(0).getPrinterName());
            tvID.setText(printerDetails.get(0).getPrinterID()); //00:04:3E:6B:28:50


            if (printerDetails.get(0).getPrinterName().equalsIgnoreCase("NGX")) {
                isNGX = true;
                ngx_check.setChecked(true);
                ngxLyt.setVisibility(View.VISIBLE);
                mBtp.setPreferredPrinter(printerDetails.get(0).getPrinterID());

            } else {
                isExel = true;
                exel_check.setChecked(true);
                exelLyt.setVisibility(View.VISIBLE);
            }
        }


        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mBtp.showDeviceList(PrinterActivity.this);

            }

        });

        btnUnpair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Builder u = new Builder(PrinterActivity.this);
                u.setTitle("Bluetooth Printer unpair");
                // d.setIcon(R.drawable.ic_launcher);
                u.setMessage("Are you sure you want to unpair all Bluetooth printers ?");
                u.setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (mBtp.unPairBluetoothPrinters()) {
                                    PrinterDB printerDB = new PrinterDB(getApplicationContext());
                                    printerDB.open();
                                    printerDB.deletePrinterTable();
                                    printerDB.createPrinterDetailsTable();
                                    printerDB.close();
                                    tvName.setText("");
                                    tvID.setText("");
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "All NGX Bluetooth printer(s) unpaired",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                u.setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        });
                u.show();


            }

        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Builder d = new Builder(PrinterActivity.this);
                d.setTitle("NGX Bluetooth Printer");
                // d.setIcon(R.drawable.ic_launcher);
                d.setMessage("Are you sure you want to delete your preferred Bluetooth printer ?");
                d.setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                mBtp.clearPreferredPrinter();

                                PrinterDB printerDB = new PrinterDB(getApplicationContext());
                                printerDB.open();
                                printerDB.deletePrinterTable();
                                printerDB.createPrinterDetailsTable();
                                printerDB.close();
                                tvName.setText("");
                                tvID.setText("");
                                Toast.makeText(getApplicationContext(),
                                        "Preferred Bluetooth printer cleared",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                d.setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        });
                d.show();

            }

        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(PrinterActivity.this)
                        .setMessage("Are you sure you want to Submit?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                PrinterDB printerDB = new PrinterDB(getApplicationContext());
                                printerDB.open();
                                ArrayList<PrinterDetails> printerDetails = printerDB.getPrinterDetails();
                                printerDB.close();
                                if (printerDetails.size() > 0) {
                                    CardDB cardDB = new CardDB(getApplicationContext());
                                    cardDB.open();
                                    ArrayList<CardDetails> cardDetails = cardDB.getEDCdetails();
                                    cardDB.close();
                                    if (cardDetails.size() <= 0) {
                                        AlertDialog.Builder d = new AlertDialog.Builder(PrinterActivity.this);
                                        d.setTitle("Card Setting");
                                        // d.setIcon(R.drawable.ic_launcher);
                                        d.setMessage("EDC details not configured, Please click OK to continue");
                                        d.setPositiveButton("OK",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        Intent intent = new Intent(PrinterActivity.this, CardSettingsActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });
                                        d.show();
                                    } else {

                                        Intent i = new Intent(PrinterActivity.this, LoyaltyActivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                } else {

                                    Builder d = new Builder(PrinterActivity.this);
                                    d.setTitle("Printer Setting");
                                    // d.setIcon(R.drawable.ic_launcher);
                                    d.setMessage("You will not be able to print bill without configuring printer. Do you want to continue?");
                                    d.setPositiveButton("Yes",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {

                                                    Intent i = new Intent(PrinterActivity.this, LoyaltyActivity.class);
                                                    startActivity(i);
                                                    finish();
                                                }
                                            });
                                    d.setNegativeButton("No",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // do nothing
                                                }
                                            });
                                    d.show();

                                    // Toast.makeText(PrinterActivity.this, "Printer Not Configured", Toast.LENGTH_SHORT).show();
                                }


                            }

                        }).setNegativeButton("No", null)
                        .show();
            }

        });

        ngx_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    exel_check.setChecked(false);
                    rugtek_check.setChecked(false);
                    ngxLyt.setVisibility(View.VISIBLE);
                    exelLyt.setVisibility(View.GONE);


                } else {
                    ngxLyt.setVisibility(View.GONE);
                    PrinterDB printerDB = new PrinterDB(getApplicationContext());
                    printerDB.open();
                    printerDB.deletePrinterTable();
                    printerDB.createPrinterDetailsTable();
                    printerDB.close();
                    tvName.setText("");
                    tvID.setText("");
                    tvStatus.setText("not connected");
                }

            }
        });

        exel_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    ngx_check.setChecked(false);
                    rugtek_check.setChecked(false);
                    ngxLyt.setVisibility(View.GONE);
                    exelLyt.setVisibility(View.VISIBLE);


                } else {
                    //doCloseEzetap();
                    exelLyt.setVisibility(View.GONE);
                    PrinterDB printerDB = new PrinterDB(getApplicationContext());
                    printerDB.open();
                    printerDB.deletePrinterTable();
                    printerDB.createPrinterDetailsTable();
                    printerDB.close();
                    tvName.setText("");
                    tvID.setText("");
                    tvStatus.setText("not connected");
                }

            }
        });

        rugtek_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    ngx_check.setChecked(false);
                    exel_check.setChecked(false);
                    tvStatus.setText("connected");

                    HashMap<String, String> PrinterDetails = new HashMap<String, String>();
                    PrinterDetails.put("printerID", "N/A");
                    PrinterDetails.put("printerName", "Rugtek");
                    PrinterDB printerDB = new PrinterDB(getApplicationContext());
                    printerDB.open();
                    printerDB.deletePrinterTable();
                    printerDB.createPrinterDetailsTable();
                    printerDB.insertPrinterDetails(PrinterDetails);
                    ArrayList<in.hng.mpos.gettersetter.PrinterDetails> printerDetails = printerDB.getPrinterDetails();
                    printerDB.close();
                    if (printerDetails.size() > 0) {
                        tvName.setText(printerDetails.get(0).getPrinterName());
                        tvID.setText(printerDetails.get(0).getPrinterID());
                        //
                    }

                } else {
//                    //doCloseEzetap();
//                    exelLyt.setVisibility(View.GONE);
                    PrinterDB printerDB = new PrinterDB(getApplicationContext());
                    printerDB.open();
                    printerDB.deletePrinterTable();
                    printerDB.createPrinterDetailsTable();
                    printerDB.close();
                    tvName.setText("");
                    tvID.setText("");
                    tvStatus.setText("Not connected");
                }

            }
        });


        final Button btnConnectExelPrinter = (Button) findViewById(R.id.connectExel);
        btnConnectExelPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPrinter.disconnectFromPrinter();
                mPrinter.selectPrinter(PrinterActivity.this);
            }
        });

        final Button btnClearExelPrinter = (Button) findViewById(R.id.clearExel);
        btnClearExelPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Builder d = new Builder(PrinterActivity.this);
                d.setTitle("NGX Bluetooth Printer");
                // d.setIcon(R.drawable.ic_launcher);
                d.setMessage("Are you sure you want to delete your preferred Bluetooth printer ?");
                d.setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                mPrinter.clearPreferredPrinter();

                                PrinterDB printerDB = new PrinterDB(getApplicationContext());
                                printerDB.open();
                                printerDB.deletePrinterTable();
                                printerDB.createPrinterDetailsTable();
                                printerDB.close();
                                tvName.setText("");
                                tvID.setText("");
                                Toast.makeText(getApplicationContext(),
                                        "Preferred Bluetooth printer cleared",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                d.setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        });
                d.show();
            }
        });
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
                    break;
                case RECEIPT_PRINTER_CONN_DEVICE_NAME:
                    //savePrinterMac(b.getString(RECEIPT_PRINTER_NAME));
                    break;
                case RECEIPT_PRINTER_NOTIFICATION_ERROR_MSG:
                    String n = b.getString(RECEIPT_PRINTER_MSG);
                    tvStatus.setText(R.string.printer_not_conn);
                    break;
                case RECEIPT_PRINTER_NOTIFICATION_MSG:
                    String m = b.getString(RECEIPT_PRINTER_MSG);
                    tvStatus.setText(m);
                    break;
                case RECEIPT_PRINTER_NOT_CONNECTED:
                    tvStatus.setText("Status : Printer Not Connected");
                    break;
                case RECEIPT_PRINTER_NOT_FOUND:
                    tvStatus.setText("Status : Printer Not Found");
                    break;
                case RECEIPT_PRINTER_SAVED:
                    tvStatus.setText(R.string.printer_connected);

                    HashMap<String, String> PrinterDetails = new HashMap<String, String>();
                    PrinterDetails.put("printerID", mPrinter.name());
                    PrinterDetails.put("printerName", "Exel");
                    PrinterDB printerDB = new PrinterDB(getApplicationContext());
                    printerDB.open();
                    printerDB.deletePrinterTable();
                    printerDB.createPrinterDetailsTable();
                    printerDB.insertPrinterDetails(PrinterDetails);
                    ArrayList<in.hng.mpos.gettersetter.PrinterDetails> printerDetails = printerDB.getPrinterDetails();
                    printerDB.close();
                    if (printerDetails.size() > 0) {
                        tvName.setText(printerDetails.get(0).getPrinterName());
                        tvID.setText(printerDetails.get(0).getPrinterID());
                        //
                    }
                    isExel = true;
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
        if (isNGX)
            mBtp.onActivityPause();
        if (isExel)
            mPrinter.onActivityPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        if (isNGX)
            mBtp.onActivityResume();
        if (isExel)
            mPrinter.onActivityPause();
        DebugLog.logTrace("onResume");
        super.onResume();
    }

    @Override
    public void onDestroy() {
        // mBtp.onActivityDestroy();
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
        super.onStop();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(ReceiptPrinterMessageReceiver);
        } catch (Exception e) {
            com.cie.btp.DebugLog.logException(e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mBtp.onActivityResult(requestCode, resultCode, this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            new AlertDialog.Builder(PrinterActivity.this)
                    .setMessage("Are you sure you want to Cancel?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            PrinterDB printerDB = new PrinterDB(getApplicationContext());
                            printerDB.open();
                            ArrayList<PrinterDetails> printerDetails = printerDB.getPrinterDetails();
                            printerDB.close();
                            if (printerDetails.size() > 0) {
                                CardDB cardDB = new CardDB(getApplicationContext());
                                cardDB.open();
                                ArrayList<CardDetails> cardDetails = cardDB.getEDCdetails();
                                cardDB.close();
                                if (cardDetails.size() <= 0) {
                                    AlertDialog.Builder d = new AlertDialog.Builder(PrinterActivity.this);
                                    d.setTitle("Card Setting");
                                    // d.setIcon(R.drawable.ic_launcher);
                                    d.setMessage("EDC details not configured, Please click OK to continue");
                                    d.setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {

                                                    Intent intent = new Intent(PrinterActivity.this, CardSettingsActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });
                                    d.show();
                                } else {

                                    Intent i = new Intent(PrinterActivity.this, LoyaltyActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                            } else {
                                Builder d = new Builder(PrinterActivity.this);
                                d.setTitle("Printer Setting");
                                // d.setIcon(R.drawable.ic_launcher);
                                d.setMessage("You will not be able to print bill without configuring printer. Do you want to continue?");
                                d.setPositiveButton("Yes",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                                Intent i = new Intent(PrinterActivity.this, LoyaltyActivity.class);
                                                startActivity(i);
                                                finish();
                                            }
                                        });
                                d.setNegativeButton("No",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // do nothing
                                            }
                                        });
                                d.show();

                            }

                        }

                    }).setNegativeButton("No", null)
                    .show();
        }
        return super.onKeyDown(keyCode, event);
    }
}
