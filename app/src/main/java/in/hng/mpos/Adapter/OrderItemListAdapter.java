package in.hng.mpos.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import in.hng.mpos.Database.OrderedProductDetailsDB;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.ProductList;

public class OrderItemListAdapter extends BaseAdapter {


    Context mContext;
    ArrayList<in.hng.mpos.gettersetter.ProductList> ProductList;
    private LayoutInflater inflater;
    OrderItemListAdapter.ViewHolder viewHolder;

    public OrderItemListAdapter(Context context, ArrayList<ProductList> ProductList) {

        this.mContext = context;
        this.ProductList = ProductList;
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
        TextView sku, qty, mrp, scan_qty, tot_amt;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {

            view = inflater.inflate(R.layout.order_list_product, null);
            viewHolder = new OrderItemListAdapter.ViewHolder();
            viewHolder.sku = view.findViewById(R.id.txtSkuName);
            viewHolder.qty = view.findViewById(R.id.txtqty);
            viewHolder.mrp = view.findViewById(R.id.txtmrp);
            viewHolder.scan_qty = view.findViewById(R.id.txtscanqty);
            viewHolder.tot_amt = view.findViewById(R.id.txttotmrp);

            view.setTag(viewHolder);

        } else {
            viewHolder = (OrderItemListAdapter.ViewHolder) view.getTag();
        }

        OrderedProductDetailsDB podb = new OrderedProductDetailsDB(this.mContext);
        podb.open();
        boolean isProductPending = podb.isProductPending(ProductList.get(position).getSkuCode());
        if (isProductPending)
            view.setBackgroundColor(Color.RED);
        else
            view.setBackgroundColor(Color.GREEN);

        viewHolder.sku.setText(ProductList.get(position).getSkuName());
        viewHolder.qty.setText(ProductList.get(position).getQty());
        viewHolder.mrp.setText(ProductList.get(position).getMrp());
        viewHolder.scan_qty.setText(ProductList.get(position).getOrderQty());
        viewHolder.tot_amt.setText(ProductList.get(position).getTotalAmt());


        return view;
    }
}
