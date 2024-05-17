package com.example.h_eduapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceConfig;
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceFragment;

public class ConferenceActivity extends AppCompatActivity {


    TextView meeetingIDTv;
    ImageView shareBtn;

    String meetingID,uID,name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conference);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        meeetingIDTv=findViewById(R.id.meeting_id_tv);
        shareBtn=findViewById(R.id.share_btn);

        meetingID=getIntent().getStringExtra("meetingID");
        name=getIntent().getStringExtra("name");
        uID=getIntent().getStringExtra("userID");

        meeetingIDTv.setText("Meeting ID : "+meetingID);

        addFragment();

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT,"Join meeting in HEDDUAPP \n Meeting ID :"+meetingID);
                startActivity(Intent.createChooser(intent,"Share via"));
            }
        });
    }
    public void addFragment() {
        long appID = AppConstants.appId;
        String appSign = AppConstants.appSign;

        String conferenceID = meetingID;
        String userID = uID;
        String userName = name;

        ZegoUIKitPrebuiltVideoConferenceConfig config = new ZegoUIKitPrebuiltVideoConferenceConfig();
        config.turnOnCameraWhenJoining=false;
        config.turnOnMicrophoneWhenJoining=false;
        ZegoUIKitPrebuiltVideoConferenceFragment fragment = ZegoUIKitPrebuiltVideoConferenceFragment.newInstance(appID, appSign, userID, userName,conferenceID,config);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow();
    }
}