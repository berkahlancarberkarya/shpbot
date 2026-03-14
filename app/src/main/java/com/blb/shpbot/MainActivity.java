package com.blb.shpbot;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import android.widget.Switch;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {

    RecyclerView listSchedule;

    FloatingActionButton btnAdd;

    TextView txtStatus;

    Button btnAccessibility;
    Button btnPublishNow;

    Switch switchBot;
    SharedPreferences prefs;

    List<String> schedules;

    ScheduleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        txtStatus=findViewById(R.id.txtStatus);
        btnAccessibility=findViewById(R.id.btnAccessibility);
        btnPublishNow=findViewById(R.id.btnPublishNow);

        listSchedule=findViewById(R.id.listSchedule);
        btnAdd=findViewById(R.id.btnAddSchedule);

        // AUTO CEK ACCESSIBILITY
        if(!isAccessibilityEnabled()){

            startActivity(
                    new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            );

        }

        btnAccessibility.setOnClickListener(v->

                startActivity(
                        new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                )

        );

        // TOMBOL TEST PUBLISH
        btnPublishNow.setOnClickListener(v->{

            PublishReceiver.restartShopee(this);

        });

        schedules=ScheduleStorage.load(this);

        adapter=new ScheduleAdapter(
                schedules,
                new ScheduleAdapter.Listener(){

                    @Override
                    public void onEdit(int pos){

                        editSchedule(pos);

                    }

                    @Override
                    public void onDelete(int pos){

                        schedules.remove(pos);

                        ScheduleStorage.save(
                                MainActivity.this,
                                schedules
                        );

                        adapter.notifyDataSetChanged();

                    }

                });

        listSchedule.setLayoutManager(
                new LinearLayoutManager(this));

        listSchedule.setAdapter(adapter);

        btnAdd.setOnClickListener(v->addSchedule());

        switchBot = findViewById(R.id.switchBot);

        prefs = getSharedPreferences("bot",MODE_PRIVATE);

        boolean enabled = prefs.getBoolean("enabled",true);

        switchBot.setChecked(enabled);

        switchBot.setOnCheckedChangeListener((buttonView, isChecked) -> {

            prefs.edit().putBoolean("enabled",isChecked).apply();

            if(MyAccessibilityService.instance != null){

                MyAccessibilityService.instance.botPaused = !isChecked;

            }

        });

    }

    @Override
    protected void onResume(){

        super.onResume();

        boolean enabled = isAccessibilityEnabled();

        txtStatus.setText(
                enabled ? "Accessibility : AKTIF"
                        : "Accessibility : MATI"
        );

    }

    void addSchedule(){

        TimePickerDialog dialog=
                new TimePickerDialog(
                        this,
                        (view,hour,minute)->{

                            String time=
                                    String.format("%02d:%02d",hour,minute);

                            schedules.add(time);

                            ScheduleStorage.save(this,schedules);

                            ScheduleManager.schedule(this,hour,minute);

                            adapter.notifyDataSetChanged();

                        },
                        12,
                        0,
                        true
                );

        dialog.show();

    }

    void editSchedule(int pos){

        String t=schedules.get(pos);

        String[] s=t.split(":");

        int h=Integer.parseInt(s[0]);

        int m=Integer.parseInt(s[1]);

        TimePickerDialog dialog=
                new TimePickerDialog(
                        this,
                        (view,hour,minute)->{

                            schedules.set(
                                    pos,
                                    String.format("%02d:%02d",hour,minute)
                            );

                            ScheduleStorage.save(this,schedules);

                            ScheduleManager.schedule(this,hour,minute);

                            adapter.notifyDataSetChanged();

                        },
                        h,
                        m,
                        true
                );

        dialog.show();

    }

    private boolean isAccessibilityEnabled(){

        String service =
                getPackageName()+"/"+MyAccessibilityService.class.getName();

        int accessibilityEnabled = 0;

        try{

            accessibilityEnabled =
                    Settings.Secure.getInt(
                            getContentResolver(),
                            Settings.Secure.ACCESSIBILITY_ENABLED
                    );

        }catch(Exception e){}

        if(accessibilityEnabled == 1){

            String settingValue =
                    Settings.Secure.getString(
                            getContentResolver(),
                            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                    );

            if(settingValue != null){

                return settingValue.contains(service);

            }

        }

        return false;

    }

}