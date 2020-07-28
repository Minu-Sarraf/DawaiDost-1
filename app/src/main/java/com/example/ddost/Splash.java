package com.example.ddost;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Splash extends Activity {

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 2000;
    String name;
    Intent intent;
    Thread t;
    String mResponse;
    JSONObject response;
    JSONArray images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //direct to Login Page
        SharedPreferencesValue sharedPreferencesValue = new SharedPreferencesValue(getApplicationContext());
        sharedPreferencesValue.setSharedPreferences();
        name = sharedPreferencesValue.getName();

        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        }

        t = new Thread(new Runnable() {
            @Override
            public void run() {
                getImages();
            }
        });
        t.start();

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        final boolean finalConnected = connected;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                if(!finalConnected){
                    intent = new Intent(Splash.this,NoConnection.class);
                }else if(name.equals(" ")){
                    intent = new Intent(Splash.this,LoginPage.class);
                }else{
                    intent = new Intent(Splash.this,MainActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    public void getImages(){
        String url = "https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec?id=1XcU1TbA56-JNM0Qsj9ihyt3mgzFGVWeHFFIUn-7_4wM&sheet=images";

        HttpRequest mRequest = new HttpRequest();
        mResponse=mRequest.sendGet(url);
        if(mResponse!=null){
            Log.d("data",mResponse);
            try {
                response = new JSONObject(mResponse);
                images=response.getJSONArray("images");
                int count =0;
                JSONObject finalObject;

                SQLiteOpenHelper helper = new Database(this);
                SQLiteDatabase db = helper.getReadableDatabase();
                db.delete("IMAGES",null,null);
                while(count<images.length()){
                    finalObject=images.getJSONObject(count);
                    String home= String.valueOf(finalObject.get("HomePage"));
                    String cart= String.valueOf(finalObject.get("Cart"));
                    String benefits= String.valueOf(finalObject.get("Benefits"));

                    ContentValues contentValues= new ContentValues();
                    contentValues.put("HOME",home);
                    contentValues.put("CART",cart);
                    contentValues.put("BENEFITS",benefits);
                    db.insert("IMAGES",null,contentValues);
                    Log.d("saved","saved");
                    count++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
