package com.example.h_eduapp.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.h_eduapp.AddPostActivity;
import com.example.h_eduapp.MainActivity;
import com.example.h_eduapp.R;
import com.example.h_eduapp.SettinggsActivity;
import com.example.h_eduapp.adapters.AdapterPosts;
import com.example.h_eduapp.models.ModelPoost;
import com.google.android.material.textfield.TextInputEditText;
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
import java.util.List;

/**
 * A simple {@link Fragment} subclass.

 */
public class HomeFragment extends Fragment {
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    TextInputEditText addpost_btn;
    ImageView avatarIv;
    RecyclerView recyclerView;
    List<ModelPoost> poostList;
    AdapterPosts adapterPosts;
    public HomeFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        View view= inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView= view.findViewById(R.id.postsRecyclerview);
        addpost_btn = view.findViewById(R.id.addpost_btn);
        avatarIv = view.findViewById(R.id.avatarIv);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getActivity());

        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until requiredd data get
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String image = "" + ds.child("image").getValue();
                    try {
                        // if image is received the get
                        Picasso.get().load(image).into(avatarIv);

                    } catch (Exception e) {
                        // if there is any exception while getting image the get default
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        addpost_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddPostActivity.class));
            }
        });
        recyclerView.setLayoutManager(linearLayoutManager);
        
        poostList = new ArrayList<>();
        
        loadPosrs();
        

        return  view;
    }

    private void loadPosrs() {

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                poostList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelPoost modelPoost= ds.getValue(ModelPoost.class);
                    poostList.add(modelPoost);

                    adapterPosts= new AdapterPosts(getActivity(),poostList);
                    
                    recyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private void searchPosts(String searchQuery){
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                poostList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelPoost modelPoost= ds.getValue(ModelPoost.class);

                    if(modelPoost.getpTitle().contains(searchQuery.toLowerCase())
                    ||modelPoost.getpDescr().contains(searchQuery.toLowerCase())){
                        poostList.add(modelPoost);
                    }


                    adapterPosts= new AdapterPosts(getActivity(),poostList);

                    recyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);// to show menu option in fragment

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_main, menu);


        MenuItem item= menu.findItem(R.id.action_search);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        SearchView searchView= (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!TextUtils.isEmpty(s)){
                    searchPosts(s);
                }else{
                    loadPosrs();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!TextUtils.isEmpty(s)){
                    searchPosts(s);
                }else{
                    loadPosrs();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu,menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
       else  if (id == R.id.action_addpost) {
         startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        else if (id==R.id.action_settings) {
            //go to settinggs activity
            startActivity((new Intent(getActivity(), SettinggsActivity.class)));
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