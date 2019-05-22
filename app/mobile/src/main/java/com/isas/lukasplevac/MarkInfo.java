package com.isas.lukasplevac;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import org.eazegraph.lib.models.PieModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MarkInfo extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private IFragment ifragment;
    private ClassFragment classfragment;
    private JSONObject mark;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mark_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPref = getSharedPreferences("isas", Context.MODE_PRIVATE);

        ifragment = new IFragment();
        classfragment = new ClassFragment();

        viewPager = (ViewPager) findViewById(R.id.pager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        final FloatingActionButton share = (FloatingActionButton) findViewById(R.id.share);

        share.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.sharetext, mark.getString("subject"), mark.getString("note"), mark.getString("mark")));
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                } catch (org.json.JSONException e) {
                    Log.e("isasDoKapsy", e.getMessage());
                }
            }
        });

        //wait for fragments

        final Handler handler = new Handler();
        final Runnable waitforifragment = new Runnable() {
            @Override
            public void run() {
                if (!ifragment.created || !classfragment.created) {
                    handler.postDelayed(this, 100);
                } else {
                    if (getIntent().hasExtra("mark_info")) {
                        showMarkInfo(getIntent().getStringExtra("mark_info"));
                    }

                    loadFromServer();
                }
            }
        };

        waitforifragment.run();
    }

    private void showClassMarks(String json) {
        try {
            JSONObject ClassMarks = new JSONObject(json);

            classfragment.pieChart.addPieSlice(new PieModel("1", Integer.parseInt(ClassMarks.getString("1")), Color.parseColor(getResources().getString(R.color.mark1))));
            classfragment.pieChart.addPieSlice(new PieModel("2", Integer.parseInt(ClassMarks.getString("2")), Color.parseColor(getResources().getString(R.color.mark2))));
            classfragment.pieChart.addPieSlice(new PieModel("3", Integer.parseInt(ClassMarks.getString("3")), Color.parseColor(getResources().getString(R.color.mark3))));
            classfragment.pieChart.addPieSlice(new PieModel("4", Integer.parseInt(ClassMarks.getString("4")), Color.parseColor(getResources().getString(R.color.mark4))));
            classfragment.pieChart.addPieSlice(new PieModel("5", Integer.parseInt(ClassMarks.getString("5")), Color.parseColor(getResources().getString(R.color.mark5))));

            classfragment.nodata.setVisibility(View.GONE);
            classfragment.pieChart.setVisibility(View.VISIBLE);
            classfragment.pieChart.startAnimation();
        } catch (org.json.JSONException e) {
            Log.e("isasDoKapsy", e.getMessage());
        }
    }

    private void loadFromServer() {
        isasCommunication isas = new isasCommunication(getApplicationContext(), new onIsasComListener<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                View view = findViewById(android.R.id.content);

                try {
                    showMarkInfo(result.getJSONObject(0).getJSONObject("info").toString());
                    showClassMarks(result.getJSONObject(0).getJSONObject("classmarks").toString());
                } catch (org.json.JSONException e) {
                    Log.e("isasDoKapsy", e.getMessage());
                }

            }

            @Override
            public void onFailure(String e) {
                View view = findViewById(android.R.id.content);
                Snackbar.make(view, e, Snackbar.LENGTH_LONG).show();
            }
        });

        try {
            isas.execute(
                    sharedPref.getString("server", ""),
                    sharedPref.getString("user", ""),
                    sharedPref.getString("pass", ""),
                    "getmarkinfo",
                    mark.getString("id")
            );
        } catch (org.json.JSONException e) {
            Log.e("isasDoKapsy", e.getMessage());
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(ifragment, "JÁ");
        adapter.addFragment(classfragment, "TŘÍDA");
        viewPager.setAdapter(adapter);
    }

    private void showMarkInfo(String info) {
        try {
            mark = new JSONObject(info);

            TextView marktv = (TextView) findViewById(R.id.mark);
            TextView subjecttv = (TextView) findViewById(R.id.subject);
            TextView notetv = (TextView) findViewById(R.id.note);

            marktv.setText(mark.getString("mark"));
            subjecttv.setText(mark.getString("subject"));
            notetv.setText(mark.getString("note"));

            String value;
            try {
                value = mark.getString("value");
            } catch (Exception e) {
                value = mark.getString("mark");
            }

            String weight;
            try {
                weight = mark.getString("weight");
            } catch (Exception e) {
                weight = "100";
            }

            ifragment.base.setText(getString(R.string.basemarkinfo, mark.getString("mark"), mark.getString("date"), mark.getString("subject"), mark.getString("note")));
            ifragment.value.setText(getString(R.string.valuemarkinfo, value, mark.getString("type"), weight));
            ifragment.teacher.setText(getString(R.string.teachermarkinfo, mark.getString("teacher")));

        } catch (org.json.JSONException e) {
            Log.e("isasDoKapsy", e.getMessage());
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
