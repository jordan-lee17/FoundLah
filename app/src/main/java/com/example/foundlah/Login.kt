package com.example.foundlah

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class Login : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()

        emailField = findViewById(R.id.editTextTextEmailAddress)
        passwordField = findViewById(R.id.editTextTextPassword)
        val loginButton = findViewById<Button>(R.id.button)
        val createAccount = findViewById<TextView>(R.id.textCreateAccount)

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if(validateForm(email, password)) {
                loginUser(email, password)
            }
        }

        createAccount.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        var isValid = true

        if(email.isEmpty()) {
            emailField.error = "Please enter email"
            isValid = false
        }

        if(password.isEmpty()) {
            passwordField.error = "Please enter password"
            isValid = false
        }

        return isValid
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storeFCMToken()
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Invalid login details", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun storeFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("FCM Token retrieval failed: ${task.exception}")
                return@addOnCompleteListener
            }

            val token = task.result
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnCompleteListener

            // Store token under userId in Firebase Realtime Database
            val database = FirebaseDatabase.getInstance("https://foundlah-31344-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
            database.child("users").child(userId).child("fcmToken").setValue(token)
                .addOnSuccessListener { println("Token saved successfully") }
                .addOnFailureListener { println("Failed to save token: ${it.message}") }
        }
    }

}