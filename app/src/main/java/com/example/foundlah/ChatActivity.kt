package com.example.foundlah

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class ChatActivity : AppCompatActivity() {
    private var db: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null
    private var messageInput: EditText? = null
    private lateinit var sendButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private val messageList: MutableList<Message?> = ArrayList()
    private var chatroomID: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
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

        sendButton.setOnClickListener(View.OnClickListener { v: View? -> sendMessage() })
        loadMessages(chatroomID!!)

        val backButton = findViewById<Button>(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }
    }


    private fun sendMessage() {
        val text = messageInput!!.text.toString()

        if (!text.isEmpty()) {
            val currentTime = System.currentTimeMillis()
            val message = Message(
                text, auth!!.currentUser!!
                    .email, currentTime
            )

            Log.d("ChatActivity", "Current time: $currentTime")

            db!!.collection("chatrooms").document(chatroomID!!)
                .collection("messages")
                .add(message)
                .addOnSuccessListener { documentReference: DocumentReference? ->
                    Log.d(
                        "ChatActivity",
                        "Message sent"
                    )
                }
                .addOnFailureListener((OnFailureListener { e: Exception? ->
                    Log.e(
                        "ChatActivity",
                        "Error sending message",
                        e
                    )
                }))

            messageInput!!.setText("")
        }
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
}