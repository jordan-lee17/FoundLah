package com.example.foundlah

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OngoingActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var lostItemsLayout: LinearLayout
    private lateinit var foundItemsLayout: LinearLayout
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
        lostItemsLayout = findViewById(R.id.lostItemContainer)
        foundItemsLayout = findViewById(R.id.foundItemContainer)
        ongoingText = findViewById(R.id.ongoing_no)

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
    }

    private fun fetchUserItems(userId: String) {
        var lostItemsCount = 0
        var foundItemsCount = 0
        var queriesCompleted = 0

        database.child("lost items").orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lostItemsLayout.removeAllViews()
                    lostItemsCount = snapshot.childrenCount.toInt()
                    for (itemSnapshot in snapshot.children) {
                        lostItemsLayout.addView(createItemButton(itemSnapshot))
                    }

                    queriesCompleted++
                    updateOngoingCount(lostItemsCount, foundItemsCount, queriesCompleted)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@OngoingActivity, "Failed to load items", Toast.LENGTH_SHORT).show()
                }
            })
        database.child("found items").orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    foundItemsLayout.removeAllViews()
                    foundItemsCount = snapshot.childrenCount.toInt()
                    for (itemSnapshot in snapshot.children) {
                        foundItemsLayout.addView(createItemButton(itemSnapshot))
                    }

                    queriesCompleted++
                    updateOngoingCount(lostItemsCount, foundItemsCount, queriesCompleted)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@OngoingActivity, "Failed to load items", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Update ongoingCount when both queries complete
    private fun updateOngoingCount(lostCount: Int, foundCount: Int, queriesCompleted: Int) {
        if (queriesCompleted == 2) { // Ensure both queries are done
            ongoingCount = lostCount + foundCount
            ongoingText.text = ongoingCount.toString()
        }
    }

    private fun createItemButton(itemSnapshot: DataSnapshot): Button {
        return Button(this).apply {
            val itemName = itemSnapshot.child("name").getValue(String::class.java)
            val itemLocation = itemSnapshot.child("location").getValue(String::class.java)
            val itemDate = itemSnapshot.child("date").getValue(String::class.java)
            val itemId = itemSnapshot.key ?: ""

            val itemData = ItemData(
                itemName,
                itemSnapshot.child("category").getValue(String::class.java),
                itemDate,
                itemLocation,
                itemSnapshot.child("description").getValue(String::class.java),
                itemSnapshot.child("imageBase64").getValue(String::class.java),
                itemSnapshot.child("type").getValue(String::class.java)
            )

            text = "${itemName}\n${itemLocation}\n${itemDate}"
            background = ContextCompat.getDrawable(this@OngoingActivity, R.drawable.rounded_button)
            setTextColor(resources.getColor(android.R.color.white))
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(20, 25, 20, 25)
            }
            setOnClickListener {
                val intent = Intent(this@OngoingActivity, ItemDetailsActivity::class.java)
                intent.putExtra("itemData", itemData)
                intent.putExtra("itemId", itemId)
                startActivity(intent)
            }
        }
    }

}