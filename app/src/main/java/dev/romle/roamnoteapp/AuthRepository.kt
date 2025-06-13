package dev.romle.roamnoteapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthRepository {
    private val tag = "AuthRepository: "

    private val firebaseAuth = FirebaseAuth.getInstance()
}