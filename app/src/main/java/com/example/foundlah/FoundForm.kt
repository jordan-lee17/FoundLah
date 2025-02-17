package com.example.foundlah

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min

class FoundForm : ComponentActivity() {
    private lateinit var uploadImageButton: Button
    private lateinit var openCameraButton: Button
    private lateinit var imagePreview: ImageView
    private lateinit var noImageText: TextView
    private var imageUri: Uri? = null
    private lateinit var frameLayout: FrameLayout

    private lateinit var itemName: EditText
    private lateinit var spinner: Spinner
    private lateinit var date: EditText
    private lateinit var location: EditText
    private lateinit var description: EditText

    private val IMAGE_PICK_CODE = 1000
    private val CAMERA_CAPTURE_CODE = 1001
    private val CAMERA_PERMISSION_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_found_desc)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        spinner = findViewById<Spinner>(R.id.spinner)
        val cancelButton = findViewById<Button>(R.id.foundDescCancelButton)
        val nextButton = findViewById<Button>(R.id.foundDescNextButton)
        uploadImageButton = findViewById<Button>(R.id.uploadImageButton)
        openCameraButton = findViewById<Button>(R.id.openCameraButton)
        imagePreview = findViewById<ImageView>(R.id.imagePreview)
        noImageText = findViewById<TextView>(R.id.noImageText)
        frameLayout = findViewById(R.id.frameLayout2)

        itemName = findViewById(R.id.foundItemName)
        date = findViewById(R.id.foundDate)
        location = findViewById(R.id.foundItemLocation)
        description = findViewById(R.id.foundItemDescription)

        // List of categories
        val categories = arrayOf("Category", "Phone", "Bottle", "Earpiece", "Charger", "Others")
        val arrayAdapter = object :
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categories) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)

                // Gray out first option
                if (position == 0) {
                    (view as TextView).setTextColor(resources.getColor(android.R.color.darker_gray))
                } else {
                    (view as TextView).setTextColor(resources.getColor(android.R.color.black))
                }
                return view
            }
        }

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = arrayAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    (view as TextView).setTextColor(resources.getColor(android.R.color.darker_gray))
                } else {
                    if (parent != null) {
                        (view as TextView).setTextColor(resources.getColor(android.R.color.black))
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        cancelButton.setOnClickListener {
            Toast.makeText(this, "cancelled. Back to home page", Toast.LENGTH_SHORT).show()
        }

        nextButton.setOnClickListener {
            var selectedCategory = spinner.selectedItem.toString()

            if (selectedCategory == "Category") {
                selectedCategory = ""
            }

            // Convert image to base64
            var base64Image: String? = null
            if (imageUri != null) {
                val bitmap = uriToBitmap(imageUri!!)
                base64Image = bitmapToBase64(bitmap)
            }

            // Pass form data to summary page
            val itemData = ItemData(
                itemName.text.toString(),
                selectedCategory,
                date.text.toString(),
                location.text.toString(),
                description.text.toString(),
                base64Image,
                "found"
            )

            val intent = Intent(this, FoundSummary::class.java)
            intent.putExtra("itemData", itemData)
            startActivity(intent)
        }

        uploadImageButton.setOnClickListener {
            pickImageFromGallery()
        }

        openCameraButton.setOnClickListener {
            openCamera()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("itemName", itemName.text.toString())
        outState.putString("category", spinner.selectedItem.toString())
        outState.putString("date", date.text.toString())
        outState.putString("location", location.text.toString())
        outState.putString("description", description.text.toString())
        outState.putString("imageUri", imageUri?.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        itemName.setText(savedInstanceState.getString("itemName"))

        val category = savedInstanceState.getString("category")
        val position = (spinner.adapter as ArrayAdapter<String>).getPosition(category)
        spinner.setSelection(position)

        date.setText(savedInstanceState.getString("date"))
        location.setText(savedInstanceState.getString("location"))
        description.setText(savedInstanceState.getString("description"))

        val savedImageUri = savedInstanceState.getString("imageUri")
        if (!savedImageUri.isNullOrEmpty()) {
            // Convert string back to URI
            imageUri = Uri.parse(savedImageUri)
            imagePreview.setImageURI(imageUri)
            noImageText.visibility = View.GONE
            // Adjust frame layout size
            adjustFrameLayoutSize()
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        return bitmap
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun openCamera() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED -> {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_CAPTURE_CODE)
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            }

            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            }
        }
    }

    private fun saveImage(bitmap: Bitmap): Uri? {
        val filesDir = applicationContext.filesDir
        val imageFile = File(filesDir, "captured_image.jpg")
        return try {
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Uri.fromFile(imageFile)
        } catch (e: IOException) {
            e.printStackTrace()
            null
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IMAGE_PICK_CODE -> {
                    imageUri = data?.data
                    if (imageUri != null) {
                        imagePreview.setImageURI(imageUri)
                        noImageText.visibility = View.GONE
                        adjustFrameLayoutSize()
                    }
                }

                CAMERA_CAPTURE_CODE -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    if (bitmap != null) {
                        imageUri = saveImage(bitmap)
                        imagePreview.setImageBitmap(bitmap)
                        noImageText.visibility = View.GONE
                        adjustFrameLayoutSize()
                    }
                }
            }
        }
    }
}
