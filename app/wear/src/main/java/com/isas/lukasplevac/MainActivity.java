package com.isas.lukasplevac;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Enables Always-on
        setAmbientEnabled();

        final ListView list = (ListView) findViewById(R.id.list);

        ArrayList<MarkListDataModel> MarkListDataModels = new ArrayList<>();
        //Testovací data
        MarkListDataModels.add(new MarkListDataModel("1", "Matematika", "rovnice"));
        MarkListDataModels.add(new MarkListDataModel("4", "Český jazyk", "sloh"));
        MarkListDataModels.add(new MarkListDataModel("3", "Anglický jazky", "past simple"));
        MarkListDataModels.add(new MarkListDataModel("1", "Programování", "vektory a matice"));
        MarkListDataModels.add(new MarkListDataModel("1", "Ekonomika", "výpočet DPH"));

        // Add a header to the ListView
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.header, list, false);
        list.addHeaderView(header);

        // DataBind ListView with items from ArrayAdapter
        ListAdapter adapter = new ListAdapter(MarkListDataModels, getApplicationContext());

        list.setAdapter(adapter);
    }
}
