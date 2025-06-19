package dev.romle.roamnoteapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepository {
    private val tag = "AuthRepository: "

    private val firebaseAuth = FirebaseAuth.getInstance()

    fun isLoggedIn(): Boolean{
        if(firebaseAuth.currentUser != null) {
            println(tag + "Already logged in")
            return true
        }

        return false
    }

    suspend fun register(email: String, password: String): Boolean{
        try {
            val result = suspendCoroutine { continuation -> firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener {
                    println(tag + "register success")
                        continuation.resume(true)

                }
                .addOnFailureListener {
                    println(tag + "register failure")
                    continuation.resume(false)
                }
            }

            return  result

        }catch (e: Exception){
            e.printStackTrace()
            if (e is CancellationException) throw e
            println(tag + "register exception ${e.message}")
            return false
        }
    }

    suspend fun login(email: String, password: String): Boolean{
        try {
            val result = suspendCoroutine { continuation -> firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener {
                    println(tag + "login success")
                    continuation.resume(true)
                }
                .addOnFailureListener {
                    println(tag + "login failure")
                    continuation.resume(false)
                }
            }

            return  result

        }catch (e: Exception){
            e.printStackTrace()
            if (e is CancellationException) throw e
            println(tag + "login exception ${e.message}")
            return false
        }
    }

    fun getUid():String?{
        return firebaseAuth.currentUser?.uid
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }


    fun logout(){
        firebaseAuth.signOut()
    }

}