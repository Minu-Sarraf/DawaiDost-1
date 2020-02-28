package com.example.dawaidost;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private ListView listView;
    private Button btn;
    JSONObject mResponse;
    SQLiteOpenHelper helper = new Database(MainActivity.this);
    SQLiteDatabase db;
    SearchView editSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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

        listView = findViewById(R.id.listView);
        btn= findViewById(R.id.getJson);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetData getData = new GetData();
                getData.execute("hello");
            }
        });


        editSearch = findViewById(R.id.search_items);
        editSearch.setOnQueryTextListener(this);

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        db=helper.getReadableDatabase();
        Cursor cursor = db.query("DAWAI",
                new String[] {"CODE","TYPE","BRANDNAME","GENERIC"},
                null,null,null,null,null);

        if(newText.length()<3){
            ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,
                    new String[] {});
            listView.setAdapter(listAdapter);
        }else{
            ArrayList<String> matchDawai = new ArrayList<>();
            boolean cursorValue = cursor.moveToFirst();
            while(cursorValue){
                if (cursor.getString(3).contains(newText.toUpperCase(Locale.ENGLISH))){
                    matchDawai.add(cursor.getString(1));
                }
                cursorValue=cursor.moveToNext();
            }
            ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,
                    matchDawai);
            listView.setAdapter(listAdapter);
        }
        return false;
    }

    public void saveToDB(JSONObject mResponse){
        Log.d("checking",mResponse.toString());

        try {
            JSONArray array = mResponse.getJSONArray("Sheet1");
            int totalData = array.length();
            JSONObject finalObject;
            db=helper.getReadableDatabase();

            Log.d("hello", String.valueOf(totalData));

            //Log.d("genDon", String.valueOf(array.getJSONObject(0).get("CODE")));
            //finalObject=array.getJSONObject(0);
            //Log.d("genDon", String.valueOf(finalObject.get("CODE")));

            int count=0;

            while(count<totalData){

                Log.d("print","1");

                try{
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

        private JSONObject finalResponse = new JSONObject();
        ProgressDialog progressDialog;

        @Override
        protected JSONObject doInBackground(String... voids) {
            publishProgress(5);

            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

            final String url = "https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec?id=1Otp-z4nZshefjvji-tBIKqyG6fO74G6sXZ9TfmMlQfI&sheet=Sheet1";

            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            // display response
                            //                        Log.d("Response", response.toString());
                            try {
                                JSONArray arr = response.getJSONArray("Sheet1");
                                //Log.d("sizeArray", String.valueOf(arr.length()));
                                //Log.d("Array", String.valueOf(arr.getJSONObject(0).get("CODE")));
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
                            Log.d("Error.Response", String.valueOf(error));
                        }
                    }
            );

            queue.add(getRequest);

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return finalResponse;
        }

        @Override
        protected void onPreExecute(){
            progressDialog= ProgressDialog.show(MainActivity.this,"ProgressDialog","Wait");
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
