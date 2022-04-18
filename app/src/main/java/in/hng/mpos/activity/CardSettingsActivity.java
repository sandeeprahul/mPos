package in.hng.mpos.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.eze.api.EzeAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import in.hng.mpos.Database.CardDB;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.CardDetails;
import in.hng.mpos.helper.Log;


public class CardSettingsActivity extends BaseActivity {

    private String TAG = CardSettingsActivity.class.getSimpleName();

    SwitchCompat innoviti_check, ezetap_check;
    private final int REQUEST_CODE_INITIALIZE = 10001;
    private final int REQUEST_CODE_PREPARE = 10002;
    private final int REQUEST_CODE_CLOSE = 10014;
    Button btnOK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_settings);

        setActionBarTitle("h&g mPOS - Card Settings");

        innoviti_check = (SwitchCompat) findViewById(R.id
                .innoviti_check);
        innoviti_check.setSwitchPadding(40);

        ezetap_check = (SwitchCompat) findViewById(R.id
                .ezetap_check);
        ezetap_check.setSwitchPadding(40);

        btnOK = (Button) findViewById(R.id.btnOK);

        CardDB edcDB = new CardDB(getApplicationContext());
        edcDB.open();
        ArrayList<CardDetails> edcDetails = edcDB.getEDCdetails();
        edcDB.close();
        if (edcDetails.size() > 0) {
            if (edcDetails.get(0).getEdcID().toString().equalsIgnoreCase("1")) {
                ezetap_check.setChecked(true);
            } else {
                innoviti_check.setChecked(true);
            }

        }

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(CardSettingsActivity.this)
                        .setMessage("Do you want to submit?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                CardDB edcDB = new CardDB(getApplicationContext());
                                edcDB.open();
                                ArrayList<CardDetails> edcDetails = edcDB.getEDCdetails();
                                edcDB.close();
                                if (edcDetails.size() > 0) {
                                    Intent i = new Intent(CardSettingsActivity.this, LoyaltyActivity.class);
                                    startActivity(i);
                                    finish();
                                } else {
                                    AlertDialog.Builder d = new AlertDialog.Builder(CardSettingsActivity.this);
                                    d.setTitle("EDC Setting");
                                    d.setMessage("Please select any the EDC device which you are using for payment");
                                    d.setPositiveButton("Ok",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            });
                                    d.show();

                                }

                            }

                        }).setNegativeButton("No", null)
                        .show();
            }
        });

        innoviti_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    ezetap_check.setChecked(false);
                    HashMap<String, String> PrinterDetails = new HashMap<String, String>();
                    PrinterDetails.put("edcID", "2");
                    PrinterDetails.put("edcName", "Innovitti");
                    CardDB cardDB = new CardDB(getApplicationContext());
                    cardDB.open();
                    cardDB.deletePrinterTable();
                    cardDB.createEDCDetailsTable();
                    cardDB.insertEDCDetails(PrinterDetails);
                    ArrayList<CardDetails> cardDetails = cardDB.getEDCdetails();
                    cardDB.close();

                } else {
                    CardDB cardDB = new CardDB(getApplicationContext());
                    cardDB.open();
                    cardDB.deletePrinterTable();
                    cardDB.createEDCDetailsTable();
                    cardDB.close();
                }

            }
        });

        ezetap_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    innoviti_check.setChecked(false);
                    doInitializeEzeTap();
                    doPrepareDeviceEzeTap();
                    HashMap<String, String> PrinterDetails = new HashMap<String, String>();
                    PrinterDetails.put("edcID", "1");
                    PrinterDetails.put("edcName", "Ezetap");
                    CardDB cardDB = new CardDB(getApplicationContext());
                    cardDB.open();
                    cardDB.deletePrinterTable();
                    cardDB.createEDCDetailsTable();
                    cardDB.insertEDCDetails(PrinterDetails);
                    ArrayList<CardDetails> cardDetails = cardDB.getEDCdetails();
                    cardDB.close();

                } else {
                    //doCloseEzetap();
                    CardDB cardDB = new CardDB(getApplicationContext());
                    cardDB.open();
                    cardDB.deletePrinterTable();
                    cardDB.createEDCDetailsTable();
                    cardDB.close();
                }

            }
        });
    }

    private void doInitializeEzeTap() {

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("demoAppKey", "626169f2-44fe-46a2-ab43-1a2a2b029db0");
            jsonRequest.put("prodAppKey", "52692f5a-8367-497b-9500-1a6a90911218");
            jsonRequest.put("merchantName", "HEALTH_AND_GLOW");
            jsonRequest.put("userName", "8105277766");
            jsonRequest.put("currencyCode", "INR");
            jsonRequest.put("appMode", "PROD");
            jsonRequest.put("captureSignature", "true");
            jsonRequest.put("prepareDevice", "true");
            EzeAPI.initialize(this, REQUEST_CODE_INITIALIZE, jsonRequest);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSON Exception <<::>> "+e);
        }

        /*JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("demoAppKey", "626169f2-44fe-46a2-ab43-1a2a2b029db0");
            jsonRequest.put("prodAppKey", "626169f2-44fe-46a2-ab43-1a2a2b029db0");
            jsonRequest.put("merchantName", "HEALTH_AND_GLOW");
            jsonRequest.put("userName", "8105277766");
            jsonRequest.put("currencyCode", "INR");
            jsonRequest.put("appMode", "DEMO");
            jsonRequest.put("captureSignature", "true");
            jsonRequest.put("prepareDevice", "true");
            EzeAPI.initialize(this, REQUEST_CODE_INITIALIZE, jsonRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    private void doPrepareDeviceEzeTap() {
        EzeAPI.prepareDevice(this, REQUEST_CODE_PREPARE);
    }

    private void doCloseEzetap() {
        EzeAPI.close(this, REQUEST_CODE_CLOSE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            new AlertDialog.Builder(CardSettingsActivity.this)
                    .setMessage("Are you sure you want to Cancel?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            CardDB edcDB = new CardDB(getApplicationContext());
                            edcDB.open();
                            ArrayList<CardDetails> edcDetails = edcDB.getEDCdetails();
                            edcDB.close();
                            if (edcDetails.size() > 0) {
                                Intent i = new Intent(CardSettingsActivity.this, LoyaltyActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                AlertDialog.Builder d = new AlertDialog.Builder(CardSettingsActivity.this);
                                d.setTitle("Printer Setting");
                                d.setMessage("Please select any the EDC device which you are using for payment");
                                d.setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

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
