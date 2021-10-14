package com.example.chat.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import com.example.chat.Activities.chatActivity;
import com.example.chat.R;
import com.example.chat.models.Users;
import com.example.chat.utilities.constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MessagingService extends FirebaseMessagingService {


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Users user = new Users();
        user.id = remoteMessage.getData().get(constants.KEY_USER_ID);
        user.name = remoteMessage.getData().get(constants.KEY_NAME);
        user.token = remoteMessage.getData().get(constants.KEY_FCM_TOKEN);

        int notificationId = new Random().nextInt();
        String channelId = "chat_message";

        Intent intent = new Intent(this, chatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(constants.KEY_USER, user);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setSmallIcon(R.drawable.ic_notifications);
        builder.setContentTitle(user.name);
        builder.setContentText(remoteMessage.getData().get(constants.KEY_MESSAGE));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(
                remoteMessage.getData().get(constants.KEY_MESSAGE)
        ));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName="chat message";
            String channelDescription="this notification channel is used for chat message notification ";
            int importance= NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel=new NotificationChannel(channelId,channelName,importance);
            notificationChannel.setDescription(channelDescription);
            NotificationManager notificationManager=getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);

        }

        NotificationManagerCompat notificationManagerCompat=NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId, builder.build());
    }
}
