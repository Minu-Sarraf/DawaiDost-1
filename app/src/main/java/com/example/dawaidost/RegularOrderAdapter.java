package com.example.dawaidost;

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

public class RegularOrderAdapter extends RecyclerView.Adapter<RegularOrderAdapter.MyViewHolder> {

    private ArrayList<String> code, brand = new ArrayList<>();
    private ArrayList<Float> price = new ArrayList<>();
    private ArrayList<Integer> maxOrder = new ArrayList<>();
    Context context;
    SQLiteDatabase db;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView textGeneric, textPrice,textCode;
        public ImageView imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textCode= (TextView) itemView.findViewById(R.id.code);
            textGeneric = (TextView) itemView.findViewById(R.id.text1);
            textPrice = (TextView) itemView.findViewById(R.id.text2);
            imageView = (ImageView) itemView.findViewById(R.id.image);

            //imageView.setOnClickListener(this);

        }

        //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View view) {
            int position=getAdapterPosition();
            switch(view.getId()){
                case R.id.image:
                    //delete selected item from cart
                    SQLiteOpenHelper helper = new Database(context);
                    db=helper.getReadableDatabase();
                    final String cd= code.get(position);

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete from cart?");

                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            db.delete("CART","CODE=?",new String[] {cd});
                            dialog.dismiss();
                            Intent intent1 = new Intent(context,MyCart.class);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(intent1);
                            Toast.makeText(context," DELETED",Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.create().show();
                    break;
            }
        }

    }

    public RegularOrderAdapter(Context context,ArrayList<String> code, ArrayList<Float> price, ArrayList<Integer> maxOrder, ArrayList<String> brand){
        this.code=code;
        this.price=price;
        this.maxOrder=maxOrder;
        this.brand = brand;
        this.context= context;

    }



    @NonNull
    @Override
    public RegularOrderAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view,parent,false);

        return new RegularOrderAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RegularOrderAdapter.MyViewHolder holder, int position) {

        String cd = code.get(position);
        Float prc = price.get(position);
        Integer order = maxOrder.get(position);
        String gn = brand.get(position);
        holder.textCode.setText(cd+": ");
        holder.textGeneric.setText(gn);
        holder.textPrice.setText("Rs "+prc+" * "+order+" pcs = Rs "+String.format("%.02f",prc*order));
        //holder.imageView.setImageResource(R.drawable.delete);

    }

    @Override
    public int getItemCount() {
        return code.size();
    }


}
