package com.smewise.camera2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class CmodeActivity extends AppCompatActivity {

    private Button btnAdd, btnView;
    private ImageButton btnGoBack;
    CDBHelper CDB;
    TextView data;
    Global g = Global.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmode);
        CDB = new CDBHelper(this);
        btnAdd = (Button) findViewById(R.id.c_add);
        btnView = (Button) findViewById(R.id.c_view);
        btnGoBack = (ImageButton)findViewById(R.id.c_goBack);
        btnGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
                startActivity(intent);
            }
        });
        data = (TextView) findViewById(R.id.Cdata);
        data.setText(g.getResult());

        btnAdd.setOnClickListener(AddListener);
        btnView.setOnClickListener(ViewListener);


    }

    private View.OnClickListener AddListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String newEntry = data.getText().toString();
            if(data.length() != 0){
                AddData(newEntry);
            }else{
                Toast.makeText(getApplicationContext(),"Data is invalid!",Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener ViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(),ListDataActivity.class);
            startActivity(intent);
        }
    };

    public void AddData(String entry){
        boolean insertData = CDB.addData(entry);
        if(insertData){
            Toast.makeText(this,"Data Successfully Inserted!",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,"Insertion Failed!",Toast.LENGTH_SHORT).show();
        }
    }
}