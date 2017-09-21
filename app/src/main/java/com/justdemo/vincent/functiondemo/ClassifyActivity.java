package com.justdemo.vincent.functiondemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ClassifyActivity extends AppCompatActivity {
    private ListView classList;

    public enum classifyItemEnum {
        CLASSIFY_NO_SUCH, // = ALL, 0
        CLASSIFY_TEMPLE,
        CLASSIFY_PARK,
        CLASSIFY_NUMBER,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classify);

        initView();

        classList.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, ListActivity.classifyItemStr));

        classList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = getIntent();
                Bundle bundle = new Bundle();
                bundle.putInt("classify", position);
                intent.putExtras(bundle);
                setResult(ListActivity.REQUEST_CLASSIFY, intent);
                ClassifyActivity.this.finish();
            }
        });

        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        bundle.putInt("classify", classifyItemEnum.CLASSIFY_NO_SUCH.ordinal());
        intent.putExtras(bundle);
        setResult(ListActivity.REQUEST_CLASSIFY, intent);
    }

    private void initView() {
        classList = (ListView) findViewById(R.id.ListClassify);
    }
}
