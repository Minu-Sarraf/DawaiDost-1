package com.example.dawaidost;


import android.content.Context;
import android.content.Intent;
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


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private ArrayList<String> code, packing, brand, generic = new ArrayList<>();
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

        //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View view) {
            int position=getAdapterPosition();
            switch(view.getId()){
                case R.id.row:
                    Intent intent = new Intent(context, AddCart.class);
                    intent.putExtra("Code",code.get(position));
                    intent.putExtra("Type", packing.get(position));
                    intent.putExtra("Brand",brand.get(position));
                    intent.putExtra("Generic",generic.get(position));
                    context.startActivity(intent);
                    break;
                case R.id.image:
                    //go to add cart page
                    Log.d("code",code.get(position));
                    Intent intent1 = new Intent(context, AddCart.class);
                    intent1.putExtra("Code",code.get(position));
                    intent1.putExtra("Type", packing.get(position));
                    intent1.putExtra("Brand",brand.get(position));
                    intent1.putExtra("Generic",generic.get(position));
                    context.startActivity(intent1);
                    break;
            }
        }

    }

    public CustomAdapter(Context context,ArrayList<String> code, ArrayList<String> type, ArrayList<String> brand, ArrayList<String> generic){
        this.code=code;
        this.packing =type;
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
        String tp = packing.get(position);
        String bd = brand.get(position);
        String gn = generic.get(position);
        //change to different column
        holder.textCode.setText(cd + ": ");
        holder.textView1.setText(gn);
        //indicate type n brand
        holder.textView2.setText("Packing: " + tp+",  Brand: "+bd);
        //add to cart image
        holder.imageView.setImageResource(R.mipmap.add_cart);

    }

    @Override
    public int getItemCount() {
        return code.size();
    }
}
