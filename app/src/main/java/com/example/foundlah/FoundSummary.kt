package com.example.foundlah
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.min
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FoundSummary : ComponentActivity() {
    private lateinit var database: DatabaseReference

    private lateinit var imagePreview: ImageView
    private lateinit var noImageText: TextView
    private lateinit var frameLayout: FrameLayout
    private lateinit var summaryTextView: TextView
    private lateinit var detailsContainer: View
    private lateinit var photoTextView: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_found_summary)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        summaryTextView = findViewById(R.id.summaryTextView)
        detailsContainer = findViewById(R.id.detailsContainer)
        photoTextView = findViewById(R.id.photoTextView)

        // Initialise realtime database
        database = FirebaseDatabase.getInstance("https://foundlah-31344-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        imagePreview = findViewById<ImageView>(R.id.imagePreview)
        noImageText = findViewById<TextView>(R.id.noImageText)
        frameLayout = findViewById(R.id.frameLayout2)

        val name = findViewById<TextView>(R.id.itemTextView)
        var category = findViewById<TextView>(R.id.categoryTextView)
        val date = findViewById<TextView>(R.id.dateTextView)
        val location = findViewById<TextView>(R.id.locationTextView)
        val description = findViewById<TextView>(R.id.descriptionTextView)

        val itemData: ItemData? = intent.getParcelableExtra("itemData")

        if (itemData != null) {
            name.text = "Item: ${itemData.name}"
            category.text = "Category: ${itemData.category}"
            date.text = "Date: ${itemData.date}"
            location.text = "Location: ${itemData.location}"
            description.text = "Description: ${itemData.description}"

            imagePreview = findViewById<ImageView>(R.id.imagePreview)
            if (!itemData?.imageBase64.isNullOrEmpty()) {
                val decodedBitmap = base64ToBitmap(itemData.imageBase64!!)
                imagePreview.setImageBitmap(decodedBitmap)
                noImageText.visibility = TextView.GONE
                adjustFrameLayoutSize(itemData.imageBase64)
            }
        }

        val backButton = findViewById<Button>(R.id.foundSumBackButton)
        val cancelButton = findViewById<Button>(R.id.foundSumCancelButton)
        val submitButton = findViewById<Button>(R.id.foundSumSubmitButton)

        backButton.setOnClickListener {
            finish()
        }

        cancelButton.setOnClickListener {
            Toast.makeText(this, "cancelled. Back to home page", Toast.LENGTH_SHORT).show()
        }

        submitButton.setOnClickListener {
            if (itemData != null) {
                uploadToFirebase(itemData)
            }
        }

        summaryTextView.alpha = 0f
        detailsContainer.alpha = 0f
        photoTextView.alpha = 0f
        frameLayout.alpha = 0f

        playFadeInAnimations()
    }

    private fun playFadeInAnimations() {
        fun fadeIn(view: View, delay: Long):ObjectAnimator {
            return ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
                duration = 1000
                startDelay = delay
            }
        }

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(
            fadeIn(summaryTextView, 500),
            fadeIn(detailsContainer, 1000),
            fadeIn(photoTextView, 1500),
            fadeIn(frameLayout, 2000)
        )
        animatorSet.start()
    }

    private fun uploadToFirebase(item: ItemData) {
        // Get userID
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Generate unique ID
        val itemId = database.child("found items").push().key

        val itemMap = mapOf(
            "userId" to userId,
            "name" to item.name,
            "category" to item.category,
            "date" to item.date,
            "location" to item.location,
            "description" to item.description,
            "imageBase64" to item.imageBase64,
            "type" to item.type
        )

        if (itemId != null) {
            database.child("found items").child(itemId).setValue(itemMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Form submitted successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun base64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun adjustFrameLayoutSize(base64String: String?) {
        if (base64String.isNullOrEmpty()) {
            frameLayout.layoutParams.height =
                resources.getDimensionPixelSize(R.dimen.fixed_frame_height)
            frameLayout.requestLayout()
            return
        }

        val bitmap = base64ToBitmap(base64String)

        frameLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val frameWidth = frameLayout.width // ✅ Ensure width is measured
            if (frameWidth > 0 && bitmap.width > 0 && bitmap.height > 0) {
                val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
                val newHeight = (frameWidth * aspectRatio).toInt()

                frameLayout.layoutParams.height = min(
                    newHeight,
                    resources.getDimensionPixelSize(R.dimen.max_frame_height)
                )
                frameLayout.requestLayout()
            }
        }
    }
}