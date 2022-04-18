package in.hng.mpos.Adapter;

/**
 * Created by Cbly on 29-Jan-18.
 */

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import in.hng.mpos.R;
import in.hng.mpos.gettersetter.LoyaltyDetails;
import in.hng.mpos.gettersetter.OfferList;

/**
 * Created by Cbly on 29-Jan-18.
 */


public class SelectOfferListAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<LoyaltyDetails> list;
    private LayoutInflater inflater;
    ViewHolder viewHolder;


    public SelectOfferListAdapter(Context context, ArrayList<LoyaltyDetails> list) {

        this.mContext=context;
        this.list=list;
        inflater = LayoutInflater.from(this.mContext);

    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    private class ViewHolder {
        TextView txtoffers;
    }


    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (convertView == null) {
            view = inflater.inflate(R.layout.offer_list, null);
            viewHolder = new ViewHolder();
            viewHolder.txtoffers= view.findViewById(R.id.txtoffers);
            viewHolder.txtoffers.setBackgroundColor(Color.TRANSPARENT);
            view.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.txtoffers.setBackgroundColor(Color.TRANSPARENT);

        if(list.size() > 0) {
            viewHolder.txtoffers.setText(list.get(position).getOfferDESC());
            //viewHolder.txtoffers.setText(list.get(position).toString());
        }
       // #ffcf93
       viewHolder.txtoffers.setBackgroundColor(selectedOffers.contains(list.get(position).getCustomerOfferID()) ? Color.parseColor("#ffcf93") : Color.TRANSPARENT);
        return view;
    }
    private static ArrayList<String> selectedOffers = new ArrayList<String>();

    public static ArrayList getArrayList()
    {
        return selectedOffers;
    }

}










