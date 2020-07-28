package com.example.ddost;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

public class ConfirmOrder extends AppCompatActivity {
    Button sendButton, okButton, cancelButton;
    TextView confirmSend, orderSent;

    SharedPreferencesValue sharedPreferencesValue;

    String mResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);
        setTitle("Confirm Order!");

        sharedPreferencesValue= new SharedPreferencesValue(getApplicationContext());
        sharedPreferencesValue.setSharedPreferences();

        orderSent = findViewById(R.id.sentText);
        confirmSend = findViewById(R.id.confirmText);

        okButton = findViewById(R.id.buttonDone);
        sendButton = findViewById(R.id.buttonConfirm);
        cancelButton = findViewById(R.id.buttonCancel);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GetPrescriptionLink getPrescriptionLink = new GetPrescriptionLink();
                getPrescriptionLink.execute();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConfirmOrder.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            sharedPreferencesValue.setPrescription(false);

            sendOrder();
            orderResult="Thank You! \n Your order has been sent.";
        }
        orderSent.setText(orderResult);
    }

    public void sendOrder(){
        String phone = sharedPreferencesValue.getPhone();

        Log.d("link?",sharedPreferencesValue.getLink());

        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());

        String[] entry = new String[]{"entry_802967619=", "entry_527235997=", "entry_839196317=",
                "entry_395111369=", "entry_1311478421=", "entry_54051543=", "entry_1193724692=",
                "entry_1497300814=", "entry_620783175=", "entry_1834701998=", "entry_258939252=",
                "entry_858870482=", "entry_588667569=", "entry_1740932551=", "entry_842208329=",
                "entry_784390368=", "entry_462812293=", "entry_1458049303=", "entry_1653515120=",
                "entry_611847112=", "entry_1651299782=", "entry_969284198=", "entry_1033380290=",
                "entry_1142347456=", "entry_2069081410=", "entry_1378399258=", "entry_651886602=",
                "entry_2105732772=", "entry_810396073=", "entry_1421463361="
        };

        ArrayList<String> code = new ArrayList<>();
        ArrayList<String> qty = new ArrayList<>();
        ArrayList<String> price = new ArrayList<>();

        SQLiteOpenHelper helper = new Database(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor =  db.query("CART",
                new String[]{"CODE","TYPE","BRANDNAME","GENERIC","COMPANY","PRICE", "MAXORDER", "TOTAL"},
                null, null, null, null, null);
        boolean cursorValue = cursor.moveToFirst();
        while(cursorValue){

            Cursor cursor1 = db.query("DAWAI",
                    new String[]{"CODE","PRICE"},
                    "CODE=?",
                    new String[]{cursor.getString(0)},
                    null,null,null);

            if(cursor1.moveToFirst()){
                code.add(cursor.getString(0));
                qty.add(String.valueOf(cursor.getInt(6)));
                price.add(String.valueOf(cursor1.getFloat(1)));
            }

            cursorValue=cursor.moveToNext();
        }
        cursor.close();

        String url = "https://docs.google.com/forms/u/0/d/e/1FAIpQLSdhhJUtEU84uIYfbygYV2kFTSvqz2-k7lIdz-WWTcOcLNPx4w/formResponse";
        String data = "entry_264892848="+ URLEncoder.encode(phone)+"&"+
                "entry_733211671="+URLEncoder.encode(ip);

        int i=0,j=0;
        while(i<code.size()){
            data = data+"&"+entry[j]+URLEncoder.encode(code.get(i));
            j++;
            data = data+"&"+entry[j]+URLEncoder.encode(qty.get(i));
            j++;
            data = data+"&"+entry[j]+URLEncoder.encode(price.get(i));
            j++;
            i++;
        }

        data=data+"&"+"entry_1647272914="+URLEncoder.encode(sharedPreferencesValue.getLink());

        SendSheet sendSheet = new SendSheet(getApplicationContext(),url,data);
        sendSheet.execute();
    }

    public void checkUser(){
        try {
            JSONObject jsonObject = new JSONObject(mResponse);
            JSONArray jsonArray = jsonObject.getJSONArray("Sheet1");
            int count =0;
            String prescriptionLink = null;
            while(count<jsonArray.length()) {
                JSONObject finalObject = jsonArray.getJSONObject(count);
                String user = String.valueOf(finalObject.get("uId"));

                if (user.equals(sharedPreferencesValue.getPhone())) {
                    prescriptionLink = String.valueOf(finalObject.get("uImage"));
                }
                count++;
            }
            sharedPreferencesValue.setLink(prescriptionLink);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class GetPrescriptionLink extends AsyncTask<Void, Void, Void> {
        final String url = "https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec?id=1hBrkk4k5dGqE_V6u3MroRs3QdlEYR472F9N5X6BHFlM&sheet=Sheet1";
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(ConfirmOrder.this);
            dialog.setTitle("Please Wait");
            dialog.setMessage("Sending Order...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpRequest request = new HttpRequest();
            mResponse=request.sendGet(url);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();

            checkUser();

            sendButton.setVisibility(View.INVISIBLE);
            confirmSend.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.INVISIBLE);

            okButton.setVisibility(View.VISIBLE);
            orderSent.setVisibility(View.VISIBLE);

            showResult();
        }
    }
}
