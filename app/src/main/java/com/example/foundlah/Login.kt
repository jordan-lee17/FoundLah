package com.example.foundlah

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.activity.ComponentActivity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
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
        val loginButton = findViewById<Button>(R.id.btnLogin)
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
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser
                    if (user != null) {
                        storeFCMToken()
                        checkUserRole(user.uid, email)
                    }
                } else {
                    Toast.makeText(this, "Invalid login details", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserRole(userId: String, email: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get()
            .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                if (documentSnapshot.exists()) {
                    val role = documentSnapshot.getString("role")
                    if (role == "admin") {
                        startActivity(Intent(this, AdminHomeActivity::class.java))
                    } else {
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this, "User role not found!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e: Exception? ->
                Toast.makeText(this, "Failed to fetch user role", Toast.LENGTH_SHORT).show()
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

            // Store token under userId in Firestore Database
            val database = FirebaseFirestore.getInstance()
            val userRef = database.collection("users").document(userId)
            userRef.update("fcmToken", token)
                .addOnSuccessListener { println("Token saved successfully in Firestore") }
                .addOnFailureListener {
                    println("Failed to save token: ${it.message}")

                    // If the document doesn't exist, create it with the token
                    userRef.set(mapOf("fcmToken" to token))
                        .addOnSuccessListener { println("Token document created in Firestore") }
                        .addOnFailureListener { println("Failed to create token document: ${it.message}") }
                }
        }
    }
}