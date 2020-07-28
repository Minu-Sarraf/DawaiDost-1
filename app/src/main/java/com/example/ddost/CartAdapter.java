package com.example.ddost;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.MyViewHolder> {

    private ArrayList<String> code, brand, packing, generic = new ArrayList<>();
    private ArrayList<Float> price, mrp = new ArrayList<>();
    private ArrayList<Integer> quantity, prescription = new ArrayList<>();
    Context context;
    SQLiteDatabase db;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textGeneric, textPackingBrand, textMrpPrice, textPrescription, textQuantity, textCartPrice, textSaving;
        public ImageView imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            textGeneric = (TextView) itemView.findViewById(R.id.textGeneric);
            textPackingBrand= (TextView) itemView.findViewById(R.id.textBrand);
            textMrpPrice = (TextView) itemView.findViewById(R.id.textMrpPrice);
            textPrescription = (TextView) itemView.findViewById(R.id.textNeedPrescription);
            textQuantity = (TextView) itemView.findViewById(R.id.textQuantity);
            textCartPrice = (TextView) itemView.findViewById(R.id.textCartPrice);
            textSaving = (TextView) itemView.findViewById(R.id.textSaving);

            imageView = (ImageView) itemView.findViewById(R.id.image);

            imageView.setOnClickListener(this);

        }

        //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View view) {
            final int position = getAdapterPosition();
            if (view.getId() == R.id.image) {
                //delete selected item from cart
                SQLiteOpenHelper helper = new Database(context);
                db = helper.getReadableDatabase();
                final String cd = code.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete from cart?");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.delete("CART", "CODE=?", new String[]{cd});
                        dialog.dismiss();
                        Intent intent1 = new Intent(context, ShowCart.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent1.putExtra("DELETED","DELETED");
                        context.startActivity(intent1);
                        Toast.makeText(context, " DELETED", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();
            }
        }

    }

    public CartAdapter(Context context, ArrayList<String> code, ArrayList<String> brand,ArrayList<String> generic, ArrayList<String> packing,ArrayList<Float> mrp, ArrayList<Float> price, ArrayList<Integer> quantity, ArrayList<Integer> prescription) {
        this.code = code;
        this.generic = generic;
        this.brand = brand;
        this.packing= packing;
        this.mrp = mrp;
        this.price = price;
        this.quantity = quantity;
        this.prescription = prescription;

        this.context = context;
    }


    @NonNull
    @Override
    public CartAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_list, parent, false);

        return new CartAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.MyViewHolder holder, int position) {

        String cd = code.get(position);
        String gn = generic.get(position);
        String bd = brand.get(position);
        String pk = packing.get(position);
        Float mp = mrp.get(position);
        Float prc = price.get(position);
        Integer pcp = prescription.get(position);
        Integer order = quantity.get(position);

        holder.textGeneric.setText(gn);
        Float sv = ((mrp.get(position)-price.get(position))/mrp.get(position))*100;
        int saving = Math.round(sv);
        holder.textPackingBrand.setText(pk+" | "+bd);
        holder.textMrpPrice.setText("MRP Rs: "+mp+" | "+"DD Price Rs: "+prc);
        holder.textQuantity.setText(order+" Pcs");
        holder.textSaving.setText("Saving: "+saving+"%");

        if(pcp==1){
            holder.textPrescription.setText("Rx");
        }

        holder.textCartPrice.setText("Rs: "+String.format("%.02f",prc*order));

        holder.imageView.setImageResource(R.drawable.delete);
    }

    @Override
    public int getItemCount() {
        return code.size();
    }

}
