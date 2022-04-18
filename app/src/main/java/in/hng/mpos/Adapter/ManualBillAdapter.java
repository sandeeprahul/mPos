package in.hng.mpos.Adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.hng.mpos.Database.ProductDetailsDB;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.ManualBillModel;
import in.hng.mpos.gettersetter.ProductList;
import in.hng.mpos.helper.Log;
import in.hng.mpos.interfaces.UpdateTotal;

public class ManualBillAdapter extends RecyclerView.Adapter<ManualBillAdapter.ItemViewHolder> {

    private String TAG = ManualBillAdapter.class.getSimpleName();

    private List<ManualBillModel> manualBillModelList;
    private Context context;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private double totalAmount = 0;
    private UpdateTotal updateTotalListener;
    private Dialog billDialog;
    private HashMap<String, String> productReasonMap;

    public ManualBillAdapter(Context context, List<ManualBillModel> manualBillModelList, HashMap<String, String> productReason,
                             UpdateTotal updateTotalListener) {
        this.context = context;
        this.manualBillModelList = manualBillModelList;
        this.productReasonMap = productReason;
        this.updateTotalListener = updateTotalListener;
        billDialog = new Dialog(context);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.manual_bill_item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {

        ManualBillModel manualBillModel = manualBillModelList.get(holder.getAdapterPosition());

        if (holder.getAdapterPosition() != getItemCount()) {
            holder.tvSNo.setText(String.valueOf(holder.getAdapterPosition() + 1));
            float total = Float.parseFloat(manualBillModel.getQty()) * Float.parseFloat(manualBillModel.getMrp());
            holder.tvAmount.setText(String.format(String.valueOf(total), decimalFormat));
            totalAmount += total;
        } else
            holder.tvAmount.setText(String.format(String.valueOf(totalAmount), decimalFormat));

        holder.tvSNo.setText(String.valueOf(holder.getAdapterPosition() + 1));

        updateTotalListener.updateTotalAmount(totalAmount);

        holder.tvProductName.setText(manualBillModel.getProductName());
        holder.tvQty.setText(manualBillModel.getQty());
        holder.tvMrp.setText(manualBillModel.getMrp());

        holder.llFull_layout.setOnClickListener(view -> {
            if (!billDialog.isShowing())
                showQtyMrpDialog(manualBillModel, holder.getAdapterPosition());
        });
    }

    private void removeItem(int position) {
        manualBillModelList.remove(position);
        notifyDataSetChanged();
        if (getItemCount() == 0) {
            updateTotalListener.setLayoutVisible(false);
        }
        totalAmount = 0;
    }

    private void showQtyMrpDialog(ManualBillModel manualBillModel, int position) {

        billDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        billDialog.setContentView(R.layout.manual_bill_mrp_qty_dialog);

        final EditText EtSkuCode = billDialog.findViewById(R.id.etSkuCode);
        final EditText EtProductName = billDialog.findViewById(R.id.etProductName);
        final EditText EtQty = billDialog.findViewById(R.id.dlgEtQty);
        final EditText EtMrp = billDialog.findViewById(R.id.dlgEtMrp);
        final Button DlgbtnAdd = billDialog.findViewById(R.id.btn_dlg_add);
        final Button DlgbtnCancel = billDialog.findViewById(R.id.btn_dlg_cancel);
        final Button DlgbtnRemove = billDialog.findViewById(R.id.btn_dlg_remove);
        Spinner mySpinner = billDialog.findViewById(R.id.SpnrMrp);
        mySpinner.setVisibility(View.GONE);
        EtMrp.setVisibility(View.VISIBLE);

        Spinner spReason = billDialog.findViewById(R.id.spReason);
        List<String> productManualReason = new ArrayList<>(productReasonMap.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.custom_spinner_items, productManualReason);
        spReason.setAdapter(adapter);

        DlgbtnRemove.setVisibility(View.VISIBLE);
        DlgbtnAdd.setText("Modify");

        EtSkuCode.setText(manualBillModel.getSkuCode());
        EtProductName.setText(manualBillModel.getProductName());
        EtQty.setText(manualBillModel.getQty());
        EtMrp.setText(manualBillModel.getMrp());

        int spinnerIndex = productManualReason.indexOf(manualBillModel.getReasonName());
        Log.w(TAG, "Reason Spinner Index " + spinnerIndex);
        spReason.setSelection(spinnerIndex);

        DlgbtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Integer.parseInt(EtQty.getText().toString().trim()) > 0) {

                    writeData(manualBillModel.getProductName(), manualBillModel.getSkuCode(),
                            manualBillModel.getStoreSkuLocNo(), EtMrp.getText().toString().trim(), manualBillModel.getTaxCode(),
                            manualBillModel.getTaxRate(), manualBillModel.getEanCode(), EtQty.getText().toString().trim(),
                            productReasonMap.get(spReason.getSelectedItem().toString()));

                    manualBillModel.setSkuCode(EtSkuCode.getText().toString().trim());
                    manualBillModel.setProductName(EtProductName.getText().toString().trim());
                    manualBillModel.setQty(EtQty.getText().toString().trim());
                    manualBillModel.setMrp(EtMrp.getText().toString().trim());
                    manualBillModel.setReasonCode(productReasonMap.get(spReason.getSelectedItem().toString()));
                    manualBillModel.setReasonName(spReason.getSelectedItem().toString());
                    //notifyItemChanged(position);

                } else {
                    removeProduct(position, manualBillModel.getSkuCode(), manualBillModel.getStoreSkuLocNo(), manualBillModel.getMrp());
                }

                notifyDataSetChanged();
                billDialog.dismiss();
            }
        });

        DlgbtnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setMessage("Are you sure you want to remove this product?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                removeProduct(position, manualBillModel.getSkuCode(), manualBillModel.getStoreSkuLocNo(),
                                        manualBillModel.getMrp());
                                dialog.dismiss();
                                billDialog.dismiss();
                            }
                        }).setNegativeButton("No", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }).show();
            }
        });

        DlgbtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                billDialog.dismiss();
            }
        });
        billDialog.show();

        Window window = billDialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private void removeProduct(int pos, String skuCode, String skuLoc, String mrp) {
        ProductDetailsDB podb = new ProductDetailsDB(context);
        podb.open();
        podb.deleteProductManual(skuCode, mrp);
        podb.close();

        removeItem(pos);
    }

    @Override
    public int getItemCount() {
        return manualBillModelList.size();
    }

    public List<ManualBillModel> getManualBillModelList() {
        return manualBillModelList;
    }

    public ManualBillModel getManualBill(String skuCode) {

        ManualBillModel manualBillModel;
        for (int i = 0; i < manualBillModelList.size(); i++) {

            manualBillModel = manualBillModelList.get(i);
            if (manualBillModel.getSkuCode().equalsIgnoreCase(skuCode))
                return manualBillModel;
        }
        return null;
    }

    public void clearList() {
        manualBillModelList.clear();
        notifyDataSetChanged();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView tvSNo, tvProductName, tvQty, tvMrp, tvAmount;
        LinearLayout llFull_layout;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            llFull_layout = itemView.findViewById(R.id.ll_full_layout);
            tvSNo = itemView.findViewById(R.id.tvSNo);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQty = itemView.findViewById(R.id.tvQty);
            tvMrp = itemView.findViewById(R.id.tvMrp);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }

    private void writeData(String skuName, String skuCode, String storeSKU, String mrp, String taxCode, String taxRate, String eanCode, String qty, String reasonCode) {

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

            Log.w(TAG, "SKU Code <<::>> "+skuCode+" MRP <<::>> "+mrp);

            ProductDetailsDB podb = new ProductDetailsDB(context);
            podb.open();
            ArrayList<ProductList> poList = podb.checkProductListManual(skuCode, mrp);
            Log.w(TAG, "Product List Size " + poList);
            if (poList.size() > 0) {
                podb.updateProductList(ProductDetails);
            } else {
                podb.insertProductDetails(ProductDetails);
            }

            podb.close();
            Toast toast = Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT);
            toast.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
