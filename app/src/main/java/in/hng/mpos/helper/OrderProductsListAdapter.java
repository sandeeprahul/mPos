package in.hng.mpos.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.SearchProduct;

public class OrderProductsListAdapter extends BaseAdapter {


    Context mContext;
    ArrayList<SearchProduct> ProductList;
    private LayoutInflater inflater;
    OrderProductsListAdapter.ViewHolder viewHolder;

    public OrderProductsListAdapter(Context context, ArrayList<SearchProduct> ProductList) {

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
        TextView code,name,qty;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {

            view = inflater.inflate(R.layout.order_product_list, null);
            viewHolder = new OrderProductsListAdapter.ViewHolder();
            viewHolder.code = view.findViewById(R.id.txtSkuCode);
            viewHolder.name = view.findViewById(R.id.txtSkuName);
            viewHolder.qty = view.findViewById(R.id.txtqty);


            view.setTag(viewHolder);

        } else {
            viewHolder = (OrderProductsListAdapter.ViewHolder) view.getTag();
        }

        viewHolder.code.setText(ProductList.get(position).getSkuCode());
        viewHolder.name.setText(ProductList.get(position).getSkuName());
        viewHolder.qty.setText(ProductList.get(position).getQty());



        return view;
    }
}

