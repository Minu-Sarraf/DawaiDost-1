package com.example.ddost;

import android.util.Log;

import androidx.annotation.NonNull;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ControllerCart {
    public static final String TAG = "TAG";

    public static final String WAURL="https://script.google.com/macros/s/AKfycbz9gHp10siw2sbq2fCgmqK6EUGhrerWu6pwL0fq-1x4VVF3xM8/exec?";
    // EG : https://script.google.com/macros/s/AKfycbwXXXXXXXXXXXXXXXXX/exec?
//Make Sure '?' Mark is present at the end of URL

    private static Response response;

    public static JSONObject readAllData() {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(WAURL+"action=readAll")
                    .build();
            response = client.newCall(request).execute();
            return new JSONObject(response.body().string());
        } catch (@NonNull IOException | JSONException e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
        return null;
    }


    public static JSONObject insertData(String id, String name) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(WAURL+"action=insert&id="+id+"&name="+name)
                    .build();
            response = client.newCall(request).execute();
            //    Log.e(TAG,"response from gs"+response.body().string());
            return new JSONObject(response.body().string());


        } catch (@NonNull IOException | JSONException e) {
            Log.e(TAG, "recieving null " + e.getLocalizedMessage());
        }
        return null;
    }

    public static JSONObject updateData(String id, String name) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(WAURL+"action=update&id="+id+"&name="+name)
                    .build();
            response = client.newCall(request).execute();
            //    Log.e(TAG,"response from gs"+response.body().string());
            return new JSONObject(response.body().string());


        } catch (@NonNull IOException | JSONException e) {
            Log.e(TAG, "recieving null " + e.getLocalizedMessage());
        }
        return null;
    }

    public static JSONObject readData(String id) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(WAURL+"action=read&id="+id)
                    .build();
            response = client.newCall(request).execute();
            // Log.e(TAG,"response from gs"+response.body().string());
            return new JSONObject(response.body().string());


        } catch (@NonNull IOException | JSONException e) {
            Log.e(TAG, "recieving null " + e.getLocalizedMessage());
        }
        return null;
    }

    public static JSONObject deleteData(String id) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(WAURL+"action=delete&id="+id)
                    .build();
            response = client.newCall(request).execute();
            //Log.e(TAG,"response from gs"+response.body().string());
            return new JSONObject(response.body().string());

        } catch (@NonNull IOException | JSONException e) {
            Log.e(TAG, "recieving null " + e.getLocalizedMessage());
        }
        return null;
    }

}
