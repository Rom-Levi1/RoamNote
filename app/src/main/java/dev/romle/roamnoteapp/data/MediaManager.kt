package dev.romle.roamnoteapp.data

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import dev.romle.roamnoteapp.model.Trip
import java.util.UUID

class MediaManager {
    private val storageRef = FirebaseStorage.getInstance().reference

    fun uploadImage(context: Context, uri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val filename = "images/${UUID.randomUUID()}"
        val fileRef = storageRef.child(filename)

        fileRef.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                fileRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                Toast.makeText(context, "Upload successful!", Toast.LENGTH_SHORT).show()
                onSuccess(downloadUri.toString())
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                onFailure(e)
            }
    }

    fun deleteImage(imageUrl: String) {
        try {
            val fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
            fileRef.delete()
                .addOnSuccessListener {
                    Log.d("Media Manager", "Deleted image successfully")
                }
                .addOnFailureListener { e ->
                    // Failed to delete image (you can log it if needed)
                    Log.d("Media Manager", "Failed to delete image")

                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}