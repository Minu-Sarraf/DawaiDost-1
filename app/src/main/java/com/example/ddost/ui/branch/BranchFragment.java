package com.example.ddost.ui.branch;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ddost.BranchAdapter;
import com.example.ddost.Database;
import com.example.ddost.GetData;
import com.example.ddost.MainActivity;
import com.example.ddost.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BranchFragment extends Fragment {

    ArrayList<String> branchName= new ArrayList<>();
    ArrayList<String> openingTime = new ArrayList<>();
    ArrayList<String> mapLink = new ArrayList<>();
    SQLiteOpenHelper helper;
    SQLiteDatabase db;

    View root;

    static boolean syncedBranches=false;

    ProgressBar branchDialog;

    BranchAdapter adapter;
    GetData getData;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_branch, container, false);

        helper =  new Database(getContext());
        db=helper.getReadableDatabase();

        String url = "https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec?id=1XcU1TbA56-JNM0Qsj9ihyt3mgzFGVWeHFFIUn-7_4wM&sheet=BRANCHES";

        GetBranches getBranches = new GetBranches();
        getData = new GetData(getContext(),getActivity(),url,"Branches");
        getData.showProgressBar();
        getBranches.execute();

        return root;
    }

    public void loadDatabase(){
        Cursor cursor = db.query("BRANCHES",
                new String[] {"LOCATION","LINK","OPENTIME"},
                null,null,null,null,null);
        boolean cursorValue= cursor.moveToFirst();
        while(cursorValue){
            branchName.add(cursor.getString(0));
            mapLink.add(cursor.getString(1));
            openingTime.add(cursor.getString(2));

            cursorValue= cursor.moveToNext();
        }

        cursor.close();

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        BranchAdapter adapter = new BranchAdapter(getContext(),branchName,mapLink,openingTime);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    public void storeBranches(JSONObject response){
        try {
            JSONArray array = response.getJSONArray("BRANCHES");
            int totalData = array.length()-1;
            JSONObject finalObject;

            int count=0;
            while(count<totalData){
                try{
                    //extracting data from json response
                    finalObject=array.getJSONObject(count);

                    String location = String.valueOf(finalObject.get("Location"));
                    String link =String.valueOf(finalObject.get("Link"));
                    String time =String.valueOf(finalObject.get("Opening_Hours"));

                    ContentValues contentValues = new ContentValues();
                    contentValues.put("LOCATION",location);
                    contentValues.put("LINK",link);
                    contentValues.put("OPENTIME",time);

                    db.insert("BRANCHES",null,contentValues);

                    branchName.add(location);
                    mapLink.add(link);
                    openingTime.add(time);

                }catch(JSONException e){
                    e.printStackTrace();
                }
                count += 1;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        adapter = new BranchAdapter(getContext(),branchName,mapLink,openingTime);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }


    public class GetBranches extends AsyncTask<String, Integer, JSONObject> {
        //get request to a google sheet
        private JSONObject finalResponse = new JSONObject();

        @Override
        protected JSONObject doInBackground(String... voids) {
            publishProgress(5);

            RequestQueue queue = Volley.newRequestQueue(getContext());
            //url of the google sheet
            //it should be kept in separate file at one place
            final String url = "https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec?id=1XcU1TbA56-JNM0Qsj9ihyt3mgzFGVWeHFFIUn-7_4wM&sheet=BRANCHES";
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray arr = response.getJSONArray("BRANCHES");

                                getData.hideProgressBar();

                                db.delete("BRANCHES",null,null);

                                storeBranches(response);

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
                            Toast.makeText(getContext(),"Check Your Connection to Fetch Latest Data",Toast.LENGTH_SHORT).show();
                            Log.d("Error.Response", String.valueOf(error));
                            getData.hideProgressBar();
                            loadDatabase();
                        }
                    }
            );
            queue.add(getRequest);
            //mResponse=finalResponse;
            return finalResponse;
        }

    }

}
