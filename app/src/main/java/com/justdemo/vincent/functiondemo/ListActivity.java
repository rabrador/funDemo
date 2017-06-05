package com.justdemo.vincent.functiondemo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    ArrayList<Touris> dbTouris = new ArrayList<>();
    private String data;
    private ListView mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        initView();

        //Image List
        final LayoutInflater image_layout = LayoutInflater.from(this);

        // Load Raw file and covert to String
        covRawToString(loadRawFile());

        // Parser json data
        parserJson(data);

        // insert to ListView
        String[] namesArr = new String[dbTouris.size()];

        for (int i = 0; i < dbTouris.size(); i++) {
            namesArr[i] = dbTouris.get(i).getName();
        }

        mList.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, namesArr));

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View view_layout = image_layout.inflate(R.layout.image_list, null);

                /* Show introduction */
                new AlertDialog.Builder(ListActivity.this)
                        .setView(view_layout)
                        .setTitle("介紹")
                        .setMessage(dbTouris.get(position).getIntroduction())
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();

                //Toast.makeText(getApplicationContext(), dbTouris.get(position).getIntroduction(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public InputStream loadRawFile() {
        return getResources().openRawResource(R.raw.data);
    }

    public String covRawToString(InputStream inputStream) {

        try {
            int size = inputStream.available();

            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            data = new String(buffer, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    public void parserJson(String sJson) {
        try {
            JSONObject obj = new JSONObject(sJson);
            JSONArray array = obj.getJSONArray("data");

            for (int i = 0; i < array.length(); i++) {
                JSONObject subObj = array.getJSONObject(i);

                Touris touris = new Touris(subObj.getString("Name"), subObj.getString("Title"), subObj.getString("Introduction"),
                        subObj.getString("Nlat"), subObj.getString("Elong"));
                dbTouris.add(touris);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mList = (ListView) findViewById(R.id.mainList);
    }
}
