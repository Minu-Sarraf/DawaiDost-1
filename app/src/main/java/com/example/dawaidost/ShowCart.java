package com.example.dawaidost;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpRetryException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ShowCart extends AppCompatActivity {

    SQLiteOpenHelper helper = new Database(ShowCart.this);
    ArrayList<String> code = new ArrayList<>();
    ArrayList<Float> price = new ArrayList<>();
    ArrayList<Integer> maxOrder = new ArrayList<>();
    ArrayList<String> generic = new ArrayList<>();
    public CartAdapter adapter;
    String address, ip;
    double latitude, longitude;
    Float totalPrice= Float.valueOf(0);
    Cursor cursor;
    boolean connected = false;

    SharedPreferences sharedPreferences;
    public static final String MYPREFERENCES="MyPrefs";
    public static final String Name="nameKey";
    public static final String Phone="phoneKey";
    public static final String Address="addressKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_cart);
        setTitle("My Cart");

        //back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //getting ip address
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        address = info.getMacAddress();
        ip = Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());


        //check internet connection
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        }
        else{
            connected = false;
        }

/*        //getting location
        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        LocationManager mLoc = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLoc.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);*/

        //floating action button to send data to sheet
        FloatingActionButton fb= findViewById(R.id.fab);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        postData();
                    }
                });
                if(connected==true){
                    t.start();
                    Toast.makeText(ShowCart.this,"Order Sent",Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    Snackbar.make(v,"Check Internet Connection",Snackbar.LENGTH_SHORT).show();
                }

            }
        });

        try {

            //showing items in cart
            SQLiteDatabase db = helper.getReadableDatabase();

            cursor = db.query("CART",
                    new String[]{"CODE","TYPE","BRANDNAME","GENERIC","COMPANY","PRICE", "MAXORDER", "TOTAL"},
                    null, null, null, null, null);

            //recycler view
            boolean cursorValue = cursor.moveToFirst();

            while(cursorValue) {
                code.add(cursor.getString(0));
                price.add(cursor.getFloat(5));
                maxOrder.add(cursor.getInt(6));
                generic.add(cursor.getString(3));
                totalPrice+=cursor.getFloat(7);
                cursorValue= cursor.moveToNext();
            }

        } catch(SQLiteException e) {
            Toast toast = Toast.makeText(ShowCart.this, "Database Unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        //setting the total value
        TextView textView = findViewById(R.id.showPrice);
        textView.setText(" Rs "+totalPrice);

        if (code.size()==0){
            ImageView image = findViewById(R.id.no_item);
            image.setVisibility(View.VISIBLE);

            RelativeLayout relativeLayout = findViewById(R.id.relative_layout);
            relativeLayout.setVisibility(View.VISIBLE);

            fb.setVisibility(View.INVISIBLE);

            CardView cardView = findViewById(R.id.totalPrice);
            cardView.setVisibility(View.INVISIBLE);
        }

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        // Create adapter passing in the sample user data
        adapter = new CartAdapter(ShowCart.this,code,price,maxOrder,generic);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                goToHome();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        goToHome();
        super.onBackPressed();
    }

    public void goToHome(){
        Intent intent = new Intent(ShowCart.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void postData(){
        sharedPreferences=getSharedPreferences(MYPREFERENCES,Context.MODE_PRIVATE);
        String name = sharedPreferences.getString(Name," ");
        String phone =sharedPreferences.getString(Phone,"");
        String address=sharedPreferences.getString(Address,"");

        String userData = "entry_993656695="+ URLEncoder.encode(name)+"&"+
                "entry_405352747="+ URLEncoder.encode(phone)+"&"+
                "entry_1591763232="+ URLEncoder.encode(address);

        String url = "https://docs.google.com/forms/u/1/d/e/1FAIpQLScZZRFngxsCcNr1dbWFsLL8AsXBjVS_euKsr2zhtuWQM_Zi4A/formResponse";
        HttpRequest mRequest = new HttpRequest();

        String resp = mRequest.sendPost(url,userData);

        boolean cursorValue = cursor.moveToFirst();
        while(cursorValue){
            String data = "entry_993656695="+ URLEncoder.encode(cursor.getString(0))+"&"+
                    "entry_405352747="+ URLEncoder.encode(cursor.getString(1))+"&"+
                    "entry_1591763232="+ URLEncoder.encode(cursor.getString(2))+"&"+
                    "entry_995971312="+ URLEncoder.encode(cursor.getString(3))+"&"+
                    "entry_167376227="+ URLEncoder.encode(cursor.getString(4))+"&"+
                    "entry_397157865="+ URLEncoder.encode(cursor.getString(4))+"&"+
                    "entry_1330510750="+ URLEncoder.encode(String.valueOf(cursor.getFloat(5)))+"&"+
                    "entry_326885020="+ URLEncoder.encode(String.valueOf(cursor.getFloat(5)))+"&"+
                    "entry_1732604557="+ URLEncoder.encode(String.valueOf(cursor.getInt(6)))+"&"+
                    "entry_1281130329="+URLEncoder.encode(String.valueOf(cursor.getFloat(7)));

            String mResponse = mRequest.sendPost(url,data);
            if (mResponse!=null){
                //delete cart
                SQLiteDatabase db = helper.getReadableDatabase();
                db.delete("CART",null,null);
            }
            cursorValue=cursor.moveToNext();
        }

        String totPrice = "entry_397157865="+ URLEncoder.encode("Total")+"&"+
                "entry_1281130329="+URLEncoder.encode(String.valueOf(totalPrice));
        String resp2 = mRequest.sendPost(url,totPrice);

        String data = "entry_993656695="+ URLEncoder.encode(" ")+"&"+
                "entry_405352747="+ URLEncoder.encode(" ")+"&"+
                "entry_1591763232="+ URLEncoder.encode(" ")+"&"+
                "entry_995971312="+ URLEncoder.encode(" ")+"&"+
                "entry_167376227="+ URLEncoder.encode(" ")+"&"+
                "entry_397157865="+ URLEncoder.encode(" ")+"&"+
                "entry_1330510750="+ URLEncoder.encode(" ")+"&"+
                "entry_326885020="+ URLEncoder.encode(" ")+"&"+
                "entry_1732604557="+ URLEncoder.encode(" ")+"&"+
                "entry_1281130329="+URLEncoder.encode(" ");

        String mResponse = mRequest.sendPost(url,data);
    }
}
