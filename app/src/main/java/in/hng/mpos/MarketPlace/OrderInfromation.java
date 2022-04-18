package in.hng.mpos.MarketPlace;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import in.hng.mpos.Events.FetchVendorListEvent;
import in.hng.mpos.activity.LoyaltyActivity;
import in.hng.mpos.Networking.FetchVendorList;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.Detail;
import in.hng.mpos.gettersetter.FetchVendorsModel;

public class OrderInfromation extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    Button Proceed;
    RadioGroup radioGroup;
    Detail[] details;
    ArrayList<Detail> detailArrayList;
    EditText etOderId;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private ProgressDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_infromation);
        Init();
    }

    private void Init() {
        detailArrayList = new ArrayList<>();
        etOderId = findViewById(R.id.et_orderinfo_orderid);
        Proceed = findViewById(R.id.btn_market_proceed);
        radioGroup = findViewById(R.id.rg_orderinfo);
        radioGroup.setOrientation(LinearLayout.VERTICAL);
        radioGroup.removeAllViews();
        radioGroup.setOnCheckedChangeListener(this);
        Proceed.setOnClickListener(this);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        editor.putString("vendor_code", "");
        editor.commit();

        LoadVendors();
    }

    private void LoadVendors() {
        dialog = ProgressDialog.show(OrderInfromation.this, "",
                "Loading Partner List....", true);
        String url = "http://10.46.16.18/selfcheck_mkt/api/mposmarketplace/getparameter_mkt";

        new FetchVendorList().makeGetCallToServer(url);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }


    }

    @Override
    protected void onPause() {
        super.onPause();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe
    public void onSendCustomerInfoToServerResponse(FetchVendorListEvent event) {

        if (event.getStatusCode() == 200) {
            FetchVendorsModel fetchVendorsModel = new FetchVendorsModel();

            String mJsonString = event.responseString;
            JsonParser parser = new JsonParser();
            JsonElement mJson = parser.parse(mJsonString);
            Gson gson = new Gson();
            fetchVendorsModel = gson.fromJson(mJson, FetchVendorsModel.class);
            details = new Detail[fetchVendorsModel.getDetail().length];
            details = fetchVendorsModel.getDetail();
            LoadVendorList(details);
        } else {
            Toast.makeText(this, event.responseString, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
    }

    private void LoadVendorList(Detail[] detail) {


        for (int i = 0; i < detail.length; i++) {
            detailArrayList.add(detail[i]);
            addToRadioGroup(detail[i]);
        }

        dialog.dismiss();
    }

    private void addToRadioGroup(Detail detail) {
        RadioButton rbn = new RadioButton(this);
        rbn.setId(View.generateViewId());
        rbn.setText(detail.getValue_name());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        rbn.setLayoutParams(params);
        radioGroup.addView(rbn);
    }

    @Override
    public void onClick(View view) {
        String vendorcode = sp.getString("vendor_code", "");
        String orderid = etOderId.getText().toString();
        if (orderid.length() > 0) {
            editor.putString("order_id", orderid);
            editor.commit();
            if (vendorcode == "") {
                Toast.makeText(this, "Please select any Partners", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, ScanProducts.class);
                startActivity(intent);
                finish();
            }
        } else {
            Toast.makeText(this, "Please enter orderid", Toast.LENGTH_SHORT).show();
        }


    }


    @SuppressLint("ResourceType")
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {

        RadioButton checkedradioButton = (RadioButton) findViewById(i);
        String val = checkedradioButton.getText().toString();
        for (int j = 0; j < detailArrayList.size(); j++)
            if (detailArrayList.get(j).getValue_name() == val) {
                editor.putString("vendor_code", detailArrayList.get(j).getValue_code());
                editor.putString("vendor_name", detailArrayList.get(j).getValue_name());
                editor.commit();
            }
    }

    public void goBack() {
        Intent intent = new Intent(this, LoyaltyActivity.class);
        startActivity(intent);

    }
    //here we maintain our products in various departments


    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(OrderInfromation.this)
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


}
