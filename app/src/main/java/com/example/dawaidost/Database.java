package com.example.dawaidost;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
    public static String DB_NAME="DAWAI";
    public static int DB_VERSION=1;

    Database(Context context){
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
                "MRP INTEGER,"+
                "PRICE INTEGER,"+
                "MAXORDER INTEGER)");

        //table for saving order
        db.execSQL("CREATE TABLE CART(CODE TEXT PRIMARY KEY,"+
                "TYPE TEXT,"+
                "BRANDNAME TEXT,"+
                "GENERIC TEXT,"+
                "COMPANY TEXT,"+
                "PRICE TEXT,"+
                "MAXORDER INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
