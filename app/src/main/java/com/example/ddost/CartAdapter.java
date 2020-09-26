package com.example.ddost;

import android.app.Activity;
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

                        CartDeleteUpdate cartAdd = new CartDeleteUpdate(context);
                        new CartDeleteUpdate.DeleteDataActivity(context).execute();
                        cartAdd.sendData();

                        Intent intent = new Intent(context,ShowCart.class);
                        context.startActivity(intent);
                        Activity activity = (Activity) context;
                        activity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

                        code.remove(position);
                        brand.remove(position);
                        generic.remove(position);
                        packing.remove(position);
                        price.remove(position);
                        mrp.remove(position);
                        quantity.remove(position);
                        prescription.remove(position);
                        notifyDataSetChanged();

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
        holder.textMrpPrice.setText("MRP Rs: "+String.format("%.02f",mp)+" | "+"DD Price Rs: "+String.format("%.02f",prc));
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
