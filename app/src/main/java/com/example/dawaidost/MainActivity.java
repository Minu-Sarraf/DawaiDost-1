package com.example.dawaidost;

import android.app.ActionBar;
import android.app.LauncherActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import com.mancj.materialsearchbar.MaterialSearchBar;

import androidx.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.Locale;

//https://www.google.com/maps/place/Dawai+Dost+-+1+-+Upper+Bazaar/@23.376317,85.3170739,15z/data=!4m5!3m4!1s0x0:0xdabe8770a0909b0!8m2!3d23.376317!4d85.3170739


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private AppBarConfiguration mAppBarConfiguration;

    SQLiteOpenHelper helper = new Database(MainActivity.this);
    SQLiteDatabase db;
    private MaterialSearchBar materialSearchBar;
    MaterialSpinner spinner;
    String selectSearch;
    RecyclerView recyclerView;
    ProgressBar dawaiLoadingDialog;
    JSONObject mResponse;
    static boolean synced = false;
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        }


        //on click floating action button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //show items on cart
                Intent intent =new Intent(MainActivity.this,ShowCart.class);
                startActivity(intent);
            }
        });

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);



        //showing search match
        recyclerView = findViewById(R.id.recycler_view1);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);



        //material spinner for selecting search type
/*        spinner = findViewById(R.id.spinner);
        spinner.setItems("Code", "Type", "Brand", "Generic");
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                selectSearch= item;
            }
        });*/


        //search  bar
        materialSearchBar = findViewById(R.id.searchBar);
        materialSearchBar.enableSearch();
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                showList();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    public void showList(){

        int searchLength=3;

        //extracting data from database
        db=helper.getReadableDatabase();
        Cursor cursor = db.query("DAWAI",
                new String[] {"CODE","TYPE","BRANDNAME","GENERIC"},
                null,null,null,null,null);

        if(materialSearchBar.getText().length()<searchLength){
            //when search length is not enough
            ArrayList<String> nothing = new ArrayList<>();
            CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(),nothing,nothing,nothing,nothing);
            recyclerView.setAdapter(customAdapter);

            ImageView imageView = findViewById(R.id.home_page);
            imageView.setVisibility(View.VISIBLE);

        }else{
            //search length is reached.

            ImageView imageView = findViewById(R.id.home_page);
            imageView.setVisibility(View.INVISIBLE);

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
                String mText=materialSearchBar.getText().toUpperCase(Locale.ENGLISH);
                if (c.contains(mText) || t.contains(mText)|| b.contains(mText) || g.contains(mText)){
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id= item.getItemId();

/*        if (id==R.id.SyncDawai){

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

            //show no connection page
            if (connected==false){
                Toast.makeText(MainActivity.this,"No Internet Connection",Toast.LENGTH_SHORT).show();
            } else {

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
            }


        }else */if(id == R.id.branches){
            Intent intent = new Intent(MainActivity.this, Branches.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        //when clicked in the drawer
        /*int id = item.getItemId();
        Log.d("showId",String.valueOf(id));

        if(id==R.id.branches){
            Toast.makeText(MainActivity.this, "Branches Selected",Toast.LENGTH_SHORT).show();
        }
        drawer.closeDrawer(GravityCompat.START);*/
        return true;
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

    public class DrawerItemClickListener implements ListView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Log.d("selected","selected");
        }
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

        @Override
        protected void onPreExecute(){
            //show progress dialog
            //progressDialog= ProgressDialog.show(MainActivity.this,"Fetching Data","Wait");
        }

        @Override
        protected void onPostExecute(JSONObject response){
            //progressDialog.dismiss();
           /* mResponse=response;
            Log.d("Response",response.toString());
            saveToDB(mResponse);*/
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }


}
