package com.example.h_eduapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.h_eduapp.R;
import com.example.h_eduapp.models.ModelComment;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComments extends  RecyclerView.Adapter<AdapterComments.MyHolder>{

    Context context;
    List<ModelComment> commentList;

    public AdapterComments(Context context, List<ModelComment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind the roow_comments.xml layout
        View view= LayoutInflater.from(context).inflate(R.layout.row_comments,parent,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

//get the data
        String uid = commentList.get(position).getUid();
        String name = commentList.get(position).getuName();
        String email = commentList.get(position).getuEmail();
        String image = commentList.get(position).getuDp();
        String cid = commentList.get(position).getcId();
        String comment = commentList.get(position).getComment();
        String timeStamp = commentList.get(position).getTimeStamp();

        String pTime = "";
        if (timeStamp != null) {
            //convert time stamp to dd/MM/yyyy hh:mm am/pm
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(Long.parseLong(timeStamp));

            // Use SimpleDateFormat to format the date
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH);
            pTime = sdf.format(cal.getTime());
        }


        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.timeTv.setText(pTime);

        try {
            Picasso.get().load(image)
                    .placeholder(R.drawable.ic_default_img_users)
                    .into(holder.avatarIv);
        } catch (Exception e) {
            // Handle exception if Picasso fails to load image
        }

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{

        ImageView avatarIv;
        TextView nameTv,commentTv,timeTv;


        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv=itemView.findViewById(R.id.avatarIv);
            nameTv=itemView.findViewById(R.id.nameTv);
            commentTv=itemView.findViewById(R.id.commentTv);

            timeTv=itemView.findViewById(R.id.timeTv);
        }
    }
}
