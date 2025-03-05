package com.example.foundlah

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Arrays

class MatchedItemDetailsActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    @SuppressLint("MissingInflatedId")
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
        val matchedUserId = intent.getStringExtra("matchedUserId")
        val submittedUserId = intent.getStringExtra("submittedUserId")

        if (itemData != null) {
            name.text = "Item: ${itemData.name}"
            category.text = "Category: ${itemData.category}"
            date.text = "Date: ${itemData.date}"
            location.text = "Location: ${itemData.location}"
            description.text = "Description: ${itemData.description}"
        }

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            backButton.setBackgroundColor(Color.GRAY)
            backButton.text = "Going Back .."
            finish()
        }
        val chatButton = findViewById<Button>(R.id.chatButton)
        chatButton.setOnClickListener {
            getUserEmail(matchedUserId) { matchedUseremail ->
                if (matchedUseremail != null) {
                    getUserEmail(submittedUserId) { SubmittedUseremail ->
                        if (SubmittedUseremail != null) {
                            openChat(SubmittedUseremail, matchedUseremail)
                        }
                    }
                }
            }
            Toast.makeText(this, "Opening chat with user..", Toast.LENGTH_SHORT).show()
            chatButton.setBackgroundColor(Color.GRAY)

            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getUserEmail(userId: String?, onEmailReceived: (String?) -> Unit) {
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val email = document.getString("email")
                        // Return email
                        onEmailReceived(email)
                    } else {
                        Log.e("Firestore", "No such document")
                        onEmailReceived(null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching email", e)
                    onEmailReceived(null)
                }
        }
    }

    private fun generateChatroomID(user1: String, user2: String): String {
        return if (user1.compareTo(user2) < 0) {
            user1 + "_" + user2
        } else {
            user2 + "_" + user1
        }
    }

    private fun openChatActivity(chatroomID: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("chatroomID", chatroomID)
        startActivity(intent)
    }

    private fun openChat(matchedUserId: String, submittedUserId: String) {
        val chatroomID = generateChatroomID(submittedUserId, matchedUserId)

        val chatroomRef = db.collection("chatrooms").document(chatroomID)

        chatroomRef.get().addOnCompleteListener { task: Task<DocumentSnapshot?> ->
            if (task.isSuccessful && task.result != null && task.result!!
                    .exists()
            ) {
                Log.d("Chat", "Chatroom exists: $chatroomID")
                openChatActivity(chatroomID)
            } else {
                val chatroom: MutableMap<String, Any> =
                    HashMap()
                chatroom["users"] =
                    Arrays.asList(submittedUserId, matchedUserId)
                chatroomRef.set(chatroom).addOnSuccessListener { aVoid: Void? ->
                    Log.d("Chat", "New room created: $chatroomID")
                    openChatActivity(chatroomID)
                }
            }
        }
    }
}