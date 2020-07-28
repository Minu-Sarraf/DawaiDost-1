package com.example.ddost.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddost.Database;
import com.example.ddost.GetImage;
import com.example.ddost.R;
import com.example.ddost.SearchAdapter;
import com.example.ddost.SearchMedicine;
import com.example.ddost.SharedPreferencesValue;
import com.example.ddost.ShowCart;
import com.example.ddost.Splash;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    Context context;
    View root;
    GetImage getImage;

    SearchView searchView;
    SQLiteOpenHelper helper;
    SQLiteDatabase db;

    Bitmap rbitmap;
    String userImage;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_home, container, false);

        ImageView imageView = root.findViewById(R.id.samajhdar);
        ImageView imageView2 = root.findViewById(R.id.home_page);

        helper= new Database(getContext());
        db= helper.getReadableDatabase();
        Cursor images = db.query("IMAGES",
                new String[]{"HOME"},
                null,null,null,null,null);
        ArrayList<String> image = new ArrayList<>();
        boolean cursorValue = images.moveToFirst();
        while(cursorValue){
            image.add(images.getString(0));
            cursorValue=images.moveToNext();
        }


        Picasso
                .get()
                .load(image.get(0))
                .resize(2000,2000)
                .into(imageView2);
        images.close();


        searchView = root.findViewById(R.id.searchView);
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        LinearLayout linearLayout = root.findViewById(R.id.relativeLayout);
        LinearLayout linearLayout1 = root.findViewById(R.id.relativeLayout2);

        SearchMedicine searchMedicine = new SearchMedicine(getContext(),recyclerView,searchView,linearLayout,linearLayout1);
        searchMedicine.search();

        getImage = new GetImage(getContext(),"HOME");

        imageView = root.findViewById(R.id.prescription);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        return root;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Intent intent = new Intent(Intent.ACTION_SEND);
        if(resultCode!=RESULT_CANCELED){
            if(requestCode==1){
                if(data.getExtras()==null && data.getData()==null){
                    //Log.d("where?","camera");
                }else{
                    Uri pic= getImage.getImageFromResult(getContext(),resultCode,data);
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), pic);
                        //rbitmap = getImage.getResizedBitmap(bitmap,500);//Setting the Bitmap to ImageView
                        userImage = getImage.getStringImage(bitmap);
                        getImage.uploadPhoto(userImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void showMessage(String string){
        Toast.makeText(getContext(),string,Toast.LENGTH_SHORT).show();
    }
}
