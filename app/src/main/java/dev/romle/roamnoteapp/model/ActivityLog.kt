package dev.romle.roamnoteapp.model

import java.io.Serializable

data class ActivityLog (
    val name: String,
    val location: Location,
    val cost: Double,
    val tags: List<ActivityTag>,
    val satisfaction: Int,
    val note: String?
): Serializable{
    class Builder(
        var name:String = "",
        var location: Location.Builder = Location.Builder(),
        var cost: Double = 0.0,
        var tags: List<ActivityTag> = listOf(),
        var satisfaction: Int = 5,
        var note: String? = null
    ){
        fun name(name: String) = apply{this.name = name}
        fun location(lat: Double, lon: Double) = apply {
            this.location = Location.Builder().latitude(lat).longitude(lon)
        }
        fun cost(cost: Double) = apply{this.cost = cost}
        fun tags(tags: List<ActivityTag>) = apply { this.tags = tags }
        fun satisfaction(sat:Int) = apply { this.satisfaction = sat }
        fun note(note:String?) = apply { this.note = note }

        fun build() = ActivityLog(
            name, location.build(), cost, tags, satisfaction, note
        )
    }
}
