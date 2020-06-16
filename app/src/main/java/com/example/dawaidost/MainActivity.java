package com.example.dawaidost;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LauncherActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.mancj.materialsearchbar.MaterialSearchBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static androidx.core.graphics.TypefaceCompatUtil.getTempFile;


public class MainActivity extends AppCompatActivity{

    private AppBarConfiguration mAppBarConfiguration;

    SQLiteOpenHelper helper = new Database(MainActivity.this);
    SQLiteDatabase db;
    RecyclerView recyclerView;
    ProgressBar dawaiLoadingDialog;
    JSONObject mResponse;
    static boolean synced = false;
    DrawerLayout drawer;
    private static final int CAMERA_REQUEST = 1888;
    File picture;

    SearchView searchView;
    SharedPreferences sharedPreferences;
    String userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Home (Dawai Dost)");

        //phone number
        sharedPreferences = getSharedPreferences("MyPrefs",Context.MODE_PRIVATE);
        userPhone = sharedPreferences.getString("phoneKey"," ");

        //check internet connection
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        }
        else{
            connected = false;
        }


        //getdata for the first boot
        boolean firstBoot = getSharedPreferences("BOOT_PREF",MODE_PRIVATE).getBoolean("firstBoot",true);
        if(firstBoot){

            //show no connection page
            if (connected==false){
                Intent intent = new Intent(MainActivity.this,NoConnection.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else{
                //Show progressbar while volley request is serviced
                dawaiLoadingDialog = new ProgressBar(this,null,android.R.attr.progressBarStyleLargeInverse);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                ((FrameLayout)getWindow().getDecorView().findViewById(android.R.id.content)).addView(dawaiLoadingDialog,params);
                dawaiLoadingDialog.setVisibility(View.VISIBLE);  //To show ProgressBar
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                GetData getData = new GetData();
                getData.execute("hello");


                getSharedPreferences("BOOT_PREF",MODE_PRIVATE)
                        .edit()
                        .putBoolean("firstBoot",false)
                        .commit();

                synced=!synced;
            }

        }else{
            //sync everytime you open the app
            if (synced==false){
                //Show progressbar while volley request is serviced
                dawaiLoadingDialog = new ProgressBar(this,null,android.R.attr.progressBarStyleLargeInverse);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                ((FrameLayout)getWindow().getDecorView().findViewById(android.R.id.content)).addView(dawaiLoadingDialog,params);
                dawaiLoadingDialog.setVisibility(View.VISIBLE);  //To show ProgressBar
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                GetData getData = new GetData();
                getData.execute("hello");
                synced=!synced;
            }
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //showing search match
        recyclerView = findViewById(R.id.recycler_view1);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);


        //search view and searching data
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                showList(newText);
                return false;
            }
        });

        //upload prescription
        ImageView imageView =  findViewById(R.id.prescription);
        imageView.setClickable(true);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},0);
                }else{
                    Intent intent = getPickImageIntent(MainActivity.this);
                    startActivityForResult(intent,1);
                }
            }
        });

        //speed dial floating action button
        SpeedDialView speedDialView = findViewById(R.id.speedDial);
        speedDialView.inflate(R.menu.activity_main_drawer);

        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem speedDialActionItem) {
                Intent intent;
                switch (speedDialActionItem.getId()) {
                    case R.id.nav_branches:
                        intent = new Intent(MainActivity.this, Branches.class);
                        startActivity(intent);
                        return false; // true to keep the Speed Dial open
                    case R.id.nav_cart:
                        //show items on cart
                        intent =new Intent(MainActivity.this,ShowCart.class);
                        startActivity(intent);
                        return false;
                    case R.id.nav_logout:
                        onClickLogOut();
                        return false;
                    default:
                        return false;
                }
            }
        });


        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.nav_branches, R.drawable.branches)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.yellow, getTheme()))
                        .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.dark, getTheme()))
                        .setLabel("Branches")
                        .setLabelColor(Color.BLACK)
                        .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.yellow, getTheme()))
                        .setLabelClickable(false)
                        .create()
        );

        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.nav_cart, R.drawable.add_cart)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.yellow, getTheme()))
                        .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.dark, getTheme()))
                        .setLabel("My Cart")
                        .setLabelColor(Color.BLACK)
                        .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.yellow, getTheme()))
                        .setLabelClickable(false)
                        .create()
        );

        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.nav_logout, R.mipmap.logout)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.yellow, getTheme()))
                        .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.dark, getTheme()))
                        .setLabel("Log Out")
                        .setLabelColor(Color.BLACK)
                        .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.yellow, getTheme()))
                        .setLabelClickable(false)
                        .create()
        );
    }

    public void onClickLogOut(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Do you want to log out? You may loose your data!");

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("nameKey"," ");
                editor.putString("phoneKey"," ");
                editor.putString("addressKey"," ");
                editor.commit();

                SQLiteDatabase db = helper.getReadableDatabase();
                db.delete("CART", null, null);

                Intent intent = new Intent(MainActivity.this, LoginPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        dialog.create().show();

    }

    public static Intent getPickImageIntent(Context context){
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();
        Intent pickIntent = new Intent (Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
        pickIntent.putExtra("return-data",true);
        pickIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(context)));
        intentList = addIntentsToList(context,intentList,pickIntent);
        intentList = addIntentsToList(context,intentList,takePhotoIntent);

        if(intentList.size()>0){
            chooserIntent= Intent.createChooser(intentList.remove(intentList.size()-1), "Choose from...");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,intentList.toArray(new Parcelable[]{}));
        }
        return chooserIntent;
    }

    private static List<Intent> addIntentsToList(Context context,List<Intent> list, Intent intent){
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent,0);
        for(ResolveInfo resolveInfo: resInfo){
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent= new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode!=RESULT_CANCELED){
            if(requestCode==1){
                //Log.d("where?","camera2");
                if(data.getExtras()==null && data.getData()==null){
                    Log.d("where?","camera");
                }else{
                    Uri pic= getImageFromResult(this,resultCode,data);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setData(Uri.parse("mailto:"));
                    intent.setType("vnd.android.cursor.dir/email");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"minusarraf96@gmail.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Prescription");
                    intent.putExtra(Intent.EXTRA_TEXT,userPhone);
                    intent.putExtra(Intent.EXTRA_STREAM, pic);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    //intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            }
        } else if(requestCode==60){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("prescriptionKey",true);
            editor.commit();
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    public Uri getImageFromResult(Context context, int resultCode,
                                  Intent imageReturnedIntent) {
        Bitmap bm = null;
        File imageFile = getTempFile(context);
        Uri selectedImage = null;
        if (resultCode == Activity.RESULT_OK) {
            
            boolean isCamera = (imageReturnedIntent == null ||
                    imageReturnedIntent.getData() == null  ||
                    imageReturnedIntent.getData().toString().contains(imageFile.toString()));
            if (isCamera) {
                Bitmap theImage=(Bitmap) imageReturnedIntent.getExtras().get("data");
                try{
                    File root = getExternalFilesDir(null);
                    Log.d("hello", String.valueOf(root));
                    if(root.canWrite()){
                        File pic=new File(root,"prescription.png");
                        picture = pic;
                        FileOutputStream out = new FileOutputStream(pic);
                        theImage.compress(Bitmap.CompressFormat.PNG,100,out);
                        out.flush();
                        out.close();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                selectedImage = FileProvider.getUriForFile(context,BuildConfig.APPLICATION_ID+".provider",picture);
            } else {
                selectedImage = imageReturnedIntent.getData();
            }
        }
        return selectedImage;
    }

    //show the matched list of dawais
    public void showList(String mText){

        int searchLength=3;

        //extracting data from database
        db=helper.getReadableDatabase();
        Cursor cursor = db.query("DAWAI",
                new String[] {"CODE","PACKING","BRANDNAME","GENERIC"},
                null,null,null,null,null);

        if(mText.length()<searchLength){   //when search length is not enough
            ArrayList<String> nothing = new ArrayList<>();
            CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(),nothing,nothing,nothing,nothing);
            recyclerView.setAdapter(customAdapter);

            LinearLayout relativeLayout = findViewById(R.id.relativeLayout);
            relativeLayout.setVisibility(View.VISIBLE);
        }else{
            //search length is reached.

            LinearLayout relativeLayout = findViewById(R.id.relativeLayout);
            relativeLayout.setVisibility(View.INVISIBLE);

            ArrayList<String> code = new ArrayList<>();
            ArrayList<String> packing = new ArrayList<>();
            ArrayList<String> brand = new ArrayList<>();
            ArrayList<String> generic = new ArrayList<>();

            boolean cursorValue = cursor.moveToFirst();
            String c,t,b,g;
            while(cursorValue){
                c=cursor.getString(0);
                t=cursor.getString(1);
                b=cursor.getString(2);
                g=cursor.getString(3);
                String sText=mText.toUpperCase(Locale.ENGLISH);
                if (c.contains(sText) || t.contains(sText)|| b.contains(sText) || g.contains(sText)){
                    code.add(cursor.getString(0));
                    packing.add(cursor.getString(1));
                    brand.add(cursor.getString(2));
                    generic.add(cursor.getString(3));
                }
                cursorValue=cursor.moveToNext();
            }

            //populating recycler view
            CustomAdapter customAdapter = new CustomAdapter(MainActivity.this,code,packing,brand,generic);
            recyclerView.setAdapter(customAdapter);

        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id= item.getItemId();
        if(id == R.id.branches){
            Intent intent = new Intent(MainActivity.this, Branches.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public void saveToDB(JSONObject mResponse){
        //saving data from google sheet into database
        try {
            JSONArray array = mResponse.getJSONArray("Sheet1");
            int totalData = array.length();
            JSONObject finalObject;
            db=helper.getReadableDatabase();

            int count=0;
            while(count<totalData){
                try{
                    //extracting data from json response
                    finalObject=array.getJSONObject(count);

                    String code = String.valueOf(finalObject.get("CODE"));
                    String type = String.valueOf(finalObject.get("TYPE"));
                    String bName = String.valueOf(finalObject.get("BRAND_NAME"));
                    String generic = String.valueOf(finalObject.get("GENERIC"));
                    String company = String.valueOf(finalObject.get("COMPANY"));
                    String packing = String.valueOf(finalObject.get("PACKING"));
                    Float mrp = Float.valueOf((finalObject.get("MRP").toString()));
                    Float price = Float.valueOf(String.valueOf(finalObject.get("PRICE")));
                    Integer maxOrder = Integer.valueOf(String.valueOf(finalObject.get("MAXORDER")));
                    Integer prescription = (Integer) finalObject.get("PRESCRIPTION");

                    //inserting into table
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("CODE",code);
                    contentValues.put("TYPE",type);
                    contentValues.put("GENERIC",generic);
                    contentValues.put("BRANDNAME",bName);
                    contentValues.put("COMPANY",company);
                    contentValues.put("PACKING",packing);
                    contentValues.put("MRP",mrp);
                    contentValues.put("PRICE",price);
                    contentValues.put("MAXORDER",maxOrder);
                    contentValues.put("PRESCRIPTION",prescription);
                    db.insert("DAWAI",null,contentValues);
                }catch(JSONException e){
                    e.printStackTrace();
                }catch(SQLiteException e){
                    Toast.makeText(this,"Database Unavailable",Toast.LENGTH_SHORT).show();
                }
                count += 1;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public class GetData extends AsyncTask<String, Integer, JSONObject> {
        //get request to a google sheet
        private JSONObject finalResponse = new JSONObject();
        ProgressDialog progressDialog;

        @Override
        protected JSONObject doInBackground(String... voids) {
            publishProgress(5);

            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
            //url of the google sheet
            //it should be kept in separate file at one place
            final String url = "https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec?id=1Otp-z4nZshefjvji-tBIKqyG6fO74G6sXZ9TfmMlQfI&sheet=Sheet1";
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray arr = response.getJSONArray("Sheet1");

                                //progressbar for syncing dawai
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                dawaiLoadingDialog.setVisibility(View.GONE);

                                db=helper.getReadableDatabase();
                                db.delete("DAWAI",null,null);

                                saveToDB(response);

                            } catch (JSONException e) {
                                Log.d("Error.Response", "fail");
                                e.printStackTrace();
                            }
                            finalResponse= response;

                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            dawaiLoadingDialog.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this,"Check Your Connection to Fetch Latest Data",Toast.LENGTH_SHORT).show();
                            Log.d("Error.Response", String.valueOf(error));
                        }
                    }
            );
            queue.add(getRequest);
            mResponse=finalResponse;
            return finalResponse;
        }

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }


}
