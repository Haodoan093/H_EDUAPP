package com.example.h_eduapp.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;


import com.example.h_eduapp.ChatActivity;
import com.example.h_eduapp.PostDetailActivity;
import com.example.h_eduapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class FirebaseMessaging extends FirebaseMessagingService {

    private static final String ADMIN_CHANNEL_ID = "admin_channel";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);


        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String savedCurrentUser = sp.getString("Current_USERID", "None");


        if (message.getData() != null) {


            String notificationType = message.getData().get("nofiticationType");

            if (notificationType != null &&notificationType.equals("PostNotification")) {

                String sender = message.getData().get("sender");
                String pId = message.getData().get("pId");
                String pTitle = message.getData().get("pTitle");
                String pDescription = message.getData().get("pDescription");


                if (!sender.equals(savedCurrentUser)) {
                    showPostNotification("" + pId, "" + pTitle, "" + pDescription);
                }
            } else if (notificationType != null &&notificationType.equals("ChatNotification")) {
                String sent = message.getData().get("sent");
                String user = message.getData().get("user");
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null && sent.equals(firebaseUser.getUid())) {
                    if (!savedCurrentUser.equals(user)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sendOAndAboveNotification(message);
                        } else {
                            sendNormalNotification(message);
                        }
                    }
                }
            }else {

            }
        }




    }

    private void showPostNotification(String pId, String pTitle, String pDescription) {

        NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int notifID= new Random().nextInt(3000);

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            setupPostNotificationChannel(notificationManager);
        }

        Intent intent= new Intent(this, PostDetailActivity.class);
        intent.putExtra("postId",pId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent= PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Bitmap largeIcon= BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        Uri notificatonSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder= new NotificationCompat.Builder(this,""+ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(largeIcon)
                .setContentTitle(pTitle)
                .setContentText(pDescription)


                .setSound(notificatonSoundUri)
                .setContentIntent(pendingIntent);

notificationManager.notify(notifID,notificationBuilder.build());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupPostNotificationChannel(NotificationManager notificationManager) {
        CharSequence  channelName="New Notification";

        String channelDescription="Device to device pos notification";

        NotificationChannel adminchannel= new NotificationChannel(ADMIN_CHANNEL_ID,channelName,NotificationManager.IMPORTANCE_HIGH);
        adminchannel.setDescription(channelDescription);
        adminchannel.enableLights(true);
        adminchannel.setLightColor(Color.RED);
        adminchannel.enableVibration(true);
        if(notificationManager!=null){
            notificationManager.createNotificationChannel(adminchannel);
        }

    }

    private void sendOAndAboveNotification(RemoteMessage message) {
        String user = message.getData().get("user");
        String icon = message.getData().get("icon");
        String title = message.getData().get("title");
        String body = message.getData().get("body");

        RemoteMessage.Notification notification = message.getNotification();
        int i = Integer.parseInt(user.replaceAll("\\D", ""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid", user);

        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentText(body).setContentTitle(title)
                .setAutoCancel(true)
                .setSound(defSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int j = 0;
        if (i > 0) {
            j = i;
        }
        notificationManager.notify(j, builder.build());


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNormalNotification(RemoteMessage message) {
        String user = message.getData().get("user");
        String icon = message.getData().get("icon");
        String title = message.getData().get("title");
        String body = message.getData().get("body");

        RemoteMessage.Notification notification = message.getNotification();
        int i = Integer.parseInt(user.replaceAll("\\D", ""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid", user);

        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        Notification.Builder builder = notification1.getONotifications(title, body, pendingIntent, defSoundUri, icon);


        int j = 0;
        if (i > 0) {
            j = i;
        }
        notification1.getManager().notify(j, builder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            //signed in
            updateToken(token);
        }
    }

    private void updateToken(String tokenRefresh) {

        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1= new Token(tokenRefresh);
        ref.child(user.getUid()).setValue(token1);
    }
}
