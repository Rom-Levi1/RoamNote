package dev.romle.roamnoteapp.model

import java.util.Date

data class DayLog private constructor(
    val tripName: String,
    val date: Date,
    val hotel: Hotel?,
    val activities: MutableList<ActivityLog>,
    val expenses: MutableList<Expense>
){
    class Builder(
        var tripName: String ="",
        var date: Date = Date(),
        var hotel: Hotel? = null,
        var activities: MutableList<ActivityLog> = mutableListOf(),
        var expenses: MutableList<Expense> = mutableListOf()
    ){
        fun date(date: Date) = apply { this.date = date }
        fun hotel(hotel: Hotel?) = apply { this.hotel = hotel }
        fun addActivity(activity: ActivityLog) = apply { (this.activities as MutableList).add(activity) }
        fun addExpense(expense: Expense) = apply { (this.expenses as MutableList).add(expense)}
        fun tripName(name:String) = apply { this.tripName = name }

        fun build(): DayLog = DayLog(tripName,date, hotel, activities, expenses)
    }

}
