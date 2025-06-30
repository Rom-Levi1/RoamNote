package dev.romle.roamnoteapp.adaptors

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.romle.roamnoteapp.databinding.ItemForumPostBinding
import dev.romle.roamnoteapp.model.ForumPost
import java.text.SimpleDateFormat
import java.util.*
import android.view.View


class ForumAdapter : RecyclerView.Adapter<ForumAdapter.ForumViewHolder>() {

    private val posts: MutableList<ForumPost> = mutableListOf()
    var onPostClicked: ((View, ForumPost) -> Unit)? = null

    inner class ForumViewHolder(val binding: ItemForumPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumViewHolder {
        val binding = ItemForumPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ForumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForumViewHolder, position: Int) {
        val post = posts[position]
        val b = holder.binding

        b.postLBLTitle.text = post.title
        b.postLBLAuthor.text = post.username
        b.postLBLContent.text = post.content

        val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        b.postLBLTime.text = formatter.format(Date(post.timestamp))

        holder.itemView.setOnClickListener {
            onPostClicked?.invoke(it,post)
        }
    }

    override fun getItemCount(): Int = posts.size

    //  Add a new post to the top
    fun addPost(post: ForumPost) {
        posts.add(0, post)
        notifyItemInserted(0)
    }

    //  Replace entire list
    fun setPosts(newPosts: List<ForumPost>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    //  Remove a post
    fun removePost(post: ForumPost) {
        val index = posts.indexOfFirst { it.id == post.id }
        if (index != -1) {
            posts.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}