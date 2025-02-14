package com.example.foundlah

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FollowupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_followup)

        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener{
            finish()
        }

        val acceptButton: Button = findViewById(R.id.buttonAccept)
        acceptButton.setOnClickListener{
            val intent = Intent(this, AdminSummaryActivity::class.java)
            startActivity(intent)
        }

        val rejectButton: Button = findViewById(R.id.buttonReject)
        rejectButton.setOnClickListener{
            val intent = Intent(this, AdminSummaryActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}