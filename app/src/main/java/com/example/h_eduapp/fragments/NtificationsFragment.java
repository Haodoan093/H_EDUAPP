package com.example.h_eduapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.h_eduapp.R;
import com.example.h_eduapp.adapters.AdapterNotification;
import com.example.h_eduapp.models.ModelNotification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * <p>
 * create an instance of this fragment.
 */
public class NtificationsFragment extends Fragment {

    RecyclerView notificationsRv;
    FirebaseAuth firebaseAuth;

    private ArrayList<ModelNotification> notificationsList;
private AdapterNotification adapterNotification;
    public NtificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view   =inflater.inflate(R.layout.fragment_ntifications,container,false);
        notificationsRv=view.findViewById(R.id.notififcationsRv);

        firebaseAuth=FirebaseAuth.getInstance();

        getAllNotifications();
        
        return view;
    }

    private void getAllNotifications() {


        notificationsList= new ArrayList<>();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Notifications")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notificationsList.clear();
                        for(DataSnapshot ds :snapshot.getChildren()){
                            ModelNotification model= ds.getValue(ModelNotification.class);

                            notificationsList.add(model);
                        }
                        adapterNotification= new AdapterNotification(getActivity(),notificationsList);
                        notificationsRv.setAdapter(adapterNotification);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}