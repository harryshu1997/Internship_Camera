package com.smewise.camera2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

public class CDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "CDataBaseHelper";

    private static final String TABLE_NAME = "C_Mode_DATA";
    private static final String Col1 = "ID";
    private static final String Col2 = "Data";
//    private static final String Col3 = "Sensitivity";
//    private static final String Col4 = "ExpTime";





    public CDBHelper(Context context) {
        super(context, TABLE_NAME, null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " ( " + Col1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Col2 + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String item){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Col2, item);
       Log.d(TAG,"addData: Adding " + item + " to " + TABLE_NAME);

       //check col names
//       SQLiteDatabase mdb;
//       mdb = getReadableDatabase();
//       Cursor c = mdb.query(TABLE_NAME, null,null,null,null,null,null);
//       String[] names = c.getColumnNames();
//       for(int i=0; i<names.length;i++){
//           System.out.println(names[i]);
//       }

       long result = db.insert(TABLE_NAME,null,contentValues);

       //check if it is inserted correctly
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query,null);
        return data;
    }

    public Cursor getItemId(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + Col1 + " FROM " + TABLE_NAME + " WHERE " + Col2 + "  = '" + name + "'";
        Cursor data = db.rawQuery(query,null);
        return data;
    }

    public void updateData(String newData, int id, String oldData){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME + " SET " + Col2 + " = '" + newData + "' WHERE " + Col1 +
                " = '" + id + "'" + " AND " + Col2 + " = '" + oldData + "'";
        Log.d(TAG,"update query: " + query);
        Log.d(TAG,"set data to : " + newData);
        db.execSQL(query);
    }

    public void deleteData(int id, String data){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE " + Col1 +" = '" + id + "'" + " AND " +
                Col2 + " = '" + data + "'";

        Log.d(TAG,"delete query: " + query);
        Log.d(TAG,"delete data : " + data);
        db.execSQL(query);
    }
}













