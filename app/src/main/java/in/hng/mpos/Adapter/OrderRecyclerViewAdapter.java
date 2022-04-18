package in.hng.mpos.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.hng.mpos.R;
import in.hng.mpos.gettersetter.OrderInfo;

public class OrderRecyclerViewAdapter extends RecyclerView
        .Adapter<OrderRecyclerViewAdapter
        .DataObjectHolder> {
    private static String LOG_TAG = "MyRecyclerViewAdapter";
    public List<OrderInfo> orders;
    private static OrderRecyclerViewAdapter.MyClickListener myClickListener;


    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        public TextView orderID, orderDate, custName, custMobile, delvType;
        public LinearLayout parentLayout;

        public DataObjectHolder(View itemView) {
            super(itemView);
            orderID = (TextView) itemView.findViewById(R.id.txtorderID);
            orderDate = (TextView) itemView.findViewById(R.id.txtordrDate);
            custName = (TextView) itemView.findViewById(R.id.txtcustName);
            custMobile = (TextView) itemView.findViewById(R.id.txtcustMobile);
            delvType = (TextView) itemView.findViewById(R.id.txtdeliveryType);

            parentLayout = (LinearLayout) itemView.findViewById(R.id.parent_layout);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(OrderRecyclerViewAdapter.MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public OrderRecyclerViewAdapter(Context context, List<OrderInfo> orders) {
        this.orders = orders;
    }

    @Override
    public OrderRecyclerViewAdapter.DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                                                         int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.layout_order_details, parent, false);
        return new OrderRecyclerViewAdapter.DataObjectHolder(itemView);
    }

    @Override
    public void onBindViewHolder(OrderRecyclerViewAdapter.DataObjectHolder holder, int position) {
        holder.orderID.setText("Order ID : "+ orders.get(position).getOrderID());
        holder.orderDate.setText("Oder Date : " +orders.get(position).getOrderDate());
        holder.custName.setText("Customer Name : " +orders.get(position).getCustName());
        holder.custMobile.setText("Customer Mobile : " +orders.get(position).getCustMobile());
        holder.delvType.setText("Delivery Type : " +orders.get(position).getDelType());
        /*
        //holder.subname.setText(courses.get(position).getSubname());
        //holder.startDate.setText("Start Date: " + courses.get(position).getStartDate().substring(0, courses.get(position).getStartDate().indexOf(" ")));
        //holder.endDate.setText("End Date: " + courses.get(position).getEndDate().substring(0, courses.get(position).getEndDate().indexOf(" ")));
        if (courses.get(position).getStatus().equalsIgnoreCase("Y"))
            holder.status.setVisibility(View.VISIBLE);
        else
            holder.status.setVisibility(View.INVISIBLE);
            */
    }


    @Override
    public int getItemCount() {
        return orders.size();
    }

    public interface MyClickListener {
        public void onItemClick(int position, View v);
    }
}