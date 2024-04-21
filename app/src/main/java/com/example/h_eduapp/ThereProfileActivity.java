package com.example.h_eduapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.h_eduapp.adapters.AdapterPosts;
import com.example.h_eduapp.models.ModelPoost;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class ThereProfileActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    RecyclerView postRecyclerview;
    List<ModelPoost> postlist;
    AdapterPosts adapterPosts;
    String uid;
    ImageView avatarIv, coverIv;

    TextView nameTv, emailTv, phoneTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);
        postRecyclerview = findViewById(R.id.recyclerview_posts);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        //get uid of click user to retrieve his posts

        Intent intent = getIntent();

        uid = intent.getStringExtra("uid");
        avatarIv = findViewById(R.id.avatarIv);
        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        coverIv = findViewById(R.id.coverIv);


        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until requiredd data get
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try {
                        // if image is received the get
                        Picasso.get().load(image).into(avatarIv);
                    } catch (Exception e) {
                        // if there is any exception while getting image the get default
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);
                    }
                    try {
                        // if image is received the get
                        Picasso.get().load(cover).into(coverIv);
                    } catch (Exception e) {
                        // if there is any exception while getting image the get default
                        //   Picasso.get().load(R.drawable.ic_default_img_white).into(coverIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postlist = new ArrayList<>();


        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();
        loadHisPosts();


    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void searchHisPosts(final String searchQuery) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ThereProfileActivity.this);

        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        postRecyclerview.setLayoutManager(linearLayoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        Query query = ref.orderByChild("uid").equalTo(uid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postlist.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPoost mypost = ds.getValue(ModelPoost.class);


                    if (mypost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            mypost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        postlist.add(mypost);
                    }


                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postlist);


                    postRecyclerview.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadHisPosts() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ThereProfileActivity.this);

        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        postRecyclerview.setLayoutManager(linearLayoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        Query query = ref.orderByChild("uid").equalTo(uid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postlist.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPoost modelPoost = ds.getValue(ModelPoost.class);

                    postlist.add(modelPoost);


                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postlist);

                    postRecyclerview.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_addpost).setVisible(false);

        menu.findItem(R.id.action_create_group).setVisible(false);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //goi khi search
                if (!TextUtils.isEmpty(s)) {
                    searchHisPosts(s);
                } else {
                    loadHisPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // goi khi khong tim kiem
                if (!TextUtils.isEmpty(s)) {
                    searchHisPosts(s);
                } else {
                    loadHisPosts();
                }
                return false;
            }
        });
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

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {

            // Người dùng đã đăng nhập
        } else {
            // Người dùng chưa đăng nhập, chuyển hướng về màn hình đăng nhập
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}