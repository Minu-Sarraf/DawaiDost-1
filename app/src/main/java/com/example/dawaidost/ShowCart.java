package com.example.dawaidost;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ShowCart extends AppCompatActivity {

    SQLiteOpenHelper helper = new Database(ShowCart.this);
    ArrayList<String> code= new ArrayList<>();
    ArrayList<String> type= new ArrayList<>();
    ArrayList<String> brand= new ArrayList<>();
    ArrayList<String> generic= new ArrayList<>();
    public CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_cart);
        setTitle("Your Cart");

        //floating action button to send mail
        FloatingActionButton fb= findViewById(R.id.fab);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //code to send mail

                Toast.makeText(ShowCart.this,"Send Mail",Toast.LENGTH_SHORT).show();

                //delete all items from cart after sending mail
                SQLiteOpenHelper helper = new Database(ShowCart.this);
                SQLiteDatabase db = helper.getReadableDatabase();
                db.delete("CART",null,null);

                //go to homepage
                Intent intent = new Intent(ShowCart.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });


        try {

            //showing items in cart
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query("CART",
                    new String[]{"CODE", "TYPE", "BRANDNAME", "GENERIC"},
                    null, null, null, null, null);


            //recycler view
            boolean cursorValue = cursor.moveToFirst();

            while(cursorValue) {
                code.add(cursor.getString(0));
                type.add(cursor.getString(1));
                brand.add(cursor.getString(2));
                generic.add(cursor.getString(3));
                cursorValue= cursor.moveToNext();
            }
            cursor.close();

        } catch(SQLiteException e) {
            Toast toast = Toast.makeText(ShowCart.this, "Database Unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        if (code.size()==0){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Items on cart");

            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    Intent intent = new Intent(ShowCart.this,MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                }
            });

            builder.create().show();
        }

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // Create adapter passing in the sample user data
        adapter = new CartAdapter(ShowCart.this,code,type,brand,generic);

        /*adapter.notifyItemRemoved(adapter.getAdapterPosition());
        adapter.notifyDataSetChanged();*/

        // Attach the adapter to the recyclerview to populate items
        recyclerView.setAdapter(adapter);

        // Set layout manager to position the items
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //set divider
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
