package com.example.h_eduapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.h_eduapp.R;
import com.example.h_eduapp.UserFragment;
import com.example.h_eduapp.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder>{

 private static final int MSG_TYPE_LEFT=0;
 private static final int MSG_TYPE_RIGHT=1;
 Context context ;
 List<ModelChat> chatList;
 String imageUrl;

 FirebaseUser fUser;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @androidx.annotation.NonNull
    @Override
    public MyHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
        //inflate ;ayouts : row_chatleft ot right
        if (viewType==MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent,false);
            return new MyHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left,parent,false);
            return new MyHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull MyHolder holder, int position) {
        //get data
        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();

        // Convert timestamp to dd/MM/yyyy hh:mm a format

        String dateTime = "";
        if (timeStamp != null) {
            //convert time stamp to dd/MM/yyyy hh:mm am/pm
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(Long.parseLong(timeStamp));

            // Use SimpleDateFormat to format the date
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH);
            dateTime = sdf.format(cal.getTime());
        }


        //set data
        holder.messageTv.setText(message);
        holder.timeTv.setText(dateTime);

        try {
            Picasso.get().load(imageUrl).placeholder(R.drawable.ic_default_img_users)
                    .into(holder.mProfileIv);
        } catch (Exception e) {
            // Handle exception if Picasso fails to load image
        }

        //set seen/delivered status of message
        if (position == chatList.size() - 1) {
            if (chatList.get(position).getSeen() != null && chatList.get(position).getSeen()) {
                holder.mSeenTv.setText("Seen");
            } else {
                holder.mSeenTv.setText("Delivered");
            }
        } else {
            holder.mSeenTv.setVisibility(View.GONE);
        }
    }



    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get currently signed in user
        fUser= FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }

    }
    //view holder class

    class MyHolder extends RecyclerView.ViewHolder {


        ImageView mProfileIv;
        TextView timeTv,messageTv, mSeenTv;

        public MyHolder(@NonNull View view) {

            super(view);

            messageTv = view.findViewById(R.id.messageTv);
            timeTv = view.findViewById(R.id.timeTv);
            mSeenTv = view.findViewById(R.id.isSeenTv);
            mProfileIv = view.findViewById(R.id.profileIv);

        }


    }
}
