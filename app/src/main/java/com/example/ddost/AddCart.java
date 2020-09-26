package com.example.ddost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ddost.ui.cart.CartFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddCart extends AppCompatActivity {

    TextView tv1, tv2, tv3, tv4,tv5,tv6,tv7;
    String code, type, brand, generic, company,packing;
    Float price;
    Float mrp;
    int maxOrder;
    Float total;
    Integer prescription;
    int noOrder=1;
    SQLiteOpenHelper helper = new Database(AddCart.this);
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cart);
        setTitle("Add to Cart");

        //backbutton
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //populating views
        tv1=findViewById(R.id.code);
        tv2=findViewById(R.id.type);
        tv3=findViewById(R.id.brand);
        tv4=findViewById(R.id.generic);
        tv5=findViewById(R.id.company);
        tv6=findViewById(R.id.mrp);
        tv7=findViewById(R.id.price);


        Intent intent = getIntent();
        code = intent.getStringExtra("Code");
        type = intent.getStringExtra("Type");
        brand = intent.getStringExtra("Brand");
        generic = intent.getStringExtra("Generic");

        db=helper.getReadableDatabase();

        //number of items in cart
        Cursor cursor1 = db.query("CART",
                new String[]{"CODE"},
                null,null,null,null,null);
        int count = 0;
        boolean countValue = cursor1.moveToFirst();
        while(countValue){
            count++;
            countValue=cursor1.moveToNext();
        }

        cursor1.close();

        //getting values of dawai
        final Cursor cursor = db.query("DAWAI",
                new String[] {"CODE","COMPANY","MRP","PRICE","PRESCRIPTION","PACKING","MAXORDER"},
                "CODE=?",
                new String[] {code},
                null,null,null);

        if (cursor.moveToFirst()){
            company=(cursor.getString(1));
            price=(cursor.getFloat(3));
            mrp=cursor.getFloat(2);
            prescription=cursor.getInt(4);
            packing=cursor.getString(5);
            maxOrder=cursor.getInt(6);
        }
        cursor.close();

        tv1.setText(code);
        tv2.setText(type);
        tv3.setText(brand);
        tv4.setText(generic);
        tv5.setText(company);
        tv6.setText("Rs. "+String.format("%.02f",mrp));
        tv7.setText("Rs. "+String.format("%.02f",price));


        //select number of order
        MaterialSpinner spinner = findViewById(R.id.spinner);
        ArrayList<String> list = new ArrayList<>();
        int cnt=0;
        while(cnt<maxOrder){
            list.add(String.valueOf(cnt+1));
            cnt++;
        }
        spinner.setItems(list);
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                //Log.d("clicked",item);
                noOrder=Integer.parseInt(item);
            }
        });

        //adding to cart
        FloatingActionButton fab = findViewById(R.id.fab);
        final int finalCount = count;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(finalCount ==10){
                    Snackbar.make(view,
                            "Max is 10 items in 1 order",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

                total=price*noOrder;

                Cursor cursor1 = db.query("CART",
                        new String[] {"PRICE","MAXORDER","TOTAL"},
                        "CODE=?",
                        new String[]{code},
                        null,null,null
                );

                //update value in cart
                if(cursor1.moveToFirst()){
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("MAXORDER",noOrder+cursor1.getInt(1));
                    contentValues.put("TOTAL",total+cursor1.getFloat(2));
                    db.update("CART",contentValues,"CODE=?", new String[]{code});
                    cursor1.close();
                }

                //add an order to cart
                try{
                    db= helper.getReadableDatabase();
                    ContentValues contentValues = new ContentValues();

                    contentValues.put("CODE",code);
                    contentValues.put("TYPE",type);
                    contentValues.put("BRANDNAME",brand);
                    contentValues.put("GENERIC",generic);
                    contentValues.put("PACKING",packing);
                    contentValues.put("COMPANY",company);
                    contentValues.put("MRP",mrp);
                    contentValues.put("PRICE",price);
                    contentValues.put("MAXORDER",noOrder);
                    contentValues.put("TOTAL",total);
                    contentValues.put("PRESCRIPTION",prescription);

                    db.insert("CART",null,contentValues);
                    Toast.makeText(AddCart.this,"Added to Cart",Toast.LENGTH_LONG).show();
                } catch(SQLException e){
                    Toast.makeText(AddCart.this,"Database Unavailable",Toast.LENGTH_LONG).show();
                }

                CartDeleteUpdate cartAdd = new CartDeleteUpdate(AddCart.this);
                new CartDeleteUpdate.DeleteDataActivity(AddCart.this).execute();
                cartAdd.sendData();
                Intent intent = new Intent(AddCart.this, ShowCart.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}