package com.example.dawaidost.ui.main;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dawaidost.CartAdapter;
import com.example.dawaidost.Database;
import com.example.dawaidost.R;
import com.example.dawaidost.ShowCart;
import com.example.dawaidost.UpdateDetails;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class CartFragment extends Fragment {
    Context mContext;
    ArrayList<String> code = new ArrayList<>();
    ArrayList<Float> price = new ArrayList<>();
    ArrayList<Integer> maxOrder = new ArrayList<>();
    ArrayList<String> brand = new ArrayList<>();
    public CartAdapter adapter;

    Float totalPrice= Float.valueOf(0);
    Integer delCharge;
    ArrayList<String> PinCode = new ArrayList<>();

    public CartFragment(Context context) {
        mContext=context;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setting avail Pincodes
        PinCode.add("834001");
        PinCode.add("834002");
        PinCode.add("834003");
        PinCode.add("834004");
        PinCode.add("834005");
        PinCode.add("834006");
        PinCode.add("834007");
        PinCode.add("834008");
        PinCode.add("834009");
        PinCode.add("834010");
        PinCode.add("834217");
        PinCode.add("834219");

        try {
            //showing items in cart
            SQLiteOpenHelper helper = new Database(mContext);
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query("CART",
                    new String[]{"CODE","TYPE","BRANDNAME","GENERIC","COMPANY","PRICE", "MAXORDER", "TOTAL"},
                    null, null, null, null, null);

            //recycler view
            boolean cursorValue = cursor.moveToFirst();

            while(cursorValue) {
                code.add(cursor.getString(0));
                price.add(cursor.getFloat(5));
                maxOrder.add(cursor.getInt(6));
                brand.add(cursor.getString(2));
                totalPrice+=cursor.getFloat(7);
                cursorValue= cursor.moveToNext();
            }

        }catch(SQLiteException e) {
            Toast toast = Toast.makeText(mContext, "Database Unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        String pin = mContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("pinKey"," ");
        if(PinCode.contains(pin)){
            delCharge=50;
        }else{
            delCharge=50;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.activity_show_cart, container, false);

        //floating action button to send data
        FloatingActionButton fb= root.findViewById(R.id.fab);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, UpdateDetails.class);
                intent.putExtra("TABLE_NAME","CART");
                startActivity(intent);
            }
        });

        //setting the total value and sub total value
        TextView textView = root.findViewById(R.id.showPrice);

        totalPrice=totalPrice+delCharge;
        textView.setText(" Rs "+String.format("%.02f",totalPrice) );

        textView = root.findViewById(R.id.showDeliveryPrice);
        textView.setText(" Rs "+delCharge);


        if (code.size()==0){
            ImageView image = root.findViewById(R.id.no_item);
            image.setVisibility(View.VISIBLE);

            image = root.findViewById(R.id.logo);
            image.setVisibility(View.INVISIBLE);

            RelativeLayout relativeLayout = root.findViewById(R.id.relative_layout);
            relativeLayout.setVisibility(View.VISIBLE);

            fb.setVisibility(View.INVISIBLE);

            CardView cardView = root.findViewById(R.id.totalPrice);
            cardView.setVisibility(View.INVISIBLE);

            cardView = root.findViewById(R.id.subTotal);
            cardView.setVisibility(View.INVISIBLE);
        }

        final RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        // Create adapter passing in the sample user data
        adapter = new CartAdapter(mContext,code,price,maxOrder, brand);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(adapter);

        return root;
    }
}