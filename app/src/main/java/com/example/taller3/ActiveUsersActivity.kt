package com.example.taller3

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

class ActiveUsersActivity : AppCompatActivity()
{

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private lateinit var myRef: DatabaseReference
    private val storage = Firebase.storage
    private lateinit var availableUsers: ArrayList<User>
    private lateinit var profilePicsBitmaps: ArrayList<Bitmap>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_users)
        auth = Firebase.auth
        myRef = database.getReference(PATH_USERS)
        availableUsers = ArrayList<User>()
        profilePicsBitmaps = ArrayList<Bitmap>()

        val recViewUsers: RecyclerView = findViewById(R.id.availableUsersRecView)
        val recViewAdapter = ActiveUsersAdapter(this, availableUsers, profilePicsBitmaps)
        recViewUsers.adapter = recViewAdapter
        recViewUsers.layoutManager = LinearLayoutManager(this)



        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                GlobalScope.launch(Dispatchers.IO) {
                    availableUsers.clear()
                    profilePicsBitmaps.clear()
                    for (userSnapshot in dataSnapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        if (auth.currentUser != null && user != null && user.available && user.uid != null && auth.currentUser!!.uid != null && user.uid != auth.currentUser!!.uid) {
                            val imageRef = storage.reference.child("images/profile/${user.uid}/image.jpg")
                            try {
                                val imageUrl = imageRef.downloadUrl.await()
                                Log.d(TAG, "Download URL for user ${user.uid}: $imageUrl")
                                val bitmap = downloadImageFromUrl(imageUrl)
                                if (bitmap != null) {
                                    withContext(Dispatchers.Main) {
                                        availableUsers.add(user)
                                        profilePicsBitmaps.add(bitmap)
                                        recViewAdapter.notifyDataSetChanged()
                                    }
                                } else {
                                    Log.e(TAG, "Failed to download image for user ${user.uid}")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error downloading image for user ${user.uid}", e)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Database operation cancelled", databaseError.toException())
            }
        })



    }

    private suspend fun downloadImageFromUrl(url: Uri): Bitmap? {

    return withContext(Dispatchers.IO) {
        try {
            val inputStream = URL(url.toString()).openStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmap
        } catch (e: IOException) {
            Log.e(TAG, "Failed to download image from URL $url", e)
            null

        }
    }
}




}