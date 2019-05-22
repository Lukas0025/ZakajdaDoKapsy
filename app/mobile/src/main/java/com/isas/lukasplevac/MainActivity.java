package com.isas.lukasplevac;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ArrayList<MarkListDataModel> MarkListDataModels = new ArrayList<>();
    private ArrayList<String> subjects = new ArrayList<String>();
    private ArrayList<JSONObject> activelist = new ArrayList<>();
    private SharedPreferences sharedPref;
    private ListAdapter adapter;
    private SubjectListAdapter sla;
    private MenuItem menuSelectItem;
    private NavigationView navigationView;
    private ValueLineChart mCubicValueLineChart;
    public final String[] weekdays = {"ne", "po", "út", "st", "čt", "pá", "so"};
    public final String isasDateFormat = "dd.MM.yyyy";

    public void updateMarksFromSrever() {
        isasCommunication isas = new isasCommunication(getApplicationContext(), new onIsasComListener<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                View view = findViewById(android.R.id.content);

                try {
                    final JSONArray oldmarks = new JSONArray(sharedPref.getString("marks", "[]"));

                    if (oldmarks.length() != result.length()) {

                        Snackbar.make(view, "Byly načteny nové známky.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("zobrazit", new RefreshListView()).show();
                    }

                    Log.i("isasDoKapsy", "Marks from server len: " + result.length());
                    Log.i("isasDoKapsy", "Marks in app len: " + oldmarks.length());

                } catch (org.json.JSONException e) {
                    Log.e("isasDoKapsy", e.getMessage());
                }

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("marks", result.toString());
                editor.commit();
            }

            @Override
            public void onFailure(String e) {
                View view = findViewById(android.R.id.content);
                Snackbar.make(view, e, Snackbar.LENGTH_LONG).show();
            }
        });

        isas.execute(
                sharedPref.getString("server", ""),
                sharedPref.getString("user", ""),
                sharedPref.getString("pass", ""),
                "getmarks"
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPref = getSharedPreferences("isas", Context.MODE_PRIVATE);

        //check is user login
        if (sharedPref.getString("server", null) == null) {
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            finish();
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        menuSelectItem = navigationView.getMenu().getItem(0);
        menuSelectItem.setChecked(true);
        navigationView.setNavigationItemSelectedListener(this);

        getSupportActionBar().setTitle("Přehled");

        final ListView list = (ListView) findViewById(R.id.list);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (activelist.size() > 0) {
                    JSONObject mark = activelist.get(position - 1);
                    Intent intent = new Intent(getBaseContext(), MarkInfo.class);
                    intent.putExtra("mark_info", mark.toString());
                    startActivity(intent);
                }
            }
        });

        // Add a header to the ListView
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.list_header, list, false);
        list.addHeaderView(header);

        View nomarks = getLayoutInflater().inflate(R.layout.nodata, null, false);
        nomarks.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // DataBind ListView with items from ArrayAdapter
        adapter = new ListAdapter(MarkListDataModels, getApplicationContext());
        ((ViewGroup)list.getParent()).addView(nomarks);
        list.setEmptyView(nomarks);
        list.setAdapter(adapter);

        RecyclerView mHorizontalRecyclerView = (RecyclerView) findViewById(R.id.chip_subject_view);

        sla = new SubjectListAdapter(subjects, getApplication());

        sla.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView subtv = (TextView) v.findViewById(R.id.subject_text_chip);
                String subject = subtv.getText().toString();
                loadMarksBySubject(subject);
            }
        });

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        mHorizontalRecyclerView.setLayoutManager(horizontalLayoutManager);
        mHorizontalRecyclerView.setAdapter(sla);

        mCubicValueLineChart = (ValueLineChart) findViewById(R.id.cubiclinechart);

        //set texts in nav header
        View navheader = navigationView.getHeaderView(0);
        TextView fullname = (TextView) navheader.findViewById(R.id.fullname);
        TextView school = (TextView) navheader.findViewById(R.id.school);

        fullname.setText(sharedPref.getString("fullname", "NaN"));
        school.setText(sharedPref.getString("school", "NaN"));

        //Load save marks
        loadMarks(5);
        //Load subject list form save marks
        loadSubjects();
        //load chart
        loadChart();
        //Try update marks from server
        updateMarksFromSrever();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(getBaseContext(), R.string.soon, Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadSubjects() {
        try {
            subjects.clear();

            final JSONArray marks = new JSONArray(sharedPref.getString("marks", "[]"));

            for (int i = 0; i < marks.length(); i++) {
                JSONObject mark = marks.getJSONObject(i);
                if (!subjects.contains(mark.getString("subject"))) {
                    subjects.add(mark.getString("subject"));
                }
            }

            sla.notifyDataSetChanged();

        } catch (org.json.JSONException e) {
            Log.e("isasDoKapsy", e.getMessage());
        }
    }

    private void loadDiameters() {
        MarkListDataModels.clear();
        activelist.clear();

        for (int i = 0; i < subjects.size(); i++) {
            try {

                float sum = 0;
                float weights = 0;

                final JSONArray marks = new JSONArray(sharedPref.getString("marks", "[]"));

                for (int j = 0; j < marks.length(); j++) {
                    JSONObject mark = marks.getJSONObject(j);

                    String marksrt;
                    try {
                        marksrt = mark.getString("value");
                    } catch (org.json.JSONException e) {
                        //nemusí mít hodnoty na sasu
                        marksrt = mark.getString("mark");
                    }

                    if (mark.getString("subject").equals(subjects.get(i)) && isFloat(marksrt)) {

                        float weight;
                        try {
                            weight = Float.parseFloat(mark.getString("weight"));
                        } catch (Exception e) {
                            //nemusí mít hodnoty na sasu
                            weight = 1;
                        }

                        sum += Float.parseFloat(marksrt) * weight;
                        weights += weight;
                    }
                }

                if (weights != 0) {
                    float diameter = sum / weights;

                    DecimalFormat shortf = new DecimalFormat("#.#");
                    DecimalFormat longf = new DecimalFormat("#.#####");

                    MarkListDataModels.add(new MarkListDataModel(shortf.format(diameter), subjects.get(i), longf.format(diameter)));
                } else {
                    MarkListDataModels.add(new MarkListDataModel("N", subjects.get(i), "NaN"));
                }

            } catch (org.json.JSONException e) {
                Log.e("isasDoKapsy", e.getMessage());
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void loadMarksBySubject(String subject) {
        onNavigationItemSelected(navigationView.getMenu().getItem(1));
        menuSelectItem.setChecked(true);

        try {
            MarkListDataModels.clear();
            activelist.clear();

            final JSONArray marks = new JSONArray(sharedPref.getString("marks", "[]"));

            for (int i = 0; i < marks.length(); i++) {
                JSONObject mark = marks.getJSONObject(i);

                if (mark.getString("subject").equals(subject)) {
                    MarkListDataModels.add(new MarkListDataModel(mark.getString("mark"), mark.getString("subject"), mark.getString("note")));
                    activelist.add(mark);
                }
            }

            adapter.notifyDataSetChanged();

        } catch (org.json.JSONException e) {
            Log.e("isasDoKapsy", e.getMessage());
        }
    }

    private void loadMarks(int limit) {
        try {
            MarkListDataModels.clear();
            activelist.clear();

            final JSONArray marks = new JSONArray(sharedPref.getString("marks", "[]"));

            for (int i = 0; i < marks.length(); i++) {
                if ((limit == -1) || (i < limit)) {
                    JSONObject mark = marks.getJSONObject(i);
                    MarkListDataModels.add(new MarkListDataModel(mark.getString("mark"), mark.getString("subject"), mark.getString("note")));
                    activelist.add(mark);
                } else {
                    break;
                }
            }

            adapter.notifyDataSetChanged();

        } catch (org.json.JSONException e) {
            Log.e("isasDoKapsy", e.getMessage());
        }
    }

    public boolean isFloat(String number) {
        try {
            Float.parseFloat(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void loadChart() {
        ValueLineSeries chartdata = new ValueLineSeries();
        chartdata.setColor(0xFF56B7F1);

        Calendar calendar = Calendar.getInstance();
        Calendar markcalendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -6);

        int count = 0;

        for (int i = 0; i < 7; i++) {
            float sum = 0;
            float weights = 0;

            try {
                final JSONArray marks = new JSONArray(sharedPref.getString("marks", "[]"));

                for (int j = 0; j < marks.length(); j++) {
                    JSONObject mark = marks.getJSONObject(j);

                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat(isasDateFormat, Locale.ITALY);
                        Date date = dateFormat.parse(mark.getString("date"));
                        markcalendar.setTime(date);
                    } catch(ParseException e){
                        Log.e("isasDoKapsy", e.getMessage());
                    }

                    boolean sameDay = calendar.get(Calendar.YEAR) == markcalendar.get(Calendar.YEAR) &&
                            calendar.get(Calendar.DAY_OF_YEAR) == markcalendar.get(Calendar.DAY_OF_YEAR);

                    String markstr;
                    try {
                        markstr = mark.getString("value");
                    } catch (Exception e) {
                        //Ne každý sas vá hodnoty známek
                        markstr = mark.getString("mark");
                    }

                    if (sameDay && isFloat(markstr)) {

                        float weight;
                        try{
                            weight = Float.parseFloat(mark.getString("weight"));
                        } catch (Exception e) {
                            //nemusí mít hodnoty na sasu
                            weight = 1;
                        }

                        sum += Float.parseFloat(markstr) * weight;
                        weights += weight;
                        count++;
                    }
                }

            } catch (org.json.JSONException e) {
                Log.e("isasDoKapsy", e.getMessage());
            }

            float diameter = 0;

            if (weights != 0) {
                diameter = sum / weights;
            }

            chartdata.addPoint(new ValueLinePoint(weekdays[calendar.get(Calendar.DAY_OF_WEEK) - 1], diameter));
            calendar.add(Calendar.DATE, 1);
        }

        TextView markscount = (TextView) findViewById(R.id.markscount);
        if (count == 1) {
            markscount.setText("1 známka");
        } else if (count > 1 && count < 5) {
            markscount.setText(count + " známky");
        } else {
            markscount.setText(count + " známek");
        }

        mCubicValueLineChart.clearChart();
        mCubicValueLineChart.addSeries(chartdata);
        mCubicValueLineChart.reloadView();
        mCubicValueLineChart.startAnimation();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        RelativeLayout graf = (RelativeLayout) findViewById(R.id.cardgraf);
        LinearLayout list_header = (LinearLayout) findViewById(R.id.list_header);

        menuSelectItem = item;

        if (id == R.id.nav_overview) {
            graf.setVisibility(View.VISIBLE);
            list_header.setVisibility(View.VISIBLE);
            loadMarks(5);
            getSupportActionBar().setTitle("Přehled");
        } else if (id == R.id.nav_diameters) {
            list_header.setVisibility(View.GONE);
            loadDiameters();
            getSupportActionBar().setTitle("Průměry");
        } else if (id == R.id.nav_marks) {
            graf.setVisibility(View.GONE);
            list_header.setVisibility(View.VISIBLE);
            loadMarks(-1);
            getSupportActionBar().setTitle("Známky");
        } else if (id == R.id.nav_setting) {
            Toast.makeText(getBaseContext(), R.string.soon, Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_setting_watch) {
            Toast.makeText(getBaseContext(), R.string.soon, Toast.LENGTH_LONG).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class RefreshListView implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            loadSubjects();
            onNavigationItemSelected(menuSelectItem);
            loadChart();
        }
    }
}
