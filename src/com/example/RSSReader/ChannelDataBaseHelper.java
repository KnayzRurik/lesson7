package com.example.RSSReader;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Дмитрий
 * Date: 25.01.14
 * Time: 0:08
 * To change this template use File | Settings | File Templates.
 */
public class ChannelDataBaseHelper extends SQLiteOpenHelper implements BaseColumns {
    public static final String DATABASE_NAME = "rssDataba";
    public static final String CHANNEL_LIST_BASE = "channels";
    public static final String CHANNEL_NAME_ROW = "channemName";
    public static final String CHANNEL_LINK_ROW  = "channelLink";
    public static final String TITLE_ROW = "title";
    public static final String DESCRIPTION_ROW = "description";
    public static final String SETTINGS_BASE = "settings";
    public static final String RELOAD_TIME_ROW = "time";
    public static final String TABLE_NAME_ROW = "name";
    private static final String support = "table";

    public ChannelDataBaseHelper(Context context){
        super(context, DATABASE_NAME, null, 1);
    }

    public boolean isEmpty(){
        SQLiteDatabase liteDatabase = this.getReadableDatabase();
        if(liteDatabase == null) {
            liteDatabase.close();
            return true;
        }
        else{
            try{
                Cursor cursor = liteDatabase.query(CHANNEL_LIST_BASE, null, null, null, null, null, null);
                if(cursor.getCount() == 0){
                    cursor.close();
                    liteDatabase.close();
                    return true;
                }
                cursor.close();
                liteDatabase.close();
            }catch (Exception e){
                return true;
            }finally {
                liteDatabase.close();
            }
            return false;
        }

    }

    public void createListTable(){
        SQLiteDatabase liteDatabase = this.getWritableDatabase();
        liteDatabase.execSQL("DROP TABLE IF EXISTS " + CHANNEL_LIST_BASE);
        liteDatabase.execSQL("CREATE TABLE " + CHANNEL_LIST_BASE +
                " (" + ChannelDataBaseHelper._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + CHANNEL_NAME_ROW + " TEXT, " +
                CHANNEL_LINK_ROW + " TEXT, " + TABLE_NAME_ROW + " INTEGER);");
        liteDatabase.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + CHANNEL_LIST_BASE +
                " (" + ChannelDataBaseHelper._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + CHANNEL_NAME_ROW + " TEXT, " +
                CHANNEL_LINK_ROW + " TEXT, " + TABLE_NAME_ROW + " INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old, int nw){
        db.execSQL("DROP TABLE IF EXISTS " + CHANNEL_LIST_BASE);
        db.execSQL("CREATE TABLE " + CHANNEL_LIST_BASE +
                " (" + this._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + CHANNEL_NAME_ROW + " TEXT, " +
                CHANNEL_LINK_ROW + " TEXT, " + TABLE_NAME_ROW + " INTEGER);");
    }

    public boolean pushChannel(String channelName, String channelLink, ArrayList<String> titles, ArrayList<String> descriptions){
        int tt = getNewName();
        String name = support + new Integer(tt).toString();
        SQLiteDatabase liteDatabase = this.getWritableDatabase();
        String table = "";
        Cursor cur = liteDatabase.query(CHANNEL_LIST_BASE, null, null, null, null, null, null);
        while(cur.moveToNext()){
            if(cur.getString(cur.getColumnIndex(CHANNEL_LINK_ROW)).equals(channelLink))
                table = cur.getString(cur.getColumnIndex(TABLE_NAME_ROW));
        }
        cur.close();
        if(!table.equals(""))
            liteDatabase.execSQL(makeQuery(false, support + table));
        liteDatabase.execSQL(makeQuery(false, name));
        liteDatabase.execSQL(makeQuery(true, name));
        ContentValues values = new ContentValues();
        values.put(CHANNEL_LINK_ROW, channelLink);
        Cursor cursor = liteDatabase.query(CHANNEL_LIST_BASE, null, null, null, null, null, null);
        liteDatabase.execSQL("DELETE FROM " + CHANNEL_LIST_BASE + " WHERE " + CHANNEL_LINK_ROW + " = '" + channelLink + "'");
        cursor.close();
        values.put(CHANNEL_NAME_ROW, channelName);
        values.put(TABLE_NAME_ROW, tt);
        liteDatabase.insert(CHANNEL_LIST_BASE, null, values);
        if(titles == null)
            return true;
        for(int i = 0; i < titles.size(); i++){
            ContentValues value = new ContentValues();
            value.put(TITLE_ROW, titles.get(i));
            value.put(DESCRIPTION_ROW, descriptions.get(i));
            liteDatabase.insert(name, null, value);
        }
        liteDatabase.close();
        return true;
    }

    public void deleteChannel(String channelLink){
        SQLiteDatabase liteDatabase = this.getWritableDatabase();
        String table = "";
        Cursor cur = liteDatabase.query(CHANNEL_LIST_BASE, null, null, null, null, null, null);
        while(cur.moveToNext()){
            if(cur.getString(cur.getColumnIndex(CHANNEL_LINK_ROW)).equals(channelLink))
                table = cur.getString(cur.getColumnIndex(TABLE_NAME_ROW));
        }
        cur.close();
        if(!table.equals(""))
            liteDatabase.execSQL(makeQuery(false, support + table));
        liteDatabase.execSQL("DELETE FROM " + CHANNEL_LIST_BASE + " WHERE " + CHANNEL_LINK_ROW + " = '" + channelLink + "'");
        liteDatabase.close();
    }

    public ArrayList<String> getChannelNameList(){
        ArrayList<String> respons = new ArrayList<String>();
        SQLiteDatabase liteDatabase = this.getReadableDatabase();
        Cursor cursor = liteDatabase.query(CHANNEL_LIST_BASE, null, null, null , null, null, null);
        while(cursor.moveToNext()){
            respons.add(cursor.getString(cursor.getColumnIndex(CHANNEL_NAME_ROW)));
        }
        cursor.close();
        liteDatabase.close();
        return respons;
    }

    public ArrayList<String> getChannelLinkList(){
        ArrayList<String> respons = new ArrayList<String>();
        SQLiteDatabase liteDatabase = this.getReadableDatabase();
        Cursor cursor = liteDatabase.query(CHANNEL_LIST_BASE, null, null, null , null, null, null);
        while(cursor.moveToNext()){
            respons.add(cursor.getString(cursor.getColumnIndex(CHANNEL_LINK_ROW)));
        }
        cursor.close();
        liteDatabase.close();
        return respons;
    }

    public ArrayList<String> getChannelTitles(String channelLink){
        ArrayList<String> respons = new ArrayList<String>();
        SQLiteDatabase liteDatabase = this.getReadableDatabase();
        String table = "";
        Cursor cur = liteDatabase.query(CHANNEL_LIST_BASE, null, null, null, null, null, null);
        while(cur.moveToNext()){
            if(cur.getString(cur.getColumnIndex(CHANNEL_LINK_ROW)).equals(channelLink))
                table = cur.getString(cur.getColumnIndex(TABLE_NAME_ROW));
        }
        cur.close();
        Cursor cursor = liteDatabase.query(support + table, null, null, null , null, null, null);
        while(cursor.moveToNext()){
            respons.add(cursor.getString(cursor.getColumnIndex(TITLE_ROW)));
        }
        cursor.close();
        liteDatabase.close();
        return respons;
    }

    public ArrayList<String> getChannelDescriptions(String channelLink){
        ArrayList<String> respons = new ArrayList<String>();
        SQLiteDatabase liteDatabase = this.getReadableDatabase();
        String table = "";
        Cursor cur = liteDatabase.query(CHANNEL_LIST_BASE, null, null, null, null, null, null);
        while(cur.moveToNext()){
            if(cur.getString(cur.getColumnIndex(CHANNEL_LINK_ROW)).equals(channelLink))
                table = cur.getString(cur.getColumnIndex(TABLE_NAME_ROW));
        }
        cur.close();
        Cursor cursor = liteDatabase.query(support + table, null, null, null , null, null, null);
        while(cursor.moveToNext()){
            respons.add(cursor.getString(cursor.getColumnIndex(DESCRIPTION_ROW)));
        }
        cursor.close();
        liteDatabase.close();
        return respons;
    }

    public void setSettings(int time){
        SQLiteDatabase liteDatabase = this.getWritableDatabase();
        liteDatabase.execSQL(makeQuery(false, SETTINGS_BASE));
        liteDatabase.execSQL("CREATE TABLE " + SETTINGS_BASE +
                " (" + this._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + RELOAD_TIME_ROW + " INTEGER);");
        ContentValues values = new ContentValues();
        values.put(RELOAD_TIME_ROW, time);
        liteDatabase.insert(SETTINGS_BASE, null, values);
        liteDatabase.close();
    }

    public int getReloadTime(){
        SQLiteDatabase liteDatabase = this.getReadableDatabase();
        Cursor cursor = liteDatabase.query(SETTINGS_BASE, null, null, null, null, null, null);
        cursor.moveToNext();
        int ans = cursor.getInt(cursor.getColumnIndex(RELOAD_TIME_ROW));
        liteDatabase.close();
        return ans;
    }

    public int getNewName(){
        SQLiteDatabase liteDatabase = this.getReadableDatabase();
        Cursor cursor = liteDatabase.query(CHANNEL_LIST_BASE, null, null, null, null, null, null);
        int max = 0;
        while(cursor.moveToNext()){
            max = Math.max(max, cursor.getInt(cursor.getColumnIndex(TABLE_NAME_ROW)));
        }
        return max + 1;
    }

    // create/delete channel table
    private String makeQuery(boolean create, String tableName){
        if(create)
            return "CREATE TABLE " + tableName +
                " (" + ChannelDataBaseHelper._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TITLE_ROW + " TEXT, " +
                    DESCRIPTION_ROW + " TEXT);";
        else
            return "DROP TABLE IF EXISTS " + tableName;
    }
}
