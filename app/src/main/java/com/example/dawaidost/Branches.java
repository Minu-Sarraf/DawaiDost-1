package com.example.dawaidost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.sheets.v4.Sheets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

public class Branches extends AppCompatActivity {
    ArrayList<String> branchName= new ArrayList<>();
    ArrayList<String> openingTime = new ArrayList<>();
    ArrayList<String> mapLink = new ArrayList<>();

    ProgressBar branchDialog;

    BranchAdapter adapter;

    SQLiteOpenHelper helper = new Database(this);
    SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branches);
        setTitle("Branches");

        db = helper.getReadableDatabase();

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //backbutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Show progressbar while volley request is serviced
        branchDialog = new ProgressBar(this,null,android.R.attr.progressBarStyleLargeInverse);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        ((FrameLayout)getWindow().getDecorView().findViewById(android.R.id.content)).addView(branchDialog,params);
        branchDialog.setVisibility(View.VISIBLE);  //To show ProgressBar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        Branches.GetData getData = new Branches.GetData();
        getData.execute("hello");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadDatabase(){
        Cursor cursor = db.query("BRANCHES",
                new String[] {"LOCATION","LINK","OPENTIME"},
                null,null,null,null,null);
        boolean cursorValue= cursor.moveToFirst();
        while(cursorValue){
            branchName.add(cursor.getString(0));
            mapLink.add(cursor.getString(1));
            openingTime.add(cursor.getString(2));

            cursorValue= cursor.moveToNext();
        }

        cursor.close();

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        adapter = new BranchAdapter(Branches.this,branchName,mapLink,openingTime);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    public void storeBranches(JSONObject response){
        try {
            JSONArray array = response.getJSONArray("BRANCHES");
            int totalData = array.length()-1;
            JSONObject finalObject;

            int count=0;
            while(count<totalData){
                try{
                    //extracting data from json response
                    finalObject=array.getJSONObject(count);

                    String location = String.valueOf(finalObject.get("Location"));
                    String link =String.valueOf(finalObject.get("Link"));
                    String time =String.valueOf(finalObject.get("Opening_Hours"));

                    ContentValues contentValues = new ContentValues();
                    contentValues.put("LOCATION",location);
                    contentValues.put("LINK",link);
                    contentValues.put("OPENTIME",time);

                    db.insert("BRANCHES",null,contentValues);

                    branchName.add(location);
                    mapLink.add(link);
                    openingTime.add(time);

                }catch(JSONException e){
                    e.printStackTrace();
                }
                count += 1;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        adapter = new BranchAdapter(Branches.this,branchName,mapLink,openingTime);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    public class GetData extends AsyncTask<String, Integer, JSONObject> {
        //get request to a google sheet
        private JSONObject finalResponse = new JSONObject();

        @Override
        protected JSONObject doInBackground(String... voids) {
            publishProgress(5);

            RequestQueue queue = Volley.newRequestQueue(Branches.this);
            //url of the google sheet
            //it should be kept in separate file at one place
            final String url = "https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec?id=1XcU1TbA56-JNM0Qsj9ihyt3mgzFGVWeHFFIUn-7_4wM&sheet=BRANCHES";
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray arr = response.getJSONArray("BRANCHES");

                                //progressbar for syncing dawai
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                branchDialog.setVisibility(View.GONE);

                                db.delete("BRANCHES",null,null);

                                storeBranches(response);

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
                            branchDialog.setVisibility(View.GONE);
                            Toast.makeText(Branches.this,"Check Your Connection to Fetch Latest Data",Toast.LENGTH_SHORT).show();
                            Log.d("Error.Response", String.valueOf(error));

                            loadDatabase();
                        }
                    }
            );
            queue.add(getRequest);
            //mResponse=finalResponse;
            return finalResponse;
        }

    }
}
