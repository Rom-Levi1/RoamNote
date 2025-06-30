package dev.romle.roamnoteapp.ui.dialogfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import dev.romle.roamnoteapp.R
import dev.romle.roamnoteapp.databinding.FragmentAddHotelBinding
import dev.romle.roamnoteapp.databinding.FragmentAddPostBinding
import dev.romle.roamnoteapp.model.ForumPost
import dev.romle.roamnoteapp.model.SessionManager


class AddPostFragment : DialogFragment() {

    private var _binding: FragmentAddPostBinding? = null

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAddPostBinding.inflate(layoutInflater)
        val root: View = binding.root

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() {
        binding.addPostBTNSave.setOnClickListener {
            val title = binding.addPostTXTTitle.text.toString().trim()
            val content = binding.addPostTXTContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(requireContext(), "Title and content are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val post = ForumPost.Builder()
                .title(title)
                .content(content)
                .userId(SessionManager.currentUser?.uid ?: "unknown")
                .username(SessionManager.currentUser?.username ?: "anonymous")
                .build()

            val bundle = Bundle()
            bundle.putSerializable("post", post)

            parentFragmentManager.setFragmentResult("new_post",bundle)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}