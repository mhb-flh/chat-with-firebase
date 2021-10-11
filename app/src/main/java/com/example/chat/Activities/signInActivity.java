package com.example.chat.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chat.databinding.ActivitySignInBinding;
import com.example.chat.utilities.PreferenceManager;
import com.example.chat.utilities.constants;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;

public class signInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(constants.KEY_IS_SIGN_IN)){

            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        setListener();

    }

    private void setListener() {
        binding.createNewAccount.setOnClickListener(view ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.loginBtn.setOnClickListener(view -> {
            if (isValidSignIn()){
                signIn();
            }
        });
    }

    private void signIn(){
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(constants.KEY_COLLECTION_USER)
                .whereEqualTo(constants.KEY_EMAIL,binding.inputEmailLogin.getText().toString())
                .whereEqualTo(constants.KEY_PASSWORD,binding.inputPasswordLogin.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() !=null && task.getResult().getDocuments().size()>0){
                        DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                        preferenceManager.putBooleqn(constants.KEY_IS_SIGN_IN,true);
                        preferenceManager.putString(constants.KEY_USER_ID,documentSnapshot.getId());
                        preferenceManager.putString(constants.KEY_NAME,documentSnapshot.getString(constants.KEY_NAME));
                        preferenceManager.putString(constants.KEY_IMAGE,documentSnapshot.getString(constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }else {
                        loading(false);
                        showToast("unable to sign in");
                    }
                });

    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.loginBtn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.loginBtn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignIn(){
       if (binding.inputEmailLogin.getText().toString().trim().isEmpty()) {
            showToast("enter email ");
            return false;
        }  else if (! Patterns.EMAIL_ADDRESS.matcher(binding.inputEmailLogin.getText().toString()).matches()) {
            showToast("enter valid email");
            return false;
        } else if (binding.inputPasswordLogin.getText().toString().trim().isEmpty()) {
            showToast("enter password");
            return false;
        }else {
           return true;
       }
    }


}