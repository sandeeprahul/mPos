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

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {


    ArrayList<ProductInfo> data;
    Context ctx;
    int Limit;
    int Selected;

    public void setLimit(int limit) {
        Limit = limit;
    }

    public ProductListAdapter(ArrayList<ProductInfo> data, int Limit, Context ctx) {
        this.data = data;
        this.ctx = ctx;
        this.Limit=Limit;
        Selected=0;
    }

    public ArrayList<ProductInfo> getData() {
        return data;
    }

    @NonNull
    @Override
    public ProductListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View listItem= layoutInflater.inflate(R.layout.rv_product_card, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ProductListAdapter.ViewHolder viewHolder, int i) {

        viewHolder.code.setText(data.get(i).code);
        viewHolder.name.setText(data.get(i).Name);
        viewHolder.isSelected.setChecked(data.get(i).IsChecked);
        viewHolder.isSelected.setClickable(false);
                //        viewHolder.isSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//              if(b){
//
//                  if(Selected<=2){
//                      Selected=Selected+1;
//                      Toast.makeText(ctx,Selected+"",Toast.LENGTH_SHORT);
//                      data.get(viewHolder.getAdapterPosition()).IsChecked= b;
//                  }else {
//                      compoundButton.setChecked(false);
//                      Toast.makeText(ctx,"You Cannot Select More than 2 items",Toast.LENGTH_SHORT);
//                  }
//
//              }else {
//                  Selected= Selected-1;
//
//              }
//
//
//
//            }
//        });
        viewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( data.get(viewHolder.getAdapterPosition()).IsChecked){
                    data.get(viewHolder.getAdapterPosition()).IsChecked= false;
                    viewHolder.isSelected.setChecked(false);
                    Selected= Selected-1;
                }else {
                    if(Selected<Limit){
                        data.get(viewHolder.getAdapterPosition()).IsChecked= true;
                        viewHolder.isSelected.setChecked(true);
                        Selected= Selected+1;
                    }else {
                        Toast.makeText(view.getContext(),"You Cannot Select More than "+Limit+" items",Toast.LENGTH_SHORT).show();
                    }

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
