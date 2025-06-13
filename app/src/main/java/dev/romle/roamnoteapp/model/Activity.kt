package dev.romle.roamnoteapp.model

data class Activity private constructor(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val cost: Double,
    val tags: List<String>,
    val satisfaction: Int,
    val note: String?
){
    class Builder(
        var name:String = "",
        var latitude: Double = 0.0,
        var longitude: Double = 0.0,
        var cost: Double = 0.0,
        var tags: List<String> = listOf(),
        var satisfaction: Int = 5,
        var note: String? = null
    ){
        fun name(name: String) = apply{this.name = name}
        fun latitude(lat: Double) = apply{this.latitude = lat}
        fun longitude(lon: Double) = apply{this.longitude = lon}
        fun cost(cost: Double) = apply{this.cost = cost}
        fun tags(tags: List<String>) = apply { this.tags = tags }
        fun satisfaction(sat:Int) = apply { this.satisfaction = sat }
        fun note(note:String?) = apply { this.note = note }

        fun build() = Activity(
            name, latitude, longitude, cost, tags, satisfaction, note
        )
    }
}
