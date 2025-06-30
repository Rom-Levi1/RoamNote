package dev.romle.roamnoteapp.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dev.romle.roamnoteapp.model.SessionManager

class UserRepository {

    private val usersRef =
        FirebaseDatabase.getInstance().getReference("users")

    fun addUsername(username: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Log.e("TripsRepo", "UID is null. Username not saved.")
            return
        }

        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        userRef.child("username").setValue(username)
            .addOnSuccessListener {
                Log.d("TripsRepo", "Username saved successfully")
            }
            .addOnFailureListener { error ->
                Log.e("TripsRepo", "Failed to save username", error)
            }

        userRef.child("email").setValue(FirebaseAuth.getInstance().currentUser?.email)
            .addOnSuccessListener {
                Log.d("TripsRepo", "email saved successfully")
            }
            .addOnFailureListener { error ->
                Log.e("TripsRepo", "Failed to save email", error)
            }
    }

    fun loadBasicUserData(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val mail = FirebaseAuth.getInstance().currentUser?.email

        if (uid == null || mail == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        usersRef.child(uid).child("username").get()
            .addOnSuccessListener { snapshot ->
                val username = snapshot.getValue(String::class.java)
                if (username != null) {
                    val user = dev.romle.roamnoteapp.model.User.Builder()
                        .uid(uid)
                        .mail(mail)
                        .username(username)
                        .trips(mutableListOf())
                        .build()

                    SessionManager.currentUser = user
                    Log.d("UserRepo", "Basic user loaded: $username ($uid)")
                    onSuccess()
                } else {
                    onFailure(Exception("Username not found in DB"))
                }
            }
            .addOnFailureListener { error ->
                Log.e("UserRepo", "Failed to load basic user", error)
                onFailure(error)
            }
    }


    fun isUsernameAvailable(username: String, onResult: (Boolean) -> Unit) {
        usersRef.get()
            .addOnSuccessListener { snapshot ->
                var isTaken = false

                for (userSnap in snapshot.children) {
                    val existingUsername = userSnap.child("username").getValue(String::class.java)
                    if (existingUsername.equals(username, ignoreCase = true)) {
                        isTaken = true
                        break
                    }
                }

                onResult(!isTaken)
            }
            .addOnFailureListener { error ->
                Log.e("TripsRepo", "Username check failed", error)
                onResult(false)
            }
    }
}