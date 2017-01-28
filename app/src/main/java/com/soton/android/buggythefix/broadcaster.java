package com.soton.android.buggythefix;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by AndroidHPE on 1/28/2017.
 */

public class broadcaster extends IntentService {
    //this class do not have default constructor
    public broadcaster()
    {
        super("mybroadcaster");
    }
    public static boolean broadcasting=false;

    @Override
    protected void onHandleIntent(Intent intent)
    {
        while ( broadcasting) {
            Intent broadcastintent = new Intent();
            broadcastintent.setAction("com.soton.android.bugysqliteapp");
            broadcastintent.putExtra("Message", "This is always running service");
            sendBroadcast(broadcastintent,"android.permission.SEND_SMS");
            try
            {
                Thread.sleep(3000);
            }catch (Exception ex){}


        }

    }
}