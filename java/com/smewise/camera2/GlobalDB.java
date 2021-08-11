//package com.smewise.camera2;
//
//import android.content.Context;
//
//public class GlobalDB {
//    private static GlobalDB instance;
//
//    //Global Variables
//    private boolean isC1 = false;
//    private boolean isC2 = false;
//    private CDBHelper CDB1;
//    private CDBHelper CDB2;
//
//
//    private GlobalDB(Context context){
//        CDB1 = new CDBHelper(context);
//        CDB2 = new CDBHelper(context);
//
//    }
//
//    public void setC1(boolean c){
//        this.isC1 = c;
//    }
//
//    public void setC2(boolean c){
//        this.isC2 = c;
//    }
//
//    public boolean getC1(){
//        return this.isC1;
//    }
//
//    public boolean getC2(){
//        return this.isC2;
//    }
//
//    public CDBHelper getCDB1(){
//        return this.CDB1;
//    }
//
//    public CDBHelper getCDB2(){
//        return this.CDB2;
//    }
//
//
//
//    public static synchronized GlobalDB getInstance(Context context){
//        if(instance == null){
//            instance = new GlobalDB(context);
//        }
//        return instance;
//    }
//
//
//}
