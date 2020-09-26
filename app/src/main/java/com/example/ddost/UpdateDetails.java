package com.example.ddost;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

public class UpdateDetails extends AppCompatActivity {
    EditText userPhone, userName, userAge, userAddress, userPin, userEmail;
    String uPhone, uName, uAge, uAddress, uPin, uEmail;
    SharedPreferencesValue pValue;
    String id;
    String uPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_details);
        setTitle("Confirm Details");
        setTitleColor(R.color.colorBlue);

        pValue =new SharedPreferencesValue(getApplicationContext());
        pValue.setSharedPreferences();

        userPhone = findViewById(R.id.userPhone);
        userName = findViewById(R.id.userName);
        userAge= findViewById(R.id.userAge);
        userAddress = findViewById(R.id.userAddress);
        userPin= findViewById(R.id.userPin);
        userEmail = findViewById(R.id.userEmail);

        userPhone.setText(pValue.getPhone());
        userName.setText(pValue.getName());
        userAge.setText(pValue.getAge());
        userAddress.setText(pValue.getAddress());
        userPin.setText(pValue.getPincode());
        userEmail.setText(pValue.getEmail());

        Button button = findViewById(R.id.buttonLogin);
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
                    Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();
                    return;
                }

                uPhone = userPhone.getText().toString();
                uName = userName.getText().toString();
                uAge= userAge.getText().toString();
                uAddress = userAddress.getText().toString();
                uPin = userPin.getText().toString();
                uEmail = userEmail.getText().toString();

                validateData();
            }
        });
    }

    public void validateData(){
        if(uPhone.trim().length()!=10) {
            userPhone.setError("Please Enter a valid Phone Number!");
        }else if (uName.trim().length()<2 ){
            userName.setError("Please enter your full name!");
        }else if(uAge.trim().length()==0){
            userAge.setError("Please enter your age!");
        }else if(Integer.parseInt(uAge)<18) {
            userAge.setError("Cannot accept order from age under 18!");
        }else if(uEmail.length()==0 || !pValue.isValidEmail(uEmail.trim())){
            userEmail.setError("Please enter a valid Email Address!");
        }else if(uAddress.trim().length()<2) {
            userAddress.setError("Please enter a valid address!");
        }else if(uPin.trim().length()!=6){
            userPin.setError("Please enter a valid pin number!");
        }else{
            if(!uPhone.equals(pValue.getPhone()) || !uName.equals(pValue.getName()) || !uAge.equals(pValue.getAge()) || !uEmail.equals(pValue.getEmail()) || !uAddress.equals(pValue.getAddress()) || !uPin.equals(pValue.getPincode())){
                id=uPhone;
                new DeleteDataActivity().execute();
                pValue.setValues(uPhone,uName,uAge,uAddress,uPin,uEmail,pValue.getPassword(),pValue.getQuestion(),pValue.getAnswer());
            }else{
                Intent intent = new Intent(UpdateDetails.this,ConfirmOrder.class);
                startActivity(intent);
                finish();
            }

        }
    }

    public void updateData(){
        Encrypt encrypt = new Encrypt();
        String url = "https://docs.google.com/forms/u/0/d/e/1FAIpQLSfI12ZL25vRcRNWaJKn2KXwEzUWkugHvhiPLCnrClnb-hVulA/formResponse";
        String data = "entry_1554870898="+ URLEncoder.encode(uPhone)+"&"+
                "entry_145080611="+ URLEncoder.encode(uName)+"&"+
                "entry_1735723515="+ URLEncoder.encode(uAge)+"&"+
                "entry_1723792066="+ URLEncoder.encode(uPhone)+"&"+
                "entry_957026713="+ URLEncoder.encode(uAddress)+"&"+
                "entry_447845151="+ URLEncoder.encode(uPin)+"&"+
                "entry_733184386="+ URLEncoder.encode(uEmail)+"&"+
                "entry_2069835835="+ URLEncoder.encode(encrypt.encryptThisString(pValue.getPassword()))+"&"+
                "entry_2101863006="+ URLEncoder.encode(pValue.getQuestion())+"&"+
                "entry_24963331="+ URLEncoder.encode(pValue.getAnswer());

        SendSheet sendSheet = new SendSheet(getApplicationContext(),url,data);
        sendSheet.execute();
    }

    public class DeleteDataActivity extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;
        int jIndex;
        int x;
        String result=null;
        SharedPreferencesValue pValue;
        Context context;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            context=UpdateDetails.this;

            dialog = new ProgressDialog(context);
            dialog.setTitle("Wait Please...");
            dialog.setMessage("Saving... ");
            dialog.show();

        }

        @Nullable
        @Override
        protected Void doInBackground(Void... params) {
            Log.i(Controller.TAG,"IDVALUE"+id);
            JSONObject jsonObject = Controller.deleteData(id);
            Log.i(Controller.TAG, "Json obj "+jsonObject);

            try {
                /**
                 * Check Whether Its NULL???
                 */
                if (jsonObject != null) {

                    result=jsonObject.getString("result");


                }
            } catch (JSONException je) {
                Log.i(Controller.TAG, "" + je.getLocalizedMessage());
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            Toast.makeText(context,"Saved", Toast.LENGTH_SHORT).show();
            updateData();
            Intent intent = new Intent(context,ConfirmOrder.class);
            context.startActivity(intent);
            finish();
        }
    }
}
