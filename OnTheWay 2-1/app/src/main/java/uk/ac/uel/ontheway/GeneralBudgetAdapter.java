package uk.ac.uel.ontheway;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class GeneralBudgetAdapter extends BaseAdapter {

    LayoutInflater mInflator;
    ArrayList<String> travelNames;
    ArrayList<String> costs;

    public GeneralBudgetAdapter(Context c, ArrayList<String> tN, ArrayList<String> co){
        travelNames = tN;
        costs = co ;
        mInflator = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        if(travelNames == null)
            return 0;
        else
            return travelNames.size();
    }

    @Override
    public Object getItem(int position) {
        return travelNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        View v = mInflator.inflate(R.layout.general_budget_single,null);
        TextView tripTextView = v.findViewById(R.id.tripTextView);
        TextView costTextView = v.findViewById(R.id.costTextView);


        if (travelNames == null || costs == null){
            return v;
        }
        else {

            tripTextView.setText(travelNames.get(i));
            costTextView.setText("£" + costs.get(i));

            return v;
        }
    }
}
