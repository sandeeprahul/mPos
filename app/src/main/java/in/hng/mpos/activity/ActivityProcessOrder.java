package in.hng.mpos.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import in.hng.mpos.Database.OrderedProductDetailsDB;
import in.hng.mpos.Database.ProcessedOrderDB;
import in.hng.mpos.Adapter.ListAdapter;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.ProductList;


public class ActivityProcessOrder extends BaseActivity {

    private static final String TAG = "ActivityProcessOrder";

    ListView listView;
    TextView bill_no, date, location_code, cashier_id, total_amt, total_qty, sku_count;
    Button Submit;

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    ListAdapter adapter;

    ArrayList<ProductList> skuList;
    int totalQty = 0;
    String Bill_no, Location_code, Cashier_code, Total_Amt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_process);

        setActionBarTitle("h&g Assisted Order");

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        editor.apply();

        Bill_no = sp.getString("Bill_no", "");
        Location_code = sp.getString("Location_code", "");
        Cashier_code = sp.getString("Cashier_code", "");
        Total_Amt = sp.getString("Total_Amt", "");
        skuList = readFinalData();
        listView = findViewById(R.id.listv);
        bill_no = findViewById(R.id.txtbillNo);
        date = findViewById(R.id.txtdate);
        location_code = findViewById(R.id.txtlocation);
        cashier_id = findViewById(R.id.txtcashierID);
        total_amt = findViewById(R.id.txttotalAmt);
        total_qty = findViewById(R.id.txttotalQty);
        sku_count = findViewById(R.id.txtSkuCount);
        Submit = findViewById(R.id.btnSubmit);
        //pay_bill = findViewById(R.id.paybill);
        //cancel_bill = findViewById(R.id.BillCancel);
        //new_bill= findViewById(R.id.BillNew);

        adapter = new ListAdapter(ActivityProcessOrder.this, skuList);
        listView.setAdapter(adapter);
        bill_no.setText(Bill_no);
        String date_now = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        date.setText(date_now);
        location_code.setText(Location_code);
        cashier_id.setText(Cashier_code);
        total_amt.setText(Total_Amt);
        for (int i = 0; i < skuList.size(); i++) {
            totalQty = totalQty + Integer.parseInt(skuList.get(i).getQty());
        }
        total_qty.setText(String.valueOf(totalQty));
        sku_count.setText(String.valueOf(skuList.size()));

    }

    public void SubmitOrder(View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityProcessOrder.this);
        builder.setTitle("HnG Order Taking");
        builder.setMessage("Order Processed Succesfully")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        OrderedProductDetailsDB podb = new OrderedProductDetailsDB(getApplicationContext());
                        podb.open();
                        podb.deleteOrder(Bill_no);
                        podb.close();

                        Intent i = new Intent(ActivityProcessOrder.this, ActivityLoadOrders.class);
                        startActivity(i);
                        finish();

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    public ArrayList<ProductList> readFinalData() {
        ProcessedOrderDB podb = new ProcessedOrderDB(getApplicationContext());
        podb.open();
        ArrayList<ProductList> skuList = podb.getAllProductsDetails();
        podb.close();
        return skuList;
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(ActivityProcessOrder.this)
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

    private void goBack() {
        editor.putString("Bill_no", Bill_no);
        editor.apply();
        Intent i = new Intent(ActivityProcessOrder.this, ActivityScanOrderedProducts.class);
        startActivity(i);
        finish();
    }
}
