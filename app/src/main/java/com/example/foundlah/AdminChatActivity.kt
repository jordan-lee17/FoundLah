package com.example.foundlah

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class AdminChatActivity : AppCompatActivity() {
    private var db: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null
    private val nextButton: Button? = null
    private val backButton: Button? = null
    private lateinit var recyclerView: RecyclerView
    private var messageAdapter: MessageAdapter? = null
    private val messageList: MutableList<Message?> = ArrayList()
    private var chatroomID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_admin_chat)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.setLayoutManager(LinearLayoutManager(this))
        messageAdapter = MessageAdapter(messageList)
        recyclerView.setAdapter(messageAdapter)

        chatroomID = intent.getStringExtra("chatroomID")
        if (chatroomID == null) {
            Log.e("ChatActivity", "No chatroomID provided!")
            finish()
            return
        }
        loadMessages(chatroomID!!)
    }

    private fun loadMessages(chatroomID: String) {
        db!!.collection("chatrooms").document(chatroomID)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots: QuerySnapshot?, e: FirebaseFirestoreException? ->
                if (e != null) {
                    Log.e("ChatActivity", "Listen failed", e)
                    return@addSnapshotListener
                }
                messageList.clear()
                for (doc in snapshots!!.documents) {
                    val message = doc.toObject(
                        Message::class.java
                    )
                    messageList.add(message)
                }

                messageAdapter!!.notifyDataSetChanged()
                recyclerView!!.scrollToPosition(messageList.size - 1)
            }
    }

    private fun nextPage() {
    }
}