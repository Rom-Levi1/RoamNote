package dev.romle.roamnoteapp.data

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import dev.romle.roamnoteapp.model.ActivityLog
import dev.romle.roamnoteapp.model.DayLog
import dev.romle.roamnoteapp.model.Expense
import dev.romle.roamnoteapp.model.Trip
import java.text.SimpleDateFormat
import java.util.*

class TripsRepository {

    private val authRepo = AuthRepository()
    private val uid = authRepo.getUid()
    private val tripsRef = FirebaseDatabase.getInstance().getReference("users").child(uid!!).child("trips")


    fun addTrip(trip: Trip) {
        val tripId = tripsRef.push().key
        if (tripId == null) {
            Log.e("TripsRepository", "Failed to generate trip ID")
            return
        }

        tripsRef.child(tripId).setValue(trip)
            .addOnSuccessListener {
                Log.d("TripsRepository", "Trip saved!")
            }
            .addOnFailureListener {
                Log.e("TripsRepository", "Failed to save trip", it)
            }
    }

    fun addOrUpdateDailyLog(tripId: String, log: DayLog) {

        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(log.date))

        val dayLogsRef = tripsRef.child(tripId).child("dayLogs")
        val tripRef = tripsRef.child(tripId)

        dayLogsRef.child(dateId).setValue(log)
            .addOnSuccessListener {
            dayLogsRef.get().addOnSuccessListener { snapshot ->
                var total = 0.0
                var minDate = Long.MAX_VALUE
                var maxDate = Long.MIN_VALUE

                for (logSnap in snapshot.children) {
                    val dayLog = logSnap.getValue(DayLog::class.java)
                    if (dayLog != null){
                        total += dayLog.logCost
                        if (dayLog.date < minDate) minDate = dayLog.date
                        if (dayLog.date > maxDate) maxDate = dayLog.date
                    }
                }
                val updates = mapOf(
                    "cost" to total,
                    "startDate" to minDate,
                    "lastDate" to maxDate
                )

                tripRef.updateChildren(updates)
            }
        }
    }

    fun addActivity(tripId: String, logDate: Long, activity: ActivityLog) {

        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(logDate))
        val activityId = UUID.randomUUID().toString()

        val activitiesRef = tripsRef
            .child(tripId)
            .child("dayLogs")
            .child(dateId)
            .child("activities")


        activitiesRef.child(activityId).setValue(activity)
            .addOnSuccessListener {
                Log.d("TripsRepo", "Activity added to $dateId!")
            }
            .addOnFailureListener {
                Log.e("TripsRepo", "Failed to add activity", it)
            }

    }

    fun deleteActivity(tripId: String, logDate: Long, activityId: String) {
        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(logDate))

        val activityRef = tripsRef
            .child(tripId)
            .child("dayLogs")
            .child(dateId)
            .child("activities")
            .child(activityId)

        activityRef.removeValue()
            .addOnSuccessListener {
                Log.d("TripsRepo", "Activity $activityId deleted from $dateId!")
            }
            .addOnFailureListener {
                Log.e("TripsRepo", " Failed to delete activity", it)
            }
    }

    fun addExpense(tripId: String, logDate: Long,expense: Expense){
        val dateId = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(Date(logDate))
        val expenseId = UUID.randomUUID().toString()

        val expenseRef = tripsRef
            .child(tripId)
            .child("dayLogs")
            .child(dateId)
            .child("expenses")

        expenseRef.child(expenseId).setValue(expense)
            .addOnSuccessListener {
                Log.d("TripsRepo", "Expense added to $dateId!")
            }
            .addOnFailureListener {
                Log.e("TripsRepo", "Failed to add expense", it)
            }
    }

    fun deleteExpense(tripId: String, logDate: Long, expenseId: String) {
        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(logDate))

        val expenseRef = tripsRef
            .child(tripId)
            .child("dayLogs")
            .child(dateId)
            .child("expenses")
            .child(expenseId)

        expenseRef.removeValue()
            .addOnSuccessListener {
                Log.d("TripsRepo", "Expense $expenseId deleted from $dateId!")
            }
            .addOnFailureListener {
                Log.e("TripsRepo", "Failed to delete expense", it)
            }
    }

}