package com.example.chat.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.example.chat.R;
import com.example.chat.databinding.ActivityMainBinding;
import com.example.chat.utilities.PreferenceManager;
import com.example.chat.utilities.constants;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager=new PreferenceManager(getApplicationContext());
        loadUserDetail();
        getToken();
        setListeners();

    }

    private void setListeners(){
        binding.imgSignOut.setOnClickListener(view -> signOut());
    }

    private void loadUserDetail(){
        binding.txtName.setText(preferenceManager.getString(constants.KEY_NAME));
        byte[] bytes= Base64.decode(preferenceManager.getString(constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        FirebaseFirestore database =FirebaseFirestore.getInstance();
        DocumentReference documentReference=database.collection(constants.KEY_COLLECTION_USER).document(
                preferenceManager.getString(constants.KEY_USER_ID)
        );
        documentReference.update(constants.KEY_FCM_TOKEN,token)
                .addOnSuccessListener(unused -> showToast("Token updated successfully"))
                .addOnFailureListener(e -> showToast( "unable to update token"));
    }

    private void signOut(){
        showToast("signing out ...");
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference documentReference=database.collection(constants.KEY_COLLECTION_USER).document(
                preferenceManager.getString(constants.KEY_USER_ID)
        );
        HashMap<String,Object> updates=new HashMap<>();
        updates.put(constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(),signInActivity.class));
                    finish();
                }).addOnFailureListener(e -> showToast("unable to sign out"));
    }





}