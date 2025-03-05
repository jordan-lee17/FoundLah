package com.example.foundlah;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messageList;
    private String currentUserEmail;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
        this.currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        boolean isCurrentUser = message.getSender().equals(currentUserEmail);

        Log.d("MessageAdapter", "Binding: " + message.getText()); // Log binding // Log binding)

        if (isCurrentUser) {
            holder.senderTextView.setText("You:");
        } else {
            holder.senderTextView.setText(message.getSender());
        }

        holder.messageTextView.setText(message.getText());
    }

    @Override
    public int getItemCount() {
        Log.d("MessageAdapter", "Item count: " + messageList.size());
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView, messageTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.senderTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }
    }
}