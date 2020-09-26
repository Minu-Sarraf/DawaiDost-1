package com.example.ddost;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NoConnection extends AppCompatActivity {

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_connection);
        setTitle("No Connection");

        button = findViewById(R.id.refresh);
        final ProgressDialog dialog = new ProgressDialog(NoConnection.this);
        dialog.setMessage("Connecting...");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dialog.show();
                //check internet connection
                boolean connected = false;
                ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    connected = true;
                }

                if(connected){
                    //direct to Login Page
                    SharedPreferencesValue sharedPreferencesValue = new SharedPreferencesValue(getApplicationContext());
                    sharedPreferencesValue.setSharedPreferences();
                    String name = sharedPreferencesValue.getName();

                    if(name.equals(" ")){
                        //dialog.dismiss();
                        Intent intent = new Intent(NoConnection.this,LoginPage.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }else{
                        //dialog.dismiss();
                        Intent intent = new Intent(NoConnection.this,MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }else{
                    //dialog.dismiss();
                }

            }
        });

    }
}