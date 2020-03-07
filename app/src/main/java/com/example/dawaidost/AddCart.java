package com.example.dawaidost;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;

public class AddCart extends AppCompatActivity implements View.OnClickListener {

    TextView tv1, tv2, tv3, tv4,tv5,tv6;
    String code, type, brand, generic, company, price, mrp;
    int noOrder;
    SQLiteOpenHelper helper = new Database(AddCart.this);
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cart);
        setTitle("Add to Cart");

        //populating views
        tv1=findViewById(R.id.code);
        tv2=findViewById(R.id.type);
        tv3=findViewById(R.id.brand);
        tv4=findViewById(R.id.generic);
        tv5=findViewById(R.id.company);
        tv6=findViewById(R.id.price);

        Intent intent = getIntent();
        code = intent.getStringExtra("Code");
        type = intent.getStringExtra("Type");
        brand = intent.getStringExtra("Brand");
        generic = intent.getStringExtra("Generic");

        db=helper.getReadableDatabase();

        //getting values of dawai
        Cursor cursor = db.query("DAWAI",
                new String[] {"CODE","COMPANY","MRP","PRICE"},
                "CODE=?",
                new String[] {code},
                null,null,null);

        if (cursor.moveToFirst()){
            company=(cursor.getString(1));
            price=(cursor.getString(3));
            mrp=cursor.getString(2);
        }

        tv1.setText(code);
        tv2.setText(type);
        tv3.setText(brand);
        tv4.setText(generic);
        tv5.setText(company);
        tv6.setText(price+"/"+mrp);


        //select number of order
        MaterialSpinner spinner = findViewById(R.id.spinner);
        spinner.setItems("1","2","3","4","5","6","7","8","9","10");
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                //Log.d("clicked",item);
                noOrder=Integer.parseInt(item);
            }
        });

        //adding to cart
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //add an order to cart
                try{
                    db= helper.getReadableDatabase();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("CODE",code);
                    contentValues.put("TYPE",type);
                    contentValues.put("BRANDNAME",brand);
                    contentValues.put("GENERIC",generic);
                    contentValues.put("PRICE",price);
                    contentValues.put("MAXORDER",noOrder);
                    db.insert("CART",null,contentValues);
                }catch(SQLException e){
                    Toast.makeText(AddCart.this,"Database Unavailabe",Toast.LENGTH_SHORT).show();
                }

                //go to home page
                Intent intent = new Intent(AddCart.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Toast.makeText(AddCart.this,"Added to Cart",Toast.LENGTH_SHORT).show();
                startActivity(intent);

            }
        });

    }

    @Override
    public void onClick(View view) {

    }
}
