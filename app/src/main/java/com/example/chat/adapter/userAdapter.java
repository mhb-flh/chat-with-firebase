package com.example.chat.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.chat.databinding.ItemContainerUserBinding;
import com.example.chat.listeners.userListener;
import com.example.chat.models.Users;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class userAdapter extends RecyclerView.Adapter<userAdapter.userViewHolder> {

    private final List<Users> users;
    private final userListener userListener;

    public userAdapter(List<Users> users, com.example.chat.listeners.userListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public userViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding=ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),parent,false);
        return new userViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull userViewHolder holder, int position) {
            holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class userViewHolder extends RecyclerView.ViewHolder {

        ItemContainerUserBinding binding;

        public userViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding=itemContainerUserBinding;
        }


        void setUserData(Users users){
            binding.txtName.setText(users.name);
            binding.txtEmail.setText(users.email);
            binding.profileImg.setImageBitmap(getUserImage(users.image));
            binding.getRoot().setOnClickListener(view -> userListener.onUserClicked(users));
        }

    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes= Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
}
