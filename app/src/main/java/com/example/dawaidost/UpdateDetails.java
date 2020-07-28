package com.example.dawaidost;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.style.UpdateAppearance;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URLEncoder;

public class UpdateDetails extends AppCompatActivity {
    EditText name, age, address, pinCode, phone, altPhone, email, landmark;
    Button loginButton;

    SharedPreferences sharedPreferences;
    public static final String MYPREFERENCES="MyPrefs";
    public static final String Name="nameKey";
    public static final String Age="ageKey";
    public static final String Address="addressKey";
    public static final String Pincode="pinKey";
    public static final String Phone="phoneKey";
    public static final String AltPhone="altPhoneKey";
    public static final String Email ="emailKey";
    public static final String Landmark ="landmarkKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_details);
        setTitle("Confirm your details");

        name = findViewById(R.id.userName);
        age = findViewById(R.id.userAge);
        address = findViewById(R.id.userAddress);
        pinCode = findViewById(R.id.userPin);
        phone = findViewById(R.id.userPhone);
        altPhone= findViewById(R.id.userAltPhone);
        email = findViewById(R.id.userEmail);
        landmark = findViewById(R.id.userLandmark);

        final String savedName = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("nameKey"," ");
        final String savedAge = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("ageKey"," ");
        final String savedAddress = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("addressKey"," ");
        final String savedPincode = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("pinKey"," ");
        final String savedPhone = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("phoneKey"," ");
        final String savedAltPhone = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("altPhoneKey"," ");
        final String savedEmail = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("emailKey"," ");
        final String savedLandmark = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("landmarkKey"," ");

        name.setText(savedName);
        age.setText(savedAge);
        address.setText(savedAddress);
        pinCode.setText(savedPincode);
        phone.setText(savedPhone);
        altPhone.setText(savedAltPhone);
        email.setText(savedEmail);
        landmark.setText(savedLandmark);

        loginButton=findViewById(R.id.buttonLogin);
        sharedPreferences=getSharedPreferences(MYPREFERENCES, Context.MODE_PRIVATE);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String uName = name.getText().toString();
                final String uAge = age.getText().toString();
                final String uAddress= address.getText().toString();
                final String uPin= pinCode.getText().toString();
                final String uPhone= phone.getText().toString();
                final String uAltPhone= altPhone.getText().toString();
                final String uEmail= email.getText().toString();
                final String uLandmark = landmark.getText().toString();

/*                Log.d("check", String.valueOf(uAltPhone.equals(savedAltPhone)));
                Log.d("check", String.valueOf(uName.equals(savedName)));
                Log.d("check", String.valueOf(uAge.equals(savedAge)));
                Log.d("check", String.valueOf(uAddress.equals(savedAddress)));
                Log.d("check", String.valueOf(uPin.equals(savedPincode)));
                Log.d("check", String.valueOf(uPhone.equals(savedPhone)));
                Log.d("check", String.valueOf(uEmail.equals(savedEmail)));
                Log.d("check", String.valueOf(uLandmark.equals(savedLandmark)));*/

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendData(uName, uAge, uAddress, uPin, uPhone, uAltPhone, uEmail,uLandmark);
                    }
                });

                if(uName.length()==0) {
                    name.setError("Enter your full name");
                }else if(uAge.length()==0) {
                    age.setError("Enter your age");
                }else if(uAddress.length()==0) {
                    address.setError("Enter your full address");
                }else if(uPin.length()!=6){
                    pinCode.setError("Please enter a valid pin number!");
                }else if (uPhone.length()!=10){
                    phone.setError("Please Enter a valid Phone Number!");
                }else if(uEmail.length()==0){
                    email.setError("Please enter a valid email!");
                }else if(uLandmark.length()==0){
                    landmark.setError("Please enter your nearest landmark!");
                }else if(!uName.equals(savedName) || !uAge.equals(savedAge) || !uAddress.equals(savedAddress) || !uPin.equals(savedPincode) || !uPhone.equals(savedPhone) || !uAltPhone.equals(savedAltPhone) || !uEmail.equals(savedEmail) || !uLandmark.equals(savedLandmark)){
                    t.start();
                    saveUserData(uName,uAge,uAddress,uPin,uPhone,uAltPhone,uEmail,uLandmark);
                }else{
                    Intent intent = new Intent(UpdateDetails.this,ConfirmOrder.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public void saveUserData(String uName, String uAge, String uAddress, String uPin, String uPhone, String uAltPhone, String uEmail, String uLandmark){

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(Name,uName);
        editor.putString(Age,uAge);
        editor.putString(Address,uAddress);
        editor.putString(Pincode,uPin);
        editor.putString(Phone,uPhone);
        editor.putString(AltPhone,uAltPhone);
        editor.putString(Email,uEmail);
        editor.putString(Landmark,uLandmark);
        editor.commit();


        Intent intent = new Intent(UpdateDetails.this,ConfirmOrder.class);
        startActivity(intent);
        finish();
    }

    public void sendData(String uName, String uAge, String uAddress, String uPin, String uPhone, String uAltPhone, String uEmail, String uLandmark){
        String url = "https://docs.google.com/forms/u/0/d/e/1FAIpQLSfI12ZL25vRcRNWaJKn2KXwEzUWkugHvhiPLCnrClnb-hVulA/formResponse";
        String data = "entry_1554870898="+ URLEncoder.encode(uPhone)+"&"+
                "entry_145080611="+ URLEncoder.encode(uName)+"&"+
                "entry_1735723515="+ URLEncoder.encode(uAge)+"&"+
                "entry_1723792066="+ URLEncoder.encode(uPhone)+"&"+
                "entry_607916949="+ URLEncoder.encode(uAltPhone)+"&"+
                "entry_957026713="+ URLEncoder.encode(uAddress)+"&"+
                "entry_2069835835="+ URLEncoder.encode(uLandmark)+"&"+
                "entry_447845151="+ URLEncoder.encode(uPin)+"&"+
                "entry_733184386="+ URLEncoder.encode(uEmail);

        HttpRequest mRequest = new HttpRequest();
        String mResponse =mRequest.sendPost(url,data);

        if(mResponse!=null){

        }

    }
}