package in.hng.mpos.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import in.hng.mpos.R;
import in.hng.mpos.gettersetter.LoyaltyDetails;

/**
 * Created by Cbly on 08-Mar-18.
 */

public class OfferListAdapter extends BaseAdapter {


    Context mContext;
    ArrayList<LoyaltyDetails> OfferList;
    private LayoutInflater inflater;
    OfferListAdapter.ViewHolder viewHolder;
    public OfferListAdapter(Context context, ArrayList<LoyaltyDetails> OfferList) {

        this.mContext=context;
        this.OfferList=OfferList;
        inflater = LayoutInflater.from(this.mContext);

    }



    @Override
    public int getCount() {
        return OfferList.size();
    }

    @Override
    public Object getItem(int position) {

        return OfferList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        TextView offr;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {

            view = inflater.inflate(R.layout.offer_list, null);
            viewHolder = new OfferListAdapter.ViewHolder();
            viewHolder.offr = view.findViewById(R.id.txtoffers);
            view.setTag(viewHolder);

        } else {
            viewHolder = (OfferListAdapter.ViewHolder) view.getTag();
        }

        viewHolder.offr.setText(OfferList.get(position).getOfferDESC());


        return view;
    }
}
