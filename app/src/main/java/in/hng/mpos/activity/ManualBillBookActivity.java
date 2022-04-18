package in.hng.mpos.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import in.hng.mpos.Adapter.ManualBillAdapter;
import in.hng.mpos.Database.CustomerDB;
import in.hng.mpos.Database.ProductDetailsDB;
import in.hng.mpos.Database.UrlDB;
import in.hng.mpos.Database.UserDB;
import in.hng.mpos.R;
import in.hng.mpos.Utils.Constants;
import in.hng.mpos.gettersetter.CustomerDetails;
import in.hng.mpos.gettersetter.ManualBillModel;
import in.hng.mpos.gettersetter.MrpList;
import in.hng.mpos.gettersetter.ProductList;
import in.hng.mpos.gettersetter.UserDetails;
import in.hng.mpos.helper.ApiCall;
import in.hng.mpos.helper.AppController;
import in.hng.mpos.helper.Log;
import in.hng.mpos.interfaces.UpdateTotal;

public class ManualBillBookActivity extends AppCompatActivity implements UpdateTotal {

    private String TAG = ManualBillBookActivity.class.getSimpleName();

    Button btnProductSearch, btnProcess, btnCancel;
    TextView tvDate, tvTotalAmount, tvCharacterCnt;
    EditText etSkuCode, etReasonForManualUser, etCustomerName, etMobileNumber;
    LinearLayout llTotal;
    RecyclerView rvManualBill;
    Spinner spBillReason;
    View llBottomView;

    ManualBillAdapter manualBillAdapter;
    List<ManualBillModel> manualBillModelList = new ArrayList<>();

    String PATH, Location, tillNo, cashierId;

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy hh:mm aa");
    String url = "";
    String billno = "";

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private ProgressDialog dialog = null;

    //showQtyMrpDialog variables
    Dialog billDialog;
    private EditText DlgEtSkuCode, DlgEtProductName, DlgEtQty, DlgEtMrp;
    private Spinner spReason;

    HashMap<String, String> productBaseReasonMap = new HashMap<>();
    HashMap<String, String> overAllBillReasonMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_bill_book);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        UrlDB urlDB = new UrlDB(getApplicationContext());
        PATH = urlDB.getUrlDetails();
        Log.d(TAG, "URL PATH ==========> " + PATH);

        UserDB userDB = new UserDB(getApplicationContext());
        userDB.open();
        ArrayList<UserDetails> userDetailses = userDB.getUserDetails();
        userDB.close();

        Location = userDetailses.get(0).getStoreID();
        tillNo = userDetailses.get(0).getTillNo();
        cashierId = userDetailses.get(0).getUserID();
        Log.w(TAG, "Location " + Location + " TillNo " + tillNo + " cashierId " + cashierId);

        billno = sp.getString("Bill_no", "");
        Log.w(TAG, "Bill NO############# " + billno);

        btnProcess = findViewById(R.id.btn_process);
        btnCancel = findViewById(R.id.btn_cancel);
        btnProductSearch = findViewById(R.id.btnProductSearch);
        etSkuCode = findViewById(R.id.etSkuCode);
        tvDate = findViewById(R.id.tvDate);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        rvManualBill = findViewById(R.id.rvManualBill);
        llTotal = findViewById(R.id.llTotal);
        llBottomView = findViewById(R.id.llBottomView);
        etReasonForManualUser = findViewById(R.id.etReasonForManualUser);
        etCustomerName = findViewById(R.id.etCustomerName);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        spBillReason = findViewById(R.id.spBillReason);
        tvCharacterCnt = findViewById(R.id.tvCharacterCnt);

        getReasons();

        manualBillAdapter = new ManualBillAdapter(this, manualBillModelList, productBaseReasonMap, this);
        rvManualBill.setHasFixedSize(true);
        rvManualBill.setLayoutManager(new LinearLayoutManager(this));
        rvManualBill.setAdapter(manualBillAdapter);
        manualBillAdapter.clearList();

        tvDate.setText(dateFormat.format(new Date()));

        etReasonForManualUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().isEmpty()) {
                    if (editable.toString().length() > 100)
                        showFailedAlert("Cannot be greater than 100 Character");
                    else
                        tvCharacterCnt.setText(String.valueOf(editable.toString().length()));
                } else
                    tvCharacterCnt.setText("0");
            }
        });

        CustomerDB custmrdb = new CustomerDB(getApplicationContext());
        custmrdb.open();
        ArrayList<CustomerDetails> customerdata = custmrdb.getCustomerDetails();
        custmrdb.close();

        Log.w(TAG, "Customer Data Size " + customerdata.size());

        if (customerdata.size() > 0) {
            etCustomerName.setText(customerdata.get(0).getCustomerName());
            etMobileNumber.setText(customerdata.get(0).getMobileNO());
            Log.w(TAG, "Mobile Number########## " + customerdata.get(0).getMobileNO());
        }

        try {

            ProductDetailsDB prdctDB = new ProductDetailsDB(getApplicationContext());
            prdctDB.open();
            ArrayList<ProductList> productLists = prdctDB.getEANDetails();

            if (productLists.size() > 0) {

                for (ProductList productList : productLists) {
                    ManualBillModel manualBillModel = new ManualBillModel();
                    manualBillModel.setEanCode(productList.getEanCode());
                    manualBillModel.setSkuCode(productList.getSkuCode());
                    manualBillModel.setReasonName(productList.getStoreSKU());
                    manualBillModel.setProductName(productList.getSkuName().replace(productList.getSkuCode() + "-", ""));
                    manualBillModel.setQty(productList.getQty());
                    manualBillModel.setMrp(productList.getMrp());

                    updateBill(manualBillModel);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        btnProductSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getReasons();
                searchEan();
                //showQtyMrpDialog(etSkuCode.getText().toString().trim());
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ManualBillBookActivity.this);
                builder.setTitle("HnG POS");
                builder.setMessage("Are you sure you want to cancel manual billing?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    if (validate()) {

                        dialog = ProgressDialog.show(ManualBillBookActivity.this, "",
                                "Processing Bill...", true);

                        JSONArray detailLogArray = new JSONArray();
                        JSONArray headerArray = new JSONArray();
                        JSONObject finalJsonObject = new JSONObject();

                        for (ManualBillModel manualBillModel : manualBillAdapter.getManualBillModelList()) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("locationCode", Location);
                            jsonObject.put("storeSkuLocNo", 0);
                            jsonObject.put("eanCode", manualBillModel.getEanCode());
                            jsonObject.put("skuCode", manualBillModel.getSkuCode());
                            jsonObject.put("skuName", manualBillModel.getProductName());
                            jsonObject.put("skuQty", manualBillModel.getQty());
                            jsonObject.put("mrp", manualBillModel.getMrp());
                            jsonObject.put("casherCode", cashierId);
                            jsonObject.put("tillno", tillNo);
                            jsonObject.put("reasonCode", manualBillModel.getReasonCode());
                            detailLogArray.put(jsonObject);
                        }

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("locationCode", Location);
                        jsonObject.put("billNo", billno.isEmpty() ? "" : billno);
                        jsonObject.put("tillNo", tillNo);
                        jsonObject.put("customerName", etCustomerName.getText().toString().trim());
                        jsonObject.put("phoneNo", etMobileNumber.getText().toString().trim());
                        jsonObject.put("userCode", cashierId);
                        jsonObject.put("reasonForManual", etReasonForManualUser.getText().toString().trim());
                        jsonObject.put("reasonCode", overAllBillReasonMap.get(spBillReason.getSelectedItem().toString()));
                        headerArray.put(jsonObject);

                        finalJsonObject.put("detailLog", detailLogArray);
                        finalJsonObject.put("header", headerArray);

                        Log.w(TAG, "Final JsonObject Manual Billing " + finalJsonObject.toString());

                        processManualBill(finalJsonObject.toString());

                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    dismissDialog();
                }
            }
        });

    }

    private void showQtyMrpDialog(String skuCode) {

        Dialog billDialog = new Dialog(this);
        billDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        billDialog.setContentView(R.layout.manual_bill_mrp_qty_dialog);
        billDialog.setCancelable(false);
        billDialog.setCanceledOnTouchOutside(false);

        DlgEtSkuCode = billDialog.findViewById(R.id.etSkuCode);
        DlgEtProductName = billDialog.findViewById(R.id.etProductName);
        DlgEtQty = billDialog.findViewById(R.id.dlgEtQty);
        DlgEtMrp = billDialog.findViewById(R.id.dlgEtMrp);
        Button dlgbtnAdd = billDialog.findViewById(R.id.btn_dlg_add);
        Button dlgbtnCancel = billDialog.findViewById(R.id.btn_dlg_cancel);
        Spinner mySpinner = billDialog.findViewById(R.id.SpnrMrp);
        spReason = billDialog.findViewById(R.id.spReason);
        mySpinner.setVisibility(View.GONE);
        DlgEtMrp.setVisibility(View.VISIBLE);

        Log.w(TAG, "productBaseReasonMap Size " + productBaseReasonMap.size());

        List<String> productManualReason = new ArrayList<>(productBaseReasonMap.keySet());

        Log.w(TAG, "productManualReason List Size " + productManualReason.size());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_items, productManualReason);
        spReason.setAdapter(adapter);

        DlgEtSkuCode.setText(skuCode);
        DlgEtProductName.setText(sku_name);

        if (DlgEtSkuCode.getText().toString().isEmpty())
            DlgEtSkuCode.requestFocus();
        else if (DlgEtProductName.getText().toString().isEmpty())
            DlgEtProductName.requestFocus();
        else if (DlgEtQty.getText().toString().isEmpty())
            DlgEtQty.requestFocus();
        else if (DlgEtMrp.getText().toString().isEmpty())
            DlgEtMrp.requestFocus();

        dlgbtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.w(TAG, "Length of Sku code " + DlgEtSkuCode.getText().toString().trim().length());

                try {
                    if (dialogValidation()) {
                        hideKeyboard();
                        ManualBillModel manualBillModel = new ManualBillModel();
                        if (eanCode.isEmpty()) {
                            manualBillModel.setEanCode(DlgEtSkuCode.getText().toString().trim());
                        } else
                            manualBillModel.setEanCode(eanCode);

                        if (skuCode.isEmpty())
                            manualBillModel.setSkuCode(DlgEtSkuCode.getText().toString().trim());
                        else
                            manualBillModel.setSkuCode(skuCode);

                        if (mrp_list.size() > 0)
                            manualBillModel.setStoreSkuLocNo(mrp_list.get(0).getStoreSKU());
                        else
                            manualBillModel.setStoreSkuLocNo(storeSKU);

                        manualBillModel.setProductName(DlgEtProductName.getText().toString().trim());
                        manualBillModel.setQty(DlgEtQty.getText().toString().trim());
                        manualBillModel.setMrp(DlgEtMrp.getText().toString().trim());
                        manualBillModel.setTaxCode(TAXcode);
                        manualBillModel.setTaxRate(TAXrate);
                        manualBillModel.setReasonCode(productBaseReasonMap.get(spReason.getSelectedItem().toString()));
                        manualBillModel.setReasonName(spReason.getSelectedItem().toString());

                        updateBill(manualBillModel);
                        billDialog.dismiss();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showFailedAlert("Something went wrong! Please contact IT support ");
                }
            }
        });

        dlgbtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                billDialog.dismiss();
            }
        });
        billDialog.show();

        Window window = billDialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private void updateBill1(ManualBillModel manualBillModel) {

        //writeData(sku_name, skuCode, storeSKU, mrp, TAXcode, TAXcode, eanCode);
        Log.w(TAG, "TAX CODE #### " + manualBillModel.getTaxCode());
        Log.w(TAG, "TAX Rate #### " + manualBillModel.getTaxRate());
        Log.w(TAG, "Product Name #### " + manualBillModel.getProductName());
        Log.w(TAG, "Sku Code #### " + manualBillModel.getSkuCode());
        Log.w(TAG, "Sku LocNo #### " + manualBillModel.getStoreSkuLocNo());
        Log.w(TAG, "MRP #### " + manualBillModel.getMrp());

        writeData(manualBillModel.getProductName(), manualBillModel.getSkuCode(),
                manualBillModel.getStoreSkuLocNo(), manualBillModel.getMrp(), manualBillModel.getTaxCode(),
                manualBillModel.getTaxRate(), manualBillModel.getEanCode(), manualBillModel.getQty(), manualBillModel.getReasonCode());

        manualBillModelList.add(manualBillModel);
        rvManualBill.setAdapter(manualBillAdapter);
        manualBillAdapter.notifyDataSetChanged();

        llTotal.setVisibility(View.VISIBLE);
        llBottomView.setVisibility(View.VISIBLE);
        etSkuCode.setText(null);

    }

    private void updateBill(ManualBillModel manualBill) {

        Log.w(TAG, "TAX CODE #### " + manualBill.getTaxCode());
        Log.w(TAG, "TAX Rate #### " + manualBill.getTaxRate());
        Log.w(TAG, "Product Name #### " + manualBill.getProductName());
        Log.w(TAG, "Sku Code #### " + manualBill.getSkuCode());
        Log.w(TAG, "Sku LocNo #### " + manualBill.getStoreSkuLocNo());
        Log.w(TAG, "MRP #### " + manualBill.getMrp());

        boolean Found = false;
        List<ManualBillModel> manualBillModelList = manualBillAdapter.getManualBillModelList();
        for (int i = 0; i < manualBillAdapter.getItemCount(); i++) {

            ManualBillModel manualBillModel1 = manualBillModelList.get(i);
            Log.w(TAG, "SKU Code " + manualBillModel1.getSkuCode());
            Log.w(TAG, "MRP " + manualBillModel1.getMrp());

            if (manualBill.getSkuCode().equalsIgnoreCase(manualBillModel1.getSkuCode()) &&
                    manualBill.getMrp().equalsIgnoreCase(manualBillModel1.getMrp())) {

                int qty = Integer.parseInt(manualBillModel1.getQty()) + Integer.parseInt(manualBill.getQty());
                manualBillModel1.setQty(String.valueOf(qty));

                writeData(manualBillModel1.getProductName(), manualBillModel1.getSkuCode(),
                        manualBillModel1.getStoreSkuLocNo(), manualBillModel1.getMrp(), manualBillModel1.getTaxCode(),
                        manualBillModel1.getTaxRate(), manualBillModel1.getEanCode(), manualBillModel1.getQty(),
                        manualBillModel1.getReasonCode());
                manualBillAdapter.notifyDataSetChanged();

                Found = true;
                break;
            }
        }
        if (!Found) {
            writeData(manualBill.getProductName(), manualBill.getSkuCode(),
                    manualBill.getStoreSkuLocNo(), manualBill.getMrp(), manualBill.getTaxCode(),
                    manualBill.getTaxRate(), manualBill.getEanCode(), manualBill.getQty(), manualBill.getReasonCode());

            manualBillModelList.add(manualBill);
            rvManualBill.setAdapter(manualBillAdapter);
            manualBillAdapter.notifyDataSetChanged();

        }
        llTotal.setVisibility(View.VISIBLE);
        llBottomView.setVisibility(View.VISIBLE);
        etSkuCode.setText(null);
    }

    String urlPath = "", statusCode, mrp_ind, qty, sku_name, skuCode, TAXcode, TAXrate, eanCode, Msg, storeSKU, mrp;
    List<MrpList> mrp_list = new ArrayList<>();
    List<String> mrp_spnList;
    JSONArray jsonArrayProduct, jsonArray;

    private void searchEan() {

        eanCode = skuCode = sku_name = mrp = storeSKU = TAXcode = TAXrate = "";
        mrp_list.clear();

        String urlPath = PATH + "manualgeteandetail?ean_code=" + etSkuCode.getText().toString() + "&location=" + Location;
        Log.w(TAG, "URL PAth " + urlPath);

        ApiCall.make(this, urlPath, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "Api Call response: " + response);
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            statusCode = responseObject.getString("statusCode");
                            Log.e(TAG, "Status Code ====> " + statusCode);

                            if (statusCode.equalsIgnoreCase("200")) {

                                jsonArrayProduct = responseObject.getJSONArray("product");
                                JSONObject json_product;
                                json_product = jsonArrayProduct.getJSONObject(0);
                                qty = "1";
                                sku_name = json_product.getString("SKU_NAME");
                                skuCode = json_product.getString("SKU_CODE");
                                eanCode = json_product.getString("ean_code");

                            } else {

                                Log.d(TAG, "Else Statement");
                                Log.d(TAG, "Status Code ==> " + statusCode);
                                Msg = responseObject.getString("Message");
                            }

                            if (skuCode.isEmpty() && etSkuCode.getText().toString().trim().length() < 7)
                                showQtyMrpDialog(etSkuCode.getText().toString().trim());
                            else
                                showQtyMrpDialog(skuCode);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        showVolleyError(error);
                        showQtyMrpDialog(etSkuCode.getText().toString().trim());
                    }
                }
        );
    }

    ArrayList<ProductList> skuList;

    private void processManualBill(String mRequestBody) {
        RequestQueue requestQueue = AppController.getInstance().getRequestQueue();

        url = PATH + "manualbilldetail";
        Log.w(TAG, "Process Manual Billing URl " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    dismissDialog();
                    Log.w(TAG, "Manual Bill REsponse message " + response);
                    JSONObject Jsonobj = new JSONObject(response);
                    String status = Jsonobj.getString("statusCode");

                    if (status.equalsIgnoreCase("200")) {
                        Log.d("Bill_log API response", Jsonobj.toString());

                        JSONArray refershLog = Jsonobj.getJSONArray("refershLog");

                        /*
                        Reason for creating separate JSONArray
                        Coming back to manual book from process activity, require reason name which I'm fetching frm
                        and storing in storeSkuLocStockNo, since reason code and reason name are not returned by API
                         */
                        JSONArray jsonArray = new JSONArray();
                        for (int i = 0; i < refershLog.length(); i++) {

                            JSONObject jObj = refershLog.getJSONObject(i);
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("skuCode", jObj.getString("skuCode"));
                            jsonObject.put("skuName", jObj.getString("skuName"));
                            jsonObject.put("skuQty", jObj.getString("skuQty"));
                            jsonObject.put("mrp", jObj.getString("mrp"));
                            jsonObject.put("totalAmt", jObj.getString("totalAmt"));
                            jsonObject.put("storeSkuLocStockNo", manualBillAdapter.
                                    getManualBill(jObj.getString("skuCode")).getReasonName());//storeSkuLocStockNo - to store reasonname
                            jsonObject.put("sellValue", jObj.getString("sellValue"));
                            jsonObject.put("unitSellValue", jObj.getString("unitSellValue"));
                            jsonObject.put("eanCode", jObj.getString("eanCode"));

                            jsonArray.put(jsonObject);
                        }
                        ProductDetailsDB podb = new ProductDetailsDB(getApplicationContext());
                        podb.open();
                        podb.deleteUserTable();
                        podb.createProductDetailsTable();
                        podb.insertBulkManualSKUDetails(jsonArray);
                        podb.close();

                        UserDB userdb = new UserDB(getApplicationContext());
                        userdb.open();
                        ArrayList<UserDetails> userdata = userdb.getUserDetails();
                        userdb.close();

                        editor = sp.edit();
                        editor.putString("Bill_no", Jsonobj.getString("billNo"));
                        editor.putString("Total_Amt", Jsonobj.getString("billValue"));
                        editor.putString("Location_code", userdata.get(0).getStoreID());
                        editor.putString("Cashier_code", userdata.get(0).getUserID());
                        editor.putString("Loyalty_Disc", "0");
                        editor.putString("Disc_Amt", "0");
                        editor.putString("bbPromo", "0");
                        editor.putString("Promo_Txt", "");
                        editor.putString("Coupon_Disc", "0.00");
                        editor.apply();

                        Intent intent = new Intent(ManualBillBookActivity.this, ProcessActivity.class);
                        intent.putExtra(Constants.FROM, Constants.MANUAL_BILL);
                        startActivity(intent);
                        finish();

                        /*ProductDetailsDB prdctDB = new ProductDetailsDB(getApplicationContext());
                        prdctDB.open();
                        prdctDB.deleteUserTable();
                        prdctDB.close();*/

                    } else {
                        showFailedAlert(Jsonobj.getString("Message"));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    dismissDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dismissDialog();
                showVolleyError(error);
            }
        }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                    return null;
                }
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(stringRequest);
    }

    private void getReasons() {

        String urlPath = PATH + "manualreasonmaster";
        Log.w(TAG, "ReasonMaster URL PAth " + urlPath);

        ApiCall.make(this, urlPath, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "Api Call response: " + response);
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            statusCode = responseObject.getString("statusCode");
                            Log.e(TAG, "Status Code ====> " + statusCode);

                            productBaseReasonMap.clear(); //TYPE_CODE = 1
                            overAllBillReasonMap.clear(); //TYPE_CODE = 2

                            if (statusCode.equalsIgnoreCase("200")) {
                                JSONArray jsonArray = responseObject.getJSONArray("reason");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    if (jsonObject.getString("typeCode").equalsIgnoreCase("1"))
                                        productBaseReasonMap.put(jsonObject.getString("reasonName"),
                                                jsonObject.getString("reasonCode"));
                                    else
                                        overAllBillReasonMap.put(jsonObject.getString("reasonName"),
                                                jsonObject.getString("reasonCode"));
                                }

                                List<String> billReason = new ArrayList<>(overAllBillReasonMap.keySet());
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ManualBillBookActivity.this,
                                        R.layout.custom_spinner_items, billReason);
                                spBillReason.setAdapter(adapter);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        showVolleyError(error);
                        showQtyMrpDialog(etSkuCode.getText().toString().trim());
                    }
                }
        );
    }

    public void writeData(String skuName, String skuCode, String storeSKU, String mrp, String taxCode, String taxRate, String eanCode, String qty, String reasonCode) {

        try {
            HashMap<String, String> ProductDetails = new HashMap<String, String>();
            ProductDetails.put("skuName", skuName);
            ProductDetails.put("skuCode", skuCode);
            ProductDetails.put("storeSKU", reasonCode);
            ProductDetails.put("Qty", qty);
            ProductDetails.put("Mrp", mrp);
            ProductDetails.put("taxCode", taxCode);
            ProductDetails.put("taxRate", taxRate);
            ProductDetails.put("eanCode", eanCode);

            Log.w(TAG, "SKU Code <<::>> " + skuCode + " MRP <<::>> " + mrp);

            ProductDetailsDB podb = new ProductDetailsDB(getApplicationContext());
            podb.open();
            ArrayList<ProductList> poList = podb.checkProductListManual(skuCode, mrp);
            if (poList.size() > 0) {
                podb.updateProductList(ProductDetails);
            } else {
                podb.insertProductDetails(ProductDetails);
            }

            podb.close();
            Toast toast = Toast.makeText(ManualBillBookActivity.this, "Item added successfully", Toast.LENGTH_SHORT);
            toast.show();
            etSkuCode.requestFocus();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTotalAmount(double amt) {

        float totalAmt = 0;
        for (int i = 0; i < manualBillAdapter.getItemCount(); i++) {
            float total = Float.parseFloat(manualBillAdapter.getManualBillModelList().get(i).getQty()) *
                    Float.parseFloat(manualBillAdapter.getManualBillModelList().get(i).getMrp());

            String s = String.format("%.2f", total);
            float t1 = Float.parseFloat(s);
            String totAmt = String.format("%.2f", totalAmt);
            totalAmt = Float.parseFloat(totAmt);
            totalAmt = totalAmt + t1;

            Log.w(TAG, "Total " + s);
            Log.w(TAG, "Total " + i + " <<<::>>> " + totalAmt);
        }

        Log.w(TAG, "Total Amount ====> " + totalAmt);

        tvTotalAmount.setText(String.format("%.2f", totalAmt));
    }

    @Override
    public void setLayoutVisible(boolean visible) {
        llTotal.setVisibility(visible ? View.VISIBLE : View.GONE);
        llBottomView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private boolean dialogValidation() {

        if (DlgEtSkuCode.getText().toString().trim().isEmpty()) {
            showToastMessage("Enter Sku Code");
            return false;
        } else if (DlgEtProductName.getText().toString().trim().isEmpty()) {
            showToastMessage("Enter Product Name");
            return false;
        } else if (DlgEtQty.getText().toString().trim().isEmpty()) {
            showToastMessage("Enter Product Qty");
            return false;
        } else if (Integer.parseInt(DlgEtQty.getText().toString()) == 0) {
            showToastMessage("Product Qty cannot be zero");
            return false;
        } else if (DlgEtMrp.getText().toString().trim().isEmpty()) {
            showToastMessage("Enter Product Mrp");
            return false;
        } else if (Double.parseDouble(DlgEtMrp.getText().toString()) == 0) {
            showToastMessage("Product Mrp cannot be zero");
            return false;
        } else if (spReason.getSelectedItem().toString().isEmpty()) {
            showToastMessage("Reason cannot be Empty");
            return false;
        } else
            return true;

    }

    private boolean validate() {

        if (manualBillAdapter.getItemCount() == 0) {
            showFailedAlert("Add products for Manual Billing");
            return false;
        } /*else if (etReasonForManualUser.getText().toString().isEmpty()) {
            showFailedAlert("Enter Reason for Manual Bill");
            return false;
        }*/ /*else if (etCustomerName.getText().toString().trim().isEmpty()) {
            showFailedAlert("Enter Customer Name");
            return false;
        } else if (etMobileNumber.getText().toString().trim().isEmpty()) {
            showFailedAlert("Enter Customer Mobile Number");
            return false;
        } */

        else
            return true;
    }

    private void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showFailedAlert(final String msg) {

        this.runOnUiThread(new Runnable() {
            public void run() {

                if (!(ManualBillBookActivity.this).isFinishing()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ManualBillBookActivity.this);
                    builder.setTitle("HnG POS");
                    builder.setMessage(msg)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });
    }

    private void showVolleyError(VolleyError error) {
        try {
            if (error instanceof TimeoutError) {
                showFailedAlert("Time out error occurred.Please click on OK and try again");
                Log.e(TAG, "Time out error occurred.");

            } else if (error instanceof NoConnectionError) {
                showFailedAlert("Network error occurred.Please click on OK and try again");
                Log.e(TAG, "Network error occurred.");

            } else if (error instanceof AuthFailureError) {
                showFailedAlert("Authentication error occurred.Please click on OK and try again");
                Log.e(TAG, "Authentication error occurred.");

            } else if (error instanceof ServerError) {
                showFailedAlert("Server error occurred.Please click on OK and try again");
                Log.e(TAG, "Server error occurred.");

            } else if (error instanceof NetworkError) {
                showFailedAlert("Network error occurred.Please click on OK and try again");
                Log.e(TAG, "Network error occurred.");

            } else if (error instanceof ParseError) {

                showFailedAlert("An error occurred.Please click on OK and try again");
                Log.e(TAG, "An error occurred.");
            } else {

                showFailedAlert("An error occurred.Please click on OK and try again");
                Log.e(TAG, "An error occurred.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dismissDialog() {
        try {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideKeyboard() {

        View view = ManualBillBookActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private ArrayList<ProductList> readFinalData() {
        ProductDetailsDB podb = new ProductDetailsDB(getApplicationContext());
        podb.open();
        ArrayList<ProductList> skuList = podb.getEANDetails();

        podb.close();

        return skuList;
    }

}
