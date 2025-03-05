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

// Display items in RecycleView
class ItemAdapter(
    private val context: Context,
    private val itemList: MutableList<Pair<String?, ItemData?>>,
    private val onItemClick: (String, ItemData) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_EMPTY = 0
    private val VIEW_NOT_EMPTY = 1

    // Viewholder for displaying items
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.itemName)
        val itemDate: TextView = itemView.findViewById(R.id.itemDate)
        val matchBadge: TextView = itemView.findViewById(R.id.matchBadge)
    }

    // Viewholder for displaying empty list
    inner class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emptyText: TextView = itemView.findViewById(R.id.emptyText)
    }

    // Type of view to display based on item data
    override fun getItemViewType(position: Int): Int {
        return if (itemList[position].first == null && itemList[position].second == null) VIEW_EMPTY else VIEW_NOT_EMPTY
    }

    // create view holder based on view type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_EMPTY) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_empty_list, parent, false)
            EmptyViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_list_row, parent, false)
            ItemViewHolder(view)
        }
    }

    // binds data to the viewholder, handle both regular and empty state
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val (itemId, itemData) = itemList[position]

            if (itemData != null) {
                // set item data
                holder.itemName.text = itemData.name
                holder.itemDate.text = itemData.date

                // Check for any matches in database, update app UI accordingly
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
            // Set test for empty state
            holder.emptyText.text = "No reports found."
        }
    }

    // get total number of items in db
    override fun getItemCount(): Int = itemList.size

    // to check for matches in database
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

    // Update the list when items removed
    fun updateList(newList: List<Pair<String?, ItemData?>>) {
        itemList.clear()

        // Add empty state placeholder if list is empty
        if (newList.isEmpty()) {
            itemList.add(Pair(null, null))
        } else {
            itemList.addAll(newList)
        }

        notifyDataSetChanged()
    }

    // Remove item and show empty state 
    fun removeItem(position: Int) {
        if (position < 0 || position >= itemList.size) return

        itemList.removeAt(position)

        // If list is now empty, add empty placeholder
        if (itemList.isEmpty()) {
            itemList.add(Pair(null, null))
            notifyDataSetChanged()
        } else {
            notifyItemRemoved(position)
        }
    }

}