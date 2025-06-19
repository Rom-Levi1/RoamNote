package dev.romle.roamnoteapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap

class LocationRequestManager (
    private val context: Context,
    private val permissionLauncher: ActivityResultLauncher<Array<String>>,
    private val map: GoogleMap? = null,
    private val onLocationRetrieved: ((Double, Double) -> Unit)? = null
) {

    companion object {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    fun requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(permissions)
        } else {
            enableUserLocation()
            fetchLocation()
        }
    }

    fun handlePermissionsResult(permissions: Map<String, Boolean>) {
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fine || coarse) {
            enableUserLocation()
            fetchLocation()
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map?.isMyLocationEnabled = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (location != null) {
                onLocationRetrieved?.invoke(location.latitude, location.longitude)
            } else {
                Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
                onLocationRetrieved?.invoke(0.0, 0.0)
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Location fetch failed", Toast.LENGTH_SHORT).show()
            onLocationRetrieved?.invoke(0.0, 0.0)
        }
    }
}
