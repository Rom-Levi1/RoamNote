package dev.romle.roamnoteapp.model

import java.io.Serializable
import java.util.Date

data class Trip (
    val name: String,
    val id: String,
    val startDate: Long,
    val lastDate: Long,
    val dayLogs: Map<String, DayLog>,
    val photoUrl: String?,
    val cost: Double

): Serializable{
    class Builder(
        var name: String = "",
        var id: String = "",
        var startDate: Long = System.currentTimeMillis(),
        var lastDate: Long = System.currentTimeMillis(),
        var dayLogs: MutableMap<String, DayLog> = mutableMapOf(),
        var photoUrl: String? = null,
        var cost: Double = 0.0,
    ){
        fun id(id: String) = apply { this.id = id }
        fun name(name: String) = apply { this.name = name }
        fun startDate(sDate: Date) = apply { this.startDate = sDate.time }
        fun latDate(lDate: Date) = apply { this.lastDate = lDate.time }
        fun photoUrl(url: String?) = apply { this.photoUrl = url }
        fun cost(cost: Double) = apply { this.cost = cost }
        fun addDayLog(id: String,log: DayLog) = apply { this.dayLogs[id] = log }



        fun build() = Trip(name,id, startDate, lastDate, dayLogs, photoUrl, cost)
    }

    constructor() : this("","",  0L, 0L, mapOf(), null, 0.0)

}
