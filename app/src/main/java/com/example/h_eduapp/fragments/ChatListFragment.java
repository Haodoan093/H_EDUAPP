package com.example.h_eduapp.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.h_eduapp.GroupCreateActivity;
import com.example.h_eduapp.MainActivity;
import com.example.h_eduapp.R;
import com.example.h_eduapp.SettinggsActivity;
import com.example.h_eduapp.adapters.AdapterChatlist;
import com.example.h_eduapp.models.ModelChat;
import com.example.h_eduapp.models.ModelChatlist;
import com.example.h_eduapp.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
   RecyclerView recyclerView;


   List<ModelChatlist>  chatlistList;
   List<ModelUsers>usersList;
   DatabaseReference reference;
   FirebaseUser currentUser;
   AdapterChatlist adapterChatlist;
    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        recyclerView= view.findViewById(R.id.recyclerView);


        currentUser=FirebaseAuth.getInstance().getCurrentUser();
        chatlistList= new ArrayList<>();
        reference= FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                chatlistList.clear();
                for(DataSnapshot ds :snapshot.getChildren()){
                    ModelChatlist chatlist= ds.getValue(ModelChatlist.class);
                    chatlistList.add(chatlist);
                }
                loadChats();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }

    private void loadChats() {
        usersList= new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelUsers user= ds.getValue(ModelUsers.class);
                    for (ModelChatlist chatlist: chatlistList){
                        if(user.getUid()!=null&& user.getUid().equals(chatlist.getId())){
                            usersList.add(user);

                            break;
                        }
                    }
                }

                adapterChatlist= new AdapterChatlist(getContext(),usersList);
                //set adapter
                
                recyclerView.setAdapter(adapterChatlist);
                //set lastMessage
                for(int i= 0;i<usersList.size();i++){
                    lastMessage(usersList.get(i).getUid());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String Userid) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage= "default";
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelChat chat= ds.getValue(ModelChat.class);
                    if(chat==null){
                        continue;
                    }
                    String sender= chat.getSender();
                    String receiver= chat.getReceiver();

                    if(sender==null||receiver==null){
                        continue;
                    }


                    if(chat.getReceiver().equals(currentUser.getUid() )&&chat.getSender().equals(Userid)
                    ||chat.getReceiver().equals(Userid)&&
                    chat.getSender().equals(currentUser.getUid())) {
                        //instead of display url in message show "sent photo"
                        if ( chat.getType().equals("image")) {
                            theLastMessage = "Sent a photo";
                        }
                        else {
                            theLastMessage = chat.getMessage();
                        }

                    }

                }
                adapterChatlist.setLastMessageMap(Userid,theLastMessage);
                adapterChatlist.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);// to show menu option in fragment

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        // Hide the menu item if it exists
   menu.findItem(R.id.action_addpost).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }else if (id==R.id.action_settings) {
            //go to settinggs activity
            startActivity((new Intent(getActivity(), SettinggsActivity.class)));
        }else if (id==R.id.action_create_group) {
            //go to settinggs activity
            startActivity((new Intent(getActivity(), GroupCreateActivity.class)));
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {

            // Người dùng đã đăng nhập
        } else {
            // Người dùng chưa đăng nhập, chuyển hướng về màn hình đăng nhập
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
}