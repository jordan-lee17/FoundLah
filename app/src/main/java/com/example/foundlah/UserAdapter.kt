package com.example.foundlah

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foundlah.UserAdapter.UserViewHolder

class UserAdapter(
    private val userList: List<User>,
    private val onUserClickListener: OnUserClickListener
) :
    RecyclerView.Adapter<UserViewHolder>() {
    // Interface for click listener
    fun interface OnUserClickListener {
        fun onUserClick(user: User?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.nameTextView.text = user.name
        holder.emailTextView.text = user.email

        holder.itemView.setOnClickListener { v: View? ->
            onUserClickListener.onUserClick(
                user
            )
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nameTextView: TextView =
            itemView.findViewById(R.id.userName)
        var emailTextView: TextView =
            itemView.findViewById(R.id.userEmail)
    }
}
