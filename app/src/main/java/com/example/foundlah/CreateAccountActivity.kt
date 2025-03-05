package com.example.foundlah

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class CreateAccountActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var  emailField: EditText
    private lateinit var  passwordField: EditText
    private lateinit var  confirmPasswordField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val backText = findViewById<TextView>(R.id.textBack)
        emailField = findViewById<EditText>(R.id.editTextTextEmailAddress)
        passwordField = findViewById<EditText>(R.id.editTextTextPassword)
        confirmPasswordField = findViewById<EditText>(R.id.confirmPassword)
        val createAccountButton = findViewById<Button>(R.id.button)

        auth = FirebaseAuth.getInstance()

        createAccountButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()

            if(validateForm(email, password, confirmPassword)) {
                registerUser(email, password)
            }
        }

        backText.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    private fun validateForm(email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        if(email.isEmpty()) {
            emailField.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.error = "Invalid email format"
            isValid = false
        }

        if(password.isEmpty() || password.length < 6) {
            passwordField.error = "Password must be at least 6 characters"
            isValid = false
        }

        if(confirmPassword != password) {
            confirmPasswordField.error = "Password does not match"
            isValid = false
        }

        return isValid
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = auth.currentUser
                    user?.let {
                        val db = FirebaseFirestore.getInstance()
                        val userData = hashMapOf(
                            "email" to email,
                            "role" to "user"
                        )
                        db.collection("users").document(user.uid).set(userData)
                            .addOnSuccessListener {
                                Log.d("Register", "User data stored")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Register", "Error storing user data", e)
                            }
                    }
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, Login::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}