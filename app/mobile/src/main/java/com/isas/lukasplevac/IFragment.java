package com.isas.lukasplevac;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class IFragment extends Fragment {

    public View view;
    public TextView base;
    public TextView value;
    public TextView teacher;
    public Boolean created = false;


    public IFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.content_mark_info, container, false);

        base = (TextView) view.findViewById(R.id.base);
        value = (TextView) view.findViewById(R.id.value);
        teacher = (TextView) view.findViewById(R.id.teacher);

        created = true;

        return view;
    }
}