package com.example.ddost.ui.account;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ddost.Database;
import com.example.ddost.IntroduceFriend;
import com.example.ddost.LoadWebView;
import com.example.ddost.LoginPage;
import com.example.ddost.MainActivity;
import com.example.ddost.R;
import com.example.ddost.SendQuery;
import com.example.ddost.SharedPreferencesValue;
import com.example.ddost.UserInfo;

public class AccountFragment extends Fragment {
    ListView listView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account, container, false);
        listView = root.findViewById(R.id.list_view);

        final String[] accountTablist = {
                "Edit Infomation",
                "FAQ",
                "Introduce a Friend",
                "Privacy Policy",
                "Send a Query",
                "Shipping and Return Policy",
                "Terms and Conditions",
                "About Us",
                "Log Out"
        };

        ArrayAdapter<String> listAdapter =
                new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, accountTablist);

        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("clicked",String.valueOf(position));
                Intent intent=null;
                switch(position){
                    case 4:
                        intent= new Intent(getContext(), SendQuery.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                        break;
                    case 2:
                        intent = new Intent(getContext(), IntroduceFriend.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                        break;
                    case 0:
                        intent = new Intent(getContext(), UserInfo.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                        break;
                    case 1:
                        intent = new Intent(getContext(),LoadWebView.class);
                        intent.putExtra("NAME",accountTablist[position]);
                        intent.putExtra("URL","https://dawaidost.com/faq-ext/");
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                        break;
                    case 3:
                        intent = new Intent(getContext(),LoadWebView.class);
                        intent.putExtra("NAME",accountTablist[position]);
                        intent.putExtra("URL","https://dawaidost.com/privacy-policy/");
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                        break;
                    case 5:
                        intent = new Intent(getContext(),LoadWebView.class);
                        intent.putExtra("NAME",accountTablist[position]);
                        intent.putExtra("URL","https://dawaidost.com/order-terms-and-conditions/");
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                        break;
                    case 6:
                        intent = new Intent(getContext(),LoadWebView.class);
                        intent.putExtra("NAME",accountTablist[position]);
                        intent.putExtra("URL","https://dawaidost.com/terms-and-conditions/");
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                        break;
                    case 7:
                        intent = new Intent(getContext(), LoadWebView.class);
                        intent.putExtra("NAME",accountTablist[position]);
                        intent.putExtra("URL","https://dawaidost.com/about-dawai-dost/");
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                        break;
                    case 8:
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Log Out?");
                        builder.setMessage("Are you sure you want to log out? \n You may loose your data!");

                        builder.setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                SQLiteOpenHelper helper = new Database(getContext());
                                SQLiteDatabase db = helper.getReadableDatabase();
                                db.delete("CART",null,null);
                                db.delete("RATE",null,null);

                                SharedPreferencesValue sharedPreferencesValue = new SharedPreferencesValue(getContext());
                                sharedPreferencesValue.setSharedPreferences();
                                sharedPreferencesValue.setValues(" "," "," "," "," "," "," "," "," ");
                                sharedPreferencesValue.setPrescription(false);

                                Intent intent = new Intent(getContext(), LoginPage.class);
                                startActivity(intent);
                                getActivity().overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                                getActivity().finish();
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
        });

        return root;
    }
}
