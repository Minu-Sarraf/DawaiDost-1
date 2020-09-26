package com.example.ddost;

import androidx.annotation.NonNull;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

public class SignUp extends AppCompatActivity {
    EditText userPhone, userName, userAge, userAddress, userPin, userEmail, userPassword,userAnswer;
    String uPhone, uName, uAge, uAddress, uPin, uEmail,uPassword,uAnswer, uQuestion;
    String id;
    ArrayList<String> security = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle("Sign Up");

        //backbutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        security.add("What is your hometown?");
        security.add("What is your nickname?");
        security.add("What is your best friend's name?");
        security.add("What is your mother's name?");

        final MaterialSpinner spinner = findViewById(R.id.spinnerText);
        spinner.setItems(security);
        uQuestion="What is your hometown?";
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                uQuestion=security.get(position);
            }
        });

        userPhone = findViewById(R.id.userPhone);
        userName = findViewById(R.id.userName);
        userAge= findViewById(R.id.userAge);
        userAddress = findViewById(R.id.userAddress);
        userPin= findViewById(R.id.userPin);
        userEmail = findViewById(R.id.userEmail);
        userPassword = findViewById(R.id.userPassword);
        userAnswer=findViewById(R.id.userAnswer);


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
                    Snackbar.make(v,"No Internet Connection",Snackbar.LENGTH_SHORT).show();
                    return;
                }

                uPhone = userPhone.getText().toString();
                uName = userName.getText().toString();
                uAge= userAge.getText().toString();
                uAddress = userAddress.getText().toString();
                uPin = userPin.getText().toString();
                uEmail = userEmail.getText().toString();
                uPassword= userPassword.getText().toString();
                uAnswer = userAnswer.getText().toString();

                validateData();
            }
        });
    }

    public void validateData(){
        SharedPreferencesValue sharedPreferencesValue = new SharedPreferencesValue(SignUp.this);
        sharedPreferencesValue.setSharedPreferences();
        if(uPhone.trim().length()!=10) {
            userPhone.setError("Please Enter a valid Phone Number!");
        }else if (uName.trim().length()<2){
            userName.setError("Please enter your full name!");
        }else if(uAge.trim().length()==0){
            userAge.setError("Please enter your age!");
        }else if(Integer.parseInt(uAge)<18) {
            userAge.setError("Cannot accept order from age under 18");
        }else if(uEmail.length()==0 || !sharedPreferencesValue.isValidEmail(uEmail.trim())){
            userEmail.setError("Please enter a valid Email Address!");
        }else if(uAddress.trim().length()<2 ) {
            userAddress.setError("Please enter a valid address!");
        }else if(uPin.trim().length()!=6){
            userPin.setError("Please enter a valid pin number!");
        }else if(uPassword.length()<8){
            userPassword.setError("Password should consist at least 8 characters");
        }else if(uAnswer.trim().length()<2){
            userAnswer.setError("Please enter a valid text!");
        }else{

            sharedPreferencesValue.setValues(uPhone.trim(),uName.trim(),uAge.trim(),uAddress.trim(),uPin.trim(),uEmail.trim(),uPassword,uQuestion,uAnswer.trim());

            id=uPhone;
            new DeleteDataActivity().execute();
        }
    }

    public void postData(){
        Encrypt encrypt = new Encrypt();
        String url = "https://docs.google.com/forms/u/0/d/e/1FAIpQLSfI12ZL25vRcRNWaJKn2KXwEzUWkugHvhiPLCnrClnb-hVulA/formResponse";
        String data = "entry_1554870898="+ URLEncoder.encode(uPhone)+"&"+
                "entry_145080611="+ URLEncoder.encode(uName)+"&"+
                "entry_1735723515="+ URLEncoder.encode(uAge)+"&"+
                "entry_1723792066="+ URLEncoder.encode(uPhone)+"&"+
                "entry_957026713="+ URLEncoder.encode(uAddress)+"&"+
                "entry_447845151="+ URLEncoder.encode(uPin)+"&"+
                "entry_733184386="+ URLEncoder.encode(uEmail)+"&"+
                "entry_2069835835="+ URLEncoder.encode(encrypt.encryptThisString(uPassword))+"&"+
                "entry_2101863006="+ URLEncoder.encode(uQuestion)+"&"+
                "entry_24963331="+ URLEncoder.encode(uAnswer);

        SendSheet sendSheet = new SendSheet(getApplicationContext(),url,data);
        sendSheet.execute();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
    }

    class DeleteDataActivity extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;
        String result=null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = new ProgressDialog(SignUp.this);
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

            postData();
            Intent intent = new Intent(SignUp.this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(SignUp.this,"Welcome "+uName,Toast.LENGTH_SHORT).show();
            finish();
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);

        }
    }
}
