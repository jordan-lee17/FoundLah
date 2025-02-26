package com.example.foundlah

import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.airbnb.lottie.LottieAnimationView

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Find Views
        val lottieAnimation = findViewById<LottieAnimationView>(R.id.lottieAnimation)
        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val lostButton = findViewById<Button>(R.id.lostButton)
        val foundButton = findViewById<Button>(R.id.foundButton)
        val ongoingButton = findViewById<Button>(R.id.ongoingButton)

        // Get User's Name (Optional)
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: "User"
        welcomeText.text = "Welcome $userEmail!\nHow can I help you today?"

        // **Ensure buttons are visible but transparent (so they remain clickable)**
        welcomeText.alpha = 0f
        welcomeText.visibility = View.VISIBLE

        lostButton.alpha = 0f
        lostButton.visibility = View.VISIBLE  // ✅ This ensures button exists & is clickable

        foundButton.alpha = 0f
        foundButton.visibility = View.VISIBLE  // ✅ Ensures it is there

        ongoingButton.alpha = 0f
        ongoingButton.visibility = View.VISIBLE  // ✅ Ensures it is there

        // Play Lottie Animation
        lottieAnimation.playAnimation()

        // Fade-in animation function
        fun fadeInView(view: View, startDelay: Long): ObjectAnimator {
            return ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
                duration = 800  // Smooth fade-in
                this.startDelay = startDelay  // Properly apply the delay
            }
        }

        // **Create a smooth animation sequence**
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(
            fadeInView(welcomeText, 500),     // Welcome text appears first
            fadeInView(lostButton, 500),      // Lost button appears after text
            fadeInView(foundButton, 500),     // Found button appears after Lost
            fadeInView(ongoingButton, 500)    // Ongoing button appears last
        )
        animatorSet.start()

        // **Ensure Buttons Are Clickable**
        lostButton.setOnClickListener {
            startActivity(Intent(this, LostForm::class.java))
        }

        foundButton.setOnClickListener {
            startActivity(Intent(this, FoundForm::class.java))
        }

        ongoingButton.setOnClickListener {
            startActivity(Intent(this, OngoingActivity::class.java))
        }
    }
}