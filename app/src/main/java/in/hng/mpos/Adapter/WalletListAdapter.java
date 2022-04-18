package in.hng.mpos.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import in.hng.mpos.R;
import in.hng.mpos.gettersetter.WalletDetails;

public class WalletListAdapter extends BaseAdapter {


    Context mContext;
    ArrayList<WalletDetails> walletDetails;
    private LayoutInflater inflater;
    ViewHolder viewHolder;
    public WalletListAdapter(Context context, ArrayList<WalletDetails> walletDetails) {

        this.mContext=context;
        this.walletDetails=walletDetails;
        inflater = LayoutInflater.from(this.mContext);

    }

    @Override
    public int getCount() {
        return walletDetails.size();
    }

    @Override
    public Object getItem(int position) {

        return walletDetails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        TextView ID,Name,Amt;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {

            view = inflater.inflate(R.layout.wallet_list, null);
            viewHolder = new ViewHolder();
            viewHolder.ID = view.findViewById(R.id.txtwalletID);
            viewHolder.Name = view.findViewById(R.id.txtwalletName);
            viewHolder.Amt = view.findViewById(R.id.txtwalletAmount);


            view.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.ID.setText(walletDetails.get(position).getWalletID());
        viewHolder.Name.setText(walletDetails.get(position).getWalletName());
        viewHolder.Amt.setText(walletDetails.get(position).getWalletAmount());


        return view;
    }
}
