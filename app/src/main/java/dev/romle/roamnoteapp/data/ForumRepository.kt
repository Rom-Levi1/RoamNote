package dev.romle.roamnoteapp.data

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import dev.romle.roamnoteapp.model.ForumPost
import dev.romle.roamnoteapp.model.Trip

class ForumRepository {

    private val forumRef = FirebaseDatabase.getInstance().getReference("forumPosts")

    fun addPost(post: ForumPost, onSuccess: (ForumPost) -> Unit, onFailure: (Exception) -> Unit) {
        val postId = forumRef.push().key
        if (postId != null) {
            val postWithId = post.copy(id = postId)
            forumRef.child(postId).setValue(postWithId)
                .addOnSuccessListener {
                    onSuccess(postWithId)
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        } else {
            onFailure(Exception("Could not generate post ID"))
        }
    }

    fun deletePost(postId: String, context: android.content.Context) {
        val postRef = forumRef.child(postId)

        postRef.removeValue()
            .addOnSuccessListener {
                android.widget.Toast.makeText(context, "Post deleted", android.widget.Toast.LENGTH_SHORT).show()
                Log.d("ForumRepository", "Deleted post: $postId")
            }
            .addOnFailureListener { e ->
                android.widget.Toast.makeText(context, "Failed to delete post!", android.widget.Toast.LENGTH_SHORT).show()
                Log.e("ForumRepository", "Delete failed: ${e.message}")
            }
    }

    fun loadPosts(callback: (List<ForumPost>) -> Unit){
        forumRef.get()
            .addOnSuccessListener { snapShot ->
                val posts = mutableListOf<ForumPost>()
                for (postSnap in snapShot.children)
                {
                    val post = postSnap.getValue(ForumPost::class.java)
                    if (post != null)
                    {
                        posts.add(post)
                    }
                }

                callback(posts)
            }
            .addOnFailureListener { error ->
                Log.e("ForumRepo", "Failed to load forum posts", error)
                callback(emptyList()) // Or handle the error more gracefully
            }
    }
}


