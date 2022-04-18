package in.hng.mpos.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import in.hng.mpos.R;
import in.hng.mpos.gettersetter.ProductInfo;

public class ServiceListAdapter  extends RecyclerView.Adapter<ServiceListAdapter.ViewHolder> {


    ArrayList<ProductInfo> data;
    Context ctx;

    public ArrayList<ProductInfo> getData() {
        return data;
    }

    public ServiceListAdapter(ArrayList<ProductInfo> data, Context ctx) {
        this.data = data;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public ServiceListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View listItem= layoutInflater.inflate(R.layout.rv_product_card, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ServiceListAdapter.ViewHolder viewHolder, int i) {

        viewHolder.code.setText(data.get(i).code);
        viewHolder.name.setText(data.get(i).Name);
        viewHolder.isSelected.setChecked(data.get(i).IsChecked);
       viewHolder.isSelected.setClickable(false);
        viewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( data.get(viewHolder.getAdapterPosition()).IsChecked){
                    data.get(viewHolder.getAdapterPosition()).IsChecked= false;
                    viewHolder.isSelected.setChecked(false);
                }else {
                    data.get(viewHolder.getAdapterPosition()).IsChecked= true;
                    viewHolder.isSelected.setChecked(true);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView code,name;
        public CheckBox isSelected;
        public LinearLayout layout;

        public ViewHolder(final View itemView) {
            super(itemView);

            isSelected= itemView.findViewById(R.id.rv_select);
            code= itemView.findViewById(R.id.rv_tv_code);
            name= itemView.findViewById(R.id.rv_tv_name);
            layout=itemView.findViewById(R.id.rv_layout);


        }
    }
}
