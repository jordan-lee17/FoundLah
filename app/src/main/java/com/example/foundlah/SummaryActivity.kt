package com.example.foundlah

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.os.Bundle
import android.util.Log
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
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlin.math.abs
import kotlin.math.min
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Locale

class SummaryActivity : ComponentActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var imagePreview: ImageView
    private lateinit var noImageText: TextView
    private lateinit var frameLayout: FrameLayout
    private lateinit var formType: String
    private lateinit var summaryTextView: TextView
    private lateinit var detailsContainer: View
    private lateinit var photoTextView: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_summary)
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

        imagePreview = findViewById(R.id.imagePreview)
        noImageText = findViewById(R.id.noImageText)
        frameLayout = findViewById(R.id.frameLayout2)

        val name = findViewById<TextView>(R.id.itemTextView)
        var category = findViewById<TextView>(R.id.categoryTextView)
        val date = findViewById<TextView>(R.id.dateTextView)
        val location = findViewById<TextView>(R.id.locationTextView)
        val description = findViewById<TextView>(R.id.descriptionTextView)

        val itemData: ItemData? = intent.getParcelableExtra("itemData")

        if (itemData != null) {
            formType = itemData.type.toString()
            summaryTextView.text = "Summary of ${itemData.type} item"
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

        val backButton = findViewById<Button>(R.id.backButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)
        val submitButton = findViewById<Button>(R.id.submitButton)

        backButton.setOnClickListener {
            finish()
        }

        cancelButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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
        fun fadeIn(view: View, delay: Long): ObjectAnimator {
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
        val itemId = database.child("${formType} items").push().key

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
            database.child("${formType} items").child(itemId).setValue(itemMap)
                .addOnSuccessListener {
                    checkMatches(item, itemId) {
                        runOnUiThread {
                            Toast.makeText(this, "Form submitted successfully!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
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
            val frameWidth = frameLayout.width
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

    private fun checkMatches(submittedItem: ItemData, submittedId: String, onComplete: () -> Unit) {
        val oppositeFormType: String
        if (formType == "found") {
            oppositeFormType = "lost"
        } else {
            oppositeFormType = "found"
        }
        database.child("${oppositeFormType} items").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val matches = mutableListOf<Triple<String?, String, Int>>()

                for (itemSnapshot in snapshot.children) {
                    val matchItem = itemSnapshot.getValue(ItemData::class.java)
                    val matchUserId = itemSnapshot.child("userId").getValue(String::class.java)

                    if (matchItem != null) {
                        val matchScore = calculateMatchScore(submittedItem, matchItem)

                        if (matchScore > 60) {
                            matches.add(Triple(matchUserId, itemSnapshot.key!!, matchScore))
                        }
                    }
                }

                matches.sortByDescending { it.second }
                saveMatch(submittedId, matches)

                onComplete()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MatchCheck", "Database Error: ${error.message}")
                onComplete()
            }
        })
    }

    private fun calculateMatchScore(submittedItem: ItemData, item2: ItemData): Int {
        var score = 0

        // Category check
        if (submittedItem.category == item2.category){
            score += 20
        }
        // Location check
        if (submittedItem.location == item2.location) {
            score += 20
        } else if (submittedItem.location.orEmpty().contains(item2.location.orEmpty(), ignoreCase = true) || item2.location.orEmpty().contains(submittedItem.location.orEmpty(), ignoreCase = true)) {
            score += 10
        }
        val date1 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(submittedItem.date)
        val date2 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(item2.date)
        // Check difference in date reported
        if (date1 != null && date2 != null) {
            val daysDifference = abs((date1.time - date2.time) / (1000 * 60 * 60 * 24))

            // Same day report
            if (daysDifference == 0L) {
                score += 30
            } else if (daysDifference <= 3) { // 1-3 Days report
                score += 20
            } else if (daysDifference <=7) { // 4-7 days report
                score += 10
            }
        }
        // Similar name
        if (submittedItem.name.orEmpty().contains(item2.name.orEmpty(), ignoreCase = true) || item2.name.orEmpty().contains(submittedItem.name.orEmpty(), ignoreCase = true)) {
            score += 20
        }

        // Similar description
        if (submittedItem.description.orEmpty().contains(item2.description.orEmpty(), ignoreCase = true) || item2.description.orEmpty().contains(submittedItem.description.orEmpty(), ignoreCase = true)) {
            score += 10
        }

        return score
    }

    private fun saveMatch(submittedItemId: String, matches: MutableList<Triple<String?, String, Int>>) {
        val matchId = database.child("matches").push().key
        val userMatchCount = mutableMapOf<String, Int>()

        for ((userId, itemId, score) in matches) {
            val matchData = mapOf(
                "submittedItemId" to submittedItemId,
                "matchedItemId" to itemId,
                "matchedUserId" to userId,
                "score" to score
            )
            if (matchId != null) {
                database.child("matches").child(matchId).setValue(matchData)
            }
            // Count matches per user
            if (userId != null) {
                userMatchCount[userId] = userMatchCount.getOrDefault(userId, 0) + 1
            }
        }

        // Send a single notification per user
        for ((userId, matchCount) in userMatchCount) {
            sendNotification(userId, matchCount)
        }
    }

    private fun sendNotification(matchedUserId: String, matchCount: Int) {
        getFCMToken(matchedUserId) { fcmToken ->
            Log.d("FCM", "Token: ${fcmToken}")
            if (fcmToken != null) {
                val url = "https://fcm.googleapis.com/v1/projects/foundlah-31344/messages:send"

                val json = """
                {
                    "message": {
                        "token": "${fcmToken}",
                        "notification": {
                            "title": "New Matches Found!",
                            "body": "You have ${matchCount} new match(es) for your lost item!"
                        }
                    }
                }
            """.trimIndent()

                val request = object : StringRequest(Method.POST, url,
                    { response -> Log.d("FCM", "Notification sent successfully: ${response}") },
                    { error -> Log.d("FCM", "Error sending notification: ${error.message}") }
                ) {
                    override fun getHeaders(): Map<String, String> {
                        val token = getAccessToken()
                        return mapOf(
                            "Authorization" to "Bearer ${token}",
                            "Content-Type" to "application/json"
                        )
                    }

                    override fun getBody(): ByteArray {
                        return json.toByteArray()
                    }
                }

                Volley.newRequestQueue(this@SummaryActivity).add(request)
            } else {
                println("FCM Token not found for user: ${matchedUserId}")
            }
        }
    }

    private fun getFCMToken(userId: String, onTokenReceived: (String?) -> Unit) {
        database.child("users").child(userId).child("fcmToken")
            .get().addOnSuccessListener { snapshot ->
                val token = snapshot.getValue(String::class.java)
                onTokenReceived(token)
            }.addOnFailureListener {
                println("Failed to fetch FCM Token: ${it.message}")
                onTokenReceived(null)
            }
    }

    private fun getAccessToken(): String? {
        return try {
            val inputStream = assets.open("service-account.json")
            val credentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            credentials.refreshIfExpired()
            credentials.accessToken.tokenValue
        } catch (e: Exception) {
            Log.e("FCM", "Error getting access token", e)
            null
        }
    }

}