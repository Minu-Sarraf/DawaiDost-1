package com.example.ddost;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static androidx.core.graphics.TypefaceCompatUtil.getTempFile;
import static com.example.ddost.Configuration.ADD_USER_URL;
import static com.example.ddost.Configuration.KEY_ACTION;
import static com.example.ddost.Configuration.KEY_ID;
import static com.example.ddost.Configuration.KEY_IMAGE;
import static com.example.ddost.Configuration.KEY_NAME;

public class GetImage {
    File picture;
    Context context;
    boolean imageResponse=false;
    String from;

    public GetImage (Context context,String from){
        this.context =context;
        this.from = from;
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
                    File root = context.getExternalFilesDir(null);
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

    public  Intent getPickImageIntent(Context context){
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

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);

    }


    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        return encodedImage;
    }

    public void uploadPhoto(final String userImage){
        final SharedPreferencesValue sharedPreferencesValue = new SharedPreferencesValue(context);
        sharedPreferencesValue.setSharedPreferences();

        final ProgressDialog loading = ProgressDialog.show(context,"Uploading...","Please wait...",false,false);


        StringRequest stringRequest = new StringRequest(Request.Method.POST,ADD_USER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        imageResponse=true;
                        loading.dismiss();
                        Toast.makeText(context,"Prescription Uploaded Successfully",Toast.LENGTH_LONG).show();
                        SharedPreferencesValue sharedPreferencesValue = new SharedPreferencesValue(context);
                        sharedPreferencesValue.setSharedPreferences();
                        sharedPreferencesValue.setPrescription(true);
                        if(from.equals("CART")){
                            Intent intent = new Intent (context,UpdateDetails.class);
                            context.startActivity(intent);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            Toast.makeText(context, "Timeout, check your internet connection", Toast.LENGTH_LONG).show();
                        } else if (error instanceof AuthFailureError) {
                            Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
                        } else if (error instanceof ServerError) {
                            Toast.makeText(context, "Error uploading", Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(context, "Error image", Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(context, "Error uploading image", Toast.LENGTH_LONG).show();
                        }
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put(KEY_ACTION,"insert");
                params.put(KEY_ID,sharedPreferencesValue.getPhone());
                params.put(KEY_NAME,sharedPreferencesValue.getName());
                params.put(KEY_IMAGE,userImage);

                return params;
            }

        };

        int socketTimeout = 30000; // 30 seconds. You can change it
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        stringRequest.setRetryPolicy(policy);


        RequestQueue requestQueue = Volley.newRequestQueue(context);

        requestQueue.add(stringRequest);
    }

    public boolean getResponse(){
        return imageResponse;
    }
}
