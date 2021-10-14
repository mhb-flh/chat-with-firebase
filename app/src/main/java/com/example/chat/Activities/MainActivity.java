package com.example.chat.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.chat.adapter.RecentConversationAdapter;
import com.example.chat.databinding.ActivityMainBinding;
import com.example.chat.listeners.conversationListener;
import com.example.chat.models.Users;
import com.example.chat.models.chatMessage;
import com.example.chat.utilities.PreferenceManager;
import com.example.chat.utilities.constants;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements conversationListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<chatMessage> conversations;
    private RecentConversationAdapter conversationAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        loadUserDetail();
        getToken();
        setListeners();
        listenConversations();
    }

    private void init() {
        conversations = new ArrayList<>();
        conversationAdapter = new RecentConversationAdapter(conversations, this);
        binding.recentConversationRecycler.setAdapter(conversationAdapter);
        database = FirebaseFirestore.getInstance();

    }

    private void setListeners() {
        binding.imgSignOut.setOnClickListener(view -> signOut());
        binding.fabNewChat.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), UsersActivity.class));
        });
    }

    private void loadUserDetail() {
        binding.txtName.setText(preferenceManager.getString(constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void listenConversations() {
        database.collection(constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(constants.KEY_SENDER_ID, preferenceManager.getString(constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(constants.KEY_RECEIVER_ID, preferenceManager.getString(constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(constants.KEY_RECEIVER_ID);
                    chatMessage chatMessage = new chatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if (preferenceManager.getString(constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversationImage = documentChange.getDocument().getString(constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(constants.KEY_RECEIVER_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(constants.KEY_RECEIVER_ID);
                    } else {
                        chatMessage.conversationImage = documentChange.getDocument().getString(constants.KEY_SENDER_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(constants.KEY_SENDER_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(constants.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(constants.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)) {
                            conversations.get(i).message = documentChange.getDocument().getString(constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationAdapter.notifyDataSetChanged();
            binding.recentConversationRecycler.smoothScrollToPosition(0);
            binding.recentConversationRecycler.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        preferenceManager.putString(constants.KEY_FCM_TOKEN,token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(constants.KEY_COLLECTION_USER).document(
                preferenceManager.getString(constants.KEY_USER_ID)
        );
        documentReference.update(constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("unable to update token"));
    }

    private void signOut() {
        showToast("signing out ...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(constants.KEY_COLLECTION_USER).document(
                preferenceManager.getString(constants.KEY_USER_ID)
        );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), signInActivity.class));
                    finish();
                }).addOnFailureListener(e -> showToast("unable to sign out"));
    }


    @Override
    public void onConversationClicked(Users users) {
        Intent intent = new Intent(getApplicationContext(), chatActivity.class);
        intent.putExtra(constants.KEY_USER, users);
        startActivity(intent);
    }
}