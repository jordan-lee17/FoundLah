package com.example.foundlah

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.min

class ItemDetailsActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var potentialMatchesLayout: LinearLayout

    private lateinit var imagePreview: ImageView
    private lateinit var noImageText: TextView
    private lateinit var frameLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_item_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = FirebaseDatabase.getInstance("https://foundlah-31344-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
        potentialMatchesLayout = findViewById(R.id.potentialMatchesContainer)

        imagePreview = findViewById(R.id.imagePreview)
        noImageText = findViewById(R.id.noImageText)
        frameLayout = findViewById(R.id.frameLayout2)

        val name = findViewById<TextView>(R.id.itemTextView)
        var category = findViewById<TextView>(R.id.categoryTextView)
        val date = findViewById<TextView>(R.id.dateTextView)
        val location = findViewById<TextView>(R.id.locationTextView)
        val description = findViewById<TextView>(R.id.descriptionTextView)
        val type: String?

        val itemData: ItemData? = intent.getParcelableExtra("itemData")
        val itemId = intent.getStringExtra("itemId") ?: ""

        if (itemData != null) {
            name.text = "Item: ${itemData.name}"
            category.text = "Category: ${itemData.category}"
            date.text = "Date: ${itemData.date}"
            location.text = "Location: ${itemData.location}"
            description.text = "Description: ${itemData.description}"
            imagePreview = findViewById(R.id.imagePreview)
            if (!itemData?.imageBase64.isNullOrEmpty()) {
                val decodedBitmap = base64ToBitmap(itemData.imageBase64!!)
                imagePreview.setImageBitmap(decodedBitmap)
                noImageText.visibility = TextView.GONE
                adjustFrameLayoutSize(itemData.imageBase64)
            }
            type = itemData.type

            fetchPotentialMatches(itemId, type)
        }

        val backButton = findViewById<Button>(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchPotentialMatches(itemId: String, type: String?) {
        potentialMatchesLayout.removeAllViews()

        var matchesFound = false
        var queriesCompleted = 0

        // Checks if both queries are done
        fun checkCompletion() {
            queriesCompleted++
            if (queriesCompleted == 2 && !matchesFound) {
                val noMatchText = TextView(this@ItemDetailsActivity).apply {
                    text = "No matches found."
                    textSize = 30f
                    setTextColor(ContextCompat.getColor(this@ItemDetailsActivity, android.R.color.holo_red_dark))
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER
                        setMargins(20, 50, 20, 50)
                    }
                }
                potentialMatchesLayout.addView(noMatchText)
            }
        }

        // Query matches where the report is the submitted item
        database.child("matches").orderByChild("submittedItemId").equalTo(itemId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        matchesFound = true
                        for (itemSnapshot in snapshot.children) {
                            val matchedItemId = itemSnapshot.child("matchedItemId").getValue(String::class.java)
                            val matchedUserId = itemSnapshot.child("matchedUserId").getValue(String::class.java)
                            val submittedUserId = itemSnapshot.child("submittedUserId").getValue(String::class.java)
                            val matchScore = itemSnapshot.child("score").getValue(Int::class.java) ?: 0
                            if (matchedItemId != null) {
                                fetchMatchedItemDetails(matchedItemId,matchedUserId, submittedUserId, matchScore, type)
                            }
                        }
                    }
                    checkCompletion()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ItemDetailsActivity, "Failed to load matches", Toast.LENGTH_SHORT).show()
                    checkCompletion()
                }
            })

        // Query matches where the report is the matched item
        database.child("matches").orderByChild("matchedItemId").equalTo(itemId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        matchesFound = true
                        for (itemSnapshot in snapshot.children) {
                            val submittedItemId = itemSnapshot.child("submittedItemId").getValue(String::class.java)
                            val matchedUserId = itemSnapshot.child("matchedUserId").getValue(String::class.java)
                            val submittedUserId = itemSnapshot.child("submittedUserId").getValue(String::class.java)
                            val matchScore = itemSnapshot.child("score").getValue(Int::class.java) ?: 0
                            if (submittedItemId != null) {
                                fetchMatchedItemDetails(submittedItemId, matchedUserId, submittedUserId, matchScore, type)
                            }
                        }
                    }
                    checkCompletion()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ItemDetailsActivity, "Failed to load matches", Toast.LENGTH_SHORT).show()
                    checkCompletion()
                }
            })
    }

    private fun fetchMatchedItemDetails(matchedItemId: String, matchedUserId: String?, submittedUserId: String?, score: Int, type: String?) {
        var oppositeType: String
        if (type == "found") {
            oppositeType = "lost"
        } else {
            oppositeType = "found"
        }
        database.child("${oppositeType} items").child(matchedItemId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(itemSnapshot: DataSnapshot) {
                    if (!itemSnapshot.exists()) {
                        return
                    }

                    val itemName = itemSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                    val itemDate = itemSnapshot.child("date").getValue(String::class.java) ?: "Unknown"

                    val itemData = ItemData(
                        itemName,
                        itemSnapshot.child("category").getValue(String::class.java),
                        itemDate,
                        itemSnapshot.child("location").getValue((String::class.java)),
                        itemSnapshot.child("description").getValue(String::class.java),
                        itemSnapshot.child("imageBase64").getValue(String::class.java),
                        itemSnapshot.child("type").getValue(String::class.java)
                    )

                    val itemButton = Button(this@ItemDetailsActivity).apply {
                        text = "${itemName}\n${itemDate}\nMatch Score: ${score}"
                        background = ContextCompat.getDrawable(this@ItemDetailsActivity, R.drawable.rounded_button)
                        setTextColor(resources.getColor(android.R.color.white))
                        textSize = 16f
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(20, 25, 20, 25)
                        }
                        setOnClickListener {
                            val intent = Intent(this@ItemDetailsActivity, MatchedItemDetailsActivity::class.java)
                            intent.putExtra("itemData", itemData)
                            intent.putExtra("matchedUserId", matchedUserId)
                            intent.putExtra("submittedUserId", submittedUserId)
                            startActivity(intent)
                        }
                    }

                    potentialMatchesLayout.addView(itemButton)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ItemDetailsActivity, "Failed to load item details", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun base64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun adjustFrameLayoutSize(base64String: String?) {
        if (base64String.isNullOrEmpty()) {
            frameLayout.layoutParams.height =
                resources.getDimensionPixelSize(R.dimen.fixed_frame_height)
            frameLayout.requestLayout()
            return
        }

        val bitmap = base64ToBitmap(base64String)

        frameLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val frameWidth = frameLayout.width
            if (frameWidth > 0 && bitmap.width > 0 && bitmap.height > 0) {
                val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
                val newHeight = (frameWidth * aspectRatio).toInt()

                frameLayout.layoutParams.height = min(
                    newHeight,
                    resources.getDimensionPixelSize(R.dimen.max_frame_height)
                )
                frameLayout.requestLayout()
            }
        }
    }
}