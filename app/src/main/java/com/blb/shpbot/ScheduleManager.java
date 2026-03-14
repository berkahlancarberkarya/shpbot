package com.blb.shpbot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class ScheduleManager {

    public static void schedule(Context ctx,int hour,int minute){

        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY,hour);
        cal.set(Calendar.MINUTE,minute);
        cal.set(Calendar.SECOND,0);

        AlarmManager am =
                (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(ctx,PublishReceiver.class);

        PendingIntent pi =
                PendingIntent.getBroadcast(
                        ctx,
                        hour*100+minute,
                        i,
                        PendingIntent.FLAG_IMMUTABLE
                );

        am.setRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pi
        );

    }

}