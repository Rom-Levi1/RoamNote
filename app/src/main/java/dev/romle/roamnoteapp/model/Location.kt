package dev.romle.roamnoteapp.model

import java.io.Serializable

data class Location (
    val latitude: Double,
    val longitude: Double
): Serializable {
    class Builder(
        var latitude: Double = 0.0,
        var longitude: Double = 0.0
    ){
        fun latitude(lat: Double) = apply{this.latitude = lat}
        fun longitude(lon: Double) = apply{this.longitude = lon}

        fun build() = Location(
            latitude,longitude
        )
    }

    constructor() : this(0.0, 0.0)

}
