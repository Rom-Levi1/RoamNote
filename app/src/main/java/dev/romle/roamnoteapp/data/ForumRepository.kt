package dev.romle.roamnoteapp.data

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import dev.romle.roamnoteapp.model.ForumPost

class ForumRepository {

    private val forumRef = FirebaseDatabase.getInstance().getReference("forum")

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

    fun loadPosts(callback: (List<ForumPost>) -> Unit) {
        forumRef.get()
            .addOnSuccessListener { snapshot ->
                val now = System.currentTimeMillis()
                val twoWeeksMillis = 14 * 24 * 60 * 60 * 1000L

                val posts = snapshot.children.mapNotNull { it.getValue(ForumPost::class.java) }
                    .filter { now - it.timestamp <= twoWeeksMillis } // Only keep recent posts
                    .sortedByDescending { it.timestamp }

                callback(posts)
            }
            .addOnFailureListener { error ->
                Log.e("ForumRepo", "Failed to load forum posts", error)
                callback(emptyList())
            }
    }

    fun updatePost(post: ForumPost, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        forumRef.child(post.id).setValue(post)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}


