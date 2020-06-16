package com.example.dawaidost;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URLEncoder;
import java.util.ArrayList;

public class ConfirmOrder extends AppCompatActivity {
    TextView textView;
    TextView orderSent;
    Button button, okButton;

    Thread t;
    ProgressBar progressBar;

    SQLiteOpenHelper helper = new Database(ConfirmOrder.this);
    SQLiteDatabase db;

    SharedPreferences sharedPreferences;
    public static final String MYPREFERENCES="MyPrefs";
    public static final String Phone="phoneKey";
    public static final String Prescription="prescriptionKey";

    String mResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);
        setTitle("Confirm!");

        db = helper.getReadableDatabase();

        textView = findViewById(R.id.confirmText);

        orderSent = findViewById(R.id.sentText);
        okButton = findViewById(R.id.buttonDone);

        t = new Thread(new Runnable() {
            @Override
            public void run() {
                postData();
            }
        });

        button = findViewById(R.id.buttonConfirm);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setVisibility(View.INVISIBLE);
                button.setVisibility(View.INVISIBLE);
                //okButton.setVisibility(View.VISIBLE);
                //orderSent.setVisibility(View.VISIBLE);

                showResult();

            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(ConfirmOrder.this,ShowCart.class);
                startActivity(intent1);
                finish();
            }
        });

    }

    public void showResult(){
        //check internet connection
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        }

        String orderResult;
        if(!connected){
            orderResult="Please check your internet connection!";
        }else{
            //clear prescription
            sharedPreferences=getSharedPreferences(MYPREFERENCES,Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Prescription,false);
            editor.commit();

            progressBar = new ProgressBar(this,null,android.R.attr.progressBarStyleLargeInverse);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            ((FrameLayout)getWindow().getDecorView().findViewById(android.R.id.content)).addView(progressBar,params);
            progressBar.setVisibility(View.VISIBLE);  //To show ProgressBar
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            orderResult="Thank You! Your order has been sent.";
            t.start();
            while(t.isAlive()){
                if(mResponse!=null){
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    progressBar.setVisibility(View.GONE);
                    okButton.setVisibility(View.VISIBLE);
                    orderSent.setVisibility(View.VISIBLE);
                }
            }
        }
        orderSent.setText(orderResult);
    }

    //sending data to sheet
    public void postData(){
        //phone number
        sharedPreferences=getSharedPreferences(MYPREFERENCES,Context.MODE_PRIVATE);
        String phone =sharedPreferences.getString(Phone,"");

        //getting ip address
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        String ip = Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());

        //google form
        String[] entry = new String[]{"entry_802967619=",
                "entry_527235997=",
                "entry_839196317=",
                "entry_395111369=",
                "entry_1311478421=",
                "entry_54051543=",
                "entry_1193724692=",
                "entry_1497300814=",
                "entry_620783175=",
                "entry_1834701998=",
                "entry_258939252=",
                "entry_858870482=",
                "entry_588667569=",
                "entry_1740932551=",
                "entry_842208329=",
                "entry_784390368=",
                "entry_462812293=",
                "entry_1458049303=",
                "entry_1653515120=",
                "entry_611847112=",
                "entry_1651299782=",
                "entry_969284198=",
                "entry_1033380290=",
                "entry_1142347456=",
                "entry_2069081410=",
                "entry_1378399258=",
                "entry_651886602=",
                "entry_2105732772=",
                "entry_810396073=",
                "entry_1421463361="
        };

        ArrayList<String> code = new ArrayList<>();
        ArrayList<String> qty = new ArrayList<>();
        ArrayList<String> price = new ArrayList<>();
        Cursor cursor =  db.query("CART",
                new String[]{"CODE","TYPE","BRANDNAME","GENERIC","COMPANY","PRICE", "MAXORDER", "TOTAL"},
                null, null, null, null, null);
        boolean cursorValue = cursor.moveToFirst();
        while(cursorValue){
            code.add(cursor.getString(0));
            qty.add(String.valueOf(cursor.getInt(6)));
            price.add(String.valueOf(cursor.getFloat(5)));
            cursorValue=cursor.moveToNext();
        }

        String url = "https://docs.google.com/forms/u/0/d/e/1FAIpQLSdhhJUtEU84uIYfbygYV2kFTSvqz2-k7lIdz-WWTcOcLNPx4w/formResponse";
        String data = "entry_264892848="+URLEncoder.encode(phone)+"&"+
                "entry_733211671="+URLEncoder.encode(ip);

        int i=0;
        int j=0;
        while(i<code.size()){
            data = data+"&"+entry[j]+URLEncoder.encode(code.get(i));
            j++;
            data = data+"&"+entry[j]+URLEncoder.encode(qty.get(i));
            j++;
            data = data+"&"+entry[j]+URLEncoder.encode(price.get(i));
            j++;
            i++;
        }

        HttpRequest mRequest = new HttpRequest();
        mResponse = mRequest.sendPost(url,data);
    }
}
