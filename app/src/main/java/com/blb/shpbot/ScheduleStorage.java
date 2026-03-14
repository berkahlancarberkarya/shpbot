package com.blb.shpbot;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ScheduleStorage {

    static String PREF="schedule_pref";
    static String KEY="schedule_list";

    public static void save(Context ctx,List<String> list){

        SharedPreferences sp =
                ctx.getSharedPreferences(PREF,Context.MODE_PRIVATE);

        JSONArray arr=new JSONArray(list);

        sp.edit().putString(KEY,arr.toString()).apply();

    }

    public static List<String> load(Context ctx){

        List<String> result=new ArrayList<>();

        SharedPreferences sp =
                ctx.getSharedPreferences(PREF,Context.MODE_PRIVATE);

        String json=sp.getString(KEY,"[]");

        try{

            JSONArray arr=new JSONArray(json);

            for(int i=0;i<arr.length();i++){

                result.add(arr.getString(i));

            }

        }catch(Exception e){}

        return result;

    }

}