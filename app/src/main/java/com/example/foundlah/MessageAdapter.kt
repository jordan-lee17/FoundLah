package com.example.foundlah

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foundlah.MessageAdapter.MessageViewHolder
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(private val messageList: MutableList<Message?>) :
    RecyclerView.Adapter<MessageViewHolder>() {
    private val currentUserEmail = FirebaseAuth.getInstance().currentUser!!.email

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        val isCurrentUser = message?.sender == currentUserEmail

        Log.d("MessageAdapter", "Binding: " + message?.text) // Log binding // Log binding)

        if (isCurrentUser) {
            holder.senderTextView.text = "You:"
        } else {
            holder.senderTextView.text = message?.sender
        }

        holder.messageTextView.text = message?.text
    }

    override fun getItemCount(): Int {
        Log.d("MessageAdapter", "Item count: " + messageList.size)
        return messageList.size
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var senderTextView: TextView =
            itemView.findViewById(R.id.senderTextView)
        var messageTextView: TextView =
            itemView.findViewById(R.id.messageTextView)
    }
}