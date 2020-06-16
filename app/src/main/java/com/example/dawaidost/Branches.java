package com.example.dawaidost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.sheets.v4.Sheets;

import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

public class Branches extends AppCompatActivity {
    ArrayList<String> branchName= new ArrayList<>();
    ArrayList<String> openingTime = new ArrayList<>();
    ArrayList<String> mapLink = new ArrayList<>();

    BranchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branches);
        setTitle("Branches");

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //backbutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String[] branch= new String[] {
                "North Market Road, Upper Bazar (Between Magar gola and Modi Dharamshala)",
                "Garikhana (opposite Rajasthan Marbles), Harmu Road.",
                "RIMS (nr Emergency) (NOTE: Only for RIMS patients)**",
                "Ranchi Club Complex, Main Road",
                "Opposite Ryeen School, Lake Road",
                "Chaggan Lal Market, Kantatoli Chowk",
                "Tatisilwe Chowk, Tatisilwe",
                "Sewa Sadan, Sewa Sadan Path",
                "Metro Gali, Ratu Road",
                "Next to Rasik Lal, Doranda",
                "Ashok Nagar",
                "Gopal Comlex, Kuchery Road",
                "Kanke Block Chowk, Next to Kanke Nursing Home",
                "near Mahadev Manda, Lower Chutia, Samlong",
                "Bazar, Bindeshwari Complex, Sadabahar Chowk, Namkum",
                "Opposite Kamal Medical, Lalpur Chowk, Kokar Rd",
                "Church Rd, near Mahavir Mandir, Lower Bazaar"
        };

        String[] link = new String[]{
                "https://goo.gl/maps/E3MZQqB4RY1sq3TD7",
                "https://goo.gl/maps/kETrpbgWhHytRWGZ9",
                "https://goo.gl/maps/sUbiKUAxA94Ftoih7",
                "https://goo.gl/maps/x2gRXc3nz14bcBcH9",
                "https://goo.gl/maps/As2LWvhWbFZY5y8R6",
                "https://goo.gl/maps/yX77cyu74jNjbNvP8",
                "https://goo.gl/maps/LyP3rXynhNpod7Ci9",
                "https://goo.gl/maps/fhLA1B1xjdNqgU548",
                "https://goo.gl/maps/zpRAEiDuRYS48nZW7",
                "https://goo.gl/maps/1daiteUzWXSdB4VF6",
                "https://goo.gl/maps/o8yxVcoD3jyBfUuKA",
                "https://goo.gl/maps/ZHWQTVjcuwsdFzXZA",
                "https://goo.gl/maps/XqaWWQsgoYXfFESX7",
                "https://goo.gl/maps/darem5FckTJaiVRE9",
                "https://goo.gl/maps/5MMdsquVxVv9GwqDA",
                "https://goo.gl/maps/5MMdsquVxVv9GwqDA",
                "https://goo.gl/maps/gZkJbGeZT8sUdhnN7"
        };

        String[] time = new String[]{
                "10:00 to 19:00(Closed Sun)",
                "10:00 to 19:00",
                "24 hrs (Except Sun closes 6pm)",
                "10:00 to 19:00",
                "Currently Closed",
                "10:00 to 19:00",
                "10:00 to 19:00",
                "10:00 to 19:00",
                "10:00 to 19:00",
                "10:00 to 19:00",
                "10:00 to 19:00",
                "10:00 to 19:00",
                "10:00 to 19:00",
                "10:00 to 19:00",
                "10:00 to 19:00",
                "10:00 to 19:00",
                "10:00 to 19:00"
        };

        for(int i=0; i<17; i++){
            branchName.add(branch[i]);
            mapLink.add(link[i]);
            openingTime.add(time[i]);
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        adapter = new BranchAdapter(Branches.this,branchName,mapLink,openingTime);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
