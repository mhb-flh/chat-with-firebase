package com.example.chat.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.chat.databinding.ItemContainerRecentConversationBinding;
import com.example.chat.listeners.conversationListener;
import com.example.chat.models.Users;
import com.example.chat.models.chatMessage;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.conversationViewHolder> {

    private final List<chatMessage> chatMessages;
    private final conversationListener conversationListener;

    public RecentConversationAdapter(List<chatMessage> chatMessages, conversationListener conversationListener) {
        this.chatMessages = chatMessages;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public conversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new conversationViewHolder(
                ItemContainerRecentConversationBinding.inflate(LayoutInflater.from(
                        parent.getContext()),parent,false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull conversationViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class conversationViewHolder extends RecyclerView.ViewHolder{

        ItemContainerRecentConversationBinding binding;

        public conversationViewHolder(ItemContainerRecentConversationBinding itemContainerRecentConversationBinding) {
            super(itemContainerRecentConversationBinding.getRoot());
            binding=itemContainerRecentConversationBinding;
        }

        void setData(chatMessage chatMessage){
            binding.profileImg.setImageBitmap(getConversationImage(chatMessage.conversationImage));
            binding.txtName.setText(chatMessage.conversationName);
            binding.txtRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(view -> {
                Users user=new Users();
                user.id=chatMessage.conversationId;
                user.name=chatMessage.conversationName;
                user.image=chatMessage.conversationImage;
                conversationListener.onConversationClicked(user);
            });
        }
    }

    private Bitmap getConversationImage(String encodedImage){
        byte[] bytes= Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
}
