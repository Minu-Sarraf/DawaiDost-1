package com.example.ddost.ui.subscription;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ddost.Database;
import com.example.ddost.HttpRequest;
import com.example.ddost.R;
import com.example.ddost.SendSheet;
import com.example.ddost.SharedPreferencesValue;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.net.URLEncoder;
import java.util.ArrayList;


public class SubscriptionFragment extends Fragment {
    EditText name, phone, email, medicine;
    String uName, uPhone, uEmail, uMedicine;

    public SubscriptionFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root= inflater.inflate(R.layout.fragment_subscription, container, false);
        ImageView imageView = root.findViewById(R.id.imageView);
        ImageView imageView1 = root.findViewById(R.id.imageView1);
        ImageView imageView2 = root.findViewById(R.id.imageView2);

        SQLiteOpenHelper helper = new Database(getContext());
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor images = db.query("IMAGES",
                new String[]{"BENEFITS"},
                null,null,null,null,null);
        ArrayList<String> image = new ArrayList<>();
        boolean cursorValue = images.moveToFirst();
        while(cursorValue){
            image.add(images.getString(0));

            cursorValue=images.moveToNext();
        }

        Picasso.get()
                .load(image.get(0))
                .fit()
                .into(imageView);

        Picasso.get()
                .load(image.get(1))
                .fit()
                .into(imageView1);

        Picasso.get()
                .load(image.get(2))
                .fit()
                .into(imageView2);
        images.close();


        name= root.findViewById(R.id.fullNameText);
        phone= root.findViewById(R.id.phoneNumberText);
        email= root.findViewById(R.id.emailIdText);
        medicine= root.findViewById(R.id.regularMedicineText);

        SharedPreferencesValue sharedPreferencesValue = new SharedPreferencesValue(getContext());
        sharedPreferencesValue.setSharedPreferences();
        name.setText(sharedPreferencesValue.getName());
        phone.setText(sharedPreferencesValue.getPhone());
        email.setText(sharedPreferencesValue.getEmail());

        Button button = root.findViewById(R.id.submitButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uName=name.getText().toString();
                uPhone=phone.getText().toString();
                uEmail=email.getText().toString();
                uMedicine=medicine.getText().toString();
                if(uName.length()==0){
                    name.setError("Please enter your full name!");
                }else if (uPhone.length()!=10){
                    phone.setError("Please enter a valid phone number!");
                }else if(uEmail.length()==0){
                    email.setError("Please enter a valid email");
                }else if(uMedicine.length()==0){
                    medicine.setError("Please enter a medicine");
                }else{
                    boolean connected=false;
                    ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                    if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                        connected = true;
                    }
                    if(!connected){
                        Snackbar.make(v,"No Internet Connection",Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    SendMedicine sendMedicine = new SendMedicine();
                    sendMedicine.execute();
                }
            }
        });

        return root;
    }

    public void postData(){
        String url ="https://docs.google.com/forms/u/3/d/e/1FAIpQLSemiKHSLNNbhopYWhF1FkrNSLQA06nJXoZGPFwgRYIjwYr1Fg/formResponse";
        String data = "entry_107466921="+ URLEncoder.encode(uPhone)+"&"+
                "entry_2038832908="+ URLEncoder.encode(uName)+"&"+
                "entry_985234850="+ URLEncoder.encode(uEmail)+"&"+
                "entry_841824018="+ URLEncoder.encode(uMedicine);

        SendSheet sendSheet = new SendSheet(getContext(),url,data);
        sendSheet.execute();
    }

    public class SendMedicine extends AsyncTask<Void, Void, Void>{
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog= new ProgressDialog(getContext());
            progressDialog.setTitle("Please Wait");
            progressDialog.setMessage("Sending data");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String url ="https://docs.google.com/forms/u/3/d/e/1FAIpQLSemiKHSLNNbhopYWhF1FkrNSLQA06nJXoZGPFwgRYIjwYr1Fg/formResponse";
            String data = "entry_107466921="+ URLEncoder.encode(uPhone)+"&"+
                    "entry_2038832908="+ URLEncoder.encode(uName)+"&"+
                    "entry_985234850="+ URLEncoder.encode(uEmail)+"&"+
                    "entry_841824018="+ URLEncoder.encode(uMedicine);

            HttpRequest mRequest = new HttpRequest();
            mRequest.sendPost(url,data);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            medicine.setText("");
            Toast.makeText(getContext(),"Data Sent",Toast.LENGTH_SHORT).show();
        }
    }
}
