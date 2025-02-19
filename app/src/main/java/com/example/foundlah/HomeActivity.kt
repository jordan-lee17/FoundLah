package com.example.foundlah

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val lostButton = findViewById<Button>(R.id.lostButton)
        val foundButton = findViewById<Button>(R.id.foundButton)
        val ongoingButton = findViewById<Button>(R.id.ongoingButton)

        lostButton.setOnClickListener {
            val intent = Intent(this, LostForm::class.java)
            startActivity(intent)
        }

        foundButton.setOnClickListener {
            val intent = Intent(this, FoundForm::class.java)
            startActivity(intent)
        }

        ongoingButton.setOnClickListener {
            val intent = Intent(this, OngoingActivity::class.java)
            startActivity(intent)
        }
    }
}