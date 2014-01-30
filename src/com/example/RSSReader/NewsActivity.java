package com.example.RSSReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Дмитрий
 * Date: 25.01.14
 * Time: 2:45
 * To change this template use File | Settings | File Templates.
 */
public class NewsActivity extends Activity {
    boolean reloadStart;
    int currentReload = 0;
    String link = "";
    IntentFilter filter;
    StartLoadCatcher broadcast = new StartLoadCatcher();
    FinishLoadCatcher finishLoadCatcher = new FinishLoadCatcher();
    CurrentLoadingCatcher loadingCatcher = new CurrentLoadingCatcher();


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newslist);
        link = getIntent().getExtras().getString("link");
        filter = new IntentFilter(UpdateIntentService.start);
        registerReceiver(broadcast, filter);
        filter = new IntentFilter(UpdateIntentService.current);
        registerReceiver(loadingCatcher, filter);
        filter = new IntentFilter(UpdateIntentService.success);
        registerReceiver(finishLoadCatcher, filter);
        drawList(link);
    }


    private void drawList(String name){
        ListView view = (ListView) findViewById(R.id.newsListView);
        ChannelDataBaseHelper dataBaseHelper = new ChannelDataBaseHelper(getApplicationContext());
        final ArrayList<String> titles = dataBaseHelper.getChannelTitles(link);
        final ArrayList<String> descriptions = dataBaseHelper.getChannelDescriptions(link);
        if(titles.size() == 0){
            titles.add(getResources().getString(R.string.channelError));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles);
        view.setAdapter(adapter);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(reloadStart && currentReload <= getIntent().getExtras().getInt("ID")){
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.channelUpdating, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }else{
                    Intent channel = new Intent(NewsActivity.this, PostViewActivity.class);
                    channel.putExtra("title", titles.get(i));
                    channel.putExtra("description", descriptions.get(i));
                    startActivity(channel);
                }
            }
        });
    }

    public class StartLoadCatcher extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            reloadStart = true;
            TextView text = (TextView) findViewById(R.id.textView);
            text.setText(R.string.updating);
        }
    }

    public class CurrentLoadingCatcher extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            currentReload = intent.getExtras().getInt("number");
            TextView text = (TextView) findViewById(R.id.textView);
            text.setText(new Integer(currentReload).toString() + getResources().getString(R.string.channelsReloaded));
        }
    }

    public class FinishLoadCatcher extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            currentReload = 0;
            reloadStart = false;
            TextView text = (TextView) findViewById(R.id.textView);
            text.setText(R.string.app_name);
            drawList(link);
        }
    }

    public void onReload1ClickListener(View view){
        if(reloadStart){
            Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.reloadStarted), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }
        if(checkConnection()){
            TextView text = (TextView) findViewById(R.id.textView);
            text.setText("Обновление началось!");
            Intent intentService = new Intent(this, UpdateIntentService.class);
            intentService.putExtra("time", 0);
            startService(intentService);
        }else{
            Toast toast = Toast.makeText(getApplicationContext(), R.string.internetAvailable, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    public boolean checkConnection(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(manager == null){
            return false;
        }
        NetworkInfo[] networkInfos = manager.getAllNetworkInfo();
        for(NetworkInfo currentInfo : networkInfos){
            if(currentInfo.getTypeName().equalsIgnoreCase("WIFI") || currentInfo.getTypeName().equalsIgnoreCase("MOBILE"))
                if(currentInfo.isConnected())
                    return true;
        }
        return false;
    }


}
