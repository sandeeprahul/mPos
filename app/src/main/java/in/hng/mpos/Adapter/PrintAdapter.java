package in.hng.mpos.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import in.hng.mpos.R;
import in.hng.mpos.gettersetter.BillPrintDetailsPojo.Bill_detail;

public class PrintAdapter extends RecyclerView.Adapter<PrintAdapter.ViewHolder> {


    ArrayList<Bill_detail> data;
    Context ctx;

    public ArrayList<Bill_detail> getData() {
        return data;
    }

    public PrintAdapter(ArrayList<Bill_detail> data, Context ctx) {
        this.data = data;
        this.ctx = ctx;
    }

    public void updateData(ArrayList<Bill_detail> viewModels) {
        data.clear();
        data.addAll(viewModels);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PrintAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View listItem = layoutInflater.inflate(R.layout.card_printtemp, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        holder.Name.setText(data.get(i).getSkuname());
        holder.Qty.setText(data.get(i).getQty());
        holder.MRP.setText(data.get(i).getMrp());
        holder.HGPrice.setText(data.get(i).getHgprice());
        holder.NET.setText(data.get(i).getNetPrice());
        holder.HSN.setText(data.get(i).getHsncode());
        holder.Tax.setText(data.get(i).getTaxvalue());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView Name, Qty, MRP, HGPrice, NET, HSN, Tax;

        public ViewHolder(final View itemView) {
            super(itemView);
            Name = itemView.findViewById(R.id.tv_print_name);
            Qty = itemView.findViewById(R.id.tv_print_qty);
            MRP = itemView.findViewById(R.id.tv_print_mrp);
            HGPrice = itemView.findViewById(R.id.tv_print_hgprice);
            NET = itemView.findViewById(R.id.tv_print_net);
            HSN = itemView.findViewById(R.id.tv_card_hsn);
            Tax = itemView.findViewById(R.id.tv_card_tax);


        }
    }
}
