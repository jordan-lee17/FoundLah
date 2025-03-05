package com.example.foundlah

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.Arrays

class ConvoListActivity : ComponentActivity() {
    private lateinit var recyclerView: RecyclerView
    private var userAdapter: UserAdapter? = null
    private val userList: MutableList<User> = ArrayList()
    private var auth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_convo_list)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.setLayoutManager(LinearLayoutManager(this))

        auth = FirebaseAuth.getInstance()

        userAdapter = UserAdapter(userList,
            UserAdapter.OnUserClickListener { user: User? ->
                if (user != null) {
                    openChat(
                        user.email!!
                    )
                } // Open chat on user click
            })

        recyclerView.setAdapter(userAdapter)

        loadUsers()
    }

    private fun loadUsers() {
        val currentUserEmail = auth!!.currentUser!!.email

        // Get users from Firestore
        FirebaseFirestore.getInstance().collection("users").get()
            .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                userList.clear()
                for (doc in queryDocumentSnapshots.documents) {
                    val user =
                        doc.toObject(User::class.java)
                    if (user != null && user.email != currentUserEmail) {
                        userList.add(user) // Exclude current user
                    }
                }
                userAdapter!!.notifyDataSetChanged()
            }
            .addOnFailureListener { e: Exception? ->
                Log.e(
                    "UserList",
                    "Error loading users",
                    e
                )
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

    private fun openChat(otherUserEmail: String) {
        val currentUserEmail = auth!!.currentUser!!.email
        val chatroomID = generateChatroomID(currentUserEmail!!, otherUserEmail)

        val chatroomRef = db!!.collection("chatrooms").document(chatroomID)

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
                    Arrays.asList(currentUserEmail, otherUserEmail)
                chatroomRef.set(chatroom).addOnSuccessListener { aVoid: Void? ->
                    Log.d("Chat", "New room created: $chatroomID")
                    openChatActivity(chatroomID)
                }
            }
        }
    }
}