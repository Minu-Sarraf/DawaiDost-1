package com.example.ddost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

public class IntroduceFriend extends AppCompatActivity {
    EditText friend1,friend2,friend3,friend4,friend5;
    EditText phone1,phone2,phone3,phone4,phone5;
    String data;
    String friendName, phoneNumber;
    EditText[] friendTexts;
    EditText[] phoneTexts;
    String[] entry;

    ConstraintLayout constraintLayout;
    GetData getData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduce_friend);
        setTitle("Introduce Your Friends");

        //get delivery charge
        GetDelivery getDelivery = new GetDelivery();
        constraintLayout = findViewById(R.id.introduce_friend);
        constraintLayout.setVisibility(View.INVISIBLE);
        String url="";
        getData= new GetData(getApplicationContext(),this,url,"Delivery");
        getData.showProgressBar();
        getDelivery.execute();


        //backbutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView textView = findViewById(R.id.termsConditions);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntroduceFriend.this,LoadWebView.class);
                intent.putExtra("NAME","Terms and Conditions");
                intent.putExtra("URL","https://dawaidost.com/terms-and-conditions/");
                startActivity(intent);
            }
        });

        friend1=findViewById(R.id.friendName1);
        friend2=findViewById(R.id.friendName2);
        friend3=findViewById(R.id.friendName3);
        friend4=findViewById(R.id.friendName4);
        friend5=findViewById(R.id.friendName5);
        phone1=findViewById(R.id.phoneNumber1);
        phone2=findViewById(R.id.phoneNumber2);
        phone3=findViewById(R.id.phoneNumber3);
        phone4=findViewById(R.id.phoneNumber4);
        phone5=findViewById(R.id.phoneNumber5);

        friendTexts=new EditText[]{friend1,friend2,friend3,friend4,friend5};
        phoneTexts= new EditText[]{phone1,phone2,phone3,phone4,phone5};
        entry= new String[]{
          "entry_1477506104=","entry_752538567=","entry_1855380469=","entry_531424797=","entry_1792342388=",
                "entry_535366333=","entry_773988292=","entry_444526097=","entry_518570938=","entry_647669898="
        };

        final SharedPreferencesValue sharedPreferencesValue = new SharedPreferencesValue(getApplicationContext());
        sharedPreferencesValue.setSharedPreferences();

        Button buttonSend = findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean connected=false;
                ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    connected = true;
                }
                if(!connected){
                    Snackbar.make(v,"No Internet Connection",Snackbar.LENGTH_SHORT).show();
                    return;
                }

                data = "entry_1826224696="+ URLEncoder.encode(sharedPreferencesValue.getPhone());
                validateData();
            }
        });
    }

    public void validateData(){
        boolean send=true;
        int count=0;
        while(count<(friendTexts.length)){
            friendName = friendTexts[count].getText().toString();
            phoneNumber=phoneTexts[count].getText().toString();
            if(friendName.length()!=0){
                if(phoneNumber.length()!=10){
                    phoneTexts[count].setError("Please enter a valid phone number");
                    data="";
                    send=false;
                }else{
                    data=data+"&"+entry[2*count]+URLEncoder.encode(friendName)+"&"+entry[2*count+1]+URLEncoder.encode(phoneNumber);
                }
            }else{
                break;
            }
            count++;
        }
        if(send){
            postData();
        }
    }

    public void postData(){
        String url ="https://docs.google.com/forms/u/3/d/e/1FAIpQLSf_xQqaVcad2h5myuLKq3Kd4ad5YjBWBrOddmxJ6MPejfj_oA/formResponse";

        SendSheet sendSheet = new SendSheet(getApplicationContext(),url,data);
        sendSheet.execute();
        Toast.makeText(IntroduceFriend.this,"Data Sent",Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();

        }
        return super.onOptionsItemSelected(item);
    }

    public class GetDelivery extends AsyncTask<String, Integer, JSONObject> {
        //get request to a google sheet
        private JSONObject finalResponse = new JSONObject();

        @Override
        protected JSONObject doInBackground(String... voids) {
            publishProgress(5);

            RequestQueue queue = Volley.newRequestQueue(IntroduceFriend.this);
            //url of the google sheet
            //it should be kept in separate file at one place
            final String url = "https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec?id=1XcU1TbA56-JNM0Qsj9ihyt3mgzFGVWeHFFIUn-7_4wM&sheet=DEL";
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray arr = response.getJSONArray("DEL");

                                //progressbar for syncing dawai
                                getData.hideProgressBar();
                                showPrice(response);
                                constraintLayout.setVisibility(View.VISIBLE);

                            } catch (JSONException e) {
                                Log.d("Error.Response", "fail");
                                e.printStackTrace();
                            }
                            finalResponse = response;

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            getData.hideProgressBar();
                            Intent intent = new Intent(IntroduceFriend.this,NoConnection.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(IntroduceFriend.this, "No internet connection", Toast.LENGTH_SHORT).show();
                            Log.d("Error.Response", String.valueOf(error));
                        }
                    }
            );
            queue.add(getRequest);
            //mResponse=finalResponse;
            return finalResponse;
        }
    }

    public void showPrice(JSONObject response){
        try {
            JSONArray array = response.getJSONArray("DEL");
            int totalData = array.length();
            JSONObject finalObject;

            int count=0;
            while(count<totalData){
                try{
                    //extracting data from json response
                    finalObject=array.getJSONObject(count);

                    String deliveryType = String.valueOf(finalObject.get("DELIVERY_TYPE"));
                    String price =String.valueOf(finalObject.get("PRICE"));

                    if(deliveryType.equals("Introduce a Friend")){
                        TextView textView = findViewById(R.id.textRefer);
                        textView.setText("Refer your friend and earn Rs "+price);
                        return;
                    }

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
