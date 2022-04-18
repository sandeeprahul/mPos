package in.hng.mpos.Adapter;

/**
 * Created by Cbly on 29-Jan-18.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import in.hng.mpos.R;
import in.hng.mpos.gettersetter.ProductList;

/**
 * Created by Cbly on 29-Jan-18.
 */

public class ListAdapter extends BaseAdapter {


    Context mContext;
    ArrayList<ProductList> ProductList;
    private LayoutInflater inflater;
    ViewHolder viewHolder;
    public ListAdapter(Context context, ArrayList<ProductList> ProductList) {

        this.mContext=context;
        this.ProductList=ProductList;
        inflater = LayoutInflater.from(this.mContext);

    }

    @Override
    public int getCount() {
        return ProductList.size();
    }

    @Override
    public Object getItem(int position) {

        return ProductList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        TextView sku,qty,mrp,disc_mrp,tot_amt;

    }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {

                view = inflater.inflate(R.layout.product_list, null);
                viewHolder = new ViewHolder();
                viewHolder.sku = view.findViewById(R.id.txtSkuName);
                viewHolder.qty = view.findViewById(R.id.txtqty);
                viewHolder.mrp = view.findViewById(R.id.txtmrp);
                viewHolder.disc_mrp = view.findViewById(R.id.txtdiscmrp);
                viewHolder.tot_amt = view.findViewById(R.id.txttotmrp);

                view.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.sku.setText(ProductList.get(position).getSkuName());
            viewHolder.qty.setText(ProductList.get(position).getQty());
            viewHolder.mrp.setText(ProductList.get(position).getMrp());
            viewHolder.disc_mrp.setText(ProductList.get(position).getDiscMRP());
            viewHolder.tot_amt.setText(ProductList.get(position).getTotalAmt());


            return view;
        }
    }
