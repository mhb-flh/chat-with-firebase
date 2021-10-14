package com.example.chat.Activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.chat.adapter.chatAdapter;
import com.example.chat.databinding.ActivityChatBinding;
import com.example.chat.models.Users;
import com.example.chat.models.chatMessage;
import com.example.chat.network.ApiClient;
import com.example.chat.network.ApiService;
import com.example.chat.utilities.PreferenceManager;
import com.example.chat.utilities.constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class chatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private Users receiverUser;
    private List<chatMessage> chatMessages;
    private chatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversationId = null;
    private Boolean isReceiverOnline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        listenMessage();
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(constants.KEY_SENDER_ID, preferenceManager.getString(constants.KEY_USER_ID));
        message.put(constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(constants.KEY_TIMESTAMP, new Date());
        database.collection(constants.KEY_COLLECTION_CHAT).add(message);
        if (conversationId != null) {
            updateConversation(binding.inputMessage.getText().toString());
        } else {
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put(constants.KEY_SENDER_ID, preferenceManager.getString(constants.KEY_USER_ID));
            conversation.put(constants.KEY_SENDER_NAME, preferenceManager.getString(constants.KEY_NAME));
            conversation.put(constants.KEY_SENDER_IMAGE, preferenceManager.getString(constants.KEY_IMAGE));
            conversation.put(constants.KEY_RECEIVER_ID, receiverUser.id);
            conversation.put(constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversation.put(constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversation.put(constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversation.put(constants.KEY_TIMESTAMP, new Date());
            addConversation(conversation);
        }
        if (! isReceiverOnline){
            try {
                    JSONArray tokens=new JSONArray();
                    tokens.put(receiverUser.token);

                    JSONObject data=new JSONObject();
                    data.put(constants.KEY_USER_ID,preferenceManager.getString(constants.KEY_USER_ID));
                    data.put(constants.KEY_NAME,preferenceManager.getString(constants.KEY_NAME));
                    data.put(constants.KEY_FCM_TOKEN,preferenceManager.getString(constants.KEY_FCM_TOKEN));
                    data.put(constants.KEY_MESSAGE,binding.inputMessage.getText().toString());

                    JSONObject body=new JSONObject();
                    body.put(constants.REMOTE_MSG_DATA,data);
                    body.put(constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

                    sendNotification(body.toString());

            }catch (Exception exception){
                showToast(exception.getMessage());
            }
        }
        binding.inputMessage.setText(null);
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new chatAdapter(
                chatMessages, getBitmapFromEncodedImage(receiverUser.image),
                preferenceManager.getString(constants.KEY_USER_ID)
        );
        binding.chatRecycler.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()){
                    try {
                        if (response.body() != null){
                            JSONObject responseJson=new JSONObject(response.body());
                            JSONArray results=responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1){
                                JSONObject error=(JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    showToast("Notification sent successfully");
                }else {
                    showToast("Error : "+response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void ListenAvailabilityOfReceiver() {
        database.collection(constants.KEY_COLLECTION_USER).document(
                receiverUser.id
        ).addSnapshotListener(chatActivity.this, (value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getLong(constants.KEY_AVAILABILITY) != null) {
                    int availability = Objects.requireNonNull(
                            value.getLong(constants.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverOnline = availability == 1;
                }

                receiverUser.token=value.getString(constants.KEY_FCM_TOKEN);
                if (receiverUser.image == null){
                    receiverUser.image=value.getString(constants.KEY_IMAGE);
                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodedImage(receiverUser.image));
                    chatAdapter.notifyItemRangeChanged(0,chatMessages.size());
                }
            }
            if (isReceiverOnline){
                binding.txtAvailability.setVisibility(View.VISIBLE);
            }else {
                binding.txtAvailability.setVisibility(View.GONE);
            }

        });
    }

    private void listenMessage() {
        database.collection(constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(constants.KEY_SENDER_ID, preferenceManager.getString(constants.KEY_USER_ID))
                .whereEqualTo(constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(constants.KEY_RECEIVER_ID, preferenceManager.getString(constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    chatMessage chatMessage = new chatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(constants.KEY_MESSAGE);
                    chatMessage.DateTime = getReadableDateTime(documentChange.getDocument().getDate(constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecycler.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecycler.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversationId == null) {
            checkForConversation();
        }
    };

    private Bitmap getBitmapFromEncodedImage(String encodedImage) {
        if (encodedImage != null){
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }else {
            return null;
        }

    }

    private void loadReceiverDetails() {
        receiverUser = (Users) getIntent().getSerializableExtra(constants.KEY_USER);
        binding.txtName.setText(receiverUser.name);
    }

    private void setListeners() {
        binding.imgBack.setOnClickListener(view -> onBackPressed());
        binding.layoutSend.setOnClickListener(view -> sendMessage());
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd,yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversation(HashMap<String, Object> conversation) {
        database.collection(constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    private void updateConversation(String message) {
        DocumentReference documentReference =
                database.collection(constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        documentReference.update(
                constants.KEY_LAST_MESSAGE, message,
                constants.KEY_TIMESTAMP, new Date()
        );
    }

    private void checkForConversation() {
        if (chatMessages.size() != 0) {
            checkForConversationRemotely(
                    preferenceManager.getString(constants.KEY_USER_ID)
                    , receiverUser.id
            );
            checkForConversationRemotely(
                    receiverUser.id,
                    preferenceManager.getString(constants.KEY_USER_ID)
            );

        }
    }

    private void checkForConversationRemotely(String senderId, String receiverId) {
        database.collection(constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);

    }

    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        ListenAvailabilityOfReceiver();
    }
}