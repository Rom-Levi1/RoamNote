package dev.romle.roamnoteapp.model

import java.io.Serializable
import java.util.Date

data class DayLog(
    val tripName: String,
    val date: Long,
    val hotel: Hotel?,
    val activities: Map<String, ActivityLog>,
    val expenses: Map<String, Expense>,
    val logCost: Double,
    val photoUrl: String?
): Serializable {
    class Builder(
        var tripName: String = "",
        var date: Long = System.currentTimeMillis(),
        var hotel: Hotel? = null,
        var expenses: MutableMap<String, Expense> = mutableMapOf(),
        var activities: MutableMap<String, ActivityLog> = mutableMapOf(),
        var photoUrl: String? = null,

    ) {
        fun date(date: Date) = apply { this.date = date.time }
        fun hotel(hotel: Hotel?) = apply { this.hotel = hotel }
        fun addActivity(id: String, activity: ActivityLog) =
            apply { this.activities[id] = activity }

        fun addExpense(id: String, expense: Expense) = apply { this.expenses[id] = expense }
        fun tripName(name: String) = apply { this.tripName = name }
        fun photoUrl(url: String) = apply { this.photoUrl = url }
        fun removeActivity(activityId: String) = apply { this.activities.remove(activityId) }
        fun removeExpense(expenseId: String)  = apply{ this.expenses.remove(expenseId)}



        fun build(): DayLog {
            val expensesCost = expenses.values.sumOf { it.cost }
            val hotelCost = hotel?.cost ?: 0.0
            val activitiesCost = activities.values.sumOf { it.cost }
            val totalCost = expensesCost + hotelCost + activitiesCost
            return DayLog(tripName, date, hotel, activities, expenses, totalCost, photoUrl)
        }


        fun reset(): Builder = apply {
            hotel = null
            activities.clear()
            expenses.clear()
            photoUrl = null
        }

    }


        constructor() : this(
            "",
            System.currentTimeMillis(),
            null,
            mapOf(),
            mapOf(),
            0.0,
            null
        )

}


