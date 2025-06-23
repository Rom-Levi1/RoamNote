package dev.romle.roamnoteapp.ui.dialogfragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.google.android.gms.location.LocationServices
import dev.romle.roamnoteapp.LocationRequestManager
import dev.romle.roamnoteapp.databinding.FragmentAddHotelBinding
import dev.romle.roamnoteapp.model.Hotel
import dev.romle.roamnoteapp.model.Location


class AddHotelFragment : DialogFragment() {

    private var _binding: FragmentAddHotelBinding? = null

    private val binding get() = _binding!!

    private lateinit var locationManager: LocationRequestManager

    private var hotelLat: Double = 0.0
    private var hotelLon: Double = 0.0

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            locationManager.handlePermissionsResult(permissions)
        }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddHotelBinding.inflate(layoutInflater)
        val root: View = binding.root

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {

        locationManager = LocationRequestManager(requireContext(),permissionLauncher,null){
            lat, lon ->
            hotelLat = lat
            hotelLon = lon

            Toast.makeText(
                requireContext(),
                "Location saved!",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.logIMGLocation.setOnClickListener {
            locationManager.requestLocationPermissions()

        }

        binding.addHotelBTNSave.setOnClickListener {
            val name = binding.addHotelTXTHotelName.text.toString().trim()
            val cost = binding.addHotelTXTCost.text.toString().toDoubleOrNull() ?: 0.0
            val rating = binding.addHotelRatingBar.rating
            val description = binding.addHotelTXTDescription.text.toString().let {
                if (it.isNotBlank()) it else null
            }

            val hotelLocation = Location.Builder()
                .latitude(hotelLat)
                .longitude(hotelLon)
                .build()

            if (name.isEmpty()) {
                binding.addHotelTXTHotelName.error = "Please enter hotel name"
                return@setOnClickListener
            }

            val parsedCost = cost
            if (parsedCost == null) {
                binding.addHotelTXTCost.error = "Enter a valid number"
                return@setOnClickListener
            }

            val hotel = Hotel(
                name = name,
                cost = cost,
                rating = rating,
                description = description,
                location = hotelLocation)

            val bundle = Bundle().apply {
                putSerializable("HOTEL", hotel)
            }
            parentFragmentManager.setFragmentResult("HOTEL_RESULT", bundle)
            dismiss()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}