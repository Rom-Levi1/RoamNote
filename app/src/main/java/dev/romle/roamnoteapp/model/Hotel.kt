package dev.romle.roamnoteapp.model

import android.media.Rating
import android.widget.RatingBar

data class Hotel(
    val name: String,
    val rating: Float,
    val cost: Double,
    val latitude: Double,
    val longitude: Double,
    val description: String?
){

    class Builder(
    var name:String = "",
    var rating: Float = 0.0F,
    var cost: Double = 0.0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var description: String? = null
){
        fun name(name: String) = apply{this.name = name}
        fun rating(rating: Float) = apply { this.rating = rating }
        fun cost(cost: Double) = apply{this.cost = cost}
        fun latitude(lat: Double) = apply{this.latitude = lat}
        fun longitude(lon: Double) = apply{this.longitude = lon}
        fun description(description: String?) = apply { this.description = description }

        fun build() = Hotel(
            name, rating, cost, latitude, longitude, description
        )
    }
}
