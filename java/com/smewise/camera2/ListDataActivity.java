package com.smewise.camera2;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ListDataActivity extends AppCompatActivity {

    private static final String TAG = "ListDataActivity";

    CDBHelper cdb;
    private ListView list;
    private ImageButton  goBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data);
        list = (ListView) findViewById(R.id.c_listView);
        cdb = new CDBHelper(this);
        goBack = findViewById(R.id.listView_goBack);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),CmodeActivity.class);
                startActivity(intent);
            }
        });
        populateListView();
    }

    private void populateListView() {
        Log.d(TAG,"populateList: Displaying Data in the list view");

        Cursor data = cdb.getData();
        ArrayList<String> listData = new ArrayList<String>();
        while(data.moveToNext()){
            listData.add(data.getString(1));
        }
        ListAdapter adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,listData);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String cdata = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "onItemClicked " + id);

                Cursor data = cdb.getItemId(cdata);
                int itemID = -1;
                while(data.moveToNext()){
                    itemID = data.getInt(0);
                }
                if(itemID > -1){
                    Log.d(TAG, "onClick ID is: " + itemID);
                    Intent editIntent = new Intent(ListDataActivity.this, EditDataActivity.class);
                    editIntent.putExtra("id",itemID);
                    editIntent.putExtra("cdata",cdata);
                    startActivity(editIntent);
                }else{
                    Toast.makeText(getApplicationContext(),"No ID Found",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}