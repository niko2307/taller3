package com.example.taller3

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage

const val PATH_USERS="users/"

class SignUpActivity : AppCompatActivity()
{


    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private lateinit var myRef: DatabaseReference
    private val storage = Firebase.storage
    private var uri : Uri = Uri.EMPTY // Inicializar con un valor predeterminado


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        auth = Firebase.auth
        val profilePicImg: ImageView = findViewById(R.id.profilePicImg)
        val profilePicBtn: FloatingActionButton = findViewById(R.id.profilePicBtn)
        val txtEmailIn: EditText = findViewById(R.id.editTextEmailAddress)
        val txtNameIn: EditText = findViewById(R.id.editTextName)
        val txtLastNameIn: EditText = findViewById(R.id.editTextLastName)
        val txtPassIn: EditText = findViewById(R.id.editTextPassword)
        val txtIdIn: EditText = findViewById(R.id.editTextId)
        val signUpBtn: Button = findViewById(R.id.signUpBtn)
        listen(profilePicImg, profilePicBtn, txtEmailIn, txtNameIn, txtLastNameIn, txtPassIn, txtIdIn, signUpBtn)


    }

    private fun listen(pProfilePicImg: ImageView, pProfilePicBtn: FloatingActionButton, pTxtEmailIn: EditText, pTxtNameIn: EditText, pTxtLastNameIn: EditText, pTxtPassIn: EditText, pTxtIdIn: EditText, pSignUpBtn: Button)
    {

        pProfilePicBtn.setOnClickListener {
            ImagePicker.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }
        pSignUpBtn.setOnClickListener {
           // Toast.makeText(baseContext, "Presionado", Toast.LENGTH_LONG).show()

            auth.createUserWithEmailAndPassword(pTxtEmailIn.text.toString(), pTxtPassIn.text.toString()).addOnCompleteListener(this){ task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful)
                    val user = auth.currentUser
                    if (user != null)
                    {
                    // Update user info
                        val myUser = User()
                        myUser.name = pTxtNameIn.text.toString()
                        myUser.lastName = pTxtLastNameIn.text.toString()
                        myUser.id = pTxtIdIn.text.toString()
                        myUser.available = true
                        myUser.uid = user.uid
                        myRef = database.getReference(PATH_USERS+auth.currentUser!!.uid)
                        myRef.setValue(myUser)
                        val upcrb = UserProfileChangeRequest.Builder()
                        upcrb.displayName = pTxtNameIn.text.toString() + " " + pTxtLastNameIn.text.toString()
                        user.updateProfile(upcrb.build())
                        uploadFile(uri)
                        updateUI(user)
                    }
                } else {
                    Toast.makeText(this, "createUserWithEmail:Failure: " + task.exception.toString(),
                        Toast.LENGTH_SHORT).show()
                    task.exception?.message?.let { Log.e(TAG, it) }
                }

            }
        }
    }

    private fun uploadFile(uri: Uri) {
        if (uri != Uri.EMPTY) {
            val imageRef = storage.reference.child("images/profile/"+ auth.currentUser!!.uid +"/image.jpg")
            imageRef.putFile(uri)
                .addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {
                    override fun onSuccess(taskSnapshot: UploadTask.TaskSnapshot) {
                        // Get a URL to the uploaded content
                        Log.i("FBApp", "Successfully uploaded image")
                    }
                })
                .addOnFailureListener(object : OnFailureListener {
                    override fun onFailure(exception: Exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                })
        } else {
            Log.e(TAG, "URI is empty")
        }
    }


    private fun updateUI(currentUser: FirebaseUser?)
    {
        if (currentUser != null) {
            val intent = Intent(this, MainMenuActivity::class.java)
            intent.putExtra("user", currentUser.email)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
        {
            //Image Uri will not be null for RESULT_OK
            uri = data?.data!!

            // Use Uri object instead of File to avoid storage permissions
            val profilePicImg: ImageView = findViewById(R.id.profilePicImg)
            profilePicImg.setImageURI(uri)
        }
        else if (resultCode == ImagePicker.RESULT_ERROR)
        {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        }
        else
        {
            Toast.makeText(this, "Imagen no seleccionada", Toast.LENGTH_SHORT).show()
        }
    }
}