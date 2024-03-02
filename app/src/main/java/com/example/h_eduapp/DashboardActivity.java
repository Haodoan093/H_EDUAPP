package com.example.h_eduapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        firebaseAuth = FirebaseAuth.getInstance();

        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        BottomNavigationView navigationView = findViewById(R.id.navigation);

        navigationView.setOnNavigationItemSelectedListener(selectedListener);
        //dat mac dinh
        actionBar.setTitle("Home");
        HomeFragment fragment1= new HomeFragment();
        FragmentTransaction ft1= getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content,fragment1,"");
        ft1.commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId() == R.id.nav_home) {
                       actionBar.setTitle("Home");
                       HomeFragment fragment1= new HomeFragment();
                        FragmentTransaction ft1= getSupportFragmentManager().beginTransaction();
                        ft1.replace(R.id.content,fragment1,"");
                        ft1.commit();
                        return true;
                    } else if (item.getItemId() ==  R.id.nav_profile) {
                        actionBar.setTitle("Profile");
                        ProfileFragment fragment2= new ProfileFragment();
                        FragmentTransaction ft2= getSupportFragmentManager().beginTransaction();
                        ft2.replace(R.id.content,fragment2,"");
                        ft2.commit();
                        return true;
                    } else if (item.getItemId() ==  R.id.nav_users) {
                        // Xử lý khi người dùng chọn mục Users
                        actionBar.setTitle("User");
                        UserFragment fragment3= new UserFragment();
                        FragmentTransaction ft3= getSupportFragmentManager().beginTransaction();
                        ft3.replace(R.id.content,fragment3,"");
                        ft3.commit();
                        return true;
                    }
                    return false;
                }
            };


    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {

            // Người dùng đã đăng nhập
        } else {
            // Người dùng chưa đăng nhập, chuyển hướng về màn hình đăng nhập
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
