package com.example.dawaidost;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private ArrayList<String> code, type, brand, generic = new ArrayList<>();
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView textView1, textView2, textCode;
        public ImageView imageView;
        public LinearLayout linearLayout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.row);
            textView1 = (TextView) itemView.findViewById(R.id.text1);
            textView2 = (TextView) itemView.findViewById(R.id.text2);
            textCode = (TextView) itemView.findViewById(R.id.code);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            linearLayout.setOnClickListener(this);
            imageView.setOnClickListener(this);

        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View view) {
            int position=getAdapterPosition();
            switch(view.getId()){
                case R.id.row:
                case R.id.image:
                    //go to add cart page
                    Intent intent = new Intent(context, AddCart.class);
                    intent.putExtra("Code",code.get(position));
                    intent.putExtra("Type",type.get(position));
                    intent.putExtra("Brand",brand.get(position));
                    intent.putExtra("Generic"   ,generic.get(position));
                    context.startActivity(intent);

                    break;
            }
        }

    }

    public CustomAdapter(Context context,ArrayList<String> code, ArrayList<String> type, ArrayList<String> brand, ArrayList<String> generic){
        this.code=code;
        this.type=type;
        this.brand= brand;
        this.generic= generic;
        this.context= context;

    }



    @NonNull
    @Override
    public CustomAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view,parent,false);


        return new CustomAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomAdapter.MyViewHolder holder, int position) {

        String cd = code.get(position);
        String tp = type.get(position);
        String bd = brand.get(position);
        String gn = generic.get(position);
        //change to different column
        holder.textCode.setText(cd + ": ");
        holder.textView1.setText(gn);
        //indicate type n brand
        holder.textView2.setText("Type: " + tp+",  Brand: "+bd);
        //add to cart image
        holder.imageView.setImageResource(R.drawable.add_cart);

    }

    @Override
    public int getItemCount() {
        return code.size();
    }
}
