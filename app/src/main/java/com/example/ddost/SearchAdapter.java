package com.example.ddost;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> {

    private ArrayList<String> code, brand, generic, company, packing= new ArrayList<>();
    ArrayList<Float> mrp, price,saving = new ArrayList<>();
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView textGeneric, textBrand, textPackingCompany, textMrpPrice, textNeedPrescription, textQuantity, textCartPrice;
        public ImageView imageView;
        public LinearLayout linearLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.row);

            textGeneric = (TextView) itemView.findViewById(R.id.textGeneric);
            textBrand = (TextView) itemView.findViewById(R.id.textBrand);
            textPackingCompany = (TextView) itemView.findViewById(R.id.textPackingCompany);
            textMrpPrice = (TextView) itemView.findViewById(R.id.textMrpPrice);
            textNeedPrescription = (TextView) itemView.findViewById(R.id.textNeedPrescription);
            textQuantity= (TextView) itemView.findViewById(R.id.textQuantity);
            textCartPrice = (TextView) itemView.findViewById(R.id.textCartPrice);

            imageView = (ImageView) itemView.findViewById(R.id.image);
            linearLayout.setOnClickListener(this);
            imageView.setOnClickListener(this);
        }

        //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View view) {
            int position=getAdapterPosition();
            switch(view.getId()){
                case R.id.row:
                case R.id.image:
                    Intent intent = new Intent(context, AddCart.class);
                    intent.putExtra("Code",code.get(position));
                    intent.putExtra("Type", packing.get(position));
                    intent.putExtra("Brand",brand.get(position));
                    intent.putExtra("Generic",generic.get(position));
                    context.startActivity(intent);
                    break;
            }
        }

    }

    public SearchAdapter(Context context,ArrayList<String> code, ArrayList<String> brand, ArrayList<String> generic, ArrayList<String> company, ArrayList<String> packing, ArrayList<Float> saving, ArrayList<Float> mrp, ArrayList<Float> price){
        this.code=code;
        this.brand= brand;
        this.generic= generic;
        this.company=company;
        this.packing =packing;
        this.saving=saving;
        this.mrp=mrp;
        this.price=price;
        this.context= context;
    }



    @NonNull
    @Override
    public SearchAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_list,parent,false);


        return new SearchAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.MyViewHolder holder, int position) {

        String cd = code.get(position);
        String pk = packing.get(position);
        String bd = brand.get(position);
        String gn = generic.get(position);
        String cp = company.get(position);
        Float mp= mrp.get(position);
        Float pr = (price.get(position));
        Float sv = saving.get(position)*100;
        int saving = Math.round(sv);
        //change to different column
        holder.textGeneric.setText(cd+ ": "+gn);
        holder.textBrand.setText("Brand: "+bd);
        holder.textPackingCompany.setText("Packing: "+pk+" | Company: "+cp);
        holder.textMrpPrice.setText("MRP: "+String.format("%.02f",mp)+" | DD Price: "+String.format("%.02f",pr)+" | Saving "+saving+"%");

        //add to cart image
        holder.imageView.setImageResource(R.mipmap.add_cart);
        holder.textCartPrice.setText("Add to Cart");

    }

    @Override
    public int getItemCount() {
        return code.size();
    }
}

