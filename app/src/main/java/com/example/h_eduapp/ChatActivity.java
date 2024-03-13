package com.example.h_eduapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.example.h_eduapp.notifications.APIService;
import com.example.h_eduapp.notifications.Client;
import com.example.h_eduapp.notifications.Data;
import com.example.h_eduapp.notifications.Respone;
import com.example.h_eduapp.notifications.Sender;
import com.example.h_eduapp.notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    ImageView onlineIndicator;

    APIService apiService;
    boolean notify = false;

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
        onlineIndicator = findViewById(R.id.onlineIndicator);


        //layout fo recyclerView

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //creat api sevice

        apiService = Client.getRetrofit("https://fcm.googleaips.com/").create(APIService.class);


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
                    String typingStatus = "" + ds.child("typingTo").getValue();


                    //check typing status
                    if (typingStatus.equals(myUid)) {
                        userStatusTv.setText("typing...");
                    } else {

                        //get value of onlinestatus

                        Object onlineStatusObj = ds.child("onlineStatus").getValue();
                        if (onlineStatusObj != null) {
                            String onlineStatus = onlineStatusObj.toString(); // Convert the object to String
                            if (!onlineStatus.isEmpty()) {
                                if (onlineStatus.equals("online")) {
                                    // Hiển thị chấm màu xanh khi online

                                    onlineIndicator.setVisibility(View.VISIBLE);
                                    onlineIndicator.setImageResource(R.drawable.online_indicator_green);
                                    userStatusTv.setText(onlineStatus);
                                } else {
                                    // Hiển thị chấm màu xám khi offline
                                    onlineIndicator.setVisibility(View.VISIBLE);
                                    onlineIndicator.setImageResource(R.drawable.online_indicator_gray);
                                    // Xử lý thời gian offline và cập nhật userStatusTv
                                    try {
                                        long timestamp = Long.parseLong(onlineStatus);
                                        long currentTime = System.currentTimeMillis();
                                        long timeDifference = currentTime - timestamp;
                                        String timeAgo = getTimeAgo(timeDifference);
                                        userStatusTv.setText(timeAgo);
                                    } catch (NumberFormatException e) {
                                        // Xử lý khi không thể chuyển đổi timestamp thành số
                                        userStatusTv.setText("Offline");
                                    }
                                }
                            } else {
                                // Xử lý trường hợp onlineStatus là trống
                                userStatusTv.setText("Offline");
                                onlineIndicator.setVisibility(View.VISIBLE);
                                onlineIndicator.setImageResource(R.drawable.online_indicator_gray);
                            }
                        } else {
                            // Xử lý trường hợp onlineStatus là null
                            userStatusTv.setText("Offline");
                            onlineIndicator.setVisibility(View.VISIBLE);
                            onlineIndicator.setImageResource(R.drawable.online_indicator_gray);
                        }

                    }


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

                notify = true;
                //get text from edit
                String message = messageEt.getText().toString().trim();

                //check if text is empty or not
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(ChatActivity.this, "Cannot send the empty message...", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessaage(message);
                }
                messageEt.setText("");
            }
        });

        //check edit text change listener
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() == 0) {
                    checkTypingStatus("noOne");
                } else {
                    checkTypingStatus(hisUid);//uid of receiver
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        checkUserStatus();
        readMessage();
        seenMessage();


    }

    private String getTimeAgo(long timeDifference) {
        // Convert milliseconds to minutes
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference);

        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else {
            // Convert minutes to hours
            long hours = TimeUnit.MINUTES.toHours(minutes);
            if (hours < 24) {
                return hours + " hours ago";
            } else {
                // Convert hours to days
                long days = TimeUnit.HOURS.toDays(hours);
                return days + " days ago";
            }
        }
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



        String msg = message;
        DatabaseReference data = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUsers user = snapshot.getValue(ModelUsers.class);
                if(notify){
                    sendNotification(hisUid,user.getName(),message);
                }
                notify=false;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendNotification(String hisUid, String name, String message) {

        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token= ds.getValue(Token.class);
                    Data data= new Data(myUid,name+" : "+message, "New Message",hisUid
                    ,R.drawable.ic_default_img_users
                    );
                    Sender sender= new Sender(data,token.getToken());

                    apiService.sendNotificatioon(sender)
                            .enqueue(new Callback<Respone>() {
                                @Override
                                public void onResponse(Call<Respone> call, Response<Respone> response) {
                                    Toast.makeText(ChatActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Respone> call, Throwable t) {

                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timeStamp = String.valueOf(System.currentTimeMillis());
        checkTypingStatus("noOne");
        checkOnlineStatus(timeStamp);
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
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

    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        dbRef.updateChildren(hashMap);


    }

    private void checkTypingStatus(String typing) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        dbRef.updateChildren(hashMap);


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