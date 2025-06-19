package dev.romle.roamnoteapp.model

import android.media.Rating
import android.widget.RatingBar
import java.io.Serializable

data class Hotel(
    val name: String,
    val rating: Float,
    val cost: Double,
    val location: Location,
    val description: String?
): Serializable{

    class Builder(
    var name:String = "",
    var rating: Float = 0.0F,
    var cost: Double = 0.0,
    var location: Location.Builder = Location.Builder(),
    var description: String? = null
){
        fun name(name: String) = apply{this.name = name}
        fun rating(rating: Float) = apply { this.rating = rating }
        fun cost(cost: Double) = apply{this.cost = cost}
        fun location(lat: Double, lon: Double) = apply {
            this.location = Location.Builder().latitude(lat).longitude(lon)
        }
        fun description(description: String?) = apply { this.description = description }

        fun build() = Hotel(
            name, rating, cost, location.build(), description
        )
    }
}
