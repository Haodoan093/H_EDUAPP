package com.example.h_eduapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.h_eduapp.adapters.AdapterChat;
import com.example.h_eduapp.adapters.AdapterUsers;
import com.example.h_eduapp.models.ModelChat;
import com.example.h_eduapp.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {


    //views from xml
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv, userStatusTv;

    EditText messageEt;
    ImageButton sendBtn;
    FirebaseAuth firebaseAuth;

    String hisUid;
    String myUid;
    String hisImage;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;

    //for checking if use has seen message or not

    ValueEventListener seenListener;

    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;

    AdapterChat adapterChat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //inti views

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        recyclerView = findViewById(R.id.chat_recyclerView);

        nameTv = findViewById(R.id.nameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        profileIv = findViewById(R.id.profileIv);

        //layout fo recyclerView

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        //On cllick user from uers list we have passed that user's UID using intent
        //So get that uid here to get the profile picture, name and start chat with that user

        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference("Users");

        //search user to get that user's info
        Query userQuery = usersDbRef.orderByChild("uid").equalTo(hisUid);
        //get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required ifo is received
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String name = "" + ds.child("name").getValue();
                    hisImage = "" + ds.child("image").getValue();

                    //set data
                    nameTv.setText(name);

                    try {
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_default_img_users)
                                .into(profileIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_img_users)
                                .into(profileIv);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //click send

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get text from edit
                String message = messageEt.getText().toString().trim();

                //check if text is empty or not
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(ChatActivity.this, "Cannot send the empty message...", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessaage(message);
                }
            }
        });
        checkUserStatus();
        readMessage();
        seenMessage();



    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");

        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                try {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ModelChat chat = ds.getValue(ModelChat.class);
                        if (chat != null) {
                            String receiver = chat.getReceiver();
                            String sender = chat.getSender();
                            if (receiver == null || sender == null) {
                                Log.e("seenMessage", "Receiver or sender is null");
                                return;
                            }
                            if (myUid != null && hisUid != null && myUid.equals(receiver) && hisUid.equals(sender)) {
                                HashMap<String, Object> hasSeenHasMap = new HashMap<>();
                                hasSeenHasMap.put("isSeen", true);
                                ds.getRef().updateChildren(hasSeenHasMap);
                            }
                        } else {
                            Log.e("seenMessage", "Chat object is null");
                        }
                    }
                } catch (Exception e) {
                    Log.e("seenMessage", "Error: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("seenMessage", "Database error: " + error.getMessage());
            }
        });
    }

    private void readMessage() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    chatList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ModelChat chat = ds.getValue(ModelChat.class);

                        //get
                        if (chat != null) {
                            String receiver = chat.getReceiver();
                            String sender = chat.getSender();
                            if (receiver == null || sender == null) {
                                Log.e("readMessage", "Receiver or sender is null");
                                return;
                            }
                            if (myUid != null && hisUid != null &&
                                    (receiver.equals(myUid) && sender.equals(hisUid)
                                            || receiver.equals(hisUid) && sender.equals(myUid))) {
                                chatList.add(chat);
                            }
                        } else {
                            Log.e("readMessage", "Chat object is null");
                        }
                    }

                    //adapter
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    recyclerView.setAdapter(adapterChat);
                    adapterChat.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e("readMessage", "Error: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("readMessage", "Database error: " + error.getMessage());
            }
        });
    }


    private void sendMessaage(String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        String timeStamp = String.valueOf(System.currentTimeMillis());
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);

        hashMap.put("timestamp", timeStamp);
        hashMap.put("isSeen", false);
        reference.child("Chats").push().setValue(hashMap);

        messageEt.setText("");
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        userRefForSeen.removeEventListener(seenListener);
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {

            // Người dùng đã đăng nhập
            myUid = user.getUid();

        } else {
            // Người dùng chưa đăng nhập, chuyển hướng về màn hình đăng nhập
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //hide searchView, as we dont need it here
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}