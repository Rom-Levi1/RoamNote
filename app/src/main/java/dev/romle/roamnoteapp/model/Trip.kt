package dev.romle.roamnoteapp.model

import java.util.Date

data class Trip (
    val name: String,
    val locations: MutableList<Location>,
    val startDate: Long,
    val lastDate: Long,
    val dayLogs: MutableList<DayLog>,
    val photoUrl: String?,
    val cost: Double

){
    class Builder(
        var name: String = "",
        var locations: MutableList<Location> = mutableListOf(),
        var startDate: Long = System.currentTimeMillis(),
        var lastDate: Long = System.currentTimeMillis(),
        var dayLogs: MutableList<DayLog> = mutableListOf(),
        var photoUrl: String? = null,
        var cost: Double = 0.0,
    ){

        fun name(name: String) = apply { this.name = name }
        fun addLocation(location: Location) = apply { (this.locations as MutableList).add(location)}
        fun startDate(sDate: Date) = apply { this.startDate = sDate.time }
        fun latDate(lDate: Date) = apply { this.lastDate = lDate.time }
        fun photoUrl(url: String) = apply { this.photoUrl = url }
        fun addExpense(expense: Double) = apply { this.cost += expense }

        fun build() = Trip(name, locations, startDate, lastDate, dayLogs, photoUrl, cost)
    }

    constructor() : this("", mutableListOf(), 0L, 0L, mutableListOf(), null, 0.0)

}
