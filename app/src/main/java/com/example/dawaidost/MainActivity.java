package com.example.dawaidost;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LauncherActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.mancj.materialsearchbar.MaterialSearchBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;

    SQLiteOpenHelper helper = new Database(MainActivity.this);
    SQLiteDatabase db;
    RecyclerView recyclerView;
    ProgressBar dawaiLoadingDialog;
    JSONObject mResponse;
    static boolean synced = false;
    DrawerLayout drawer;
    private static final int CAMERA_REQUEST = 1888;

    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Home (Dawai Dost)");


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


        //getdata for the first boot
        boolean firstBoot = getSharedPreferences("BOOT_PREF",MODE_PRIVATE).getBoolean("firstBoot",true);
        if(firstBoot){

            //show no connection page
            if (connected==false){
                Intent intent = new Intent(MainActivity.this,NoConnection.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else{
                //Show progressbar while volley request is serviced
                dawaiLoadingDialog = new ProgressBar(this,null,android.R.attr.progressBarStyleLargeInverse);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                ((FrameLayout)getWindow().getDecorView().findViewById(android.R.id.content)).addView(dawaiLoadingDialog,params);
                dawaiLoadingDialog.setVisibility(View.VISIBLE);  //To show ProgressBar
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                GetData getData = new GetData();
                getData.execute("hello");


                getSharedPreferences("BOOT_PREF",MODE_PRIVATE)
                        .edit()
                        .putBoolean("firstBoot",false)
                        .commit();
            }

        }else{
            //sync everytime you open the app
            if (synced==false){
                //Show progressbar while volley request is serviced
                dawaiLoadingDialog = new ProgressBar(this,null,android.R.attr.progressBarStyleLargeInverse);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                ((FrameLayout)getWindow().getDecorView().findViewById(android.R.id.content)).addView(dawaiLoadingDialog,params);
                dawaiLoadingDialog.setVisibility(View.VISIBLE);  //To show ProgressBar
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                GetData getData = new GetData();
                getData.execute("hello");
                synced=!synced;
            }
        }


/*        //on click floating action button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //show items on cart
                Intent intent =new Intent(MainActivity.this,ShowCart.class);
                startActivity(intent);
            }
        });*/


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


/*        //navigation view
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,R.id.nav_branches,R.id.nav_cart)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);*/



        //showing search match
        recyclerView = findViewById(R.id.recycler_view1);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);


        //search view and searching data
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                showList(newText);
                return false;
            }
        });

        //upload prescription
        ImageView imageView =  findViewById(R.id.prescription);
        imageView.setClickable(true);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
                }else{
                    Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent,CAMERA_REQUEST);
                }
            }
        });

        //speed dial floating action button
        SpeedDialView speedDialView = findViewById(R.id.speedDial);
        speedDialView.inflate(R.menu.activity_main_drawer);

        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem speedDialActionItem) {
                Intent intent;
                switch (speedDialActionItem.getId()) {
                    case R.id.nav_branches:
                        intent = new Intent(MainActivity.this, Branches.class);
                        startActivity(intent);
                        return false; // true to keep the Speed Dial open
                    case R.id.nav_cart:
                        //show items on cart
                        intent =new Intent(MainActivity.this,ShowCart.class);
                        startActivity(intent);
                        return false;
                    case R.id.nav_logout:
                        onClickLogOut();
                        return false;
                    default:
                        return false;
                }
            }
        });


        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.nav_branches, R.drawable.branches)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.yellow, getTheme()))
                        .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.dark, getTheme()))
                        .setLabel("Branches")
                        .setLabelColor(Color.BLACK)
                        .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.yellow, getTheme()))
                        .setLabelClickable(false)
                        .create()
        );

        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.nav_cart, R.drawable.add_cart)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.yellow, getTheme()))
                        .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.dark, getTheme()))
                        .setLabel("My Cart")
                        .setLabelColor(Color.BLACK)
                        .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.yellow, getTheme()))
                        .setLabelClickable(false)
                        .create()
        );

        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.nav_logout, R.mipmap.logout)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.yellow, getTheme()))
                        .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.dark, getTheme()))
                        .setLabel("Log Out")
                        .setLabelColor(Color.BLACK)
                        .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.yellow, getTheme()))
                        .setLabelClickable(false)
                        .create()
        );
    }

    public void onClickLogOut(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Do you want to log out? You may loose your data!");

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("nameKey"," ");
                editor.putString("phoneKey"," ");
                editor.putString("addresskey"," ");
                editor.commit();

                SQLiteDatabase db = helper.getReadableDatabase();
                db.delete("CART", null, null);

                Intent intent = new Intent(MainActivity.this, LoginPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        dialog.create().show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        File pic = null;
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap theImage=(Bitmap) data.getExtras().get("data");
            try{
                File root = Environment.getExternalStorageDirectory();
                if(root.canWrite()){
                    pic=new File(root,"prescription.png");
                    FileOutputStream out = new FileOutputStream(pic);
                    theImage.compress(Bitmap.CompressFormat.PNG,100,out);
                    out.flush();
                    out.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Uri uri = FileProvider.getUriForFile(this,BuildConfig.APPLICATION_ID+".provider",pic);

            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("vnd.android.cursor.dir/email");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"minusarraf96@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Prescription");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //show the matched list of dawais
    public void showList(String mText){

        int searchLength=3;

        //extracting data from database
        db=helper.getReadableDatabase();
        Cursor cursor = db.query("DAWAI",
                new String[] {"CODE","TYPE","BRANDNAME","GENERIC"},
                null,null,null,null,null);

        if(mText.length()<searchLength){   //when search length is not enough
            ArrayList<String> nothing = new ArrayList<>();
            CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(),nothing,nothing,nothing,nothing);
            recyclerView.setAdapter(customAdapter);

            LinearLayout relativeLayout = findViewById(R.id.relativeLayout);
            relativeLayout.setVisibility(View.VISIBLE);
        }else{
            //search length is reached.

            LinearLayout relativeLayout = findViewById(R.id.relativeLayout);
            relativeLayout.setVisibility(View.INVISIBLE);

            ArrayList<String> code = new ArrayList<>();
            ArrayList<String> type = new ArrayList<>();
            ArrayList<String> brand = new ArrayList<>();
            ArrayList<String> generic = new ArrayList<>();

            boolean cursorValue = cursor.moveToFirst();
            String c,t,b,g;
            while(cursorValue){
                c=cursor.getString(0);
                t=cursor.getString(1);
                b=cursor.getString(2);
                g=cursor.getString(3);
                String sText=mText.toUpperCase(Locale.ENGLISH);
                if (c.contains(sText) || t.contains(sText)|| b.contains(sText) || g.contains(sText)){
                    code.add(cursor.getString(0));
                    type.add(cursor.getString(1));
                    brand.add(cursor.getString(2));
                    generic.add(cursor.getString(3));
                }
                cursorValue=cursor.moveToNext();
            }

            //populating recycler view
            CustomAdapter customAdapter = new CustomAdapter(MainActivity.this,code,type,brand,generic);
            recyclerView.setAdapter(customAdapter);

        }
    }

 /*   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id= item.getItemId();
        if(id == R.id.branches){
            Intent intent = new Intent(MainActivity.this, Branches.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

/*    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }*/

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public void saveToDB(JSONObject mResponse){
        //saving data from google sheet into database
        try {
            JSONArray array = mResponse.getJSONArray("Sheet1");
            int totalData = array.length();
            JSONObject finalObject;
            db=helper.getReadableDatabase();

            int count=0;
            while(count<totalData){
                try{
                    //extracting data from json response
                    finalObject=array.getJSONObject(count);
                    //Log.e("finObj",finalObject.toString());

                    String code = String.valueOf(finalObject.get("CODE"));
                    String type = String.valueOf(finalObject.get("TYPE"));
                    String bName = String.valueOf(finalObject.get("BRAND_NAME"));
                    String generic = String.valueOf(finalObject.get("GENERIC"));
                    String company = String.valueOf(finalObject.get("COMPANY"));
                    String packing = String.valueOf(finalObject.get("PACKING"));
                    Float mrp = Float.valueOf((finalObject.get("MRP").toString()));
                    Float price = Float.valueOf(String.valueOf(finalObject.get("PRICE")));
                    Integer maxOrder = Integer.valueOf(String.valueOf(finalObject.get("MAXORDER")));
                    Log.d("print",code);

                    //inserting into table
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("CODE",code);
                    contentValues.put("TYPE",type);
                    contentValues.put("GENERIC",generic);
                    contentValues.put("BRANDNAME",bName);
                    contentValues.put("COMPANY",company);
                    contentValues.put("PACKING",packing);
                    contentValues.put("MRP",mrp);
                    contentValues.put("PRICE",price);
                    contentValues.put("MAXORDER",maxOrder);
                    db.insert("DAWAI",null,contentValues);

                    Log.d("print","saved");
                }catch(JSONException e){
                    e.printStackTrace();
                }catch(SQLiteException e){
                    Toast.makeText(this,"Database Unavailable",Toast.LENGTH_SHORT).show();
                }
                count += 1;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id= item.getItemId();
        Log.d("Clicked",String.valueOf(id));
        if(id==R.id.nav_branches){
            Toast.makeText(this,"branches",Toast.LENGTH_SHORT).show();
        }
        return true;
    }


    public class GetData extends AsyncTask<String, Integer, JSONObject> {
        //get request to a google sheet
        private JSONObject finalResponse = new JSONObject();
        ProgressDialog progressDialog;

        @Override
        protected JSONObject doInBackground(String... voids) {
            publishProgress(5);

            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
            //url of the google sheet
            //it should be kept in separate file at one place
            final String url = "https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec?id=1Otp-z4nZshefjvji-tBIKqyG6fO74G6sXZ9TfmMlQfI&sheet=Sheet1";
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray arr = response.getJSONArray("Sheet1");

                                //progressbar for syncing dawai
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                dawaiLoadingDialog.setVisibility(View.GONE);

                                db=helper.getReadableDatabase();
                                db.delete("DAWAI",null,null);

                                saveToDB(response);

                            } catch (JSONException e) {
                                Log.d("Error.Response", "fail");
                                e.printStackTrace();
                            }
                            finalResponse= response;

                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            dawaiLoadingDialog.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this,"New Data not updated",Toast.LENGTH_SHORT).show();
                            Log.d("Error.Response", String.valueOf(error));
                        }
                    }
            );
            queue.add(getRequest);
            mResponse=finalResponse;
            return finalResponse;
        }

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }


}
