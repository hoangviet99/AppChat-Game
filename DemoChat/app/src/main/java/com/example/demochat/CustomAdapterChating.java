package com.example.demochat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapterChating extends ArrayAdapter<Chating> {

    private Context context;
    private int resource;
    private ArrayList<Chating> arrChatting;

    public CustomAdapterChating(@NonNull Context context, int resource, @NonNull ArrayList<Chating> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.arrChatting = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tv_Username = convertView.findViewById(R.id.tv_User);
            viewHolder.tv_Chatting = convertView.findViewById(R.id.tv_Chat);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Chating chatting = arrChatting.get(position);
        viewHolder.tv_Username.setText(chatting.getmUsername());
        viewHolder.tv_Chatting.setText(chatting.getmChat());

        return convertView;
    }

    public class ViewHolder{
        TextView tv_Username, tv_Chatting;
    }
}
