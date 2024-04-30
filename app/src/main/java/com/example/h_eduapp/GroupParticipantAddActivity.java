package com.example.h_eduapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.h_eduapp.adapters.AdapterParticipantAdd;
import com.example.h_eduapp.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupParticipantAddActivity extends AppCompatActivity {


    private RecyclerView usersRv;
    private ActionBar actionBar;
    private FirebaseAuth firebaseAuth;
    private String groupId,myGroupRole="";
    private ArrayList<ModelUsers> uersList;
    private AdapterParticipantAdd adapterParticipantAdd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_group_participant_add);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        firebaseAuth= FirebaseAuth.getInstance();


        usersRv=findViewById(R.id.userRv);

        groupId=getIntent().getStringExtra("groupId");
        loadGroupInfo();


    }

    private void getAllUsers() {

        uersList= new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                uersList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelUsers model= ds.getValue(ModelUsers.class);

                    if(!firebaseAuth.getUid().equals(model.getUid())){
                        uersList.add(model);
                    }
                }
                adapterParticipantAdd= new AdapterParticipantAdd(GroupParticipantAddActivity.this,uersList,""+groupId,""+myGroupRole);

                usersRv.setAdapter(adapterParticipantAdd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadGroupInfo() {
        DatabaseReference ref1= FirebaseDatabase.getInstance().getReference("Groups");

        DatabaseReference reference =  FirebaseDatabase.getInstance().getReference("Groups");

        reference.orderByChild("groupId")
                .equalTo(groupId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds :snapshot.getChildren()){
                            String groupId= ""+ds.child("groupId").getValue();
                            String groupTitle= ""+ds.child("groupTitle").getValue();
                            String groupDescription= ""+ds.child("groupDescription").getValue();
                            String groupIcon= ""+ds.child("groupIcon").getValue();
                            String createdBy= ""+ds.child("createdBy").getValue();
                            String timestamp= ""+ds.child("timestamp").getValue();

                            actionBar = getSupportActionBar();

                            actionBar.setTitle("Add participants");


                            ref1.child(groupId).child("Participants").child(firebaseAuth.getUid())
                                    .addValueEventListener(new ValueEventListener() {

                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()) {


                                                myGroupRole = "" + snapshot.child("role").getValue();
                                                actionBar.setTitle(groupTitle + "(" + myGroupRole + ")");
                                                getAllUsers();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }) ;

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}