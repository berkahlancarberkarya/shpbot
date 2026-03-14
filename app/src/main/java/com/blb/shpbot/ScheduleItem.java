package com.blb.shpbot;

public class ScheduleItem {

    public int hour;
    public int minute;

    public ScheduleItem(int h,int m){
        hour=h;
        minute=m;
    }

    public String getText(){
        return String.format("%02d:%02d",hour,minute);
    }

}