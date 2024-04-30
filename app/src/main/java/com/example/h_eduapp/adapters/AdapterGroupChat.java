package com.example.h_eduapp.adapters;

import android.content.Context;
import android.nfc.tech.NfcA;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.h_eduapp.R;
import com.example.h_eduapp.models.ModelGroupChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.HoldergroupChat>{

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
    private ArrayList<ModelGroupChat> modelGroupChatList;

    private FirebaseAuth firebaseAuth;

    public AdapterGroupChat(Context context, ArrayList<ModelGroupChat> modelGroupChatList) {
        this.context = context;
        this.modelGroupChatList = modelGroupChatList;

        firebaseAuth= FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HoldergroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right, parent, false);
            return new HoldergroupChat(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left, parent, false);
            return new HoldergroupChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HoldergroupChat holder, int position) {
        ModelGroupChat model=modelGroupChatList.get(position);
        String message= model.getMessage();
        String senderUid=model.getSender();
        String timeStamp= model.getTimestamp();
        String messageType=model.getType();
        String dateTime = "";
        if (timeStamp != null) {
            //convert time stamp to dd/MM/yyyy hh:mm am/pm
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(Long.parseLong(timeStamp));

            // Use SimpleDateFormat to format the date
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH);
            dateTime = sdf.format(cal.getTime());
        }
        if(messageType.equals("text")){
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setText(message);
        }else{
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(holder.messageIv);
            }catch (Exception e){
                holder.messageIv.setImageResource(R.drawable.ic_image_black);
            }
        }




        //set data
        holder.messageTv.setText(message);
        holder.timeTv.setText(dateTime);

        setUserName(model,holder);

    }

    private void setUserName(ModelGroupChat model, HoldergroupChat holder) {

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {


                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                         for(DataSnapshot ds :snapshot.getChildren()) {
                                String name= ""+ds.child("name").getValue();

                                holder.nameTv.setText(name);

                         }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemViewType(int position) {
       if(modelGroupChatList.get(position).getSender().equals(firebaseAuth.getUid())){
           return MSG_TYPE_RIGHT;
       }else {
           return MSG_TYPE_LEFT;
       }
    }

    @Override
    public int getItemCount() {
        return modelGroupChatList.size();
    }


    class HoldergroupChat extends RecyclerView.ViewHolder{

        private TextView nameTv,messageTv,timeTv;
        private ImageView messageIv;
        public HoldergroupChat(@NonNull View itemView) {
            super(itemView);

            nameTv= itemView.findViewById(R.id.nameTv);
            messageTv= itemView.findViewById(R.id.messageTv);
            timeTv= itemView.findViewById(R.id.timeTv);
            messageIv= itemView.findViewById(R.id.messageIv);
        }
    }

}
