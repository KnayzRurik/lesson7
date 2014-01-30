package com.example.RSSReader;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Created with IntelliJ IDEA.
 * User: Дмитрий
 * Date: 25.01.14
 * Time: 3:06
 * To change this template use File | Settings | File Templates.
 */
public class SettingsActivity extends Activity {
    int time = 2 * 60000;
    int[] times = {2*60000, 5 * 60000, 10 * 60000, 30 * 60000, 2 * 60 * 60000, 0};

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        final Spinner spinner = (Spinner)findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {
                time = times[selectedItemPosition];
            }
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void onSaveClick(View view){
        ChannelDataBaseHelper dataBaseHelper = new ChannelDataBaseHelper(getApplicationContext());
        dataBaseHelper.setSettings(time);
        Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.settingToast), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        finish();
    }
}
