package dev.romle.roamnoteapp.model

import java.util.Date

data class Trip private constructor(
    val name: String,
    val locations: MutableList<Location>,
    val startDate: Date,
    val lastDate: Date,
    val dayLogs: MutableList<DayLog>,
    val photoUrl: String?,
    val cost: Double

){
    class Builder(
        var name: String = "",
        var locations: MutableList<Location> = mutableListOf(),
        var startDate: Date = Date(),
        var lastDate: Date = Date(),
        var dayLogs: MutableList<DayLog> = mutableListOf(),
        var photoUrl: String? = null,
        var cost: Double = 0.0,
    ){

        fun name(name: String) = apply { this.name = name }
        fun addLocation(location: Location) = apply { (this.locations as MutableList).add(location)}
        fun startDate(sDate: Date) = apply { this.startDate = sDate }
        fun latDate(lDate: Date) = apply { this.lastDate = lDate }
        fun photoUrl(url: String) = apply { this.photoUrl = url }
        fun addExpense(expense: Double) = apply { this.cost += expense }

        fun build() = Trip(name, locations, startDate, lastDate, dayLogs, photoUrl, cost)
    }
}
