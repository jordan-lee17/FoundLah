package com.example.foundlah

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.min
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LostSummary : ComponentActivity() {
    private lateinit var database: DatabaseReference

    private lateinit var imagePreview: ImageView
    private lateinit var noImageText: TextView
    private lateinit var frameLayout: FrameLayout

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lost_summary)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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

        val backButton = findViewById<Button>(R.id.lostSumBackButton)
        val cancelButton = findViewById<Button>(R.id.lostSumCancelButton)
        val submitButton = findViewById<Button>(R.id.lostSumSubmitButton)

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
    }

    private fun uploadToFirebase(item: ItemData) {
        // Generate unique ID
        val itemId = database.child("lost items").push().key

        if (itemId != null) {
            database.child("lost items").child(itemId).setValue(item)
                .addOnSuccessListener {
                    Toast.makeText(this, "Form submitted successfully!", Toast.LENGTH_SHORT).show()
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