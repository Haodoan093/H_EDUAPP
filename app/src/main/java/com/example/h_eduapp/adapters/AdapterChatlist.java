package com.example.h_eduapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.h_eduapp.ChatActivity;
import com.example.h_eduapp.R;
import com.example.h_eduapp.models.ModelChatlist;
import com.example.h_eduapp.models.ModelUsers;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChatlist extends  RecyclerView.Adapter<AdapterChatlist.MyHolder>{

    Context context;
    List<ModelUsers> usersList;
    private HashMap<String , String> lastMessageMap;
//contructor
    public AdapterChatlist(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
        lastMessageMap = new HashMap<>();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<ModelUsers> getUsersList() {
        return usersList;
    }

    public void setUsersList(List<ModelUsers> usersList) {
        this.usersList = usersList;
    }

    public HashMap<String, String> getLastMessageMap() {
        return lastMessageMap;
    }

    public void setLastMessageMap(String userId, String lastMessage) {
        lastMessageMap.put(userId,lastMessage);
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_chatlist,parent,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String hisUid= usersList.get(position).getUid();
        String userImage= usersList.get(position).getImage();
        String userName= usersList.get(position).getName();
        String lastMessage= lastMessageMap.get(hisUid);


        //set data
        holder.nameTv.setText(userName);
        if(lastMessage==null||lastMessage.equals("default")){
            holder.lastMessageTv.setVisibility(View.GONE);
        }else{
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }
        try
        {
            Picasso.get().load(userImage).placeholder(R.drawable.avata).into(holder.profileIv);
        }catch (Exception e){

            Picasso.get().load(R.drawable.avata).into(holder.profileIv);

        }
        //set online status

        if(usersList.get(position).getOnlineStatus().equals("online")){
            holder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        }else{
            holder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start chat activity with that user
                Intent intent= new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid",hisUid);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView profileIv,onlineStatusIv;
        TextView nameTv,lastMessageTv;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileIv= itemView.findViewById(R.id.profileIv);
            onlineStatusIv= itemView.findViewById(R.id.onlineStatusIv);
            nameTv= itemView.findViewById(R.id.nameTv);
            lastMessageTv= itemView.findViewById(R.id.lastMessageTv);
        }
    }

}
