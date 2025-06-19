package dev.romle.roamnoteapp.ui.dialogfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import dev.romle.roamnoteapp.databinding.FragmentAddTripBinding


class AddTripFragment : DialogFragment() {

    private var _binding: FragmentAddTripBinding? = null

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
        binding.addTripBTNSave.setOnClickListener{

            val tripName = binding.addTripTXTTipName.text.toString()
            if (tripName.isEmpty()) {
            binding.addTripTXTTipName.error = "Please enter trip name"    }

            val result = Bundle().apply {
                putString("trip_name", tripName)
            }

            parentFragmentManager.setFragmentResult("add_trip_request", result)
            dismiss()

        }
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