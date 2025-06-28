package dev.romle.roamnoteapp.model

import java.io.Serializable

data class ActivityLog (
    val id: String,
    val name: String,
    val location: Location,
    val cost: Double,
    val tags: List<ActivityTag>,
    val satisfaction: Int,
    val note: String?
): Serializable{
    class Builder(
        var id: String = "",
        var name:String = "",
        var location: Location.Builder = Location.Builder(),
        var cost: Double = 0.0,
        var tags: List<ActivityTag> = listOf(),
        var satisfaction: Int = 5,
        var note: String? = null
    ): Serializable{
        fun id(id: String) = apply { this.id = id }
        fun name(name: String) = apply{this.name = name}
        fun location(lat: Double, lon: Double) = apply {
            this.location = Location.Builder().latitude(lat).longitude(lon)
        }
        fun cost(cost: Double) = apply{this.cost = cost}
        fun tags(tags: List<ActivityTag>) = apply { this.tags = tags }
        fun satisfaction(sat:Int) = apply { this.satisfaction = sat }
        fun note(note:String?) = apply { this.note = note }

        fun build() = ActivityLog(
            id,name, location.build(), cost, tags, satisfaction, note
        )
    }

    constructor() : this(
        id = "",
        name = "",
        location = Location(),
        cost = 0.0,
        tags = listOf(),
        satisfaction = 0,
        note = null
    )
}
