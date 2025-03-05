package com.example.foundlah

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AdminHomeActivity : ComponentActivity() {
    private lateinit var btnTrack: Button
    private lateinit var btnChat: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_admin_home)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnTrack = findViewById(R.id.btnTrackConvos)
        btnTrack.setOnClickListener(View.OnClickListener { v: View? -> trackConvos() })

        btnChat = findViewById(R.id.btnChat)
        btnChat.setOnClickListener(View.OnClickListener { v: View? -> trackChats() })
    }

    private fun trackConvos() {
        val intent = Intent(
            this,
            AdminConvoListActivity::class.java
        )
        startActivity(intent)
    }

    private fun trackChats() {
        val intent = Intent(
            this,
            ConvoListActivity::class.java
        )
        startActivity(intent)
    }
}