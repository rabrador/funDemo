package com.justdemo.vincent.functiondemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    ArrayList<Touris> dbTourism = new ArrayList<>();
    ArrayList<Touris> displayTourism = new ArrayList<>();
    private String data;
    private ListView mList;
    private ImageView ivCircle;
    private LinearLayout linear;
    private TextView textClass;
    public static int REQUEST_CLASSIFY = 1;
    public static String classifyItemStr[] = {"全部", "寺廟", "公園"}; // please refer to ClassifyActivity.jave :: classifyItemEnum

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        initView();

        //Image List
        final LayoutInflater image_layout = LayoutInflater.from(this);

        // Load Raw file and covert to String
        data = useAPI.covRawToString(loadRawFile());

        // Parser json data
        dbTourism = useAPI.parserJsonFromTouris(data);

        // Classify tourism
        for (int i=0; i<dbTourism.size();i++) {
            if (dbTourism.get(i).getName().indexOf("宮") > 0 || dbTourism.get(i).getName().indexOf("廟") > 0 ) {
                dbTourism.get(i).setClassification(ClassifyActivity.classifyItemEnum.CLASSIFY_TEMPLE.ordinal()); // class 1 : 宮廟
                continue;
            }

            if (dbTourism.get(i).getName().indexOf("公") > 0 && dbTourism.get(i).getName().indexOf("園") > 0) {
                dbTourism.get(i).setClassification(ClassifyActivity.classifyItemEnum.CLASSIFY_PARK.ordinal()); // class 1 : 公園
                continue;
            }
        }

        // insert to ListView
        String[] namesArr = new String[dbTourism.size()];
        displayTourism = Touris.copyList(dbTourism, dbTourism.size());

        for (int i = 0; i < dbTourism.size(); i++) {
            namesArr[i] = displayTourism.get(i).getName();
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
                        .setMessage(displayTourism.get(position).getIntroduction())
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

        ivCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListActivity.this, ClassifyActivity.class);
                startActivityForResult(intent, REQUEST_CLASSIFY);
            }
        });

    }

    /* Receive from ClassifyActivity return value. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CLASSIFY) {
            //Update TextView of Classify display
            textClass.setText(classifyItemStr[data.getExtras().getInt("classify")]);
            updateListViewDisplay(data.getExtras().getInt("classify"));
        }
    }

    public InputStream loadRawFile() {
        return getResources().openRawResource(R.raw.data);
    }

    private void updateListViewDisplay(int key) {
        int count = 0, newIndex = 0;
        ArrayList<Touris> tempList = new ArrayList<>();

        /* 1. calculate the db count after filter */
        for (int i=0; i<dbTourism.size();i++) {
            if (dbTourism.get(i).getClassification() == key) {
                Touris.createEmptyEntry(tempList);
                count++;
            }
        }

        /* 2. initialize array */
        String[] newNameArr = new String[count];

        /* 3. reassign display string and index */
        for (int i=0; i<dbTourism.size();i++) {
            if (dbTourism.get(i).getClassification() == key) {
                tempList.get(newIndex).setName(dbTourism.get(i).getName());
                tempList.get(newIndex).setIntroduction(dbTourism.get(i).getIntroduction());
                newNameArr[newIndex] = dbTourism.get(i).getName();
//                Toast.makeText(ListActivity.this,tempList.get(newIndex).getIntroduction() + "", Toast.LENGTH_SHORT).show();
                newIndex++;
            }
        }

        displayTourism.clear();
        displayTourism = Touris.copyList(tempList, tempList.size());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ListActivity.this,
                android.R.layout.simple_list_item_1, newNameArr) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(Color.BLACK);
                return view;
            }
        };

        mList.setAdapter(adapter);
    }

    private void initView() {
        mList = (ListView) findViewById(R.id.mainList);
        ivCircle = (ImageView) findViewById(R.id.imageViewWhiteCircle);
        textClass = (TextView) findViewById(R.id.textClass);
    }
}
