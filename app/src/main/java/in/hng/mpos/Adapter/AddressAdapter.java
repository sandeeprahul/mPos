package in.hng.mpos.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import in.hng.mpos.R;
import in.hng.mpos.gettersetter.AddressInfo;
import in.hng.mpos.gettersetter.ProductList;

public class AddressAdapter extends BaseAdapter {


    Context mContext;
    ArrayList<AddressInfo> AddrList;
    private LayoutInflater inflater;
    AddressAdapter.ViewHolder viewHolder;

    public AddressAdapter(Context context, ArrayList<AddressInfo> AddrList) {

        this.mContext=context;
        this.AddrList=AddrList;
        inflater = LayoutInflater.from(this.mContext);

    }

    @Override
    public int getCount() {
        return AddrList.size();
    }

    @Override
    public Object getItem(int position) {

        return AddrList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        TextView address,addrID,addrType;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {

            view = inflater.inflate(R.layout.address_list, null);
            viewHolder = new AddressAdapter.ViewHolder();
            viewHolder.address = view.findViewById(R.id.txtSkuName);
            viewHolder.addrID = view.findViewById(R.id.txtaddrID);
            viewHolder.addrType = view.findViewById(R.id.txtAddrType);


            view.setTag(viewHolder);

        } else {
            viewHolder = (AddressAdapter.ViewHolder) view.getTag();
        }

        viewHolder.address.setText(AddrList.get(position).getAddr());
        viewHolder.addrType.setText(AddrList.get(position).getAddrType());
        viewHolder.addrID.setText(AddrList.get(position).getAddrID());



        viewHolder.address.setBackgroundColor(selectedIds.contains(AddrList.get(position).getAddrID().toString()) ? Color.CYAN :Color.TRANSPARENT);






        return view;
    }

    private static ArrayList<Integer> selectedIds = new ArrayList<Integer>();
    public static ArrayList getArrayList()
    {
        return selectedIds;
    }
}
