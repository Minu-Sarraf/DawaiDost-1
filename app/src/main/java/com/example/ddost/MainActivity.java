package com.example.ddost;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.ddost.ui.cart.CartFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class MainActivity extends AppCompatActivity {

    static boolean syncedMedicine = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String url = "https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec?id=1XcU1TbA56-JNM0Qsj9ihyt3mgzFGVWeHFFIUn-7_4wM&sheet=Med";

        String dataType = "Medicine";

        Intent intent = getIntent();
        dataType = intent.getStringExtra("LOGIN");
        if(dataType==null){
            dataType="Medicine";
        }

        SQLiteOpenHelper helper = new Database(MainActivity.this);
        final SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("CART",
                new String[]{"CODE"},
                null,null,null,null,null);

        if(!syncedMedicine){
            GetData getData = new GetData(MainActivity.this,this,url,dataType);
            getData.showProgressBar();
            getData.execute("Home");
            syncedMedicine=!syncedMedicine;
        }else if(dataType.equals("login") && cursor.moveToFirst()){
            GetData getData = new GetData(MainActivity.this,this,url,dataType);
            getData.showProgressBar();
            getData.execute("login");
        }
        cursor.close();


        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_cart, R.id.navigation_subscription, R.id.navigation_account)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }


}
