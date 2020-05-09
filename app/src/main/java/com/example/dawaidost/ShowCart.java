package com.example.dawaidost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;

public class ShowCart extends AppCompatActivity {

    SQLiteOpenHelper helper = new Database(ShowCart.this);
    ArrayList<String> code = new ArrayList<>();
    ArrayList<String> type = new ArrayList<>();
    ArrayList<String> brand = new ArrayList<>();
    ArrayList<String> generic = new ArrayList<>();
    public CartAdapter adapter;
    String address, ip;
    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_cart);
        setTitle("My Cart");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //getting ip address
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        address = info.getMacAddress();
        ip = Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());

        //onclick add medicine
/*        Button button = findViewById(R.id.add_medicine);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowCart.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });*/


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

        //floating action button to send mail
        FloatingActionButton fb= findViewById(R.id.fab);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(ShowCart.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ShowCart.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
                }else{

                    //check internet connection
                    boolean connected = false;
                    ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                    if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                        connected = true;
                    }
                    else{
                        connected = false;
                    }

                    if (!connected){
                        Toast.makeText(ShowCart.this, "No Internet Connection",Toast.LENGTH_SHORT).show();
                    }else{
                        //export the database into csv file
                        try {
                            exportTheDB();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //code to send mail
                        sendmail();

                        //Toast.makeText(ShowCart.this,"Send Mail",Toast.LENGTH_SHORT).show();
                        //delete all items from cart after sending mail
                        SQLiteDatabase db = helper.getReadableDatabase();
                        db.delete("CART",null,null);
                    }
                }
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

/*
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

            builder.create().show();*/
            ImageView image = findViewById(R.id.no_item);
            image.setVisibility(View.VISIBLE);

            RelativeLayout relativeLayout = findViewById(R.id.relative_layout);
            relativeLayout.setVisibility(View.VISIBLE);

            fb.setVisibility(View.INVISIBLE);
        }

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // Create adapter passing in the sample user data
        adapter = new CartAdapter(ShowCart.this,code,type,brand,generic);

        // Set layout manager to position the items
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Attach the adapter to the recyclerview to populate items
        recyclerView.setAdapter(adapter);

    }

    private void exportTheDB() throws IOException
    {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("CART",
                new String[] {"CODE","TYPE","BRANDNAME","GENERIC","COMPANY","PRICE", "MAXORDER"},
                null,null,null,null,null);

        File myFile;
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DawaiDost/";
        Log.d("paths",path);

        try {
            boolean mFile = new File(path).mkdir();
            Log.d("direct",String.valueOf(mFile));

            myFile = new File(path+"order.csv");
            Log.d("path", String.valueOf(myFile));
            //myFile.createNewFile();

            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

            //giving the column names
            myOutWriter.append("CODE,TYPE,BRAND NAME,GENERIC,COMPANY,PRICE,QUANTITY,IP Address,"+ip+",MAC Address,"+address);
            myOutWriter.append("\n");

            boolean cursorValue= cursor.moveToFirst();

            while(cursorValue){

                myOutWriter.append(cursor.getString(0));
                myOutWriter.append(",");
                myOutWriter.append(cursor.getString(1));
                myOutWriter.append(",");
                myOutWriter.append(cursor.getString(2));
                myOutWriter.append(",");
                myOutWriter.append(cursor.getString(3));
                myOutWriter.append(",");
                myOutWriter.append(cursor.getString(4));
                myOutWriter.append(",");
                myOutWriter.append(cursor.getString(5));
                myOutWriter.append(",");
                myOutWriter.append(cursor.getString(6));

                myOutWriter.append("\n");

                cursorValue = cursor.moveToNext();
            }

            myOutWriter.close();
            fOut.close();
            cursor.close();

        } catch (SQLiteException e) {
            Log.e("okas","Could not create or Open the database");
        }

    }

    //sending mail
    public void sendmail(){
        String filename = "order.csv";
        File filelocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/DawaiDost/", filename);
        Uri uri = FileProvider.getUriForFile(ShowCart.this,BuildConfig.APPLICATION_ID+".provider",filelocation);
        Log.d("path", String.valueOf(filelocation));

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("vnd.android.cursor.dir/email");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"minusarraf96@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Order");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
            finish();
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
