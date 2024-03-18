package com.example.h_eduapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.h_eduapp.ChatActivity;
import com.example.h_eduapp.R;
import com.example.h_eduapp.ThereProfileActivity;
import com.example.h_eduapp.models.ModelUsers;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    Context context;
    List<ModelUsers> usersList;

    public AdapterUsers(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @androidx.annotation.NonNull
    @Override
    public MyHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
        //inflate layout(row_users)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull MyHolder holder, int position) {
        final String hisUID = usersList.get(position).getUid();
        String userImage = usersList.get(position).getImage();
        String userName = usersList.get(position).getName();
       final   String userEmail = usersList.get(position).getEmail();

        holder.mNameTv.setText(userName);
        holder.mEmailTv.setText(userEmail);
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img_users)
                    .into(holder.mAvatarIv);
        } catch (Exception e) {

        }
        //handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                ///show dialog

                AlertDialog.Builder  builder= new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile","Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                          if (i==0){
                              ///profile clicked
                              Intent intent= new Intent(context, ThereProfileActivity.class);

                              intent.putExtra("uid",hisUID);

                              context.startActivity(intent);
                          }
                        if (i==1){
                            ///profile clicked
                            //Click user from user list to start chatting/messaging
                            //Start activity by putting UID of receiver
                            //we will use that UID to identify the user we are gonna chat

                            Intent intent   = new Intent(context, ChatActivity.class);
                            intent.putExtra("hisUid",hisUID);
                            context.startActivity(intent);
                        }
                    }
                });
                builder.create().show();

            }
        });


    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {


        ImageView mAvatarIv;
        TextView mNameTv, mEmailTv;

        public MyHolder(@NonNull View view) {

            super(view);

            mAvatarIv = view.findViewById(R.id.avatarIv);
            mNameTv = view.findViewById(R.id.nameTv);
            mEmailTv = view.findViewById(R.id.emailTv);

        }


    }


}
