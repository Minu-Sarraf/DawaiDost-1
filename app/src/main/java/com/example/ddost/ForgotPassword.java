package com.example.ddost;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

public class ForgotPassword extends AppCompatActivity {
    ArrayList<String> security = new ArrayList<>();
    TextView phone, answer;
    Button button;
    String phoneNumber;
    String userQuestion;
    String securityAnswer;
    String mResponse;
    String id;
    String postNumber, postQuestion, postAnswer;

    String user, name, age, address, pin, email, password, question, answers;

    Activity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        setTitle("Forgot Password");

        activity = this;

        //backbutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        phone= findViewById(R.id.userPhone);
        answer=findViewById(R.id.userAnswer);

        security.add("What is your hometown?");
        security.add("What is your nickname?");
        security.add("What is your best friend's name?");
        security.add("What is your mother's name?");

        final MaterialSpinner spinner = findViewById(R.id.spinnerText);
        spinner.setItems(security);
        userQuestion="What is your hometown?";
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                userQuestion=security.get(position);
            }
        });

        button = findViewById(R.id.submit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber=phone.getText().toString();
                securityAnswer=answer.getText().toString();

                if(phoneNumber.length()==0){
                    phone.setError("Please enter your number!");
                }else if(securityAnswer.length()==0){
                    answer.setError("Please enter your answer!");
                }else{
                    GetCustomers getCustomers = new GetCustomers();
                    getCustomers.execute();
                }
            }
        });
    }

    public void checkUser(){
        try {
            JSONObject jsonObject = new JSONObject(mResponse);
            JSONArray jsonArray = jsonObject.getJSONArray("Customers");
            int count =0;
            boolean check = false;
            while(count<jsonArray.length()){
                final JSONObject finalObject = jsonArray.getJSONObject(count);
                user = String.valueOf(finalObject.get("User_Number"));
                question = String.valueOf(finalObject.get("Question"));
                answers = String.valueOf(finalObject.get("Answer"));
                Log.d("customers",user+question+answers);
                Log.d("customers",phoneNumber+userQuestion+securityAnswer);

                if(user.equals(phoneNumber) && question.equals(userQuestion) && (answers.toUpperCase(Locale.ENGLISH)).equals(securityAnswer.toUpperCase(Locale.ENGLISH))){
                    check=true;
                    AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPassword.this);
                    builder.setTitle("Enter your new password");
                    final EditText input = new EditText(ForgotPassword.this);
                    int padding = getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin);
                    input.setPadding(padding,0,40,padding);
                    input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    input.setLayoutParams(lp);
                    builder.setView(input);

                    builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            password = input.getText().toString();
                            try {
                                name=String.valueOf(finalObject.get("Name"));
                                age= String.valueOf(finalObject.get("Age"));
                                address= String.valueOf(finalObject.get("Delivery_Address"));
                                pin=String.valueOf(finalObject.get("PinCode"));
                                email=String.valueOf(finalObject.get("Email"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                id=String.valueOf(finalObject.get("User_Number"));
                                postNumber = String.valueOf(finalObject.get("User_Number"));
                                postQuestion=String.valueOf(finalObject.get("Question"));
                                postAnswer=String.valueOf(finalObject.get("Answer"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            new DeleteDataActivity().execute();

                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
                Log.d("user",user);
                count++;
            }
            if(!check){
                AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPassword.this);
                builder.setMessage("Your information is incorrect!");
                builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void postData(){
        SharedPreferencesValue sharedPreferencesValue = new SharedPreferencesValue(getApplicationContext());
        sharedPreferencesValue.setSharedPreferences();
        sharedPreferencesValue.setValues(postNumber,name,age,address,pin,email,password,postQuestion,postAnswer);

        Encrypt encrypt = new Encrypt();
        String url = "https://docs.google.com/forms/u/0/d/e/1FAIpQLSfI12ZL25vRcRNWaJKn2KXwEzUWkugHvhiPLCnrClnb-hVulA/formResponse";
        String data = "entry_1554870898="+ URLEncoder.encode(postNumber)+"&"+
                "entry_145080611="+ URLEncoder.encode(name)+"&"+
                "entry_1735723515="+ URLEncoder.encode(age)+"&"+
                "entry_1723792066="+ URLEncoder.encode(user)+"&"+
                "entry_957026713="+ URLEncoder.encode(address)+"&"+
                "entry_447845151="+ URLEncoder.encode(pin)+"&"+
                "entry_733184386="+ URLEncoder.encode(email)+"&"+
                "entry_2069835835="+ URLEncoder.encode(encrypt.encryptThisString(password))+"&"+
                "entry_2101863006="+ URLEncoder.encode(postQuestion)+"&"+
                "entry_24963331="+ URLEncoder.encode(postAnswer);

        SendSheet sendSheet = new SendSheet(getApplicationContext(),url,data);
        sendSheet.execute();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();

        }
        return super.onOptionsItemSelected(item);
    }

    public class GetCustomers extends AsyncTask<Void, Void, Void> {
        final String url = "https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec?id=1NRluNvYf9BeECtLVRZFnO1CENYm_4wmZWrjWHoEb8uc&sheet=Customers";
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(ForgotPassword.this);
            dialog.setTitle("Please Wait");
            dialog.setMessage("Checking");
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
        }
    }

    class DeleteDataActivity extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;
        int jIndex;
        int x;
        String result=null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = new ProgressDialog(ForgotPassword.this);
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

            GetUserDetails getUserDetails = new GetUserDetails(ForgotPassword.this,user,activity,name);
            getUserDetails.execute();

        }
    }
}
