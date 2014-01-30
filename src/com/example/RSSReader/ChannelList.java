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
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import java.util.ArrayList;


public class ChannelList extends Activity {
    /**
     * Called when the activity is first created.
     */
    int currentReload = 0;
    boolean reloadStart = false;
    private final String lenta = "http://lenta.ru/rss";
    private final String lentaName = "Лента";
    IntentFilter filter;
    String data[] = {"Delete", "Change"};
    StartLoadCatcher broadcast = new StartLoadCatcher();
    FinishLoadCatcher finishLoadCatcher = new FinishLoadCatcher();
    CurrentLoadingCatcher loadingCatcher = new CurrentLoadingCatcher();
    AlertDialog.Builder builder;
    int currentChannel = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //broadcasts
        filter = new IntentFilter(UpdateIntentService.start);
        registerReceiver(broadcast, filter);
        filter = new IntentFilter(UpdateIntentService.current);
        registerReceiver(loadingCatcher, filter);
        filter = new IntentFilter(UpdateIntentService.success);
        registerReceiver(finishLoadCatcher, filter);

        ChannelDataBaseHelper dataBaseHelper = new ChannelDataBaseHelper(getApplicationContext());
        if(dataBaseHelper.isEmpty()){
            if(checkConnection()){
                dataBaseHelper.createListTable();
                dataBaseHelper.setSettings(120000);
                Intent intentService = new Intent(this, UpdateIntentService.class);
                intentService.putExtra("time", dataBaseHelper.getReloadTime());
                startService(intentService);
                dataBaseHelper.close();
            }else{
                Toast toast = Toast.makeText(getApplicationContext(), R.string.internetAvailable, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }else{
            dataBaseHelper.close();
            drawList(true);

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

    private void drawList(boolean firstStart){
        ListView view = (ListView) findViewById(R.id.channelList);
        ChannelDataBaseHelper dataBaseHelper = new ChannelDataBaseHelper(getApplicationContext());
        final ArrayList<String> names = dataBaseHelper.getChannelNameList();
        final ArrayList<String> links = dataBaseHelper.getChannelLinkList();
        if(names.size() == 0){
            names.add(getResources().getString(R.string.noChannels));
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(ChannelList.this, android.R.layout.simple_list_item_1, names);
            view.setAdapter(adapter);
            return;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ChannelList.this, android.R.layout.simple_list_item_1, names);
        view.setAdapter(adapter);
        view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                currentChannel = i;
                if(reloadStart && currentReload < i){
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.channelUpdating, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }else{
                    showDialog(0);
                }
                return true;
            }
        });
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(reloadStart && currentReload <= i){
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.channelUpdating, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }else{
                    if(names.get(i).equals(getResources().getString(R.string.updateError))){
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.updateError, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    } else{
                        Intent channel = new Intent(ChannelList.this, NewsActivity.class);
                        channel.putExtra("link", links.get(i));
                        channel.putExtra("ID", i);
                        startActivity(channel);
                    }

                }
            }
        });
        if(firstStart){
            if(checkConnection()){
                Intent intentService = new Intent(this, UpdateIntentService.class);
                intentService.putExtra("time", dataBaseHelper.getReloadTime());
                startService(intentService);
            }else{
                Toast toast = Toast.makeText(getApplicationContext(), R.string.internetAvailable, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }else{
            Toast toast = Toast.makeText(getApplicationContext(), R.string.updatingSuccess, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    public void onReloadClickListener(View view){
        if(reloadStart){
            Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.reloading), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }
        if(checkConnection()){
            TextView text = (TextView) findViewById(R.id.appNameView);
            text.setText(getResources().getString(R.string.reloadStarted));
            Intent intentService = new Intent(this, UpdateIntentService.class);
            intentService.putExtra("time", 0);
            startService(intentService);
        }else{
            Toast toast = Toast.makeText(getApplicationContext(), R.string.internetAvailable, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    public void onSettingsClickListener(View vies){
        Intent intent = new Intent(ChannelList.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onAddClickListener(View view){
        Intent intent = new Intent(ChannelList.this, AddChannelActivity.class);
        intent.putExtra("name", lentaName);
        intent.putExtra("link", lenta);
        startActivity(intent);
        finish();
    }

    public class StartLoadCatcher extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            reloadStart = true;
            TextView text = (TextView) findViewById(R.id.appNameView);
            text.setText(R.string.updating);
        }
    }

    public class CurrentLoadingCatcher extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            currentReload = intent.getExtras().getInt("number");
            TextView text = (TextView) findViewById(R.id.appNameView);
            text.setText(new Integer(currentReload + 1).toString() +  getResources().getString(R.string.channelsReloaded));
        }
    }

    public class FinishLoadCatcher extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            currentReload = 0;
            reloadStart = false;
            TextView text = (TextView) findViewById(R.id.appNameView);
            text.setText(R.string.app_name);
            drawList(false);
        }
    }


    protected Dialog onCreateDialog(int id){
        builder = new AlertDialog.Builder(this);
        switch (id){
            case 0:
                builder.setItems(data, myClickListener);
                break;
            case 1:
                builder.setPositiveButton(getResources().getString(R.string.YES), myDelClickListener);
                builder.setNegativeButton(getResources().getString(R.string.NO), myDelClickListener);
                builder.setTitle(getResources().getString(R.string.areYouSure));
                break;
            default:
                break;
        }
        return builder.create();
    }

    OnClickListener myClickListener = new OnClickListener(){
        public void onClick(DialogInterface dialog, int id){
            switch (id){
                case 0:
                    showDialog(1);
                    break;
                case 1:
                    Intent intent = new Intent(ChannelList.this, AddChannelActivity.class);
                    ChannelDataBaseHelper dataBaseHelper = new ChannelDataBaseHelper(getApplicationContext());
                    String name = dataBaseHelper.getChannelNameList().get(currentChannel);
                    String link = dataBaseHelper.getChannelLinkList().get(currentChannel);
                    dataBaseHelper.deleteChannel(link);
                    intent.putExtra("link", link);
                    intent.putExtra("name", name);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };

    OnClickListener myDelClickListener = new OnClickListener(){
        public void onClick(DialogInterface dialog, int id){
            switch (id){
                case Dialog.BUTTON_POSITIVE:
                    ChannelDataBaseHelper channelDataBaseHelper = new ChannelDataBaseHelper(getApplicationContext());
                    String link = channelDataBaseHelper.getChannelLinkList().get(currentChannel);
                    channelDataBaseHelper.deleteChannel(link);
                    Intent intent = new Intent(ChannelList.this, ChannelList.class);
                    startActivity(intent);
                    finish();
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    //finish();
                    break;
            }
        }
    };
}
