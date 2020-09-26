package com.example.ddost;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetUserDetails extends AsyncTask<Void, Void, Void> {
    @SuppressLint("StaticFieldLeak")
    private Context context;
    private ProgressDialog dialog;
    private String mResponse;
    private String phoneNumber;
    @SuppressLint("StaticFieldLeak")
    private Activity activity;
    private String name;

    GetUserDetails(Context context, String phoneNumber, Activity activity, String name){
        this.context=context;
        this.phoneNumber=phoneNumber;
        this.activity= activity;
        this.name = name;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setTitle("Please Wait...");
        dialog.setMessage("Getting your details");
        dialog.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        HttpRequest mRequest = new HttpRequest();
        String url = "https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec?id=1NRluNvYf9BeECtLVRZFnO1CENYm_4wmZWrjWHoEb8uc&sheet=Cart";
        mResponse=mRequest.sendGet(url);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        dialog.dismiss();
        if(mResponse!=null){
            saveCart(mResponse);
            Intent intent = new Intent(context,MainActivity.class);
            intent.putExtra("LOGIN","login");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            activity.finish();
            activity.overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
            Toast.makeText(context,"Welcome "+name,Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(context,"Error Fetching Data",Toast.LENGTH_SHORT).show();
        }

    }

    private void saveCart(String mResponse){
        SQLiteOpenHelper helper = new Database(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        JSONObject cartDetails = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(mResponse);
            JSONArray array = jsonObject.getJSONArray("Cart");
            JSONObject response;
            int count = 0;
            while(count<array.length()){
                response=array.getJSONObject(count);
                String userNumber = response.getString("UserNumber");
                if(phoneNumber.equals(userNumber)){
                    cartDetails=response;
                }
                count++;
            }
            int number = 1;

            ContentValues contentValues = new ContentValues();
            while(number<=10){
                String code = "Code"+number;
                String qty = "Qty"+number;
                String codeValue = cartDetails.getString(code);
                Integer qtyValue = cartDetails.getInt(qty);
                if(codeValue.length()==0){
                    return;
                }
                contentValues.put("CODE",codeValue);
                contentValues.put("MAXORDER",qtyValue);
                db.insert("CART",null,contentValues);

                number++;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

