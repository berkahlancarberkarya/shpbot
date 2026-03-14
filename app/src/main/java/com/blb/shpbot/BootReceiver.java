package com.blb.shpbot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent launchIntent =
                context.getPackageManager()
                        .getLaunchIntentForPackage("com.shopee.id");

        if(launchIntent != null){

            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(launchIntent);

        }

    }

}