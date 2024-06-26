package com.example.h_eduapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettinggsActivity extends AppCompatActivity {

    SwitchCompat postSwitch;


    //use shared preferences to save the state of Switch

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    //constant for topic
    private static final String TOPIC_POST_NOTIFICATION = "POST";

    //asssinggg any value but use same for this kind of notification
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settinggs);

        postSwitch = findViewById(R.id.postSwitch);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle("Settings");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        //init
        sp = getSharedPreferences("Notification_SP", MODE_PRIVATE);
        boolean isPostEnabled = sp.getBoolean("" + TOPIC_POST_NOTIFICATION, false);
        //if enable check switch ,otherwise uncheck switch- by default unckecked /false

        postSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                //edit switch state
                editor = sp.edit();
                editor.putBoolean("" + TOPIC_POST_NOTIFICATION, isChecked);
                editor.apply();
                if (isChecked) {
                    subscribePostNotification();
                } else {
                    unsubscribePostNotification();
                }
            }
        });
    }

    private void unsubscribePostNotification() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(("" + TOPIC_POST_NOTIFICATION))
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You will not receive notification";
                        if (!task.isSuccessful()) {
                            msg = "Unsubscription failed";

                        }
                        Toast.makeText(SettinggsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void subscribePostNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic(("" + TOPIC_POST_NOTIFICATION))
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You will receive notification";
                        if (!task.isSuccessful()) {
                            msg = "Subscription failed";

                        }
                        Toast.makeText(SettinggsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}