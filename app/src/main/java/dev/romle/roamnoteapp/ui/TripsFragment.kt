package dev.romle.roamnoteapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import dev.romle.roamnoteapp.R
import dev.romle.roamnoteapp.TripAdapter
import dev.romle.roamnoteapp.databinding.FragmentTripsBinding
import dev.romle.roamnoteapp.model.Trip

class TripsFragment : Fragment() {

    private var _binding: FragmentTripsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var tripAdapter: TripAdapter
    private val tripList = mutableListOf<Trip>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTripsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        parentFragmentManager.setFragmentResultListener("add_trip_request", viewLifecycleOwner) { _, bundle ->
            val tripName = bundle.getString("trip_name")
            if (!tripName.isNullOrEmpty()) {
                val newTrip = Trip.Builder()
                    .name(tripName)
                    .build()

                tripAdapter.addTrip(newTrip)
            }
        }

        binding.tripsBTNAddTrip.setOnClickListener{
            val dialog = AddTripFragment()
            dialog.show(parentFragmentManager, "AddTripDialog")
        }

        tripAdapter = TripAdapter(tripList)
        binding.mainRVList.adapter = tripAdapter
        binding.mainRVList.layoutManager = LinearLayoutManager(requireContext())

        return root
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}