package com.example.dawaidost;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginPage extends AppCompatActivity {
    EditText name, phone, address;
    Button loginButton;

    SharedPreferences sharedPreferences;
    public static final String MYPREFERENCES="MyPrefs";
    public static final String Name="nameKey";
    public static final String Phone="phoneKey";
    public static final String Address="addressKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        setTitle("Login Page");

        name = findViewById(R.id.userName);
        phone = findViewById(R.id.userPhone);
        address = findViewById(R.id.userAddress);

        loginButton=findViewById(R.id.buttonLogin);
        sharedPreferences=getSharedPreferences(MYPREFERENCES, Context.MODE_PRIVATE);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uName = name.getText().toString();
                String uPhone = phone.getText().toString();
                String uAddress= address.getText().toString();

                if(uName.length()==0){
                    name.setError("This field cannot be empty");
                }else if (uPhone.length()!=10){
                    phone.setError("Please Enter a valid Phone Number!");
                }else if (uAddress.length()==0){
                    address.setError("This field cannot be empty");
                }else{
                    saveUserData(uName,uPhone,uAddress);
                }
            }
        });
    }

    public void saveUserData(String uName, String uPhone, String uAddress){
        Toast.makeText(LoginPage.this,"Welcome "+uName,Toast.LENGTH_SHORT).show();

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(Name,uName);
        editor.putString(Phone,uPhone);
        editor.putString(Address,uAddress);
        editor.commit();


        Intent intent = new Intent(LoginPage.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

}
