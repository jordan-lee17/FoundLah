//package com.example.foundlah
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.View
//import android.widget.Button
//import android.widget.EditText
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.enableEdgeToEdge
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.google.android.gms.tasks.Task
//import com.google.firebase.auth.AuthResult
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.DocumentSnapshot
//import com.google.firebase.firestore.FirebaseFirestore
//
//class LoginActivity : ComponentActivity() {
//    private var mAuth: FirebaseAuth? = null
//    private var emailField: EditText? = null
//    private var passwordField: EditText? = null
//    private var btnLogin: Button? = null
//    private val btnRegister: Button? = null
//    private val btnTest: Button? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        this.enableEdgeToEdge()
//        setContentView(R.layout.activity_login)
//        ViewCompat.setOnApplyWindowInsetsListener(
//            findViewById(R.id.main)
//        ) { v: View, insets: WindowInsetsCompat ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        mAuth = FirebaseAuth.getInstance()
//        emailField = findViewById(R.id.editTextTextEmailAddress)
//        passwordField = findViewById(R.id.editTextTextPassword)
//        btnLogin = findViewById(R.id.btnLogin)
//
//        //btnRegister = findViewById(R.id.register);
//        //btnTest = findViewById(R.id.btnTest);
//        btnLogin.setOnClickListener(View.OnClickListener { v: View? -> loginUser() })
//        //btnRegister.setOnClickListener(v -> registerUser());
//        //btnTest.setOnClickListener(v -> test());
//    }
//
//    private fun loginUser() {
//        val email = emailField!!.text.toString()
//        val password = passwordField!!.text.toString()
//
//        mAuth!!.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener(
//                this
//            ) { task: Task<AuthResult?> ->
//                if (task.isSuccessful) {
//                    val user = mAuth!!.currentUser
//                    if (user != null) {
//                        checkUserRole(user.uid)
//                    }
//                } else {
//                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            }
//    }
//
//    private fun checkUserRole(userId: String) {
//        val db = FirebaseFirestore.getInstance()
//        db.collection("users").document(userId).get()
//            .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
//                if (documentSnapshot.exists()) {
//                    val role = documentSnapshot.getString("role")
//                    startActivity(
//                        Intent(
//                            this,
//                            AdminHomeActivity::class.java
//                        )
//                    )
//                    if ("admin" == role) {
//                        startActivity(
//                            Intent(
//                                this,
//                                AdminHomeActivity::class.java
//                            )
//                        )
//                    } else {
//                        startActivity(
//                            Intent(
//                                this,
//                                UserHomeActivity::class.java
//                            )
//                        ) // Regular user screen
//                    }
//                } else {
//                    Toast.makeText(this, "User role not found!", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            }
//            .addOnFailureListener { e: Exception? ->
//                Toast.makeText(
//                    this,
//                    "Failed to fetch user role",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//    }
//}