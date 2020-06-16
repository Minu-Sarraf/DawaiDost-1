package com.example.dawaidost;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BranchAdapter extends RecyclerView.Adapter<BranchAdapter.MyViewHolder> {

    private ArrayList<String> branchName= new ArrayList<>();
    private ArrayList<String> openingTime = new ArrayList<>();
    private ArrayList<String> mapLink = new ArrayList<>();
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView textGeneric, textPrice,textCode;
        public ImageView imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textCode= (TextView) itemView.findViewById(R.id.code);
            textGeneric = (TextView) itemView.findViewById(R.id.text1);
            textGeneric.setPaintFlags(textGeneric.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
            textPrice = (TextView) itemView.findViewById(R.id.text2);
            imageView = (ImageView) itemView.findViewById(R.id.image);

            textGeneric.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position=getAdapterPosition();
            switch(view.getId()){
                case R.id.text1:
                    String url = mapLink.get(position);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    context.startActivity(intent);
                    break;
            }
        }

    }

    public BranchAdapter(Context context,ArrayList<String> branchName, ArrayList<String> mapLink, ArrayList<String> openingTime){
        this.branchName=branchName;
        this.mapLink=mapLink;
        this.openingTime=openingTime;
        this.context= context;

    }



    @NonNull
    @Override
    public BranchAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view,parent,false);

        return new BranchAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BranchAdapter.MyViewHolder holder, int position) {

        String branch=branchName.get(position);
        String time=  openingTime.get(position);
        holder.textGeneric.setText(branch);
        holder.textPrice.setText("Opens: "+time);
    }

    @Override
    public int getItemCount() {
        return branchName.size();
    }

}