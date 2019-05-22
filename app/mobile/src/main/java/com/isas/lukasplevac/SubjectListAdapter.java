package com.isas.lukasplevac;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class SubjectListAdapter extends RecyclerView.Adapter<SubjectListAdapter.ViewHolder> {

    private ArrayList<String> Subjects = new ArrayList<String>();;
    private Context context;
    private View.OnClickListener mClickListener;

    public SubjectListAdapter(ArrayList<String> subjects, Context context) {
        this.Subjects = subjects;
        this.context = context;
    }

    public void setClickListener(View.OnClickListener callback) {
        mClickListener = callback;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView subjectTV;

        public ViewHolder(View view) {
            super(view);
            subjectTV = (TextView) view.findViewById(R.id.subject_text_chip);
        }
    }

    @Override
    public SubjectListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subject_chip, parent, false);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onClick(view);
            }
        });

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.subjectTV.setText(Subjects.get(position));

        int[] colors = {R.drawable.ic_chip, R.drawable.ic_chip_2, R.drawable.ic_chip_3, R.drawable.ic_chip_4};

        int rnd = new Random().nextInt(colors.length);

        final int sdk = android.os.Build.VERSION.SDK_INT;


        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            holder.subjectTV.setBackgroundDrawable(ContextCompat.getDrawable(this.context, colors[rnd]));
        } else {
            holder.subjectTV.setBackground(ContextCompat.getDrawable(this.context, colors[rnd]));
        }

    }

    @Override
    public int getItemCount() {
        return Subjects.size();
    }
}