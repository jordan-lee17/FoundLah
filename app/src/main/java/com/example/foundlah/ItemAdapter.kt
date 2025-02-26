package com.example.foundlah

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ItemAdapter(
    private val context: Context,
    private val itemList: MutableList<Pair<String?, ItemData?>>,
    private val onItemClick: (String, ItemData) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_EMPTY = 0
    private val VIEW_NOT_EMPTY = 1

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.itemName)
        val itemDate: TextView = itemView.findViewById(R.id.itemDate)
        val matchBadge: TextView = itemView.findViewById(R.id.matchBadge)
    }

    inner class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emptyText: TextView = itemView.findViewById(R.id.emptyText)
    }

    override fun getItemViewType(position: Int): Int {
        return if (itemList[position].first == null && itemList[position].second == null) VIEW_EMPTY else VIEW_NOT_EMPTY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_EMPTY) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_empty_list, parent, false)
            EmptyViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_list_row, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val (itemId, itemData) = itemList[position]

            if (itemData != null) {
                holder.itemName.text = itemData.name
                holder.itemDate.text = itemData.date

                checkForMatches(itemId) { matchCount ->
                    if (matchCount > 0) {
                        holder.matchBadge.text = matchCount.toString() // Set match count
                        holder.matchBadge.visibility = View.VISIBLE
                    } else {
                        holder.matchBadge.visibility = View.GONE
                    }
                }
            }

            // Set Click Listener to Open ItemDetailsActivity
            holder.itemView.setOnClickListener {
                if (itemId != null && itemData != null) {
                        onItemClick(itemId, itemData)
                }
            }
        } else if (holder is EmptyViewHolder) {
            holder.emptyText.text = "No reports found."
        }
    }

    override fun getItemCount(): Int = itemList.size

    private fun checkForMatches(itemId: String?, callback: (Int) -> Unit) {
        if (itemId == null) {
            callback(0)
            return
        }

        val matchesRef = FirebaseDatabase.getInstance("https://foundlah-31344-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child("matches")
        var matchCount = 0

        // Check for submittedItemId matches
        matchesRef.orderByChild("submittedItemId").equalTo(itemId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    matchCount += snapshot.childrenCount.toInt()

                    // Check for matchedItemId matches
                    matchesRef.orderByChild("matchedItemId").equalTo(itemId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                matchCount += snapshot.childrenCount.toInt()
                                callback(matchCount) // Return final count
                            }
                            override fun onCancelled(error: DatabaseError) {
                                callback(matchCount)
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(0)
                }
            })
    }

}