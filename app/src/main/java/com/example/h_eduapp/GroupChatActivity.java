package com.example.h_eduapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.h_eduapp.adapters.AdapterGroupChat;
import com.example.h_eduapp.models.ModelGroupChat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {


    private FirebaseAuth firebaseAuth;

    private String groupId;

    private RecyclerView chatRv;
    private Toolbar toolbar;
    private ImageView groupIconIv;
    private ImageButton attachBtn, sendBtn;
    private TextView groupTitleTv;
    private EditText messageEt;

    private ArrayList<ModelGroupChat> groupChatList;
    private AdapterGroupChat adapterGroupChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_group_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        ///init wviews
        toolbar = findViewById(R.id.toolbar);
        groupIconIv = findViewById(R.id.groupIconIv);
        attachBtn = findViewById(R.id.attachBtn);
        sendBtn = findViewById(R.id.sendBtn);
        groupTitleTv = findViewById(R.id.groupTitleTv);

        messageEt = findViewById(R.id.messageEt);

        chatRv = findViewById(R.id.chatRv);
        Intent intent = getIntent();

        groupId = intent.getStringExtra("groupId");

        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupInfo();
        loadGroupMessages();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageEt.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(GroupChatActivity.this, "Can't sned empty messagge ...", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(message);
                }
            }
        });
    }

    private void loadGroupMessages() {
        groupChatList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        groupChatList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            ModelGroupChat model = ds.getValue(ModelGroupChat.class);
                            groupChatList.add(model);
                        }

                        adapterGroupChat = new AdapterGroupChat(GroupChatActivity.this, groupChatList);

                        chatRv.setAdapter(adapterGroupChat);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendMessage(String message) {
        String timestamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", "" + firebaseAuth.getUid());
        hashMap.put("message", "" + message);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("type", "" + "text");


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        messageEt.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener() {

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadGroupInfo() {


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String groupTitle = "" + ds.child("groupTitle").getValue();
                            String groupDescription = "" + ds.child("groupDescription").getValue();
                            String groupIcon = "" + ds.child("groupIcon").getValue();
                            String timestamp = "" + ds.child("timestamp").getValue();

                            String createdBy = "" + ds.child("createdBy").getValue();

                            groupTitleTv.setText(groupTitle);

                            try {

                                Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group).into(groupIconIv);
                            } catch (Exception e) {
                                groupIconIv.setImageResource(R.drawable.ic_group);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }
}