package com.example.dawaidost;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static androidx.core.graphics.TypefaceCompatUtil.getTempFile;

public class ShowCart extends AppCompatActivity {

    SQLiteOpenHelper helper = new Database(ShowCart.this);
    ArrayList<String> code = new ArrayList<>();
    ArrayList<Float> price = new ArrayList<>();
    ArrayList<Integer> maxOrder = new ArrayList<>();
    ArrayList<String> brand = new ArrayList<>();
    public CartAdapter adapter;
    String address, ip;
    Float totalPrice= Float.valueOf(0);
    Cursor cursor;
    boolean connected = false;
    int valuePrescription=0;
    File picture;

    ProgressBar deliveryDialog;

    SQLiteDatabase db;

    final static Integer emailCode = 1000;

    ArrayList<String> PinCode = new ArrayList<>();
    Intent intent = new Intent(Intent.ACTION_SEND);

    boolean preferenceCheck;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_cart);
        setTitle("My Cart");

        db=helper.getReadableDatabase();

        //Show progressbar while volley request is serviced
        deliveryDialog = new ProgressBar(this,null,android.R.attr.progressBarStyleLargeInverse);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        ((FrameLayout)getWindow().getDecorView().findViewById(android.R.id.content)).addView(deliveryDialog,params);
        deliveryDialog.setVisibility(View.VISIBLE);  //To show ProgressBar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        ShowCart.GetData getData = new ShowCart.GetData();
        getData.execute("hello");


        //back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //getting ip address
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        address = info.getMacAddress();
        ip = Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());


        //check internet connection
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        }
        else{
            connected = false;
        }

        getPreferenceCheck();

        //floating action button to send data
        FloatingActionButton fb= findViewById(R.id.fab);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(valuePrescription>0 && !preferenceCheck){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(ShowCart.this);
                    builder.setTitle("Prescription");
                    builder.setMessage("Please send prescription for the selected medicines!");

                    builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if(ContextCompat.checkSelfPermission(ShowCart.this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED){
                                ActivityCompat.requestPermissions(ShowCart.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},0);
                            }else{
                                Intent intent = getPickImageIntent(ShowCart.this);
                                startActivityForResult(intent,1);
                            }
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }else{
                    Intent intent = new Intent(ShowCart.this,UpdateDetails.class);
                    startActivity(intent);
                }
            }
        });

        try {
            //showing items in cart

            cursor = db.query("CART",
                    new String[]{"CODE","TYPE","BRANDNAME","GENERIC","COMPANY","PRICE", "MAXORDER", "TOTAL","PRESCRIPTION"},
                    null, null, null, null, null);

            //recycler view
            boolean cursorValue = cursor.moveToFirst();

            while(cursorValue) {
                code.add(cursor.getString(0));
                price.add(cursor.getFloat(5));
                maxOrder.add(cursor.getInt(6));
                brand.add(cursor.getString(2));
                totalPrice+=cursor.getFloat(7);
                valuePrescription += cursor.getInt(8);
                cursorValue= cursor.moveToNext();
            }
            cursor.close();
        }catch(SQLiteException e) {
            Toast toast = Toast.makeText(ShowCart.this, "Database Unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        //setting the total value and sub total value
        TextView textView = findViewById(R.id.showPrice);

        String pin = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("pinKey"," ");
        Integer delCharge=0;
        Cursor cursor1 = db.query("RATE",
                new String[] {"RATE"},
                "DELIVERYTYPE=?",
                new String[]{"Local"},
                null,null,null);
        boolean cursorValue1=cursor1.moveToFirst();

        Cursor cursor2 = db.query("RATE",
                new String[] {"RATE"},
                "DELIVERYTYPE=?",
                new String[]{"Non Local"},
                null,null,null);
        boolean cursorValue2 = cursor2.moveToFirst();

        if(PinCode.contains(pin) && cursorValue1){
            delCharge= Integer.valueOf(cursor1.getString(0));
        }else if(cursorValue2){
            delCharge= Integer.valueOf(cursor2.getString(0));
        }
        cursor1.close();
        cursor2.close();

        Cursor cursor3 = db.query("RATE",
                new String[] {"PRICE"},
                "DELIVERYTYPE=?",
                new String[]{"Free Delivery"},
                null,null,null);
        int freeDelivery;
        if(cursor3.moveToFirst()){
            freeDelivery = Integer.parseInt(cursor3.getString(0));
            if(totalPrice>freeDelivery){
                delCharge=0;
            }
        }

        totalPrice=totalPrice+delCharge;
        textView.setText(" Rs "+String.format("%.02f",totalPrice) );

        textView = findViewById(R.id.showDeliveryPrice);
        textView.setText(" Rs "+delCharge);

        if (code.size()==0){
            ImageView image = findViewById(R.id.logo);
            image.setVisibility(View.INVISIBLE);

            RelativeLayout relativeLayout = findViewById(R.id.relative_layout);
            relativeLayout.setVisibility(View.VISIBLE);

            fb.setVisibility(View.INVISIBLE);

            CardView cardView = findViewById(R.id.totalPrice);
            cardView.setVisibility(View.INVISIBLE);

            cardView = findViewById(R.id.subTotal);
            cardView.setVisibility(View.INVISIBLE);
        }

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        // Create adapter passing in the sample user data
        adapter = new CartAdapter(ShowCart.this,code,price,maxOrder, brand);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                goToHome();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        goToHome();
        super.onBackPressed();
    }

    public void goToHome(){
        Intent intent = new Intent(ShowCart.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
        sharedPreferences = getSharedPreferences("MyPrefs",Context.MODE_PRIVATE);
        String userPhone = sharedPreferences.getString("phoneKey", " ");

        Log.d("request", String.valueOf(requestCode));
        Log.d("request", String.valueOf(resultCode));
        if(resultCode!=RESULT_CANCELED){
            if(requestCode==1){
                if(data.getExtras()==null && data.getData()==null){
                    Log.d("where?","camera");
                }else{
                    Uri pic= getImageFromResult(this,resultCode,data);
                    intent.setData(Uri.parse("mailto:"));
                    intent.setType("vnd.android.cursor.dir/email");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"trymesan204@gmail.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Prescription");
                    intent.putExtra(Intent.EXTRA_TEXT,userPhone);
                    intent.putExtra(Intent.EXTRA_STREAM, pic);
                    intent.putExtra(Intent.EXTRA_RETURN_RESULT,true);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent,60);
                    }
                }
            }
        }else if(requestCode==60){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("prescriptionKey",true);
            editor.commit();
            getPreferenceCheck();
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    public void getPreferenceCheck(){
        //prescription sent or not
        sharedPreferences= getSharedPreferences("MyPrefs",Context.MODE_PRIVATE);
        preferenceCheck = sharedPreferences.getBoolean("prescriptionKey",false);
    }

    public Uri getImageFromResult(Context context, int resultCode,
                                  Intent imageReturnedIntent){
        Bitmap bm = null;
        File imageFile = getTempFile(context);
        Uri selectedImage = null;
        if (resultCode == Activity.RESULT_OK){
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

    public void storeDelivery(JSONObject response){
        try {
            JSONArray array = response.getJSONArray("DEL");
            int totalData = array.length();
            JSONObject finalObject;

            int count=0;
            while(count<totalData){
                try{
                    //extracting data from json response
                    finalObject=array.getJSONObject(count);

                    String deliveryType = String.valueOf(finalObject.get("DELIVERY_TYPE"));
                    String price =String.valueOf(finalObject.get("PRICE"));
                    String rate =String.valueOf(finalObject.get("RATE"));
                    String localZip =String.valueOf(finalObject.get("LOCAL_ZIP"));

                    ContentValues contentValues = new ContentValues();
                    contentValues.put("DELIVERYTYPE",deliveryType);
                    contentValues.put("PRICE",price);
                    contentValues.put("RATE",rate);
                    contentValues.put("LOCALZIP",localZip);

                    db.insert("RATE",null,contentValues);

                    PinCode.add(localZip);

                }catch(JSONException e){
                    e.printStackTrace();
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

        @Override
        protected JSONObject doInBackground(String... voids) {
            publishProgress(5);

            RequestQueue queue = Volley.newRequestQueue(ShowCart.this);
            //url of the google sheet
            //it should be kept in separate file at one place
            final String url = "https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec?id=1XcU1TbA56-JNM0Qsj9ihyt3mgzFGVWeHFFIUn-7_4wM&sheet=DEL";
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray arr = response.getJSONArray("DEL");

                                //progressbar for syncing dawai
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                deliveryDialog.setVisibility(View.GONE);

                                db.delete("RATE",null,null);

                                storeDelivery(response);

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
                            deliveryDialog.setVisibility(View.GONE);
                            Toast.makeText(ShowCart.this,"Check Your Connection to Fetch Latest Data",Toast.LENGTH_SHORT).show();
                            Log.d("Error.Response", String.valueOf(error));
                        }
                    }
            );
            queue.add(getRequest);
            //mResponse=finalResponse;
            return finalResponse;
        }

    }
}
