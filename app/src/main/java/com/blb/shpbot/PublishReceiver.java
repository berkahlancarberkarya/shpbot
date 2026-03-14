package com.blb.shpbot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class PublishReceiver extends BroadcastReceiver {

    static String TAG = "SHPBOT";

    @Override
    public void onReceive(Context context, Intent intent) {

        restartShopee(context);

    }

    public static void restartShopee(Context ctx){

        Log.d(TAG,"Schedule trigger");

        try{

            Intent launchIntent =
                    ctx.getPackageManager()
                            .getLaunchIntentForPackage("com.shopee.id");

            if(launchIntent != null){

                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                ctx.startActivity(launchIntent);

                Log.d(TAG,"Start Shopee");

            }

        }catch(Exception e){
            Log.e(TAG,"Gagal membuka Shopee");
        }

        // tunggu Shopee load
        new Handler().postDelayed(() -> {

            boolean needRestart = true;

            if(MyAccessibilityService.instance != null){

                int page = MyAccessibilityService.instance.getCurrentPage();

                if(page != -1){

                    Log.d(TAG,"Shopee sudah di halaman benar → tidak restart");

                    MyAccessibilityService.instance.botPaused = false;
                    MyAccessibilityService.instance.STEP = 0;

                    needRestart = false;
                }
            }

            if(!needRestart){
                return;
            }

            Log.d(TAG,"Restart Shopee");

            if(MyAccessibilityService.instance != null){

                MyAccessibilityService.instance.botPaused = true;

                Log.d(TAG,"CALL restartShopee service");

                MyAccessibilityService.instance.restartShopee();

            }

        },9000);

    }
}