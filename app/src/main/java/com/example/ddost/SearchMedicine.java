package com.example.ddost;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

import javax.annotation.Nullable;

public class SearchMedicine {
    Context context;
    RecyclerView recyclerView;
    SearchView searchView;
    LinearLayout linearLayout;
    LinearLayout linearLayout1;

    public SearchMedicine(Context context, RecyclerView recyclerView, SearchView searchView, LinearLayout linearLayout,@Nullable LinearLayout linearLayout1){
        this.context=context;
        this.recyclerView = recyclerView;
        this.searchView = searchView;
        this.linearLayout = linearLayout;
        this.linearLayout1 = linearLayout1;
    }

    public void search(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchResult(newText);
                return false;
            }
        });
    }

    public void searchResult(String mText){
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        int searchLength=3;

        //extracting data from database

        SQLiteOpenHelper helper=new Database(context);
        SQLiteDatabase db=helper.getReadableDatabase();
        Cursor cursor = db.query("DAWAI",
                new String[] {"CODE","BRANDNAME","GENERIC","COMPANY","PACKING","MRP","PRICE","SAVINGS"},
                null,null,null,null,null);

        if(mText.length()<searchLength){   //when search length is not enough
            ArrayList<String> nothing = new ArrayList<>();
            ArrayList<Float> nothing2 = new ArrayList<>();
            SearchAdapter searchAdapter = new SearchAdapter(context,nothing,nothing,nothing,nothing,nothing, nothing2,nothing2,nothing2);
            recyclerView.setAdapter(searchAdapter);

            linearLayout.setVisibility(View.VISIBLE);
            if(linearLayout1!=null){
                linearLayout1.setVisibility(View.VISIBLE);
            }
        }else{
            //search length is reached.
            linearLayout.setVisibility(View.INVISIBLE);
            if(linearLayout1!=null){
                linearLayout1.setVisibility(View.INVISIBLE);
            }

            ArrayList<String> code=new ArrayList<>();
            ArrayList<String> brand = new ArrayList<>();
            ArrayList<String> generic= new ArrayList<>();
            ArrayList<String> company= new ArrayList<>();
            ArrayList<String> packing = new ArrayList<>();
            ArrayList<Float> saving = new ArrayList<>();
            ArrayList<Float> mrp = new ArrayList<>();
            ArrayList<Float> price = new ArrayList<>();

            boolean cursorValue = cursor.moveToFirst();
            String c,t,b,g;
            while(cursorValue){
                c=cursor.getString(0);
                b=cursor.getString(1);
                g=cursor.getString(2);

                String sText=mText.toUpperCase(Locale.ENGLISH);
                if (c.contains(sText) || b.contains(sText) || g.contains(sText)){
                    code.add(cursor.getString(0));
                    brand.add(cursor.getString(1));
                    generic.add(cursor.getString(2));
                    company.add(cursor.getString(3));
                    packing.add(cursor.getString(4));
                    saving.add(cursor.getFloat(7));
                    mrp.add(cursor.getFloat(5));
                    price.add(cursor.getFloat(6));
                }
                cursorValue=cursor.moveToNext();
            }

            //populating recycler view
            SearchAdapter searchAdapter = new SearchAdapter(context,code,brand, generic,company, packing, saving, mrp, price);
            recyclerView.setAdapter(searchAdapter);

        }
    }
}
