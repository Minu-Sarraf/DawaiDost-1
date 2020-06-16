package com.example.dawaidost;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
    public static String DB_NAME="DD";
    public static int DB_VERSION=1;

    public Database(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //table for updating dawais
        db.execSQL("CREATE TABLE DAWAI(CODE TEXT PRIMARY KEY,"+
                "TYPE TEXT,"+
                "BRANDNAME TEXT,"+
                "GENERIC TEXT,"+
                "COMPANY TEXT,"+
                "PACKING TEXT,"+
                "MRP FLOAT,"+
                "PRICE FLOAT,"+
                "MAXORDER INTEGER,"+
                "PRESCRIPTION INTEGER)");

        //table for saving order
        db.execSQL("CREATE TABLE CART(CODE TEXT PRIMARY KEY,"+
                "TYPE TEXT,"+
                "BRANDNAME TEXT,"+
                "GENERIC TEXT,"+
                "COMPANY TEXT,"+
                "PRICE FLOAT,"+
                "MAXORDER INTEGER,"+
                "TOTAL FLOAT,"+
                "PRESCRIPTION INTEGER)");

        //table for regular order
        db.execSQL("CREATE TABLE REGULARORDER(CODE TEXT,"+
                "QUANTITY INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
