package in.hng.mpos.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import in.hng.mpos.R;
import in.hng.mpos.gettersetter.RegularizeManualBill.BillDetail;
import in.hng.mpos.gettersetter.RegularizeManualBill.Detail;

public class RegManualBillDataAdapter extends RecyclerView.Adapter<RegManualBillDataAdapter.ItemViewHolder> {

    private static final String TAG = RegManualBillDataAdapter.class.getSimpleName();

    private List<BillDetail> detailList = new ArrayList<>();

    public RegManualBillDataAdapter(List<BillDetail> detailList) {
        this.detailList = detailList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reg_manual_bill_item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {

        holder.tvSkuCode.setText(detailList.get(holder.getAdapterPosition()).getSkuCode());
        holder.tvItemName.setText(detailList.get(holder.getAdapterPosition()).getSkuName());
        holder.tvQty.setText(detailList.get(holder.getAdapterPosition()).getSkuQty());
        holder.tvMrp.setText(detailList.get(holder.getAdapterPosition()).getMrp());

        float hgDiscount = Float.parseFloat(detailList.get(holder.getAdapterPosition()).getSellValue()) -
                Float.parseFloat(detailList.get(holder.getAdapterPosition()).getSkuDiscount()) -
                Float.parseFloat(detailList.get(holder.getAdapterPosition()).getOtherDiscount());
        holder.tvHGamt.setText(String.valueOf(hgDiscount));

        holder.tvTotalAmount.setText(detailList.get(holder.getAdapterPosition()).getSellValue());
        holder.startView.setVisibility(View.VISIBLE);
        holder.endView.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return detailList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView tvSkuCode, tvItemName, tvQty, tvMrp, tvHGamt, tvTotalAmount;
        View startView, endView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSkuCode = itemView.findViewById(R.id.tvSkuCode);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvQty = itemView.findViewById(R.id.tvQty);
            tvMrp = itemView.findViewById(R.id.tvMrp);
            tvHGamt = itemView.findViewById(R.id.tvHGamt);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            startView = itemView.findViewById(R.id.startview);
            endView = itemView.findViewById(R.id.endview);

        }
    }
}
