package com.example.chat.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chat.adapter.userAdapter;
import com.example.chat.databinding.ActivityUsersBinding;
import com.example.chat.listeners.userListener;
import com.example.chat.models.Users;
import com.example.chat.utilities.PreferenceManager;
import com.example.chat.utilities.constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements userListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }

    private void setListeners() {
        binding.imgBack.setOnClickListener(view -> onBackPressed());
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(constants.KEY_COLLECTION_USER)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserID = preferenceManager.getString(constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Users> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserID.equals(queryDocumentSnapshot.getId())) {

                                continue;
                            }
                            Users user = new Users();
                            user.name = queryDocumentSnapshot.getString(constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            userAdapter userAdapter = new userAdapter(users, this);
                            binding.userRecycler.setAdapter(userAdapter);
                            binding.userRecycler.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }

                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage() {
        binding.ErrorMessage.setText(String.format("%s", "no user available"));
        binding.ErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onUserClicked(Users users) {
        Intent intent = new Intent(getApplicationContext(), chatActivity.class);
        intent.putExtra(constants.KEY_USER, users);
        startActivity(intent);
        finish();
    }
}