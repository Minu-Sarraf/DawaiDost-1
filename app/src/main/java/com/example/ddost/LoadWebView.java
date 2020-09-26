package com.example.ddost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import java.util.Objects;

public class LoadWebView extends AppCompatActivity{
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_web_view);

        //backbutton
        (getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        url = intent.getStringExtra("URL");
        String title = intent.getStringExtra("NAME");
        setTitle(title);

        WebView mWebView = findViewById(R.id.webView);
        mWebView.loadUrl(url);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:

                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
