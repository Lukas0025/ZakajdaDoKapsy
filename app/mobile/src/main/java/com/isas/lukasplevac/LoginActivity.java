package com.isas.lukasplevac;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;


public class LoginActivity extends AppCompatActivity {

    private EditText serveredit;
    private EditText useredit;
    private EditText passedit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.

        serveredit = (EditText) findViewById(R.id.input_server);
        useredit = (EditText) findViewById(R.id.input_user);
        passedit = (EditText) findViewById(R.id.input_pass);

        TextView privacylink = (TextView) findViewById(R.id.link_privacy);

        privacylink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.isasdokapsy.ga/privacy.html"));
                startActivity(browserIntent);
            }
        });

        Button login = (Button) findViewById(R.id.btn_login);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                login(
                        serveredit.getText().toString(),
                        useredit.getText().toString(),
                        passedit.getText().toString()
                );
            }
        });
    }

    private String getScool(String server) {
        try {
            URL url = new URL(server);
            return url.getHost();
        } catch (MalformedURLException e) {
            Log.e("isasDoKapsy", e.getMessage());
            return "neznámá škola";
        }
    }

    private void login(final String server, final String user, final String pass) {

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Přihlašování");
        progressDialog.show();

        isasCommunication isas = new isasCommunication(getApplicationContext(), new onIsasComListener<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                SharedPreferences sharedPref = getSharedPreferences("isas", Context.MODE_PRIVATE);

                try {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("server", server);
                    editor.putString("user", user);
                    editor.putString("pass", pass);
                    editor.putString("school", getScool(server));
                    editor.putString("fullname", result.getJSONObject(0).getString("fullname"));
                    editor.commit();

                    progressDialog.hide();

                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    finish();
                    startActivity(intent);
                } catch (JSONException e) {
                    Log.e("isasDoKapsy", e.getMessage());
                }
            }

            @Override
            public void onFailure(String e) {
                progressDialog.hide();
                View view = findViewById(android.R.id.content);
                Snackbar.make(view, e, Snackbar.LENGTH_LONG).show();
            }
        });

        isas.execute(
                server,
                user,
                pass,
                "getuser"
        );
    }
}