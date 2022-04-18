package in.hng.mpos.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.hng.mpos.R;
import in.hng.mpos.activity.RegularizeManualBillActivity;
import in.hng.mpos.gettersetter.RegularizeManualBill.Detail;
import in.hng.mpos.helper.AppConstants;
import in.hng.mpos.helper.Log;

public class RegularizeManualBillAdapter extends RecyclerView.Adapter<RegularizeManualBillAdapter.ItemViewHolder> {

    private static final String TAG = RegularizeManualBillAdapter.class.getSimpleName();

    private List<Detail> tempList = new ArrayList<>();
    private Context context;

    public RegularizeManualBillAdapter(Context context, List<Detail> tempList) {
        this.context = context;
        this.tempList = tempList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.regularize_manual_bill_item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, final int position) {

        Detail detail = tempList.get(holder.getAdapterPosition());

        holder.tvBillNo.setText(detail.getBillNo());
        holder.customerName.setText(tempList.get(position).getCustomerName());
        holder.customerPhone.setText(tempList.get(position).getCustomerPhone());
        holder.llActualBillNo.setVisibility(detail.getActualBillNo().isEmpty() ? View.GONE : View.VISIBLE);

        if (detail.getActualBillNo().isEmpty()) {
            holder.tvBillStatus.setText("Pending");
            holder.tvBillStatus.setTextColor(Color.parseColor("#FF0000"));
        } else {
            holder.tvActualBillNo.setText(detail.getActualBillNo());
            holder.tvBillStatus.setText("Completed");
            holder.tvBillStatus.setTextColor(Color.parseColor("#008000"));
        }

        holder.bottomview.setVisibility(holder.getAdapterPosition() == tempList.size() - 1 ? View.GONE : View.VISIBLE);

        holder.llManualBillLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(TAG, "Bill No Clicked <::>> " + tempList.get(position).getBillNo());

                Intent intent = new Intent(context, RegularizeManualBillActivity.class);
                intent.putExtra(AppConstants.BILL_NO, detail.getBillNo());
                intent.putExtra(AppConstants.BILL_DATE, detail.getBillDate());
                intent.putExtra(AppConstants.LOCATION_CODE, detail.getLocationCode());
                intent.putExtra(AppConstants.CASHIER_ID, detail.getCashierUserCode());
                intent.putExtra(AppConstants.TOTAL_BILL, detail.getBillValue());
                intent.putExtra(AppConstants.ACTUAL_BILL_NO, detail.getActualBillNo());
                intent.putExtra(AppConstants.CUSTOMER_NAME, detail.getCustomerName());
                intent.putExtra(AppConstants.CUSTOMER_PHONE, detail.getCustomerPhone());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tempList.size();
    }

    public void clearList(){
        tempList.clear();
        notifyDataSetChanged();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView tvBillNo, tvBillStatus, tvActualBillNo,customerName,customerPhone;
        LinearLayout llManualBillLayout, llActualBillNo;
        View bottomview;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBillNo = itemView.findViewById(R.id.tvBillNo);
            tvBillStatus = itemView.findViewById(R.id.tvStatus);
            tvActualBillNo = itemView.findViewById(R.id.tvActualBillNo);
            llManualBillLayout = itemView.findViewById(R.id.llManualBillLayout);
            llActualBillNo = itemView.findViewById(R.id.llActualBillNo);
            customerName = itemView.findViewById(R.id.customer_name);
            customerPhone = itemView.findViewById(R.id.customer_phone);
            bottomview = itemView.findViewById(R.id.bottomview);
        }
    }
}
