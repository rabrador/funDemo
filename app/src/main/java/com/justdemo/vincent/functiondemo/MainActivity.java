package com.justdemo.vincent.functiondemo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnT1;
    private ImageButton btnT2;
    private ImageButton btnT3;
    private ImageButton btnT4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        /* Connect to EDU */
        //new connectEDU().execute("http://140.115.197.16/?school=nptu&app=pingtungtravel&year=106");

        /* Navigation */
        btnT1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intMap = new Intent();
                intMap.setClass(MainActivity.this, MapsActivity.class);
                startActivity(intMap);
            }
        });

        /* List View */
        btnT2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intList = new Intent();
                intList.setClass(MainActivity.this, ListActivity.class);
                startActivity(intList);
            }
        });

        /* Login in */
        btnT3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intLogin = new Intent();
                intLogin.setClass(MainActivity.this, LoginActivity.class);
                startActivity(intLogin);
            }
        });

        /* Camera */
        btnT4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intCam = new Intent();
                intCam.setClass(MainActivity.this, CamActivity.class);
                startActivity(intCam);
            }
        });
    }

    private void initView() {
        btnT1 = (ImageButton) findViewById(R.id.imgBtnT1);
        btnT2 = (ImageButton) findViewById(R.id.imgBtnT2);
        btnT3 = (ImageButton) findViewById(R.id.imgBtnT3);
        btnT4 = (ImageButton) findViewById(R.id.imgBtnT4);
    }

    private class connectEDU extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                int response = conn.getResponseCode();
                Log.d("TAG", "The response is: " + response);

            } catch (Exception e) {
                //ERROR
                //Toast.makeText(MainActivity.this, "Connect ERROR", Toast.LENGTH_LONG).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            //Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_LONG).show();
            super.onPostExecute(s);
        }

    }
}
