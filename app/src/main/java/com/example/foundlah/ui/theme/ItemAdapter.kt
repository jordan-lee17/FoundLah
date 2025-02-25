package com.example.foundlah

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
            }
            if (itemData != null) {
                holder.itemDate.text = itemData.date
            }

            // Set Click Listener to Open ItemDetailsActivity
            holder.itemView.setOnClickListener {
                if (itemId != null) {
                    if (itemData != null) {
                        onItemClick(itemId, itemData)
                    }
                }
            }
        } else if (holder is EmptyViewHolder) {
            holder.emptyText.text = "No reports found."
        }
    }

    override fun getItemCount(): Int = itemList.size
}