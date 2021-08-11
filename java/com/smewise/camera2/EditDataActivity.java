package com.smewise.camera2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class EditDataActivity extends AppCompatActivity {

    private ImageButton btn_goBack;
    private Button btnSave,btnDelete;
    private EditText text;

    CDBHelper cdb;

    private String selectedData;
    private int selectedID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_data);
        btn_goBack = findViewById(R.id.list_goBack);
        btn_goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ListDataActivity.class);
                startActivity(intent);
            }
        });

        btnSave = (Button)findViewById(R.id.c_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String item = text.getText().toString();
                if(!item.equals("")){
                    //save the string
                    cdb.updateData(item,selectedID,selectedData);
                }else{
                    Toast.makeText(getApplicationContext(),"need to add data!",Toast.LENGTH_SHORT).show();
                }
            }
        });



        btnDelete = (Button)findViewById(R.id.c_delete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cdb.deleteData(selectedID,selectedData);
                text.setText("");
               // Intent intent = new Intent(getApplicationContext(),ListDataActivity.class);
               // startActivity(intent);
            }
        });

        text = (EditText)findViewById(R.id.list_data);
        cdb = new CDBHelper(this);


        //get result
        Intent receivedIntent = getIntent();
        selectedID = receivedIntent.getIntExtra("id",-1);
        selectedData = receivedIntent.getStringExtra("cdata");

        //set text
        text.setText(selectedData);
    }
}