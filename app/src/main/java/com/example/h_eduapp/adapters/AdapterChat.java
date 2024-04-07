package com.example.h_eduapp.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.h_eduapp.R;
import com.example.h_eduapp.UserFragment;
import com.example.h_eduapp.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
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
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new MyHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MyHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull MyHolder holder, @SuppressLint("RecyclerView") final int position) {
        //get data
        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();
        String type = chatList.get(position).getType();
        // Convert timestamp to dd/MM/yyyy hh:mm a format


        if(type.equals("text")){
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setText(message);
        }else{
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);

            Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(holder.messageIv);
        }
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

        //click to show delete dialog

        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Choose Action");
                // Add the buttons
                builder.setPositiveButton("Recall", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        recallMessage(position);
                    }
                });
                builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteMessage(position);
                    }
                });
                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                });
                // Create and show the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


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

    private void deleteMessage(int position) {

        String myUid= FirebaseAuth.getInstance().getUid();
        String msgTimeStamp= chatList.get(position).getTimestamp();
        DatabaseReference dbRef= FirebaseDatabase.getInstance().getReference("Chats");
        Query query=dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){

                    if(ds.child("sender").getValue().equals(myUid)){
                        //xoa
                        ds.getRef().removeValue();

                    }else{
                        Toast.makeText(context, "You can delete only your messages...", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }
    private void recallMessage(int position) {

        String myUid= FirebaseAuth.getInstance().getUid();
        String msgTimeStamp= chatList.get(position).getTimestamp();
        DatabaseReference dbRef= FirebaseDatabase.getInstance().getReference("Chats");
        Query query=dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){

                    if(ds.child("sender").getValue().equals(myUid)){

                        //thu hooi
                        HashMap<String,Object> hashMap= new HashMap<>();
                        hashMap.put("message","This message was deleted...");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context, "message deleted...", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(context, "You can delete only your messages...", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get currently signed in user
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }

    }
    //view holder class

    class MyHolder extends RecyclerView.ViewHolder {


        ImageView mProfileIv,messageIv;
        TextView timeTv, messageTv, mSeenTv;
        LinearLayout messageLayout;

        public MyHolder(@NonNull View view) {

            super(view);

            messageTv = view.findViewById(R.id.messageTv);
            timeTv = view.findViewById(R.id.timeTv);
            mSeenTv = view.findViewById(R.id.isSeenTv);
            mProfileIv = view.findViewById(R.id.profileIv);
            messageIv = view.findViewById(R.id.messageIv);
            messageLayout = view.findViewById(R.id.messageLayout);

        }


    }
}
