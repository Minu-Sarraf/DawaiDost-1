package com.example.ddost;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

public class CartDeleteUpdate {
    Context context;
    String phone;

    public CartDeleteUpdate(Context context){
        this.context=context;
    }

    public void sendData(){
        ArrayList<String> code = new ArrayList<>();
        ArrayList<String> qty = new ArrayList<>();

        SharedPreferencesValue sharedPreferencesValue = new SharedPreferencesValue(context);
        sharedPreferencesValue.setSharedPreferences();
        phone = sharedPreferencesValue.getPhone();

        String[] codeEntry = new String[]{"entry_1117078003=", "entry_162454499=", "entry_1568869403=",
                "entry_985284583=", "entry_942382068=", "entry_425545483=", "entry_1776202752=",
                "entry_1181384028=", "entry_604989513=", "entry_1511858555="
        };

        String[] qtyEntry = new String[]{"entry_1914371787=", "entry_2100478333=", "entry_646673213=",
                "entry_920683511=", "entry_1103815504=", "entry_1235854274=", "entry_327556000=",
                "entry_81439086=", "entry_865514189=", "entry_465688282="
        };

        SQLiteOpenHelper helper = new Database(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor =  db.query("CART",
                new String[]{"CODE","TYPE","BRANDNAME","GENERIC","COMPANY","PRICE", "MAXORDER", "TOTAL"},
                null, null, null, null, null);

        boolean cursorValue = cursor.moveToFirst();
        while(cursorValue){

            Cursor cursor1 = db.query("DAWAI",
                    new String[]{"CODE","PRICE","PRESCRIPTION"},
                    "CODE=?",
                    new String[]{cursor.getString(0)},
                    null,null,null);

            if(cursor1.moveToFirst()){
                code.add(cursor.getString(0));
                qty.add(cursor.getString(6));
            }
            cursorValue=cursor.moveToNext();
        }
        cursor.close();

        String url = "https://docs.google.com/forms/u/1/d/e/1FAIpQLSeDBaWEg6TFvaK45j3rFkU6Qkmt6Y9tIO8Ui7pY2omTaYhVgw/formResponse";
        String data = "entry_565191591="+ URLEncoder.encode(phone);

        int i=0;
        while(i<code.size()){
            data = data+"&"+codeEntry[i]+URLEncoder.encode(code.get(i));
            data = data+"&"+qtyEntry[i]+URLEncoder.encode(qty.get(i));
            i++;
        }

        SendSheet sendSheet = new SendSheet(context,url,data);
        sendSheet.execute();
    }

    public static class DeleteDataActivity extends AsyncTask<Void, Void, Void> {

        String result=null;
        Context context;
        String phone;
        ProgressDialog progressDialog;

        public DeleteDataActivity(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            SharedPreferencesValue sharedPreferencesValue = new SharedPreferencesValue(context);
            sharedPreferencesValue.setSharedPreferences();
            phone=sharedPreferencesValue.getPhone();
        }

        @Nullable
        @Override
        protected Void doInBackground(Void... params) {
            Log.i(ControllerCart.TAG,"IDVALUE"+phone);
            JSONObject jsonObject = ControllerCart.deleteData(phone);
            Log.i(ControllerCart.TAG, "Json obj "+jsonObject);

            try {
                /**
                 * Check Whether Its NULL???
                 */
                if (jsonObject != null) {

                    result=jsonObject.getString("result");


                }
            } catch (JSONException je) {
                Log.i(ControllerCart.TAG, "" + je.getLocalizedMessage());
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}
