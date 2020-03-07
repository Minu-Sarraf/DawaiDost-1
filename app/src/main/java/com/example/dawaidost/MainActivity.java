package com.example.dawaidost;

import android.app.LauncherActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.mancj.materialsearchbar.MaterialSearchBar;

import androidx.annotation.NonNull;
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

public class MainActivity extends AppCompatActivity{

    private AppBarConfiguration mAppBarConfiguration;

    JSONObject mResponse;
    SQLiteOpenHelper helper = new Database(MainActivity.this);
    SQLiteDatabase db;
    private MaterialSearchBar materialSearchBar;
    MaterialSpinner spinner;
    String selectSearch;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        //showing search match
        recyclerView = findViewById(R.id.recycler_view);

        //material spinner for selecting search type
        spinner = findViewById(R.id.spinner);
        spinner.setItems("Code", "Type", "Brand", "Generic");
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                selectSearch= item;
            }
        });


        //search  bar
        materialSearchBar = findViewById(R.id.searchBar);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                //search action when selected from spinner
                if (selectSearch==null)
                    selectSearch="Code";

                if(selectSearch.equals("Type")){
                    showList(1,0);
                }else if (selectSearch.equals("Brand")){
                    showList(2,0);
                }else if (selectSearch.equals("Generic")){
                    showList(3,0);
                }else{
                    //search length 3 for code search
                    showList(0,2);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    public void showList(int value, int searchLength ){

        //extracting data from database
        db=helper.getReadableDatabase();
        Cursor cursor = db.query("DAWAI",
                new String[] {"CODE","TYPE","BRANDNAME","GENERIC"},
                null,null,null,null,null);

        if(materialSearchBar.getText().length()<=searchLength){
            //when search length is not enough
            ArrayList<String> nothing = new ArrayList<>();
            CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(),nothing,nothing,nothing,nothing);
            recyclerView.setAdapter(customAdapter);

        }else{
            //search length is reached.
            ArrayList<String> code = new ArrayList<>();
            ArrayList<String> type = new ArrayList<>();
            ArrayList<String> brand = new ArrayList<>();
            ArrayList<String> generic = new ArrayList<>();
            boolean cursorValue = cursor.moveToFirst();
            while(cursorValue){
                if (cursor.getString(value).contains(materialSearchBar.getText().toUpperCase(Locale.ENGLISH))){
                    code.add(cursor.getString(0));
                    type.add(cursor.getString(1));
                    brand.add(cursor.getString(2));
                    generic.add(cursor.getString(3));
                }
                cursorValue=cursor.moveToNext();
            }


            //populating recycler view
            CustomAdapter customAdapter = new CustomAdapter(MainActivity.this,code,type,brand,generic);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);
            RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(MainActivity.this,DividerItemDecoration.VERTICAL);
            recyclerView.addItemDecoration(itemDecoration);
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

        if (id==R.id.SyncDawai){
            db.delete("DAWAI",null,null);
            GetData getData = new GetData();
            getData.execute("SyncDawai");
        }

        return super.onOptionsItemSelected(item);
    }

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
                    Log.d("finObj",finalObject.toString());

                    String code = String.valueOf(finalObject.get("CODE"));
                    String type = String.valueOf(finalObject.get("TYPE"));
                    String bName = String.valueOf(finalObject.get("BRAND_NAME"));
                    String generic = String.valueOf(finalObject.get("GENERIC"));
                    String company = String.valueOf(finalObject.get("COMPANY"));
                    String packing = String.valueOf(finalObject.get("PACKING"));
                    Integer mrp = Integer.parseInt(finalObject.get("MRP").toString());
                    Integer price = Integer.valueOf(String.valueOf(finalObject.get("PRICE")));
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

    public class GetData extends AsyncTask<String, Integer, JSONObject> {

        //get request to a google sheet

        private JSONObject finalResponse = new JSONObject();
        ProgressDialog progressDialog;

        @Override
        protected JSONObject doInBackground(String... voids) {
            publishProgress(5);

            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

            //url of the google sheet
            final String url = "https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec?id=1Otp-z4nZshefjvji-tBIKqyG6fO74G6sXZ9TfmMlQfI&sheet=Sheet1";

            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray arr = response.getJSONArray("Sheet1");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            finalResponse= response;
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(MainActivity.this,"Sync not Completed. Try Again",Toast.LENGTH_SHORT).show();
                            Log.d("Error.Response", String.valueOf(error));
                        }
                    }
            );

            queue.add(getRequest);

            try {
                //wait time for json response
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return finalResponse;
        }

        @Override
        protected void onPreExecute(){
            //show progress dialog
            progressDialog= ProgressDialog.show(MainActivity.this,"Fetching Data","Wait");
        }

        @Override
        protected void onPostExecute(JSONObject response){
            progressDialog.dismiss();
            mResponse=response;
            Log.d("Response",response.toString());
            saveToDB(mResponse);
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
