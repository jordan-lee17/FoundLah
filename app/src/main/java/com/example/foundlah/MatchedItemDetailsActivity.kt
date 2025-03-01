package com.example.foundlah

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MatchedItemDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_matched_item_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val name = findViewById<TextView>(R.id.itemTextView)
        var category = findViewById<TextView>(R.id.categoryTextView)
        val date = findViewById<TextView>(R.id.dateTextView)
        val location = findViewById<TextView>(R.id.locationTextView)
        val description = findViewById<TextView>(R.id.descriptionTextView)

        val itemData: ItemData? = intent.getParcelableExtra("itemData")

        if (itemData != null) {
            name.text = "Item: ${itemData.name}"
            category.text = "Category: ${itemData.category}"
            date.text = "Date: ${itemData.date}"
            location.text = "Location: ${itemData.location}"
            description.text = "Description: ${itemData.description}"
        }

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val chatButton = findViewById<Button>(R.id.button3)
        chatButton.setOnClickListener {
            Toast.makeText(this, "Opening chat with user..", Toast.LENGTH_SHORT).show()
            chatButton.setBackgroundColor(Color.GRAY)

            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
    }
}