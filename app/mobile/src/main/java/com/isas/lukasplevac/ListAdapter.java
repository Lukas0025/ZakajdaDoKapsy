package com.isas.lukasplevac;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter<MarkListDataModel> {

    private ArrayList<MarkListDataModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtDesc;
        TextView txtSubject;
        TextView txtMark;
    }

    public ListAdapter(ArrayList<MarkListDataModel> data, Context context) {
        super(context, R.layout.mark_list_item, data);
        this.dataSet = data;
        this.mContext = context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        MarkListDataModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.mark_list_item, parent, false);
            viewHolder.txtMark = (TextView) convertView.findViewById(R.id.item_mark);
            viewHolder.txtSubject = (TextView) convertView.findViewById(R.id.item_title);
            viewHolder.txtDesc = (TextView) convertView.findViewById(R.id.item_desc);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        final String mark = dataModel.getMark();

        viewHolder.txtMark.setText(mark);

        final int sdk = android.os.Build.VERSION.SDK_INT;
        int color = R.drawable.ic_mark_1;

        try {
            float markvalue = Float.parseFloat(mark.replace(',', '.'));

            if (markvalue >= 1.5 && markvalue < 2.5) {
                color = R.drawable.ic_mark_2;
            } else if (markvalue >= 2.5 && markvalue < 3.5) {
                color = R.drawable.ic_mark_3;
            } else if (markvalue >= 3.5 && markvalue < 4.5) {
                color = R.drawable.ic_mark_4;
            } else if (markvalue >= 4.5 && markvalue <= 5) {
                color = R.drawable.ic_mark_5;
            }

        } catch (NumberFormatException e) { }

        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            viewHolder.txtMark.setBackgroundDrawable(ContextCompat.getDrawable(this.mContext, color));
        } else {
            viewHolder.txtMark.setBackground(ContextCompat.getDrawable(this.mContext, color));
        }

        viewHolder.txtSubject.setText(dataModel.getSubject());
        viewHolder.txtDesc.setText(dataModel.getDesc());
        return convertView;
    }
}
