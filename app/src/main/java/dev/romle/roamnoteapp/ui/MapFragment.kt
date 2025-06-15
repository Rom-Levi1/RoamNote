package dev.romle.roamnoteapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dev.romle.roamnoteapp.R
import dev.romle.roamnoteapp.databinding.FragmentMapBinding
import dev.romle.roamnoteapp.databinding.FragmentTripsBinding
import dev.romle.roamnoteapp.model.Location

class MapFragment : Fragment() {

    private lateinit var googleMap: GoogleMap


    private var _binding: FragmentMapBinding? = null

    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync{ gmap ->
            googleMap = gmap
        }

        return root
    }

    fun zoom(location: Location, name: String){
        val pos = LatLng(location.latitude, location.longitude)

        googleMap.let {
            it.clear()
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 12f)) //zooming
            it.addMarker(MarkerOptions().position(pos).title(name))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
