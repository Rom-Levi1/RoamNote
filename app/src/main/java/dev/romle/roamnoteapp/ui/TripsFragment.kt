package dev.romle.roamnoteapp.ui

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import dev.romle.roamnoteapp.R
import dev.romle.roamnoteapp.TripAdapter
import dev.romle.roamnoteapp.data.MediaManager
import dev.romle.roamnoteapp.data.TripsRepository
import dev.romle.roamnoteapp.databinding.FragmentTripsBinding
import dev.romle.roamnoteapp.model.SessionManager
import dev.romle.roamnoteapp.model.Trip
import dev.romle.roamnoteapp.ui.dialogfragments.AddTripFragment
import java.util.Date

class TripsFragment : Fragment() {

    private var _binding: FragmentTripsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var tripAdapter: TripAdapter
    private val tripList = mutableListOf<Trip>()
    private val repo = TripsRepository()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTripsBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

    }

    private fun initViews() {

        parentFragmentManager.setFragmentResultListener("add_trip_request", viewLifecycleOwner) { _, bundle ->
            val tripName = bundle.getString("trip_name")
            val imageUri: Uri? = bundle.getParcelable("trip_image_uri")

            if (!tripName.isNullOrEmpty()) {
                val alreadyExists = tripList.any { it.name.trim().equals(tripName, ignoreCase = true) }

                if (alreadyExists) {
                    Toast.makeText(requireContext(), "Trip \"$tripName\" already exists!", Toast.LENGTH_SHORT).show()
                    return@setFragmentResultListener
                }

                if (imageUri != null) {
                    MediaManager().uploadImage(requireContext(), imageUri,
                        onSuccess = { imageUrl ->
                            val newTrip = Trip.Builder()
                                .name(tripName)
                                .photoUrl(imageUrl)
                                .build()

                            tripAdapter.addTrip(newTrip)
                            SessionManager.currentUser?.trips?.add(newTrip)
                            repo.addTrip(newTrip)
                        },
                        onFailure = {
                            Toast.makeText(requireContext(), "Failed to upload trip image", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    val newTrip = Trip.Builder()
                        .name(tripName)
                        .build()

                    tripAdapter.addTrip(newTrip)
                    SessionManager.currentUser?.trips?.add(newTrip)
                    repo.addTrip(newTrip)
                }
            }
        }

        parentFragmentManager.setFragmentResultListener("edit_trip_request", viewLifecycleOwner) { _, bundle ->
            val tripName = bundle.getString("trip_name")
            val originalName = bundle.getString("original_name")
            val imageUri: Uri? = bundle.getParcelable("trip_image_uri")

            Toast.makeText(requireContext(), "values ${tripName} , ${originalName}, ${imageUri}", Toast.LENGTH_SHORT).show()


            if (tripName.isNullOrBlank() || originalName.isNullOrBlank()) return@setFragmentResultListener

            val originalTrip = tripList.find { it.name == originalName } ?: return@setFragmentResultListener

            if (imageUri != null) {
                MediaManager().uploadImage(requireContext(), imageUri,
                    onSuccess = { imageUrl ->


                        val updatedTrip = Trip.Builder()
                            .name(tripName)
                            .photoUrl(imageUrl)
                            .startDate(Date(originalTrip.startDate))
                            .latDate(Date(originalTrip.lastDate))
                            .cost(originalTrip.cost)
                            .apply {
                                originalTrip.locations.forEach { addLocation(it) }
                                originalTrip.dayLogs.forEach { dayLogs[it.key] = it.value }
                            }
                            .build()

                        val index = SessionManager.currentUser?.trips?.indexOfFirst { it.name == originalTrip.name }
                        if (index != null && index != -1) {
                            SessionManager.currentUser?.trips?.set(index, updatedTrip)
                        }
                        tripAdapter.updateTrip(updatedTrip,originalTrip.name)
                        repo.updateTrip(updatedTrip,originalTrip.name,originalTrip.photoUrl)

                        Toast.makeText(requireContext(), "Updated trip successfully", Toast.LENGTH_SHORT).show()

                    },
                    onFailure = {
                        Toast.makeText(requireContext(), "Failed to upload trip image", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            else{
                val updatedTrip = Trip.Builder()
                    .name(tripName)
                    .photoUrl(originalTrip.photoUrl)
                    .startDate(Date(originalTrip.startDate))
                    .latDate(Date(originalTrip.lastDate))
                    .cost(originalTrip.cost)
                    .apply {
                        originalTrip.dayLogs.forEach { (id, log) -> addDayLog(id, log) }
                    }
                    .build()

                val index = SessionManager.currentUser?.trips?.indexOfFirst { it.name == originalTrip.name }
                if (index != null && index != -1) {
                    SessionManager.currentUser?.trips?.set(index, updatedTrip)
                }
                tripAdapter.updateTrip(updatedTrip,originalTrip.name)
                repo.updateTrip(updatedTrip,originalTrip.name,originalTrip.photoUrl)
            }

        }

        tripAdapter = TripAdapter(binding.root.context,tripList)
        binding.mainRVList.adapter = tripAdapter
        binding.mainRVList.layoutManager = LinearLayoutManager(requireContext())

        val cachedTrips = SessionManager.currentUser?.trips.orEmpty()
        tripList.clear()
        tripList.addAll(cachedTrips)
        tripAdapter.notifyDataSetChanged()


        binding.tripsBTNAddTrip.setOnClickListener{
            val dialog = AddTripFragment()
            dialog.show(parentFragmentManager, "AddTripDialog")
        }

        tripAdapter.onTripClicked = { view, trip ->
            showTripOptionsMenu(view, trip)
        }

    }

    private fun showTripOptionsMenu(anchorView: View, trip: Trip) {
        val popup = PopupMenu(anchorView.context, anchorView)
        popup.menuInflater.inflate(R.menu.trip_item_menu, popup.menu)

        @Suppress("RestrictedApi")
        if (popup.menu is MenuBuilder) {
            val menuBuilder = popup.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_trip_edit -> {
                    onEditTrip(trip)
                    true
                }
                R.id.menu_trip_info -> {
                    //onOverviewTrip(trip)
                    true
                }

                R.id.menu_trip_delete -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Delete Trip")
                        .setMessage("Are you sure you want to delete \"${trip.name}\"?")
                        .setPositiveButton("Delete") { _, _ ->
                            onDeleteTrip(trip)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun onEditTrip(trip: Trip) {
        val bundle = Bundle().apply {
            putBoolean("is_edit", true)
            putString("trip_name",trip.name)
            putSerializable("trip", trip)
        }

        val dialog = AddTripFragment()
        dialog.arguments = bundle
        dialog.show(parentFragmentManager, "EditTripDialog")

    }

    private fun onDeleteTrip(trip: Trip) {
        tripAdapter.removeTrip(trip)
        SessionManager.currentUser?.trips?.remove(trip)

        deleteTripImages(trip)
        repo.deleteTrip(trip.name)
        Toast.makeText(requireContext(), "Trip deleted", Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun deleteTripImages(trip: Trip) {
        trip.dayLogs.values.forEach { log ->
            log.photoUrl?.let { MediaManager().deleteImage(it) }

        }
        trip.photoUrl?.let { MediaManager().deleteImage(it) }
    }
}