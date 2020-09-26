package com.example.ddost;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetData extends AsyncTask<String, Integer, JSONObject> {
    private JSONObject finalResponse = new JSONObject();
    @SuppressLint("StaticFieldLeak")
    private Context context;
    @SuppressLint("StaticFieldLeak")
    private ProgressBar dawaiLoadingDialog;
    @SuppressLint("StaticFieldLeak")
    private Activity activity;

    private SQLiteOpenHelper helper;
    private SQLiteDatabase db;
    private String url;
    private String dataType;

    public GetData(Context context, Activity activity, String url, String dataType){
        this.context = context;
        this.activity = activity;
        this.url = url;
        this.dataType= dataType;
    }


    @Override
    protected JSONObject doInBackground(String... voids) {

        RequestQueue queue = Volley.newRequestQueue(context);
        //url of the google sheet
        //it should be kept in separate file at one place
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {

                        //progressbar for syncing dawai
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        dawaiLoadingDialog.setVisibility(View.GONE);

                        helper= new Database(context);
                        db=helper.getReadableDatabase();

                        Cursor cursor = db.query("CART",
                                new String[]{"CODE"},
                                null,null,null,null,null);


                        switch (dataType){
                            case "Medicine":
                                db.delete("DAWAI",null,null);
                                saveMedicine(response);
                                break;
                            case "Branches":
                                db.delete("BRANCHES",null,null);
                                saveBranches(response);
                                break;
                            case "login":
                                db.delete("DAWAI",null,null);
                                saveMedicine(response);
                                if(cursor.moveToFirst()){
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Check cart!");
                                    builder.setMessage("You have items on cart!");
                                    builder.setPositiveButton("View Cart", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            Intent intent = new Intent(context,ShowCart.class);
                                            context.startActivity(intent);
                                        }
                                    });
                                    builder.setNegativeButton("Clear Cart", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            db.delete("CART",null,null);
                                            new CartDeleteUpdate.DeleteDataActivity(context).execute();
                                            Toast.makeText(context,"Cart Cleared",Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    builder.create().show();
                                }
                        }
                        cursor.close();
                        finalResponse= response;

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        dawaiLoadingDialog.setVisibility(View.GONE);
                        Toast.makeText(context,"Check Your Connection to Fetch Latest Data",Toast.LENGTH_SHORT).show();
                        Log.d("Error.Response", String.valueOf(error));
                    }
                }
        );
        queue.add(getRequest);
        return finalResponse;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
    }

    public void showProgressBar(){
        dawaiLoadingDialog = new ProgressBar(context,null,android.R.attr.progressBarStyleLargeInverse);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        ((FrameLayout)activity.getWindow().getDecorView().findViewById(android.R.id.content)).addView(dawaiLoadingDialog,params);
        dawaiLoadingDialog.setVisibility(View.VISIBLE);  //To show ProgressBar
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void hideProgressBar(){
        //progressbar for syncing dawai
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        dawaiLoadingDialog.setVisibility(View.GONE);
    }

    private void saveMedicine(JSONObject mResponse){
        //saving data from google sheet into database
        try {
            JSONArray array = mResponse.getJSONArray("Med");
            int totalData = array.length();
            JSONObject finalObject;
            db=helper.getReadableDatabase();

            int count=0;
            while(count<totalData){
                try{
                    //extracting data from json response
                    finalObject=array.getJSONObject(count);

                    String code = String.valueOf(finalObject.get("CODE"));
                    String type = String.valueOf(finalObject.get("TYPE"));
                    String bName = String.valueOf(finalObject.get("BRAND_NAME"));
                    String generic = String.valueOf(finalObject.get("GENERIC"));
                    String company = String.valueOf(finalObject.get("COMPANY"));
                    String packing = String.valueOf(finalObject.get("PACKING"));
                    Float mrp = Float.valueOf((finalObject.get("MRP").toString()));
                    Float price = Float.valueOf(String.valueOf(finalObject.get("PRICE")));
                    Integer maxOrder = Integer.valueOf(String.valueOf(finalObject.get("MAXORDER")));
                    Integer prescription = (Integer) finalObject.get("PRESCRIPTION");
                    String savings = String.valueOf(finalObject.get("SAVINGS"));

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
                    contentValues.put("PRESCRIPTION",prescription);
                    contentValues.put("SAVINGS",savings);
                    db.insert("DAWAI",null,contentValues);
                }catch(JSONException e){
                    e.printStackTrace();
                }catch(SQLiteException e){
                    Toast.makeText(context,"Database Unavailable",Toast.LENGTH_SHORT).show();
                }
                count += 1;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void saveBranches(JSONObject response){
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

                }catch(JSONException e){
                    e.printStackTrace();
                }
                count += 1;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
