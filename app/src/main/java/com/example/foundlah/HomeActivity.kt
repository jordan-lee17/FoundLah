package com.example.foundlah

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView

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

        // Find Views
        val lottieAnimation = findViewById<LottieAnimationView>(R.id.lottieAnimation)
        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val lostButton = findViewById<Button>(R.id.lostButton)
        val foundButton = findViewById<Button>(R.id.foundButton)
        val ongoingButton = findViewById<Button>(R.id.ongoingButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        val profileButton = findViewById<Button>(R.id.profileButton)

        // Get User's Name (Optional)
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: "User"
        welcomeText.text = "Welcome $userEmail!\nHow can I help you today?"

        // **Ensure buttons are visible but transparent (so they remain clickable)**
        welcomeText.alpha = 0f
        welcomeText.visibility = View.VISIBLE

        // This ensures button exists & is clickable
        lostButton.alpha = 0f
        lostButton.visibility = View.VISIBLE

        // Ensures it is there
        foundButton.alpha = 0f
        foundButton.visibility = View.VISIBLE


        ongoingButton.alpha = 0f
        ongoingButton.visibility = View.VISIBLE

        // Play Lottie Animation
        lottieAnimation.playAnimation()

        // Fade-in animation function
        fun fadeInView(view: View, startDelay: Long): ObjectAnimator {
            return ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
                // Smooth fade-in
                duration = 800
                // Properly apply the delay
                this.startDelay = startDelay
            }
        }

        // **Create a smooth animation sequence**
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(
            fadeInView(welcomeText, 200),     // Welcome text appears first
            fadeInView(lostButton, 200),      // Lost button appears after text
            fadeInView(foundButton, 200),     // Found button appears after Lost
            fadeInView(ongoingButton, 200)    // Ongoing button appears last
        )
        animatorSet.start()

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

        settingsButton.setOnClickListener {
            Toast.makeText(this, "Settings button clicked", Toast.LENGTH_SHORT).show()
        }

        profileButton.setOnClickListener {
            Toast.makeText(this, "Profile button clicked", Toast.LENGTH_SHORT).show()
        }
    }
}