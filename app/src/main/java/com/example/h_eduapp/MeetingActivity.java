package com.example.h_eduapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Random;
import java.util.UUID;

public class MeetingActivity extends AppCompatActivity {

    TextInputEditText meetingIdInput, nameInput;
    MaterialButton joinBtn, createBtn;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_meeting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sharedPreferences = getSharedPreferences("name_pref", MODE_PRIVATE);

        meetingIdInput = findViewById(R.id.meeting_id_input);
        nameInput = findViewById(R.id.name_input);
        joinBtn = findViewById(R.id.join_btn);
        createBtn = findViewById(R.id.creat_btn);


        nameInput.setText(sharedPreferences.getString("name",""));

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String meetingID = meetingIdInput.getText().toString();
                if (meetingID.length() != 10) {
                    meetingIdInput.setError("Invalid Meeting ID");
                    meetingIdInput.requestFocus();
                    return;
                }
                String name = nameInput.getText().toString();
                if (name.isEmpty()) {
                    nameInput.setError("Name is required to join the meeting");
                    nameInput.requestFocus();
                    return;
                }
                startMeeeting(meetingID, name);
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameInput.getText().toString();
                if (name.isEmpty()) {
                    nameInput.setError("Name is required to join the meeting");
                    nameInput.requestFocus();
                    return;
                }
                startMeeeting(getRandomMeetingID(), name);
            }
        });
    }

    void startMeeeting(String meetingID, String name) {

        sharedPreferences.edit().putString("name",name).apply();
        String userID = UUID.randomUUID().toString();

        Intent intent = new Intent(MeetingActivity.this, ConferenceActivity.class);
        intent.putExtra("meetingID", meetingID);
        intent.putExtra("name", name);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }

    String getRandomMeetingID() {
        StringBuilder id = new StringBuilder();
        while ((id.length() != 10)) {
            int random = new Random().nextInt(10);
            id.append(random);
        }
        return id.toString();
    }


}