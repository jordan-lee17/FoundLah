package com.example.foundlah

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OngoingActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var lostAdapter: ItemAdapter
    private lateinit var foundAdapter: ItemAdapter
    private val lostItemsList = mutableListOf<Pair<String?, ItemData?>>()
    private val foundItemsList = mutableListOf<Pair<String?, ItemData?>>()
    private lateinit var ongoingText: TextView
    private var ongoingCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ongoing)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://foundlah-31344-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
        val lostItemsLayout = findViewById<RecyclerView>(R.id.lostItemContainer)
        val foundItemsLayout = findViewById<RecyclerView>(R.id.foundItemContainer)
        ongoingText = findViewById(R.id.ongoing_no)

        lostItemsLayout.layoutManager = LinearLayoutManager(this)
        foundItemsLayout.layoutManager = LinearLayoutManager(this)
        lostAdapter = ItemAdapter(this, lostItemsList) { itemId, itemData ->
            openItemDetails(itemId, itemData)
        }

        foundAdapter = ItemAdapter(this, foundItemsList) { itemId, itemData ->
            openItemDetails(itemId, itemData)
        }
        lostItemsLayout.adapter = lostAdapter
        foundItemsLayout.adapter = foundAdapter

        val currentUser = auth.currentUser
        if (currentUser != null) {
            fetchUserItems(currentUser.uid)
            ongoingText.text = ongoingCount.toString()
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
        }

        val backButton = findViewById<Button>(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

        attachSwipeToDelete(lostItemsLayout, lostItemsList, lostAdapter, "lost items")
        attachSwipeToDelete(foundItemsLayout, foundItemsList, foundAdapter, "found items")
    }

    private fun openItemDetails(itemId: String, itemData: ItemData) {
        val intent = Intent(this, ItemDetailsActivity::class.java)
        intent.putExtra("itemData", itemData)
        intent.putExtra("itemId", itemId)
        startActivity(intent)
    }

    private fun fetchUserItems(userId: String) {
        database.child("lost items").orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lostItemsList.clear()
                    if (!snapshot.exists()) {
                        lostItemsList.add(null to null)
                    } else {
                        for (itemSnapshot in snapshot.children) {
                            val item = itemSnapshot.getValue(ItemData::class.java)
                            if (item != null) {
                                lostItemsList.add(itemSnapshot.key!! to item)
                            }
                        }
                    }
                    lostAdapter.notifyDataSetChanged()
                    updateOngoingCount()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@OngoingActivity, "Failed to load items", Toast.LENGTH_SHORT).show()
                }
            })
        database.child("found items").orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    foundItemsList.clear()
                    if(!snapshot.exists()) {
                        foundItemsList.add(null to null)
                    } else {
                        for (itemSnapshot in snapshot.children) {
                            val item = itemSnapshot.getValue(ItemData::class.java)
                            if (item != null) {
                                foundItemsList.add(itemSnapshot.key!! to item)
                            }
                        }
                    }
                    foundAdapter.notifyDataSetChanged()
                    updateOngoingCount()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@OngoingActivity, "Failed to load items", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Update ongoingCount
    private fun updateOngoingCount() {
        val lostCount = if (lostAdapter.itemCount == 1 && lostItemsList.firstOrNull()?.first == null) 0 else lostAdapter.itemCount
        val foundCount = if (foundAdapter.itemCount == 1 && foundItemsList.firstOrNull()?.first == null) 0 else foundAdapter.itemCount

        ongoingCount = lostCount + foundCount
        ongoingText.text = ongoingCount.toString()
    }


    private fun attachSwipeToDelete(
        recyclerView: RecyclerView,
        itemList: MutableList<Pair<String?, ItemData?>>,
        adapter: ItemAdapter,
        databasePath: String
    ) {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                // Check if this is the empty view holder
                if (position >= 0 && position < itemList.size &&
                    itemList[position].first == null && itemList[position].second == null) {
                    // Don't allow swiping the empty state
                    adapter.notifyItemChanged(position)
                    return
                }

                val (itemId, _) = itemList[position]

                AlertDialog.Builder(this@OngoingActivity)
                    .setTitle("Delete report")
                    .setMessage("Are you sure you want to delete this report and all associated matches?")
                    .setPositiveButton("Delete") { _, _ ->
                        if (itemId != null) {
                            deleteItemAndMatches(itemId, databasePath) { success ->
                                if (success) {
                                    Toast.makeText(this@OngoingActivity, "Report and matches deleted", Toast.LENGTH_SHORT).show()
                                    // Remove the item from the list
                                    itemList.removeAt(position)

                                    // If the list is now empty, add the empty placeholder
                                    if (itemList.isEmpty()) {
                                        itemList.add(Pair(null, null))
                                        adapter.notifyDataSetChanged()
                                    } else {
                                        adapter.notifyItemRemoved(position)
                                    }

                                    updateOngoingCount()
                                } else {
                                    Toast.makeText(this@OngoingActivity, "Failed to delete report", Toast.LENGTH_SHORT).show()
                                    adapter.notifyItemChanged(position)
                                }
                            }
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        adapter.notifyItemChanged(position)
                    }
                    .show()
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun deleteItemAndMatches(itemId: String, databasePath: String, callback: (Boolean) -> Unit) {
        val matchesRef = database.child("matches")

        // Delete report
        database.child(databasePath).child(itemId).removeValue()
            .addOnSuccessListener {
                var tasksCompleted = 0
                var isSuccess = true

                // Check if both deletions are done
                fun checkCompletion() {
                    tasksCompleted++
                    if (tasksCompleted == 2) {
                        callback(isSuccess)
                    }
                }

                // Delete matches where item is submittedItemId
                matchesRef.orderByChild("submittedItemId").equalTo(itemId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val updates = mutableMapOf<String, Any?>()
                            for (match in snapshot.children) {
                                updates[match.key!!] = null
                            }
                            matchesRef.updateChildren(updates)
                                .addOnSuccessListener {
                                    checkCompletion()
                                }
                                .addOnFailureListener {
                                    isSuccess = false
                                    checkCompletion()
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            isSuccess = false
                            checkCompletion()
                        }
                    })

                // Delete matches where item is matchedItemId
                matchesRef.orderByChild("matchedItemId").equalTo(itemId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val updates = mutableMapOf<String, Any?>()
                            for (match in snapshot.children) {
                                updates[match.key!!] = null
                            }
                            matchesRef.updateChildren(updates)
                                .addOnSuccessListener {
                                    checkCompletion()
                                }
                                .addOnFailureListener {
                                    isSuccess = false
                                    checkCompletion()
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            isSuccess = false
                            checkCompletion()
                        }
                    })
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}