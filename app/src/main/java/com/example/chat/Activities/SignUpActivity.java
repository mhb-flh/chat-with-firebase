package com.example.chat.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chat.R;
import com.example.chat.databinding.ActivitySignInBinding;
import com.example.chat.databinding.ActivitySignUpBinding;
import com.example.chat.utilities.PreferenceManager;
import com.example.chat.utilities.constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.Constants;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private String encodedImage;
    private ActivitySignUpBinding binding;
    private com.example.chat.utilities.PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListener();
    }

    private void setListener() {
        binding.txtSignIn.setOnClickListener(view ->
                onBackPressed());
        binding.signUpBtn.setOnClickListener(view -> {
            if (isValidSignUpDetails()) {
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();

        user.put(constants.KEY_NAME, binding.inputNameSignUp.getText().toString());
        user.put(constants.KEY_EMAIL, binding.inputEmailSignUp.getText().toString());
        user.put(constants.KEY_PASSWORD, binding.inputPasswordSignUp.getText().toString());
        user.put(constants.KEY_IMAGE, encodedImage);
        database.collection(constants.KEY_COLLECTION_USER)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBooleqn(constants.KEY_IS_SIGN_IN, true);
                    preferenceManager.putString(constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(constants.KEY_NAME, binding.inputNameSignUp.getText().toString());
                    preferenceManager.putString(constants.KEY_IMAGE, encodedImage);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }).addOnFailureListener(exception -> {
                     loading(false);
                     showToast(exception.getMessage());

        });

    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);

    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.profileImg.setImageBitmap(bitmap);
                            binding.txtAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private boolean isValidSignUpDetails() {
        if (encodedImage == null) {
            Toast.makeText(getApplicationContext(), "select profile image", Toast.LENGTH_SHORT).show();
            return false;
        } else if (binding.inputNameSignUp.getText().toString().trim().isEmpty()) {
            showToast("enter name ");
            return false;
        } else if (binding.inputEmailSignUp.getText().toString().trim().isEmpty()) {
            showToast("enter email");
            return false;
        } else if (! Patterns.EMAIL_ADDRESS.matcher(binding.inputEmailSignUp.getText().toString()).matches()) {
            showToast("enter valid email");
            return false;
        } else if (binding.inputPasswordSignUp.getText().toString().trim().isEmpty()) {
            showToast("enter password");
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("confirmed your password");
            return false;
        } else if (!binding.inputConfirmPassword.getText().toString().equals(binding.inputPasswordSignUp.getText().toString())) {
            showToast("your password and confirm must be the same");
            return false;
        } else {
            return true;
        }

    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.signUpBtn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.signUpBtn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }


}