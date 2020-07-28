package com.example.ddost;

import android.app.ActionBar;
import android.content.Context;

public class SendSheet {
    String url;
    String data;
    String mResponse;
    Context context;

    public SendSheet(Context context, String url, String data){
        this.url = url;
        this.data = data;
        this.context = context;
    }

    public void execute(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                postData();
            }
        });
        t.start();
/*        while(t.isAlive()){
            if(mResponse==null){

            }
        }*/
    }

    public void postData(){
        HttpRequest mRequest = new HttpRequest();
        mResponse = mRequest.sendPost(url,data);
    }
}
