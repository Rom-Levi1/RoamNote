package dev.romle.roamnoteapp.model

import java.util.Date

data class DayLog(
    val tripName: String,
    val date: Long,
    val hotel: Hotel?,
    val activities: MutableList<ActivityLog>,
    val expenses: MutableList<Expense>,
    val logCost: Double
) {
    class Builder(
        var tripName: String = "",
        var date: Long = System.currentTimeMillis(),
        var hotel: Hotel? = null,
        var activities: MutableList<ActivityLog> = mutableListOf(),
        var expenses: MutableList<Expense> = mutableListOf()
    ) {
        fun date(date: Date) = apply { this.date = date.time }
        fun hotel(hotel: Hotel?) = apply { this.hotel = hotel }
        fun addActivity(activity: ActivityLog) = apply { this.activities.add(activity) }
        fun addExpense(expense: Expense) = apply { this.expenses.add(expense) }
        fun tripName(name: String) = apply { this.tripName = name }

        fun build(): DayLog {
            val expensesCost = expenses.sumOf { it.cost }
            val hotelCost = hotel?.cost ?: 0.0
            val activitiesCost = activities.sumOf { it.cost }
            val totalCost = expensesCost + hotelCost + activitiesCost
            return DayLog(tripName, date, hotel, activities, expenses, totalCost)
        }
    }
}


