package com.example.taller3

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class AvailableChangeService(private val context: Context) {

    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")
    private lateinit var auth: FirebaseAuth

    private var availableUsers: MutableList<User> = mutableListOf()
    private val listeners: MutableList<AvailableChangeListener> = mutableListOf()

    private val childEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val user = snapshot.getValue(User::class.java)
            if (user != null && user.available) {
                availableUsers.add(user)
                notifyListeners()
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            val user = snapshot.getValue(User::class.java)
            if (user != null && user.available) {
                val existingUser = availableUsers.find { it.uid == user.uid }
                if (existingUser == null) {
                    availableUsers.add(user)
                } else {
                    availableUsers[availableUsers.indexOf(existingUser)] = user
                }
                notifyListeners()
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            val user = snapshot.getValue(User::class.java)
            if (user != null) {
                availableUsers.remove(user)
                notifyListeners()
            }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            // Not implemented
        }

        override fun onCancelled(error: DatabaseError) {
            // Failed to read value
        }
    }

    fun startListening() {
        auth = Firebase.auth
        usersRef.addChildEventListener(childEventListener)
    }

    fun stopListening() {
        usersRef.removeEventListener(childEventListener)
    }

    fun addAvailableChangeListener(listener: AvailableChangeListener) {
        listeners.add(listener)
    }

    fun removeAvailableChangeListener(listener: AvailableChangeListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it.onAvailableUsersChanged(availableUsers) }
    }

    interface AvailableChangeListener {
        fun onAvailableUsersChanged(users: List<User>)
    }

}





