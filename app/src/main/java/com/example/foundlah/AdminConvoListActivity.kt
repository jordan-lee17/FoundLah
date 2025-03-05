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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class AdminConvoListActivity : ComponentActivity() {
    private lateinit var recyclerView: RecyclerView
    private val convoList: MutableList<Convo> = ArrayList()
    private var convoAdapter: ConvoAdapter? = null
    private var auth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_admin_convo_list)
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

        convoAdapter = ConvoAdapter(convoList,
            ConvoAdapter.OnUserClickListener { convo: Convo? ->
                if (convo != null) {
                    openChat(convo.chatroomID)
                }
            })

        recyclerView.setAdapter(convoAdapter)
        loadConvos()
    }

    private fun loadConvos() {
        FirebaseFirestore.getInstance().collection("chatrooms").get()
            .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                convoList.clear()
                for (doc in queryDocumentSnapshots.documents) {
                    Log.d("FirestoreData", "Raw Data: " + doc.data)
                    val users = doc["users"] as List<String?>?
                    val convo = Convo()
                    convo.setUsers(users)
                    convoList.add(convo)
                }
                Log.d("AdminConvoList", "ConvoList count:" + convoList.size)
                convoAdapter!!.notifyDataSetChanged()
            }.addOnFailureListener { e: Exception? ->
                Log.e(
                    "AdminConvoList",
                    "Error loading convos",
                    e
                )
            }
    }

    private fun openChat(chatroomID: String?) {
        val intent = Intent(
            this,
            AdminChatActivity::class.java
        )
        intent.putExtra("chatroomID", chatroomID)
        startActivity(intent)
    }
}