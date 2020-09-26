package com.example.ddost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.net.URLEncoder;

public class SendQuery extends AppCompatActivity {
    EditText userQuery;
    String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_query);

        setTitle("Send Query");

        //backbutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userQuery= findViewById(R.id.userQuery);

        Button button = findViewById(R.id.buttonSend);
        button.setOnClickListener(new View.OnClickListener() {
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

                query = userQuery.getText().toString();
                if(query.length()==0){
                    userQuery.setError("Please type your query!");
                }else{
                    sendData();
                    Toast.makeText(SendQuery.this,"Query Sent",Toast.LENGTH_SHORT).show();

                    finish();
                    overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                }
            }
        });

    }

    public void sendData(){
        SharedPreferencesValue sharedPreferencesValue = new SharedPreferencesValue(getApplicationContext());
        sharedPreferencesValue.setSharedPreferences();

        String url = "https://docs.google.com/forms/u/3/d/e/1FAIpQLSe76rGrKrk-u6813xBYziJBBCVcTYGRZmjGta4tlzvOLy4fXg/formResponse";
        String data = "entry_41147933="+ URLEncoder.encode(sharedPreferencesValue.getPhone())+"&"+
                "entry_637105799="+ URLEncoder.encode(query);

        SendSheet sendSheet = new SendSheet(getApplicationContext(),url,data);
        sendSheet.execute();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:

                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
