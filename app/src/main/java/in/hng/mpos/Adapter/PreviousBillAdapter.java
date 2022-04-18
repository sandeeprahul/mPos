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
import in.hng.mpos.gettersetter.Details;

public class PreviousBillAdapter extends RecyclerView.Adapter<PreviousBillAdapter.ViewHolder> {


    ArrayList<Details> data;
    Context ctx;

    public ArrayList<Details> getData() {
        return data;
    }

    public PreviousBillAdapter(ArrayList<Details> data, Context ctx) {
        this.data = data;
        this.ctx = ctx;
    }

    public void updateData(ArrayList<Details> viewModels) {
        data.clear();
        data.addAll(viewModels);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PreviousBillAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View listItem = layoutInflater.inflate(R.layout.card_previousbill, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        holder.Billno.setText(data.get(i).getBill_no());
        holder.name.setText(data.get(i).getCustomer_name());
        holder.status.setText(data.get(i).getBill_status());

    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView Billno, name, status;

        public ViewHolder(final View itemView) {
            super(itemView);
            Billno = itemView.findViewById(R.id.tv_rv_billno);
            name = itemView.findViewById(R.id.tv_rv_name);
            status = itemView.findViewById(R.id.tv_rv_status);


        }
    }
}
