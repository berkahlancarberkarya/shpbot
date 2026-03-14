package com.blb.shpbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduleAdapter
        extends RecyclerView.Adapter<ScheduleAdapter.Holder>{

    public interface Listener{

        void onEdit(int pos);

        void onDelete(int pos);

    }

    List<String> list;

    Listener listener;

    public ScheduleAdapter(List<String> l,Listener lis){

        list=l;

        listener=lis;

    }

    static class Holder extends RecyclerView.ViewHolder{

        TextView txt;

        Button edit,del;

        public Holder(View v){

            super(v);

            txt=v.findViewById(R.id.txtTime);

            edit=v.findViewById(R.id.btnEdit);

            del=v.findViewById(R.id.btnDelete);

        }

    }

    @Override
    public Holder onCreateViewHolder(ViewGroup p,int v){

        View view=
                LayoutInflater.from(p.getContext())
                        .inflate(R.layout.item_schedule,p,false);

        return new Holder(view);

    }

    @Override
    public void onBindViewHolder(Holder h,int pos){

        h.txt.setText(list.get(pos));

        h.edit.setOnClickListener(v->listener.onEdit(pos));

        h.del.setOnClickListener(v->listener.onDelete(pos));

    }

    @Override
    public int getItemCount(){

        return list.size();

    }

}