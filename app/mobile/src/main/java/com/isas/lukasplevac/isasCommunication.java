package com.isas.lukasplevac;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class isasCommunication extends AsyncTask<String, Void, JSONArray> {

    private final Context mContext;
    private onIsasComListener<JSONArray> mCallBack;
    private String fail = null;
    private final String apiServer = "https://example.com/api.php";

    public isasCommunication (final Context context, onIsasComListener callback) {
        mContext = context;
        mCallBack = callback;
    }

    @Override
    protected JSONArray doInBackground(String... params) {
        //server, username, pass, action
        try {

            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(apiServer + "?action=" + params[3]);

            List<NameValuePair> POSTparams = new ArrayList<NameValuePair>();

            POSTparams.add(new BasicNameValuePair("server", params[0]));
            POSTparams.add(new BasicNameValuePair("user", params[1]));
            POSTparams.add(new BasicNameValuePair("pass", params[2]));
            if (params.length > 4 && params[4] != null) {
                POSTparams.add(new BasicNameValuePair("id", params[4]));
            }

            httpPost.setEntity(new UrlEncodedFormEntity(POSTparams));

            HttpResponse response = httpclient.execute(httpPost);

            if(response.getStatusLine().getStatusCode()==200){

                String server_response = EntityUtils.toString(response.getEntity());

                try {
                    final JSONObject obj = new JSONObject(server_response);

                    if (obj.getString("status").equals("ok")) {
                        final JSONArray data = obj.getJSONArray("data");
                        return data;
                    } else {
                        fail = obj.getString("message");
                    }

                } catch (org.json.JSONException e) {
                    fail = "rozbalení dat ze serveru selhalo";
                    Log.e("isasDoKapsy", e.getMessage());
                    Log.e("isasDoKapsy", server_response);
                }

            } else {
                fail = "připojení k SAS serveru selhalo";
            }
        } catch(IOException e) {
            fail = "nepodařilo se připojit k serveru";
            Log.e("isasDoKapsy", e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(JSONArray result) {
        if (mCallBack != null) {
            if (fail == null) {
                mCallBack.onSuccess(result);
            } else {
                mCallBack.onFailure(fail);
            }
        }
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Void... values) {}
}
