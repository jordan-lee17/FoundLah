package com.example.foundlah

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
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

class SummaryPage : ComponentActivity() {
    private lateinit var imagePreview: ImageView
    private lateinit var noImageText: TextView
    private var imageUri: Uri? = null
    private lateinit var frameLayout: FrameLayout

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_summary_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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
            if (!itemData.imageUri.isNullOrEmpty()) {
                imageUri = Uri.parse(itemData.imageUri)
                noImageText = findViewById<TextView>(R.id.noImageText)
                imagePreview.setImageURI(imageUri)
                noImageText.visibility = TextView.GONE
                // Adjust frame layout size
                adjustFrameLayoutSize()
            }
        }

        val backButton = findViewById<Button>(R.id.backButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)
        val submitButton = findViewById<Button>(R.id.submitButton)

        backButton.setOnClickListener {
            finish()
        }

        cancelButton.setOnClickListener {
            Toast.makeText(this, "cancelled. Back to home page", Toast.LENGTH_SHORT).show()

        }

        submitButton.setOnClickListener {
            Toast.makeText(this, "submit.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun adjustFrameLayoutSize() {
        imagePreview.viewTreeObserver.addOnGlobalLayoutListener {
            if (imageUri != null) {
                // Load image dimensions
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                val inputStream = contentResolver.openInputStream(imageUri!!)
                BitmapFactory.decodeStream(inputStream, null, options)
                inputStream?.close()

                val imageWidth = options.outWidth
                val imageHeight = options.outHeight

                if (imageWidth > 0 && imageHeight > 0) {
                    // Get aspect ratio from image
                    val aspectRatio = imageHeight.toFloat() / imageWidth.toFloat()
                    // Adjust height
                    val newHeight = (frameLayout.width * aspectRatio).toInt()

                    frameLayout.layoutParams.height = min(
                        newHeight,
                        resources.getDimensionPixelSize(R.dimen.max_frame_height)
                    )

                }
            } else {
                // Keep fixed height when no image
                frameLayout.layoutParams.height = resources.getDimensionPixelSize(R.dimen.fixed_frame_height)
            }
            // Apply height
            frameLayout.requestLayout()
        }
    }
}