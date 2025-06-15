package dev.romle.roamnoteapp.model

data class Location private constructor(
    val latitude: Double,
    val longitude: Double
){
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
}
