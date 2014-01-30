package com.example.RSSReader;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: Дмитрий
 * Date: 25.01.14
 * Time: 3:58
 * To change this template use File | Settings | File Templates.
 */
public class PostViewActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.postlayout);
        TextView text = (TextView) findViewById(R.id.titleView);
        text.setTextSize(30);
        text.setText(getIntent().getExtras().getString("title"));
        WebView webView = (WebView) findViewById(R.id.postView);
        webView.loadData(getIntent().getExtras().getString("description").trim().replace("\n", "<br>"), "text/html; charset=utf-8", null);
    }
}
