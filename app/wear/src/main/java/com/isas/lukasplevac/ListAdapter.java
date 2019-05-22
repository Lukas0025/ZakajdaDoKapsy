package com.isas.lukasplevac;

import android.content.Context;
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
        super(context, R.layout.list_item, data);
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
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            viewHolder.txtMark = (TextView) convertView.findViewById(R.id.item_mark);
            viewHolder.txtSubject = (TextView) convertView.findViewById(R.id.item_title);
            viewHolder.txtDesc = (TextView) convertView.findViewById(R.id.item_desc);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.txtMark.setText(dataModel.getMark());
        viewHolder.txtSubject.setText(dataModel.getSubject());
        viewHolder.txtDesc.setText(dataModel.getDesc());
        return convertView;
    }
}
