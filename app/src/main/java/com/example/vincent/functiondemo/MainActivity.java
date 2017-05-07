package com.example.vincent.functiondemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

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

        /* List View */
        btnT2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intList = new Intent();
                intList.setClass(MainActivity.this, ListActivity.class);
                startActivity(intList);
            }
        });



    }

    private void initView() {
        btnT1 = (ImageButton) findViewById(R.id.imgBtnT1);
        btnT2 = (ImageButton) findViewById(R.id.imgBtnT2);
        btnT3 = (ImageButton) findViewById(R.id.imgBtnT3);
        btnT4 = (ImageButton) findViewById(R.id.imgBtnT4);
    }
}
