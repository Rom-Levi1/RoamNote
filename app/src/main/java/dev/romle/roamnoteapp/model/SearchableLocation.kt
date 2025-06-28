package dev.romle.roamnoteapp.model

import com.google.android.gms.maps.model.LatLng

data class SearchableLocation(
    val name: String,
    val location: LatLng,
    val tag: String
)