package com.example.RSSReader;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Дмитрий
 * Date: 25.01.14
 * Time: 0:47
 * To change this template use File | Settings | File Templates.
 */
public class UpdateIntentService extends IntentService {
    ArrayList<String> titles = new ArrayList<String>();
    ArrayList<String> descriptions = new ArrayList<String>();
    ArrayList<String> links = new ArrayList<String>();
    ArrayList<String> names = new ArrayList<String>();
    public final static String start = "reloadStart";
    public final static String success = "reloadSuccess";
    public final static String current = "currentLoad";

    boolean result = false;

    public UpdateIntentService(){
        super("Service");
    }

    @Override
    public void onHandleIntent(Intent intent){
        int reloadTime  = intent.getExtras().getInt("time");
        Intent responce = new Intent();
        responce.setAction(start);
        sendBroadcast(responce);
        ChannelDataBaseHelper dataBaseHelper = new ChannelDataBaseHelper(getApplicationContext());
        links = dataBaseHelper.getChannelLinkList();
        names = dataBaseHelper.getChannelNameList();
        for(int i = 0; i < links.size(); i++){
            try{
                SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                SAXParser saxParser = saxParserFactory.newSAXParser();
                saxParser.parse(new ByteArrayInputStream(EntityUtils.toString(new DefaultHttpClient().execute(
                        new HttpGet(links.get(i))).getEntity()).getBytes()), new RSSHandler());
                result = true;
            } catch (IOException e){
                //result remain false
            } catch (SAXException e){
                //result remain false
            } catch (ParserConfigurationException e){
                //result remain false
            }
            catch (Exception e){
                //result remain false
            }
            if(result){
                dataBaseHelper.pushChannel(names.get(i), links.get(i), titles, descriptions);
            }else{
                dataBaseHelper.pushChannel(names.get(i), links.get(i), null, null);
            }
            descriptions.clear();
            titles.clear();
            responce = new Intent();
            responce.putExtra("number", i);
            responce.setAction(current);
            sendBroadcast(responce);
        }
        responce = new Intent();
        responce.setAction(success);
        sendBroadcast(responce);
        if (reloadTime != 0){
            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            manager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + reloadTime, reloadTime, pendingIntent);
        }
    }


    private class RSSHandler extends DefaultHandler{
        private String ITEM = "entry";
        private String SUMMARY = "summary";
        private String CONTENT = "content";
        private String TITLE = "title";
        private String CHECK_ATOM = "rss";
        private String buffer = "";
        private String currentDescription = "";
        private String currentTitle = "";
        int num = 0;
        boolean item = false,
                summary = false,
                content = false,
                title = false,
                atom = true;

        public void setDocumentLocator(Locator locator){
            locator.getLineNumber();
        }

        public void startElement(String uri, String localName, String qName,
                                 Attributes attrs)throws SAXException{
            if(num > 20)
                return;
            buffer = "";
            if(CHECK_ATOM.equals(localName)){
                atom = false;
                ITEM = "item";
                SUMMARY = "description";
                CONTENT = "";
            }

            if(ITEM.equals(localName)){
                item = true;
            }

            if(SUMMARY.equals(localName) && item){
                summary = true;
                currentDescription = "";
            }

            if(CONTENT.equals(localName) && item){
                content = true;
                currentDescription = "";
            }

            if(TITLE.equals(localName) && item){
                title = true;
                currentTitle = "";
            }
        }

        public void endElement(String uri, String localName, String qName)throws SAXException{
            if(num > 20)
                return;
            if(ITEM.equals(localName)){
                item = false;
                descriptions.add(currentDescription);
                titles.add(currentTitle);
                num++;
            }

            if(SUMMARY.equals(localName)){
                currentDescription += buffer + "<br>";
            }

            if(CONTENT.equals(localName)){
                currentDescription += buffer + "<br>";
            }

            if(TITLE.equals(localName))
                currentTitle += buffer;

            buffer = "";
        }

        @Override
        public void characters(char[] ch, int start, int length){
            if(num > 20)
                return;
            if(item)
                buffer += new String(ch, start, length);

        }

    }
}
