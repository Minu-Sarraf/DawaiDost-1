package com.example.ddost.ui.cart;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ddost.CartAdapter;
import com.example.ddost.Database;
import com.example.ddost.GetData;
import com.example.ddost.GetImage;
import com.example.ddost.MainActivity;
import com.example.ddost.NoConnection;
import com.example.ddost.R;
import com.example.ddost.SearchMedicine;
import com.example.ddost.SharedPreferencesValue;
import com.example.ddost.ShowCart;
import com.example.ddost.UpdateDetails;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static android.app.Activity.RESULT_CANCELED;

public class CartFragment extends Fragment {
    Context mContext;
    ArrayList<String> code= new ArrayList<>();
    ArrayList<String> brand= new ArrayList<>();
    ArrayList<String> generic= new ArrayList<>();
    ArrayList<String> packing= new ArrayList<>();
    ArrayList<Float> price= new ArrayList<>();
    ArrayList<Float> mrp= new ArrayList<>();
    ArrayList<Integer> prescription = new ArrayList<>();
    ArrayList<Integer> maxOrder = new ArrayList<>();

    public CartAdapter adapter;

    Float totalPrice= Float.valueOf(0);
    Float totalMrp= Float.valueOf(0);
    Integer delCharge;
    ArrayList<String> PinCode = new ArrayList<>();

    View root;
    Boolean diableMenuButton=false;

    SQLiteOpenHelper helper;
    SQLiteDatabase db;
    ConstraintLayout myCart;
    GetData getData;

    int valuePrescription=0;
    GetImage getImage;

    SharedPreferencesValue sharedPreferencesValue;
    static boolean syncedRate = false;

    public CartFragment(){}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.activity_show_cart, container, false);

        mContext=getContext();

        CardView cardView = root.findViewById(R.id.card_view);

        //showing items in cart
        helper=new Database(getContext());
        db = helper.getReadableDatabase();

        Button button = root.findViewById(R.id.add_medicine);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        ImageView imageView = root.findViewById(R.id.logo);

        SQLiteOpenHelper helper = new Database(getContext());
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor images = db.query("IMAGES",
                new String[]{"CART"},
                null,null,null,null,null);
        ArrayList<String> image = new ArrayList<>();
        boolean cursorValue = images.moveToFirst();
        while(cursorValue){
            image.add(images.getString(0));
            cursorValue=images.moveToNext();
        }

        Picasso.get().load(image.get(0))
                .fit()
                .into(imageView);
        images.close();

        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        }
        if(!connected){
            Intent intent = new Intent(getContext(), NoConnection.class);
            startActivity(intent);
        }

        //user details
        sharedPreferencesValue = new SharedPreferencesValue(getContext());
        sharedPreferencesValue.setSharedPreferences();

        //get delivery charge
            GetDelivery getDelivery = new GetDelivery();
            String url="";
            myCart = root.findViewById(R.id.myCart);
            getData= new GetData(getContext(),getActivity(),url,"Delivery");
            getData.showProgressBar();
            myCart.setVisibility(View.INVISIBLE);
            getDelivery.execute();



        //getting images
        getImage=new GetImage(getContext(),"CART");


        //search Medicines
        SearchView searchView = root.findViewById(R.id.searchView);
        RecyclerView recyclerView1 = root.findViewById(R.id.recycler_view1);
        LinearLayout linearLayout = root.findViewById(R.id.relativeLayout);
        LinearLayout linearLayout1 = root.findViewById(R.id.textTotalPrice);

        SearchMedicine searchMedicine = new SearchMedicine(getContext(),recyclerView1,searchView,linearLayout,linearLayout1);
        searchMedicine.search();

        try {

            Cursor cursors = db.query("CART",
                    new String[]{"CODE","TYPE","BRANDNAME","GENERIC","PACKING","MRP","PRICE", "MAXORDER", "TOTAL","PRESCRIPTION"},
                    null, null, null, null, null);

            //recycler view
            cursorValue = cursors.moveToFirst();

            while(cursorValue) {

                Cursor cursor = db.query("DAWAI",
                        new String[]{"CODE","TYPE","BRANDNAME","GENERIC","PACKING","MRP","PRICE", "MAXORDER", "SAVINGS","PRESCRIPTION"},
                        "CODE=?",
                        new String[]{cursors.getString(0)},
                        null,null,null);

                if(cursor.moveToFirst()){
                    code.add(cursor.getString(0));
                    brand.add(cursor.getString(2));
                    generic.add(cursor.getString(3));
                    packing.add(cursor.getString(4));
                    mrp.add(cursor.getFloat(5));
                    price.add(cursor.getFloat(6));
                    maxOrder.add(cursors.getInt(7));
                    prescription.add(cursor.getInt(9));

                    totalPrice+=cursor.getFloat(6)*cursors.getInt(7);
                    valuePrescription += cursor.getInt(9);
                    totalMrp+=cursor.getFloat(5)*cursors.getInt(7);
                }
                cursorValue= cursors.moveToNext();
            }

            cursors.close();

        }catch(SQLiteException e) {
            Toast toast = Toast.makeText(mContext, "Database Unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        //setting the total value and sub total value
        TextView textView = root.findViewById(R.id.totalMrpPrice);
        textView.setText("Rs "+String.format("%.02f",totalMrp));
        textView=root.findViewById(R.id.totalDdPrice);
        textView.setText("Rs "+String.format("%.02f",totalPrice));
        textView=root.findViewById(R.id.totalSavingPercent);
        Float savingPercent = ((1-(totalPrice/totalMrp))*100);
        textView.setText("Rs "+String.format("%.02f",totalMrp-totalPrice)+" ("+String.format("%.0f",savingPercent)+"%)");


        if (code.size()==0){
            ImageView image1 = root.findViewById(R.id.no_item);
            image1.setVisibility(View.VISIBLE);

            image1 = root.findViewById(R.id.logo);
            image1.setVisibility(View.INVISIBLE);

            RelativeLayout relativeLayout = root.findViewById(R.id.relative_layout);
            relativeLayout.setVisibility(View.VISIBLE);

            cardView.setVisibility(View.INVISIBLE);

            searchView.setVisibility(View.INVISIBLE);
            linearLayout1.setVisibility(View.INVISIBLE);

            diableMenuButton=true;

            getActivity().invalidateOptionsMenu();

        }

        final RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        adapter = new CartAdapter(getContext(),code,brand,generic,packing,mrp,price,maxOrder,prescription);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_item,menu);
        if(diableMenuButton){
            menu.findItem(R.id.sendOrder).setEnabled(false);
            menu.findItem(R.id.clearCart).setEnabled(false);
        }else{
            menu.findItem(R.id.sendOrder).setEnabled(true);
            menu.findItem(R.id.clearCart).setEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.sendOrder:
                boolean connected=false;
                ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    connected = true;
                }
                if(!connected){
                    Toast.makeText(getContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();
                }else{
                    checkPrescription();
                }
                return true;
            case R.id.clearCart:
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Clear Cart!");
                builder.setMessage("Are you sure you want to clear your cart?");
                builder.setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.delete("CART",null,null);
                        Toast.makeText(getContext(),"Cart Cleared",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        Intent intent1 = new Intent(getContext(), ShowCart.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent1);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void checkPrescription(){
        boolean preferenceCheck = sharedPreferencesValue.getPrescription();
        if(valuePrescription>0 && !preferenceCheck){
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Prescription");
            builder.setMessage("Please send prescription for the selected medicines!");

            builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if(ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED){
                        requestPermissions(
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},1881);
                    }else{
                        Intent intent = getImage.getPickImageIntent(getContext());
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
            Intent intent = new Intent(getContext(),UpdateDetails.class);
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Bitmap rbitmap;
        String userImage;
        if(resultCode!=RESULT_CANCELED){
            if(requestCode==1){
                if(data.getExtras()==null && data.getData()==null){
                    Log.d("where?","camera");
                }else{
                    Uri pic= getImage.getImageFromResult(getContext(),resultCode,data);
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), pic);
                        rbitmap = getImage.getResizedBitmap(bitmap,500);//Setting the Bitmap to ImageView
                        userImage = getImage.getStringImage(rbitmap);
                        getImage.uploadPhoto(userImage);
                        if(getImage.getResponse()){
                            Intent intent = new Intent(getContext(),UpdateDetails.class);
                            startActivity(intent);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void setDeliveryCharge(){
        String pin = sharedPreferencesValue.getPincode();
        delCharge=0;

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

        Log.d("check", String.valueOf(PinCode.contains(pin)));
        Log.d("check", String.valueOf(cursorValue1));
        Log.d("check", String.valueOf(cursorValue2));

        Cursor cursor = db.query("RATE",
                new String[]{"RATE"},
                "LOCALZIP=?",
                new String[]{pin},
                null,null,null);

        if(cursor.moveToFirst()){
            delCharge= Integer.valueOf(cursor1.getString(0));
        }else{
            delCharge= Integer.valueOf(cursor2.getString(0));
        }
        cursor1.close();
        cursor2.close();
        cursor.close();

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
        cursor3.close();

        TextView textView = root.findViewById(R.id.totalDeliveryCharge);
        float dc = Float.valueOf(delCharge);
        textView.setText("Rs "+String.format("%.02f",dc));
        textView=root.findViewById(R.id.totalPrice);
        textView.setText("Rs "+String.format("%.02f",totalPrice+delCharge));
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1881){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Intent intent = getImage.getPickImageIntent(getContext());
                startActivityForResult(intent,1);
            }
        }
    }


    public class GetDelivery extends AsyncTask<String, Integer, JSONObject> {
        //get request to a google sheet
        private JSONObject finalResponse = new JSONObject();

        @Override
        protected JSONObject doInBackground(String... voids) {
            publishProgress(5);

            RequestQueue queue = Volley.newRequestQueue(getContext());
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
                                getData.hideProgressBar();

                                db.delete("RATE",null,null);

                                storeDelivery(response);
                                setDeliveryCharge();
                                myCart.setVisibility(View.VISIBLE);

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
                            getData.hideProgressBar();
                            setDeliveryCharge();
                            myCart.setVisibility(View.VISIBLE);
                            Toast.makeText(getContext(),"Check Your Connection to Fetch Latest Data",Toast.LENGTH_SHORT).show();
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
