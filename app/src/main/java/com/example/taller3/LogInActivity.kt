package com.example.taller3

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LogInActivity : AppCompatActivity()
{
    private lateinit var auth: FirebaseAuth

    override fun onStart()
    {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        auth = Firebase.auth



        val txtEmailIn: EditText = findViewById(R.id.editTextEmailAddress)
        val txtPassIn: EditText = findViewById(R.id.editTextPassword)
        val btnLogIn: Button = findViewById(R.id.loginBtn)
        val txtBtnSignUp: TextView = findViewById(R.id.registerTxtBtn)
        listen(txtEmailIn,txtPassIn, btnLogIn, txtBtnSignUp)
    }

    private fun listen(pTxtEmailIn: EditText, pTxtPasswordIn: EditText, pBtnLogin : Button, pTxtBtnSignUp : TextView)
    {
        pBtnLogin.setOnClickListener {
            signInUser(pTxtEmailIn, pTxtPasswordIn)
        }

        pTxtBtnSignUp.setOnClickListener {
            val intentSignUp = Intent(this, SignUpActivity::class.java)

            startActivity(intentSignUp)
        }
    }

    private fun signInUser(pTxtEmailIn: EditText, pTxtPasswordIn: EditText)
    {
        if(validateForm(pTxtEmailIn, pTxtPasswordIn))
        {
            val email = pTxtEmailIn.text.toString()
            val password = pTxtPasswordIn.text.toString()

            auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful)
                    {
                        // Sign in success, update UI
                        Log.d(TAG, "signInWithEmail:success:")
                        val user = auth.currentUser
                        updateUI(auth.currentUser)
                    }
                    else
                    {
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(this, "Credenciales Invalidas", Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
        }
    }

    private fun validateForm(pTxtEmailIn: EditText, pTxtPasswordIn: EditText): Boolean
    {
        var valid = true
        val email = pTxtEmailIn.text.toString()
        if(TextUtils.isEmpty(email))
        {
            pTxtEmailIn.error = "Requerido"
            valid = false
        }
        else
        {
            if(isEmailValid(email))
            {
                pTxtEmailIn.error = null
            }
            else
            {
                pTxtEmailIn.error = "Email Invalido"
                valid = false
            }

        }
        val pass = pTxtPasswordIn.text.toString()
        if(TextUtils.isEmpty(pass))
        {
            pTxtPasswordIn.error = "Requerido"
            valid = false
        }
        else
        {
            pTxtPasswordIn.error = null
        }
        return valid
    }

    private fun isEmailValid(pEmail : String): Boolean
    {
        if(!pEmail.contains("@")||!pEmail.contains(".")||pEmail.length < 5)
        {
            return false
        }
        return true
    }

    private fun updateUI(currentUser: FirebaseUser?)
    {
        if (currentUser != null) {
            val intent = Intent(this, MainMenuActivity::class.java)
            intent.putExtra("user", currentUser.email)
            startActivity(intent)
        }
    }

    override fun onBackPressed()
    {
        // do nothing
    }
}