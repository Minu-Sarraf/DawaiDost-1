package com.example.ddost;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginPage extends AppCompatActivity {
    Button register;
    EditText phoneNumber, password;
    Button loginButton;
    String userPhone, userPassword;
    ProgressBar loading;
    String mResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        phoneNumber = findViewById(R.id.userPhone);
        password = findViewById(R.id.userPassword);
        loginButton = findViewById(R.id.login);

        loading=findViewById(R.id.loading);

        phoneNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    hideKeyboard(v);
                }
            }
        });

        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    hideKeyboard(v);
                }
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
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

                userPhone= phoneNumber.getText().toString();
                userPassword=password.getText().toString();

                if(userPhone.trim().length()!=10){
                    phoneNumber.setError("Please enter a valid number!");
                }else if(userPassword.length()==0){
                    password.setError("Please enter your password!");
                }else{
                    GetCustomers getCustomers = new GetCustomers();
                    getCustomers.execute();
                }
            }
        });

        register = findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginPage.this,SignUp.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
            }
        });

        TextView forgot = findViewById(R.id.forgotPassword);
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginPage.this,ForgotPassword.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
            }
        });
    }

    public void checkPassword(){
        try {
            JSONObject jsonObject = new JSONObject(mResponse);
            JSONArray jsonArray = jsonObject.getJSONArray("Customers");
            int count =0;
            boolean check = false;
            while(count<jsonArray.length()){
                JSONObject finalObject = jsonArray.getJSONObject(count);
                String user = String.valueOf(finalObject.get("User_Number"));

                if(user.equals(userPhone)){
                    check=true;
                    String password = String.valueOf(finalObject.get("SecretKey"));
                    Encrypt encrypt = new Encrypt();
                    String encryptedPw= encrypt.encryptThisString(userPassword);
                    if(encryptedPw.equals(password)){
                        String name=String.valueOf(finalObject.get("Name"));
                        String age= String.valueOf(finalObject.get("Age"));
                        String address= String.valueOf(finalObject.get("Delivery_Address"));
                        String pin=String.valueOf(finalObject.get("PinCode"));
                        String email=String.valueOf(finalObject.get("Email"));
                        String question = String.valueOf(finalObject.get("Question"));
                        String answer = String.valueOf(finalObject.get("Answer"));
                        SharedPreferencesValue sharedPreferencesValue = new SharedPreferencesValue(getApplicationContext());
                        sharedPreferencesValue.setSharedPreferences();
                        sharedPreferencesValue.setValues(user,name,age,address,pin,email,userPassword,question,answer);

                        GetUserDetails getUserDetails = new GetUserDetails(LoginPage.this,userPhone,this, name);
                        getUserDetails.execute();


                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginPage.this);
                        builder.setMessage("Password Incorrect");
                        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();
                    }
                }
                count++;
            }
            if(!check){
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginPage.this);
                builder.setMessage("Phone Number not registered!");
                builder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(LoginPage.this,SignUp.class);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public class GetCustomers extends AsyncTask<Void, Void, Void>{
        final String url = "https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec?id=1NRluNvYf9BeECtLVRZFnO1CENYm_4wmZWrjWHoEb8uc&sheet=Customers";
        ProgressDialog dialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(LoginPage.this);
            dialog.setTitle("Please Wait");
            dialog.setMessage("Logging In");
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
            if(mResponse!=null){
                checkPassword();
            }else{
                Toast.makeText(LoginPage.this,"Could not fetch data!",Toast.LENGTH_SHORT).show();
            }
        }
    }
}