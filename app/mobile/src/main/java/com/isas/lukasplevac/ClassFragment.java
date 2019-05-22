package com.isas.lukasplevac;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

public class ClassFragment extends Fragment {

    public Boolean created = false;
    public View nodata;
    public PieChart pieChart;

    public ClassFragment() {
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

        View view = inflater.inflate(R.layout.class_mark_info, container, false);

        pieChart = (PieChart) view.findViewById(R.id.piechart);
        nodata = view.findViewById(R.id.nodataclass);

        created = true;

        return view;
    }

}