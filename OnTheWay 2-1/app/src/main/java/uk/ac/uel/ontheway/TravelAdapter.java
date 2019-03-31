package uk.ac.uel.ontheway;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class TravelAdapter extends BaseAdapter {

    LayoutInflater mInflator;
    ArrayList<String> travelNames;
    ArrayList<String> createTimes;

    public TravelAdapter(Context c, ArrayList<String> n, ArrayList<String> t){
        travelNames = n;
        createTimes = t;
        mInflator = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        if(travelNames == null)
            return 0;
        else
            return travelNames.size();
    }

    @Override
    public Object getItem(int i) {

        return travelNames.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        View v = mInflator.inflate(R.layout.travellist_view_single,null);
        TextView nameTextView = v.findViewById(R.id.nameTextView);
        TextView timeTextView = v.findViewById(R.id.timeTextView);
        if (travelNames == null || createTimes == null){
            return v;
        }
        else {
            nameTextView.setText(travelNames.get(i));
            timeTextView.setText(createTimes.get(i));

            return v;
        }
    }
}
