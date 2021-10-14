package com.example.chat.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.chat.databinding.ItemContainerReceiveMessageBinding;
import com.example.chat.databinding.ItemContainerSentMessageBinding;
import com.example.chat.models.chatMessage;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class chatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<chatMessage> chatMessages;
    private Bitmap receiverProfileImage;
    private final String senderId;

    public void setReceiverProfileImage(Bitmap bitmap){
        receiverProfileImage=bitmap;
    }

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public chatAdapter(List<chatMessage> chatMessages, Bitmap receiverProfileImage, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==VIEW_TYPE_SENT){
            return new sendMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),parent,false)
            );
        }else {
                return new receivedMessageViewHolder(
                        ItemContainerReceiveMessageBinding.inflate(
                                LayoutInflater.from(parent.getContext()),parent,false)
                );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position)==VIEW_TYPE_SENT){
            ((sendMessageViewHolder)holder).setData(chatMessages.get(position));
        }else {
            ((receivedMessageViewHolder) holder).setData(chatMessages.get(position),receiverProfileImage);
        }

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public int getItemViewType(int position){
        if (chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class sendMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerSentMessageBinding binding;

        sendMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(chatMessage chatMessage) {
            binding.txtMessage.setText(chatMessage.message);
            binding.txtDateTime.setText(chatMessage.DateTime);

        }
    }

    static class receivedMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerReceiveMessageBinding binding;

        public receivedMessageViewHolder(ItemContainerReceiveMessageBinding itemContainerReceiveMessageBinding) {
            super(itemContainerReceiveMessageBinding.getRoot());
            binding = itemContainerReceiveMessageBinding;
        }

        void setData(chatMessage chatMessage, Bitmap receiveProfileImage) {
            binding.txtMessage.setText(chatMessage.message);
            binding.txtDateTime.setText(chatMessage.DateTime);
            if (receiveProfileImage != null){
                binding.imageProfile.setImageBitmap(receiveProfileImage);
            }


        }

    }


}
