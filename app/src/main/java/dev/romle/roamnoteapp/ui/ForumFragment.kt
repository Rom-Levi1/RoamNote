package dev.romle.roamnoteapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dev.romle.roamnoteapp.R
import dev.romle.roamnoteapp.adaptors.ForumAdapter
import dev.romle.roamnoteapp.data.ForumRepository
import dev.romle.roamnoteapp.databinding.FragmentForumBinding
import dev.romle.roamnoteapp.model.ForumPost
import dev.romle.roamnoteapp.model.SessionManager
import dev.romle.roamnoteapp.ui.dialogfragments.AddPostFragment

class ForumFragment : Fragment() {

    private var _binding: FragmentForumBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val forumRepo = ForumRepository()

    private lateinit var forumAdapter: ForumAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentForumBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {
        forumAdapter = ForumAdapter()
        binding.mainRVList.layoutManager = LinearLayoutManager(requireContext())
        binding.mainRVList.adapter = forumAdapter

        forumRepo.loadPosts { loadedPosts ->
            forumAdapter.setPosts(loadedPosts)
        }

        parentFragmentManager.setFragmentResultListener("new_post", viewLifecycleOwner) { _, bundle ->
            val post = bundle.getSerializable("post") as? ForumPost ?: return@setFragmentResultListener

            forumRepo.addPost(
                post,
                onSuccess = { savedPost ->
                    Toast.makeText(requireContext(), "Post uploaded!", Toast.LENGTH_SHORT).show()
                    Log.d("ForumFragment", "Post saved with ID: ${savedPost.id}")
                    forumAdapter.addPost(savedPost)
                },
                onFailure = { e ->
                    Toast.makeText(requireContext(), "Failed to upload post!", Toast.LENGTH_SHORT).show()
                    Log.e("ForumFragment", "Error: ${e.message}")
                }
            )
        }

        parentFragmentManager.setFragmentResultListener("edit_post", viewLifecycleOwner){_, bundle ->
            val updatedPost = bundle.getSerializable("edit_post") as? ForumPost ?: return@setFragmentResultListener

            forumRepo.updatePost(
                updatedPost,
                onSuccess = {
                    Toast.makeText(requireContext(), "Post updated!", Toast.LENGTH_SHORT).show()
                    forumAdapter.updatePost(updatedPost)
                },
                onFailure = {
                    Toast.makeText(requireContext(), "Failed to update post", Toast.LENGTH_SHORT).show()
                }
            )        }

        binding.forumBTNNewPost.setOnClickListener {
            AddPostFragment().show(parentFragmentManager, "AddPostFragment")
        }

        forumAdapter.onPostClicked = { view, post ->
            showPostOptionMenu(view,post)
        }

    }

    private fun showPostOptionMenu(anchorView: View,post: ForumPost) {
        val popup = PopupMenu(anchorView.context,anchorView)
        popup.menuInflater.inflate(R.menu.forum_post_menu,popup.menu)

        @Suppress("RestrictedApi")
        if (popup.menu is MenuBuilder){
            val menuBuilder = popup.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
        }

        val isOwner = post.userId == SessionManager.currentUser?.uid
        popup.menu.findItem(R.id.menu_post_edit).isVisible = isOwner
        popup.menu.findItem(R.id.menu_post_delete).isVisible = isOwner

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_post_edit -> {
                    onEditPost(post)
                    true
                }
                R.id.menu_post_delete -> {
                    onDeletePost(post)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun onDeletePost(post: ForumPost) {
        if (post.userId != SessionManager.currentUser?.uid) {
            Toast.makeText(requireContext(), "You can only delete your own post", Toast.LENGTH_SHORT).show()
            return
        }
        forumRepo.deletePost(post.id, requireContext())
        forumAdapter.removePost(post)
    }

    private fun onEditPost(post: ForumPost) {
        if (post.userId != SessionManager.currentUser?.uid) {
            Toast.makeText(requireContext(), "You can only edit your own post", Toast.LENGTH_SHORT).show()
            return
        }
        val dialog = AddPostFragment().apply {
            arguments = Bundle().apply {
                putSerializable("edit_post", post)
            }
        }
        dialog.show(parentFragmentManager, "EditPostFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}