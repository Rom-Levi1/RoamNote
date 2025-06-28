package dev.romle.roamnoteapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.PopupMenu
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dev.romle.roamnoteapp.R
import dev.romle.roamnoteapp.databinding.FragmentMapBinding
import dev.romle.roamnoteapp.databinding.FragmentTripsBinding
import dev.romle.roamnoteapp.model.Location
import dev.romle.roamnoteapp.model.SearchableLocation
import dev.romle.roamnoteapp.model.SessionManager
import dev.romle.roamnoteapp.model.Trip

class MapFragment : Fragment() {

    private lateinit var googleMap: GoogleMap

    // Stores all locations from selected trip for search/autocomplete
    private val searchLocations = mutableListOf<SearchableLocation>()

    private var _binding: FragmentMapBinding? = null

    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root



        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load Google Map fragment

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync{ gmap ->
            googleMap = gmap
        }

        // Show trip selection menu

        binding.mapIMGMenu.setOnClickListener {
            showTripPopupMenu(it)
        }
    }


    // Opens popup menu to select a trip
    private fun showTripPopupMenu(anchor: View) {
        val popupMenu = PopupMenu(requireContext(), anchor)
        val tripList = SessionManager.currentUser?.trips ?: return

        tripList.forEachIndexed { index, trip ->
            popupMenu.menu.add(0, index, index, trip.name)
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            val selectedTrip = tripList[menuItem.itemId]
            handleTripSelected(selectedTrip)
            true
        }

        popupMenu.show()
    }

    // Handles pinning all locations from selected trip
    private fun handleTripSelected(trip: Trip) {
        googleMap.clear()
        searchLocations.clear()

        val boundsBuilder = com.google.android.gms.maps.model.LatLngBounds.Builder()
        var hasValidLocation = false

        val addedHotelNames = mutableSetOf<String>()

        // Loop over all day logs in the trip
        trip.dayLogs.values.forEach { log ->

            // Add all hotel pins
            log.hotel?.let { hotel ->
                if (hotel.name !in addedHotelNames) {
                    val loc = hotel.location
                    if (loc.latitude != 0.0 && loc.longitude != 0.0) {
                        val latLng = LatLng(loc.latitude, loc.longitude)
                        googleMap.addMarker(MarkerOptions().position(latLng).title("Hotel: ${hotel.name}"))
                        boundsBuilder.include(latLng)
                        searchLocations.add(SearchableLocation(hotel.name, latLng,"Hotel"))
                        hasValidLocation = true
                        addedHotelNames.add(hotel.name)
                    }
                }
            }

            // Add all activity pins
            log.activities.values.forEach { activity ->
                val loc = activity.location
                if (loc.latitude != 0.0 && loc.longitude != 0.0) {
                    val latLng = LatLng(loc.latitude, loc.longitude)
                    googleMap.addMarker(
                        MarkerOptions().position(latLng).title("Activity: ${activity.name}")
                    )
                    searchLocations.add(SearchableLocation(activity.name, latLng,"Activity"))
                    boundsBuilder.include(latLng)
                    hasValidLocation = true
                }
            }
        }

        // Auto-fit all of the markers
        if (hasValidLocation) {
            val bounds = boundsBuilder.build()
            val padding = 300

            binding.mapFragment.post {
                googleMap.setOnMapLoadedCallback {
                    //Fit bounds
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))

                    //Check zoom
                    val currentZoom = googleMap.cameraPosition.zoom

                    //If too close, zoom out manually
                    if (currentZoom > 15f) {
                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15f)) // or 10f for a wider view
                    }
                }
            }
        }

        // Load search suggestions
        setupSearchBar()
    }


    // Sets up search suggestions and zooms when an item is selected
    private fun setupSearchBar() {
        val displayNames = searchLocations.map { "${it.tag}: ${it.name}" }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, displayNames)
        binding.mapEDTSearch.setAdapter(adapter)

        binding.mapEDTSearch.setOnItemClickListener { _, _, position, _ ->
            val selectedDisplay = adapter.getItem(position) ?: return@setOnItemClickListener
            val result = searchLocations.find { "${it.tag}: ${it.name}" == selectedDisplay } ?: return@setOnItemClickListener

            // Add marker
            googleMap.addMarker(
                MarkerOptions()
                    .position(result.location)
                    .title("${result.tag}: ${result.name}")
            )?.showInfoWindow()

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(result.location, 15f))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
