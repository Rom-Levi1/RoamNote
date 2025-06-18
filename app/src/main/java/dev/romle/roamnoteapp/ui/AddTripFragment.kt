package dev.romle.roamnoteapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import dev.romle.roamnoteapp.R
import dev.romle.roamnoteapp.databinding.FragmentAddTripBinding
import dev.romle.roamnoteapp.databinding.FragmentDailyLogBinding


class AddTripFragment : DialogFragment() {

    private var _binding: FragmentAddTripBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        _binding = FragmentAddTripBinding.inflate(layoutInflater)
        val root: View = binding.root

        initViews()

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

}