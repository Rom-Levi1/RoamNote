package dev.romle.roamnoteapp.ui.dialogfragments

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import dev.romle.roamnoteapp.R
import dev.romle.roamnoteapp.databinding.FragmentAddTripBinding
import dev.romle.roamnoteapp.model.Trip


class AddTripFragment : DialogFragment() {

    private var _binding: FragmentAddTripBinding? = null

    private var selectedImageUri: Uri? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.addTripIMGPhoto.setImageResource(R.drawable.check_24px)
        }
    }



    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        _binding = FragmentAddTripBinding.inflate(layoutInflater)
        val root: View = binding.root

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        return root
    }

    private fun initViews() {
        val isEdit = arguments?.getBoolean("is_edit") ?: false
        val originalName = arguments?.getString("trip_name")


        val trip = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("trip", Trip::class.java)
        } else {
            arguments?.getSerializable("trip") as? Trip
        }

        if (isEdit && trip != null) {
            binding.addTripLBLTitle.text = "Edit Trip"
            binding.addTripTXTTipName.setText(trip.name)

            trip.photoUrl?.let {
                selectedImageUri = Uri.parse(it)
                binding.addTripIMGPhoto.setImageResource(R.drawable.check_24px)
                binding.addTripTXTPhoto.text = "Change photo"
            }

        }

        binding.addTripIMGPhoto.setOnClickListener {
            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.addTripBTNSave.setOnClickListener {
            val tripName = binding.addTripTXTTipName.text.toString().trim()
            if (tripName.isEmpty()) {
                binding.addTripTXTTipName.error = "Please enter trip name"
                return@setOnClickListener
            }

            val result = Bundle().apply {
                putString("trip_name", tripName)
                putString("original_name", originalName)
                selectedImageUri?.let {
                    val isRemote = it.toString().startsWith("https://firebasestorage")
                    if (!isRemote) {
                        putParcelable("trip_image_uri", it)
                    }
                }
            }

            if (isEdit) {
                parentFragmentManager.setFragmentResult("edit_trip_request", result)
            } else {
                parentFragmentManager.setFragmentResult("add_trip_request", result)
            }


            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.80).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}