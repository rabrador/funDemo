package com.example.vincent.functiondemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<Touris> dbTouris = new ArrayList<>();
    private String data;
    private ListView mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

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
    }

    public InputStream loadRawFile() {
        return getResources().openRawResource(R.raw.data);
    }

    public String covRawToString(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

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

                Touris touris = new Touris(subObj.getString("Name"), subObj.getString("Title"), subObj.getString("Introduction"));
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
