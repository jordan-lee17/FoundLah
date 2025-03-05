package com.example.foundlah

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foundlah.ConvoAdapter.ConvoViewHolder

class ConvoAdapter(
    private val convoList: List<Convo>,
    private val onUserClickListener: OnUserClickListener
) :
    RecyclerView.Adapter<ConvoViewHolder>() {
    fun interface OnUserClickListener {
        fun onUserClick(convo: Convo?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConvoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_convo, parent, false)
        return ConvoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConvoViewHolder, position: Int) {
        val convo = convoList[position]
        holder.user1.text = convo.user1
        holder.user2.text = convo.user2

        Log.d("ConvoAdapter", "Binding" + convo.user1 + "&" + convo.user2)
        holder.itemView.setOnClickListener { v: View? ->
            onUserClickListener.onUserClick(
                convo
            )
        }
    }

    override fun getItemCount(): Int {
        return convoList.size
    }

    class ConvoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var user1: TextView =
            itemView.findViewById(R.id.user1)
        var user2: TextView =
            itemView.findViewById(R.id.user2)
    }
}
